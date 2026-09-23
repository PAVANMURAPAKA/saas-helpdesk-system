package com.helpdesk.agent;

import com.helpdesk.model.Ticket;

/**
 * Base Abstract Agent class demonstrating Encapsulation and Polymorphic design.
 * All specialized agent tiers extend this base class.
 */
public abstract class Agent {
    private int agentId;
    private String name;
    private String email;
    private String tierLevel; // L1, L2, L3, Specialist_Lead
    private String specialization; // Billing, Technical, Auth_Access, General
    private int currentLoad;
    private int maxCapacity;
    private boolean available;

    public Agent(int agentId, String name, String email, String tierLevel, 
                 String specialization, int currentLoad, int maxCapacity) {
        this.agentId = agentId;
        this.name = name;
        this.email = email;
        this.tierLevel = tierLevel;
        this.specialization = specialization;
        this.currentLoad = currentLoad;
        this.maxCapacity = maxCapacity;
        this.available = currentLoad < maxCapacity;
    }

    /**
     * Polymorphic Resolution Method.
     * Each concrete subclass provides its specific resolution strategy.
     */
    public abstract String resolveTicket(Ticket ticket);

    /**
     * Checks if agent can take on more tickets.
     */
    public boolean canAcceptTicket() {
        return this.available && this.currentLoad < this.maxCapacity;
    }

    public void assignTicket() {
        if (!canAcceptTicket()) {
            throw new IllegalStateException("Agent " + name + " is at maximum capacity!");
        }
        this.currentLoad++;
        if (this.currentLoad >= this.maxCapacity) {
            this.available = false;
        }
    }

    public void releaseTicket() {
        if (this.currentLoad > 0) {
            this.currentLoad--;
            this.available = true;
        }
    }

    // Getters and Setters
    public int getAgentId() { return agentId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getTierLevel() { return tierLevel; }
    public String getSpecialization() { return specialization; }
    public int getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(int currentLoad) { 
        this.currentLoad = currentLoad; 
        this.available = currentLoad < maxCapacity;
    }
    public int getMaxCapacity() { return maxCapacity; }
    public boolean isAvailable() { return available; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) | Load: %d/%d", 
                tierLevel, name, specialization, currentLoad, maxCapacity);
    }
}
