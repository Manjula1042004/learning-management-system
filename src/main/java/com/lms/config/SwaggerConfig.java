package com.lms.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Learning Management System API")
                        .version("1.0")
                        .description("""
                                Complete LMS Platform API Documentation
                                
                                ## Features:
                                - User Authentication & Authorization
                                - Course Management
                                - Lesson Management  
                                - Student Enrollment
                                - Media Upload
                                - Progress Tracking
                                - Payment Integration
                                """)
                        .termsOfService("https://example.com/terms")
                        .contact(new Contact()
                                .name("LMS Support")
                                .email("support@lms.com")
                                .url("https://lms.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}