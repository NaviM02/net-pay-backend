package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.dto.ReportTable;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportTableExporterTest {

    private final HtmlReportTablePdfExporter pdfExporter = new HtmlReportTablePdfExporter();
    private final CsvReportTableExporter csvExporter = new CsvReportTableExporter();

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
    void csvExporter_writesBomHeadersAndEscapedRows() {
        byte[] csv = csvExporter.export(sampleTable());
        String text = new String(csv, StandardCharsets.UTF_8);

        assertThat(text).startsWith("\uFEFF");
        assertThat(text).contains("Cliente;Contacto;Deuda total");
        assertThat(text).contains("\"Café; Central\";Juan <Pérez>;1500.50");
    }
}
