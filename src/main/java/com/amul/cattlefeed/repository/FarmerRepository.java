package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FarmerRepository extends JpaRepository<Farmer, Long> {

        Optional<Farmer> findByZoneAndDistrictAndSocietyCodeAndSabhasadNo(
                        String zone,
                        String district,
                        String societyCode,
                        String sabhasadNo);

        Optional<Farmer> findBySabhasadNo(String sabhasadNo);

        long countByZoneAndDistrict(String zone, String district);

        long countBySocietyCode(String societyCode);

        Optional<Farmer> findByMobileNo(String mobileNo);

        boolean existsByMobileNo(String mobileNo);

        List<Farmer> findByZoneAndDistrict(String zone, String district);

        Optional<Farmer> findByAadhaarNumber(String aadhaarNumber);

        boolean existsByAadhaarNumber(String aadhaarNumber);

        Optional<Farmer> findByAadhaarNumberAndBirthdate(String aadhaarNumber, LocalDate birthdate);

        // Used for narrowing reset-password candidates by DOB before AES decryption
        List<Farmer> findByBirthdate(LocalDate birthdate);
}
