package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.repository.AppPaymentRepository;
import com.navi.net_pay_backend.domain.repository.ImportLookupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static com.navi.net_pay_backend.application.model.ImportValues.*;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class UserPaymentImportHandler {
    private final AppUserUseCase userUseCase;
    private final AppPaymentUseCase paymentUseCase;
    private final AppPaymentRepository payments;
    private final ImportLookupRepository lookup;
    private final ImportPartyResolver resolver;

    public String user(Map<String, String> row) {
        String name = required(row, "fullName", 150);
        String email = email(row, "email");
        if (email == null) throw new DomainException("import_email_required");
        email = email.toLowerCase(Locale.ROOT);
        String password = text(row, "password", 72);
        if (password != null && password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new DomainException("import_password_too_long");
        }
        Long role = typology(row, "role", 62L, 60L);
        Long status = typology(row, "status", 49L, 48L);
        lookup.lockUserEmail(email);
        if (!lookup.findUserIds(email, null).isEmpty()) throw new DomainException("import_user_email_already_exists");
        return userUseCase.create(AppUser.builder().fullName(name).email(email)
                .password(password == null ? UUID.randomUUID().toString() : password)
                .tpRole(AdmTypology.builder().internalId(role).build())
                .tpStatus(AdmTypology.builder().internalId(status).build()).build()).getHashId();
    }

    public String payment(Map<String, String> row, boolean dryRun) {
        String key = required(row, "importKey", 150);
        if (payments.existsByImportKey(key)) throw new DomainException("import_payment_already_imported");
        BigDecimal amount = decimal(row, "amount", 2, new BigDecimal("0.01"), new BigDecimal("9999999999.99"));
        if (amount == null) throw new DomainException("import_amount_required");
        LocalDate paidAt = date(row, "paidAt");
        if (paidAt == null) throw new DomainException("import_paidAt_required");
        Long method = typology(row, "method", null, 80L);
        if (method == null) throw new DomainException("import_method_required");
        Long status = typology(row, "status", 91L, 90L);
        var client = resolver.client(row);
        var collector = resolver.collector(row);
        lookup.lockClient(client.getId());
        return paymentUseCase.createForImport(AppPayment.builder().importKey(key)
                .client(client).collector(collector).amount(amount).paidAt(paidAt)
                .tpPaymentMethod(AdmTypology.builder().internalId(method).build())
                .tpPaymentStatus(AdmTypology.builder().internalId(status).build())
                .reference(text(row, "reference", 255)).comment(text(row, "comment", 500)).build(), dryRun).getHashId();
    }
}
