package com.navi.net_pay_backend.domain.service;

import com.navi.net_pay_backend.domain.entity.Receipt;

public interface ReceiptPdfRenderer {
    byte[] render(Receipt receipt);
}
