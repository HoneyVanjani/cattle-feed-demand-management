package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.dto.FeedRequestDTO;
import com.amul.cattlefeed.entity.CattleFeed;
import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.entity.FeedRequest;
import com.amul.cattlefeed.entity.Secretary;
import com.amul.cattlefeed.repository.CattleFeedRepository;
import com.amul.cattlefeed.repository.FarmerRepository;
import com.amul.cattlefeed.repository.FeedRequestRepository;
import com.amul.cattlefeed.repository.SecretaryRepository;
import com.amul.cattlefeed.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/farmer")
public class FarmerFeedController {

    private final CattleFeedRepository cattleFeedRepository;
    private final FarmerRepository farmerRepository;
    private final FeedRequestRepository feedRequestRepository;
    private final SecretaryRepository secretaryRepository;
    private final NotificationService notificationService;

    @GetMapping("/feed")
    public List<CattleFeed> getFeedsForFarmer() {
        return cattleFeedRepository.findAll();
    }

    @PostMapping("/create")
    public FeedRequest createRequest(
            @RequestBody FeedRequestDTO requestDTO,
            Authentication authentication
    ) {

        String sabhasadNo = authentication.getName();

        Farmer farmer = farmerRepository.findBySabhasadNo(sabhasadNo)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        CattleFeed feed = cattleFeedRepository.findById(requestDTO.getFeedId())
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        FeedRequest request = new FeedRequest();
        request.setFarmer(farmer);
        request.setCattleFeed(feed);
        request.setQuantity(requestDTO.getQuantity());
        request.setCycle(requestDTO.getCycle());
        request.setRequestDate(LocalDate.now());
        request.setStatus("PENDING");

        FeedRequest savedRequest = feedRequestRepository.save(request);

        // ✅ Find the specific secretary for this farmer's society
        secretaryRepository.findBySocietyCode(farmer.getSocietyCode())
                .ifPresent(secretary -> {
                    notificationService.createNotification(
                            secretary.getId(),
                            "New Feed Request",
                            farmer.getName() + " requested " + requestDTO.getQuantity()
                                    + "kg of " + feed.getFeedName(),
                            "info",
                            secretary.getMobile(),
                            "SECRETARY"
                    );
                });

        return savedRequest;
    }

    @GetMapping("/requests")
    public List<FeedRequest> getMyRequests(Authentication authentication) {

        String sabhasadNo = authentication.getName();

        Farmer farmer = farmerRepository.findBySabhasadNo(sabhasadNo)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        return feedRequestRepository.findByFarmer(farmer);
    }
}