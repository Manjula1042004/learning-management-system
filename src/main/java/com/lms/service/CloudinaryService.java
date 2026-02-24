package com.lms.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
        System.out.println("✅ Cloudinary Service Initialized");
        System.out.println("✅ Cloud Name: " + cloudinary.config.cloudName);
    }

    public String uploadPdf(MultipartFile file) throws IOException {
        try {
            System.out.println("=== DEBUG: Cloudinary uploadPdf called ===");
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("Content type: " + file.getContentType());

            // Upload PDF with specific options for PDF files
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",  // Use "raw" for PDF files
                            "folder", "lms/pdfs",
                            "public_id", file.getOriginalFilename().replace(".pdf", ""),
                            "format", "pdf"
                    ));

            String secureUrl = (String) uploadResult.get("secure_url");
            System.out.println("=== DEBUG: Cloudinary upload successful ===");
            System.out.println("Secure URL: " + secureUrl);

            // Make sure the URL ends with .pdf
            if (!secureUrl.endsWith(".pdf")) {
                secureUrl = secureUrl + ".pdf";
            }

            System.out.println("=== DEBUG: Final PDF URL: " + secureUrl);
            return secureUrl;

        } catch (Exception e) {
            System.err.println("=== DEBUG: Cloudinary upload error: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload PDF to Cloudinary: " + e.getMessage());
        }
    }

    public String uploadVideo(MultipartFile file) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", "lms/videos"
                    ));
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new IOException("Failed to upload video to Cloudinary: " + e.getMessage());
        }
    }

    // Upload ANY file to Cloudinary
    public String uploadFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        if (contentType != null) {
            if (contentType.startsWith("video/")) {
                return uploadVideo(file);
            } else if (contentType.equals("application/pdf")) {
                return uploadPdf(file);
            }
        }

        // Default to general upload
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "lms/files"));
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new IOException("Failed to upload file to Cloudinary: " + e.getMessage());
        }
    }

    // Delete file from Cloudinary
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("cloudinary.com")) {
            System.out.println("⚠️ Not a Cloudinary URL, skipping delete: " + fileUrl);
            return;
        }

        try {
            // Extract public ID from URL
            // Example: https://res.cloudinary.com/dfunxatca/video/upload/v123456/lms_1234567890_video.mp4
            String publicId = extractPublicId(fileUrl);

            if (publicId != null) {
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                System.out.println("🗑️ File deleted from Cloudinary: " + fileUrl);
                System.out.println("🗑️ Result: " + result);
            }

        } catch (Exception e) {
            System.err.println("❌ Error deleting from Cloudinary: " + e.getMessage());
        }
    }

    private String extractPublicId(String fileUrl) {
        try {
            // Cloudinary URL format: https://res.cloudinary.com/cloudName/resource_type/upload/version/publicId.extension
            String[] parts = fileUrl.split("/");

            // Find the part after 'upload' and before the file extension
            boolean foundUpload = false;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("upload")) {
                    // The next part is version (v123456), skip it
                    // The part after version is publicId.extension
                    if (i + 2 < parts.length) {
                        String publicIdWithExt = parts[i + 2];
                        // Remove file extension
                        return publicIdWithExt.substring(0, publicIdWithExt.lastIndexOf('.'));
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // Get sample file URL if upload fails
    private String getSampleFileUrl(String contentType) {
        System.out.println("⚠️ Using sample file for content type: " + contentType);

        if (contentType == null) {
            return "https://res.cloudinary.com/dfunxatca/video/upload/v1744357757/sample_video.mp4";
        }

        if (contentType.startsWith("video/")) {
            return "https://res.cloudinary.com/dfunxatca/video/upload/v1744357757/sample_video.mp4";
        } else if (contentType.equals("application/pdf")) {
            return "https://res.cloudinary.com/dfunxatca/raw/upload/v1744357757/sample.pdf";
        } else if (contentType.startsWith("image/")) {
            return "https://res.cloudinary.com/dfunxatca/image/upload/v1744357757/sample.jpg";
        }

        return "https://res.cloudinary.com/dfunxatca/video/upload/v1744357757/sample_video.mp4";
    }

    public boolean isVideoFile(String contentType) {
        return contentType != null && contentType.startsWith("video/");
    }

    public boolean isPdfFile(String contentType) {
        return contentType != null && contentType.equals("application/pdf");
    }

    public boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
}