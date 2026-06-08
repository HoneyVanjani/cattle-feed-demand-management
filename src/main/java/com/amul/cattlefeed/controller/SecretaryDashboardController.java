package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.dto.SecretaryDashboardDTO;
import com.amul.cattlefeed.entity.Secretary;
import com.amul.cattlefeed.entity.Transaction;
import com.amul.cattlefeed.repository.FarmerRepository;
import com.amul.cattlefeed.repository.SecretaryRepository;
import com.amul.cattlefeed.repository.TransactionRepository;
import com.amul.cattlefeed.service.SecretaryDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secretary")
@RequiredArgsConstructor
public class SecretaryDashboardController {

    private final SecretaryRepository secretaryRepository;
    private final FarmerRepository farmerRepository;
    private final TransactionRepository transactionRepository;
    private final SecretaryDashboardService dashboardService;

    @GetMapping("/dashboard")
    public SecretaryDashboardDTO getDashboard() {
        return dashboardService.getDashboardStats();
    }


}