package com.amul.cattlefeed.service;



import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.entity.Transaction;
import com.amul.cattlefeed.repository.FarmerRepository;
import com.amul.cattlefeed.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FarmerRepository farmerRepository;

    public List<Transaction> getFarmerTransactions(String sabhasadNo) {

        Farmer farmer = farmerRepository
                .findBySabhasadNo(sabhasadNo)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        return transactionRepository.findByFarmer_FarmerId(farmer.getFarmerId());
    }
}