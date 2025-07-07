import xml.etree.ElementTree as ET
from pathlib import Path
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
import joblib

# FIX SUGGESTIONS MAPPING
SUGGESTIONS = {
    "DMI_CONSTANT_DB_PASSWORD": "Avoid using hardcoded database passwords. Use environment variables or secure config files.",
    "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE": "Add null checks after method calls that can return null.",
    "OBL_UNSATISFIED_OBLIGATION": "Ensure that all opened resources (files, streams, DB) are properly closed using try-with-resources.",
    "ODR_OPEN_DATABASE_RESOURCE": "Close database resources like ResultSet, Statement, and Connection properly in a finally block or try-with-resources.",
    "REC_CATCH_EXCEPTION": "Avoid catching generic Exception. Catch specific exceptions or rethrow after handling.",
    "RV_RETURN_VALUE_IGNORED": "Always use the return value of methods. Ignoring it may cause logic issues.",
    "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE": "Avoid SQL injection by using PreparedStatements instead of string concatenation in SQL.",
    "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD": "Remove unused public/protected fields or make them private.",
    "VA_FORMAT_STRING_USES_NEWLINE": "Avoid using platform-dependent newline characters in format strings. Use %n instead of \\n."

}

def extract_code_snippet(java_file: Path, start_line: int, end_line: int) -> str:
    if not java_file.exists():
        return ""
    with java_file.open("r", encoding="utf-8", errors="ignore") as f:
        lines = f.readlines()
        return "".join(lines[start_line - 1:end_line]).strip()

def parse_spotbugs(xml_path: Path, source_root: Path) -> pd.DataFrame:
    tree = ET.parse(xml_path)
    root = tree.getroot()

    records = []
    for bug in root.findall("BugInstance"):
        bug_type = bug.get("type")
        if bug_type not in SUGGESTIONS:
            continue  # Skip if no suggestion

        class_elem = bug.find("Class")
        source_line = class_elem.find("SourceLine") if class_elem is not None else None
        if source_line is None:
            continue

        rel_path = source_line.get("sourcepath")
        start = int(source_line.get("start", "0"))
        end = int(source_line.get("end", "0"))

        java_file = source_root / rel_path
        snippet = extract_code_snippet(java_file, start, end)

        records.append({
            "code_snippet": snippet,
            "suggestion": SUGGESTIONS[bug_type]
        })

    return pd.DataFrame(records)

def train_model(df: pd.DataFrame):
    X_train, X_test, y_train, y_test = train_test_split(df["code_snippet"], df["suggestion"], test_size=0.2, random_state=42)
    
    pipeline = Pipeline([
        ("tfidf", TfidfVectorizer(ngram_range=(1, 3))),
        ("clf", LogisticRegression(max_iter=1000))
    ])

    pipeline.fit(X_train, y_train)
    joblib.dump(pipeline, "vuln_suggester.joblib")

    print("✅ Model trained and saved as 'vuln_suggester.joblib'.")

def list_all_bug_types(xml_path: Path):
    tree = ET.parse(xml_path)
    root = tree.getroot()
    found_types = set()

    for bug in root.findall("BugInstance"):
        bug_type = bug.get("type")
        found_types.add(bug_type)

    print("👀 Found bug types in your XML:")
    for bug_type in sorted(found_types):
        print(f"- {bug_type}")


if __name__ == "__main__":
    SPOTBUGS_XML = Path("target/spotbugsXml.xml")
    JAVA_SRC = Path("src/main/java")

    list_all_bug_types(SPOTBUGS_XML)  # <--- Debug here

    print("🔍 Parsing SpotBugs XML...")
    df = parse_spotbugs(SPOTBUGS_XML, JAVA_SRC)

    if df.empty:
        print("⚠️ No valid data found.")
    else:
        df.to_csv("data/vuln_suggestions.csv", index=False)
        print(f"✅ Extracted {len(df)} examples. Training model...")
        train_model(df)
