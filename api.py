from fastapi import FastAPI, Request
from pydantic import BaseModel
import joblib

app = FastAPI()

model = joblib.load("combined_model.joblib")

class Snippet(BaseModel):
    code: str

@app.post("/suggest")
def get_suggestion(data: Snippet):
    prediction = model.predict([data.code])[0]
    return {"suggestion": prediction}
