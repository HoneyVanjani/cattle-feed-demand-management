package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.dto.FarmerLoginRequest;
import com.amul.cattlefeed.dto.LoginRequest;
import com.amul.cattlefeed.dto.LoginResponse;
import com.amul.cattlefeed.entity.Admin;
import com.amul.cattlefeed.entity.Secretary;
import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.repository.AdminRepository;
import com.amul.cattlefeed.repository.SecretaryRepository;
import com.amul.cattlefeed.repository.FarmerRepository;
import com.amul.cattlefeed.security.JwtUtil;
import com.amul.cattlefeed.security.AesEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import com.amul.cattlefeed.service.LoginAttemptService;
import com.amul.cattlefeed.service.AuthenticationLogService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AdminRepository adminRepository;
    private final SecretaryRepository secretaryRepository;
    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationLogService authLogService;
    private final LoginAttemptService loginAttemptService;
    private final AesEncryptionUtil aesEncryptionUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {

        String ipAddress = httpRequest.getRemoteAddr();
        String loginId = request.getLoginId();

        if (loginAttemptService.isBlocked(loginId)) {
            authLogService.logActivity(loginId, request.getRole(), "FAILED_LOGIN", ipAddress, false,
                    "Account is temporarily locked due to multiple failed attempts");
            return ResponseEntity.status(423)
                    .body("Account locked due to too many failed attempts. Please try again after 15 minutes.");
        }

        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            return adminRepository.findByLoginId(request.getLoginId()).map(admin -> {
                if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                    loginAttemptService.loginFailed(loginId);
                    authLogService.logActivity(request.getLoginId(), "ADMIN", "FAILED_LOGIN", ipAddress, false,
                            "Invalid password");
                    return ResponseEntity.status(401).body((Object) "Invalid credentials");
                }
                loginAttemptService.loginSucceeded(loginId);
                String token = jwtUtil.generateToken(admin.getLoginId(), "ADMIN");
                authLogService.logActivity(request.getLoginId(), "ADMIN", "LOGIN", ipAddress, true, null);
                return ResponseEntity
                        .ok((Object) new LoginResponse(token, "ADMIN", admin.getLoginId(), admin.getName()));
            }).orElseGet(() -> {
                loginAttemptService.loginFailed(loginId);
                authLogService.logActivity(request.getLoginId(), "ADMIN", "FAILED_LOGIN", ipAddress, false,
                        "Admin not found");
                return ResponseEntity.status(401).body((Object) "Invalid credentials");
            });

        } else if ("SECRETARY".equalsIgnoreCase(request.getRole())) {
            return secretaryRepository.findByLoginId(request.getLoginId()).map(secretary -> {
                if (!passwordEncoder.matches(request.getPassword(), secretary.getPassword())) {
                    loginAttemptService.loginFailed(loginId);
                    authLogService.logActivity(request.getLoginId(), "SECRETARY", "FAILED_LOGIN", ipAddress, false,
                            "Invalid password");
                    return ResponseEntity.status(401).body((Object) "Invalid credentials");
                }
                loginAttemptService.loginSucceeded(loginId);
                String token = jwtUtil.generateToken(secretary.getLoginId(), secretary.getRole());
                authLogService.logActivity(request.getLoginId(), "SECRETARY", "LOGIN", ipAddress, true, null);

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("role", secretary.getRole());
                response.put("name", secretary.getName());
                response.put("loginId", secretary.getLoginId());
                response.put("zone", secretary.getZone());
                response.put("district", secretary.getDistrict());
                response.put("societyCode", secretary.getSocietyCode());
                return ResponseEntity.ok((Object) response);
            }).orElseGet(() -> {
                loginAttemptService.loginFailed(loginId);
                authLogService.logActivity(request.getLoginId(), "SECRETARY", "FAILED_LOGIN", ipAddress, false,
                        "Secretary not found");
                return ResponseEntity.status(401).body((Object) "Invalid credentials");
            });
        }

        authLogService.logActivity(request.getLoginId(), "UNKNOWN", "FAILED_LOGIN", ipAddress, false, "Invalid role");
        return ResponseEntity.badRequest().body("Invalid role");
    }

    @PostMapping("/farmer/login")
    public ResponseEntity<?> farmerLogin(@RequestBody FarmerLoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String loginId = request.getSabhasadNo();

        if (loginAttemptService.isBlocked(loginId)) {
            authLogService.logActivity(loginId, "FARMER", "FAILED_LOGIN", ipAddress, false,
                    "Account is temporarily locked due to multiple failed attempts");
            return ResponseEntity.status(423)
                    .body("Account locked due to too many failed attempts. Please try again after 15 minutes.");
        }

        return farmerRepository.findBySabhasadNo(request.getSabhasadNo())
                .map(farmer -> {
                    // Check password only when one has been explicitly set (backward-compatible)
                    if (farmer.getPassword() != null && !farmer.getPassword().isBlank()
                            && request.getPassword() != null && !request.getPassword().isBlank()) {
                        if (!passwordEncoder.matches(request.getPassword(), farmer.getPassword())) {
                            loginAttemptService.loginFailed(loginId);
                            authLogService.logActivity(loginId, "FARMER", "FAILED_LOGIN", ipAddress, false,
                                    "Invalid password");
                            return ResponseEntity.status(401).body((Object) "Invalid credentials");
                        }
                    }
                    loginAttemptService.loginSucceeded(loginId);
                    String token = jwtUtil.generateToken(farmer.getSabhasadNo(), "FARMER");
                    authLogService.logActivity(farmer.getSabhasadNo(), "FARMER", "LOGIN", ipAddress, true, null);

                    Map<String, Object> response = new HashMap<>();
                    response.put("token", token);
                    response.put("role", "FARMER");
                    response.put("name", farmer.getName());
                    response.put("loginId", farmer.getSabhasadNo());
                    response.put("zone", farmer.getZone());
                    response.put("district", farmer.getDistrict());

                    return ResponseEntity.ok((Object) response);
                }).orElseGet(() -> {
                    loginAttemptService.loginFailed(loginId);
                    return ResponseEntity.status(401).body((Object) "Invalid credentials");
                });
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload, HttpServletRequest request) {

        String role = payload.get("role");
        String aadhaar = payload.get("aadhaarNumber");
        String dobString = payload.get("dob");
        String newPassword = payload.get("newPassword");

        if (role == null || aadhaar == null || dobString == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
        }

        try {
            LocalDate dob = LocalDate.parse(dobString);

            
            aadhaar = aadhaar.replaceAll("\\s", "");

            if ("SECRETARY".equalsIgnoreCase(role)) {

                Secretary matchedSecretary = null;

                // Pre-filter by DOB in DB to avoid full table scan
                for (Secretary sec : secretaryRepository.findByBirthdate(dob)) {
                    try {
                        String decrypted = aesEncryptionUtil.decrypt(sec.getAadhaarNumber());
                        String cleanDecrypted = decrypted.replaceAll("[^0-9]", "");
                        String cleanInput = aadhaar.replaceAll("[^0-9]", "");

                        if (cleanDecrypted.equals(cleanInput)) {
                            matchedSecretary = sec;
                            break;
                        }
                    } catch (Exception e) {
                        // Decryption failed for this record — skip
                    }
                }

                if (matchedSecretary != null) {
                    matchedSecretary.setPassword(passwordEncoder.encode(newPassword));
                    secretaryRepository.save(matchedSecretary);

                    authLogService.logActivity(
                            matchedSecretary.getLoginId(),
                            "SECRETARY",
                            "PASSWORD_RESET",
                            request.getRemoteAddr(),
                            true,
                            "Password reset via Aadhaar"
                    );

                    return ResponseEntity.ok(Map.of("message", "Secretary password reset successfully."));
                } else {
                    return ResponseEntity.status(400).body(Map.of("error", "Invalid Aadhaar or DOB"));
                }

            } else if ("FARMER".equalsIgnoreCase(role)) {

                Farmer matchedFarmer = null;

                // Pre-filter by DOB in DB to avoid full table scan
                for (Farmer far : farmerRepository.findByBirthdate(dob)) {
                    try {
                        String decrypted = aesEncryptionUtil.decrypt(far.getAadhaarNumber());
                        String cleanDecrypted = decrypted.replaceAll("[^0-9]", "");
                        String cleanInput = aadhaar.replaceAll("[^0-9]", "");

                        if (cleanDecrypted.equals(cleanInput)) {
                            matchedFarmer = far;
                            break;
                        }
                    } catch (Exception e) {
                        // Decryption failed for this record — skip
                    }
                }

                if (matchedFarmer != null) {
                    matchedFarmer.setPassword(passwordEncoder.encode(newPassword));
                    farmerRepository.save(matchedFarmer);

                    authLogService.logActivity(
                            matchedFarmer.getSabhasadNo(),
                            "FARMER",
                            "PASSWORD_RESET",
                            request.getRemoteAddr(),
                            true,
                            "Password reset via Aadhaar"
                    );

                    return ResponseEntity.ok(Map.of("message", "Farmer password reset successfully."));
                } else {
                    return ResponseEntity.status(400).body(Map.of("error", "Invalid Aadhaar or DOB"));
                }

            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Server Error: " + e.getMessage()));
        }
    }
}