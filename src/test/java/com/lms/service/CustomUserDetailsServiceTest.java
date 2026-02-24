package com.lms.service;

import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.UserRepository;
import com.lms.security.CustomUserDetailsService;
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
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("student1");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("student1");
        assertThat(userDetails.getPassword()).isEqualTo("password123");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_STUDENT");
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_ForInstructor() {
        when(userRepository.findByUsername("instructor2")).thenReturn(Optional.of(instructor));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("instructor2");

        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_INSTRUCTOR");
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_ForAdmin() {
        when(userRepository.findByUsername("admin3")).thenReturn(Optional.of(admin));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin3");

        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: nonexistent");
    }

    @Test
    void loadUserByUsername_ShouldSetCorrectAccountStatus() {
        student.setEnabled(false);
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("student1");

        assertThat(userDetails.isEnabled()).isFalse();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }
}