package com.helpdesk.model;

import java.time.LocalDateTime;

/**
 * EscalationLog domain model preserving complete context and audit trails.
 */
public class EscalationLog {
    private int escalationId;
    private int ticketId;
    private Integer fromAgentId;
    private int toAgentId;
    private String reason;
    private String escalationLevel; // L1_to_L2, L2_to_L3, L3_to_Lead
    private boolean slaBreached;
    private LocalDateTime escalatedAt;

    public EscalationLog(int escalationId, int ticketId, Integer fromAgentId, int toAgentId, 
                         String reason, String escalationLevel, boolean slaBreached) {
        this.escalationId = escalationId;
        this.ticketId = ticketId;
        this.fromAgentId = fromAgentId;
        this.toAgentId = toAgentId;
        this.reason = reason;
        this.escalationLevel = escalationLevel;
        this.slaBreached = slaBreached;
        this.escalatedAt = LocalDateTime.now();
    }

    public int getEscalationId() { return escalationId; }
    public int getTicketId() { return ticketId; }
    public Integer getFromAgentId() { return fromAgentId; }
    public int getToAgentId() { return toAgentId; }
    public String getReason() { return reason; }
    public String getEscalationLevel() { return escalationLevel; }
    public boolean isSlaBreached() { return slaBreached; }
    public LocalDateTime getEscalatedAt() { return escalatedAt; }

    @Override
    public String toString() {
        return String.format("EscalationLog[Ticket#%d: Agent#%s -> Agent#%d | Level: %s | Reason: '%s']",
                ticketId, fromAgentId != null ? fromAgentId : "Start", toAgentId, escalationLevel, reason);
    }
}
