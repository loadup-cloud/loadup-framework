package io.github.loadup.modules.transfer;

import java.io.InputStream;
import java.io.OutputStream;

/** Business-supplied handler; implementations must tolerate replay of the same task ID. */
public interface TransferHandler {
    String key();
    TransferKind kind();
    String outputFilename();
    String outputContentType();
    void process(TransferContext context, InputStream input, OutputStream output) throws Exception;
}
