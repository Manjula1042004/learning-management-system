package com.lms.service;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SimpleFileServiceTest {

    @InjectMocks
    private SimpleFileService simpleFileService;

    private MockMultipartFile videoFile;
    private MockMultipartFile pdfFile;
    private MockMultipartFile imageFile;
    private MockMultipartFile otherFile;

    @BeforeEach
    void setUp() {
        videoFile = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", "video content".getBytes());
        pdfFile = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "pdf content".getBytes());
        imageFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", "image content".getBytes());
        otherFile = new MockMultipartFile(
                "file", "text.txt", "text/plain", "text content".getBytes());
    }

    @Test
    void uploadFile_ShouldReturnImagePlaceholder_ForImageFile() throws IOException {
        String result = simpleFileService.uploadFile(imageFile);

        assertThat(result).startsWith("https://images.unsplash.com/");
    }

    @Test
    void uploadFile_ShouldReturnVideoPlaceholder_ForVideoFile() throws IOException {
        String result = simpleFileService.uploadFile(videoFile);

        assertThat(result).isEqualTo("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
    }

    @Test
    void uploadFile_ShouldReturnPdfPlaceholder_ForPdfFile() throws IOException {
        String result = simpleFileService.uploadFile(pdfFile);

        assertThat(result).isEqualTo("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
    }

    @Test
    void uploadFile_ShouldReturnDefaultImage_ForOtherFileTypes() throws IOException {
        String result = simpleFileService.uploadFile(otherFile);

        assertThat(result).startsWith("https://images.unsplash.com/");
    }

    @Test
    void isImageFile_ShouldAlwaysReturnTrue() {
        assertThat(simpleFileService.isImageFile("video/mp4")).isTrue();
        assertThat(simpleFileService.isImageFile("application/pdf")).isTrue();
        assertThat(simpleFileService.isImageFile(null)).isTrue();
    }

    @Test
    void isVideoFile_ShouldReturnTrue_ForVideoContentType() {
        assertThat(simpleFileService.isVideoFile("video/mp4")).isTrue();
        assertThat(simpleFileService.isVideoFile("video/quicktime")).isTrue();
    }

    @Test
    void isVideoFile_ShouldReturnFalse_ForNonVideoContentType() {
        assertThat(simpleFileService.isVideoFile("image/jpeg")).isFalse();
        assertThat(simpleFileService.isVideoFile("application/pdf")).isFalse();
    }

    @Test
    void isPdfFile_ShouldReturnTrue_ForPdfContentType() {
        assertThat(simpleFileService.isPdfFile("application/pdf")).isTrue();
    }

    @Test
    void isPdfFile_ShouldReturnFalse_ForNonPdfContentType() {
        assertThat(simpleFileService.isPdfFile("video/mp4")).isFalse();
        assertThat(simpleFileService.isPdfFile("image/jpeg")).isFalse();
    }

    @Test
    void isPdfFile_ShouldReturnFalse_WhenContentTypeNull() {
        assertThat(simpleFileService.isPdfFile(null)).isFalse();
    }

    @Test
    void isVideoFile_ShouldReturnFalse_WhenContentTypeNull() {
        assertThat(simpleFileService.isVideoFile(null)).isFalse();
    }
}