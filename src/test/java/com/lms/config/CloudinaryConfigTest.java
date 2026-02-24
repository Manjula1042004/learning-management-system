package com.lms.config;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CloudinaryConfigTest {

    @InjectMocks
    private CloudinaryConfig cloudinaryConfig;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cloudinaryConfig, "cloudName", "test-cloud");
        ReflectionTestUtils.setField(cloudinaryConfig, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(cloudinaryConfig, "apiSecret", "test-api-secret");
    }

    @Test
    void cloudinary_ShouldCreateBeanWithCorrectConfig() {
        Cloudinary cloudinary = cloudinaryConfig.cloudinary();

        assertThat(cloudinary).isNotNull();

        // Access config through cloudinary.config
        assertThat(cloudinary.config.cloudName).isEqualTo("test-cloud");
        assertThat(cloudinary.config.apiKey).isEqualTo("test-api-key");
        assertThat(cloudinary.config.apiSecret).isEqualTo("test-api-secret");
        assertThat(cloudinary.config.secure).isTrue();
    }

    @Test
    void cloudinary_ShouldHaveAllRequiredConfigKeys() {
        Cloudinary cloudinary = cloudinaryConfig.cloudinary();

        assertThat(cloudinary.config.cloudName).isNotNull();
        assertThat(cloudinary.config.apiKey).isNotNull();
        assertThat(cloudinary.config.apiSecret).isNotNull();
    }

    @Test
    void cloudinary_ShouldUseHttpsSecureTrue() {
        Cloudinary cloudinary = cloudinaryConfig.cloudinary();

        assertThat(cloudinary.config.secure).isTrue();
    }
}