package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.application.model.ImportResult;
import com.navi.net_pay_backend.application.model.ImportRow;
import com.navi.net_pay_backend.domain.entity.*;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.repository.AppServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static com.navi.net_pay_backend.application.model.ImportValues.*;

@Service
@RequiredArgsConstructor
public class DataImportUseCase {
    private final AppServiceUseCase services;
    private final AppClientUseCase clients;
    private final AppServiceRepository serviceRepository;
    private final ImportPartyResolver partyResolver;
    private final UserPaymentImportHandler userPaymentHandler;
    private final PlatformTransactionManager transactionManager;

    private static final Set<String> SERVICE_COLUMNS = Set.of("name", "speed", "monthlyPrice", "type", "status");
    private static final Set<String> CLIENT_COLUMNS = Set.of("businessName", "contactName", "contactEmail",
            "contactPhone", "address", "latitude", "longitude", "customerSince", "serviceName", "collectorHashId",
            "collectorName", "collectorEmail", "status");
    private static final Set<String> USER_COLUMNS = Set.of("fullName", "email", "password", "role", "status");
    private static final Set<String> PAYMENT_COLUMNS = Set.of("importKey", "clientName", "clientEmail", "clientPhone",
            "collectorName", "collectorEmail", "amount", "paidAt", "method", "status", "reference", "comment");

    public ImportResult execute(String resource, List<ImportRow> rows, boolean dryRun) {
        Set<String> allowed = switch (resource) {
            case "services" -> SERVICE_COLUMNS;
            case "clients" -> CLIENT_COLUMNS;
            case "users" -> USER_COLUMNS;
            case "payments" -> PAYMENT_COLUMNS;
            default -> throw new DomainException("import_unknown_resource");
        };
        if (rows.isEmpty()) throw new DomainException("import_empty_file");
        if (rows.stream().anyMatch(row -> !allowed.containsAll(row.values().keySet()))) {
            throw new DomainException("import_unknown_columns");
        }

        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        List<ImportResult.RowResult> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int successful = 0;
        for (ImportRow row : rows) {
            try {
                String key = switch (resource) {
                    case "services" -> required(row.values(), "name", 150).toLowerCase(Locale.ROOT);
                    case "users" -> required(row.values(), "email", 150).toLowerCase(Locale.ROOT);
                    case "payments" -> required(row.values(), "importKey", 150);
                    default -> null;
                };
                if (key != null && seen.contains(key)) {
                    throw new DomainException(switch (resource) {
                        case "users" -> "import_duplicate_user_in_file";
                        case "payments" -> "import_duplicate_payment_in_file";
                        default -> "import_duplicate_service_in_file";
                    });
                }
                String hashId = transaction.execute(tx -> {
                    String saved = switch (resource) {
                        case "services" -> service(row.values());
                        case "clients" -> client(row.values());
                        case "users" -> userPaymentHandler.user(row.values());
                        case "payments" -> userPaymentHandler.payment(row.values(), dryRun);
                        default -> throw new DomainException("import_unknown_resource");
                    };
                    if (dryRun) tx.setRollbackOnly();
                    return saved;
                });
                if (key != null) seen.add(key);
                results.add(new ImportResult.RowResult(row.row(), dryRun ? "VALID" : "CREATED", dryRun ? null : hashId, null));
                successful++;
            } catch (DomainException ex) {
                results.add(new ImportResult.RowResult(row.row(), "ERROR", null, ex.getMessage()));
            } catch (DataIntegrityViolationException ex) {
                results.add(new ImportResult.RowResult(row.row(), "ERROR", null, "import_constraint_violation"));
            }
        }
        return new ImportResult(dryRun, rows.size(), successful, rows.size() - successful, results);
    }

    private String service(Map<String, String> row) {
        BigDecimal price = decimal(row, "monthlyPrice", 2, new BigDecimal("0"), new BigDecimal("99999999.99"));
        if (price == null) throw new DomainException("import_monthlyPrice_required");
        Long type = typology(row, "type", 71L, 70L);
        return services.create(AppService.builder()
                .name(required(row, "name", 150)).speed(required(row, "speed", 50))
                .monthlyPrice(price).tpType(AdmTypology.builder().internalId(type).build())
                .tpStatus(status(row)).build()).getHashId();
    }

    private String client(Map<String, String> row) {
        AppService plan = serviceRepository.findByNameNotDeleted(required(row, "serviceName", 150))
                .orElseThrow(() -> new DomainException("import_service_not_found"));
        AppUser collector = partyResolver.collector(row);
        LocalDate since = date(row, "customerSince");
        String email = email(row, "contactEmail");
        return clients.create(AppClient.builder()
                .businessName(required(row, "businessName", 150)).contactName(required(row, "contactName", 150))
                .contactEmail(email).contactPhone(text(row, "contactPhone", 30)).address(text(row, "address", 255))
                .latitude(decimal(row, "latitude", 7, new BigDecimal("-90"), new BigDecimal("90")))
                .longitude(decimal(row, "longitude", 7, new BigDecimal("-180"), new BigDecimal("180")))
                .customerSince(since).service(plan).collector(collector).tpStatus(status(row)).build()).getHashId();
    }

    private AdmTypology status(Map<String, String> row) {
        return AdmTypology.builder().internalId(typology(row, "status", 49L, 48L)).build();
    }

}
