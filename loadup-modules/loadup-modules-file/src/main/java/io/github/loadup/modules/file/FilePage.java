package io.github.loadup.modules.file;

import java.util.List;

public record FilePage<T>(List<T> records, long total, int page, int size) {}
