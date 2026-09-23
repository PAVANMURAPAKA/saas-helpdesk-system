"""
SaaS Helpdesk Ticket Classifier Training Pipeline
Subject Mapping: Python (NLP: TF-IDF Vectorizer + Multinomial Naive Bayes / Calibrated Linear Classifier)
"""

import os
import json
import joblib
import pandas as pd
import numpy as np
from sklearn.model_selection import StratifiedKFold, cross_val_score, train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB, ComplementNB
from sklearn.linear_model import LogisticRegression
from sklearn.calibration import CalibratedClassifierCV
from sklearn.svm import LinearSVC
from sklearn.pipeline import Pipeline
from sklearn.metrics import classification_report, accuracy_score, confusion_matrix

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(BASE_DIR, "dataset.csv")
MODEL_PATH = os.path.join(BASE_DIR, "ticket_classifier.joblib")
METRICS_PATH = os.path.join(BASE_DIR, "metrics.json")

def train_and_evaluate():
    print(f"Loading dataset from: {DATA_PATH}")
    df = pd.read_csv(DATA_PATH)
    print(f"Total samples: {len(df)}")
    print("Class distribution:")
    print(df['category'].value_counts())

    X = df['text']
    y = df['category']

    # 1. Candidate Pipelines
    pipelines = {
        "MultinomialNB": Pipeline([
            ('tfidf', TfidfVectorizer(ngram_range=(1, 2), stop_words='english', sublinear_tf=True)),
            ('clf', MultinomialNB(alpha=0.05))
        ]),
        "ComplementNB": Pipeline([
            ('tfidf', TfidfVectorizer(ngram_range=(1, 2), stop_words='english', sublinear_tf=True)),
            ('clf', ComplementNB(alpha=0.05))
        ]),
        "LogisticRegression": Pipeline([
            ('tfidf', TfidfVectorizer(ngram_range=(1, 2), stop_words='english', sublinear_tf=True)),
            ('clf', LogisticRegression(C=10.0, class_weight='balanced', max_iter=1000))
        ]),
        "CalibratedLinearSVC": Pipeline([
            ('tfidf', TfidfVectorizer(ngram_range=(1, 2), stop_words='english', sublinear_tf=True)),
            ('clf', CalibratedClassifierCV(LinearSVC(C=1.5, random_state=42)))
        ])
    }

    # Evaluate all models with 5-fold stratified cross-validation
    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
    best_name = None
    best_score = -1.0

    print("\n--- 5-Fold Stratified Cross-Validation Benchmark ---")
    for name, pipe in pipelines.items():
        scores = cross_val_score(pipe, X, y, cv=cv, scoring='accuracy')
        mean_s, std_s = scores.mean(), scores.std()
        print(f"  * {name:<20}: Mean Acc = {mean_s:.4f} (+/- {std_s:.4f})")
        if mean_s > best_score:
            best_score = mean_s
            best_name = name

    print(f"\nWinning Model Architecture: {best_name} with {best_score:.2%} CV Accuracy")
    selected_pipeline = pipelines[best_name]

    # Split for detailed holdout evaluation metrics
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, random_state=42, stratify=y
    )
    selected_pipeline.fit(X_train, y_train)
    y_pred = selected_pipeline.predict(X_test)
    test_acc = accuracy_score(y_test, y_pred)
    report = classification_report(y_test, y_pred, output_dict=True, zero_division=0)

    print("\n--- Holdout Test Set Evaluation Report ---")
    print(classification_report(y_test, y_pred, zero_division=0))

    # Fit best model on complete dataset for production persistence
    print("Fitting selected pipeline on 100% dataset for production deployment...")
    selected_pipeline.fit(X, y)
    joblib.dump(selected_pipeline, MODEL_PATH)
    print(f"Model saved to: {MODEL_PATH}")

    # Export metrics for presentation and viva
    metrics_summary = {
        "model_architecture": best_name,
        "feature_extraction": "TF-IDF Vectorizer (ngram_range=(1,2), sublinear_tf=True)",
        "total_training_records": len(df),
        "classes": sorted(list(df['category'].unique())),
        "cross_val_accuracy_mean": round(float(best_score), 4),
        "test_accuracy": round(float(test_acc), 4),
        "classification_report": report
    }

    with open(METRICS_PATH, "w", encoding="utf-8") as f:
        json.dump(metrics_summary, f, indent=4)
    print(f"Metrics exported to: {METRICS_PATH}")

    # Test sample verification
    test_prompts = [
        "Refund needed because Stripe deducted subscription twice for July",
        "Kubernetes pod memory leak causing 500 internal server error",
        "OAuth SSO loop with Okta SAML response error and invalid token",
        "Export CSV button is unclickable and broken on reporting table",
        "Where can I find the API documentation and schedule onboarding training?"
    ]

    print("\n--- Verification Sample Predictions ---")
    for q in test_prompts:
        probs = selected_pipeline.predict_proba([q])[0]
        pred_idx = np.argmax(probs)
        pred_class = selected_pipeline.classes_[pred_idx]
        confidence = probs[pred_idx]
        print(f"Query: '{q}'\n  -> Predicted: [{pred_class}] (Confidence: {confidence:.2%})")

if __name__ == "__main__":
    train_and_evaluate()
