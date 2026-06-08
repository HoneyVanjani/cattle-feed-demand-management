package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    List<Transaction> findByFarmer_FarmerIdIn(List<Long> farmerIds);
    List<Transaction> findByFarmer_FarmerId(Long farmerId);

}