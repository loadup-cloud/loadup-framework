package io.github.loadup.components.database.id;

import java.security.SecureRandom;

/** Generates compact random identifiers. */
public final class RandomIdGenerator implements IdGenerator {
    private static final char[] ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final int length;

    public RandomIdGenerator(int length) {
        if (length < 1 || length > 64) {
            throw new IllegalArgumentException("Random ID length must be between 1 and 64");
        }
        this.length = length;
    }

    @Override
    public String generate() {
        char[] result = new char[length];
        for (int index = 0; index < result.length; index++) {
            result[index] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(result);
    }
}
