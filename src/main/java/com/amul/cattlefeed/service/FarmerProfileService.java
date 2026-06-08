package com.amul.cattlefeed.service;

import com.amul.cattlefeed.dto.FarmerProfileDTO;
import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.repository.FarmerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FarmerProfileService {

    @Autowired
    private FarmerRepository farmerRepository;

    public FarmerProfileDTO getFarmerProfile(String sabhasadNo) {

        Farmer farmer = farmerRepository.findBySabhasadNo(sabhasadNo)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        return mapToDTO(farmer);
    }

    public FarmerProfileDTO updateFarmerProfile(String sabhasadNo, FarmerProfileDTO dto) {

        Farmer farmer = farmerRepository.findBySabhasadNo(sabhasadNo)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        if (dto.getMobile() != null)         farmer.setMobileNo(dto.getMobile());
        if (dto.getTotalCows() != null)      farmer.setTotalCows(dto.getTotalCows());
        if (dto.getTotalBuffaloes() != null)  farmer.setTotalBuffaloes(dto.getTotalBuffaloes());
        if (dto.getTotalGoats() != null)      farmer.setTotalGoats(dto.getTotalGoats());
        if (dto.getTotalCamels() != null)     farmer.setTotalCamels(dto.getTotalCamels());

        farmerRepository.save(farmer);
        return mapToDTO(farmer);
    }

    private FarmerProfileDTO mapToDTO(Farmer farmer) {

        int cows      = farmer.getTotalCows()      != null ? farmer.getTotalCows()      : 0;
        int buffaloes = farmer.getTotalBuffaloes()  != null ? farmer.getTotalBuffaloes() : 0;
        int goats     = farmer.getTotalGoats()      != null ? farmer.getTotalGoats()     : 0;
        int camels    = farmer.getTotalCamels()     != null ? farmer.getTotalCamels()    : 0;

        return FarmerProfileDTO.builder()
                .name(farmer.getName())
                .firstName(farmer.getFirstName())
                .middleName(farmer.getMiddleName())
                .surname(farmer.getSurname())
                .gender(farmer.getGender())
                .birthdate(farmer.getBirthdate() != null ? farmer.getBirthdate().toString() : null)
                .mobile(farmer.getMobileNo())
                .email(farmer.getEmail())
                .sabhasadNumber(farmer.getSabhasadNo())
                .villageSocietyCode(farmer.getSocietyCode())
                .zone(farmer.getZone())
                .district(farmer.getDistrict())
                .address(farmer.getAddress())
                .totalCows(farmer.getTotalCows())
                .totalBuffaloes(farmer.getTotalBuffaloes())
                .totalGoats(farmer.getTotalGoats())
                .totalCamels(farmer.getTotalCamels())
                .totalCattleCount(cows + buffaloes + goats + camels)
                .walletBalance(farmer.getWalletBalance())
                .accountStatus(farmer.getStatus())
                .createdDate(farmer.getCreatedAt() != null ? farmer.getCreatedAt().toLocalDate().toString() : null)
                .build();
    }
}