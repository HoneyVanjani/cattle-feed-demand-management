package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Filter by BOTH userId AND userType to prevent cross-role notification leakage
    List<Notification> findByUserIdAndUserTypeOrderByCreatedAtDesc(Long userId, String userType);

    // Legacy fallback (kept for safety)
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}