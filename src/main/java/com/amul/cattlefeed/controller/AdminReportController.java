package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.StockMovement;
import com.amul.cattlefeed.repository.StockMovementRepository;
import com.amul.cattlefeed.repository.FeedRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminReportController {

    private final StockMovementRepository stockMovementRepository;
    private final FeedRequestRepository feedRequestRepository;

    // 🔹 Cycle wise report
    @GetMapping("/cycle")
    public List<StockMovement> getCycleReport(
            @RequestParam(required = false) String cycle
    ) {

        List<StockMovement> all = stockMovementRepository.findAll();

        if (cycle == null) return all;

        return all.stream().filter(m -> {
            int day = m.getMovementDate().getDayOfMonth();

            switch (cycle) {
                case "1-10": return day >= 1 && day <= 10;
                case "11-20": return day >= 11 && day <= 20;
                case "21-31": return day >= 21 && day <= 31;
                default: return true;
            }
        }).collect(Collectors.toList());
    }

    // 🔹 Zone wise approved/rejected summary
    @GetMapping("/zone")
    public List<Map<String, Object>> getZoneReport() {

        List<Object[]> data = feedRequestRepository.getZoneWiseStatusCount();

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : data) {
            Map<String, Object> map = new HashMap<>();
            map.put("zone", row[0]);
            map.put("approved", row[1]);
            map.put("rejected", row[2]);
            result.add(map);
        }

        return result;
    }
}