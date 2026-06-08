package com.amul.cattlefeed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "stock_movement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long movementId;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "movement_type", nullable = false)
    private String movementType;  // IN / OUT

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(nullable = false)
    private String cycle;

    @Column(name = "is_emergency")
    private Boolean isEmergency = false;
}