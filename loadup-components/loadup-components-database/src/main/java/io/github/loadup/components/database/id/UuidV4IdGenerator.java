package io.github.loadup.components.database.id;

import java.util.UUID;

/** Generates RFC 4122 version 4 UUID identifiers. */
public final class UuidV4IdGenerator implements IdGenerator {
    private final boolean withHyphens;

    public UuidV4IdGenerator(boolean withHyphens) {
        this.withHyphens = withHyphens;
    }

    @Override
    public String generate() {
        String value = UUID.randomUUID().toString();
        return withHyphens ? value : value.replace("-", "");
    }
}
