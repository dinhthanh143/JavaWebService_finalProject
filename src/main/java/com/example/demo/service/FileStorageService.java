package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Path rootPath = Paths.get(uploadDir.replace("${user.home}", System.getProperty("user.home")));

        if (!Files.exists(rootPath)) {
            Files.createDirectories(rootPath);
        }

        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase() : "";

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Dinh dang file khong hop le (chi nhan jpg, png, webp).");
        }

        String fileName = UUID.randomUUID().toString() + "." + ext;

        Files.copy(file.getInputStream(), rootPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        try {
            Path rootPath = Paths.get(uploadDir.replace("${user.home}", System.getProperty("user.home")));
            Files.deleteIfExists(rootPath.resolve(fileName));
        } catch (IOException e) {
            System.err.println("Loi khi xoa file: " + fileName);
        }
    }
}