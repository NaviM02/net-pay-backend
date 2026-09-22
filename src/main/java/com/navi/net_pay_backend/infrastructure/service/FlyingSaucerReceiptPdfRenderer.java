package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.entity.Receipt;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.service.ReceiptPdfRenderer;
import com.lowagie.text.pdf.BaseFont;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class FlyingSaucerReceiptPdfRenderer implements ReceiptPdfRenderer {

    private static final String TEMPLATE = "receipts/receipt.html";
    private static final Locale SPANISH = Locale.forLanguageTag("es-DO");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy, HH:mm", SPANISH);

    @Override
    public byte[] render(Receipt receipt) {
        try {
            String html = loadTemplate();
            html = fill(html, receipt);

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
            throw new DomainException("receipt_pdf_generation_failed");
        }
    }

    private String loadTemplate() throws Exception {
        try (InputStream is = new ClassPathResource(TEMPLATE).getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }

    private String fill(String html, Receipt receipt) {
        NumberFormat money = NumberFormat.getNumberInstance(SPANISH);
        money.setMinimumFractionDigits(2);
        money.setMaximumFractionDigits(2);

        String clientName = receipt.getClient() != null ? receipt.getClient().getBusinessName() : "—";
        String clientContact = receipt.getClient() != null && receipt.getClient().getContactName() != null
                ? receipt.getClient().getContactName() : "—";
        String clientEmail = receipt.getClient() != null && receipt.getClient().getContactEmail() != null
                ? receipt.getClient().getContactEmail() : "—";
        String collectorName = receipt.getCollector() != null ? receipt.getCollector().getFullName() : "Sistema";
        String method = receipt.getPayment() != null
                && receipt.getPayment().getTpPaymentMethod() != null
                && receipt.getPayment().getTpPaymentMethod().getDescription() != null
                ? receipt.getPayment().getTpPaymentMethod().getDescription()
                : "—";
        String period = receipt.getPeriod() != null ? receipt.getPeriod().getLabel() : "—";
        String paidAt = receipt.getPayment() != null && receipt.getPayment().getPaidAt() != null
                ? receipt.getPayment().getPaidAt().toString() : "—";
        String reference = receipt.getPayment() != null && receipt.getPayment().getReference() != null
                ? receipt.getPayment().getReference() : "—";
        String issuedAt = receipt.getIssuedAt() != null ? receipt.getIssuedAt().format(DATE_FMT) : "—";
        String amount = money.format(receipt.getAmount() != null ? receipt.getAmount() : BigDecimal.ZERO);

        return html
                .replace("{{receiptNumber}}", esc(receipt.getReceiptNumber()))
                .replace("{{issuedAt}}", esc(issuedAt))
                .replace("{{clientName}}", esc(clientName))
                .replace("{{clientContact}}", esc(clientContact))
                .replace("{{clientEmail}}", esc(clientEmail))
                .replace("{{collectorName}}", esc(collectorName))
                .replace("{{method}}", esc(method))
                .replace("{{period}}", esc(period))
                .replace("{{paidAt}}", esc(paidAt))
                .replace("{{reference}}", esc(reference))
                .replace("{{amount}}", esc(amount));
    }

    private String esc(String value) {
        if (value == null) {
            return "—";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
