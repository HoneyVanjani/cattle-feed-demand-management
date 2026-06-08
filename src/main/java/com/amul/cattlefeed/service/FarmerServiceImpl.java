package com.amul.cattlefeed.service;

import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.repository.FarmerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FarmerServiceImpl implements FarmerService {

    private final FarmerRepository farmerRepository;

    public FarmerServiceImpl(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    @Override
    public Farmer saveFarmer(Farmer farmer) {
        return farmerRepository.save(farmer);
    }

    @Override
    public List<Farmer> getAllFarmers() {
        return farmerRepository.findAll();
    }

    @Override
    public Farmer getFarmerById(Long id) {
        return farmerRepository.findById(id).orElse(null);
    }

    public Farmer getFarmerProfile(String sabhasadNo) {
        return farmerRepository
                .findBySabhasadNo(sabhasadNo)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
    }
}
