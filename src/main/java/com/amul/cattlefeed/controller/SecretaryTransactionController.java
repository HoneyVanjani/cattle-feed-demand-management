package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.*;
import com.amul.cattlefeed.repository.*;
import com.amul.cattlefeed.service.WhatsappService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/secretary/transactions")
@RequiredArgsConstructor
public class SecretaryTransactionController {

    private final TransactionRepository transactionRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NotificationRepository notificationRepository;
    private final FarmerRepository farmerRepository;
    private final WhatsappService whatsappService;
    private final SecretaryRepository secretaryRepository;

    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction request) {

        StockMovement movement = new StockMovement();

        movement.setMovementType("OUT");
        movement.setQuantity(request.getQuantity().intValue());
        movement.setMovementDate(LocalDate.now());

        stockMovementRepository.save(movement);

        request.setDeductionDate(LocalDate.now());

        Transaction saved = transactionRepository.save(request);

        Farmer farmer = farmerRepository
                .findBySabhasadNo(request.getSabhasadNumber())
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        Notification n = new Notification();

        n.setUserId(farmer.getFarmerId());
        n.setTitle("Feed Purchase");
        n.setMessage("You purchased " + request.getQuantity() + " kg feed on " + LocalDate.now());
        n.setType("INFO");
        n.setCreatedAt(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(n);

        whatsappService.sendMessage(
                farmer.getMobileNo(),
                savedNotification.getTitle() + "\n" + savedNotification.getMessage()
        );

        return saved;
    }

    @GetMapping
    public List<Transaction> getSecretaryTransactions(
            Authentication authentication) {

        String loginId = authentication.getName();

        Secretary secretary = secretaryRepository
                .findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException(
                        "Secretary not found"));

        // Get all farmers in secretary's zone+district
        List<Long> farmerIds = farmerRepository
                .findByZoneAndDistrict(
                        secretary.getZone(),
                        secretary.getDistrict()
                )
                .stream()
                .map(f -> f.getFarmerId())
                .toList();

        // Get transactions for those farmers
        return transactionRepository
                .findByFarmer_FarmerIdIn(farmerIds);
    }


//    @GetMapping
//    public List<Transaction> getAllTransactions() {
//        return transactionRepository.findAll();
//    }
}