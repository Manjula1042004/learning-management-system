package com.lms.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class SimpleFileService {

    public String uploadFile(MultipartFile file) throws IOException {
        // Return placeholder URL instead of UploadCare
        if (file.getContentType().startsWith("image/")) {
            return "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&h=600&fit=crop";
        } else if (file.getContentType().startsWith("video/")) {
            return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
        } else if (file.getContentType().equals("application/pdf")) {
            return "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";
        }
        return "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&h=600&fit=crop";
    }

    public boolean isImageFile(String contentType) {
        return true; // Always return true for testing
    }

    public boolean isVideoFile(String contentType) {
        return contentType != null && contentType.startsWith("video/");
    }

    public boolean isPdfFile(String contentType) {
        return contentType != null && contentType.equals("application/pdf");
    }
}