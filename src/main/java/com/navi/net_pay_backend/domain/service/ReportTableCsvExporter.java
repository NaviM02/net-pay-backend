package com.navi.net_pay_backend.domain.service;

import com.navi.net_pay_backend.domain.dto.ReportTable;

public interface ReportTableCsvExporter {
    byte[] export(ReportTable table);
}
