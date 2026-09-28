package com.finvantage.service;

import com.finvantage.config.DatabaseConfig;
import com.finvantage.dao.UserDAO;
import com.finvantage.model.User;
import com.finvantage.util.PasswordUtil;
import com.finvantage.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles authentication, user registration, and active session state.
 */
public class AuthenticationService {

    private static final AuthenticationService INSTANCE = new AuthenticationService();
    private final UserDAO userDAO = new UserDAO();
    private User currentUser;

    private AuthenticationService() {}

    public static AuthenticationService getInstance() {
        return INSTANCE;
    }

    /**
     * Authenticates a user against Oracle Database or Mock storage.
     */
    public boolean login(String email, String plainPassword) throws Exception {
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        if (DatabaseConfig.getInstance().isMockMode()) {
            User mockUser = MockDatabaseService.getInstance().getMockUser();
            if (email.equalsIgnoreCase(mockUser.getEmail()) && 
                PasswordUtil.verifyPassword(plainPassword, mockUser.getPasswordHash())) {
                this.currentUser = mockUser;
                return true;
            }
            return false;
        }

        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
                this.currentUser = user;
                return true;
            }
        }
        return false;
    }

    /**
     * Registers a new user with hashed password.
     */
    public User register(String fullName, String email, String plainPassword, String phone) throws Exception {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Valid email is required.");
        }
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        if (DatabaseConfig.getInstance().isMockMode()) {
            User user = new User(2L, fullName, email, PasswordUtil.hashPassword(plainPassword), User.Role.USER);
            user.setPhoneNumber(phone);
            this.currentUser = user;
            return user;
        }

        if (userDAO.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User newUser = new User(null, fullName, email, PasswordUtil.hashPassword(plainPassword), User.Role.USER);
        newUser.setPhoneNumber(phone);
        User saved = userDAO.save(newUser);
        this.currentUser = saved;
        return saved;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isAuthenticated() {
        return this.currentUser != null;
    }
}
