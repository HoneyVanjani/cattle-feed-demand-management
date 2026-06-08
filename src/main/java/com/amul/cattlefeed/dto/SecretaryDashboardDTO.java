package com.amul.cattlefeed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecretaryDashboardDTO {

    private long totalFarmers;
    private long pendingRequests;
    private long approvedRequests;
    private double availableStock;
}