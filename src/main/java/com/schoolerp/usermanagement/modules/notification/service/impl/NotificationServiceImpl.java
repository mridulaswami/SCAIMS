package com.schoolerp.usermanagement.modules.notification.service.impl;

import com.schoolerp.usermanagement.common.exception.ResourceNotFoundException;
import com.schoolerp.usermanagement.common.response.PaginationResponse;
import com.schoolerp.usermanagement.modules.notification.dto.NotificationCountDto;
import com.schoolerp.usermanagement.modules.notification.dto.NotificationResponseDto;
import com.schoolerp.usermanagement.modules.notification.entity.NotificationEntity;
import com.schoolerp.usermanagement.modules.notification.repository.NotificationRepository;
import com.schoolerp.usermanagement.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<List<NotificationResponseDto>> getNotifications(UUID userId, Boolean unreadOnly, int page, int size) {
        log.info("Fetching notifications for userId={} | unreadOnly={} | page={} | size={}",
                userId, unreadOnly, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationEntity> entityPage;

        if (Boolean.TRUE.equals(unreadOnly)) {
            entityPage = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable);
        } else {
            entityPage = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        }

        List<NotificationResponseDto> dtoList = entityPage.getContent().stream()
                .map(this::mapToDto)
                .toList();

        return new PaginationResponse<>(dtoList, entityPage.getTotalElements(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationCountDto getUnreadCount(UUID userId) {
        log.info("Fetching unread notification count for userId={}", userId);
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(userId);
        return new NotificationCountDto(count);
    }

    @Override
    @Transactional
    public NotificationResponseDto markAsRead(UUID notificationId, UUID userId) {
        log.info("Marking notification as read | notificationId={} | userId={}", notificationId, userId);

        NotificationEntity notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return mapToDto(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        log.info("Marking all notifications as read for userId={}", userId);
        notificationRepository.markAllAsReadByRecipientId(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        log.info("Deleting notification id={} for userId={}", notificationId, userId);

        NotificationEntity notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        notificationRepository.delete(notification);
    }

    private NotificationResponseDto mapToDto(NotificationEntity entity) {
        return NotificationResponseDto.builder()
                .id(entity.getId())
                .recipientId(entity.getRecipient().getId())
                .actorId(entity.getActor() != null ? entity.getActor().getId() : null)
                .actorName(entity.getActor() != null ? entity.getActor().getName() : "System")
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType())
                .priority(entity.getPriority())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .isRead(entity.getIsRead())
                .readAt(entity.getReadAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
