package io.github.loadup.components.dfs;

/** Raised when the selected storage backend cannot complete an operation. */
public class DfsStorageException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DfsStorageException(String message) {
        super(message);
    }

    public DfsStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
