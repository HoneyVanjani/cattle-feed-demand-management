package com.amul.cattlefeed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Stub service — WhatsApp/Twilio integration is disabled.
 * The application uses in-app notifications only.
 */
@Service
public class WhatsappService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappService.class);

    public void sendMessage(String phone, String message) {
        // No-op: external messaging is not enabled.
        // In-app notifications handle all user alerts.
        log.debug("WhatsApp stub called for phone={} (not sent - in-app only)", phone);
    }
}