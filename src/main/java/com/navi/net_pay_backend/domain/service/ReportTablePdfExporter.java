package com.navi.net_pay_backend.domain.service;

import com.navi.net_pay_backend.domain.dto.ReportTable;

public interface ReportTablePdfExporter {
    byte[] export(ReportTable table);
}
