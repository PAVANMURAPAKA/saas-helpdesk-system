package com.helpdesk.model;

import java.time.LocalDateTime;

/**
 * Customer domain model demonstrating Encapsulation.
 * Maintains persistent customer identity and subscription tier SLA level.
 */
public class Customer {
    private int customerId;
    private String name;
    private String email;
    private String company;
    private String subscriptionTier; // Free, Pro, Enterprise
    private LocalDateTime createdAt;

    public Customer(int customerId, String name, String email, String company, String subscriptionTier) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.company = company;
        this.subscriptionTier = subscriptionTier;
        this.createdAt = LocalDateTime.now();
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSubscriptionTier() {
        return subscriptionTier;
    }

    public void setSubscriptionTier(String subscriptionTier) {
        this.subscriptionTier = subscriptionTier;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("Customer[#%d: %s (%s) - %s Tier]", 
                customerId, name, email, subscriptionTier);
    }
}
