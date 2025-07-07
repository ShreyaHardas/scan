from fastapi import FastAPI
from pydantic import BaseModel
import cloudpickle
import numpy as np
from gensim.corpora import Dictionary
from gensim.models import TfidfModel

app = FastAPI()

# Load model and vectorizer
with open("combined_model.pkl", "rb") as f:
    model = cloudpickle.load(f)

with open("combined_vectorizer.pkl", "rb") as f:
    dictionary, tfidf_model, y_labels = cloudpickle.load(f)

# Request model
class PredictionRequest(BaseModel):
    message: str

# Tokenizer
def tokenize(text):
    return text.lower().split()

# Predict function
def predict_message(message):
    tokens = tokenize(message)
    bow = dictionary.doc2bow(tokens)
    tfidf = tfidf_model[bow]
    vec = np.zeros(len(dictionary))
    for idx, val in tfidf:
        vec[idx] = val
    label_index = model.predict(vec.reshape(1, -1))[0]
    return y_labels[label_index]

@app.post("/predict")
def predict(req: PredictionRequest):
    suggestion = predict_message(req.message)
    return {"suggestion": suggestion}
