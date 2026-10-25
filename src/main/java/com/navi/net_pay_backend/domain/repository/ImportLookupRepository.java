package com.navi.net_pay_backend.domain.repository;

import java.util.List;

/** Exact, case-insensitive lookups; at most two matches are needed to detect ambiguity. */
public interface ImportLookupRepository {
    List<Long> findClientIds(String businessName, String email, String phone);
    List<Long> findUserIds(String email, String fullName);
    void lockUserEmail(String email);
    void lockClient(Long clientId);
}
