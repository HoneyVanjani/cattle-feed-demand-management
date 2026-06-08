package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.CattleFeed;
import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.service.CattleService;
import com.amul.cattlefeed.service.FarmerService;
import com.amul.cattlefeed.service.FarmerServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/farmer")
@RequiredArgsConstructor
public class FarmerCattleController {

    private final FarmerServiceImpl farmerService;

    @GetMapping("/cattle/{sabhasadNo}")
    public Farmer getFarmerProfile(@PathVariable String sabhasadNo) {
        return farmerService.getFarmerProfile(sabhasadNo);
    }

}
