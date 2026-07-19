package com.adi.naukri.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Component
public class ReportWriter {

    private static final String CSV_HEADER =
        "email,status,error,resumeOldName,resumeNewName,startedAt,endedAt,retries";
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public void write(Path runDir, List<AccountResult> results) throws IOException {
        Files.createDirectories(runDir);
        Files.createDirectories(runDir.resolve("logs"));
        writeCsv (runDir.resolve("report.csv"),  results);
        writeJson(runDir.resolve("report.json"), results);
        for (AccountResult r : results) writeLog(runDir.resolve("logs").resolve(r.email() + ".log"), r);
    }

    private void writeCsv(Path out, List<AccountResult> rs) throws IOException {
        StringBuilder sb = new StringBuilder(CSV_HEADER).append('\n');
        for (AccountResult r : rs) {
            sb.append(csv(r.email())).append(',')
              .append(r.status()).append(',')
              .append(csv(r.error())).append(',')
              .append(csv(r.resumeOldName())).append(',')
              .append(csv(r.resumeNewName())).append(',')
              .append(r.startedAt()).append(',')
              .append(r.endedAt()).append(',')
              .append(r.retries()).append('\n');
        }
        Files.writeString(out, sb.toString());
    }

    private void writeJson(Path out, List<AccountResult> rs) throws IOException {
        Files.writeString(out, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(rs));
    }

    private void writeLog(Path out, AccountResult r) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("email:  ").append(r.email()).append('\n')
          .append("status: ").append(r.status()).append('\n')
          .append("error:  ").append(r.error() == null ? "" : r.error()).append('\n')
          .append("start:  ").append(r.startedAt()).append('\n')
          .append("end:    ").append(r.endedAt()).append('\n')
          .append("retries:").append(r.retries()).append('\n')
          .append("steps:\n");
        for (StepTiming t : r.steps()) sb.append("  ").append(t.step()).append(' ').append(t.durationMs()).append("ms\n");
        if (r.dumpPaths() != null && !r.dumpPaths().isEmpty()) {
            sb.append("dumps:\n");
            for (String p : r.dumpPaths()) sb.append("  ").append(p).append('\n');
        }
        Files.writeString(out, sb.toString());
    }

    private static String csv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
}
