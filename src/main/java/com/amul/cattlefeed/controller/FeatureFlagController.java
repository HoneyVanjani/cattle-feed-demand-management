package com.amul.cattlefeed.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/features")
@CrossOrigin(origins = "http://localhost:5173")
public class FeatureFlagController {

    @GetMapping
    public Map<String, Boolean> getFeatureFlags() {
        Map<String, Boolean> flags = new HashMap<>();
        flags.put("enableFaceLogin", true);
        flags.put("enableFingerprint", false);
        flags.put("enableEmailOTP", true);
        flags.put("enableAadhaarOCR", true);
        return flags;
    }
}
