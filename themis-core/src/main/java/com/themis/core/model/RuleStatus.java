package com.themis.core.model;

/**
 * Enumeration of rule lifecycle statuses.
 */
public enum RuleStatus {
    /**
     * Rule is being developed and not ready for use.
     */
    DRAFT("Draft", "Rule is being developed"),
    
    /**
     * Rule is under review for approval.
     */
    PENDING_APPROVAL("Pending Approval", "Rule is under review"),
    
    /**
     * Rule has been approved and is ready for deployment.
     */
    APPROVED("Approved", "Rule has been approved"),
    
    /**
     * Rule is currently active and being used in production.
     */
    ACTIVE("Active", "Rule is currently active"),
    
    /**
     * Rule is temporarily disabled but can be reactivated.
     */
    INACTIVE("Inactive", "Rule is temporarily disabled"),
    
    /**
     * Rule has been deprecated and should not be used.
     */
    DEPRECATED("Deprecated", "Rule has been deprecated"),
    
    /**
     * Rule has been permanently removed from the system.
     */
    ARCHIVED("Archived", "Rule has been archived");
    
    private final String displayName;
    private final String description;
    
    RuleStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if the rule is executable in the current status.
     */
    public boolean isExecutable() {
        return this == ACTIVE;
    }
    
    /**
     * Checks if the rule can be modified in the current status.
     */
    public boolean isModifiable() {
        return this == DRAFT || this == PENDING_APPROVAL;
    }
    
    /**
     * Gets the next valid statuses that this status can transition to.
     */
    public RuleStatus[] getValidTransitions() {
        switch (this) {
            case DRAFT:
                return new RuleStatus[]{PENDING_APPROVAL, ARCHIVED};
            case PENDING_APPROVAL:
                return new RuleStatus[]{APPROVED, DRAFT, ARCHIVED};
            case APPROVED:
                return new RuleStatus[]{ACTIVE, ARCHIVED};
            case ACTIVE:
                return new RuleStatus[]{INACTIVE, DEPRECATED, ARCHIVED};
            case INACTIVE:
                return new RuleStatus[]{ACTIVE, DEPRECATED, ARCHIVED};
            case DEPRECATED:
                return new RuleStatus[]{ARCHIVED};
            case ARCHIVED:
                return new RuleStatus[]{};
            default:
                return new RuleStatus[]{};
        }
    }
    
    /**
     * Checks if transition to the target status is valid.
     */
    public boolean canTransitionTo(RuleStatus targetStatus) {
        for (RuleStatus validStatus : getValidTransitions()) {
            if (validStatus == targetStatus) {
                return true;
            }
        }
        return false;
    }
}