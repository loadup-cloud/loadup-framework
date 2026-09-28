package io.github.loadup.components.dfs;

/** Raised when a requested file identifier does not exist. */
public class DfsObjectNotFoundException extends DfsStorageException {
    private static final long serialVersionUID = 1L;

    public DfsObjectNotFoundException(String fileId) {
        super("DFS object not found: " + fileId);
    }
}
