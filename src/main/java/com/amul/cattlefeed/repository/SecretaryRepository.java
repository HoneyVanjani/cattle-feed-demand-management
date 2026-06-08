package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.Secretary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SecretaryRepository extends JpaRepository<Secretary, Long> {

    Optional<Secretary> findByLoginId(String loginId);

    List<Secretary> findByNameContainingIgnoreCaseOrMobileContainingIgnoreCase(
            String name, String mobile);

    boolean existsByLoginId(String loginId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByMobile(String mobile);

    Optional<Secretary> findByMobile(String mobile);

    Optional<Secretary> findBySocietyCode(String societyCode);

    List<Secretary> findByZoneAndDistrict(String zone, String district);

    Optional<Secretary> findByAadhaarNumber(String aadhaarNumber);

    boolean existsByAadhaarNumber(String aadhaarNumber);

    Optional<Secretary> findByAadhaarNumberAndBirthdate(String aadhaarNumber, LocalDate birthdate);

    // Used for narrowing reset-password candidates by DOB before AES decryption
    List<Secretary> findByBirthdate(LocalDate birthdate);
}
