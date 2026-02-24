package com.lms.controller;

import com.lms.entity.User;
import com.lms.entity.Role;
import com.lms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Operation(summary = "Show login form", description = "Display the login page")
    @GetMapping("/login")
    public String showLoginForm(@Parameter(description = "Error message from failed login")
                                @RequestParam(value = "error", required = false) String error,
                                @Parameter(description = "Logout success message")
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "auth/login";
    }

    @Operation(summary = "Show registration form", description = "Display the user registration page")
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @Operation(summary = "Register new user", description = "Register a new student or instructor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    @PostMapping("/register")
    public String registerUser(@Parameter(description = "Username (must be unique)", required = true)
                               @RequestParam String username,
                               @Parameter(description = "Email address (must be unique)", required = true)
                               @RequestParam String email,
                               @Parameter(description = "Password", required = true)
                               @RequestParam String password,
                               @Parameter(description = "Confirm password (must match password)")
                               @RequestParam(required = false) String confirmPassword,
                               @Parameter(description = "User role (STUDENT or INSTRUCTOR)", required = true)
                               @RequestParam String role,
                               Model model) {

        // Validate password confirmation
        if (confirmPassword != null && !password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("user", new User());
            return "auth/register";
        }

        try {
            // Validate role
            Role userRole;
            try {
                userRole = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", "Invalid role selected. Please choose Student or Instructor.");
                model.addAttribute("user", new User());
                return "auth/register";
            }

            userService.createUser(username, email, password, userRole);
            model.addAttribute("success", "Registration successful! Please login.");
            return "auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", new User());
            return "auth/register";
        }
    }

    @Operation(summary = "Login user", description = "Authenticate user with username and password")
    @PostMapping("/login")
    public String loginUser(@Parameter(description = "Username", required = true)
                            @RequestParam String username,
                            @Parameter(description = "Password", required = true)
                            @RequestParam String password,
                            Model model) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details to determine role-based redirect
            User user = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Redirect based on user role
            return switch (user.getRole()) {
                case ADMIN -> "redirect:/admin";
                case INSTRUCTOR -> "redirect:/instructor";
                case STUDENT -> "redirect:/student";
            };

        } catch (Exception e) {
            model.addAttribute("error", "Invalid username or password");
            return "auth/login";
        }
    }

    @Operation(summary = "Logout user", description = "Log out the current user")
    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/auth/login?logout=true";
    }

    @Operation(summary = "Show admin registration form", description = "Display admin registration page (requires secret key)")
    @GetMapping("/register/admin")
    public String showAdminRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register-admin";
    }

    @Operation(summary = "Register admin user", description = "Register a new admin user (requires secret key)")
    @PostMapping("/register/admin")
    public String registerAdminUser(@Parameter(description = "Username", required = true)
                                    @RequestParam String username,
                                    @Parameter(description = "Email", required = true)
                                    @RequestParam String email,
                                    @Parameter(description = "Password", required = true)
                                    @RequestParam String password,
                                    @Parameter(description = "Admin secret key", required = true)
                                    @RequestParam String secretKey,
                                    Model model) {

        // Check secret key
        if (!"ADMIN_SECRET_123".equals(secretKey)) {
            model.addAttribute("error", "Invalid secret key");
            model.addAttribute("user", new User());
            return "auth/register-admin";
        }

        try {
            userService.createUser(username, email, password, Role.ADMIN);
            model.addAttribute("success", "Admin registration successful! Please login.");
            return "auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", new User());
            return "auth/register-admin";
        }
    }
}