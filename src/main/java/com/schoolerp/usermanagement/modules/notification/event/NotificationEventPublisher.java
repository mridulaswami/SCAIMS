package com.schoolerp.usermanagement.modules.notification.event;

import com.schoolerp.usermanagement.modules.notification.dto.NotificationRequestDto;
import com.schoolerp.usermanagement.modules.notification.dto.NotificationResponseDto;
import com.schoolerp.usermanagement.modules.notification.entity.NotificationEntity;
import com.schoolerp.usermanagement.modules.notification.enums.NotificationPriority;
import com.schoolerp.usermanagement.modules.notification.enums.NotificationType;
import com.schoolerp.usermanagement.modules.notification.enums.TargetType;
import com.schoolerp.usermanagement.modules.notification.repository.NotificationRepository;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {

    private final NotificationRepository notificationRepository;
    private final UserEntityRepository userEntityRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Asynchronously processes and dispatches in-app notification to single or multiple recipients.
     * Built with DTO builder pattern matching EmailService.
     */
    @Async("taskExecutor")
    @Transactional
    public void sendNotification(NotificationRequestDto request) {
        if (request == null) {
            log.warn("Cannot send notification: request is null");
            return;
        }

        log.info("Processing async notification: type={}, title={}", request.getType(), request.getTitle());

        try {
            // 1. Single recipient dispatch
            if (request.getRecipientId() != null) {
                dispatchToRecipient(request.getRecipientId(), request);
            }

            // 2. Multiple recipients dispatch (like toList in EmailRequestDto)
            if (request.getRecipientIds() != null && !request.getRecipientIds().isEmpty()) {
                for (UUID recipientId : request.getRecipientIds()) {
                    if (recipientId != null && !recipientId.equals(request.getRecipientId())) {
                        dispatchToRecipient(recipientId, request);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error in async notification dispatch: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Persist notification in DB and push real-time STOMP message to user destination.
     */
    private void dispatchToRecipient(UUID recipientId, NotificationRequestDto request) {
        try {
            Optional<UserEntity> recipientOpt = userEntityRepository.findById(recipientId);
            if (recipientOpt.isEmpty()) {
                log.warn("Recipient user not found with id: {}. Skipping notification.", recipientId);
                return;
            }
            UserEntity recipient = recipientOpt.get();

            UserEntity actor = null;
            if (request.getActorId() != null) {
                actor = userEntityRepository.findById(request.getActorId()).orElse(null);
            }

            NotificationEntity entity = NotificationEntity.builder()
                    .recipient(recipient)
                    .actor(actor)
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .type(request.getType())
                    .priority(request.getPriority() != null ? request.getPriority() : NotificationPriority.MEDIUM)
                    .targetType(request.getTargetType())
                    .targetId(request.getTargetId())
                    .isRead(false)
                    .build();

            NotificationEntity saved = notificationRepository.save(entity);
            log.info("Saved in-app notification id={} for recipient={}", saved.getId(), recipient.getId());

            NotificationResponseDto responseDto = NotificationResponseDto.builder()
                    .id(saved.getId())
                    .recipientId(recipient.getId())
                    .actorId(actor != null ? actor.getId() : null)
                    .actorName(actor != null ? actor.getName() : "System")
                    .title(saved.getTitle())
                    .message(saved.getMessage())
                    .type(saved.getType())
                    .priority(saved.getPriority())
                    .targetType(saved.getTargetType())
                    .targetId(saved.getTargetId())
                    .isRead(saved.getIsRead())
                    .readAt(saved.getReadAt())
                    .createdAt(saved.getCreatedAt())
                    .build();

            // Real-time WebSocket push to user's private queue
            try {
                messagingTemplate.convertAndSendToUser(
                        recipient.getId().toString(),
                        "/queue/notifications",
                        responseDto
                );
                log.info("Pushed WebSocket notification to user: {}", recipient.getId());
            } catch (Exception pushEx) {
                log.warn("Failed to push WebSocket notification to user: {}. Error: {}", recipient.getId(), pushEx.getMessage());
            }

        } catch (Exception ex) {
            log.error("Failed to dispatch notification to recipient={}: {}", recipientId, ex.getMessage(), ex);
        }
    }

    /**
     * Convenience method for backward compatibility
     */
    public void publishToUser(UUID recipientId,
                              UUID actorId,
                              String title,
                              String message,
                              NotificationType type,
                              NotificationPriority priority,
                              TargetType targetType,
                              String targetId) {
        sendNotification(NotificationRequestDto.builder()
                .recipientId(recipientId)
                .actorId(actorId)
                .title(title)
                .message(message)
                .type(type)
                .priority(priority != null ? priority : NotificationPriority.MEDIUM)
                .targetType(targetType)
                .targetId(targetId)
                .build());
    }

    /**
     * Convenience method for multiple recipients
     */
    public void publishToMultipleUsers(Collection<UUID> recipientIds,
                                       UUID actorId,
                                       String title,
                                       String message,
                                       NotificationType type,
                                       NotificationPriority priority,
                                       TargetType targetType,
                                       String targetId) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return;
        }
        sendNotification(NotificationRequestDto.builder()
                .recipientIds(List.copyOf(recipientIds))
                .actorId(actorId)
                .title(title)
                .message(message)
                .type(type)
                .priority(priority != null ? priority : NotificationPriority.MEDIUM)
                .targetType(targetType)
                .targetId(targetId)
                .build());
    }
}
