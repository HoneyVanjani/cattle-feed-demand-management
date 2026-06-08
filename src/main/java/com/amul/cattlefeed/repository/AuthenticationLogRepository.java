package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.AuthenticationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationLogRepository extends JpaRepository<AuthenticationLog, Long> {
    Page<AuthenticationLog> findByUserType(String userType, Pageable pageable);
    Page<AuthenticationLog> findByAction(String action, Pageable pageable);
}
