package io.github.loadup.commons.util;

/*-
 * #%L
 * Loadup Common Utils
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * I/O utility providing stream-to-file copying and file handling.
 *
 * <p>Minimal zero-dependency alternative to Apache Commons IO.
 */
public final class IOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final int EOF = -1;

    private IOUtils() {}

    public static void copyToFile(InputStream inputStream, File file) throws IOException {
        try (OutputStream out = openOutputStream(file)) {
            copy(inputStream, out);
        }
    }

    public static FileOutputStream openOutputStream(File file) throws IOException {
        return openOutputStream(file, false);
    }

    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        Objects.requireNonNull(file, "file");
        if (file.exists()) {
            if (!file.isFile()) {
                throw new IllegalArgumentException("Not a file: " + file);
            }
            if (!file.canWrite()) {
                throw new IllegalArgumentException("File not writable: " + file);
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.mkdirs() && !parent.isDirectory()) {
                throw new IOException("Cannot create directory: " + parent);
            }
        }
        return new FileOutputStream(file, append);
    }

    public static long copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        return copy(inputStream, outputStream, DEFAULT_BUFFER_SIZE);
    }

    public static long copy(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long count = 0;
        int n;
        while (EOF != (n = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
