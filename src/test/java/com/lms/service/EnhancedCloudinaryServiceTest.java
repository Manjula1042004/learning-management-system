package com.lms.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnhancedCloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private EnhancedCloudinaryService enhancedCloudinaryService;

    private MockMultipartFile videoFile;
    private MockMultipartFile pdfFile;
    private MockMultipartFile imageFile;
    private Map<String, Object> uploadResult;

    @BeforeEach
    void setUp() {
        videoFile = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", "video content".getBytes());
        pdfFile = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "pdf content".getBytes());
        imageFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", "image content".getBytes());

        uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://cloudinary.com/test.mp4");

        // Don't stub cloudinary.uploader() here - do it in each test
    }

    @Test
    void uploadFile_ShouldUploadVideoWithOptimizations() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = enhancedCloudinaryService.uploadFile(videoFile);

        assertThat(result).isEqualTo("https://cloudinary.com/test.mp4");
        verify(uploader).upload(any(byte[].class), argThat(map ->
                "video".equals(map.get("resource_type"))
        ));
    }

    @Test
    void uploadFile_ShouldUploadImageWithOptimizations() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = enhancedCloudinaryService.uploadFile(imageFile);

        assertThat(result).isEqualTo("https://cloudinary.com/test.mp4");
        verify(uploader).upload(any(byte[].class), argThat(map ->
                "image".equals(map.get("resource_type"))
        ));
    }

    @Test
    void uploadFile_ShouldUploadPdfAsRaw() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = enhancedCloudinaryService.uploadFile(pdfFile);

        assertThat(result).isEqualTo("https://cloudinary.com/test.mp4");
        verify(uploader).upload(any(byte[].class), argThat(map ->
                "raw".equals(map.get("resource_type"))
        ));
    }

    @Test
    void isVideoFile_ShouldReturnTrue_ForVideoContentType() {
        assertThat(enhancedCloudinaryService.isVideoFile("video/mp4")).isTrue();
    }

    @Test
    void isImageFile_ShouldReturnTrue_ForImageContentType() {
        assertThat(enhancedCloudinaryService.isImageFile("image/jpeg")).isTrue();
    }

    @Test
    void isPdfFile_ShouldReturnTrue_ForPdfContentType() {
        assertThat(enhancedCloudinaryService.isPdfFile("application/pdf")).isTrue();
    }
}