package com.toolrental.infrastructure.identity.adapters.out.persistence.repository;

import com.toolrental.infrastructure.identity.adapters.out.persistence.entity.CustomerProfileJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerProfileJpaRepository
    extends JpaRepository<CustomerProfileJpaEntity, UUID> {}
