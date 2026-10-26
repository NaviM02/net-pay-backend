package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.application.model.ImportRow;
import com.navi.net_pay_backend.domain.exception.DomainException;
import org.apache.commons.csv.CSVFormat;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class SpreadsheetImportReader {
    public static final int MAX_ROWS = 5000;

    public List<ImportRow> read(InputStream input, String filename, char delimiter) {
        String name = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        try {
            if (name.endsWith(".csv")) return csv(input, delimiter);
            if (name.endsWith(".xlsx") || name.endsWith(".xls")) return excel(input);
            throw new DomainException("import_unsupported_format");
        } catch (DomainException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            throw new DomainException("import_invalid_file");
        }
    }

    private List<ImportRow> csv(InputStream input, char delimiter) throws IOException {
        if (delimiter != ',' && delimiter != ';' && delimiter != '\t') {
            throw new DomainException("import_invalid_delimiter");
        }
        try (var parser = CSVFormat.DEFAULT.builder().setDelimiter(delimiter)
                .setIgnoreEmptyLines(true).get().parse(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            var iterator = parser.iterator();
            if (!iterator.hasNext()) throw new DomainException("import_empty_file");
            List<String> headers = headers(iterator.next().toList());
            List<ImportRow> rows = new ArrayList<>();
            while (iterator.hasNext()) {
                var record = iterator.next();
                if (record.size() != headers.size()) throw new DomainException("import_column_count_row_" + record.getRecordNumber());
                add(rows, (int) record.getRecordNumber(), headers, record.toList());
            }
            return rows;
        }
    }

    private List<ImportRow> excel(InputStream input) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(input)) {
            if (workbook.getNumberOfSheets() == 0) throw new DomainException("import_empty_file");
            Sheet sheet = workbook.getSheetAt(0);
            Row first = sheet.getRow(0);
            if (first == null || first.getLastCellNum() < 1) throw new DomainException("import_header_required");
            if (first.getLastCellNum() > 30) throw new DomainException("import_too_many_columns");
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            List<String> rawHeaders = new ArrayList<>();
            for (int i = 0; i < first.getLastCellNum(); i++) rawHeaders.add(cell(first.getCell(i), formatter));
            List<String> headers = headers(rawHeaders);
            List<ImportRow> rows = new ArrayList<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                if (row.getLastCellNum() > headers.size()) throw new DomainException("import_column_count_row_" + (row.getRowNum() + 1));
                List<String> values = new ArrayList<>();
                for (int i = 0; i < headers.size(); i++) values.add(cell(row.getCell(i), formatter));
                add(rows, row.getRowNum() + 1, headers, values);
            }
            return rows;
        }
    }

    private String cell(Cell cell, DataFormatter formatter) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.FORMULA || cell.getCellType() == CellType.ERROR) {
            throw new DomainException("import_formulas_or_error_cells_not_supported");
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().toString();
        }
        return formatter.formatCellValue(cell);
    }

    private List<String> headers(List<String> raw) {
        if (raw.size() > 30) throw new DomainException("import_too_many_columns");
        List<String> result = raw.stream().map(s -> s.replace("\uFEFF", "").trim()).toList();
        if (result.stream().anyMatch(String::isEmpty) || new HashSet<>(result).size() != result.size()) {
            throw new DomainException("import_invalid_or_duplicate_headers");
        }
        return result;
    }

    private void add(List<ImportRow> rows, int row, List<String> headers, List<String> values) {
        if (values.stream().allMatch(String::isBlank)) return;
        if (rows.size() >= MAX_ROWS) throw new DomainException("import_row_limit_exceeded");
        Map<String, String> data = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String value = values.get(i).trim();
            data.put(headers.get(i), value.isEmpty() || value.equalsIgnoreCase("null") ? null : value);
        }
        rows.add(new ImportRow(row, data));
    }
}
