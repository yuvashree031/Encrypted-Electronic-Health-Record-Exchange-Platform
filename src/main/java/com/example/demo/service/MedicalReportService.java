package com.example.demo.service;

import com.example.demo.entity.MedicalReport;
import com.example.demo.repository.MedicalReportRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class MedicalReportService {
    
    @Autowired
    private MedicalReportRepository medicalReportRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    public MedicalReport uploadReport(MultipartFile file, Long doctorId, Long patientId, 
                                     String reportTitle, String description) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        if (!userRepository.existsById(doctorId)) {
            throw new IllegalArgumentException("Doctor not found");
        }
        
        if (!userRepository.existsById(patientId)) {
            throw new IllegalArgumentException("Patient not found");
        }
        
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        MedicalReport report = new MedicalReport();
        report.setDoctorId(doctorId);
        report.setPatientId(patientId);
        report.setFileName(fileName);
        report.setOriginalFileName(file.getOriginalFilename());
        report.setFileType(file.getContentType());
        report.setFileSize(file.getSize());
        report.setFilePath(filePath.toString());
        report.setReportTitle(reportTitle);
        report.setDescription(description);
        
        return medicalReportRepository.save(report);
    }
    
    public List<MedicalReport> getReportsForPatient(Long patientId) {
        return medicalReportRepository.findByPatientId(patientId);
    }
    
    public List<MedicalReport> getReportsByDoctor(Long doctorId) {
        return medicalReportRepository.findByDoctorId(doctorId);
    }
    
    public List<MedicalReport> getReportsForDoctorAndPatient(Long doctorId, Long patientId) {
        return medicalReportRepository.findByDoctorIdAndPatientId(doctorId, patientId);
    }
    
    public byte[] downloadReport(Long reportId) throws IOException {
        MedicalReport report = medicalReportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        Path filePath = Paths.get(report.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found on server");
        }
        
        return Files.readAllBytes(filePath);
    }
    
    public MedicalReport getReportById(Long reportId) {
        return medicalReportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
    }
    
    public void deleteReport(Long reportId) throws IOException {
        MedicalReport report = medicalReportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        Path filePath = Paths.get(report.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        medicalReportRepository.deleteById(reportId);
    }
    
    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
