package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.entity.Notification;
import com.amul.cattlefeed.entity.Secretary;
import com.amul.cattlefeed.repository.FarmerRepository;
import com.amul.cattlefeed.repository.NotificationRepository;
import com.amul.cattlefeed.repository.SecretaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final FarmerRepository farmerRepository;
    private final SecretaryRepository secretaryRepository;

    /**
     * GET /api/notifications/{sabhasadNo}
     * Returns notifications for the farmer identified by their sabhasadNo (which is also their loginId).
     * Filters strictly by userId + userType="FARMER" to prevent cross-role leakage.
     */
    @GetMapping("/{sabhasadNo}")
    public List<Notification> getNotifications(@PathVariable String sabhasadNo) {

        Farmer farmer = farmerRepository
                .findBySabhasadNo(sabhasadNo)
                .orElseThrow(() -> new RuntimeException("Farmer not found: " + sabhasadNo));

        return notificationRepository.findByUserIdAndUserTypeOrderByCreatedAtDesc(
                farmer.getFarmerId(), "FARMER"
        );
    }

    /**
     * GET /api/notifications/secretary/{loginId}
     * Returns notifications for the secretary identified by their loginId.
     * Filters strictly by userId + userType="SECRETARY" to prevent cross-role leakage.
     */
    @GetMapping("/secretary/{loginId}")
    public List<Notification> getSecretaryNotifications(@PathVariable String loginId) {

        Secretary secretary = secretaryRepository
                .findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("Secretary not found: " + loginId));

        return notificationRepository.findByUserIdAndUserTypeOrderByCreatedAtDesc(
                secretary.getId(), "SECRETARY"
        );
    }

    /**
     * PUT /api/notifications/read/{id}
     * Marks a single notification as read.
     */
    @PutMapping("/read/{id}")
    public Notification markRead(@PathVariable Long id) {
        Notification n = notificationRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
        n.setReadStatus(true);
        return notificationRepository.save(n);
    }
}