package io.github.loadup.components.database.id;

/** Generates string identifiers for persistent entities. */
@FunctionalInterface
public interface IdGenerator {

    /**
     * Generates a new identifier.
     *
     * @return a non-blank identifier
     */
    String generate();
}
