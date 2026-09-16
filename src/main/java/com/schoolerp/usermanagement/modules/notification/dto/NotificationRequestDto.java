package com.schoolerp.usermanagement.modules.notification.dto;

import com.schoolerp.usermanagement.modules.notification.enums.NotificationPriority;
import com.schoolerp.usermanagement.modules.notification.enums.NotificationType;
import com.schoolerp.usermanagement.modules.notification.enums.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {

    // Single recipient
    private UUID recipientId;

    // Multiple recipients (similar to toList in EmailRequestDto)
    private List<UUID> recipientIds;

    // Actor who triggered the notification (optional)
    private UUID actorId;

    private String title;

    private String message;

    private NotificationType type;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    private TargetType targetType;

    private String targetId;
}
