package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.FeedRequest;
import com.amul.cattlefeed.repository.FeedRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/requests")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminFeedRequestController {

    private final FeedRequestRepository feedRequestRepository;

    @GetMapping
    public List<FeedRequest> getAllRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String district
    ) {

        if (status != null && zone != null && district != null) {
            return feedRequestRepository
                    .findByStatusAndFarmer_ZoneAndFarmer_District(status, zone, district);
        }

        if (status != null) {
            return feedRequestRepository.findByStatus(status);
        }

        if (zone != null && district != null) {
            return feedRequestRepository
                    .findByFarmer_ZoneAndFarmer_District(zone, district);
        }

        return feedRequestRepository.findAll();
    }
}