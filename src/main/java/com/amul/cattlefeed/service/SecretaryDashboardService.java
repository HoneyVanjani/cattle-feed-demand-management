package com.amul.cattlefeed.service;

import com.amul.cattlefeed.dto.SecretaryDashboardDTO;
import com.amul.cattlefeed.entity.Secretary;
import com.amul.cattlefeed.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecretaryDashboardService {

    private final FarmerRepository farmerRepository;
    private final FeedRequestRepository feedRequestRepository;
    private final StockRepository stockRepository;
    private final SecretaryRepository secretaryRepository;

    public SecretaryDashboardDTO getDashboardStats() {

        // ✅ Get current secretary from token
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        String loginId = auth.getName();

        Secretary secretary = secretaryRepository
                .findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException(
                        "Secretary not found"));

        String zone = secretary.getZone();
        String district = secretary.getDistrict();

        // ✅ Only farmers in secretary's zone+district
        long totalFarmers = farmerRepository
                .countByZoneAndDistrict(zone, district);

        // ✅ Only pending requests in secretary's zone+district
        long pendingRequests = feedRequestRepository
                .countByStatusAndFarmer_ZoneAndFarmer_District(
                        "PENDING", zone, district);

        // ✅ Only approved requests in secretary's zone+district
        long approvedRequests = feedRequestRepository
                .countByStatusAndFarmer_ZoneAndFarmer_District(
                        "APPROVED", zone, district);

        // ✅ Stock for secretary's zone+district
        double availableStock = stockRepository
                .findByZoneAndDistrict(zone, district)
                .stream()
                .mapToDouble(s -> s.getQuantity())
                .sum();

        return new SecretaryDashboardDTO(
                totalFarmers,
                pendingRequests,
                approvedRequests,
                availableStock
        );
    }
}