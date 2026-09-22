package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.dto.ReportTable;
import com.navi.net_pay_backend.domain.service.ReportTableCsvExporter;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CsvReportTableExporter implements ReportTableCsvExporter {

    private static final String BOM = "\uFEFF";
    private static final String DELIMITER = ";";
    private static final String LINE = "\r\n";

    @Override
    public byte[] export(ReportTable table) {
        StringBuilder sb = new StringBuilder(BOM);
        sb.append(join(table.getHeaders())).append(LINE);
        if (table.getRows() != null) {
            for (List<String> row : table.getRows()) {
                sb.append(join(row)).append(LINE);
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String join(List<String> values) {
        if (values == null) {
            return "";
        }
        return values.stream().map(this::escape).collect(Collectors.joining(DELIMITER));
    }

    private String escape(String value) {
        String v = value == null ? "" : value;
        if (v.contains(DELIMITER) || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
