package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class FileStorageConfig {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @PostConstruct
    public void init() {
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
    }
}
