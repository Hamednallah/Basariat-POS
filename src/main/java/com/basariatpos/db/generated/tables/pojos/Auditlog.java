/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables.pojos;


import java.io.Serializable;
import java.time.OffsetDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Auditlog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long auditLogId;
    private String tableName;
    private String recordPk;
    private String columnName;
    private String oldValue;
    private String newValue;
    private String actionType;
    private OffsetDateTime actionTimestamp;
    private Integer userId;
    private String details;

    public Auditlog() {}

    public Auditlog(Auditlog value) {
        this.auditLogId = value.auditLogId;
        this.tableName = value.tableName;
        this.recordPk = value.recordPk;
        this.columnName = value.columnName;
        this.oldValue = value.oldValue;
        this.newValue = value.newValue;
        this.actionType = value.actionType;
        this.actionTimestamp = value.actionTimestamp;
        this.userId = value.userId;
        this.details = value.details;
    }

    public Auditlog(
        Long auditLogId,
        String tableName,
        String recordPk,
        String columnName,
        String oldValue,
        String newValue,
        String actionType,
        OffsetDateTime actionTimestamp,
        Integer userId,
        String details
    ) {
        this.auditLogId = auditLogId;
        this.tableName = tableName;
        this.recordPk = recordPk;
        this.columnName = columnName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.actionType = actionType;
        this.actionTimestamp = actionTimestamp;
        this.userId = userId;
        this.details = details;
    }

    /**
     * Getter for <code>public.auditlog.audit_log_id</code>.
     */
    public Long getAuditLogId() {
        return this.auditLogId;
    }

    /**
     * Setter for <code>public.auditlog.audit_log_id</code>.
     */
    public void setAuditLogId(Long auditLogId) {
        this.auditLogId = auditLogId;
    }

    /**
     * Getter for <code>public.auditlog.table_name</code>.
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Setter for <code>public.auditlog.table_name</code>.
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Getter for <code>public.auditlog.record_pk</code>.
     */
    public String getRecordPk() {
        return this.recordPk;
    }

    /**
     * Setter for <code>public.auditlog.record_pk</code>.
     */
    public void setRecordPk(String recordPk) {
        this.recordPk = recordPk;
    }

    /**
     * Getter for <code>public.auditlog.column_name</code>.
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * Setter for <code>public.auditlog.column_name</code>.
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Getter for <code>public.auditlog.old_value</code>.
     */
    public String getOldValue() {
        return this.oldValue;
    }

    /**
     * Setter for <code>public.auditlog.old_value</code>.
     */
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * Getter for <code>public.auditlog.new_value</code>.
     */
    public String getNewValue() {
        return this.newValue;
    }

    /**
     * Setter for <code>public.auditlog.new_value</code>.
     */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    /**
     * Getter for <code>public.auditlog.action_type</code>.
     */
    public String getActionType() {
        return this.actionType;
    }

    /**
     * Setter for <code>public.auditlog.action_type</code>.
     */
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    /**
     * Getter for <code>public.auditlog.action_timestamp</code>.
     */
    public OffsetDateTime getActionTimestamp() {
        return this.actionTimestamp;
    }

    /**
     * Setter for <code>public.auditlog.action_timestamp</code>.
     */
    public void setActionTimestamp(OffsetDateTime actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }

    /**
     * Getter for <code>public.auditlog.user_id</code>.
     */
    public Integer getUserId() {
        return this.userId;
    }

    /**
     * Setter for <code>public.auditlog.user_id</code>.
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * Getter for <code>public.auditlog.details</code>.
     */
    public String getDetails() {
        return this.details;
    }

    /**
     * Setter for <code>public.auditlog.details</code>.
     */
    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Auditlog other = (Auditlog) obj;
        if (this.auditLogId == null) {
            if (other.auditLogId != null)
                return false;
        }
        else if (!this.auditLogId.equals(other.auditLogId))
            return false;
        if (this.tableName == null) {
            if (other.tableName != null)
                return false;
        }
        else if (!this.tableName.equals(other.tableName))
            return false;
        if (this.recordPk == null) {
            if (other.recordPk != null)
                return false;
        }
        else if (!this.recordPk.equals(other.recordPk))
            return false;
        if (this.columnName == null) {
            if (other.columnName != null)
                return false;
        }
        else if (!this.columnName.equals(other.columnName))
            return false;
        if (this.oldValue == null) {
            if (other.oldValue != null)
                return false;
        }
        else if (!this.oldValue.equals(other.oldValue))
            return false;
        if (this.newValue == null) {
            if (other.newValue != null)
                return false;
        }
        else if (!this.newValue.equals(other.newValue))
            return false;
        if (this.actionType == null) {
            if (other.actionType != null)
                return false;
        }
        else if (!this.actionType.equals(other.actionType))
            return false;
        if (this.actionTimestamp == null) {
            if (other.actionTimestamp != null)
                return false;
        }
        else if (!this.actionTimestamp.equals(other.actionTimestamp))
            return false;
        if (this.userId == null) {
            if (other.userId != null)
                return false;
        }
        else if (!this.userId.equals(other.userId))
            return false;
        if (this.details == null) {
            if (other.details != null)
                return false;
        }
        else if (!this.details.equals(other.details))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.auditLogId == null) ? 0 : this.auditLogId.hashCode());
        result = prime * result + ((this.tableName == null) ? 0 : this.tableName.hashCode());
        result = prime * result + ((this.recordPk == null) ? 0 : this.recordPk.hashCode());
        result = prime * result + ((this.columnName == null) ? 0 : this.columnName.hashCode());
        result = prime * result + ((this.oldValue == null) ? 0 : this.oldValue.hashCode());
        result = prime * result + ((this.newValue == null) ? 0 : this.newValue.hashCode());
        result = prime * result + ((this.actionType == null) ? 0 : this.actionType.hashCode());
        result = prime * result + ((this.actionTimestamp == null) ? 0 : this.actionTimestamp.hashCode());
        result = prime * result + ((this.userId == null) ? 0 : this.userId.hashCode());
        result = prime * result + ((this.details == null) ? 0 : this.details.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Auditlog (");

        sb.append(auditLogId);
        sb.append(", ").append(tableName);
        sb.append(", ").append(recordPk);
        sb.append(", ").append(columnName);
        sb.append(", ").append(oldValue);
        sb.append(", ").append(newValue);
        sb.append(", ").append(actionType);
        sb.append(", ").append(actionTimestamp);
        sb.append(", ").append(userId);
        sb.append(", ").append(details);

        sb.append(")");
        return sb.toString();
    }
}
