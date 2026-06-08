package com.amul.cattlefeed.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecretaryRequest {

    private String firstName;
    private String middleName;
    private String surname;
    private String contactNumber;
    private String email;
    private String zoneCode;
    private String talukaCode;
    private String villageCode;

}