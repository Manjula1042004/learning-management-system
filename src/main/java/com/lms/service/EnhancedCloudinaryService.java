package com.lms.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class EnhancedCloudinaryService {

    private final Cloudinary cloudinary;

    public EnhancedCloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload file with optimized settings based on file type
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        byte[] fileBytes = file.getBytes();

        // Generate unique public ID
        String publicId = "lms_media_" + UUID.randomUUID().toString();

        // Configure upload options based on file type
        Map<String, Object> uploadOptions = ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "auto" // Auto-detect type
        );

        // Add type-specific optimizations
        if (isVideoFile(contentType)) {
            uploadOptions.put("resource_type", "video");
            uploadOptions.put("chunk_size", 6000000); // 6MB chunks for video
            uploadOptions.put("eager", ObjectUtils.asMap(
                    "format", "mp4",
                    "quality", "auto"
            ));
        } else if (isImageFile(contentType)) {
            uploadOptions.put("resource_type", "image");
            uploadOptions.put("quality", "auto");
            uploadOptions.put("fetch_format", "auto");
        } else if (isPdfFile(contentType)) {
            uploadOptions.put("resource_type", "raw");
        } else if (isAudioFile(contentType)) {
            uploadOptions.put("resource_type", "video"); // Cloudinary treats audio as video
        }

        // Upload to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(fileBytes, uploadOptions);

        return (String) uploadResult.get("secure_url");
    }

    /**
     * Delete file from Cloudinary
     */
    public void deleteFile(String fileUrl) {
        try {
            String publicId = extractPublicId(fileUrl);
            if (publicId != null) {
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                System.out.println("🗑️ File deleted from Cloudinary: " + fileUrl);
            }
        } catch (Exception e) {
            System.err.println("❌ Error deleting from Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Generate thumbnail URL for video
     */
    public String generateVideoThumbnail(String videoUrl) {
        if (videoUrl == null || !videoUrl.contains("cloudinary.com")) {
            return null;
        }

        try {
            String publicId = extractPublicId(videoUrl);
            if (publicId != null) {
                // Generate thumbnail at 5 second mark
                return cloudinary.url()
                        .resourceType("video")
                        .publicId(publicId)
                        .format("jpg")
                        .generate();
            }
        } catch (Exception e) {
            System.err.println("Error generating thumbnail: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get file information from Cloudinary
     */
    public Map<String, Object> getFileInfo(String fileUrl) {
        try {
            String publicId = extractPublicId(fileUrl);
            if (publicId != null) {
                return cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            System.err.println("Error getting file info: " + e.getMessage());
        }
        return null;
    }

    // Helper methods
    private String extractPublicId(String fileUrl) {
        try {
            // Cloudinary URL format: https://res.cloudinary.com/cloudName/resource_type/upload/version/publicId.extension
            String[] parts = fileUrl.split("/");

            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("upload") && i + 2 < parts.length) {
                    String publicIdWithExt = parts[i + 2];
                    // Remove file extension
                    return publicIdWithExt.substring(0, publicIdWithExt.lastIndexOf('.'));
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isVideoFile(String contentType) {
        return contentType != null && contentType.startsWith("video/");
    }

    public boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isPdfFile(String contentType) {
        return contentType != null && contentType.equals("application/pdf");
    }

    public boolean isAudioFile(String contentType) {
        return contentType != null && contentType.startsWith("audio/");
    }
}