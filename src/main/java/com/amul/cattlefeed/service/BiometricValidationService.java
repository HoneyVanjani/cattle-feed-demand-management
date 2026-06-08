package com.amul.cattlefeed.service;

import com.amul.cattlefeed.entity.BiometricData;
import com.amul.cattlefeed.repository.BiometricDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BiometricValidationService {

    private final BiometricDataRepository biometricDataRepository;
    private static final double DUPLICATE_THRESHOLD = 0.6;

    public boolean isFaceAlreadyRegistered(String newFaceDescriptor) {
        if (newFaceDescriptor == null || newFaceDescriptor.isEmpty()) {
            return false; // Skip if no face descriptor provided
        }

        List<BiometricData> allBiometrics = biometricDataRepository.findAll();
        for (BiometricData bio : allBiometrics) {
            String storedDescriptor = bio.getFaceEmbedding();
            if (storedDescriptor != null && !storedDescriptor.isEmpty() && !storedDescriptor.equals("undefined")) {
                double distance = calculateEuclideanDistance(storedDescriptor, newFaceDescriptor);
                if (distance < DUPLICATE_THRESHOLD) {
                    return true; // Match found -> identity already exists
                }
            }
        }
        return false;
    }

    private double calculateEuclideanDistance(String stored, String submitted) {
        try {
            String[] s1 = stored.replace("[", "").replace("]", "").split(",");
            String[] s2 = submitted.replace("[", "").replace("]", "").split(",");

            if (s1.length != 128 || s2.length != 128) {
                return Double.MAX_VALUE; // invalid descriptor
            }

            double sum = 0.0;
            for (int i = 0; i < 128; i++) {
                double a = Double.parseDouble(s1[i].trim());
                double b = Double.parseDouble(s2[i].trim());
                double diff = a - b;
                sum += diff * diff;
            }

            return Math.sqrt(sum);
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }
}
