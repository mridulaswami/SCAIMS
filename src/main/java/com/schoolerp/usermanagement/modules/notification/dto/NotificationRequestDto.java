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

    private UUID recipientId;
    private List<UUID> recipientIds;
    private UUID actorId;
    private String title;
    private String message;
    private NotificationType type;
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;
    private TargetType targetType;
    private String targetId;
}
