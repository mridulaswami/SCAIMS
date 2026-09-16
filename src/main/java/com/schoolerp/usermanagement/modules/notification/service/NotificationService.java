package com.schoolerp.usermanagement.modules.notification.service;

import com.schoolerp.usermanagement.common.response.PaginationResponse;
import com.schoolerp.usermanagement.modules.notification.dto.NotificationCountDto;
import com.schoolerp.usermanagement.modules.notification.dto.NotificationResponseDto;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    PaginationResponse<List<NotificationResponseDto>> getNotifications(UUID userId, Boolean unreadOnly, int page, int size);

    NotificationCountDto getUnreadCount(UUID userId);

    NotificationResponseDto markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    void deleteNotification(UUID notificationId, UUID userId);
}
