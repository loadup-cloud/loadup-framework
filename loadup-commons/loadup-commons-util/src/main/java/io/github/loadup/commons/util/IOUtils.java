package io.github.loadup.commons.util;

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
