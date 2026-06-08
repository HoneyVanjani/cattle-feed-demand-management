package com.amul.cattlefeed.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long farmerId;

    @Column(nullable = false)
    @JsonProperty("sabhasadNo")
    private String sabhasadNo;

    @Column(nullable = false)
    private String societyCode;

    @Column(nullable = false)
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

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "photo_path", length = 500)
    private String photoPath;

    @Column(name = "signature_path", length = 500)
    private String signaturePath;

    @Column(nullable = false, unique = true)
    @JsonProperty("mobileNo")  // rename for frontend
    private String mobileNo;

    private Double walletBalance;

    private Integer totalCows;

    private Integer totalBuffaloes;

    private Integer totalGoats;

    private Integer totalCamels;

    private String status;

    @Column(length = 200)
    private String password;

    @Column(nullable = false)
    private String zone;

    @Column(nullable = false)
    private String district;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 🔥 Computed total across all animal types
    @JsonProperty("cattleCount")
    public Integer getCattleCount() {
        int cows     = totalCows      != null ? totalCows      : 0;
        int buffaloes= totalBuffaloes != null ? totalBuffaloes : 0;
        int goats    = totalGoats     != null ? totalGoats     : 0;
        int camels   = totalCamels    != null ? totalCamels    : 0;
        return cows + buffaloes + goats + camels;
    }
}
