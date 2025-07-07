# serve_model.py
from fastapi import FastAPI, Request
from pydantic import BaseModel
import joblib

# Load model and vectorizer
model = joblib.load("combined_model.joblib")
vectorizer = joblib.load("combined_vectorizer.joblib")

app = FastAPI()

class CodeInput(BaseModel):
    code: str

@app.post("/predict")
def predict(input_data: CodeInput):
    vector = vectorizer.transform([input_data.code])
    prediction = model.predict(vector)[0]
    suggestions = {
        "HARD_CODED_PASSWORD": "Use env variables or secure config files.",
        "SQL_INJECTION": "Use PreparedStatement to avoid SQL injection.",
        "INSECURE_RANDOM": "Use SecureRandom for security-sensitive operations.",
        "DEFAULT": "No suggestion available."
    }
    return {
        "prediction": prediction,
        "suggestion": suggestions.get(prediction, suggestions["DEFAULT"])
    }

joblib.dump(model, "combined_model.joblib")
joblib.dump(vectorizer, "combined_vectorizer.joblib")
