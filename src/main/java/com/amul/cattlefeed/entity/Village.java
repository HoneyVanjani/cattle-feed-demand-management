package com.amul.cattlefeed.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "villages", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "village_code", "taluka_code" })
})
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
public class Village {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "village_code", nullable = false, length = 2, unique = true)
    private String villageCode;

    @Column(name = "village_name", nullable = false, length = 100)
    private String villageName;

    @Column(name = "taluka_code", nullable = false, length = 2)
    private String talukaCode;

    @Column(name = "zone_code", nullable = false, length = 2)
    private String zoneCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taluka_code", referencedColumnName = "taluka_code", insertable = false, updatable = false)
    private Taluka taluka;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_code", referencedColumnName = "zone_code", insertable = false, updatable = false)
    private Zone zone;
}
