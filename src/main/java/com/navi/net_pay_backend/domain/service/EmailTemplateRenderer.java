package com.navi.net_pay_backend.domain.service;

public interface EmailTemplateRenderer {

    String render(String title, String contentHtml);

    String renderPasswordReset(String fullName, String resetUrl, int expiryMinutes);
}
