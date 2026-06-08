package com.amul.cattlefeed.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "secretaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Secretary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String loginId;

    private String name;
    
    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "surname", length = 100)
    private String surname;

    @Column(length = 10)
    private String gender;

    private java.time.LocalDate birthdate;

    @Column(length = 150)
    private String email;

    @Column(name = "aadhaar_number", unique = true)
    private String aadhaarNumber; // AES Encrypted

    @Column(length = 10)
    private String pincode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id")
    private Village village;

    @Column(name = "photo_path", length = 500)
    private String photoPath;

    @Column(name = "signature_path", length = 500)
    private String signaturePath;

    private String password;

    @Column(unique = true)
    private String mobile;

    private String zone;
    private String district;

    @Column(unique = true, nullable = false)
    private String societyCode;

    private String status;
    private LocalDateTime createdDate;

    private String role = "SECRETARY";
}