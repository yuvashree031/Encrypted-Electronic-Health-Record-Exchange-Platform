package com.example.demo.controller;

import com.example.demo.entity.MedicalReport;
import com.example.demo.service.MedicalReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    @Autowired
    private MedicalReportService medicalReportService;
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("doctorId") Long doctorId,
            @RequestParam("patientId") Long patientId,
            @RequestParam(value = "reportTitle", required = false) String reportTitle,
            @RequestParam(value = "description", required = false) String description) {
        
        try {
            MedicalReport report = medicalReportService.uploadReport(
                file, doctorId, patientId, reportTitle, description);
            
            return ResponseEntity.ok(Map.of(
                "message", "Report uploaded successfully",
                "reportId", report.getId(),
                "fileName", report.getOriginalFileName(),
                "fileSize", report.getFileSize(),
                "uploadedAt", report.getUploadedAt()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalReport>> getReportsForPatient(@PathVariable Long patientId) {
        try {
            List<MedicalReport> reports = medicalReportService.getReportsForPatient(patientId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<MedicalReport>> getReportsByDoctor(@PathVariable Long doctorId) {
        try {
            List<MedicalReport> reports = medicalReportService.getReportsByDoctor(doctorId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/doctor/{doctorId}/patient/{patientId}")
    public ResponseEntity<List<MedicalReport>> getReportsForDoctorAndPatient(
            @PathVariable Long doctorId, @PathVariable Long patientId) {
        try {
            List<MedicalReport> reports = medicalReportService.getReportsForDoctorAndPatient(doctorId, patientId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{reportId}/download")
    public ResponseEntity<ByteArrayResource> downloadReport(@PathVariable Long reportId) {
        try {
            MedicalReport report = medicalReportService.getReportById(reportId);
            byte[] data = medicalReportService.downloadReport(reportId);
            
            ByteArrayResource resource = new ByteArrayResource(data);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + report.getOriginalFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{reportId}/view")
    public ResponseEntity<ByteArrayResource> viewReport(@PathVariable Long reportId) {
        try {
            MedicalReport report = medicalReportService.getReportById(reportId);
            byte[] data = medicalReportService.downloadReport(reportId);
            
            ByteArrayResource resource = new ByteArrayResource(data);
            
            MediaType mediaType = MediaType.parseMediaType(report.getFileType());
            if (mediaType == null) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + report.getOriginalFileName() + "\"")
                .contentType(mediaType)
                .contentLength(data.length)
                .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{reportId}")
    public ResponseEntity<MedicalReport> getReportDetails(@PathVariable Long reportId) {
        try {
            MedicalReport report = medicalReportService.getReportById(reportId);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Map<String, String>> deleteReport(@PathVariable Long reportId) {
        try {
            medicalReportService.deleteReport(reportId);
            return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete file: " + e.getMessage()));
        }
    }
}
