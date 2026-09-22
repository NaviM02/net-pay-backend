package com.navi.net_pay_backend.domain.service;

import com.navi.net_pay_backend.domain.dto.EmailMessage;

public interface EmailSender {
    void send(EmailMessage message);
}
