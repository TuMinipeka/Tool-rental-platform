package com.toolrental.domain.reservation.model.valueobject;

import com.toolrental.domain.common.exception.BusinessRuleException;

public record DeliveryAddress(String addressLine, String city, String region, String countryCode) {
  public DeliveryAddress {
    if (addressLine == null || addressLine.isBlank() || addressLine.length() > 300)
      throw new BusinessRuleException("Invalid delivery address");
    if (city == null || city.isBlank() || city.length() > 120)
      throw new BusinessRuleException("Invalid delivery city");
    if (region == null || region.isBlank() || region.length() > 120)
      throw new BusinessRuleException("Invalid delivery region");
    if (!"CO".equalsIgnoreCase(countryCode))
      throw new BusinessRuleException("Supplier delivery is available only in Colombia");
    countryCode = "CO";
  }
}
