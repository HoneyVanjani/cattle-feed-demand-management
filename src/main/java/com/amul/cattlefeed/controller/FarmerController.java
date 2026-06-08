package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.entity.Secretary;
import com.amul.cattlefeed.repository.FarmerRepository;
import com.amul.cattlefeed.repository.SecretaryRepository;
import com.amul.cattlefeed.service.NotificationService;
import com.amul.cattlefeed.service.WhatsappService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secretary/farmers")
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerRepository farmerRepository;
    private final SecretaryRepository secretaryRepository;
    private final NotificationService notificationService;
    private final WhatsappService whatsappService;


    @GetMapping
    public List<Farmer> getAllFarmers(Authentication authentication) {

        String loginId = authentication.getName();

        Secretary secretary = secretaryRepository
                .findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("Secretary not found"));

        return farmerRepository.findByZoneAndDistrict(
                secretary.getZone(),
                secretary.getDistrict()
        ).stream()
         .filter(f -> !"INACTIVE".equalsIgnoreCase(f.getStatus()))
         .toList();
    }

    @PostMapping("/add")
    public Farmer addFarmer(@RequestBody Farmer farmer, Authentication authentication) {

        String loginId = authentication.getName();

        Secretary secretary = secretaryRepository
                .findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("Secretary not found"));

        // ✅ SET ZONE & DISTRICT
        farmer.setZone(secretary.getZone());
        farmer.setDistrict(secretary.getDistrict());

        // ✅ GET SOCIETY CODE FROM SECRETARY
        String societyCode = secretary.getSocietyCode();
        farmer.setSocietyCode(societyCode);

        // ✅ COUNT FARMERS IN THIS SOCIETY
        long count = farmerRepository.countBySocietyCode(societyCode) + 1;

        // ✅ SABHASAD NUMBER
        String sabhasadNo = societyCode + "-" + String.format("%04d", count);

        farmer.setSabhasadNo(sabhasadNo);

        // ================= SAVE =================

        Farmer savedFarmer = farmerRepository.save(farmer);

        // ================= NOTIFY SECRETARY =================
        notificationService.createNotification(
                secretary.getId(),
                "New Farmer Registered",
                "Farmer " + savedFarmer.getName() + " (" + savedFarmer.getSabhasadNo() + ") has been added to your society.",
                "success",
                null,
                "SECRETARY"
        );

        // ================= NOTIFY FARMER (welcome) =================
        notificationService.createNotification(
                savedFarmer.getFarmerId(),
                "Welcome to AMUL Cattle Feed",
                "Hello " + savedFarmer.getName() + "! Your account is ready. Your Farmer ID is " + savedFarmer.getSabhasadNo() + ".",
                "success",
                savedFarmer.getMobileNo(),
                "FARMER"
        );

        // ================= WHATSAPP =================
        try {
            whatsappService.sendMessage(
                    savedFarmer.getMobileNo(),
                    "Welcome to AMUL Cattle Feed System\n" +
                            "Farmer ID: " + savedFarmer.getSabhasadNo() +
                            "\nYou can now request cattle feed from the system."
            );
        } catch (Exception e) {
            System.out.println("WhatsApp sending failed: " + e.getMessage());
        }

        return savedFarmer;
    }


    @PutMapping("/{id}")
    public Farmer updateFarmer(@PathVariable Long id, @RequestBody Farmer updatedFarmer) {

        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        farmer.setName(updatedFarmer.getName());
        farmer.setMobileNo(updatedFarmer.getMobileNo());
        farmer.setTotalCows(updatedFarmer.getTotalCows());
        farmer.setTotalBuffaloes(updatedFarmer.getTotalBuffaloes());
        farmer.setTotalGoats(updatedFarmer.getTotalGoats());
        farmer.setTotalCamels(updatedFarmer.getTotalCamels());
        farmer.setWalletBalance(updatedFarmer.getWalletBalance());

        return farmerRepository.save(farmer);
    }

    @DeleteMapping("/{id}")
    public Farmer deleteFarmer(@PathVariable Long id) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
        
        farmer.setStatus("INACTIVE"); // Soft delete
        return farmerRepository.save(farmer);
    }
}