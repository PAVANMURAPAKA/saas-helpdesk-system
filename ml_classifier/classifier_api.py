"""
SaaS Helpdesk Classifier REST API
Exposes NLP category prediction, confidence scoring, and urgency recommendation.
"""

import os
import re
import json
import joblib
import numpy as np
from flask import Flask, request, jsonify

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "ticket_classifier.joblib")
METRICS_PATH = os.path.join(BASE_DIR, "metrics.json")

app = Flask(__name__)

# Load model pipeline
model_pipeline = None
if os.path.exists(MODEL_PATH):
    model_pipeline = joblib.load(MODEL_PATH)
    print("Classifier model loaded successfully.")
else:
    print("Warning: Model file not found. Please run train_model.py first.")

def calculate_recommended_urgency(text: str, category: str, confidence: float) -> str:
    text_lower = text.lower()
    critical_keywords = [
        "crash", "down", "outage", "production", "data loss", "chargeback", 
        "security breach", "double deduction", "500 internal", "emergency", "fatal"
    ]
    high_keywords = [
        "payment failed", "cannot login", "locked out", "timeout", "latency", 
        "broken", "urgent", "unauthorized", "fail", "error", "sso loop"
    ]

    for kw in critical_keywords:
        if kw in text_lower:
            return "Critical"
            
    for kw in high_keywords:
        if kw in text_lower:
            return "High"

    if category in ["Technical", "Auth_Access"] and confidence > 0.8:
        return "High"
    elif category == "Billing":
        return "Medium"
    elif category == "Bug_Report":
        return "Medium"
    else:
        return "Low"

@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "healthy",
        "model_loaded": model_pipeline is not None,
        "classes": list(model_pipeline.classes_) if model_pipeline else []
    })

@app.route("/classify", methods=["POST"])
def classify():
    global model_pipeline
    if model_pipeline is None:
        if os.path.exists(MODEL_PATH):
            model_pipeline = joblib.load(MODEL_PATH)
        else:
            return jsonify({"error": "Model not trained yet. Run train_model.py"}), 503

    data = request.get_json(force=True, silent=True) or {}
    text = data.get("text", "")
    if not text:
        title = data.get("title", "")
        desc = data.get("description", "")
        text = f"{title}. {desc}".strip()

    if not text:
        return jsonify({"error": "No text or title/description provided"}), 400

    # Prediction & confidence probabilities
    probs = model_pipeline.predict_proba([text])[0]
    best_idx = int(np.argmax(probs))
    category = model_pipeline.classes_[best_idx]
    confidence = float(probs[best_idx])

    # Class probabilities dictionary
    prob_dict = {
        cls: round(float(prob), 4)
        for cls, prob in zip(model_pipeline.classes_, probs)
    }

    urgency = calculate_recommended_urgency(text, category, confidence)

    # Initial routing recommendation (L1 for simple general, L2 for specialized high urgency)
    recommended_tier = "L2" if urgency in ["High", "Critical"] else "L1"

    return jsonify({
        "text": text,
        "category": category,
        "confidence": round(confidence, 4),
        "recommended_urgency": urgency,
        "recommended_tier": recommended_tier,
        "class_probabilities": prob_dict
    })

@app.route("/metrics", methods=["GET"])
def metrics():
    if os.path.exists(METRICS_PATH):
        with open(METRICS_PATH, "r", encoding="utf-8") as f:
            data = json.load(f)
        return jsonify(data)
    return jsonify({"error": "Metrics not found"}), 404

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5005))
    print(f"Starting SaaS Ticket Classifier API on http://127.0.0.1:{port}")
    app.run(host="0.0.0.0", port=port, debug=False)
