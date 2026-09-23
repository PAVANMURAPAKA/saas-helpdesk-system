package com.helpdesk.agent;

import com.helpdesk.model.Ticket;

/**
 * Concrete Subclass demonstrating Inheritance and Polymorphic override.
 * Resolves authentication lockouts, SSO loops, and MFA resets.
 */
public class SecurityAuthAgent extends Agent {

    public SecurityAuthAgent(int agentId, String name, String email, String tierLevel, 
                             int currentLoad, int maxCapacity) {
        super(agentId, name, email, tierLevel, "Auth_Access", currentLoad, maxCapacity);
    }

    @Override
    public String resolveTicket(Ticket ticket) {
        if ("Auth_Access".equalsIgnoreCase(ticket.getCategory())) {
            ticket.setStatus("Resolved");
            releaseTicket();
            return String.format("[%s Security Specialist] '%s' invalidated corrupt session token & reset MFA challenge for Ticket #%d.",
                    getTierLevel(), getName(), ticket.getTicketId());
        } else {
            return String.format("[%s Security Specialist] '%s' returned ticket #%d as non-auth category.",
                    getTierLevel(), getName(), ticket.getTicketId());
        }
    }
}
