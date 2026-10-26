package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.dto.ReportTable;
import org.junit.jupiter.api.Test;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import java.io.ByteArrayInputStream;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportTableExporterTest {

    private final HtmlReportTablePdfExporter pdfExporter = new HtmlReportTablePdfExporter();

    private ReportTable sampleTable() {
        return ReportTable.builder()
                .title("Reporte · Clientes en mora")
                .subtitle("1 cliente(s)")
                .headers(List.of("Cliente", "Contacto", "Deuda total"))
                .rightAlignedColumns(List.of(2))
                .rows(List.of(
                        List.of("Hotel Palma Real", "Mariana Reyes", "7200.00"),
                        List.of("Café; Central", "Juan <Pérez>", "1500.50")))
                .build();
    }

    @Test
    void pdfExporter_rendersNonEmptyPdf() {
        byte[] pdf = pdfExporter.export(sampleTable());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void xlsExporterCreatesNativeWorkbookWithNumericAmountsAndUnicodeText() throws Exception {
        byte[] bytes = new XlsReportTableExporter().export(sampleTable());
        try (HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(3).getCell(2).getStringCellValue()).isEqualTo("Deuda total");
            assertThat(sheet.getRow(4).getCell(2).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(sheet.getRow(4).getCell(2).getNumericCellValue()).isEqualTo(7200.0);
            assertThat(sheet.getRow(5).getCell(0).getStringCellValue()).isEqualTo("Café; Central");
        }
    }

    @Test
    void xlsExporterDoesNotInterpretUserTextAsFormulas() throws Exception {
        var table = ReportTable.builder().title("Reporte").subtitle("").headers(List.of("Cliente"))
                .rows(List.of(List.of("=1+1"))).build();
        try (var workbook = new HSSFWorkbook(new ByteArrayInputStream(new XlsReportTableExporter().export(table)))) {
            assertThat(workbook.getSheetAt(0).getRow(4).getCell(0).getCellType()).isEqualTo(CellType.STRING);
        }
    }
}
