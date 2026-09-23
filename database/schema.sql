-- ============================================================================
-- SaaS Helpdesk Ticket Resolution & Escalation Management System
-- Subject Mapping: DBMS (Unit 1: ER Modeling, Constraints, Normalization 3NF)
-- Database Engine: SQLite / PostgreSQL compatible
-- ============================================================================

PRAGMA foreign_keys = ON;

-- 1. Customers Table
-- Stores customer identity, contact info, and subscription SLA level
CREATE TABLE IF NOT EXISTS customers (
    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    company VARCHAR(100),
    subscription_tier VARCHAR(20) NOT NULL CHECK (subscription_tier IN ('Free', 'Pro', 'Enterprise')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Support Agents Table
-- Models human agents with hierarchy level, technical domain, and active load
CREATE TABLE IF NOT EXISTS agents (
    agent_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    tier_level VARCHAR(20) NOT NULL CHECK (tier_level IN ('L1', 'L2', 'L3', 'Specialist_Lead')),
    specialization VARCHAR(50) NOT NULL CHECK (specialization IN ('Billing', 'Technical', 'Auth_Access', 'Bug_Report', 'General')),
    current_load INTEGER DEFAULT 0 CHECK (current_load >= 0),
    max_capacity INTEGER DEFAULT 10 CHECK (max_capacity > 0),
    is_available BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tickets Table
-- Central entity linking customer history, auto-classified category, and assigned agent
CREATE TABLE IF NOT EXISTS tickets (
    ticket_id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('Billing', 'Technical', 'Auth_Access', 'Bug_Report', 'General')),
    priority VARCHAR(20) NOT NULL DEFAULT 'Medium' CHECK (priority IN ('Low', 'Medium', 'High', 'Critical')),
    status VARCHAR(20) NOT NULL DEFAULT 'Open' CHECK (status IN ('Open', 'Assigned', 'In_Progress', 'Escalated', 'Resolved', 'Closed')),
    assigned_agent_id INTEGER,
    confidence_score REAL DEFAULT 0.0,
    sla_deadline TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_agent_id) REFERENCES agents(agent_id) ON DELETE SET NULL
);

-- 4. Escalation Logs Table
-- Complete audit trail for ticket escalations, maintaining full context transfer
CREATE TABLE IF NOT EXISTS escalation_logs (
    escalation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    ticket_id INTEGER NOT NULL,
    from_agent_id INTEGER,
    to_agent_id INTEGER NOT NULL,
    reason TEXT NOT NULL,
    escalation_level VARCHAR(20) NOT NULL CHECK (escalation_level IN ('L1_to_L2', 'L2_to_L3', 'L3_to_Lead', 'Direct_Specialist')),
    sla_breached BOOLEAN DEFAULT 0,
    escalated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id) ON DELETE CASCADE,
    FOREIGN KEY (from_agent_id) REFERENCES agents(agent_id) ON DELETE SET NULL,
    FOREIGN KEY (to_agent_id) REFERENCES agents(agent_id) ON DELETE CASCADE
);

-- Indexes for high-frequency relational join & escalation lookups
CREATE INDEX IF NOT EXISTS idx_tickets_customer ON tickets(customer_id);
CREATE INDEX IF NOT EXISTS idx_tickets_status_priority ON tickets(status, priority);
CREATE INDEX IF NOT EXISTS idx_tickets_agent ON tickets(assigned_agent_id);
CREATE INDEX IF NOT EXISTS idx_escalation_ticket ON escalation_logs(ticket_id);
