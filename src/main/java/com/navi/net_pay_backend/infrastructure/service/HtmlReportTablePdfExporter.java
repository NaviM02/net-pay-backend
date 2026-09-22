package com.navi.net_pay_backend.infrastructure.service;

import com.lowagie.text.pdf.BaseFont;
import com.navi.net_pay_backend.domain.dto.ReportTable;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.service.ReportTablePdfExporter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HtmlReportTablePdfExporter implements ReportTablePdfExporter {

    private static final String TEMPLATE = "reports/report-table.html";
    private static final DateTimeFormatter GENERATED_AT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] export(ReportTable table) {
        try {
            String html = loadTemplate()
                    .replace("{{title}}", esc(table.getTitle()))
                    .replace("{{subtitle}}", esc(table.getSubtitle()))
                    .replace("{{generatedAt}}", esc(LocalDateTime.now().format(GENERATED_AT)))
                    .replace("{{table}}", buildTable(table));

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                ITextRenderer renderer = new ITextRenderer();
                renderer.getFontResolver().addFont(
                        new ClassPathResource("fonts/Carlito-Regular.ttf").getURL().toString(),
                        BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                renderer.getFontResolver().addFont(
                        new ClassPathResource("fonts/Carlito-Bold.ttf").getURL().toString(),
                        BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                renderer.setDocumentFromString(html);
                renderer.layout();
                renderer.createPDF(os);
                return os.toByteArray();
            }
        } catch (Exception ex) {
            throw new DomainException("report_pdf_generation_failed");
        }
    }

    private String loadTemplate() throws Exception {
        try (InputStream is = new ClassPathResource(TEMPLATE).getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }

    private String buildTable(ReportTable table) {
        List<String> headers = table.getHeaders() != null ? table.getHeaders() : List.of();
        List<List<String>> rows = table.getRows() != null ? table.getRows() : List.of();
        List<Integer> right = table.getRightAlignedColumns() != null
                ? table.getRightAlignedColumns()
                : List.of();

        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"report\"><thead><tr>");
        for (int i = 0; i < headers.size(); i++) {
            sb.append("<th").append(right.contains(i) ? " class=\"right\"" : "").append(">")
                    .append(esc(headers.get(i)))
                    .append("</th>");
        }
        sb.append("</tr></thead><tbody>");

        if (rows.isEmpty()) {
            sb.append("<tr><td class=\"empty\" colspan=\"").append(Math.max(headers.size(), 1))
                    .append("\">Sin datos</td></tr>");
        } else {
            for (List<String> row : rows) {
                sb.append("<tr>");
                for (int i = 0; i < row.size(); i++) {
                    sb.append("<td").append(right.contains(i) ? " class=\"right\"" : "").append(">")
                            .append(esc(row.get(i)))
                            .append("</td>");
                }
                sb.append("</tr>");
            }
        }

        sb.append("</tbody></table>");
        return sb.toString();
    }

    private String esc(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
