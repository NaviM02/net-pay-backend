package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.AppPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportJpaRepository extends JpaRepository<AppPaymentEntity, Long> {

    @Query(value = """
            SELECT
                COALESCE(SUM(CASE WHEN tps.internal_id = :completed THEN p.amount ELSE 0 END), 0) AS total_collected,
                COUNT(CASE WHEN tps.internal_id = :completed THEN 1 END) AS completed_count,
                COUNT(CASE WHEN tps.internal_id = :failed THEN 1 END) AS failed_count,
                COUNT(CASE WHEN tps.internal_id = :pending THEN 1 END) AS pending_count
            FROM payment p
            JOIN adm_typology tps ON tps.typology_id = p.tp_payment_status
            WHERE p.deleted_at IS NULL
              AND tps.internal_id <> :deleted
              AND p.paid_at BETWEEN :from AND :to
            """, nativeQuery = true)
    List<Object[]> collectionSummary(@Param("from") LocalDate from,
                                     @Param("to") LocalDate to,
                                     @Param("completed") Long completed,
                                     @Param("failed") Long failed,
                                     @Param("pending") Long pending,
                                     @Param("deleted") Long deleted);

    @Query(value = """
            SELECT
                COALESCE(SUM(CASE WHEN tps.internal_id = :completed THEN p.amount ELSE 0 END), 0) AS total_collected,
                COUNT(CASE WHEN tps.internal_id = :completed THEN 1 END) AS completed_count,
                COUNT(CASE WHEN tps.internal_id = :failed THEN 1 END) AS failed_count,
                COUNT(CASE WHEN tps.internal_id = :pending THEN 1 END) AS pending_count
            FROM payment p
            JOIN adm_typology tps ON tps.typology_id = p.tp_payment_status
            WHERE p.deleted_at IS NULL
              AND tps.internal_id <> :deleted
              AND p.collector_id = :collectorId
              AND p.paid_at BETWEEN :from AND :to
            """, nativeQuery = true)
    List<Object[]> collectionSummaryByCollector(@Param("from") LocalDate from,
                                                @Param("to") LocalDate to,
                                                @Param("collectorId") Long collectorId,
                                                @Param("completed") Long completed,
                                                @Param("failed") Long failed,
                                                @Param("pending") Long pending,
                                                @Param("deleted") Long deleted);

    @Query(value = """
            SELECT u.hash_id, u.full_name,
                   COALESCE(SUM(p.amount), 0) AS total_collected,
                   COUNT(p.id) AS payment_count
            FROM payment p
            JOIN adm_typology tps ON tps.typology_id = p.tp_payment_status
            LEFT JOIN app_user u ON u.id = p.collector_id
            WHERE p.deleted_at IS NULL
              AND tps.internal_id = :completed
              AND p.paid_at BETWEEN :from AND :to
            GROUP BY u.id, u.hash_id, u.full_name
            ORDER BY total_collected DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> collectionsByCollector(@Param("from") LocalDate from,
                                          @Param("to") LocalDate to,
                                          @Param("completed") Long completed,
                                          @Param("limit") int limit);

    @Query(value = """
            SELECT COUNT(*)
            FROM client c
            JOIN adm_typology tcs ON tcs.typology_id = c.tp_status
            WHERE c.deleted_at IS NULL AND tcs.internal_id = :active
            """, nativeQuery = true)
    long countActiveClients(@Param("active") Long active);

    @Query(value = """
            SELECT COUNT(*)
            FROM client c
            JOIN adm_typology tcs ON tcs.typology_id = c.tp_status
            WHERE c.deleted_at IS NULL
              AND tcs.internal_id = :active
              AND c.collector_id = :collectorId
            """, nativeQuery = true)
    long countActiveClientsByCollector(@Param("active") Long active,
                                       @Param("collectorId") Long collectorId);

    @Query(value = """
            SELECT COUNT(*)
            FROM receipt r
            WHERE r.deleted_at IS NULL AND r.issued_at >= :from AND r.issued_at < :to
            """, nativeQuery = true)
    long countReceiptsIssuedBetween(@Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT COUNT(*)
            FROM receipt r
            WHERE r.deleted_at IS NULL
              AND r.collector_id = :collectorId
              AND r.issued_at >= :from AND r.issued_at < :to
            """, nativeQuery = true)
    long countReceiptsIssuedBetweenByCollector(@Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to,
                                               @Param("collectorId") Long collectorId);

    @Query(value = """
            SELECT s.hash_id, s.name, COUNT(c.id) AS active_clients
            FROM client c
            JOIN adm_typology tcs ON tcs.typology_id = c.tp_status
            JOIN app_service s ON s.id = c.service_id
            WHERE c.deleted_at IS NULL AND tcs.internal_id = :active
            GROUP BY s.id, s.hash_id, s.name
            ORDER BY active_clients DESC
            """, nativeQuery = true)
    List<Object[]> activeClientsByPlan(@Param("active") Long active);

    @Query(value = """
            SELECT tpm.internal_id, tpm.value_1, tpm.description,
                   COALESCE(SUM(p.amount), 0) AS total_collected,
                   COUNT(p.id) AS payment_count
            FROM payment p
            JOIN adm_typology tps ON tps.typology_id = p.tp_payment_status
            JOIN adm_typology tpm ON tpm.typology_id = p.tp_payment_method
            WHERE p.deleted_at IS NULL
              AND tps.internal_id = :completed
              AND p.paid_at BETWEEN :from AND :to
            GROUP BY tpm.internal_id, tpm.value_1, tpm.description
            ORDER BY tpm.internal_id
            """, nativeQuery = true)
    List<Object[]> collectionsByMethod(@Param("from") LocalDate from,
                                       @Param("to") LocalDate to,
                                       @Param("completed") Long completed);

    @Query(value = """
            SELECT tpm.internal_id, tpm.value_1, tpm.description,
                   COALESCE(SUM(p.amount), 0) AS total_collected,
                   COUNT(p.id) AS payment_count
            FROM payment p
            JOIN adm_typology tps ON tps.typology_id = p.tp_payment_status
            JOIN adm_typology tpm ON tpm.typology_id = p.tp_payment_method
            WHERE p.deleted_at IS NULL
              AND tps.internal_id = :completed
              AND p.collector_id = :collectorId
              AND p.paid_at BETWEEN :from AND :to
            GROUP BY tpm.internal_id, tpm.value_1, tpm.description
            ORDER BY tpm.internal_id
            """, nativeQuery = true)
    List<Object[]> collectionsByMethodByCollector(@Param("from") LocalDate from,
                                                  @Param("to") LocalDate to,
                                                  @Param("completed") Long completed,
                                                  @Param("collectorId") Long collectorId);

    @Query(value = """
            SELECT CAST(EXTRACT(YEAR FROM p.paid_at) AS integer) AS yr,
                   CAST(EXTRACT(MONTH FROM p.paid_at) AS integer) AS mo,
                   tpm.internal_id, tpm.value_1, tpm.description,
                   COALESCE(SUM(p.amount), 0) AS total_collected,
                   COUNT(p.id) AS payment_count
            FROM payment p
            JOIN adm_typology tps ON tps.typology_id = p.tp_payment_status
            JOIN adm_typology tpm ON tpm.typology_id = p.tp_payment_method
            WHERE p.deleted_at IS NULL
              AND tps.internal_id = :completed
              AND p.paid_at BETWEEN :from AND :to
            GROUP BY EXTRACT(YEAR FROM p.paid_at), EXTRACT(MONTH FROM p.paid_at),
                     tpm.internal_id, tpm.value_1, tpm.description
            ORDER BY yr, mo
            """, nativeQuery = true)
    List<Object[]> collectionsByMethodByMonth(@Param("from") LocalDate from,
                                              @Param("to") LocalDate to,
                                              @Param("completed") Long completed);

    @Query(value = """
            SELECT CAST(EXTRACT(YEAR FROM p.paid_at) AS integer) AS yr,
                   CAST(EXTRACT(MONTH FROM p.paid_at) AS integer) AS mo,
                   tpm.internal_id, tpm.value_1, tpm.description,
                   COALESCE(SUM(p.amount), 0) AS total_collected,
                   COUNT(p.id) AS payment_count
            FROM payment p
            JOIN adm_typology tps ON tps.typology_id = p.tp_payment_status
            JOIN adm_typology tpm ON tpm.typology_id = p.tp_payment_method
            WHERE p.deleted_at IS NULL
              AND tps.internal_id = :completed
              AND p.collector_id = :collectorId
              AND p.paid_at BETWEEN :from AND :to
            GROUP BY EXTRACT(YEAR FROM p.paid_at), EXTRACT(MONTH FROM p.paid_at),
                     tpm.internal_id, tpm.value_1, tpm.description
            ORDER BY yr, mo
            """, nativeQuery = true)
    List<Object[]> collectionsByMethodByMonthByCollector(@Param("from") LocalDate from,
                                                         @Param("to") LocalDate to,
                                                         @Param("completed") Long completed,
                                                         @Param("collectorId") Long collectorId);
}
