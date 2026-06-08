package com.amul.cattlefeed.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sabhasadNumber;

    private String farmerName;

    private String feedType;

    private Double quantity;

    private Double amount;

    private String cycle;

    private LocalDate deductionDate;

    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private Farmer farmer;
}