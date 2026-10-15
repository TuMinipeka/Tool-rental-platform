package com.toolrental.infrastructure.customer.adapters.out.persistence.repository;

import com.toolrental.infrastructure.customer.adapters.out.persistence.entity.CustomerJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository
    extends JpaRepository<CustomerJpaEntity, UUID> {}
