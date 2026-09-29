package daa.metrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvWriter {
    private CsvWriter() {}

    public static void write(Path file, List<Result> rows) throws IOException {
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8))) {
            out.println(Result.HEADER);
            for (Result r : rows) out.println(r.toCsvLine());
        }
    }
}
