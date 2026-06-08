package com.amul.cattlefeed.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerProfileDTO {

    // Identity
    private String name;
    private String firstName;
    private String middleName;
    private String surname;
    private String gender;
    private String birthdate;

    // Contact
    private String mobile;
    private String email;

    // Society / Location
    private String sabhasadNumber;
    private String villageSocietyCode;
    private String zone;
    private String district;
    private String address;

    // Livestock
    private Integer totalCows;
    private Integer totalBuffaloes;
    private Integer totalGoats;
    private Integer totalCamels;
    private Integer totalCattleCount;

    // Finance
    private Double walletBalance;

    // Account
    private String accountStatus;
    private String createdDate;
}