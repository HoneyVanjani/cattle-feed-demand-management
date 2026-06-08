package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.BiometricData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BiometricDataRepository extends JpaRepository<BiometricData, Long> {
    Optional<BiometricData> findByUserIdAndUserType(Long userId, String userType);
}
