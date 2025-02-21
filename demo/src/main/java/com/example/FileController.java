package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;


@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {

        File dir = new File(uploadDir);
        System.out.println("Upload directory: " + uploadDir); 
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("Directory created: " + created);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (!originalFilename.endsWith(".md")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid file type. Please upload a Markdown (.md) file.");
        }

        // Save the uploaded file
        File uploadedFile = new File(uploadDir, originalFilename);
        try {
            file.transferTo(uploadedFile);
            String outputFilePath = new File(uploadDir, originalFilename + ".pptx").getAbsolutePath();
            generatePPTX(uploadedFile.getAbsolutePath(), outputFilePath);

            // Check if the PPTX file was created
            File pptxFile = new File(outputFilePath);
            if (!pptxFile.exists()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("PPTX file was not generated.");
            }

            FileSystemResource resource = new FileSystemResource(pptxFile);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + pptxFile.getName());
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.presentationml.presentation");

   
            new Thread(() -> {
                try {
                    Thread.sleep(5000); 
                    if (pptxFile.exists()) {
                        pptxFile.delete();
                    }
                    if (uploadedFile.exists()) {
                        uploadedFile.delete();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating PPTX: " + e.getMessage());
        }
    }

    private void generatePPTX(String inputFilePath, String outputFilePath) throws IOException, InterruptedException {
        System.out.println("PATH: " + System.getenv("PATH"));

        ProcessBuilder processBuilder = new ProcessBuilder("C:/Users/hegde/AppData/Roaming/npm/marp.cmd", inputFilePath, "--pptx", "-o", outputFilePath);
        processBuilder.inheritIO();

        Process process = processBuilder.start();
        process.waitFor();
    }
}