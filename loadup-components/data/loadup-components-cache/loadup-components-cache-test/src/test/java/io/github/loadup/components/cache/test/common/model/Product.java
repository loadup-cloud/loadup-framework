package io.github.loadup.components.cache.test.common.model;

/** Simple business DTO used to prove that cached values survive JSON (de)serialization. */
public record Product(Long id, String name) {}
