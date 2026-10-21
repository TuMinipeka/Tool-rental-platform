package com.toolrental.domain.common.port;

import java.util.function.Supplier;

public interface UnitOfWork {
  <T> T execute(Supplier<T> operation);
}
