package com.helpdesk.model;

import java.time.LocalDateTime;

/**
 * Ticket domain model demonstrating Encapsulation.
 * Central entity linking customer history, ML classification, and assigned agent.
 */
public class Ticket {
    private int ticketId;
    private int customerId;
    private String title;
    private String description;
    private String category; // Billing, Technical, Auth_Access, Bug_Report, General
    private String priority; // Low, Medium, High, Critical
    private String status;   // Open, Assigned, Escalated, Resolved, Closed
    private Integer assignedAgentId;
    private double confidenceScore;
    private LocalDateTime slaDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Ticket(int ticketId, int customerId, String title, String description, 
                  String category, String priority, int slaHours) {
        this.ticketId = ticketId;
        this.customerId = customerId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = "Open";
        this.confidenceScore = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.slaDeadline = LocalDateTime.now().plusHours(slaHours);
    }

    public boolean isSlaBreached() {
        return LocalDateTime.now().isAfter(slaDeadline) && !"Resolved".equalsIgnoreCase(status);
    }

    // Getters and Setters
    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(Integer assignedAgentId) { 
        this.assignedAgentId = assignedAgentId; 
        this.updatedAt = LocalDateTime.now();
    }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return String.format("Ticket[#%d | Cust#%d | Cat: %s | Pri: %s | Status: %s | Agent#%s]",
                ticketId, customerId, category, priority, status, 
                assignedAgentId != null ? assignedAgentId.toString() : "None");
    }
}
