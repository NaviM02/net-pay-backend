package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.repository.ImportLookupRepository;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.navi.net_pay_backend.application.model.ImportValues.*;

@Service
@RequiredArgsConstructor
public class ImportPartyResolver {
    private final ImportLookupRepository lookup;
    private final AppUserRepository users;

    public AppClient client(Map<String, String> row) {
        String name = text(row, "clientName", 150);
        String email = email(row, "clientEmail");
        String phone = text(row, "clientPhone", 30);
        if (name == null && email == null && phone == null) throw new DomainException("import_client_reference_required");
        return AppClient.builder().id(unique(lookup.findClientIds(name, email, phone), "client")).build();
    }

    public AppUser collector(Map<String, String> row) {
        String hashId = text(row, "collectorHashId", 50);
        String email = email(row, "collectorEmail");
        String name = text(row, "collectorName", 150);
        if (hashId == null && email == null && name == null) return null;
        if (hashId != null && (email != null || name != null)) throw new DomainException("import_collector_reference_conflict");
        AppUser user = hashId != null
                ? users.findByPublicIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                    .orElseThrow(() -> new DomainException("import_collector_not_found"))
                : users.findByIdAndStatusNot(unique(lookup.findUserIds(email, name), "collector"), TypologyEnum.DELETED.getId())
                    .orElseThrow(() -> new DomainException("import_collector_not_found"));
        if (user.getTpRole() == null || !TypologyEnum.COLLECTOR.getId().equals(user.getTpRole().getInternalId())) {
            throw new DomainException("import_collector_role_invalid");
        }
        if (user.getTpStatus() == null || !TypologyEnum.ACTIVE.getId().equals(user.getTpStatus().getInternalId())) {
            throw new DomainException("import_collector_inactive");
        }
        return user;
    }

    private Long unique(List<Long> matches, String party) {
        if (matches.isEmpty()) throw new DomainException("import_" + party + "_not_found");
        if (matches.size() > 1) throw new DomainException("import_" + party + "_ambiguous");
        return matches.get(0);
    }
}
