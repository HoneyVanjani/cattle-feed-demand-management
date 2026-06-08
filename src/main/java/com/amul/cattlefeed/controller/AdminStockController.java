package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.dto.AddStockRequest;
import com.amul.cattlefeed.entity.*;
import com.amul.cattlefeed.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stock")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminStockController {

    private final StockRepository stockRepository;
    private final CattleFeedRepository cattleFeedRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NotificationRepository notificationRepository;
    private final SecretaryRepository secretaryRepository;
    private final AdminRepository adminRepository;

    // ✅ Current cycle
    private String getCurrentCycle() {
        int day = LocalDate.now().getDayOfMonth();
        int lastDay = YearMonth.now().lengthOfMonth();
        if (day <= 10) return "1-10";
        if (day <= 20) return "11-20";
        return "21-" + lastDay;
    }

    // ✅ Next cycle
    private String getNextCycle() {
        int day = LocalDate.now().getDayOfMonth();
        int lastDay = YearMonth.now().lengthOfMonth();
        if (day <= 10) return "11-20";
        if (day <= 20) return "21-" + lastDay;
        return "1-10"; // next month
    }

    // ✅ Expose cycles to frontend
    @GetMapping("/cycles")
    public Map<String, String> getAvailableCycles() {
        return Map.of(
                "currentCycle", getCurrentCycle(),
                "nextCycle", getNextCycle()
        );
    }

    @GetMapping("/feed")
    public List<CattleFeed> getFeedsForAdmin() {
        return cattleFeedRepository.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<?> addStock(@RequestBody AddStockRequest request) {

        // ✅ Validate fields
        if (request.getCycle() == null || request.getCycle().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cycle is required"));
        }
        if (request.getFeedId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Feed is required"));
        }

        String currentCycle = getCurrentCycle();
        String nextCycle = getNextCycle();
        boolean isEmergency = "EMERGENCY_IN"
                .equalsIgnoreCase(request.getMovementType());

        // ✅ Validate cycle is current or next only
        boolean isCurrentCycle = request.getCycle().equals(currentCycle);
        boolean isNextCycle = request.getCycle().equals(nextCycle);

        if (!isEmergency && !isCurrentCycle && !isNextCycle) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Only current cycle (" + currentCycle +
                            ") or next cycle (" + nextCycle +
                            ") is allowed."
            ));
        }

        // ✅ Cycle lock — ONLY for current cycle
        // Next cycle is open (multiple deliveries allowed)
        if (!isEmergency && isCurrentCycle &&
                "IN".equalsIgnoreCase(request.getMovementType())) {

            boolean alreadyAdded = stockMovementRepository
                    .existsNormalStockForCycleAndZoneAndDistrict(
                            request.getZone(),
                            request.getDistrict(),
                            request.getCycle(),
                            request.getFeedId()
                    );

            if (alreadyAdded) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Stock for this feed already added for " +
                                "current cycle (" + currentCycle + ") in " +
                                request.getDistrict() +
                                ". Use Emergency Restock to add more."
                ));
            }
        }

        // ✅ Find feed
        CattleFeed feed = cattleFeedRepository.findById(request.getFeedId())
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        // ✅ Find or create stock
        Stock stock = stockRepository
                .findByCattleFeed_FeedIdAndZoneAndDistrict(
                        request.getFeedId(),
                        request.getZone(),
                        request.getDistrict()
                )
                .orElse(null);

        if (stock == null) {
            stock = new Stock();
            stock.setCattleFeed(feed);
            stock.setZone(request.getZone());
            stock.setDistrict(request.getDistrict());
            stock.setQuantity(0.0);
        }

        // ✅ Apply movement
        if ("OUT".equalsIgnoreCase(request.getMovementType())) {
            if (stock.getQuantity() < request.getQuantity()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Not enough stock. Available: " +
                                stock.getQuantity() + " kg"
                ));
            }
            stock.setQuantity(stock.getQuantity() - request.getQuantity());
        } else {
            stock.setQuantity(stock.getQuantity() + request.getQuantity());
            stock.setLastUpdated(LocalDateTime.now());
        }

        stockRepository.save(stock);

        // ✅ Save movement
        StockMovement movement = new StockMovement();
        movement.setStock(stock);
        movement.setQuantity(request.getQuantity());
        movement.setMovementType(
                isEmergency ? "EMERGENCY_IN" : request.getMovementType());
        movement.setMovementDate(LocalDate.now());
        movement.setCycle(request.getCycle());
        movement.setIsEmergency(isEmergency);
        stockMovementRepository.save(movement);

        // ✅ Notify secretaries
        if ("IN".equalsIgnoreCase(request.getMovementType()) || isEmergency) {
            List<Secretary> secretaries = secretaryRepository
                    .findByZoneAndDistrict(
                            request.getZone(), request.getDistrict());

            for (Secretary secretary : secretaries) {
                Notification n = new Notification();
                n.setUserId(secretary.getId());
                n.setTitle(isEmergency
                        ? "Emergency Stock Added" : "New Stock Added");
                n.setMessage(
                        request.getQuantity() + " kg of " +
                                feed.getFeedName() +
                                (isEmergency ? " (Emergency)" : "") +
                                " added for cycle " + request.getCycle() +
                                " in " + request.getDistrict()
                );
                n.setType(isEmergency ? "WARNING" : "INFO");
                n.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(n);
            }
        }

        // ✅ Low stock alert
        double threshold = 200.0;
        double criticalThreshold = 100.0;

        if (stock.getQuantity() < threshold) {
            String alertTitle = stock.getQuantity() < criticalThreshold
                    ? "CRITICAL Stock Alert" : "Low Stock Alert";
            String alertMessage = feed.getFeedName() + " stock is " +
                    (stock.getQuantity() < criticalThreshold
                            ? "CRITICAL" : "LOW") +
                    " (" + stock.getQuantity() + " kg remaining) in " +
                    request.getDistrict();

            List<Secretary> secretaries = secretaryRepository
                    .findByZoneAndDistrict(
                            request.getZone(), request.getDistrict());

            for (Secretary secretary : secretaries) {
                Notification sn = new Notification();
                sn.setUserId(secretary.getId());
                sn.setTitle(alertTitle);
                sn.setMessage(alertMessage);
                sn.setType("WARNING");
                sn.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(sn);
            }
        }

        return ResponseEntity.ok(Map.of("message", isEmergency
                ? "Emergency stock added successfully"
                : "Stock added successfully for cycle "
                + request.getCycle()
        ));
    }

    @GetMapping
    public List<Stock> getStock(
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String district
    ) {
        if (zone != null && district != null) {
            return stockRepository.findByZoneAndDistrict(zone, district);
        }
        return stockRepository.findAll();
    }

    @GetMapping("/movements")
    public List<StockMovement> getAllMovements() {
        return stockMovementRepository.findAll();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SECRETARY')")
    @GetMapping("/consumption")
    public Map<Long, Double> getMonthlyConsumption() {
        List<Object[]> results = stockMovementRepository
                .getMonthlyFeedConsumption();
        Map<Long, Double> consumption = new java.util.HashMap<>();
        for (Object[] row : results) {
            Long feedId = (Long) row[0];
            Double quantity = ((Number) row[1]).doubleValue();
            consumption.put(feedId, quantity);
        }
        return consumption;
    }

    // ✅ Cycle check — only for current cycle lock
    @GetMapping("/cycle-check")
    public Map<String, Object> checkCycleStock(
            @RequestParam String zone,
            @RequestParam String district,
            @RequestParam String cycle,
            @RequestParam Long feedId
    ) {
        String currentCycle = getCurrentCycle();
        // Only check lock if it's current cycle
        boolean alreadyAdded = cycle.equals(currentCycle) &&
                stockMovementRepository
                        .existsNormalStockForCycleAndZoneAndDistrict(
                                zone, district, cycle, feedId);

        return Map.of(
                "alreadyAdded", alreadyAdded,
                "currentCycle", currentCycle,
                "nextCycle", getNextCycle()
        );
    }

    @GetMapping("/low-stock-alerts")
    public List<Map<String, Object>> getLowStockAlerts() {
        List<Stock> allStock = stockRepository.findAll();
        List<Map<String, Object>> alerts = new ArrayList<>();
        double threshold = 200.0;
        double criticalThreshold = 100.0;
        for (Stock stock : allStock) {
            if (stock.getQuantity() < threshold) {
                alerts.add(Map.of(
                        "feedName", stock.getCattleFeed().getFeedName(),
                        "zone", stock.getZone(),
                        "district", stock.getDistrict(),
                        "quantity", stock.getQuantity(),
                        "level", stock.getQuantity() < criticalThreshold
                                ? "CRITICAL" : "LOW"
                ));
            }
        }
        return alerts;
    }
}