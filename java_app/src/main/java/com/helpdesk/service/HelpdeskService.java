package com.helpdesk.service;

import com.helpdesk.agent.*;
import com.helpdesk.graph.EscalationGraph;
import com.helpdesk.model.Customer;
import com.helpdesk.model.EscalationLog;
import com.helpdesk.model.Ticket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Core Service orchestrating Customer context retention, ML category classification,
 * ADSA graph routing, and OOPJ polymorphic ticket execution.
 */
public class HelpdeskService {

    private final Map<Integer, Customer> customers = new HashMap<>();
    private final Map<Integer, Ticket> tickets = new HashMap<>();
    private final List<EscalationLog> escalationLogs = new ArrayList<>();
    private final EscalationGraph escalationGraph = new EscalationGraph();

    private int nextTicketId = 1001;
    private int nextLogId = 501;
    private String mlApiUrl = "http://127.0.0.1:5005/classify";

    public HelpdeskService() {
        seedInitialAgents();
    }

    public void setMlApiUrl(String mlApiUrl) {
        this.mlApiUrl = mlApiUrl;
    }

    private void seedInitialAgents() {
        // L1 Support
        Agent a1 = new L1SupportAgent(1, "Ramesh Patel", "ramesh@helpdesk.com", 2, 10);
        Agent a2 = new L1SupportAgent(2, "Sneha Kulkarni", "sneha@helpdesk.com", 3, 10);

        // L2 Specialists
        Agent a3 = new TechnicalLeadAgent(3, "Kiran Kumar", "kiran@helpdesk.com", "L2", 1, 8);
        Agent a4 = new BillingSpecialistAgent(4, "Deepa Nair", "deepa@helpdesk.com", "L2", 1, 8);
        Agent a5 = new SecurityAuthAgent(5, "Manish Joshi", "manish@helpdesk.com", "L2", 2, 8);

        // L3 Senior Specialists
        Agent a6 = new TechnicalLeadAgent(6, "Dr. Arvind Sen", "arvind@helpdesk.com", "L3", 1, 6);
        Agent a7 = new BillingSpecialistAgent(7, "Lakshmi Narayanan", "lakshmi@helpdesk.com", "L3", 0, 6);

        // Specialist Lead
        Agent a8 = new TechnicalLeadAgent(8, "Venkatesh Iyer", "venkat@helpdesk.com", "Specialist_Lead", 1, 5);

        // Register to graph
        for (Agent a : Arrays.asList(a1, a2, a3, a4, a5, a6, a7, a8)) {
            escalationGraph.addAgent(a);
        }

        // Add Directed Escalation Edges
        // L1 -> L2
        escalationGraph.addEscalationEdge(1, 3, 15.0);
        escalationGraph.addEscalationEdge(1, 4, 10.0);
        escalationGraph.addEscalationEdge(1, 5, 12.0);
        escalationGraph.addEscalationEdge(2, 3, 15.0);
        escalationGraph.addEscalationEdge(2, 4, 10.0);
        escalationGraph.addEscalationEdge(2, 5, 12.0);

        // L2 -> L3
        escalationGraph.addEscalationEdge(3, 6, 25.0);
        escalationGraph.addEscalationEdge(4, 7, 20.0);
        escalationGraph.addEscalationEdge(5, 6, 25.0);

        // L3 -> Specialist Lead
        escalationGraph.addEscalationEdge(6, 8, 40.0);
        escalationGraph.addEscalationEdge(7, 8, 45.0);

        // Direct fast-track edge for Critical bypass
        escalationGraph.addEscalationEdge(1, 6, 50.0);
    }

    public void registerCustomer(Customer customer) {
        customers.put(customer.getCustomerId(), customer);
    }

    public Customer getCustomer(int customerId) {
        return customers.get(customerId);
    }

    /**
     * Calls Python ML endpoint to predict category and confidence.
     * Includes intelligent fallback if Python API is not running.
     */
    public Map<String, Object> classifyTicketText(String title, String description) {
        Map<String, Object> result = new HashMap<>();
        String combined = (title + " " + description).toLowerCase();

        try {
            URI uri = URI.create(mlApiUrl);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(1500);
            conn.setReadTimeout(1500);
            conn.setDoOutput(true);

            String jsonPayload = String.format("{\"title\":\"%s\",\"description\":\"%s\"}",
                    escapeJson(title), escapeJson(description));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                String respStr = response.toString();

                // Simple JSON extraction
                String cat = extractJsonString(respStr, "category");
                String urgency = extractJsonString(respStr, "recommended_urgency");
                double conf = extractJsonDouble(respStr, "confidence");

                result.put("category", cat != null ? cat : "General");
                result.put("confidence", conf > 0 ? conf : 0.85);
                result.put("priority", urgency != null ? urgency : "Medium");
                result.put("source", "Python NLP Classifier (Live REST API)");
                return result;
            }
        } catch (Exception ignored) {
            // Fallback heuristics when ML API server is offline
        }

        // High quality fallback heuristic
        if (combined.contains("invoice") || combined.contains("stripe") || combined.contains("charge") || combined.contains("refund")) {
            result.put("category", "Billing");
            result.put("confidence", 0.94);
            result.put("priority", combined.contains("twice") || combined.contains("dispute") ? "High" : "Medium");
        } else if (combined.contains("500") || combined.contains("kubernetes") || combined.contains("database") || combined.contains("crash")) {
            result.put("category", "Technical");
            result.put("confidence", 0.96);
            result.put("priority", "Critical");
        } else if (combined.contains("2fa") || combined.contains("sso") || combined.contains("login") || combined.contains("password")) {
            result.put("category", "Auth_Access");
            result.put("confidence", 0.92);
            result.put("priority", "High");
        } else if (combined.contains("button") || combined.contains("freeze") || combined.contains("bug") || combined.contains("export")) {
            result.put("category", "Bug_Report");
            result.put("confidence", 0.89);
            result.put("priority", "Medium");
        } else {
            result.put("category", "General");
            result.put("confidence", 0.88);
            result.put("priority", "Low");
        }
        result.put("source", "Embedded Rule Engine (Fallback)");
        return result;
    }

    /**
     * Creates a ticket linked to customer context, auto-classifies category,
     * assigns an initial L1 triage agent.
     */
    public Ticket createAndAssignTicket(int customerId, String title, String description) {
        Customer customer = customers.get(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer #" + customerId + " not found!");
        }

        Map<String, Object> nlpResult = classifyTicketText(title, description);
        String category = (String) nlpResult.get("category");
        String priority = (String) nlpResult.get("priority");
        double confidence = (Double) nlpResult.get("confidence");

        int slaHours = "Enterprise".equalsIgnoreCase(customer.getSubscriptionTier()) ? 2 : 8;
        if ("Critical".equalsIgnoreCase(priority)) slaHours = 1;

        Ticket ticket = new Ticket(nextTicketId++, customerId, title, description, category, priority, slaHours);
        ticket.setConfidenceScore(confidence);

        // Assign to least loaded L1 triage agent
        Agent l1Agent = escalationGraph.getAgent(1).getCurrentLoad() <= escalationGraph.getAgent(2).getCurrentLoad() 
                ? escalationGraph.getAgent(1) : escalationGraph.getAgent(2);

        l1Agent.assignTicket();
        ticket.setAssignedAgentId(l1Agent.getAgentId());
        ticket.setStatus("Assigned");

        tickets.put(ticket.getTicketId(), ticket);
        return ticket;
    }

    /**
     * Escalates a ticket using Dijkstra's shortest path algorithm on the ADSA Escalation Graph.
     */
    public String escalateTicket(int ticketId, String reason) {
        Ticket ticket = tickets.get(ticketId);
        if (ticket == null) return "Ticket not found";

        int currentAgentId = ticket.getAssignedAgentId();
        Agent currentAgent = escalationGraph.getAgent(currentAgentId);

        // Required specialization based on ticket category
        String requiredSpec = ticket.getCategory();
        if ("Bug_Report".equalsIgnoreCase(requiredSpec)) requiredSpec = "Technical";

        Optional<EscalationGraph.EscalationRoute> routeOpt = 
                escalationGraph.findOptimalEscalationRoute(currentAgentId, requiredSpec);

        if (!routeOpt.isPresent()) {
            return String.format("Escalation failed: No reachable available specialist found for category '%s'", requiredSpec);
        }

        EscalationGraph.EscalationRoute route = routeOpt.get();
        int targetAgentId = route.getDestinationAgentId();
        Agent targetAgent = escalationGraph.getAgent(targetAgentId);

        // Update workload
        currentAgent.releaseTicket();
        targetAgent.assignTicket();

        // Update ticket state
        ticket.setAssignedAgentId(targetAgentId);
        ticket.setStatus("Escalated");

        // Log escalation event
        EscalationLog log = new EscalationLog(
                nextLogId++,
                ticketId,
                currentAgentId,
                targetAgentId,
                reason,
                currentAgent.getTierLevel() + "_to_" + targetAgent.getTierLevel(),
                ticket.isSlaBreached()
        );
        escalationLogs.add(log);

        return String.format("SUCCESS: Ticket #%d escalated from [%s %s] to [%s %s (%s)] via Dijkstra optimal path %s (Score: %.2f mins)",
                ticketId, currentAgent.getTierLevel(), currentAgent.getName(),
                targetAgent.getTierLevel(), targetAgent.getName(), targetAgent.getSpecialization(),
                route.getPath(), route.getTotalLatencyScore());
    }

    /**
     * Demonstrates Polymorphism: calls resolveTicket on the currently assigned agent.
     */
    public String attemptResolution(int ticketId) {
        Ticket ticket = tickets.get(ticketId);
        if (ticket == null) return "Ticket not found";

        Agent agent = escalationGraph.getAgent(ticket.getAssignedAgentId());
        return agent.resolveTicket(ticket);
    }

    public List<Ticket> getCustomerTicketHistory(int customerId) {
        List<Ticket> history = new ArrayList<>();
        for (Ticket t : tickets.values()) {
            if (t.getCustomerId() == customerId) {
                history.add(t);
            }
        }
        return history;
    }

    public EscalationGraph getEscalationGraph() { return escalationGraph; }
    public List<EscalationLog> getEscalationLogs() { return Collections.unmodifiableList(escalationLogs); }

    // Helper JSON extractors
    private String escapeJson(String s) {
        return s.replace("\"", "\\\"").replace("\n", " ");
    }

    private String extractJsonString(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx);
        int quote1 = json.indexOf("\"", colon);
        int quote2 = json.indexOf("\"", quote1 + 1);
        if (quote1 != -1 && quote2 != -1) {
            return json.substring(quote1 + 1, quote2);
        }
        return null;
    }

    private double extractJsonDouble(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return 0.0;
        int colon = json.indexOf(":", idx);
        int comma = json.indexOf(",", colon);
        int brace = json.indexOf("}", colon);
        int end = (comma != -1 && comma < brace) ? comma : brace;
        if (end != -1) {
            try {
                return Double.parseDouble(json.substring(colon + 1, end).trim());
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
