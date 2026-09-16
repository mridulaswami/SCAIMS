package com.schoolerp.usermanagement.modules.notification.repository;

import com.schoolerp.usermanagement.modules.notification.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Page<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    Page<NotificationEntity> findByRecipientIdAndIsReadOrderByCreatedAtDesc(UUID recipientId, Boolean isRead, Pageable pageable);

    long countByRecipientIdAndIsReadFalse(UUID recipientId);

    Optional<NotificationEntity> findByIdAndRecipientId(UUID id, UUID recipientId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true, n.readAt = :readAt WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsReadByRecipientId(@Param("recipientId") UUID recipientId, @Param("readAt") LocalDateTime readAt);
}
