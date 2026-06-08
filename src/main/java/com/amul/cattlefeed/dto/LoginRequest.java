package com.amul.cattlefeed.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String loginId;
    private String password;
    private String role;
}
//sec password: username : sec1
//password : 1234