package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.Transaction;
import com.amul.cattlefeed.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farmer/transactions")
@RequiredArgsConstructor
@CrossOrigin
public class FarmerTransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{sabhasadNo}")
    public List<Transaction> getFarmerTransactions(@PathVariable String sabhasadNo) {
        return transactionService.getFarmerTransactions(sabhasadNo);
    }
}
