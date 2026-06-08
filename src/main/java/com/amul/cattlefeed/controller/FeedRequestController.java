package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.dto.StockDTO;
import com.amul.cattlefeed.entity.*;
import com.amul.cattlefeed.repository.*;
import com.amul.cattlefeed.service.FeedRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/secretary/feed-requests")
@RequiredArgsConstructor
public class FeedRequestController {

    private final FeedRequestService feedRequestService;
    private final FeedRequestRepository feedRequestRepository;
    private final FarmerRepository farmerRepository;
    private final CattleFeedRepository cattleFeedRepository;
    private final SecretaryRepository secretaryRepository;
    private final StockRepository stockRepository;

    @GetMapping("/pending")
    public List<FeedRequest> getPending(Authentication authentication) {
        return feedRequestService.getPendingRequests(authentication);
    }

    @PutMapping("/{id}/approve")
    public FeedRequest approve(@PathVariable Long id,
                               @RequestParam(required = false) String reason) {
        return feedRequestService.approveRequest(id, reason);
    }

    @PutMapping("/{id}/reject")
    public FeedRequest reject(@PathVariable Long id,
                              @RequestParam(required = false) String reason) {
        return feedRequestService.rejectRequest(id, reason);
    }

    @GetMapping
    public List<FeedRequest> getAll(Authentication authentication) {
        return feedRequestService.getAllRequests(authentication);
    }

    @GetMapping("/stock")
    public List<StockDTO> getSecretaryStock(Authentication authentication) {

        String loginId = authentication.getName();

        Secretary secretary = secretaryRepository
                .findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("Secretary not found"));

        List<Stock> stocks = stockRepository
                .findByZoneAndDistrict(
                        secretary.getZone(),
                        secretary.getDistrict()
                );

        return stocks.stream().map(s -> {
            StockDTO dto = new StockDTO();
            dto.setFeedId(s.getCattleFeed().getFeedId());
            dto.setFeedName(s.getCattleFeed().getFeedName());
            dto.setFeedType(s.getCattleFeed().getFeedType());
            dto.setPricePerBag(s.getCattleFeed().getPricePerBag());
            dto.setWeightPerBag(s.getCattleFeed().getWeightPerBag());
            dto.setAvailableStock(s.getQuantity());
            dto.setMaxCapacity(s.getCattleFeed().getMaxCapacity());
            dto.setLastRestocked(s.getLastUpdated());
            return dto;
        }).collect(Collectors.toList());
    }
}

