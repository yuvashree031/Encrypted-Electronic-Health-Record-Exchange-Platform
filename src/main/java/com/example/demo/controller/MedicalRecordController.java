package com.example.demo.controller;

import com.example.demo.service.MedicalRecordService;
import com.example.demo.util.EncryptionUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class MedicalRecordController {
    
    private final MedicalRecordService medicalRecordService;
    private final EncryptionUtil encryptionUtil;
    
    public MedicalRecordController(MedicalRecordService medicalRecordService,
                                  EncryptionUtil encryptionUtil) {
        this.medicalRecordService = medicalRecordService;
        this.encryptionUtil = encryptionUtil;
    }
    
    @PostMapping("/create")
    public Map<String, Object> createRecord(
            @RequestHeader("Authorization") String token,
            @RequestParam("patientId") Long patientId,
            @RequestParam("recordData") String recordData,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile) {
        
        String email = encryptionUtil.extractEmailFromToken(token.replace("Bearer ", ""));
        return medicalRecordService.createRecord(email, patientId, recordData, pdfFile);
    }
    
    @GetMapping("/download-pdf/{recordId}")
    public ResponseEntity<Resource> downloadPdf(
            @RequestHeader("Authorization") String token,
            @PathVariable Long recordId) {
        
        String email = encryptionUtil.extractEmailFromToken(token.replace("Bearer ", ""));
        Map<String, Object> record = medicalRecordService.getRecordById(recordId, email);
        
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        
        String pdfFilePath = (String) record.get("pdfFilePath");
        if (pdfFilePath == null || pdfFilePath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            Path filePath = Paths.get(pdfFilePath);
            Resource resource = new FileSystemResource(filePath);
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/pdf";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"medical-record-" + recordId + ".pdf\"")
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/my-records")
    public List<Map<String, Object>> getMyRecords(@RequestHeader("Authorization") String token) {
        String email = encryptionUtil.extractEmailFromToken(token.replace("Bearer ", ""));
        return medicalRecordService.getMyRecords(email);
    }
}
