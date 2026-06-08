package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.AuthenticationLog;
import com.amul.cattlefeed.service.AuthenticationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/activity-logs")
@PreAuthorize("hasAuthority('ADMIN')")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminActivityController {

    @Autowired
    private AuthenticationLogService authLogService;

    @GetMapping
    public ResponseEntity<Page<AuthenticationLog>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return ResponseEntity.ok(authLogService.getAllLogs(pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<AuthenticationLog> logs = authLogService.getAllLogsWithoutPaging();

        long success = logs.stream()
        .filter(log -> Boolean.TRUE.equals(log.getSuccess()))
        .count();

        long failed = logs.size() - success;

        double successRate = logs.size() == 0 ? 0 : (success * 100.0 / logs.size());

        return ResponseEntity.ok(Map.of(
            "total", logs.size(),
            "success", success,
            "failed", failed,
            "successRate", successRate
        ));
    }
}
