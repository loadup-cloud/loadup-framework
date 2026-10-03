package io.github.loadup.framework.transfer;

import io.github.loadup.modules.transfer.TransferContext;
import io.github.loadup.modules.transfer.TransferHandler;
import io.github.loadup.modules.transfer.TransferKind;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Component;

/** Bounded CSV validation example; real imports implement their own business writes. */
@Component
public class DemoCsvImportHandler implements TransferHandler {
    @Override
    public String key() { return "demo-csv-import"; }
    @Override
    public TransferKind kind() { return TransferKind.IMPORT; }
    @Override
    public String outputFilename() { return "import-report.csv"; }
    @Override
    public String outputContentType() { return "text/csv"; }

    @Override
    public void process(TransferContext context, InputStream input, OutputStream output) throws Exception {
        List<String> lines = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                .lines().limit(1002).toList();
        if (lines.isEmpty() || !"value,label".equals(lines.getFirst()) || lines.size() > 1001) {
            throw new IllegalArgumentException("expected value,label header and at most 1000 rows");
        }
        var writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        writer.write("row,status\n");
        int total = lines.size() - 1;
        for (int index = 1; index < lines.size(); index++) {
            String[] fields = lines.get(index).split(",", -1);
            if (fields.length != 2 || fields[0].isBlank() || fields[1].isBlank()) {
                throw new IllegalArgumentException("invalid row " + index);
            }
            writer.write(index + ",VALID\n");
            context.report(index, total);
        }
        writer.flush();
    }
}
