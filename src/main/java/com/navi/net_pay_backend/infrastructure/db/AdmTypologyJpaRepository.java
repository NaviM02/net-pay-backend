package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.AdmTypologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdmTypologyJpaRepository extends JpaRepository<AdmTypologyEntity, Long> {

    Optional<AdmTypologyEntity> findByInternalId(Long internalId);

    @Query("""
        SELECT child
        FROM AdmTypologyEntity child
        JOIN AdmTypologyEntity parent
            ON child.parentTypologyId = parent.typologyId
        WHERE parent.internalId = :parentInternalId
        """)
    List<AdmTypologyEntity> findByParentInternalId(@Param("parentInternalId") Long parentInternalId);
}