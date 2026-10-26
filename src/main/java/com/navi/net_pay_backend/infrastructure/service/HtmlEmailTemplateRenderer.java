package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.service.EmailTemplateRenderer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Year;

@Service
public class HtmlEmailTemplateRenderer implements EmailTemplateRenderer {

    private static final String BASE_TEMPLATE = "emails/base.html";
    private static final String APP_NAME = "GRAVITY";

    @Override
    public String render(String title, String contentHtml) {
        try {
            String template = load(BASE_TEMPLATE);
            return template
                    .replace("{{appName}}", APP_NAME)
                    .replace("{{title}}", esc(title))
                    .replace("{{content}}", contentHtml != null ? contentHtml : "")
                    .replace("{{currentYear}}", String.valueOf(Year.now().getValue()));
        } catch (Exception ex) {
            throw new DomainException("email_template_render_failed");
        }
    }

    @Override
    public String renderPasswordReset(String fullName, String resetUrl, int expiryMinutes) {
        String content = """
                <p style="margin:0 0 16px 0;font-size:15px;color:#1f2937;">Hola %s,</p>
                <p style="margin:0 0 16px 0;font-size:14px;line-height:1.6;color:#4b5563;">
                    Recibimos una solicitud para restablecer la contraseña de tu cuenta en %s.
                    Hacé clic en el botón para crear una nueva contraseña.
                </p>
                <p style="text-align:center;margin:32px 0;">
                    <a href="%s" style="display:inline-block;background:#4f46e5;color:#ffffff;
                       text-decoration:none;font-weight:bold;font-size:14px;padding:14px 28px;
                       border-radius:8px;">Restablecer contraseña</a>
                </p>
                <p style="margin:0 0 8px 0;font-size:13px;line-height:1.6;color:#6b7280;">
                    Si el botón no redirige, copiá y pegá el siguiente enlace en tu navegador:
                </p>
                <p style="margin:0 0 16px 0;font-size:12px;line-height:1.6;word-break:break-all;">
                    <a href="%s" style="color:#4f46e5;">%s</a>
                </p>
                <p style="margin:0 0 16px 0;font-size:13px;line-height:1.6;color:#6b7280;">
                    El enlace vence en %d minutos y solo puede usarse una vez.
                </p>
                <p style="margin:0;font-size:13px;line-height:1.6;color:#9ca3af;">
                    Si no solicitaste este cambio, ignorá este correo; tu contraseña seguirá igual.
                </p>
                """.formatted(esc(fullName), APP_NAME, esc(resetUrl), esc(resetUrl), esc(resetUrl), expiryMinutes);

        return render("Recuperación de contraseña", content);
    }

    private String load(String path) throws Exception {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }

    private String esc(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
