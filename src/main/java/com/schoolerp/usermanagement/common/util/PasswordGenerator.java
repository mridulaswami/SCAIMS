package com.schoolerp.usermanagement.common.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+";

    private static final String ALL_CHARS = UPPER + LOWER + NUMBERS + SPECIAL;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordGenerator() {
        // Utility class
    }

    public static String generateRandomPassword() {

        int length = 16;

        List<Character> characters = new ArrayList<>(length);

        // At least one uppercase
        characters.add(UPPER.charAt(SECURE_RANDOM.nextInt(UPPER.length())));

        // At least one lowercase
        characters.add(LOWER.charAt(SECURE_RANDOM.nextInt(LOWER.length())));

        // At least one number
        characters.add(NUMBERS.charAt(SECURE_RANDOM.nextInt(NUMBERS.length())));

        // At least one special character
        characters.add(SPECIAL.charAt(SECURE_RANDOM.nextInt(SPECIAL.length())));

        // Remaining characters
        for (int i = 4; i < length; i++) {
            characters.add(ALL_CHARS.charAt(SECURE_RANDOM.nextInt(ALL_CHARS.length())));
        }

        // Secure shuffle
        Collections.shuffle(characters, SECURE_RANDOM);

        StringBuilder password = new StringBuilder(length);

        for (Character character : characters) {
            password.append(character);
        }

        return password.toString();
    }
}