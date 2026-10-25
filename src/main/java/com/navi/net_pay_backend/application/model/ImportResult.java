package com.navi.net_pay_backend.application.model;

import java.util.List;

public record ImportResult(boolean dryRun, int total, int successful, int failed, List<RowResult> rows) {
    public record RowResult(int row, String status, String hashId, String error) {}
}
