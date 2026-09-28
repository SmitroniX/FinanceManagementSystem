package com.finvantage.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * User represents an authenticated application user or administrator.
 * Demonstrates OOP Encapsulation with domain validation on email and roles.
 */
public class User extends BaseEntity {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private String fullName;
    private String email;
    private String passwordHash;
    private Role role = Role.USER;
    private String phoneNumber;
    private String status = "ACTIVE";

    public enum Role {
        ADMIN, USER, AUDITOR
    }

    public User() {
        super();
    }

    public User(Long id, String fullName, String email, String passwordHash, Role role) {
        super(id);
        setFullName(fullName);
        setEmail(email);
        this.passwordHash = passwordHash;
        this.role = role != null ? role : Role.USER;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty.");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format provided: " + email);
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role != null ? role : Role.USER;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", status='" + status + '\'' +
                '}';
    }
}
