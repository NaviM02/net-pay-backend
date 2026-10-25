package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.dto.OverdueClientQueryDto;
import com.navi.net_pay_backend.domain.repository.ClientChargeRepository;
import com.navi.net_pay_backend.domain.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.jpa.show-sql=false")
@Transactional
class ReportRepositoryIntegrationTest {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ClientChargeRepository charges;
    @Autowired private ReportRepository reports;

    @Test
    void bulkGenerationIsIdempotentScopedAndPreservesHistoricalCharges() {
        long collector = collector();
        long otherCollector = collector();
        long service = service("100.00");
        LocalDate start = YearMonth.now().minusMonths(2).atDay(1);
        long client = client("History " + UUID.randomUUID(), start, service, collector, 49);
        long other = client("Other " + UUID.randomUUID(), start, service, otherCollector, 49);
        long deleted = client("Deleted " + UUID.randomUUID(), start, service, collector, 51);
        long future = client("Future " + UUID.randomUUID(), YearMonth.now().plusMonths(1).atDay(1), service, collector, 49);
        long inactive = client("Inactive " + UUID.randomUUID(), start, service, collector, 50);
        long noDate = client("No date " + UUID.randomUUID(), null, service, collector, 49);

        charges.ensureForClients(LocalDate.now(), collector);
        assertEquals(3, chargeCount(client));
        assertEquals(3, chargeCount(inactive));
        assertEquals(1, chargeCount(noDate));
        assertEquals(0, chargeCount(other));
        assertEquals(0, chargeCount(deleted));
        assertEquals(0, chargeCount(future));

        long firstCharge = jdbc.queryForObject("select min(id) from client_charge where client_id = ?", Long.class, client);
        jdbc.update("update client_charge set expected_amount = 75, deleted_at = CURRENT_TIMESTAMP where id = ?", firstCharge);
        jdbc.update("update app_service set monthly_price = 200 where id = ?", service);
        charges.ensureForClients(LocalDate.now(), collector);
        assertEquals(3, chargeCount(client), "Do not recreate or reprice existing obligations, even deleted ones");
        assertEquals(new BigDecimal("75.00"), jdbc.queryForObject(
                "select expected_amount from client_charge where id = ?", BigDecimal.class, firstCharge));
        assertNotNull(jdbc.queryForObject("select deleted_at from client_charge where id = ?", java.sql.Timestamp.class, firstCharge));
        assertEquals(2L, jdbc.queryForObject("select count(*) from client_charge where client_id = ? and expected_amount = 100",
                Long.class, client));
    }

    @Test
    void overdueBalancesRespectPartialPaymentsDeletedPaymentsAndCurrentMonth() {
        long collector = collector();
        long service = service("100.00");
        long client = client("Balance " + UUID.randomUUID(), YearMonth.now().minusMonths(2).atDay(1), service, collector, 49);
        long free = client("Free " + UUID.randomUUID(), YearMonth.now().minusMonths(2).atDay(1), service("0.00"), collector, 49);
        charges.ensureForClients(LocalDate.now(), collector);
        var ids = jdbc.queryForList("""
                select cc.id from client_charge cc join billing_period bp on bp.id = cc.period_id
                where cc.client_id = ? order by bp.starts_on
                """, Long.class, client);
        allocate(client, service, collector, ids.get(0), "40.00", false);
        allocate(client, service, collector, ids.get(0), "60.00", true);
        allocate(client, service, collector, ids.get(1), "100.00", false);

        var overdue = reports.overdueClients(collector);
        assertEquals(1, overdue.size());
        assertEquals(1, overdue.get(0).getOverdueMonths());
        assertEquals(new BigDecimal("60.00"), overdue.get(0).getTotalDebt());
        assertEquals(java.util.List.of(YearMonth.now().minusMonths(2).toString()), overdue.get(0).getOverduePeriods());
        assertEquals(overdue.size(), reports.countOverdueClients(collector));
        assertEquals(3, chargeCount(free));

        jdbc.update("update client set deleted_at = CURRENT_TIMESTAMP where id = ?", client);
        assertEquals(0, reports.countOverdueClients(collector));
        assertTrue(reports.overdueClients(collector).isEmpty());
    }

    @Test
    void filteringAndPaginationStayConsistentIncludingEmptyPagesAndLiteralWildcards() {
        long collector = collector();
        long service = service("100.00");
        String prefix = "Report " + UUID.randomUUID();
        long first = client(prefix + " %_ A", YearMonth.now().minusMonths(1).atDay(1), service, collector, 49);
        client(prefix + " B", YearMonth.now().minusMonths(1).atDay(1), service, collector, 49);
        client(prefix + " C", YearMonth.now().minusMonths(1).atDay(1), service, collector, 50);
        charges.ensureForClients(LocalDate.now(), collector);
        var query = new OverdueClientQueryDto();
        query.setSearch(prefix.toUpperCase(java.util.Locale.ROOT));
        query.setSize(2);
        var page = reports.overdueClientsPage(query);
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getItems().size());
        query.setOffset(2);
        var last = reports.overdueClientsPage(query);
        assertEquals(1, last.getItems().size());
        assertFalse(page.getItems().stream().map(v -> v.getClientHashId()).toList().contains(last.getItems().get(0).getClientHashId()));
        query.setOffset(100);
        assertEquals(3, reports.overdueClientsPage(query).getTotal());
        assertTrue(reports.overdueClientsPage(query).getItems().isEmpty());
        query.setOffset(0);
        query.setStatus(49L);
        query.setServiceHashId(jdbc.queryForObject("select hash_id from app_service where id = ?", String.class, service));
        assertEquals(2, reports.overdueClientsPage(query).getTotal());
        assertEquals(2, reports.filteredOverdueClients(query).size());
        query.setSearch(prefix + " %_");
        var literal = reports.overdueClientsPage(query);
        assertEquals(1, literal.getTotal());
        assertEquals(jdbc.queryForObject("select hash_id from client where id = ?", String.class, first),
                literal.getItems().get(0).getClientHashId());
        query.setSearch("' OR 1=1 --");
        assertEquals(0, reports.overdueClientsPage(query).getTotal());
    }

    private long collector() {
        return jdbc.queryForObject("""
                insert into app_user (hash_id, full_name, email, password_hash, tp_role, tp_status)
                values (?, 'Report test', ?, 'unused-test-hash',
                        (select typology_id from adm_typology where internal_id = 62),
                        (select typology_id from adm_typology where internal_id = 49)) returning id
                """, Long.class, UUID.randomUUID().toString(), UUID.randomUUID() + "@example.com");
    }

    private long service(String price) {
        return jdbc.queryForObject("""
                insert into app_service (hash_id, name, speed, monthly_price, tp_type, tp_status)
                values (?, ?, '10 Mbps', ?,
                        (select typology_id from adm_typology where internal_id = 71),
                        (select typology_id from adm_typology where internal_id = 49)) returning id
                """, Long.class, UUID.randomUUID().toString(), "Report " + UUID.randomUUID(), new BigDecimal(price));
    }

    private long client(String name, LocalDate since, long service, long collector, long status) {
        return jdbc.queryForObject("""
                insert into client (hash_id, business_name, contact_name, customer_since, service_id, collector_id, tp_status)
                values (?, ?, 'Test contact', ?, ?, ?,
                        (select typology_id from adm_typology where internal_id = ?)) returning id
                """, Long.class, UUID.randomUUID().toString(), name, since, service, collector, status);
    }

    private long chargeCount(long client) {
        return jdbc.queryForObject("select count(*) from client_charge where client_id = ?", Long.class, client);
    }

    private void allocate(long client, long service, long collector, long charge, String amount, boolean deleted) {
        long payment = jdbc.queryForObject("""
                insert into payment (hash_id, client_id, service_id, collector_id, amount, paid_at, tp_payment_method, tp_payment_status)
                values (?, ?, ?, ?, ?, CURRENT_DATE,
                        (select typology_id from adm_typology where internal_id = 81),
                        (select typology_id from adm_typology where internal_id = 91)) returning id
                """, Long.class, UUID.randomUUID().toString(), client, service, collector, new BigDecimal(amount));
        if (deleted) jdbc.update("update payment set deleted_at = CURRENT_TIMESTAMP where id = ?", payment);
        jdbc.update("insert into payment_allocation (payment_id, charge_id, amount) values (?, ?, ?)",
                payment, charge, new BigDecimal(amount));
    }
}
