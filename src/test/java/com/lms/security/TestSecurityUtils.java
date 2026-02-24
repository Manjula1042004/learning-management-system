package com.lms.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

public class TestSecurityUtils {

    public static void mockAuthenticatedUser(String username, String role) {
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)))
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    public static String createMockJwtToken(String username) {
        // This is just a mock token for testing, not a real JWT
        return "eyJhbGciOiJIUzI1NiJ9." +
                java.util.Base64.getEncoder().encodeToString(("{\"sub\":\"" + username + "\"}").getBytes()) +
                ".signature";
    }
}