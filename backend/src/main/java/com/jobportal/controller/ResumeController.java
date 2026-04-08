package com.jobportal.controller;

import com.jobportal.model.User;
import com.jobportal.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

/**
 * ResumeController — handles resume upload and download.
 *
 * Endpoints:
 *   POST /upload-resume      — Seeker uploads a PDF or DOCX file (max 5 MB)
 *   GET  /my-resume          — Seeker checks if they have an uploaded resume
 *   GET  /resume/{seekerId}  — Employer downloads a seeker's resume file
 */
@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class ResumeController {

    // Inject the upload folder path from application.properties
    // Value("${file.upload-dir}") reads the key 'file.upload-dir' → "uploads"
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private UserRepository userRepository;

    // ── Helper: read X-User-Id header or fall back to session ─────────────────
    private Long resolveUserId(HttpServletRequest request, HttpSession session) {
        String h = request.getHeader("X-User-Id");
        if (h != null) {
            try { return Long.valueOf(h); } catch (Exception ignored) {}
        }
        return (Long) session.getAttribute("userId");
    }

    // ── Helper: read X-User-Role header or fall back to session ───────────────
    private String resolveRole(HttpServletRequest request, HttpSession session) {
        String h = request.getHeader("X-User-Role");
        if (h != null) return h;
        return (String) session.getAttribute("userRole");
    }

    // ── POST /upload-resume ───────────────────────────────────────────────────
    // Accepts a multipart file from the seeker.
    // @RequestParam("file") binds the "file" field from the FormData.
    @PostMapping("/upload-resume")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request,
            HttpSession session) {

        // 1. Check that a seeker is making this request
        Long userId = resolveUserId(request, session);
        String role = resolveRole(request, session);
        if (userId == null || !"seeker".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only seekers can upload a resume"));
        }

        // 2. Validate file is not empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please select a file to upload"));
        }

        // 3. Validate file type — only PDF and DOCX are allowed
        String originalName = file.getOriginalFilename();           // e.g. "MyResume.pdf"
        if (originalName == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid file name"));
        }
        String lowerName = originalName.toLowerCase();
        if (!lowerName.endsWith(".pdf") && !lowerName.endsWith(".docx")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only PDF and DOCX files are allowed"));
        }

        // 4. Validate file size — max 5 MB (5 * 1024 * 1024 bytes)
        // Spring also enforces this via application.properties, but we double-check here
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "File must be smaller than 5 MB"));
        }

        // 5. Build the ABSOLUTE path to the uploads/ folder
        // Paths.get(uploadDir).toAbsolutePath() ensures the folder is in your project root,
        // not in a temporary Windows/Tomcat folder.
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        // 6. Create the uploads/ folder if it doesn't exist
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Could not create upload directory: " + e.getMessage()));
        }

        // 7. Build the file name and the target path
        String extension = lowerName.endsWith(".pdf") ? ".pdf" : ".docx";
        String savedFileName = "resume_" + userId + extension;
        Path targetPath = uploadPath.resolve(savedFileName);

        // 8. Save the file to the uploads/ folder
        try {
            // file.getInputStream() reads the uploaded file's data
            // StandardCopyOption.REPLACE_EXISTING ensures that if a user uploads a new resume,
            // the old one is replaced.
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to save file: " + e.getMessage()));
        }

        // 8. Update the user's resume_path column in the database
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        User user = opt.get();
        user.setResumePath(targetPath.toString()); // store the full relative path
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "message",  "Resume uploaded successfully",
            "fileName", savedFileName
        ));
    }

    // ── GET /my-resume ────────────────────────────────────────────────────────
    // Called by the seeker to check whether they have a resume uploaded.
    // Returns { hasResume: true/false, fileName: "resume_5.pdf" }
    @GetMapping("/my-resume")
    public ResponseEntity<?> myResume(HttpServletRequest request, HttpSession session) {

        Long userId = resolveUserId(request, session);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = opt.get();
        String resumePath = user.getResumePath();

        // Check if the path is stored and the file actually exists on disk
        if (resumePath == null || resumePath.isBlank() || !new File(resumePath).exists()) {
            return ResponseEntity.ok(Map.of("hasResume", false));
        }

        // Extract just the file name (without the folder prefix) for display
        String fileName = new File(resumePath).getName(); // e.g. "resume_5.pdf"
        return ResponseEntity.ok(Map.of(
            "hasResume", true,
            "fileName",  fileName
        ));
    }

    // ── GET /resume/{seekerId} ────────────────────────────────────────────────
    // Called by an employer to download a seeker's resume file.
    // The browser will prompt a "Save As" dialog because of Content-Disposition: attachment.
    @GetMapping("/resume/{seekerId}")
    public ResponseEntity<?> downloadResume(
            @PathVariable Long seekerId,
            HttpServletRequest request,
            HttpSession session) {

        // 1. Only employers can download resumes
        String role = resolveRole(request, session);
        if (!"employer".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Employer access required"));
        }

        // 2. Look up the seeker
        Optional<User> opt = userRepository.findById(seekerId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Seeker not found"));
        }

        User seeker = opt.get();
        String resumePath = seeker.getResumePath();

        // 3. Check if the seeker has uploaded a resume
        if (resumePath == null || resumePath.isBlank()) {
            return ResponseEntity.status(404).body(Map.of("error", "This seeker has not uploaded a resume"));
        }

        // 4. Load the file from disk as a Spring Resource
        try {
            Path filePath = Paths.get(resumePath).toAbsolutePath(); // absolute path on server

            // toUri() can return null if the path is invalid; check before using
            java.net.URI fileUri = filePath.toUri();
            if (fileUri == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Invalid file path"));
            }

            Resource resource = new UrlResource(fileUri); // wraps the path as a URL resource

            if (!resource.exists()) {
                return ResponseEntity.status(404).body(Map.of("error", "Resume file not found on server"));
            }

            // 5. Determine MIME type based on file extension
            String contentType;
            // getFileName() can be null for root paths; use toString() safely with a fallback
            Path fileNamePath = filePath.getFileName();
            String fileName = (fileNamePath != null) ? fileNamePath.toString() : "resume";
            if (fileName.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else {
                // For .docx files
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }

            // 6. Return the file as a download response
            // Content-Disposition: attachment tells the browser to download (not display) the file
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Could not read file: " + e.getMessage()));
        }
    }
}
