package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.dto.ReportTable;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.service.ReportTableXlsExporter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class XlsReportTableExporter implements ReportTableXlsExporter {
    @Override
    public byte[] export(ReportTable table) {
        try (HSSFWorkbook workbook = new HSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
            // XLS allows 65,536 rows per sheet. Split large reports without losing records.
            int index = 0;
            int sheetNumber = 0;
            do {
                Sheet sheet = workbook.createSheet("Reporte " + ++sheetNumber);
                sheet.createRow(0).createCell(0).setCellValue(table.getTitle());
                sheet.createRow(1).createCell(0).setCellValue(table.getSubtitle());
                Row headers = sheet.createRow(3);
                for (int column = 0; column < table.getHeaders().size(); column++) {
                    Cell cell = headers.createCell(column);
                    cell.setCellValue(table.getHeaders().get(column));
                    cell.setCellStyle(headerStyle);
                    sheet.setColumnWidth(column, 24 * 256);
                }
                for (int rowNumber = 4; rowNumber < 65536 && index < table.getRows().size(); rowNumber++) {
                    List<String> values = table.getRows().get(index++);
                    Row row = sheet.createRow(rowNumber);
                    for (int column = 0; column < values.size(); column++) {
                        Cell cell = row.createCell(column);
                        String value = values.get(column);
                        if (table.getRightAlignedColumns() != null && table.getRightAlignedColumns().contains(column)
                                && value != null && value.matches("-?\\d+(\\.\\d+)?")) {
                            cell.setCellValue(Double.parseDouble(value));
                            if (value.contains(".")) cell.setCellStyle(numberStyle);
                        } else {
                            // Explicit string cells keep user content from becoming spreadsheet formulas.
                            cell.setCellValue(value);
                        }
                    }
                }
                sheet.createFreezePane(0, 4);
            } while (index < table.getRows().size());
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DomainException("report_export_failed");
        }
    }
}
