package com.lms.security;

import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.UserRepository;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User student;
    private User instructor;
    private User admin;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        admin = TestDataFactory.createAdmin(3L);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_ForStudent() {
        // Arrange
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("student1");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("student1");
        assertThat(userDetails.getPassword()).isEqualTo("password123");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_STUDENT");
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_ForInstructor() {
        // Arrange
        when(userRepository.findByUsername("instructor2")).thenReturn(Optional.of(instructor));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("instructor2");

        // Assert
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_INSTRUCTOR");
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_ForAdmin() {
        // Arrange
        when(userRepository.findByUsername("admin3")).thenReturn(Optional.of(admin));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin3");

        // Assert
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: nonexistent");
    }

    @Test
    void loadUserByUsername_ShouldHandleUserWithNullUsername() {
        // Arrange
        when(userRepository.findByUsername(null)).thenThrow(new IllegalArgumentException("Username cannot be null"));

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void loadUserByUsername_ShouldHandleEmptyUsername() {
        // Arrange
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: ");
    }

    @Test
    void loadUserByUsername_ShouldHandleWhitespaceUsername() {
        // Arrange
        when(userRepository.findByUsername("   ")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("   "))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found:    ");
    }

    @Test
    void loadUserByUsername_ShouldSetCorrectAccountStatus_WhenUserDisabled() {
        // Arrange
        student.setEnabled(false);
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("student1");

        // Assert
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_ShouldHandleSpecialCharactersInUsername() {
        // Arrange
        User specialUser = TestDataFactory.createUser(4L, "user@special#123", "special@test.com", Role.STUDENT);
        when(userRepository.findByUsername("user@special#123")).thenReturn(Optional.of(specialUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@special#123");

        // Assert
        assertThat(userDetails.getUsername()).isEqualTo("user@special#123");
    }

    @Test
    void loadUserByUsername_ShouldBeCaseSensitive() {
        // Arrange
        when(userRepository.findByUsername("STUDENT1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("STUDENT1"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: STUDENT1");
    }

    @Test
    void loadUserByUsername_ShouldReturnUserWithProperAuthorities() {
        // Arrange
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("student1");

        // Assert
        assertThat(userDetails.getAuthorities())
                .hasSize(1)
                .allMatch(auth -> auth.getAuthority().startsWith("ROLE_"));
    }
}