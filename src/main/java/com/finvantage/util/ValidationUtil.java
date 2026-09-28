package com.finvantage.util;

import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * ValidationUtil handles input verification and unique reference generation.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static String generateAccountNumber(String prefix) {
        int code = 1000 + RANDOM.nextInt(9000);
        return String.format("ACC-%s-%04d", prefix.toUpperCase(), code);
    }

    public static String generateTransactionRef() {
        long timestamp = System.currentTimeMillis() % 1000000;
        int rand = 100 + RANDOM.nextInt(900);
        return String.format("TX-VNT-%d-%d", timestamp, rand);
    }
}
