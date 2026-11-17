package com.library.service;

import com.library.dao.AdminDAO;
import com.library.model.Admin;
import com.library.util.ValidationUtil;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public class AuthService {
    private static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private final AdminDAO adminDAO;
    private final SecureRandom random = new SecureRandom();

    public AuthService() { this(new AdminDAO()); }
    public AuthService(AdminDAO adminDAO) { this.adminDAO = adminDAO; }

    public boolean hasAdministrator() throws Exception { return adminDAO.count() > 0; }

    public Admin authenticate(String username, char[] password) throws Exception {
        String normalized = ValidationUtil.required(username, "Username");
        String stored = adminDAO.passwordHash(normalized).orElse(null);
        if (stored == null || password == null || !verify(password, stored)) return null;
        return adminDAO.find(normalized);
    }

    public void createFirstAdministrator(String username, char[] password, char[] confirmation) throws Exception {
        String normalized = ValidationUtil.required(username, "Username");
        if (normalized.length() < 3 || normalized.length() > 80) throw new IllegalArgumentException("Username must be between 3 and 80 characters.");
        if (password == null || password.length < 10) throw new IllegalArgumentException("Password must contain at least 10 characters.");
        if (!constantTimeEquals(password, confirmation)) throw new IllegalArgumentException("Passwords do not match.");
        adminDAO.createFirstAdmin(normalized, hash(password));
    }

    private String hash(char[] password) throws GeneralSecurityException {
        byte[] salt = new byte[SALT_BYTES]; random.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return "PBKDF2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(derived);
    }

    private boolean verify(char[] password, String encoded) throws GeneralSecurityException {
        String[] parts = encoded.split("\\$", -1);
        if (parts.length != 4 || !"PBKDF2".equals(parts[0])) return false;
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return java.security.MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException exception) { return false; }
    }

    private byte[] derive(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); }
    }

    private boolean constantTimeEquals(char[] first, char[] second) {
        if (first == null || second == null) return false;
        int difference = first.length ^ second.length;
        int length = Math.max(first.length, second.length);
        for (int index = 0; index < length; index++) {
            char left = index < first.length ? first[index] : 0;
            char right = index < second.length ? second[index] : 0;
            difference |= left ^ right;
        }
        return difference == 0;
    }
}
