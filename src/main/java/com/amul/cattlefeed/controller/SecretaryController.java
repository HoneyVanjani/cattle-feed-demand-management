// package com.amul.cattlefeed.controller;

// import com.amul.cattlefeed.entity.Secretary;
// import com.amul.cattlefeed.repository.SecretaryRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.web.bind.annotation.*;
// import java.util.List;

// @RestController
// @RequestMapping("/api/admin/secretaries")
// @RequiredArgsConstructor
// public class SecretaryController {

//     private final SecretaryRepository secretaryRepository;

//     @GetMapping
//     public List<Secretary> getSecretaries(@RequestParam(required = false) String search) {
//         if (search != null && !search.isEmpty()) {
//             return secretaryRepository.findByNameContainingIgnoreCaseOrMobileContainingIgnoreCase(search, search);
//         }
//         return secretaryRepository.findAll();
//     }

// }
