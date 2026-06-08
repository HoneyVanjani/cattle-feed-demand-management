package com.amul.cattlefeed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String title;

    private String message;

    private String type;

    @Column(name = "user_type", length = 20)
    private String userType = "FARMER"; // "FARMER" or "SECRETARY"

    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean readStatus = false;
}