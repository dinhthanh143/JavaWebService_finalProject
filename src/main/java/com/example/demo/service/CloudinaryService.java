package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;


    public List<String> uploadMultipleFiles(List<MultipartFile> files) {
        List<String> imageUrls = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            return imageUrls;
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

                String url = (String) uploadResult.get("secure_url");
                imageUrls.add(url);

            } catch (IOException e) {
                System.err.println("Lỗi upload file: " + file.getOriginalFilename() + " -> " + e.getMessage());
            }
        }

        return imageUrls;
    }

}
