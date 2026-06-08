package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.entity.Stock;
import com.amul.cattlefeed.entity.StockMovement;
import com.amul.cattlefeed.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminDashboardController {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final FeedRequestRepository feedRequestRepository;
    private final FarmerRepository farmerRepository;
    private final SecretaryRepository secretaryRepository;

    // ================= DASHBOARD SUMMARY =================

    @GetMapping
    public Map<String, Object> getDashboard() {

        Map<String, Object> data = new HashMap<>();

        List<Stock> stocks = stockRepository.findAll();

        double totalStockAvailable = stocks
                .stream()
                .mapToDouble(Stock::getQuantity)
                .sum();

        /* LOW STOCK ALERT LOGIC */

        long lowStockAlerts = stocks
                .stream()
                .filter(s -> s.getQuantity() < 50)
                .count();

        data.put("totalZones",
                stockRepository.findAll()
                        .stream()
                        .map(Stock::getZone)
                        .distinct()
                        .count());

        data.put("totalDistricts",
                stockRepository.findAll()
                        .stream()
                        .map(Stock::getDistrict)
                        .distinct()
                        .count());


        data.put("totalSecretaries", secretaryRepository.count());

        data.put("totalSocieties",
                secretaryRepository.findAll()
                        .stream()
                        .map(com.amul.cattlefeed.entity.Secretary::getSocietyCode)
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .count());

        data.put("totalStockAvailable", totalStockAvailable);

        data.put("lowStockAlerts", lowStockAlerts);

        data.put("pendingFeedRequests",
                feedRequestRepository.findAll()
                        .stream()
                        .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                        .count());

        // ✅ Total farmers count
        data.put("totalFarmers", farmerRepository.count());

        // ✅ Farmers per zone breakdown
        List<Farmer> allFarmers = farmerRepository.findAll();
        Map<String, Long> farmersByZone = allFarmers.stream()
                .filter(f -> f.getZone() != null)
                .collect(Collectors.groupingBy(
                        com.amul.cattlefeed.entity.Farmer::getZone,
                        Collectors.counting()
                ));

        List<Map<String, Object>> farmersByZoneList = new ArrayList<>();
        farmersByZone.forEach((zone, count) -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("zone", zone);
            entry.put("count", count);
            farmersByZoneList.add(entry);
        });
        data.put("farmersByZone", farmersByZoneList);

        return data;
    }

    @GetMapping("/feed-demand-trend")
    public List<Map<String, Object>> getFeedDemandTrend() {

        List<Object[]> data = feedRequestRepository.getMonthlyFeedRequests();

        List<Map<String, Object>> result = new ArrayList<>();

        String[] months = {
                "Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"
        };

        for(Object[] row : data){

            int monthIndex = ((Number) row[0]).intValue() - 1;
            long count = ((Number) row[1]).longValue();

            Map<String,Object> map = new HashMap<>();
            map.put("month", months[monthIndex]);
            map.put("requests", count);

            result.add(map);
        }

        return result;
    }


    // ================= STOCK IN/OUT CYCLE =================

    @GetMapping("/cycle")
    public List<Map<String, Object>> getCycleStock(
            @RequestParam(required = false) String zone
    ) {

        List<StockMovement> movements = stockMovementRepository.findAll();

        if (zone != null) {
            movements = movements.stream()
                    .filter(m -> m.getStock().getZone().equalsIgnoreCase(zone))
                    .collect(Collectors.toList());
        }

        Map<String, Integer> cycleIn = new HashMap<>();
        Map<String, Integer> cycleOut = new HashMap<>();

        for (StockMovement m : movements) {

            int day = m.getMovementDate().getDayOfMonth();

            String cycle =
                    day <= 10 ? "1-10" :
                            day <= 20 ? "11-20" :
                                    "21-31";

            if ("IN".equalsIgnoreCase(m.getMovementType())) {
                cycleIn.put(cycle,
                        cycleIn.getOrDefault(cycle, 0) + m.getQuantity());
            } else if ("OUT".equalsIgnoreCase(m.getMovementType())) {
                cycleOut.put(cycle,
                        cycleOut.getOrDefault(cycle, 0) + m.getQuantity());
            }
        }

        List<String> cycles = List.of("1-10", "11-20", "21-31");

        List<Map<String, Object>> result = new ArrayList<>();

        for (String c : cycles) {
            Map<String, Object> map = new HashMap<>();
            map.put("cycle", c);
            map.put("stockIn", cycleIn.getOrDefault(c, 0));
            map.put("stockOut", cycleOut.getOrDefault(c, 0));
            result.add(map);
        }

        return result;
    }

    // ================= ZONE WISE STOCK =================

    @GetMapping("/zone-stock")
    public List<Map<String, Object>> getZoneStock() {

        List<Stock> stocks = stockRepository.findAll();

        Map<String, Double> zoneStock = new HashMap<>();

        for (Stock s : stocks) {
            zoneStock.put(
                    s.getZone(),
                    zoneStock.getOrDefault(s.getZone(), 0.0) + s.getQuantity()
            );
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<String, Double> e : zoneStock.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("zone", e.getKey());
            map.put("stock", e.getValue());
            result.add(map);
        }

        return result;
    }
}