# Relational Algebra Queries & SQL Equivalents (DBMS Unit 2)

This document provides formal Relational Algebra expressions mapped to executable SQL queries for the SaaS Helpdesk System.

---

## 1. Selection ($\sigma$)

### Query 1.1: High or Critical Priority Escalated Tickets
Filters tickets that have breached or are near breaching SLA and require immediate attention.

* **Relational Algebra Expression:**
  $$\sigma_{\text{status} = 'Escalated' \land (\text{priority} = 'High' \lor \text{priority} = 'Critical')}(\text{Tickets})$$

* **Executable SQL:**
  ```sql
  SELECT *
  FROM tickets
  WHERE status = 'Escalated' AND priority IN ('High', 'Critical');
  ```

---

## 2. Projection ($\Pi$)

### Query 2.1: Customer Name and Contact for Overdue Tickets
Eliminates unneeded columns to present a concise view of affected users.

* **Relational Algebra Expression:**
  $$\Pi_{\text{ticket\_id}, \text{title}, \text{name}, \text{email}, \text{subscription\_tier}}(\sigma_{\text{sla\_deadline} < \text{CURRENT\_TIMESTAMP} \land \text{status} \neq 'Resolved'}(\text{Tickets} \bowtie \text{Customers}))$$

* **Executable SQL:**
  ```sql
  SELECT t.ticket_id, t.title, c.name, c.email, c.subscription_tier
  FROM tickets t
  JOIN customers c ON t.customer_id = c.customer_id
  WHERE t.sla_deadline < CURRENT_TIMESTAMP AND t.status != 'Resolved';
  ```

---

## 3. Natural Join & Inner Join ($\bowtie$)

### Query 3.1: Full Escalation History with Agent Context
Binds customer complaints with the exact transfer path between support tiers.

* **Relational Algebra Expression:**
  $$\Pi_{\text{ticket\_id}, \text{customer\_name}, \text{from\_agent}, \text{to\_agent}, \text{reason}, \text{escalated\_at}}(\text{Tickets} \bowtie \text{Customers} \bowtie \text{Escalation\_Logs} \bowtie_{\text{to\_agent\_id} = \text{agent\_id}} \text{Agents})$$

* **Executable SQL:**
  ```sql
  SELECT 
      t.ticket_id,
      c.name AS customer_name,
      c.subscription_tier,
      a_from.name AS from_agent,
      a_to.name AS to_agent,
      a_to.tier_level AS to_tier,
      el.reason,
      el.escalated_at
  FROM escalation_logs el
  JOIN tickets t ON el.ticket_id = t.ticket_id
  JOIN customers c ON t.customer_id = c.customer_id
  LEFT JOIN agents a_from ON el.from_agent_id = a_from.agent_id
  JOIN agents a_to ON el.to_agent_id = a_to.agent_id
  ORDER BY el.escalated_at DESC;
  ```

---

## 4. Renaming ($\rho$) and Self Joins

### Query 4.1: Peer Agents with Lower Workload for Load Balancing
Finds alternative agents within the same tier and specialization with lower workload.

* **Relational Algebra Expression:**
  $$\Pi_{A_1.\text{name}, A_1.\text{current\_load}, A_2.\text{name}, A_2.\text{current\_load}}(\sigma_{A_1.\text{specialization} = A_2.\text{specialization} \land A_1.\text{tier\_level} = A_2.\text{tier\_level} \land A_2.\text{current\_load} < A_1.\text{current\_load}}(\rho_{A_1}(\text{Agents}) \times \rho_{A_2}(\text{Agents})))$$

* **Executable SQL:**
  ```sql
  SELECT 
      a1.name AS busy_agent,
      a1.current_load AS busy_load,
      a2.name AS available_peer,
      a2.current_load AS peer_load,
      a1.specialization,
      a1.tier_level
  FROM agents a1
  JOIN agents a2 ON a1.specialization = a2.specialization 
                AND a1.tier_level = a2.tier_level
                AND a2.current_load < a1.current_load
  WHERE a1.current_load > 3;
  ```

---

## 5. Set Difference ($-$)

### Query 5.1: High-Tier Customers with Zero Support Tickets
Identifies healthy accounts who have not raised any support requests.

* **Relational Algebra Expression:**
  $$\Pi_{\text{customer\_id}, \text{name}}(\sigma_{\text{subscription\_tier} = 'Enterprise'}(\text{Customers})) - \Pi_{\text{customer\_id}, \text{name}}(\text{Customers} \bowtie \text{Tickets})$$

* **Executable SQL:**
  ```sql
  SELECT customer_id, name, email 
  FROM customers 
  WHERE subscription_tier = 'Enterprise'
  EXCEPT
  SELECT c.customer_id, c.name, c.email
  FROM customers c
  JOIN tickets t ON c.customer_id = t.customer_id;
  ```

---

## 6. Aggregate Operations ($\mathcal{G}$)

### Query 6.1: Average Escalation Count and Ticket Volume per Category
Calculates operational bottlenecks across categories.

* **Relational Algebra Expression:**
  $$\text{category} \ \mathcal{G}_{\text{COUNT}(\text{ticket\_id}), \ \text{AVG}(\text{confidence\_score})}(\text{Tickets})$$

* **Executable SQL:**
  ```sql
  SELECT 
      category,
      COUNT(ticket_id) AS total_tickets,
      ROUND(AVG(confidence_score), 3) AS avg_classifier_confidence,
      SUM(CASE WHEN status = 'Escalated' THEN 1 ELSE 0 END) AS total_escalated
  FROM tickets
  GROUP BY category;
  ```
