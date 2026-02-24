package com.lms.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    private MockMultipartFile videoFile;
    private MockMultipartFile pdfFile;
    private MockMultipartFile imageFile;
    private Map<String, Object> uploadResult;

    @BeforeEach
    void setUp() {
        // Fix: Create a proper mock for Cloudinary config
        com.cloudinary.Configuration mockConfig = mock(com.cloudinary.Configuration.class);
        lenient().when(cloudinary.config).thenReturn(mockConfig);
        lenient().when(mockConfig.cloudName).thenReturn("test-cloud");

        videoFile = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", "video content".getBytes());
        pdfFile = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "pdf content".getBytes());
        imageFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", "image content".getBytes());

        uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://cloudinary.com/test.mp4");

        // Only stub uploader when needed in tests
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void uploadPdf_ShouldUploadPdfAndReturnUrl() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadPdf(pdfFile);

        assertThat(result).isEqualTo("https://cloudinary.com/test.mp4.pdf");
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadPdf_ShouldThrowException_WhenUploadFails() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new RuntimeException("Upload failed"));

        assertThatThrownBy(() -> cloudinaryService.uploadPdf(pdfFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to upload PDF to Cloudinary");
    }

    @Test
    void uploadVideo_ShouldUploadVideoAndReturnUrl() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadVideo(videoFile);

        assertThat(result).isEqualTo("https://cloudinary.com/test.mp4");
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadVideo_ShouldThrowException_WhenUploadFails() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new RuntimeException("Upload failed"));

        assertThatThrownBy(() -> cloudinaryService.uploadVideo(videoFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to upload video to Cloudinary");
    }

    @Test
    void uploadFile_ShouldDetectVideoAndCallUploadVideo() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadFile(videoFile);

        assertThat(result).isEqualTo("https://cloudinary.com/test.mp4");
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadFile_ShouldDetectPdfAndCallUploadPdf() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadFile(pdfFile);

        assertThat(result).isEqualTo("https://cloudinary.com/test.mp4.pdf");
    }

    @Test
    void deleteFile_ShouldExtractPublicIdAndDelete() {
        String cloudinaryUrl = "https://res.cloudinary.com/dfunxatca/video/upload/v123456/lms_12345_test.mp4";

        when(cloudinary.uploader()).thenReturn(uploader);

        try {
            when(uploader.destroy(anyString(), anyMap())).thenReturn(new HashMap());
        } catch (Exception e) {
            // Ignore
        }

        cloudinaryService.deleteFile(cloudinaryUrl);

        try {
            verify(uploader).destroy(anyString(), anyMap());
        } catch (Exception e) {
            // Ignore
        }
    }

    @Test
    void deleteFile_ShouldDoNothing_WhenNotCloudinaryUrl() {
        String nonCloudinaryUrl = "https://example.com/video.mp4";

        cloudinaryService.deleteFile(nonCloudinaryUrl);

        // No verification needed - just ensure no exception
    }

    @Test
    void isVideoFile_ShouldReturnTrue_ForVideoContentType() {
        assertThat(cloudinaryService.isVideoFile("video/mp4")).isTrue();
    }

    @Test
    void isPdfFile_ShouldReturnTrue_ForPdfContentType() {
        assertThat(cloudinaryService.isPdfFile("application/pdf")).isTrue();
    }

    @Test
    void isImageFile_ShouldReturnTrue_ForImageContentType() {
        assertThat(cloudinaryService.isImageFile("image/jpeg")).isTrue();
    }
}