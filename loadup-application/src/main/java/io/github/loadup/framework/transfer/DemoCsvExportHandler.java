package io.github.loadup.framework.transfer;

import io.github.loadup.modules.transfer.TransferContext;
import io.github.loadup.modules.transfer.TransferHandler;
import io.github.loadup.modules.transfer.TransferKind;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/** Bounded CSV generation example with progress reporting. */
@Component
public class DemoCsvExportHandler implements TransferHandler {
    @Override
    public String key() { return "demo-csv-export"; }
    @Override
    public TransferKind kind() { return TransferKind.EXPORT; }
    @Override
    public String outputFilename() { return "sample-export.csv"; }
    @Override
    public String outputContentType() { return "text/csv"; }

    @Override
    public void process(TransferContext context, InputStream input, OutputStream output) throws Exception {
        int rows = Integer.parseInt(context.options().getOrDefault("rows", "10"));
        if (rows < 1 || rows > 1000) throw new IllegalArgumentException("rows must be between 1 and 1000");
        var writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        writer.write("id,value\n");
        for (int index = 1; index <= rows; index++) {
            writer.write(index + ",sample-" + index + "\n");
            context.report(index, rows);
        }
        writer.flush();
    }
}
