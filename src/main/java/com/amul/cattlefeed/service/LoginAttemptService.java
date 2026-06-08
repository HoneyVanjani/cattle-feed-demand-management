package com.amul.cattlefeed.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 5;
    private final int LOCK_TIME_DURATION_MINUTES = 15;

    // Map of loginId -> Attempt details
    private ConcurrentHashMap<String, AttemptDetails> loginAttemptCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        loginAttemptCache.remove(key);
    }

    public void loginFailed(String key) {
        AttemptDetails details = loginAttemptCache.getOrDefault(key, new AttemptDetails(0, null));
        details.attempts++;

        if (details.attempts >= MAX_ATTEMPT) {
            details.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_TIME_DURATION_MINUTES);
        }

        loginAttemptCache.put(key, details);
    }

    public boolean isBlocked(String key) {
        AttemptDetails details = loginAttemptCache.get(key);
        if (details == null) {
            return false;
        }

        if (details.lockedUntil != null) {
            if (LocalDateTime.now().isAfter(details.lockedUntil)) {
                // Lock expired
                loginAttemptCache.remove(key);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private static class AttemptDetails {
        int attempts;
        LocalDateTime lockedUntil;

        AttemptDetails(int attempts, LocalDateTime lockedUntil) {
            this.attempts = attempts;
            this.lockedUntil = lockedUntil;
        }
    }
}
