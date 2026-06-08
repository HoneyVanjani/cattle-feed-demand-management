package com.amul.cattlefeed.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FarmerLoginRequest {
    private String zone;
    private String district;
    private String societyCode;
    private String sabhasadNo;
    private String password;
}
