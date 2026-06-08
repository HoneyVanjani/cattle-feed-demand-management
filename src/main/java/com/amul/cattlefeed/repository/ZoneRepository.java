package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Zone findByZoneCode(String zoneCode);
}
