package com.amul.cattlefeed.service;

import com.amul.cattlefeed.entity.AuthenticationLog;
import com.amul.cattlefeed.repository.AuthenticationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuthenticationLogService {

    @Autowired
    private AuthenticationLogRepository repository;

    public void logActivity(String loginId, String userType, String action, String ipAddress, boolean success, String failureReason) {
        AuthenticationLog log = new AuthenticationLog();
        log.setLoginId(loginId);
        log.setUserType(userType);
        log.setAction(action);
        log.setIpAddress(ipAddress);
        log.setSuccess(success);
        log.setFailureReason(failureReason);
        repository.save(log);
    }

    public Page<AuthenticationLog> getAllLogs(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<AuthenticationLog> getAllLogsWithoutPaging() {
        return repository.findAll();
    }
}
