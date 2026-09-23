# Comprehensive Academic Curriculum Mapping (5 Core Subjects)

This document provides academic mapping showing exactly how the **Intelligent SaaS Helpdesk Ticket Resolution & Escalation Management System** fulfills the curricular requirements of:
1. **DBMS (Database Management Systems)**
2. **DMGT (Discrete Mathematics & Graph Theory)**
3. **ADSA (Advanced Data Structures & Algorithms)**
4. **OOPJ (Object-Oriented Programming via Java)**
5. **Python (Machine Learning & NLP)**

---

## 1. DBMS (Unit 1 & Unit 2)

### Unit 1: Entity-Relationship (ER) Modeling & Referential Integrity
- **Artifact:** [`database/schema.sql`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/database/schema.sql), [`docs/ER_DIAGRAM.md`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/docs/ER_DIAGRAM.md)
- **Concept:** Solves orphaned tickets by establishing mandatory 1-to-many relationships:
  $$\text{Customer} \xrightarrow{1:N} \text{Ticket} \xrightarrow{1:N} \text{Escalation\_Log}$$
- **Foreign Key Constraints:**
  - `Tickets.customer_id REFERENCES Customers(customer_id) ON DELETE CASCADE`
  - `Escalation_Logs.ticket_id REFERENCES Tickets(ticket_id) ON DELETE CASCADE`
  - `Escalation_Logs.from_agent_id / to_agent_id REFERENCES Agents(agent_id)`

### Unit 2: Relational Algebra & Query Optimization
- **Artifact:** [`database/relational_algebra_queries.md`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/database/relational_algebra_queries.md)
- **Mathematical Formulations:**
  - **Selection ($\sigma$):** Filter overdue or critical tickets:
    $$\sigma_{\text{status} = 'Escalated' \land (\text{priority} = 'High' \lor \text{priority} = 'Critical')}(\text{Tickets})$$
  - **Projection ($\Pi$):** Extract relevant notification attributes:
    $$\Pi_{\text{ticket\_id}, \text{title}, \text{name}, \text{email}}(\text{Tickets} \bowtie \text{Customers})$$
  - **Natural Join ($\bowtie$):** Multi-way relational composition across Tickets, Customers, and Escalation Logs.
  - **Renaming ($\rho$):** Self-join on Agents to balance workloads across peers of the same tier.

---

## 2. DMGT (Discrete Mathematics & Graph Theory - Unit 2)

### Partial Order Sets (Poset) on Support Tiers
- **Artifact:** [`graph_engine/dmgt_verifier.py`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/graph_engine/dmgt_verifier.py)
- **Domain:** $S = \{\text{L1}, \text{L2}, \text{L3}, \text{Specialist\_Lead}\}$
- **Relation $R$ (Escalation Hierarchy $\le$):** $(a, b) \in R \iff \text{Rank}(a) \le \text{Rank}(b)$
- **Mathematical Proofs Verified:**
  1. **Reflexive:** $\forall a \in S, (a, a) \in R$ (Every agent tier is at least at its own level).
  2. **Antisymmetric:** $\forall a, b \in S, ((a, b) \in R \land (b, a) \in R) \implies a = b$ (No circular promotions).
  3. **Transitive:** $\forall a, b, c \in S, ((a, b) \in R \land (b, c) \in R) \implies (a, c) \in R$ ($\text{L1} \le \text{L2} \le \text{L3} \implies \text{L1} \le \text{L3}$).
- **Hasse Diagram:**
  $$\text{L1} \longrightarrow \text{L2} \longrightarrow \text{L3} \longrightarrow \text{Specialist\_Lead}$$

### Equivalence Relations on Ticket Partitioning
- **Relation $\sim$:** $T_i \sim T_j \iff \text{Category}(T_i) = \text{Category}(T_j)$
- **Properties:** Reflexive, Symmetric, and Transitive.
- **Equivalence Classes:** $[T_{\text{Billing}}], [T_{\text{Technical}}], [T_{\text{Auth\_Access}}], [T_{\text{Bug\_Report}}], [T_{\text{General}}]$
- **Partition Proof:** Classes are mutually pairwise disjoint ($\cap [T_k] = \emptyset$) and their union exhaustively reconstructs the universal ticket set ($\cup [T_k] = U$).

---

## 3. ADSA (Advanced Data Structures & Algorithms - Unit 2)

### Shortest Path Routing via Dijkstra's Algorithm
- **Artifact:** [`graph_engine/escalation_graph.py`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/graph_engine/escalation_graph.py), [`java_app/src/main/java/com/helpdesk/graph/EscalationGraph.java`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/java_app/src/main/java/com/helpdesk/graph/EscalationGraph.java)
- **Graph Topology:** Directed Acyclic Graph (DAG) where $V = \text{Support Agents}$, $E = \text{Allowed Escalation Pathways}$.
- **Dynamic Congestion Weight Formula:**
  $$\text{Weight}(u, v) = \text{BaseLatency}(u, v) \times \left(1.0 + 1.5 \times \frac{\text{CurrentLoad}(v)}{\text{MaxCapacity}(v)}\right)$$
- **Algorithmic Complexity:**
  - Using Min-Heap Priority Queue: $\mathcal{O}((V + E) \log V)$ time and $\mathcal{O}(V + E)$ space.
- **Topological Acyclic Validation:** Kahn's in-degree zero elimination verifies absence of cycles ($\mathcal{O}(V + E)$).

---

## 4. OOPJ (Object-Oriented Programming via Java)

### Four Pillars of OOP Demonstration
- **Artifact:** [`java_app/src/main/java/com/helpdesk/`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/java_app/src/main/java/com/helpdesk/)
1. **Encapsulation:**
   - Private fields in [`Customer.java`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/java_app/src/main/java/com/helpdesk/model/Customer.java), [`Ticket.java`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/java_app/src/main/java/com/helpdesk/model/Ticket.java), [`Agent.java`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/java_app/src/main/java/com/helpdesk/agent/Agent.java) with guarded getters/setters and invariant validation (`canAcceptTicket()`).
2. **Inheritance:**
   - Subclasses [`L1SupportAgent`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/java_app/src/main/java/com/helpdesk/agent/L1SupportAgent.java), [`BillingSpecialistAgent`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/java_app/src/main/java/com/helpdesk/agent/BillingSpecialistAgent.java), [`TechnicalLeadAgent`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/java_app/src/main/java/com/helpdesk/agent/TechnicalLeadAgent.java), [`SecurityAuthAgent`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/java_app/src/main/java/com/helpdesk/agent/SecurityAuthAgent.java) extend base `Agent`.
3. **Polymorphism (Runtime Dynamic Method Dispatch):**
   - Abstract method `resolveTicket(Ticket ticket)` in `Agent` is dynamically resolved at runtime depending on the concrete subclass assigned to the ticket.
4. **Abstraction:**
   - [`HelpdeskService`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/java_app/src/main/java/com/helpdesk/service/HelpdeskService.java) shields consumers from graph navigation, NLP network calls, and database operations.

---

## 5. Python (Machine Learning & NLP)

### Real-Time Text Classification Pipeline
- **Artifact:** [`ml_classifier/train_model.py`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/ml_classifier/train_model.py), [`ml_classifier/classifier_api.py`](file:///c:/Users/pavan/OneDrive/Desktop/New%20folder/ml_classifier/classifier_api.py)
- **Feature Extraction:** $\text{TF-IDF}(t, d, D) = \text{TF}(t, d) \times \log\left(\frac{1 + |D|}{1 + \text{DF}(t, D)}\right) + 1$ with n-gram range $(1, 2)$ and sublinear term frequency scaling.
- **Model Architecture:** Calibrated Support Vector Classifier / Multinomial Naive Bayes evaluated with 5-fold stratified cross-validation.
- **Serving Architecture:** Lightweight Flask REST API exposing `/classify` with confidence probabilities and urgency calculations.
