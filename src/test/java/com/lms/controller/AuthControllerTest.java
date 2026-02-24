package com.lms.controller;

import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.service.UserService;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private Model model;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        model = new BindingAwareModelMap();
    }

    @Test
    void showLoginForm_ShouldReturnLoginView() {
        // Act
        String viewName = authController.showLoginForm(null, null, model);

        // Assert
        assertThat(viewName).isEqualTo("auth/login");
        assertThat(model.containsAttribute("error")).isFalse();
        assertThat(model.containsAttribute("message")).isFalse();
    }

    @Test
    void showLoginForm_ShouldAddErrorAttribute_WhenErrorParamPresent() {
        // Act
        String viewName = authController.showLoginForm("error", null, model);

        // Assert
        assertThat(viewName).isEqualTo("auth/login");
        assertThat(model.getAttribute("error")).isEqualTo("Invalid username or password");
    }

    @Test
    void showLoginForm_ShouldAddMessageAttribute_WhenLogoutParamPresent() {
        // Act
        String viewName = authController.showLoginForm(null, "logout", model);

        // Assert
        assertThat(viewName).isEqualTo("auth/login");
        assertThat(model.getAttribute("message")).isEqualTo("You have been logged out successfully");
    }

    @Test
    void showRegisterForm_ShouldReturnRegisterView() {
        // Act
        String viewName = authController.showRegisterForm(model);

        // Assert
        assertThat(viewName).isEqualTo("auth/register");
        assertThat(model.getAttribute("user")).isNotNull();
    }

    @Test
    void registerUser_ShouldRegisterStudent_WhenValidData() {
        // Arrange
        String username = "newstudent";
        String email = "new@test.com";
        String password = "password123";
        String confirmPassword = "password123";
        String role = "STUDENT";

        when(userService.createUser(anyString(), anyString(), anyString(), any(Role.class)))
                .thenReturn(TestDataFactory.createStudent(1L));

        // Act
        String viewName = authController.registerUser(username, email, password, confirmPassword, role, model);

        // Assert
        assertThat(viewName).isEqualTo("auth/login");
        assertThat(model.getAttribute("success")).isEqualTo("Registration successful! Please login.");
        verify(userService).createUser(username, email, password, Role.STUDENT);
    }

    @Test
    void registerUser_ShouldRegisterInstructor_WhenValidData() {
        // Arrange
        String username = "newinstructor";
        String email = "instructor@test.com";
        String password = "password123";
        String confirmPassword = "password123";
        String role = "INSTRUCTOR";

        when(userService.createUser(anyString(), anyString(), anyString(), any(Role.class)))
                .thenReturn(TestDataFactory.createInstructor(1L));

        // Act
        String viewName = authController.registerUser(username, email, password, confirmPassword, role, model);

        // Assert
        assertThat(viewName).isEqualTo("auth/login");
        verify(userService).createUser(username, email, password, Role.INSTRUCTOR);
    }

    @Test
    void registerUser_ShouldReturnError_WhenPasswordsDoNotMatch() {
        // Arrange
        String username = "newuser";
        String email = "test@test.com";
        String password = "password123";
        String confirmPassword = "different";
        String role = "STUDENT";

        // Act
        String viewName = authController.registerUser(username, email, password, confirmPassword, role, model);

        // Assert
        assertThat(viewName).isEqualTo("auth/register");
        assertThat(model.getAttribute("error")).isEqualTo("Passwords do not match");
        verify(userService, never()).createUser(anyString(), anyString(), anyString(), any());
    }

    @Test
    void registerUser_ShouldReturnError_WhenInvalidRole() {
        // Arrange
        String username = "newuser";
        String email = "test@test.com";
        String password = "password123";
        String confirmPassword = "password123";
        String role = "INVALID";

        // Act
        String viewName = authController.registerUser(username, email, password, confirmPassword, role, model);

        // Assert
        assertThat(viewName).isEqualTo("auth/register");
        assertThat(model.getAttribute("error")).isEqualTo("Invalid role selected. Please choose Student or Instructor.");
        verify(userService, never()).createUser(anyString(), anyString(), anyString(), any());
    }

    @Test
    void registerUser_ShouldReturnError_WhenServiceThrowsException() {
        // Arrange
        String username = "existinguser";
        String email = "existing@test.com";
        String password = "password123";
        String confirmPassword = "password123";
        String role = "STUDENT";

        when(userService.createUser(anyString(), anyString(), anyString(), any(Role.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        // Act
        String viewName = authController.registerUser(username, email, password, confirmPassword, role, model);

        // Assert
        assertThat(viewName).isEqualTo("auth/register");
        assertThat(model.getAttribute("error")).isEqualTo("Username already exists");
    }

    @Test
    void showAdminRegisterForm_ShouldReturnAdminRegisterView() {
        // Act
        String viewName = authController.showAdminRegisterForm(model);

        // Assert
        assertThat(viewName).isEqualTo("auth/register-admin");
        assertThat(model.getAttribute("user")).isNotNull();
    }

    @Test
    void registerAdminUser_ShouldRegisterAdmin_WhenValidSecretKey() {
        // Arrange
        String username = "newadmin";
        String email = "admin@test.com";
        String password = "admin123";
        String secretKey = "ADMIN_SECRET_123";

        when(userService.createUser(anyString(), anyString(), anyString(), any(Role.class)))
                .thenReturn(TestDataFactory.createAdmin(1L));

        // Act
        String viewName = authController.registerAdminUser(username, email, password, secretKey, model);

        // Assert
        assertThat(viewName).isEqualTo("auth/login");
        assertThat(model.getAttribute("success")).isEqualTo("Admin registration successful! Please login.");
        verify(userService).createUser(username, email, password, Role.ADMIN);
    }

    @Test
    void registerAdminUser_ShouldReturnError_WhenInvalidSecretKey() {
        // Arrange
        String username = "newadmin";
        String email = "admin@test.com";
        String password = "admin123";
        String secretKey = "WRONG_KEY";

        // Act
        String viewName = authController.registerAdminUser(username, email, password, secretKey, model);

        // Assert
        assertThat(viewName).isEqualTo("auth/register-admin");
        assertThat(model.getAttribute("error")).isEqualTo("Invalid secret key");
        verify(userService, never()).createUser(anyString(), anyString(), anyString(), any());
    }

    @Test
    void logout_ShouldRedirectToLogin() {
        // Act
        String viewName = authController.logout();

        // Assert
        assertThat(viewName).isEqualTo("redirect:/auth/login?logout=true");
    }
}