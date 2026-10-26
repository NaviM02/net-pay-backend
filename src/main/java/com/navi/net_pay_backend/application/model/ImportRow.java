package com.navi.net_pay_backend.application.model;

import java.util.Map;

public record ImportRow(int row, Map<String, String> values) {
}
