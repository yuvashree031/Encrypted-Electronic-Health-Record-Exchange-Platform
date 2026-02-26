package com.example.demo.service;

import com.example.demo.entity.AuditLog;
import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.User;
import com.example.demo.repository.AuditLogRepository;
import com.example.demo.repository.MedicalRecordRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MedicalRecordService {
    
    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final EncryptionUtil encryptionUtil;
    
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;
    
    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                               UserRepository userRepository,
                               AuditLogRepository auditLogRepository,
                               EncryptionUtil encryptionUtil) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.encryptionUtil = encryptionUtil;
    }
    
    public Map<String, Object> createRecord(String email, Long patientId, String recordData, MultipartFile pdfFile) {
        User doctor = userRepository.findByEmail(email);
        if (doctor == null || !doctor.getRole().equals("DOCTOR")) {
            throw new RuntimeException("Only doctors can create records");
        }
        
        User patient = userRepository.findById(patientId).orElse(null);
        if (patient == null || !patient.getRole().equals("PATIENT")) {
            throw new RuntimeException("Invalid patient");
        }
        
        String encrypted = encryptionUtil.encrypt(recordData);
        String pdfFilePath = null;
        
        // Handle PDF file upload
        if (pdfFile != null && !pdfFile.isEmpty()) {
            try {
                // Create upload directory if it doesn't exist
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                // Generate unique filename
                String originalFilename = pdfFile.getOriginalFilename();
                String fileExtension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".pdf";
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                
                // Save file
                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(pdfFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                pdfFilePath = filePath.toString();
                
            } catch (IOException e) {
                throw new RuntimeException("Failed to store PDF file: " + e.getMessage());
            }
        }
        
        MedicalRecord record = new MedicalRecord();
        record.setDoctorId(doctor.getId());
        record.setPatientId(patientId);
        record.setEncryptedData(encrypted);
        record.setPdfFilePath(pdfFilePath);
        
        MedicalRecord saved = medicalRecordRepository.save(record);
        auditLogRepository.save(new AuditLog("RECORD_CREATED", email));
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("doctorId", saved.getDoctorId());
        response.put("patientId", saved.getPatientId());
        response.put("recordData", recordData);
        response.put("pdfFilePath", pdfFilePath);
        response.put("createdAt", saved.getCreatedAt());
        return response;
    }
    
    public List<Map<String, Object>> getMyRecords(String email) {
        User patient = userRepository.findByEmail(email);
        if (patient == null || !patient.getRole().equals("PATIENT")) {
            throw new RuntimeException("Only patients can view records");
        }
        
        List<MedicalRecord> records = medicalRecordRepository.findByPatientId(patient.getId());
        auditLogRepository.save(new AuditLog("RECORDS_VIEWED", email));
        
        List<Map<String, Object>> response = new ArrayList<>();
        for (MedicalRecord record : records) {
            String decrypted = encryptionUtil.decrypt(record.getEncryptedData());
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("doctorId", record.getDoctorId());
            map.put("patientId", record.getPatientId());
            map.put("recordData", decrypted);
            map.put("pdfFilePath", record.getPdfFilePath());
            map.put("createdAt", record.getCreatedAt());
            response.add(map);
        }
        return response;
    }
    
    public Map<String, Object> getRecordById(Long recordId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        MedicalRecord record = medicalRecordRepository.findById(recordId).orElse(null);
        if (record == null) {
            return null;
        }
        
        // Check if user has permission to access this record
        if (user.getRole().equals("PATIENT") && !record.getPatientId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        if (user.getRole().equals("DOCTOR") && !record.getDoctorId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        map.put("doctorId", record.getDoctorId());
        map.put("patientId", record.getPatientId());
        map.put("pdfFilePath", record.getPdfFilePath());
        map.put("createdAt", record.getCreatedAt());
        
        return map;
    }
}
