package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VillageRepository extends JpaRepository<Village, Long> {
    List<Village> findByTalukaCode(String talukaCode);
    Village findByVillageCodeAndTalukaCode(String villageCode, String talukaCode);
    Village findByVillageCode(String villageCode);
}
