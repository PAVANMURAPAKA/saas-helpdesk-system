package com.helpdesk.agent;

import com.helpdesk.model.Ticket;

/**
 * Concrete Subclass demonstrating Inheritance and Polymorphic override.
 * Resolves critical infrastructure, database deadlock, and memory leak tickets.
 */
public class TechnicalLeadAgent extends Agent {

    public TechnicalLeadAgent(int agentId, String name, String email, String tierLevel, 
                              int currentLoad, int maxCapacity) {
        super(agentId, name, email, tierLevel, "Technical", currentLoad, maxCapacity);
    }

    @Override
    public String resolveTicket(Ticket ticket) {
        if ("Technical".equalsIgnoreCase(ticket.getCategory()) || "Bug_Report".equalsIgnoreCase(ticket.getCategory())) {
            ticket.setStatus("Resolved");
            releaseTicket();
            return String.format("[%s Tech Lead] '%s' applied patch/hotfix to cluster for Ticket #%d ('%s').",
                    getTierLevel(), getName(), ticket.getTicketId(), ticket.getTitle());
        } else {
            return String.format("[%s Tech Lead] '%s' routed non-technical ticket #%d to relevant domain specialist.",
                    getTierLevel(), getName(), ticket.getTicketId());
        }
    }
}
