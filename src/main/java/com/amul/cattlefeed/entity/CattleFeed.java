package com.amul.cattlefeed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "cattle_feed")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CattleFeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id")
    private Long feedId;

    @Column(name = "feed_name", nullable = false, length = 100)
    private String feedName;

    @Column(name = "feed_type", nullable = false, length = 50)
    private String feedType;

    @Column(name = "price_per_bag", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerBag;

    @Column(name = "weight_per_bag", nullable = false)
    private Integer weightPerBag;

    @Column(nullable = false, name="available_stock")
    private Integer availableStock;

    @Column(name = "max_capacity")
    private Integer maxCapacity;


    @Transient
    public BigDecimal getPricePerKg() {
        if (weightPerBag == null || weightPerBag == 0) {
            return BigDecimal.ZERO;
        }

        return pricePerBag.divide(
                BigDecimal.valueOf(weightPerBag),
                2,
                RoundingMode.HALF_UP
        );
    }
}