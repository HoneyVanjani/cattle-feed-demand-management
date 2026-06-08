package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.Secretary;
import com.amul.cattlefeed.entity.BiometricData;
import com.amul.cattlefeed.repository.SecretaryRepository;
import com.amul.cattlefeed.repository.BiometricDataRepository;
import com.amul.cattlefeed.repository.VillageRepository;
import com.amul.cattlefeed.security.AesEncryptionUtil;
import com.amul.cattlefeed.service.BiometricValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/secretary")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*")
@PreAuthorize("permitAll()")
public class AdminSecretaryController {

    private final SecretaryRepository secretaryRepository;
    private final VillageRepository villageRepository;
    private final BiometricDataRepository biometricDataRepository;
    private final AesEncryptionUtil aesEncryptionUtil;
    private final BiometricValidationService biometricValidationService;
    private final PasswordEncoder passwordEncoder;

    // ================= GET ALL + SEARCH =================
    @GetMapping
    public List<Secretary> getSecretaries(
            @RequestParam(required = false) String search
    ) {
        if (search != null && !search.isEmpty()) {
            return secretaryRepository
                    .findByNameContainingIgnoreCaseOrMobileContainingIgnoreCase(
                            search, search
                    );
        }
        return secretaryRepository.findAll();
    }

    // ================= REGISTER (MULTIPART) =================
    @PostMapping("/register")
    public ResponseEntity<?> registerSecretary(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String middleName,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String birthdate,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String zoneCode,
            @RequestParam(required = false) String talukaCode,
            @RequestParam(required = false) String villageCode,
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) String aadhaarNumber,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam(required = false) MultipartFile signature,
            @RequestParam(required = false) String faceDescriptor
    ) {
        try {
            if (firstName == null || firstName.isEmpty()) return ResponseEntity.badRequest().body("First name is required");
            if (surname == null || surname.isEmpty()) return ResponseEntity.badRequest().body("Surname is required");
            if (contactNumber == null || contactNumber.isEmpty()) return ResponseEntity.badRequest().body("Contact number is required");
            if (aadhaarNumber == null || aadhaarNumber.isEmpty()) return ResponseEntity.badRequest().body("Aadhaar number is required");
            if (birthdate == null || birthdate.isEmpty()) return ResponseEntity.badRequest().body("Birthdate is required");
            if (zoneCode == null || zoneCode.isEmpty()) return ResponseEntity.badRequest().body("Zone is required");

            if (!aadhaarNumber.matches("\\d{12}")) {
                return ResponseEntity.badRequest().body("Invalid Aadhaar Number (Must be 12 digits)");
            }

            String encryptedAadhaar = aesEncryptionUtil.encrypt(aadhaarNumber);
            Optional<Secretary> existing = secretaryRepository.findByAadhaarNumber(encryptedAadhaar);
            if (existing.isPresent()) {
                return ResponseEntity.badRequest().body("This Aadhaar number is already registered.");
            }

            if (faceDescriptor != null && !faceDescriptor.isEmpty()) {
                if (biometricValidationService.isFaceAlreadyRegistered(faceDescriptor)) {
                    return ResponseEntity.badRequest().body("This face is already registered in our system.");
                }
            }

            Secretary sec = new Secretary();
            sec.setFirstName(firstName);
            sec.setMiddleName(middleName);
            sec.setSurname(surname);
            sec.setName(firstName + " " + (middleName != null ? middleName : "") + " " + surname);
            sec.setGender(gender);
            try {
                sec.setBirthdate(LocalDate.parse(birthdate));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid birthdate format. Use YYYY-MM-DD.");
            }
            sec.setMobile(contactNumber);
            sec.setEmail(email);
            sec.setZone(zoneCode);
            sec.setDistrict(talukaCode);
            sec.setPincode(pincode);

            // Generate Society Code
            String zCode = zoneCode.length() >= 2 ? zoneCode.substring(0, 2).toUpperCase() : zoneCode.toUpperCase();
            String tCode = talukaCode.length() >= 2 ? talukaCode.substring(0, 2).toUpperCase() : talukaCode.toUpperCase();
            String baseCode = zCode + "-" + tCode;
            long count = secretaryRepository.findAll().stream()
                    .filter(s -> zoneCode.equalsIgnoreCase(s.getZone()) && talukaCode.equalsIgnoreCase(s.getDistrict()))
                    .count();
            String societyCode = baseCode + "-" + String.format("%03d", count + 1);
            sec.setSocietyCode(societyCode);

            sec.setAadhaarNumber(encryptedAadhaar);
            String loginId = "SEC" + System.currentTimeMillis();
            sec.setLoginId(loginId);
            sec.setPassword(passwordEncoder.encode("sec@123"));
            sec.setStatus("ACTIVE");
            sec.setCreatedDate(LocalDateTime.now());
            sec.setRole("SECRETARY");

            if (photo != null && !photo.isEmpty()) sec.setPhotoPath(photo.getOriginalFilename());
            if (signature != null && !signature.isEmpty()) sec.setSignaturePath(signature.getOriginalFilename());

            Secretary saved = secretaryRepository.save(sec);

            if (faceDescriptor != null && !faceDescriptor.isEmpty()) {
                BiometricData bio = new BiometricData();
                bio.setUserId(saved.getId());
                bio.setUserType("SECRETARY");
                bio.setFaceEmbedding(faceDescriptor);
                biometricDataRepository.save(bio);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Secretary Registered Successfully");
            response.put("loginId", loginId);
            response.put("id", saved.getId());
            response.put("name", saved.getName());
            response.put("mobile", saved.getMobile());
            response.put("zone", saved.getZone());
            response.put("district", saved.getDistrict());
            response.put("societyCode", saved.getSocietyCode());
            response.put("status", saved.getStatus());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }

    // ================= UPDATE (MULTIPART) =================
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateSecretary(
            @PathVariable Long id,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String middleName,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String birthdate,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String zoneCode,
            @RequestParam(required = false) String talukaCode,
            @RequestParam(required = false) String villageCode,
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) String aadhaarNumber,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam(required = false) MultipartFile signature,
            @RequestParam(required = false) String faceDescriptor
    ) {
        try {
            Optional<Secretary> optional = secretaryRepository.findById(id);
            if (optional.isEmpty()) return ResponseEntity.badRequest().body("Secretary not found");

            Secretary sec = optional.get();
            if (firstName != null && !firstName.isEmpty()) sec.setFirstName(firstName);
            if (middleName != null && !middleName.isEmpty()) sec.setMiddleName(middleName);
            if (surname != null && !surname.isEmpty()) sec.setSurname(surname);
            if (firstName != null && !firstName.isEmpty() && surname != null && !surname.isEmpty()) {
                sec.setName(firstName + " " + (middleName != null ? middleName : "") + " " + surname);
            }
            if (gender != null && !gender.isEmpty()) sec.setGender(gender);
            if (birthdate != null && !birthdate.isEmpty()) {
                try { sec.setBirthdate(LocalDate.parse(birthdate)); } catch (Exception e) { }
            }
            if (contactNumber != null && !contactNumber.isEmpty()) sec.setMobile(contactNumber);
            if (email != null && !email.isEmpty()) sec.setEmail(email);
            if (zoneCode != null && !zoneCode.isEmpty()) sec.setZone(zoneCode);
            if (talukaCode != null && !talukaCode.isEmpty()) sec.setDistrict(talukaCode);
            if (villageCode != null && !villageCode.isEmpty()) sec.setSocietyCode(villageCode);
            if (pincode != null && !pincode.isEmpty()) sec.setPincode(pincode);

            if (photo != null && !photo.isEmpty()) sec.setPhotoPath(photo.getOriginalFilename());
            if (signature != null && !signature.isEmpty()) sec.setSignaturePath(signature.getOriginalFilename());

            if (faceDescriptor != null && !faceDescriptor.isEmpty() && !"EXISTING".equals(faceDescriptor)) {
                BiometricData bio = biometricDataRepository.findByUserIdAndUserType(sec.getId(), "SECRETARY")
                        .orElse(new BiometricData());
                bio.setUserId(sec.getId());
                bio.setUserType("SECRETARY");
                bio.setFaceEmbedding(faceDescriptor);
                biometricDataRepository.save(bio);
            }

            secretaryRepository.save(sec);
            return ResponseEntity.ok(sec);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Update failed: " + e.getMessage());
        }
    }

    // ================= TOGGLE STATUS =================
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Secretary> toggleSecretary(@PathVariable Long id) {
        Secretary secretary = secretaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Secretary not found"));
        String status = secretary.getStatus();
        if (status != null && status.equalsIgnoreCase("ACTIVE")) {
            secretary.setStatus("INACTIVE");
        } else {
            secretary.setStatus("ACTIVE");
        }
        secretaryRepository.save(secretary);
        return ResponseEntity.ok(secretary);
    }
}