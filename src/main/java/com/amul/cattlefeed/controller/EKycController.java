package com.amul.cattlefeed.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/ekyc")
@CrossOrigin(origins = "http://localhost:5173")
public class EKycController {

    // Simple cache for OTPs (email -> OTP)
    private final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();

    @PostMapping("/aadhaar/ocr")
    public ResponseEntity<?> parseAadhaarOcr(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "aadhaarNumber", required = false) String aadhaarNumber) {
        
        Map<String, Object> response = new HashMap<>();

        // Simulated OCR: If aadhaarNumber is provided and starts with a specific pattern (e.g. 1234), or just valid 12 digits
        if (aadhaarNumber != null && aadhaarNumber.matches("\\d{12}")) {
            response.put("status", "VERIFIED");
            response.put("message", "Aadhaar verified successfully");
            response.put("aadhaar", aadhaarNumber);
            return ResponseEntity.ok(response);
        }

        // If uploaded file, simulation
        if (file != null && !file.isEmpty()) {
            response.put("status", "VERIFIED");
            response.put("message", "Aadhaar extracted and verified");
            // Dummy extraction
            response.put("aadhaar", "123456789012");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(400).body(Map.of("status", "FAILED", "message", "Invalid Aadhaar or unreadable document"));
    }

    @PostMapping("/email/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        // Store in cache
        otpStore.put(email, otp);

        // Simulation: Just print it, since sending real email requires JavaMailSender and SMTP config
        System.out.println("====== OTP FOR " + email + " IS: " + otp + " ======");

        return ResponseEntity.ok(Map.of("message", "OTP sent successfully to " + email));
    }

    @PostMapping("/email/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "FAILED", "message", "Email and OTP are required"));
        }

        String storedOtp = otpStore.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStore.remove(email); // consume OTP
            return ResponseEntity.ok(Map.of("status", "VERIFIED", "message", "Email verified successfully"));
        }

        return ResponseEntity.status(401).body(Map.of("status", "FAILED", "message", "Invalid or expired OTP"));
    }
}
