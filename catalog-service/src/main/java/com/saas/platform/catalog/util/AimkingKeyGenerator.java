package com.saas.platform.catalog.util;

import java.security.SecureRandom;

public final class AimkingKeyGenerator {

    private static final String CHARSET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final int RANDOM_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private AimkingKeyGenerator() {
        // Utility class – prevent instantiation
    }

    /**
     * Generates key in format:
     * king_{DD}_key-{RANDOM12}
     *
     * Example:
     * king_30_key-A9sK2LmP0xZq
     */
    public static String generate(int days) {
        String randomPart = randomString(RANDOM_LENGTH);
        return String.format(
                "king_%02d_key-%s",
                days,
                randomPart
        );
    }

    // ---- Helpers ----

    private static int extractDays(String input) {
        String digits = input.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("No digits found in daysStr: " + input);
        }
        return Integer.parseInt(digits);
    }

    private static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }
}
