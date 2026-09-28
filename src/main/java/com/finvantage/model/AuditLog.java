package com.finvantage.model;

import java.time.LocalDateTime;

/**
 * AuditLog records security, transactional, and administrative events for compliance and tracking.
 */
public class AuditLog extends BaseEntity {

    private String actionType;
    private String tableName;
    private Long recordId;
    private Long userId;
    private String details;
    private LocalDateTime timestamp;

    public AuditLog() {
        super();
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(Long id, String actionType, String tableName, Long recordId, Long userId, String details) {
        super(id);
        this.actionType = actionType;
        this.tableName = tableName;
        this.recordId = recordId;
        this.userId = userId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }
}
