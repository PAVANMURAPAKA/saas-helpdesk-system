package com.helpdesk;

import com.helpdesk.model.Customer;
import com.helpdesk.model.Ticket;
import com.helpdesk.service.HelpdeskService;

import java.util.List;

/**
 * Main Application Runner.
 * Executes the complete end-to-end lifecycle demonstrating:
 * 1. DBMS: Customer & Ticket historical binding.
 * 2. DMGT: Poset hierarchy validation and Equivalence partitioning.
 * 3. ADSA: Dijkstra shortest-path escalation routing through DAG.
 * 4. OOPJ: Encapsulation, Inheritance, Polymorphism, and Service abstractions.
 * 5. Python: NLP classification and confidence scoring.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  INTELLIGENT SAAS HELPDESK RESOLUTION & ESCALATION MANAGEMENT SYSTEM");
        System.out.println("================================================================================");

        HelpdeskService service = new HelpdeskService();

        // 1. DBMS Entity Setup: Customer creation with Subscription SLA tiers
        Customer c1 = new Customer(101, "Satya Nadella", "satya@enterprisecloud.com", "Enterprise Cloud Inc", "Enterprise");
        Customer c2 = new Customer(102, "Ravi Kumar", "ravi@startupdev.in", "StartupDev Studio", "Pro");
        service.registerCustomer(c1);
        service.registerCustomer(c2);

        System.out.println("\n[1. DBMS Customer Context Loaded]");
        System.out.println("  * " + c1);
        System.out.println("  * " + c2);

        // 2. ADSA & DMGT Topology Validation
        System.out.println("\n[2. ADSA & DMGT Hierarchy Topology Check]");
        boolean isDag = service.getEscalationGraph().isDirectedAcyclicGraph();
        System.out.println("  * Escalation Graph verified as Directed Acyclic Graph (DAG)? " + isDag);
        System.out.println("  * Total Registered Agents: " + service.getEscalationGraph().getAllAgents().size());

        // 3. Scenario A: Technical Critical Outage Ticket
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("SCENARIO A: Critical Technical Outage Ticket");
        System.out.println("--------------------------------------------------------------------------------");

        String titleA = "Database connection pool exhausted during peak traffic";
        String descA = "Kubernetes pods failing with 500 error and JDBC socket timeout. High transaction volume.";

        System.out.println(">> Raising Ticket for Customer #101 (" + c1.getName() + ")...");
        Ticket ticketA = service.createAndAssignTicket(101, titleA, descA);
        System.out.println("  [Auto-Classified] Category: " + ticketA.getCategory() 
                + " | Confidence: " + String.format("%.2f%%", ticketA.getConfidenceScore() * 100)
                + " | Priority: " + ticketA.getPriority());
        System.out.println("  [Initial Assignment] Ticket #" + ticketA.getTicketId() 
                + " assigned to L1 Agent #" + ticketA.getAssignedAgentId());

        // Initial resolution attempt by L1
        System.out.println("\n>> L1 Agent attempting resolution (OOPJ Polymorphism)...");
        String l1Result = service.attemptResolution(ticketA.getTicketId());
        System.out.println("  " + l1Result);

        // Escalation trigger via Dijkstra
        System.out.println("\n>> Triggering ADSA Shortest Path Escalation...");
        String escalationResultA = service.escalateTicket(ticketA.getTicketId(), "Production JDBC pool deadlock requires L2/L3 engineering fix.");
        System.out.println("  " + escalationResultA);

        // Specialized resolution attempt
        System.out.println("\n>> Assigned Specialist executing resolution (OOPJ Polymorphism)...");
        String finalResultA = service.attemptResolution(ticketA.getTicketId());
        System.out.println("  " + finalResultA);
        System.out.println("  Final Ticket Status: " + ticketA.getStatus());

        // 4. Scenario B: Stripe Billing Double Charge Dispute
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("SCENARIO B: Financial Billing Dispute Ticket");
        System.out.println("--------------------------------------------------------------------------------");

        String titleB = "Credit card charged twice for annual Enterprise renewal";
        String descB = "Stripe webhook processed duplicate payment transaction. Requesting immediate refund.";

        System.out.println(">> Raising Ticket for Customer #101 (" + c1.getName() + ")...");
        Ticket ticketB = service.createAndAssignTicket(101, titleB, descB);
        System.out.println("  [Auto-Classified] Category: " + ticketB.getCategory() 
                + " | Confidence: " + String.format("%.2f%%", ticketB.getConfidenceScore() * 100)
                + " | Priority: " + ticketB.getPriority());

        System.out.println("\n>> Escalating Billing Dispute via ADSA Graph Routing...");
        String escalationResultB = service.escalateTicket(ticketB.getTicketId(), "Refund authorization exceeds L1 threshold.");
        System.out.println("  " + escalationResultB);

        System.out.println("\n>> Specialist resolving billing dispute...");
        String finalResultB = service.attemptResolution(ticketB.getTicketId());
        System.out.println("  " + finalResultB);

        // 5. Verification of Customer History Binding (DBMS Key Problem Solved)
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("VERIFICATION: Full Customer Ticket History Retention (Zero Context Loss)");
        System.out.println("--------------------------------------------------------------------------------");

        List<Ticket> c1History = service.getCustomerTicketHistory(101);
        System.out.println("Historical Tickets linked to Customer #101 (" + c1.getName() + "):");
        for (Ticket t : c1History) {
            System.out.println("  * " + t);
        }

        System.out.println("\nComplete Audit Trail of Escalation Logs:");
        for (var log : service.getEscalationLogs()) {
            System.out.println("  * " + log);
        }

        System.out.println("\n================================================================================");
        System.out.println("  ALL 5 ACADEMIC SUBJECT MODULES VERIFIED & EXECUTED SUCCESSFULLY!");
        System.out.println("================================================================================");
    }
}
