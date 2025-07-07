import joblib

model = joblib.load("vuln_suggester.joblib")

test_code = "Statement stmt = conn.createStatement();"
print("Prediction:", model.predict([test_code])[0])
