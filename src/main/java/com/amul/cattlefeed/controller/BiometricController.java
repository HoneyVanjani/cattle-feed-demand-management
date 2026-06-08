package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.BiometricData;
import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.entity.Secretary;
import com.amul.cattlefeed.repository.BiometricDataRepository;
import com.amul.cattlefeed.repository.FarmerRepository;
import com.amul.cattlefeed.repository.SecretaryRepository;
import com.amul.cattlefeed.security.JwtUtil;
import com.amul.cattlefeed.service.AuthenticationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/biometric")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class BiometricController {

    private final BiometricDataRepository biometricDataRepository;
    private final SecretaryRepository secretaryRepository;
    private final FarmerRepository farmerRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationLogService authLogService;

    // Threshold for Cosine Similarity (face descriptors)
    private static final double THRESHOLD = 0.41;

    @PostMapping("/face/verify")
    public ResponseEntity<?> verifyFace(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String loginId = payload.get("loginId");
        String faceDescriptorSubmit = payload.get("faceDescriptor");
        String userType = payload.get("userType"); // SECRETARY or FARMER

        if (loginId == null || faceDescriptorSubmit == null || userType == null) {
            return ResponseEntity.badRequest().body("Missing required parameters: loginId, faceDescriptor, userType");
        }

        try {
            Long userId = null;
            Object userObject = null;

            if ("SECRETARY".equalsIgnoreCase(userType)) {
                Optional<Secretary> secOpt = secretaryRepository.findByLoginId(loginId);
                if (secOpt.isPresent()) {
                    userId = secOpt.get().getId();
                    userObject = secOpt.get();
                }
            } else if ("FARMER".equalsIgnoreCase(userType)) {
                Optional<Farmer> farOpt = farmerRepository.findBySabhasadNo(loginId);
                if (farOpt.isPresent()) {
                    userId = farOpt.get().getFarmerId();
                    userObject = farOpt.get();
                }
            }

            if (userId == null) {
                authLogService.logActivity(loginId, userType, "FAILED_LOGIN", request.getRemoteAddr(), false,
                        "User not found for face verification");
                return ResponseEntity.status(401).body("User not found.");
            }

            BiometricData bio = biometricDataRepository.findByUserIdAndUserType(userId, userType.toUpperCase()).orElse(null);
            if (bio == null || bio.getFaceEmbedding() == null) {
                authLogService.logActivity(loginId, userType, "FAILED_LOGIN", request.getRemoteAddr(), false,
                        "No biometric data registered");
                return ResponseEntity.status(401).body("No biometric data registered for this user.");
            }

            // Client-side comparison is usually done because sending 128-float arrays
            // stringified is heavy,
            // but since we receive it here, we compare Euclidean distance or Cosine
            // similarity.
            // For simplicity, we assume the frontend sent a stringified JSON float array,
            // and we do Euclidean distance.
            double distance = calculateEuclideanDistance(bio.getFaceEmbedding(), faceDescriptorSubmit);
            System.out.println("FACE DISTANCE: " + distance);
            boolean isMatch = distance < THRESHOLD;

            if (isMatch) {
                // Success: Generate Token
                String token = jwtUtil.generateToken(loginId, userType.toUpperCase());
                authLogService.logActivity(loginId, userType, "LOGIN", request.getRemoteAddr(), true,
                        "Face Verification Success");

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("role", userType.toUpperCase());
                response.put("loginId", loginId);

                if (userObject instanceof Secretary) {
                    Secretary sec = (Secretary) userObject;
                    response.put("name", sec.getName());
                    response.put("zone", sec.getZone());
                    response.put("district", sec.getDistrict());
                    response.put("societyCode", sec.getSocietyCode());
                } else if (userObject instanceof Farmer) {
                    Farmer far = (Farmer) userObject;
                    response.put("name", far.getName());
                    response.put("zone", far.getZone());
                    response.put("district", far.getDistrict());
                }

                return ResponseEntity.ok(response);
            } else {
                authLogService.logActivity(loginId, userType, "FAILED_LOGIN", request.getRemoteAddr(), false,
                        "Face mismatch");
                return ResponseEntity.status(401).body("Face mismatch.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error verifying face: " + e.getMessage());
        }
    }

    private double calculateEuclideanDistance(String stored, String submitted) {
        try {
            String[] s1 = stored.replace("[", "").replace("]", "").split(",");
            String[] s2 = submitted.replace("[", "").replace("]", "").split(",");

            if (s1.length != 128 || s2.length != 128) {
                return Double.MAX_VALUE; // invalid descriptor
            }

            double sum = 0.0;
            for (int i = 0; i < 128; i++) {
                double a = Double.parseDouble(s1[i].trim());
                double b = Double.parseDouble(s2[i].trim());
                double diff = a - b;
                sum += diff * diff;
            }

            return Math.sqrt(sum);
        } catch (Exception e) {
            e.printStackTrace();
            return Double.MAX_VALUE;
        }
    }

}
