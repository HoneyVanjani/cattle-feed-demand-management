package com.amul.cattlefeed.service;

import com.amul.cattlefeed.entity.*;
import com.amul.cattlefeed.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service

@RequiredArgsConstructor
public class FeedRequestService {

    private final FeedRequestRepository feedRequestRepository;
    private final SecretaryRepository secretaryRepository;
    private final FarmerRepository farmerRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    public List<FeedRequest> getPendingRequests(Authentication authentication) {

        String loginId = authentication.getName();

        Secretary secretary = secretaryRepository
                .findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("Secretary not found"));

        return feedRequestRepository.findByStatusAndFarmer_SocietyCode(
                "PENDING",
                secretary.getSocietyCode()
        );
    }

    @Transactional
    public FeedRequest approveRequest(Long id, String reason) {

        FeedRequest request = feedRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Farmer farmer = request.getFarmer();
        CattleFeed feed = request.getCattleFeed();

        // Calculate price per kg
        BigDecimal pricePerKg = feed.getPricePerKg();

        // Calculate total cost
        BigDecimal cost = pricePerKg.multiply(
                BigDecimal.valueOf(request.getQuantity())
        );

        // Current wallet balance
        Double currentBalance = farmer.getWalletBalance() != null
                ? farmer.getWalletBalance()
                : 0.0;

        // Convert BigDecimal cost to double
        double newBalance = currentBalance - cost.doubleValue();

        // Update farmer wallet
        farmer.setWalletBalance(newBalance);
        farmerRepository.save(farmer);

        Optional<Stock> stockOpt = stockRepository
                .findByCattleFeed_FeedIdAndZoneAndDistrict(
                        feed.getFeedId(),
                        farmer.getZone(),
                        farmer.getDistrict()
                );

        if (stockOpt.isPresent()) {
            Stock stock = stockOpt.get();

            // Warn in log if insufficient but still approve
            if (stock.getQuantity() < request.getQuantity()) {
                System.out.println("⚠️ WARNING: Insufficient stock for "
                        + feed.getFeedName()
                        + " in " + farmer.getDistrict()
                        + ". Approving anyway.");
            }

            // ✅ Deduct stock
            double newQty = stock.getQuantity() - request.getQuantity();
            stock.setQuantity(Math.max(newQty, 0.0)); // floor at 0
            stockRepository.save(stock);

            // ✅ Record OUT movement
            StockMovement movement = new StockMovement();
            movement.setStock(stock);
            movement.setQuantity(request.getQuantity());
            movement.setMovementType("OUT");
            movement.setMovementDate(LocalDate.now());
            movement.setCycle(request.getCycle());
            movement.setIsEmergency(false);
            stockMovementRepository.save(movement);

        } else {
            // No stock record found — log warning, still approve
            System.out.println("⚠️ WARNING: No stock record found for "
                    + feed.getFeedName()
                    + " in " + farmer.getZone()
                    + "/" + farmer.getDistrict()
                    + ". Approving without deduction.");
        }


        Transaction transaction = Transaction.builder()
                .farmer(farmer)
                .sabhasadNumber(farmer.getSabhasadNo())
                .farmerName(farmer.getName())
                .feedType(feed.getFeedName())
                .quantity((double) request.getQuantity())
                .amount(cost.doubleValue())
                .cycle(request.getCycle())
                .deductionDate(LocalDate.now())
                .build();

        transactionRepository.save(transaction);

        // Update request status
        request.setStatus("APPROVED");
        request.setApprovalDate(LocalDate.now());
        if (reason != null && !reason.isBlank()) request.setRemarks(reason);

        // SEND NOTIFICATION TO FARMER
        String approvalMsg = "Your request for " + request.getQuantity() + "kg "
                + feed.getFeedName() + " has been approved."
                + (reason != null && !reason.isBlank() ? " Note: " + reason : "");
        notificationService.createNotification(
                farmer.getFarmerId(),
                "Feed Request Approved",
                approvalMsg,
                "success",
                farmer.getMobileNo(),
                "FARMER"
        );

        return feedRequestRepository.save(request);
    }

    public FeedRequest rejectRequest(Long id, String reason) {

        FeedRequest request = feedRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus("REJECTED");
        if (reason != null && !reason.isBlank()) request.setRemarks(reason);

        Farmer farmer = request.getFarmer();

        String rejectionMsg = "Your request for " + request.getQuantity() + "kg "
                + request.getCattleFeed().getFeedName() + " was rejected."
                + (reason != null && !reason.isBlank() ? " Reason: " + reason : "");
        notificationService.createNotification(
                farmer.getFarmerId(),
                "Feed Request Rejected",
                rejectionMsg,
                "warning",
                farmer.getMobileNo(),
                "FARMER"
        );

        return feedRequestRepository.save(request);
    }

    public List<FeedRequest> getAllRequests(Authentication authentication) {

        String loginId = authentication.getName();

        Secretary secretary = secretaryRepository
                .findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("Secretary not found"));

        return feedRequestRepository
                .findByFarmer_SocietyCode(secretary.getSocietyCode());
    }
}