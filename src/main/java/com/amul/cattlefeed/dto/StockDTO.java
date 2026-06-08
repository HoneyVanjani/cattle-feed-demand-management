package com.amul.cattlefeed.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockDTO {
    private Long feedId;
    private String feedName;
    private String feedType;
    private BigDecimal pricePerBag;
    private Integer weightPerBag;
    private Double availableStock;
    private Integer maxCapacity;
    private LocalDateTime lastRestocked;
}