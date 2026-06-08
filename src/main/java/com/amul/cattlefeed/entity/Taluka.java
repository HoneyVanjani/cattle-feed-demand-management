package com.amul.cattlefeed.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "talukas", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "taluka_code", "zone_code" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Taluka {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "taluka_code", nullable = false, length = 2, unique = true)
    private String talukaCode;

    @Column(name = "taluka_name", nullable = false, length = 100)
    private String talukaName;

    @Column(name = "zone_code", nullable = false, length = 2)
    private String zoneCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_code", referencedColumnName = "zone_code", insertable = false, updatable = false)
    private Zone zone;
}
