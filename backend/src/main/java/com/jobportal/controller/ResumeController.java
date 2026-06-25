package com.jobportal.controller;

import com.jobportal.model.User;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@RestController
// @CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
@RequiredArgsConstructor
public class ResumeController {

    private final UserRepository userRepository;

    // Inject the upload folder path from application.properties
    // Value("${file.upload-dir}") reads the key 'file.upload-dir' → "uploads"
    @Value("${file.upload-dir}")
    private String uploadDir;

    // ── POST /upload-resume ───────────────────────────────────────────────────
    @PostMapping("/upload-resume")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user) {

        if (file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Please select a file to upload"));

        if (file.getSize() > 5 * 1024 * 1024)
            return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds limit of 5 MB"));

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.endsWith(".pdf") && !originalName.endsWith(".docx"))
            return ResponseEntity.badRequest().body(Map.of("error", "Only PDF and DOCX files are allowed"));

        try {
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String savedFileName = "resume_" + user.getId() + extension; // Safe ID usage
            Path targetPath = Paths.get(uploadDir).resolve(savedFileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            user.setResumePath(targetPath.toString());
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Resume uploaded successfully", "path", targetPath.toString()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to store file: " + e.getMessage()));
        }
    }

    // ── GET /my-resume ────────────────────────────────────────────────────────
    @GetMapping("/my-resume")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<?> myResume(@AuthenticationPrincipal User user) {

        if (user.getResumePath() == null) {
            return ResponseEntity.ok(Map.of("hasResume", false));
        }

        // FIX: Extract just the filename from the absolute path string
        Path path = Paths.get(user.getResumePath());
        String fileName = path.getFileName().toString();

        // FIX: Change key from "resumePath" to "fileName" to match seeker-dashboard.html
        return ResponseEntity.ok(Map.of(
                "hasResume", true,
                "fileName", fileName
        ));
    }

    // ── GET /resume/{seekerId} ────────────────────────────────────────────────
    @GetMapping("/resume/{seekerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYER')")
    public ResponseEntity<?> downloadResume(@PathVariable Long seekerId) {

        User user = userRepository.findById(seekerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getResumePath() == null) {
            return ResponseEntity.status(404).body(Map.of("error", "This candidate has not uploaded a resume yet"));
        }

        try {
            Path filePath = Paths.get(user.getResumePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.status(404).body(Map.of("error", "Resume file not found on server"));
            }

            String contentType = user.getResumePath().endsWith(".pdf")
                    ? "application/pdf" : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Could not read file: " + e.getMessage()));
        }
    }
}
