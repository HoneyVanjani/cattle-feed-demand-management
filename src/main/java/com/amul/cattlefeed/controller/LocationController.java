package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.entity.Taluka;
import com.amul.cattlefeed.entity.Village;
import com.amul.cattlefeed.entity.Zone;
import com.amul.cattlefeed.repository.TalukaRepository;
import com.amul.cattlefeed.repository.VillageRepository;
import com.amul.cattlefeed.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class LocationController {

    private final ZoneRepository zoneRepository;
    private final TalukaRepository talukaRepository;
    private final VillageRepository villageRepository;

    @GetMapping("/zones")
    public ResponseEntity<List<Zone>> getZones() {
        return ResponseEntity.ok(zoneRepository.findAll());
    }

    @GetMapping("/talukas")
    public ResponseEntity<List<Taluka>> getTalukas(@RequestParam String zoneCode) {
        return ResponseEntity.ok(talukaRepository.findByZoneCode(zoneCode));
    }

    @GetMapping("/villages")
    public ResponseEntity<List<Village>> getVillages(@RequestParam String talukaCode) {
        return ResponseEntity.ok(villageRepository.findByTalukaCode(talukaCode));
    }
}
