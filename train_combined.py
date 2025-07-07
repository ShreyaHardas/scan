import xml.etree.ElementTree as ET
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
import joblib

# ------------ 🔎 PMD PARSER ------------
def parse_pmd(xml_file):
    tree = ET.parse(xml_file)
    root = tree.getroot()
    data = []
    rules = set()  # To collect unique PMD rule names

    # --find bug types first

    for file in root.findall('file'):
        for violation in file.findall('violation'):
            rule = violation.get('rule')
            if rule:
                rules.add(rule)
    print("📘 PMD Rules Found:")
    for rule in sorted(rules):
        print("-", rule)
     # --find bug types first end

    for file in root.findall('file'):
        for violation in file.findall('violation'):
            rule = violation.get('rule')
            msg = violation.text.strip() if violation.text else ""
            suggestion = get_pmd_suggestion(rule)
            data.append({
                "tool": "PMD",
                "message": msg,
                "rule": rule,
                "suggestion": suggestion
            })

    return pd.DataFrame(data)

def get_pmd_suggestion(rule):
    fixes = {
        "AvoidPrintStackTrace": "Use a logger instead of printStackTrace().",
        "UnusedLocalVariable": "Remove unused local variables.",
        "EmptyCatchBlock": "Log or handle the exception inside the catch block.",
    }
    return fixes.get(rule, "No suggestion available.")


# ------------ 🔍 SPOTBUGS PARSER ------------
def parse_spotbugs(xml_file):
    tree = ET.parse(xml_file)
    root = tree.getroot()
    data = []
    bug_types = set()

    for bug_instance in root.findall("BugInstance"):
        bug_type = bug_instance.get("type")
        if bug_type:
            bug_types.add(bug_type)
        msg = bug_instance.findtext("LongMessage") or bug_instance.findtext("ShortMessage")
        msg = msg.strip() if msg else ""
        suggestion = get_spotbugs_suggestion(bug_type)
        data.append({
            "tool": "SpotBugs",
            "message": msg,
            "rule": bug_type,
            "suggestion": suggestion
        })

    print("🐞 SpotBugs Bug Types Found:")
    for bug in sorted(bug_types):
        print("-", bug)

    return pd.DataFrame(data)

def get_spotbugs_suggestion(bug_type):
    fixes = {
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
    return fixes.get(bug_type, "No suggestion available.")


# ------------ 🤖 TRAINING ------------
def train_combined_model(df):
    vectorizer = TfidfVectorizer()
    X = vectorizer.fit_transform(df["message"])
    y = df["suggestion"]

    model = LogisticRegression(max_iter=1000)
    model.fit(X, y)

    joblib.dump(model, "combined_model.joblib")
    joblib.dump(vectorizer, "combined_vectorizer.joblib")

    print("✅ Model and vectorizer saved.")
    return model, vectorizer


# ------------ 🚀 MAIN ------------
def main():
    print("📦 Loading and merging PMD + SpotBugs data...")
    from pathlib import Path

    pmd_path = Path("target") / "pmd.xml"
    pmd_spotbugs_path = Path("target") / "spotbugsXml.xml"

    print(f"🔍 Looking for PMD file at: {pmd_path}")


    df_pmd = parse_pmd(pmd_path)
    df_spot = parse_spotbugs(pmd_spotbugs_path)

    combined_df = pd.concat([df_pmd, df_spot], ignore_index=True)
    print(f"🔍 Loaded {len(combined_df)} combined samples.")

    train_combined_model(combined_df)


if __name__ == "__main__":
    main()
