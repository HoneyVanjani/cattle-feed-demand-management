package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.Taluka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TalukaRepository extends JpaRepository<Taluka, Long> {

    @Query("SELECT DISTINCT t FROM Taluka t WHERE t.zoneCode = :zoneCode")
    List<Taluka> findByZoneCode(@Param("zoneCode") String zoneCode);

    Taluka findByTalukaCode(String talukaCode);
}
