package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.CattleFeed;
import com.amul.cattlefeed.repository.CattleFeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secretary/feed")
@RequiredArgsConstructor
@CrossOrigin
public class CattleFeedController {

    private final CattleFeedRepository cattleFeedRepository;

    // 🔹 1️⃣ Get All Feeds
    @GetMapping
    public List<CattleFeed> getAllFeeds() {
        return cattleFeedRepository.findAll();
    }

    // 🔹 2️⃣ Add New Feed
    @PostMapping
    public CattleFeed createFeed(@RequestBody CattleFeed feed) {
        return cattleFeedRepository.save(feed);
    }

    // 🔹 3️⃣ Update Feed
    @PutMapping("/{id}")
    public CattleFeed updateFeed(
            @PathVariable Long id,
            @RequestBody CattleFeed updatedFeed
    ) {
        CattleFeed feed = cattleFeedRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        feed.setFeedName(updatedFeed.getFeedName());
        feed.setFeedType(updatedFeed.getFeedType());
        feed.setPricePerBag(updatedFeed.getPricePerBag());
        feed.setWeightPerBag(updatedFeed.getWeightPerBag());
        return cattleFeedRepository.save(feed);
    }

    // 🔹 4️⃣ Delete Feed
    @DeleteMapping("/{id}")
    public void deleteFeed(@PathVariable Long id) {
        cattleFeedRepository.deleteById(id);
    }


    @GetMapping("/farmer")
    public List<CattleFeed> getAllFeedsFarmer() {
        return cattleFeedRepository.findAll();
    }
}
