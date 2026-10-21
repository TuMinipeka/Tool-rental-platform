package com.toolrental.domain.common.port;

import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;

/** Active profiles with coherent roles; identity will be implemented in a later phase. */
public interface ParticipantsPort {
  boolean isCustomerActive(CustomerId id);

  boolean isSupplierActive(SupplierId id);
}
