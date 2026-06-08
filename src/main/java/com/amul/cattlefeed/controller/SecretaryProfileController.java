package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.Secretary;
import com.amul.cattlefeed.repository.SecretaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/secretary/profile")
@RequiredArgsConstructor
public class SecretaryProfileController {

    private final SecretaryRepository secretaryRepository;

    // GET FULL PROFILE
    @GetMapping("/{loginId}")
    public Secretary getProfile(@PathVariable String loginId) {
        Secretary sec = secretaryRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("Secretary not found"));
        // Clear sensitive data before sending
        sec.setPassword(null);
        sec.setAadhaarNumber(null);
        return sec;
    }

    // UPDATE MOBILE and/or EMAIL
    @PutMapping("/{loginId}")
    public Secretary updateProfile(@PathVariable String loginId,
                                   @RequestBody Map<String, String> body) {
        Secretary sec = secretaryRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("Secretary not found"));

        if (body.containsKey("mobile") && body.get("mobile") != null) {
            sec.setMobile(body.get("mobile"));
        }
        if (body.containsKey("email") && body.get("email") != null) {
            sec.setEmail(body.get("email"));
        }

        Secretary saved = secretaryRepository.save(sec);
        saved.setPassword(null);
        saved.setAadhaarNumber(null);
        return saved;
    }
}