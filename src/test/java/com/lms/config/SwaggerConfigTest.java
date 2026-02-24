package com.lms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SwaggerConfigTest {

    @InjectMocks
    private SwaggerConfig swaggerConfig;

    @BeforeEach
    void setUp() {
        swaggerConfig = new SwaggerConfig();
    }

    @Test
    void customOpenAPI_ShouldCreateOpenAPIWithCorrectInfo() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        assertThat(openAPI).isNotNull();

        Info info = openAPI.getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("Learning Management System API");
        assertThat(info.getVersion()).isEqualTo("1.0");
        assertThat(info.getDescription()).contains("Complete LMS Platform API Documentation");
        assertThat(info.getTermsOfService()).isEqualTo("https://example.com/terms");

        Contact contact = info.getContact();
        assertThat(contact).isNotNull();
        assertThat(contact.getName()).isEqualTo("LMS Support");
        assertThat(contact.getEmail()).isEqualTo("support@lms.com");
        assertThat(contact.getUrl()).isEqualTo("https://lms.com");

        License license = info.getLicense();
        assertThat(license).isNotNull();
        assertThat(license.getName()).isEqualTo("Apache 2.0");
        assertThat(license.getUrl()).isEqualTo("http://www.apache.org/licenses/LICENSE-2.0.html");
    }

    @Test
    void customOpenAPI_ShouldIncludeSecurityRequirement() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        List<SecurityRequirement> securityRequirements = openAPI.getSecurity();
        assertThat(securityRequirements).isNotNull();
        assertThat(securityRequirements).isNotEmpty();
    }
}