package com.lms.security;

import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@test.com", "password", Role.STUDENT);
        testUser.setId(1L);
        validToken = "valid.jwt.token";

        when(jwtTokenUtil.extractUsername(validToken)).thenReturn("testuser");
        when(jwtTokenUtil.validateToken(validToken, null)).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    void accessProtectedEndpoint_WithValidToken_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/protected")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void accessProtectedEndpoint_WithInvalidToken_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/protected")
                        .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithoutToken_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void accessProtectedEndpoint_WithSessionAuth_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isOk());
    }
}