package io.github.loadup.modules.transfer;

import java.util.List;

public record TransferPage<T>(List<T> records, long total, int page, int size) {}
