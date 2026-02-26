package com.example.demo.service;

import com.example.demo.entity.AuditLog;
import com.example.demo.entity.User;
import com.example.demo.repository.AuditLogRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.EncryptionUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final EncryptionUtil encryptionUtil;
    
    public AuthService(UserRepository userRepository, 
                      AuditLogRepository auditLogRepository,
                      EncryptionUtil encryptionUtil) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.encryptionUtil = encryptionUtil;
    }
    
    public Map<String, String> register(String name, String email, String password, String role) {
        User existing = userRepository.findByEmail(email);
        if (existing != null) {
            throw new RuntimeException("Email already exists");
        }
        
        String hashedPassword = encryptionUtil.hashPassword(password);
        User user = new User(name, email, hashedPassword, role);
        userRepository.save(user);
        
        auditLogRepository.save(new AuditLog("USER_REGISTERED", email));
        
        String token = encryptionUtil.generateToken(email);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", email);
        response.put("role", role);
        return response;
    }
    
    public Map<String, String> login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Invalid credentials");
        }
        
        String hashedPassword = encryptionUtil.hashPassword(password);
        if (!user.getPassword().equals(hashedPassword)) {
            throw new RuntimeException("Invalid credentials");
        }
        
        auditLogRepository.save(new AuditLog("USER_LOGIN", email));
        
        String token = encryptionUtil.generateToken(email);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", email);
        response.put("role", user.getRole());
        return response;
    }
}
