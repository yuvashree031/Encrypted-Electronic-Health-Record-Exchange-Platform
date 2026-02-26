package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserRepository userRepository;
    
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @GetMapping("/patients")
    public ResponseEntity<List<Map<String, Object>>> getAllPatients() {
        List<User> patients = userRepository.findAll().stream()
                .filter(user -> user.getRole().equals("PATIENT"))
                .collect(Collectors.toList());
        
        List<Map<String, Object>> patientList = patients.stream()
                .map(patient -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", patient.getId());
                    map.put("name", patient.getName());
                    map.put("email", patient.getEmail());
                    return map;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(patientList);
    }
}
