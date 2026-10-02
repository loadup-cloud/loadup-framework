package io.github.loadup.modules.dictionary;

import java.util.List;

/** Bounded dictionary administration result. */
public record DictionaryPage<T>(List<T> records, long total, int page, int size) {}
