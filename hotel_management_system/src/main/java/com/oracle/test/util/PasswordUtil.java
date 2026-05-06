package com.oracle.test.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtil {

    private static final String SALT = "hotel";

    private PasswordUtil() {}

    public static String encrypt(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((raw + SALT).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    public static boolean matches(String raw, String encrypted) {
        if (raw == null || encrypted == null) return false;
        return encrypt(raw).equalsIgnoreCase(encrypted);
    }

    public static boolean isStrong(String pwd) {
        if (pwd == null || pwd.length() < 6) return false;
        boolean digit = false, letter = false;
        for (char c : pwd.toCharArray()) {
            if (Character.isDigit(c)) digit = true;
            else if (Character.isLetter(c)) letter = true;
        }
        return digit && letter;
    }
}
