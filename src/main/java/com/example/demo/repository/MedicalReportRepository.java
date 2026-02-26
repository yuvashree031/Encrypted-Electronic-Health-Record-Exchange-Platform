package com.example.demo.repository;

import com.example.demo.entity.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {
    
    List<MedicalReport> findByDoctorId(Long doctorId);
    
    List<MedicalReport> findByPatientId(Long patientId);
    
    List<MedicalReport> findByDoctorIdAndPatientId(Long doctorId, Long patientId);
}
