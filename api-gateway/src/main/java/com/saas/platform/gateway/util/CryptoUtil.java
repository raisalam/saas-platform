package com.saas.platform.gateway.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class CryptoUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // AES-256-CBC key (derived from B64 string)
    private static final byte[] KEY = deriveBytesFromInput(
            "YDbRebrqitw9Sf7uzhlxJDO7F9lUjBoqO1I//VWaXb0=", 32
    );

    // AES-256-GCM key (APK Hash decryption)
    private static final byte[] KEY_APK_HASH =
            "4fG7kL9vX2qR8bT1yZ6mP0sN3aD5hJ2w".getBytes(StandardCharsets.UTF_8);

    private CryptoUtil() {} // prevent instantiation

    // =============================================================
    // Key Derivation (STATIC)
    // =============================================================
    private static byte[] deriveBytesFromInput(String input, int targetLength) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[targetLength];
            System.arraycopy(hash, 0, out, 0, targetLength);
            return out;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =============================================================
    // AES-256-CBC ENCRYPT STRING
    // =============================================================
    public static String encryptString(String plainText, String androidId) {
        try {
            byte[] iv = deriveBytesFromInput(androidId, 16);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"), new IvParameterSpec(iv));

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    public static String encryptObject(Object data, String androidId) {
        try {
            String json = MAPPER.writeValueAsString(data);
            return encryptString(json, androidId);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    // =============================================================
    // AES-256-CBC DECRYPT STRING
    // =============================================================
    public static String decryptString(String encryptedB64, String androidId) {
        try {
            String sanitized = encryptedB64.trim();

            if (!sanitized.matches("^[A-Za-z0-9+/=]+$")) {
                throw new IllegalArgumentException("Invalid Base64 format");
            }

            byte[] iv = deriveBytesFromInput(androidId, 16);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"), new IvParameterSpec(iv));

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(sanitized));

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }

    public static JsonNode decryptObject(String encryptedB64, String androidId) {
        try {
            String json = decryptString(encryptedB64, androidId);
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("DecryptObject failed: " + e.getMessage(), e);
        }
    }

    public static String decryptObjectAsString(String encryptedB64, String androidId) {
        try {
            return decryptString(encryptedB64, androidId);  // ← this already returns the plain JSON string
        } catch (Exception e) {
            throw new RuntimeException("DecryptObject failed: " + e.getMessage(), e);
        }
    }

    // =============================================================
    // AES-256-GCM DECRYPT APK HASH
    // =============================================================
    public static String decryptApkHash(String payload) {
        try {
            String[] parts = payload.split(":");
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encryptedWithTag = Base64.getDecoder().decode(parts[1]);

            int tagLen = 16;
            byte[] tag = new byte[tagLen];
            byte[] ciphertext = new byte[encryptedWithTag.length - tagLen];

            // split: ciphertext | tag
            System.arraycopy(encryptedWithTag, encryptedWithTag.length - tagLen, tag, 0, tagLen);
            System.arraycopy(encryptedWithTag, 0, ciphertext, 0, encryptedWithTag.length - tagLen);

            // Combine ciphertext + tag for Java GCM
            byte[] input = new byte[ciphertext.length + tagLen];
            System.arraycopy(ciphertext, 0, input, 0, ciphertext.length);
            System.arraycopy(tag, 0, input, ciphertext.length, tagLen);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(tagLen * 8, iv);

            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY_APK_HASH, "AES"), spec);

            byte[] decrypted = cipher.doFinal(input);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("APK Hash decryption failed: " + e.getMessage(), e);
        }
    }
}
