package com.lms.testutil;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserDetails principal = User.builder()
                .username(annotation.username())
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + annotation.role())))
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, "password", principal.getAuthorities());

        context.setAuthentication(auth);
        return context;
    }
}