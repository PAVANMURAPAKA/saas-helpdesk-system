# ResolveDesk AI: Intelligent SaaS Helpdesk Ticket Resolution & Escalation Platform

[![Python 3.10+](https://img.shields.io/badge/Python-3.10%2B-blue.svg)](https://www.python.org/)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![SQLite](https://img.shields.io/badge/DBMS-SQLite%203NF-003B57.svg)](https://www.sqlite.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

An enterprise-grade, multi-disciplinary software engineering framework solving the critical SaaS helpdesk breakdown:
> *"A SaaS helpdesk's tickets aren't linked properly to customers or escalation history, delaying resolution."*

---

## 🎯 Key Capabilities

* 🧠 **Machine Learning Ticket Triage (Python NLP):** Sublinear TF-IDF vectorization with Calibrated Classifiers (87%+ test accuracy) auto-categorizing tickets into Technical, Billing, Auth/Access, Bug Reports, and General queries.
* 🗄️ **Zero-Context-Loss Relational Storage (DBMS):** Third Normal Form (3NF) SQLite/PostgreSQL schema with strict foreign-key cascading rules (`ON DELETE CASCADE`) permanently binding customer contracts to ticket histories.
* 🕸️ **Dynamic Congestion Routing (ADSA):** Directed Acyclic Graph (DAG) support hierarchy utilizing **Dijkstra's Shortest Path Algorithm** with dynamic queue penalty weights:
  $$\text{Weight}(u, v) = \text{BaseLatency}(u, v) \times \left(1.0 + 1.5 \times \frac{\text{CurrentLoad}(v)}{\text{MaxCapacity}(v)}\right)$$
* 📐 **Formal Discrete Foundations (DMGT):** Verified Partial Order Set (Poset) hierarchy ($L1 \le L2 \le L3 \le \text{Lead}$) proving Reflexive, Antisymmetric, and Transitive properties, and mutually disjoint equivalence class ticket partitioning.
* ☕ **Object-Oriented Architecture (OOPJ Java):** Encapsulation, concrete inheritance trees, and dynamic runtime polymorphism through strategy resolution dispatchers.
* 💻 **Interactive Glassmorphism Web Dashboard:** Production-ready real-time platform with animated SVG graphs, live NLP inference, relational algebra query runners, and support agent rosters.

---

## 🏛️ Academic Curriculum Integration (5 Subjects)

| Subject | Core Syllabus Concept | Project Implementation |
| :--- | :--- | :--- |
| **DBMS** | ER Modeling (Unit 1) &amp; Relational Algebra (Unit 2) | [database/schema.sql](database/schema.sql), [database/relational_algebra_queries.md](database/relational_algebra_queries.md) |
| **DMGT** | Poset Hierarchy &amp; Equivalence Relations (Unit 2) | [graph_engine/dmgt_verifier.py](graph_engine/dmgt_verifier.py) |
| **ADSA** | Directed Graph Routing &amp; Dijkstra Algorithm (Unit 2) | [graph_engine/escalation_graph.py](graph_engine/escalation_graph.py), [java_app/.../EscalationGraph.java](java_app/src/main/java/com/helpdesk/graph/EscalationGraph.java) |
| **OOPJ** | Encapsulation, Inheritance, Polymorphism via Java | [java_app/src/main/java/com/helpdesk/](java_app/src/main/java/com/helpdesk/) |
| **Python** | NLP Preprocessing, TF-IDF Vectorizer, REST Microservice | [ml_classifier/train_model.py](ml_classifier/train_model.py), [ml_classifier/classifier_api.py](ml_classifier/classifier_api.py) |

---

## 📁 Repository Structure

```
├── database/
│   ├── schema.sql                   # 3NF relational schema with Foreign Keys
│   ├── seed_data.sql                 # Sample enterprise customers, agents, tickets
│   ├── relational_algebra_queries.md # σ, π, ⋈, ρ, - mathematical queries & SQL
│   ├── init_db.py                   # Automated database builder
│   └── helpdesk.db                  # Populated SQLite database
│
├── ml_classifier/
│   ├── dataset.csv                  # 92+ domain-specific SaaS support records
│   ├── train_model.py               # NLP Pipeline training & benchmark script
│   ├── ticket_classifier.joblib     # Serialized machine learning pipeline
│   ├── metrics.json                 # Cross-validation & holdout evaluation report
│   ├── classifier_api.py            # Standalone Flask REST API (/classify)
│   └── requirements.txt             # Python requirements
│
├── graph_engine/
│   ├── escalation_graph.py          # ADSA Dijkstra routing with dynamic queue load
│   └── dmgt_verifier.py             # Poset (Reflexive, Antisymmetric, Transitive) proofs
│
├── java_app/
│   ├── pom.xml                      # Maven project configuration
│   ├── src/main/java/com/helpdesk/  # Core Java domain models, agents & services
│   └── src/test/java/com/helpdesk/  # Unit and integration test suite
│
├── web_dashboard/                   # Production-grade web interface
│   ├── index.html                   # Master UI featuring Simulator, Flowchart, Roster, ADSA Canvas
│   ├── styles.css                   # Dark theme glassmorphic styling
│   ├── app.js                       # SVG graph renderer & client controller
│   └── server.py                    # Unified server on http://127.0.0.1:8080
│
├── docs/
│   ├── ER_DIAGRAM.md                # Mermaid ER diagram & 3NF justification
│   ├── ACADEMIC_MAPPING.md          # 5-Subject academic curriculum mapping
│   └── PRESENTATION_GUIDE.md        # 12-Slide PPT blueprint & Viva Voce Q&A
│
└── run_simulation.py                # Standalone end-to-end Python orchestrator
```

---

## 🚀 Quick Start Guide

### 1. Clone & Set Up Environment
```bash
git clone <your-repo-url>
cd saas-helpdesk-system
python -m pip install -r ml_classifier/requirements.txt
```

### 2. Launch the Web Platform
```bash
python web_dashboard/server.py
```
Open **[http://127.0.0.1:8080](http://127.0.0.1:8080)** in any browser.

### 3. Run the Terminal Simulation
```bash
python run_simulation.py
```

### 4. Compile and Run the Java Engine
```bash
cd java_app
javac -d bin src/main/java/com/helpdesk/model/*.java src/main/java/com/helpdesk/agent/*.java src/main/java/com/helpdesk/graph/*.java src/main/java/com/helpdesk/service/*.java src/main/java/com/helpdesk/Main.java
java -cp bin com.helpdesk.Main
```

---

## 👥 Support Engineers & Specialist Roster

* **Ramesh Patel** (`L1 Triage Lead` · General Frontline · 96.4% Resolution Rate)
* **Sneha Kulkarni** (`L1 Support Agent` · Customer Success · 95.1% Resolution Rate)
* **Kiran Kumar** (`L2 Tech Specialist` · Postgres, Redis, Deadlocks)
* **Deepa Nair** (`L2 Billing Lead` · Stripe Reconciliation, Tax, Invoicing)
* **Manish Joshi** (`L2 Auth Security` · Okta, SAML 2.0, 2FA)
* **Dr. Arvind Sen** (`L3 Senior Staff` · Kernel, Kubernetes, Distributed Deadlocks)
* **Lakshmi Narayanan** (`L3 Finance Lead` · High-Value Wire Transfers &gt; $50k)
* **Venkatesh Iyer** (`Specialist Lead Architect` · Top Cover Poset Element)

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
