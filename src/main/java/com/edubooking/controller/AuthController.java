package com.edubooking.controller;

import com.edubooking.dto.AuthResponse;
import com.edubooking.dto.LoginRequest;
import com.edubooking.dto.RegisterRequest;
import com.edubooking.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request){
        System.out.println("=== AuthController: REGISTER HIT ===");

        return ResponseEntity.ok(authService.register(request));
    }


    @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
            return ResponseEntity.ok(authService.login(request));
        }
    }

