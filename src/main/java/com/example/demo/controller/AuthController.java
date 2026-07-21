package com.example.demo.controller;

import com.example.demo.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> request) {
        return authService.register(
            request.get("name"),
            request.get("email"),
            request.get("password"),
            request.get("role")
        );
    }
    
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        return authService.login(
            request.get("email"),
            request.get("password")
        );
    }
}
