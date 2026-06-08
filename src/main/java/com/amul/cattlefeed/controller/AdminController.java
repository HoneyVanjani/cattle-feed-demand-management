package com.amul.cattlefeed.controller;

import com.amul.cattlefeed.dto.LoginRequest;
import com.amul.cattlefeed.entity.Admin;
import com.amul.cattlefeed.repository.AdminRepository;
import com.amul.cattlefeed.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

//@RestController
//@RequestMapping("/api/admin")
//@CrossOrigin(origins = "*")
//public class AdminController {
//
//    @Autowired
//    private AdminRepository adminRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
//
//        Admin admin = adminRepository.findByLoginId(request.getLoginId())
//                .orElseThrow(() -> new RuntimeException("Admin not found"));
//
//        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
//            throw new RuntimeException("Invalid password");
//        }
//
//        //String token = jwtUtil.generateToken(admin.getLoginId());
//
//        //return ResponseEntity.ok(token);
//    }
//}