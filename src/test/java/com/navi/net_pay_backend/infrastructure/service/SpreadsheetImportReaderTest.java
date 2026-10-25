package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.exception.DomainException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SpreadsheetImportReaderTest {
    private final SpreadsheetImportReader reader = new SpreadsheetImportReader();

    @Test
    void readsBomQuotedFieldsMultilineAndNulls() {
        var rows = csv("\uFEFFbusinessName,contactName,address,contactEmail\n"
                + "\"Tienda, Norte\", Ana ,\"Calle 1\nCasa 2\",NULL\n\n");
        assertEquals(1, rows.size());
        assertEquals(2, rows.get(0).row());
        assertEquals("Tienda, Norte", rows.get(0).values().get("businessName"));
        assertEquals("Ana", rows.get(0).values().get("contactName"));
        assertEquals("Calle 1\nCasa 2", rows.get(0).values().get("address"));
        assertNull(rows.get(0).values().get("contactEmail"));
    }

    @Test
    void supportsSemicolonDelimiter() {
        var rows = reader.read(bytes("name;speed\nPlan;10 Mbps"), "sample.csv", ';');
        assertEquals("10 Mbps", rows.get(0).values().get("speed"));
    }

    @Test
    void rejectsAmbiguousHeadersMalformedRowsAndExcessRows() {
        assertThrows(DomainException.class, () -> csv("name,name\nA,B"));
        assertThrows(DomainException.class, () -> csv("name,speed\nA,B,C"));
        assertThrows(DomainException.class, () -> csv("name\n" + "Plan\n".repeat(5001)));
        assertThrows(DomainException.class, () -> reader.read(bytes("anything"), "data.txt", ','));
    }

    @Test
    void readsXlsAndXlsxWithDatesBlankCellsAndActualRowNumbers() throws IOException {
        for (boolean modern : new boolean[]{false, true}) {
            try (Workbook workbook = modern ? new XSSFWorkbook() : new HSSFWorkbook()) {
                var sheet = workbook.createSheet();
                var header = sheet.createRow(0);
                header.createCell(0).setCellValue("businessName");
                header.createCell(1).setCellValue("customerSince");
                header.createCell(2).setCellValue("contactEmail");
                var row = sheet.createRow(3);
                row.createCell(0).setCellValue("Café de prueba");
                var date = row.createCell(1);
                date.setCellValue(LocalDateTime.of(2025, 5, 2, 0, 0));
                var style = workbook.createCellStyle();
                style.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
                date.setCellStyle(style);
                var output = new ByteArrayOutputStream();
                workbook.write(output);
                var rows = reader.read(new ByteArrayInputStream(output.toByteArray()), modern ? "a.xlsx" : "a.xls", ',');
                assertEquals(4, rows.get(0).row());
                assertEquals("2025-05-02", rows.get(0).values().get("customerSince"));
                assertNull(rows.get(0).values().get("contactEmail"));
            }
        }
    }

    @Test
    void syntheticCsvFixturesHaveExpectedSizes() throws IOException {
        for (var entry : java.util.Map.of("services.csv", 10, "clients.csv", 60, "invalid-clients.csv", 6,
                "users.csv", 12, "payments.csv", 20, "invalid-payments.csv", 7).entrySet()) {
            try (var input = com.navi.net_pay_backend.support.ImportFixtures.input(entry.getKey())) {
                assertEquals(entry.getValue(), reader.read(input, entry.getKey(), ',').size());
            }
        }
    }

    private java.util.List<com.navi.net_pay_backend.application.model.ImportRow> csv(String csv) {
        return reader.read(bytes(csv), "sample.csv", ',');
    }

    private InputStream bytes(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
