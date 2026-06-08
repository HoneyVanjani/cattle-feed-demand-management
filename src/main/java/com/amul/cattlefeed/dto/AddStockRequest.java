package com.amul.cattlefeed.dto;

import lombok.Data;

@Data
public class AddStockRequest {
    private Long feedId;
    private String zone;
    private String district;
    private Integer quantity;
    private String movementType;
    private String cycle;
}