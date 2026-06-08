package com.amul.cattlefeed.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "authentication_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", length = 100)
    private String loginId;

    @Column(name = "user_type", length = 20)
    private String userType;

    @Column(name = "action", length = 50)
    private String action; // LOGIN, LOGOUT, FAILED_LOGIN, PROFILE_CREATED

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "failure_reason", length = 200)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false)
    private LocalDateTime timestamp;
}
