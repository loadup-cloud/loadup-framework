package io.github.loadup.components.database.id;

import java.security.SecureRandom;
import java.util.UUID;

/** Generates RFC 9562 version 7 UUID identifiers. */
public final class UuidV7IdGenerator implements IdGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final boolean withHyphens;

    public UuidV7IdGenerator(boolean withHyphens) {
        this.withHyphens = withHyphens;
    }

    @Override
    public String generate() {
        long timestamp = System.currentTimeMillis();
        long mostSignificantBits = (timestamp << 16) | 0x7000L | RANDOM.nextInt(1 << 12);
        long leastSignificantBits = RANDOM.nextLong() & 0x3FFF_FFFF_FFFF_FFFFL;
        leastSignificantBits |= 0x8000_0000_0000_0000L;
        String value = new UUID(mostSignificantBits, leastSignificantBits).toString();
        return withHyphens ? value : value.replace("-", "");
    }
}
