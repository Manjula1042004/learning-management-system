package com.lms;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CreateAdmin {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("admin123");
        System.out.println("Admin password hash: " + encodedPassword);

        // Use this hash in SQL or in UserService
    }

}