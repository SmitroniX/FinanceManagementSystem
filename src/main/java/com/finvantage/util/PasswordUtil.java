package com.finvantage.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for cryptographic password hashing using SHA-256 with a unique application salt.
 */
public class PasswordUtil {

    private static final String STATIC_PEPPER = "FinVantageSecureSalt@2026_DBMS_OOP";

    /**
     * Hashes a plaintext password using SHA-256 with pepper.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedInput = plainPassword + STATIC_PEPPER;
            byte[] hashBytes = digest.digest(saltedInput.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 cryptographic algorithm unavailable.", e);
        }
    }

    /**
     * Verifies whether a plaintext password matches the stored hash in constant time.
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        String calculatedHash = hashPassword(plainPassword);
        return MessageDigest.isEqual(
            calculatedHash.getBytes(StandardCharsets.UTF_8),
            storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
