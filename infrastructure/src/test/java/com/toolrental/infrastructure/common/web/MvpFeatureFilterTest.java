package com.toolrental.infrastructure.common.web;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class MvpFeatureFilterTest {
  @Test
  void disabledManualPaymentsReturnStableProblemDetailsForBothAliases() throws Exception {
    var filter = new MvpFeatureFilter(false, new ObjectMapper());
    for (var prefix : java.util.List.of("/api", "/api/v1"))
      for (var route :
          java.util.List.of(
              "/payments/simulations",
              "/payments/internal/checkouts",
              "/payments/sandbox/webhooks",
              "/payments")) {
        var response = new MockHttpServletResponse();
        filter.doFilter(
            new MockHttpServletRequest("POST", prefix + route),
            response,
            (a, b) -> {
              throw new AssertionError("Disabled route reached application");
            });
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getContentType()).startsWith("application/problem+json");
        var body = new ObjectMapper().readTree(response.getContentAsString());
        assertThat(body.path("code").asText()).isEqualTo("FEATURE_DISABLED");
        assertThat(body.path("traceId").asText()).isNotBlank();
      }
  }

  @Test
  void hostedCheckoutAndPaymentQueriesStillReachAuthentication() throws Exception {
    var filter = new MvpFeatureFilter(false, new ObjectMapper());
    for (var method : java.util.List.of("POST", "GET")) {
      var passed = new java.util.concurrent.atomic.AtomicBoolean();
      filter.doFilter(
          new MockHttpServletRequest(method, "/api/v1/payments/checkouts"),
          new MockHttpServletResponse(),
          (a, b) -> passed.set(true));
      assertThat(passed).isTrue();
    }
  }
}
