package com.amul.cattlefeed.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    private String name;

    @Column(unique = true)
    private String loginId;

    private String password;

    private String status;

    private LocalDateTime createdAt = LocalDateTime.now();
}