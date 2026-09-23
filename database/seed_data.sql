-- ============================================================================
-- SaaS Helpdesk Seed Data
-- ============================================================================

-- 1. Insert Customers
INSERT INTO customers (name, email, company, subscription_tier) VALUES
('Satya Nadella', 'satya@enterprisecloud.com', 'Enterprise Cloud Inc', 'Enterprise'),
('Sundar Pichai', 'sundar@globalsearch.io', 'Global Search Corp', 'Enterprise'),
('Ravi Kumar', 'ravi.k@startupdev.in', 'StartupDev Studio', 'Pro'),
('Ananya Sharma', 'ananya@fintechsecure.com', 'FinTech Secure Labs', 'Pro'),
('Vikram Varma', 'vikram@localfreelance.org', 'Freelance Works', 'Free'),
('Priya Reddy', 'priya@edutechfuture.edu', 'EduTech Innovations', 'Pro'),
('Arjun Mehta', 'arjun@saasautomations.com', 'SaaS Automations', 'Enterprise'),
('Kavita Rao', 'kavita@ecommgrow.com', 'E-Commerce Growth', 'Free');

-- 2. Insert Support Agents (Poset hierarchy: L1 < L2 < L3 < Specialist_Lead)
INSERT INTO agents (name, email, tier_level, specialization, current_load, max_capacity, is_available) VALUES
('Ramesh Patel', 'ramesh.l1@helpdesk.internal', 'L1', 'General', 3, 10, 1),
('Sneha Kulkarni', 'sneha.l1@helpdesk.internal', 'L1', 'General', 5, 10, 1),
('Kiran Kumar', 'kiran.l2@helpdesk.internal', 'L2', 'Technical', 2, 8, 1),
('Deepa Nair', 'deepa.l2@helpdesk.internal', 'L2', 'Billing', 1, 8, 1),
('Manish Joshi', 'manish.l2@helpdesk.internal', 'L2', 'Auth_Access', 4, 8, 1),
('Dr. Arvind Sen', 'arvind.l3@helpdesk.internal', 'L3', 'Technical', 2, 6, 1),
('Lakshmi Narayanan', 'lakshmi.l3@helpdesk.internal', 'L3', 'Billing', 1, 6, 1),
('Venkatesh Iyer', 'venkat.lead@helpdesk.internal', 'Specialist_Lead', 'Technical', 1, 5, 1);

-- 3. Insert Initial Tickets
INSERT INTO tickets (customer_id, title, description, category, priority, status, assigned_agent_id, confidence_score, sla_deadline) VALUES
(1, 'Database connection pool exhausted during peak traffic', 'Our production Kubernetes pods are failing with JDBC connection timeout 504 errors.', 'Technical', 'Critical', 'Assigned', 3, 0.96, datetime('now', '+2 hours')),
(2, 'Enterprise invoice not generated for July cycle', 'Payment was charged through credit card but monthly tax invoice PDF was not emailed or visible on billing dashboard.', 'Billing', 'High', 'Assigned', 4, 0.94, datetime('now', '+4 hours')),
(3, '2FA OTP authentication loop for developer console', 'When signing in with hardware security key or TOTP code, user is redirected back to login screen indefinitely.', 'Auth_Access', 'High', 'Escalated', 5, 0.92, datetime('now', '+1 hours')),
(4, 'CSV export crashes with out-of-memory error', 'Exporting report with more than 100,000 rows causes frontend toast to display internal server error.', 'Bug_Report', 'Medium', 'Assigned', 1, 0.89, datetime('now', '+8 hours')),
(5, 'How to invite new team member to workspace', 'Cannot find the seat management button under account settings page.', 'General', 'Low', 'Resolved', 1, 0.98, datetime('now', '-2 hours')),
(6, 'Chargeback alert received from Stripe webhook', 'Double deduction detected for quarterly SaaS subscription payment ID #TX99102.', 'Billing', 'Critical', 'Escalated', 4, 0.97, datetime('now', '+1 hours'));

-- 4. Insert Escalation Logs
INSERT INTO escalation_logs (ticket_id, from_agent_id, to_agent_id, reason, escalation_level, sla_breached) VALUES
(3, 1, 5, 'L1 triage unable to debug OAuth SAML token response payload. Escalated to L2 Auth specialist.', 'L1_to_L2', 0),
(6, 4, 7, 'Stripe reconciliation dispute requires finance lead authorization with refund limits exceeding $5,000.', 'L2_to_L3', 1);
