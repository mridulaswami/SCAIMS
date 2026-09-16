package com.schoolerp.usermanagement.modules.notification.enums;

public enum NotificationType {
    // Complaint lifecycle
    COMPLAINT_SUBMITTED,
    COMPLAINT_ACKNOWLEDGED,
    COMPLAINT_STATUS_UPDATED,
    COMPLAINT_RESOLVED,
    COMPLAINT_REJECTED,

    // Work Order lifecycle
    WORK_ORDER_ASSIGNED,
    WORK_ORDER_STATUS_CHANGED,
    WORK_ORDER_COMPLETED,

    // User & Security lifecycle
    USER_REGISTERED,
    PASSWORD_CHANGED,

    // General / System
    SYSTEM_ALERT
}
