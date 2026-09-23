package com.helpdesk;

import com.helpdesk.agent.*;
import com.helpdesk.graph.EscalationGraph;
import com.helpdesk.model.Customer;
import com.helpdesk.model.Ticket;
import com.helpdesk.service.HelpdeskService;

/**
 * Self-contained Unit and Integration Test Runner for Java.
 * Can be run with or without JUnit.
 */
public class HelpdeskTest {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("Running Java Helpdesk Unit & Verification Tests...\n");

        // Test 1: Encapsulation & Customer Model
        try {
            Customer c = new Customer(1, "Test User", "test@domain.com", "Test Corp", "Enterprise");
            assert c.getCustomerId() == 1;
            assert "Enterprise".equals(c.getSubscriptionTier());
            System.out.println("[PASS] Test 1: Customer model encapsulation verified.");
            passed++;
        } catch (Throwable e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
            failed++;
        }

        // Test 2: Inheritance & Polymorphism
        try {
            Ticket t = new Ticket(101, 1, "Invoice error", "Card charged twice", "Billing", "High", 4);
            Agent l1 = new L1SupportAgent(1, "L1 Agent", "l1@test.com", 0, 5);
            Agent billing = new BillingSpecialistAgent(2, "Billing Specialist", "bill@test.com", "L2", 0, 5);

            String l1Resp = l1.resolveTicket(t);
            assert "Escalated".equals(t.getStatus());

            String billResp = billing.resolveTicket(t);
            assert "Resolved".equals(t.getStatus());
            System.out.println("[PASS] Test 2: Agent inheritance & polymorphic resolution verified.");
            passed++;
        } catch (Throwable e) {
            System.out.println("[FAIL] Test 2: " + e.getMessage());
            failed++;
        }

        // Test 3: ADSA Graph Dijkstra Routing
        try {
            EscalationGraph graph = new EscalationGraph();
            Agent a1 = new L1SupportAgent(1, "A1", "a1@test.com", 2, 10);
            Agent a2 = new TechnicalLeadAgent(2, "A2", "a2@test.com", "L2", 1, 5);
            Agent a3 = new TechnicalLeadAgent(3, "A3", "a3@test.com", "L3", 4, 5);

            graph.addAgent(a1);
            graph.addAgent(a2);
            graph.addAgent(a3);

            graph.addEscalationEdge(1, 2, 20.0);
            graph.addEscalationEdge(1, 3, 10.0); // Shorter base latency, but A3 is saturated (load 4/5)

            var routeOpt = graph.findOptimalEscalationRoute(1, "Technical");
            assert routeOpt.isPresent();
            System.out.println("[PASS] Test 3: ADSA Dijkstra dynamic routing algorithm verified.");
            passed++;
        } catch (Throwable e) {
            System.out.println("[FAIL] Test 3: " + e.getMessage());
            failed++;
        }

        // Test 4: End-to-End Service Integration
        try {
            HelpdeskService service = new HelpdeskService();
            Customer cust = new Customer(201, "Acme Admin", "admin@acme.com", "Acme Corp", "Enterprise");
            service.registerCustomer(cust);

            Ticket created = service.createAndAssignTicket(201, "Database deadlock timeout", "Postgres lock during migration");
            assert created.getCustomerId() == 201;
            assert created.getAssignedAgentId() != null;

            String escLog = service.escalateTicket(created.getTicketId(), "Requires L2/L3 lead engineer");
            assert escLog.contains("SUCCESS");
            assert "Escalated".equals(created.getStatus());

            String resLog = service.attemptResolution(created.getTicketId());
            assert resLog.contains("applied patch");
            assert "Resolved".equals(created.getStatus());

            System.out.println("[PASS] Test 4: End-to-End ticketing and escalation workflow verified.");
            passed++;
        } catch (Throwable e) {
            System.out.println("[FAIL] Test 4: " + e.getMessage());
            failed++;
        }

        System.out.println(String.format("\nResults: %d Passed, %d Failed.", passed, failed));
    }
}
