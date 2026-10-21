package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toolrental.infrastructure.common.web.AuthenticationRateLimitFilter;
import com.toolrental.infrastructure.user.adapters.in.rest.dtos.LoginRequest;
import com.toolrental.infrastructure.user.adapters.out.password.BcryptPasswordHasher;
import jakarta.validation.Validation;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginInputSecurityTest {
  @Test
  void rejectsUnsafeEmailInputButAllowsNormalAddressPunctuation() {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      for (var email :
          java.util.List.of(
              "<script>@example.test",
              "a\u200bb@example.test",
              "a\nb@example.test",
              "a".repeat(321) + "@example.test"))
        assertThat(validator.validate(new LoginRequest(email, "Valid password!2026"))).isNotEmpty();
      var request = new LoginRequest(" Customer+tag@example.test ", "Valid password!2026");
      assertThat(validator.validate(request)).isEmpty();
      assertThat(request.email()).isEqualTo("Customer+tag@example.test");
      assertThat(request.toString()).doesNotContain(request.password(), request.email());
    }
  }

  @Test
  void enforcesBcryptByteBoundaryWithoutFilteringValidPasswordSymbols() {
    var hasher = new BcryptPasswordHasher();
    var password = "á".repeat(30) + " ' &<>!?2026";
    var hash = hasher.hash(password);
    assertThat(hasher.matches(password, hash)).isTrue();
    assertThat(hasher.matches("a".repeat(73), hash)).isFalse();
    assertThat(hasher.matches("\0" + password, hash)).isFalse();
    assertThatCode(() -> new LoginRequest("a@example.test", "á".repeat(36)))
        .doesNotThrowAnyException();
    assertThatThrownBy(() -> new LoginRequest("a@example.test", "á".repeat(37)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new LoginRequest("a@example.test", "password\0suffix"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void apiAliasesShareTheSameAuthenticationRateLimit() throws Exception {
    var filter = new AuthenticationRateLimitFilter(new ObjectMapper(), Clock.systemUTC(), 1);
    var first = new MockHttpServletResponse();
    filter.doFilter(new MockHttpServletRequest("POST", "/api/v1/auth/login"), first, (a, b) -> {});
    var second = new MockHttpServletResponse();
    filter.doFilter(
        new MockHttpServletRequest("POST", "/api/auth/login"),
        second,
        (a, b) -> {
          throw new AssertionError("Rate limit bypassed");
        });
    assertThat(second.getStatus()).isEqualTo(429);
    assertThat(second.getHeader("Retry-After")).isEqualTo("60");
    assertThat(second.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
  }
}
