package com.helpdesk.agent;

import com.helpdesk.model.Ticket;

/**
 * Concrete Subclass demonstrating Inheritance and Polymorphic override.
 * L1 Agent handles initial triage and general queries.
 */
public class L1SupportAgent extends Agent {

    public L1SupportAgent(int agentId, String name, String email, int currentLoad, int maxCapacity) {
        super(agentId, name, email, "L1", "General", currentLoad, maxCapacity);
    }

    @Override
    public String resolveTicket(Ticket ticket) {
        if ("General".equalsIgnoreCase(ticket.getCategory())) {
            ticket.setStatus("Resolved");
            releaseTicket();
            return String.format("L1 Agent '%s' resolved Ticket #%d with standard knowledge base documentation.", 
                    getName(), ticket.getTicketId());
        } else {
            // Cannot resolve complex specialized tickets at L1 -> Must Escalate!
            ticket.setStatus("Escalated");
            return String.format("L1 Agent '%s' lacks specialized domain credentials for '%s'. Initiating ADSA graph escalation.", 
                    getName(), ticket.getCategory());
        }
    }
}
