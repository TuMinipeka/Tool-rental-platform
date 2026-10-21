package com.toolrental.infrastructure.common.transaction;

import com.toolrental.domain.common.port.UnitOfWork;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class SpringUnitOfWork implements UnitOfWork {
  private final TransactionTemplate tx;

  public SpringUnitOfWork(PlatformTransactionManager manager) {
    tx = new TransactionTemplate(manager);
  }

  public <T> T execute(Supplier<T> operation) {
    return tx.execute(status -> operation.get());
  }
}
