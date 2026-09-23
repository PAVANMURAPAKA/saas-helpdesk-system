"""
Full-Stack SaaS Helpdesk Server
Integrates:
1. Static web dashboard hosting (HTML5, Vanilla CSS, JS)
2. REST API for Ticket CRUD and Customer context binding (DBMS)
3. Python NLP Classifier inference (/classify)
4. Relational Algebra SQL execution (/api/query/<id>)
"""

import os
import json
import sqlite3
import joblib
import numpy as np
from datetime import datetime, timedelta
from flask import Flask, request, jsonify, send_from_directory

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(BASE_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "database", "helpdesk.db")
MODEL_PATH = os.path.join(PROJECT_ROOT, "ml_classifier", "ticket_classifier.joblib")

app = Flask(__name__, static_folder=BASE_DIR)

# Load ML pipeline
nlp_pipeline = None
if os.path.exists(MODEL_PATH):
    try:
        nlp_pipeline = joblib.load(MODEL_PATH)
        print("NLP Classifier model loaded successfully.")
    except Exception as e:
        print(f"Error loading model: {e}")

def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

# Static Web Dashboard Routes
@app.route("/")
def index():
    return send_from_directory(BASE_DIR, "index.html")

@app.route("/<path:filename>")
def static_files(filename):
    return send_from_directory(BASE_DIR, filename)

# NLP Classification Endpoint
@app.route("/classify", methods=["POST"])
def classify():
    data = request.get_json(force=True, silent=True) or {}
    title = data.get("title", "")
    desc = data.get("description", "")
    text = data.get("text", f"{title}. {desc}".strip())

    if not text:
        return jsonify({"error": "No text provided"}), 400

    if nlp_pipeline is not None:
        probs = nlp_pipeline.predict_proba([text])[0]
        best_idx = int(np.argmax(probs))
        category = nlp_pipeline.classes_[best_idx]
        confidence = float(probs[best_idx])
        prob_dict = {
            cls: round(float(prob), 4)
            for cls, prob in zip(nlp_pipeline.classes_, probs)
        }
    else:
        category = "Technical"
        confidence = 0.85
        prob_dict = {"Technical": 0.85, "Billing": 0.05, "Auth_Access": 0.05, "Bug_Report": 0.03, "General": 0.02}

    # Urgency & SLA mapping
    text_lower = text.lower()
    if any(k in text_lower for k in ["deadlock", "500", "crash", "down", "outage", "double deduction", "chargeback"]):
        urgency = "Critical"
    elif any(k in text_lower for k in ["failed", "timeout", "latency", "error", "sso", "2fa"]):
        urgency = "High"
    elif category in ["Billing", "Bug_Report"]:
        urgency = "Medium"
    else:
        urgency = "Low"

    return jsonify({
        "category": category,
        "confidence": round(confidence, 4),
        "recommended_urgency": urgency,
        "class_probabilities": prob_dict
    })

# DBMS Customer & Ticket Endpoints
@app.route("/api/customers", methods=["GET"])
def get_customers():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT customer_id, name, email, company, subscription_tier FROM customers ORDER BY customer_id;")
    rows = [dict(r) for r in cursor.fetchall()]
    conn.close()
    return jsonify(rows)

@app.route("/api/tickets", methods=["GET"])
def get_tickets():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        SELECT t.ticket_id, t.title, t.category, t.priority, t.status, t.created_at,
               c.name AS customer_name, c.subscription_tier,
               a.name AS agent_name
        FROM tickets t
        JOIN customers c ON t.customer_id = c.customer_id
        LEFT JOIN agents a ON t.assigned_agent_id = a.agent_id
        ORDER BY t.ticket_id DESC;
    """)
    rows = [dict(r) for r in cursor.fetchall()]
    conn.close()
    return jsonify(rows)

@app.route("/api/ticket/<int:ticket_id>", methods=["GET"])
def get_ticket(ticket_id):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        SELECT t.*, c.name AS customer_name, c.subscription_tier, a.name AS agent_name
        FROM tickets t
        JOIN customers c ON t.customer_id = c.customer_id
        LEFT JOIN agents a ON t.assigned_agent_id = a.agent_id
        WHERE t.ticket_id = ?;
    """, (ticket_id,))
    row = cursor.fetchone()
    conn.close()
    if row:
        return jsonify(dict(row))
    return jsonify({"error": "Ticket not found"}), 404

@app.route("/api/ticket/create", methods=["POST"])
def create_ticket():
    data = request.get_json(force=True, silent=True) or {}
    cust_id = data.get("customer_id", 1)
    title = data.get("title", "Untitled Ticket")
    description = data.get("description", "")
    category = data.get("category", "General")
    priority = data.get("priority", "Medium")
    confidence = data.get("confidence_score", 0.85)

    sla_hours = 2 if priority == "Critical" else 8
    sla_deadline = (datetime.now() + timedelta(hours=sla_hours)).strftime("%Y-%m-%d %H:%M:%S")

    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO tickets (customer_id, title, description, category, priority, status, assigned_agent_id, confidence_score, sla_deadline)
        VALUES (?, ?, ?, ?, ?, 'Assigned', 1, ?, ?);
    """, (cust_id, title, description, category, priority, confidence, sla_deadline))
    ticket_id = cursor.lastrowid
    conn.commit()

    cursor.execute("""
        SELECT t.*, c.name AS customer_name, c.subscription_tier, a.name AS agent_name
        FROM tickets t
        JOIN customers c ON t.customer_id = c.customer_id
        LEFT JOIN agents a ON t.assigned_agent_id = a.agent_id
        WHERE t.ticket_id = ?;
    """, (ticket_id,))
    created_row = dict(cursor.fetchone())
    conn.close()
    return jsonify(created_row)

# Relational Algebra Query Execution Endpoint
@app.route("/api/query/<qid>", methods=["GET"])
def execute_algebra_query(qid):
    conn = get_db()
    cursor = conn.cursor()

    if qid == "q1":
        query = "SELECT ticket_id, title, category, priority, status FROM tickets WHERE status = 'Escalated' AND priority IN ('High', 'Critical');"
    elif qid == "q2":
        query = "SELECT t.ticket_id, t.title, c.name, c.subscription_tier FROM tickets t JOIN customers c ON t.customer_id = c.customer_id;"
    elif qid == "q3":
        query = "SELECT el.ticket_id, el.reason, a.name AS to_agent FROM escalation_logs el JOIN agents a ON el.to_agent_id = a.agent_id;"
    else:
        conn.close()
        return jsonify({"error": "Unknown query ID"}), 400

    cursor.execute(query)
    columns = [col[0] for col in cursor.description]
    rows = cursor.fetchall()
    row_data = [list(r) for r in rows]
    conn.close()

    return jsonify({"columns": columns, "rows": row_data})

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8080))
    print(f"================================================================================")
    print(f"  SAAS HELPDESK AI DASHBOARD SERVER RUNNING AT: http://127.0.0.1:{port}")
    print(f"================================================================================")
    app.run(host="0.0.0.0", port=port, debug=False)
