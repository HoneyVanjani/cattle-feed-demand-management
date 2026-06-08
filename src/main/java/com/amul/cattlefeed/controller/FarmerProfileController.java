package com.amul.cattlefeed.controller;


import com.amul.cattlefeed.dto.FarmerProfileDTO;
import com.amul.cattlefeed.service.FarmerProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farmer")
@CrossOrigin(origins = "*")
public class FarmerProfileController {

    @Autowired
    private FarmerProfileService farmerProfileService;

    // GET PROFILE
    @GetMapping("/profile/{sabhasadNo}")
    public FarmerProfileDTO getProfile(@PathVariable String sabhasadNo) {
        return farmerProfileService.getFarmerProfile(sabhasadNo);
    }

    // UPDATE PROFILE
    @PutMapping("/profile/{sabhasadNo}")
    public FarmerProfileDTO updateProfile(
            @PathVariable String sabhasadNo,
            @RequestBody FarmerProfileDTO profileDTO
    ) {
        return farmerProfileService.updateFarmerProfile(sabhasadNo, profileDTO);
    }
}
