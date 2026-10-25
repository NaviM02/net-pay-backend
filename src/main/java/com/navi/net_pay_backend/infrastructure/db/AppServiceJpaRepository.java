package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.AppServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppServiceJpaRepository extends JpaRepository<AppServiceEntity, Long>, JpaSpecificationExecutor<AppServiceEntity> {
    Optional<AppServiceEntity> findByNameIgnoreCaseAndDeletedAtIsNullAndTpStatusInternalIdNot(String name, Long status);

    Optional<AppServiceEntity> findByHashIdAndTpStatusInternalIdNot(String hashId, Long internalId);

    // IgnoreCase para calzar con el indice unico uq_app_service_name_alive, que compara LOWER(name).
    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(String name, Long id);
}
