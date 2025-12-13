package com.saas.platform.gateway.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class XorDecryptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String KEY1 = "xJ7fA2LwP!9mL#uZqT^hR5vYbK0cG8eD";
    private static final String KEY2 = "N4r*Qz1!Xv@oHc9%Wj^M8y&EaPuT5gB2";
    public static JsonNode xorDecrypt(String base64Str) throws Exception {
        String jsonString = xorDecryptAsString(base64Str); // reuse fast path
        return RequestDecrypter.MAPPER.readTree(jsonString); // only here if someone still needs JsonNode
    }

    // NEW — 5–10× faster, returns plain JSON string
    public static String xorDecryptAsString(String base64Str) throws Exception {
        if (base64Str == null || base64Str.isBlank()) {
            throw new IllegalArgumentException("Empty metadata");
        }

        try {
            // Step 1: Base64 decode
            byte[] encryptedBytes = Base64.getDecoder().decode(base64Str);

            // Step 2: XOR with key2 (reverse order)
            byte[] key2Bytes = KEY2.getBytes(StandardCharsets.UTF_8);
            byte[] step1 = new byte[encryptedBytes.length];
            for (int i = 0; i < encryptedBytes.length; i++) {
                step1[i] = (byte) (encryptedBytes[i] ^ key2Bytes[i % key2Bytes.length]);
            }

            // Step 3: XOR with key1
            byte[] key1Bytes = KEY1.getBytes(StandardCharsets.UTF_8);
            byte[] decryptedBytes = new byte[step1.length];
            for (int i = 0; i < step1.length; i++) {
                decryptedBytes[i] = (byte) (step1[i] ^ key1Bytes[i % key1Bytes.length]);
            }

            // Step 4: Return as String — NO JSON PARSING!
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new Exception("XOR metadata decryption failed", e);
        }
    }
}
