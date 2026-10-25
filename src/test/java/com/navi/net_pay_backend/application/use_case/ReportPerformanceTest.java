package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.OverdueClientQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Measures actual JDBC statements, including both Hibernate and JdbcTemplate. */
@SpringBootTest(properties = "spring.jpa.show-sql=false")
@Import(ReportPerformanceTest.CountingConfig.class)
@EnabledIfSystemProperty(named = "netpay.performance", matches = "true")
class ReportPerformanceTest {
    private static final AtomicInteger STATEMENTS = new AtomicInteger();
    @Autowired private DashboardUseCase dashboard;
    @Autowired private ReportUseCase reports;
    @Autowired private jakarta.persistence.EntityManagerFactory entityManagerFactory;
    @Autowired private LoginUseCase login;
    @Autowired private com.navi.net_pay_backend.domain.service.PasswordHashService passwords;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Test
    @org.springframework.transaction.annotation.Transactional
    void measuresRealLoginWithTemporaryUser() {
        String email = java.util.UUID.randomUUID() + "@example.com";
        String password = java.util.UUID.randomUUID().toString();
        long id = jdbc.queryForObject("""
                insert into app_user (hash_id, full_name, email, password_hash, tp_role, tp_status)
                values (?, 'Login performance test', ?, ?,
                        (select typology_id from adm_typology where internal_id = 61),
                        (select typology_id from adm_typology where internal_id = 49)) returning id
                """, Long.class, java.util.UUID.randomUUID().toString(), email, passwords.hash(password));
        measure("login", () -> assertNotNull(login.doLogin(
                new com.navi.net_pay_backend.application.model.LoginRequest(email, password, "performance-test"), "JUnit")));
        assertNotNull(jdbc.queryForObject("select last_login_at from app_user where id = ?", java.sql.Timestamp.class, id));
    }

    @Test
    void measuresDashboardAndOverdueReport() {
        measure("dashboard", () -> assertNotNull(dashboard.dashboard(null, null, 5, 6)));
        measure("overdue-page", () -> assertNotNull(reports.overdueClients(new OverdueClientQueryDto())));
    }

    private void measure(String name, Runnable operation) {
        STATEMENTS.set(0);
        long start = System.nanoTime();
        // Match the HTTP application's current Open-EntityManager-in-View lifetime.
        boolean needsSession = !org.springframework.transaction.support.TransactionSynchronizationManager.hasResource(entityManagerFactory);
        var entityManager = needsSession ? entityManagerFactory.createEntityManager() : null;
        if (needsSession) {
            org.springframework.transaction.support.TransactionSynchronizationManager.bindResource(
                    entityManagerFactory, new org.springframework.orm.jpa.EntityManagerHolder(entityManager));
        }
        try {
            operation.run();
        } finally {
            if (needsSession) {
                org.springframework.transaction.support.TransactionSynchronizationManager.unbindResource(entityManagerFactory);
                entityManager.close();
            }
        }
        long millis = (System.nanoTime() - start) / 1_000_000;
        int count = STATEMENTS.get();
        LoggerFactory.getLogger(getClass()).info("PERFORMANCE {}: {} SQL statements, {} ms", name, count, millis);
        if (Boolean.getBoolean("netpay.performance.assert-bounds")) {
            assertTrue(count <= (name.equals("dashboard") ? 30 : 4), name + ": " + count + " SQL statements");
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class CountingConfig {
        @Bean
        static BeanPostProcessor countingDataSource() {
            return new BeanPostProcessor() {
                @Override
                public Object postProcessAfterInitialization(Object bean, String name) {
                    if (!(bean instanceof DataSource source)) return bean;
                    return new DelegatingDataSource(source) {
                        @Override
                        public Connection getConnection() throws SQLException {
                            return counted(super.getConnection());
                        }

                        @Override
                        public Connection getConnection(String user, String password) throws SQLException {
                            return counted(super.getConnection(user, password));
                        }
                    };
                }
            };
        }

        private static Connection counted(Connection connection) {
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("prepareStatement") || method.getName().equals("prepareCall")) {
                            STATEMENTS.incrementAndGet();
                        }
                        try {
                            return method.invoke(connection, args);
                        } catch (InvocationTargetException ex) {
                            throw ex.getCause();
                        }
                    });
        }
    }
}
