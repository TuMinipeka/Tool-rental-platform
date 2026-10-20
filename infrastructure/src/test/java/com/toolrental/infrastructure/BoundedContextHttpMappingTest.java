package com.toolrental.infrastructure;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.toolrental.application.cancellationpolicy.usecase.ManageCancellationPolicyUseCase;
import com.toolrental.application.deliveryrate.usecase.ManageDeliveryRateUseCase;
import com.toolrental.application.passwordreset.dto.PasswordRecoveryResponse;
import com.toolrental.application.passwordreset.usecase.RecoverPasswordUseCase;
import com.toolrental.application.user.usecase.AuthenticateUserUseCase;
import com.toolrental.application.user.usecase.RegisterCustomerAccountUseCase;
import com.toolrental.application.user.usecase.RegisterSupplierAccountUseCase;
import com.toolrental.application.usersession.usecase.ManageUserSessionUseCase;
import com.toolrental.application.usersession.usecase.RefreshSessionUseCase;
import com.toolrental.infrastructure.cancellationpolicy.adapters.in.rest.controllers.CancellationPolicyController;
import com.toolrental.infrastructure.deliveryrate.adapters.in.rest.controllers.DeliveryRateController;
import com.toolrental.infrastructure.passwordreset.adapters.in.rest.controllers.PasswordRecoveryController;
import com.toolrental.infrastructure.user.adapters.in.rest.controllers.AuthenticationController;
import com.toolrental.infrastructure.usersession.adapters.in.rest.RefreshCookieFactory;
import com.toolrental.infrastructure.usersession.adapters.in.rest.controllers.UserSessionController;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class BoundedContextHttpMappingTest {
  @Test
  void splitControllersKeepVersionedAndLegacyPathsWithoutDuplicateMappings() throws Exception {
    var recovery = mock(RecoverPasswordUseCase.class);
    var rates = mock(ManageDeliveryRateUseCase.class);
    var policies = mock(ManageCancellationPolicyUseCase.class);
    when(recovery.request(anyString())).thenReturn(new PasswordRecoveryResponse(true));
    when(rates.list(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
    when(policies.list(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
    var cookies = new RefreshCookieFactory(Duration.ofDays(30), false, "Lax");
    var mvc =
        MockMvcBuilders.standaloneSetup(
                new AuthenticationController(
                    mock(AuthenticateUserUseCase.class),
                    mock(RegisterCustomerAccountUseCase.class),
                    mock(RegisterSupplierAccountUseCase.class),
                    cookies),
                new PasswordRecoveryController(recovery),
                new UserSessionController(
                    mock(RefreshSessionUseCase.class),
                    mock(ManageUserSessionUseCase.class),
                    cookies),
                new DeliveryRateController(rates),
                new CancellationPolicyController(policies))
            .build();

    var principal = (java.security.Principal) () -> UUID.randomUUID().toString();
    mvc.perform(get("/api/v1/commercial-configuration/delivery-rates").principal(principal))
        .andExpect(status().isOk());
    mvc.perform(get("/api/commercial-configuration/cancellation-policies").principal(principal))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/v1/auth/password-recovery")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"person@example.com\"}"))
        .andExpect(status().isAccepted());
  }
}
