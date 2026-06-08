package com.amul.cattlefeed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long stockId;

    @ManyToOne
    @JoinColumn(name = "feed_id", nullable = false)
    private CattleFeed cattleFeed;

    @Column(nullable = false, length = 100)
    private String zone;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}