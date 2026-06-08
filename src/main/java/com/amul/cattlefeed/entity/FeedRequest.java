package com.amul.cattlefeed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "feed_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    // FK -> Farmer
    @ManyToOne
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    // FK -> Feed (We assume Feed entity will exist)
    @ManyToOne
    @JoinColumn(name = "feed_id", nullable = false)
    private CattleFeed cattleFeed;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, length = 20)
    private String cycle;   // DAILY / WEEKLY / MONTHLY

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(nullable = false, length = 20)
    private String status;  // PENDING / APPROVED / REJECTED

    @Column(name = "is_cycle_active")
    private Boolean isCycleActive = true;

    @Column(length = 500)
    private String remarks;

    @Column(name = "approval_date")
    private LocalDate approvalDate;
}
