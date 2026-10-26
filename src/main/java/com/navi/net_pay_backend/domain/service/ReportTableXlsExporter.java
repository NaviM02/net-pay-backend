package com.navi.net_pay_backend.domain.service;

import com.navi.net_pay_backend.domain.dto.ReportTable;

public interface ReportTableXlsExporter {
    byte[] export(ReportTable table);
}
