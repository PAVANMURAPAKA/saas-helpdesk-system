package com.helpdesk.agent;

import com.helpdesk.model.Ticket;

/**
 * Concrete Subclass demonstrating Inheritance and Polymorphic override.
 * Handles financial, subscription, invoice, and payment gateway disputes.
 */
public class BillingSpecialistAgent extends Agent {

    public BillingSpecialistAgent(int agentId, String name, String email, String tierLevel, 
                                  int currentLoad, int maxCapacity) {
        super(agentId, name, email, tierLevel, "Billing", currentLoad, maxCapacity);
    }

    @Override
    public String resolveTicket(Ticket ticket) {
        if ("Billing".equalsIgnoreCase(ticket.getCategory())) {
            ticket.setStatus("Resolved");
            releaseTicket();
            return String.format("[%s Billing Specialist] '%s' reconciled Stripe transaction & issued PDF invoice for Ticket #%d.",
                    getTierLevel(), getName(), ticket.getTicketId());
        } else {
            return String.format("[%s Billing Specialist] '%s' forwarded mismatched category '%s' to dispatcher.",
                    getTierLevel(), getName(), ticket.getCategory());
        }
    }
}
