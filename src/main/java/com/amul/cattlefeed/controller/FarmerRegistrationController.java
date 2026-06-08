package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.entity.Secretary;
import com.amul.cattlefeed.entity.BiometricData;
import com.amul.cattlefeed.repository.FarmerRepository;
import com.amul.cattlefeed.repository.SecretaryRepository;
import com.amul.cattlefeed.repository.BiometricDataRepository;
import com.amul.cattlefeed.security.AesEncryptionUtil;
import com.amul.cattlefeed.service.AuthenticationLogService;
import com.amul.cattlefeed.service.BiometricValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/secretary/farmer")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class FarmerRegistrationController {

    private final FarmerRepository farmerRepository;
    private final SecretaryRepository secretaryRepository;
    private final BiometricDataRepository biometricDataRepository;
    private final AesEncryptionUtil aesEncryptionUtil;
    private final AuthenticationLogService authLogService;
    private final BiometricValidationService biometricValidationService;

    private static final String UPLOAD_DIR = "uploads/farmers/";

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('SECRETARY')")
    public ResponseEntity<?> registerFarmer(
            @RequestParam("firstName") String firstName,
            @RequestParam("middleName") String middleName,
            @RequestParam("surname") String surname,
            @RequestParam("gender") String gender,
            @RequestParam("birthdate") String birthdate,
            @RequestParam("contactNumber") String contactNumber,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam("address") String address,
            @RequestParam("societyCode") String societyCode,
            @RequestParam("aadhaarNumber") String aadhaarNumber,
            @RequestParam(value = "totalCows", defaultValue = "0") Integer totalCows,
            @RequestParam(value = "totalBuffaloes", defaultValue = "0") Integer totalBuffaloes,
            @RequestParam(value = "totalGoats", defaultValue = "0") Integer totalGoats,
            @RequestParam(value = "totalCamels", defaultValue = "0") Integer totalCamels,
            @RequestParam(value = "walletBalance", defaultValue = "0") Double walletBalance,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "signature", required = false) MultipartFile signature,
            @RequestParam(value = "faceDescriptor", required = false) String faceDescriptor,
            HttpServletRequest request) {
        try {
            if (farmerRepository.existsByMobileNo(contactNumber)) {
                return ResponseEntity.badRequest().body("Mobile already exists.");
            }

            String encryptedAadhaar = aesEncryptionUtil.encrypt(aadhaarNumber);
            if (farmerRepository.existsByAadhaarNumber(encryptedAadhaar)) {
                return ResponseEntity.badRequest().body("This Aadhaar is already registered!!");
            }

            if (biometricValidationService.isFaceAlreadyRegistered(faceDescriptor)) {
                return ResponseEntity.badRequest().body("Identity already exists in the system.");
            }

            // societyCode received from request
            String code = (societyCode != null) ? societyCode.trim() : "";
            Secretary secretary = secretaryRepository.findBySocietyCode(code)
                    .orElseGet(() -> secretaryRepository.findBySocietyCode(code.toUpperCase()).orElse(null));

            if (secretary == null) {
                return ResponseEntity.status(404).body("Secretary Society Code '" + code + "' not found in our records.");
            }

            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists())
                uploadDir.mkdirs();

            Farmer farmer = new Farmer();
            farmer.setFirstName(firstName);
            farmer.setMiddleName(middleName);
            farmer.setSurname(surname);
            farmer.setName(firstName + " " + middleName + " " + surname);
            farmer.setGender(gender);
            farmer.setBirthdate(LocalDate.parse(birthdate));
            farmer.setMobileNo(contactNumber);
            farmer.setEmail(email);
            farmer.setAddress(address);

            // Link to Society's zone and district
            farmer.setZone(secretary.getZone());
            farmer.setDistrict(secretary.getDistrict());
            farmer.setSocietyCode(societyCode);

            // eKYC Encrypt Aadhaar
            farmer.setAadhaarNumber(encryptedAadhaar);

            // Generate Sabhasad Number format: [SOCIETY_CODE]-FFFF
            long countInSociety = farmerRepository.countBySocietyCode(societyCode);
            String sabhasadNo = societyCode + "-" + String.format("%04d", countInSociety + 1);
            farmer.setSabhasadNo(sabhasadNo);

            farmer.setStatus("ACTIVE");
            farmer.setCreatedAt(LocalDateTime.now());
            farmer.setTotalCows(totalCows);
            farmer.setTotalBuffaloes(totalBuffaloes);
            farmer.setTotalGoats(totalGoats);
            farmer.setTotalCamels(totalCamels);
            farmer.setWalletBalance(walletBalance);

            // Handle Files
            if (photo != null && !photo.isEmpty()) {
                String photoPath = saveFile(photo, sabhasadNo + "_photo");
                farmer.setPhotoPath(photoPath);
            }
            if (signature != null && !signature.isEmpty()) {
                String sigPath = saveFile(signature, sabhasadNo + "_signature");
                farmer.setSignaturePath(sigPath);
            }

            Farmer saved = farmerRepository.save(farmer);

            // Store Biometric Data (Face)
            if (faceDescriptor != null && !faceDescriptor.isEmpty()) {
                BiometricData bio = new BiometricData();
                bio.setUserId(saved.getFarmerId());
                bio.setUserType("FARMER");
                bio.setFaceEmbedding(faceDescriptor);
                biometricDataRepository.save(bio);
            }

            authLogService.logActivity(sabhasadNo, "SECRETARY", "PROFILE_CREATED", request.getRemoteAddr(), true,
                    "Farmer " + sabhasadNo + " registered successfully.");

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error registering Farmer: " + e.getMessage());
        }
    }

    private String saveFile(MultipartFile file, String prefix) throws IOException {
        String filename = prefix + "_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + filename);
        Files.write(path, file.getBytes());
        return path.toString();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('SECRETARY')")
    public ResponseEntity<?> updateFarmer(
            @PathVariable Long id,
            @RequestParam("societyCode") String societyCode,
            @RequestParam("firstName") String firstName,
            @RequestParam("middleName") String middleName,
            @RequestParam("surname") String surname,
            @RequestParam("contactNumber") String contactNumber,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam("gender") String gender,
            @RequestParam("birthdate") String birthdate,
            @RequestParam("address") String address,
            @RequestParam(value = "totalCows", defaultValue = "0") Integer totalCows,
            @RequestParam(value = "totalBuffaloes", defaultValue = "0") Integer totalBuffaloes,
            @RequestParam(value = "totalGoats", defaultValue = "0") Integer totalGoats,
            @RequestParam(value = "totalCamels", defaultValue = "0") Integer totalCamels,
            @RequestParam(value = "walletBalance", defaultValue = "0") Double walletBalance,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "faceDescriptor", required = false) String faceDescriptor) {
        return farmerRepository.findById(id).map(farmer -> {
            boolean mobileExistsElsewhere = farmerRepository.findByMobileNo(contactNumber)
                    .filter(existing -> !existing.getFarmerId().equals(id))
                    .isPresent();

            if (mobileExistsElsewhere) {
                return ResponseEntity.badRequest().body("Mobile number is already registered to another farmer.");
            }
            farmer.setFirstName(firstName);
            farmer.setMiddleName(middleName);
            farmer.setSurname(surname);
            farmer.setName(firstName + " " + middleName + " " + surname);
            farmer.setMobileNo(contactNumber);
            if (email != null && !email.isBlank()) farmer.setEmail(email);
            farmer.setGender(gender);
            farmer.setBirthdate(LocalDate.parse(birthdate));
            farmer.setAddress(address);
            farmer.setTotalCows(totalCows);
            farmer.setTotalBuffaloes(totalBuffaloes);
            farmer.setTotalGoats(totalGoats);
            farmer.setTotalCamels(totalCamels);
            farmer.setWalletBalance(walletBalance);
            farmer.setSocietyCode(societyCode);
            
            try {
                if (photo != null && !photo.isEmpty()) {
                    String photoPath = saveFile(photo, farmer.getSabhasadNo() + "_photo");
                    farmer.setPhotoPath(photoPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Farmer saved = farmerRepository.save(farmer);

            if (faceDescriptor != null && !faceDescriptor.isEmpty()) {
                BiometricData bio = biometricDataRepository.findByUserIdAndUserType(saved.getFarmerId(), "FARMER")
                                    .orElse(new BiometricData());
                bio.setUserId(saved.getFarmerId());
                bio.setUserType("FARMER");
                bio.setFaceEmbedding(faceDescriptor);
                biometricDataRepository.save(bio);
            }            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }
}
