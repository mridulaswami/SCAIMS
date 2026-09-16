package com.schoolerp.usermanagement.modules.notification.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.common.response.PaginationResponse;
import com.schoolerp.usermanagement.modules.notification.dto.NotificationCountDto;
import com.schoolerp.usermanagement.modules.notification.dto.NotificationResponseDto;
import com.schoolerp.usermanagement.modules.notification.service.NotificationService;
import com.schoolerp.usermanagement.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<PaginationResponse<List<NotificationResponseDto>>> getNotifications(
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        UUID currentUserId = extractUserId(request);
        log.info("GET /api/v1/notifications | userId={} | unreadOnly={} | page={} | size={}",
                currentUserId, unreadOnly, page, size);

        PaginationResponse<List<NotificationResponseDto>> response =
                notificationService.getNotifications(currentUserId, unreadOnly, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<NotificationCountDto>> getUnreadCount(HttpServletRequest request) {
        UUID currentUserId = extractUserId(request);
        NotificationCountDto countDto = notificationService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(ApiResponse.of(true, "Unread count fetched successfully", countDto));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> markAsRead(
            @PathVariable UUID id,
            HttpServletRequest request) {

        UUID currentUserId = extractUserId(request);
        NotificationResponseDto response = notificationService.markAsRead(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.of(true, "Notification marked as read", response));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(HttpServletRequest request) {
        UUID currentUserId = extractUserId(request);
        notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.ok(ApiResponse.of(true, "All notifications marked as read", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable UUID id,
            HttpServletRequest request) {

        UUID currentUserId = extractUserId(request);
        notificationService.deleteNotification(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.of(true, "Notification deleted successfully", null));
    }

    private UUID extractUserId(HttpServletRequest request) {
        String token = jwtTokenProvider.extractAccestoken(request);
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Unauthorized: Token is missing");
        }
        String userIdStr = jwtTokenProvider.getUserIdFromJWT(token);
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new RuntimeException("Unauthorized: Invalid token claims");
        }
        return UUID.fromString(userIdStr);
    }
}
