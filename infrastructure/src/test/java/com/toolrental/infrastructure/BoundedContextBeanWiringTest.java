package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.toolrental.application.cancellationpolicy.usecase.ManageCancellationPolicyUseCase;
import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.deliveryrate.usecase.ManageDeliveryRateUseCase;
import com.toolrental.application.passwordreset.usecase.RecoverPasswordUseCase;
import com.toolrental.application.refund.usecase.QueryRefundUseCase;
import com.toolrental.application.reservationextension.usecase.ManageReservationExtensionUseCase;
import com.toolrental.application.reservationtermination.usecase.ManageReservationTerminationUseCase;
import com.toolrental.application.usersession.usecase.ManageUserSessionUseCase;
import com.toolrental.application.usersession.usecase.RefreshSessionUseCase;
import com.toolrental.domain.cancellationpolicy.port.repository.CancellationPolicyRepository;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.deliveryrate.port.repository.DeliveryRateRepository;
import com.toolrental.domain.passwordreset.port.PasswordRecoveryNotifier;
import com.toolrental.domain.passwordreset.port.repository.PasswordResetTokenRepository;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.refund.port.repository.RefundRepository;
import com.toolrental.domain.rental.port.RentalLifecycle;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservationextension.port.repository.ReservationExtensionRepository;
import com.toolrental.domain.reservationtermination.port.repository.ReservationTerminationRepository;
import com.toolrental.domain.user.port.AuthenticationTokenProvider;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.SecureTokenService;
import com.toolrental.domain.user.port.UserRoleReader;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import com.toolrental.infrastructure.cancellationpolicy.config.CancellationPolicyBeanConfiguration;
import com.toolrental.infrastructure.deliveryrate.config.DeliveryRateBeanConfiguration;
import com.toolrental.infrastructure.passwordreset.config.PasswordResetBeanConfiguration;
import com.toolrental.infrastructure.refund.config.RefundBeanConfiguration;
import com.toolrental.infrastructure.reservationextension.config.ReservationExtensionBeanConfiguration;
import com.toolrental.infrastructure.reservationtermination.config.ReservationTerminationBeanConfiguration;
import com.toolrental.infrastructure.usersession.config.UserSessionBeanConfiguration;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class BoundedContextBeanWiringTest {
  @Test
  void eachCriticalContextConfigurationProvidesItsOwnUseCases() {
    var dependencies =
        new Class<?>[] {
          Clock.class,
          UnitOfWork.class,
          AccessPolicy.class,
          WorkflowReferences.class,
          UserRepository.class,
          PasswordHasher.class,
          SecureTokenService.class,
          PasswordRecoveryNotifier.class,
          PasswordResetTokenRepository.class,
          UserSessionRepository.class,
          UserRoleReader.class,
          AuthenticationTokenProvider.class,
          CancellationPolicyRepository.class,
          DeliveryRateRepository.class,
          ReservationExtensionRepository.class,
          AtomicReservationManagement.class,
          PaymentRepository.class,
          RefundRepository.class,
          RentalRepository.class,
          RentalLifecycle.class,
          ReservationTerminationRepository.class
        };
    new ApplicationContextRunner()
        .withUserConfiguration(
            PasswordResetBeanConfiguration.class,
            UserSessionBeanConfiguration.class,
            CancellationPolicyBeanConfiguration.class,
            DeliveryRateBeanConfiguration.class,
            ReservationExtensionBeanConfiguration.class,
            RefundBeanConfiguration.class,
            ReservationTerminationBeanConfiguration.class)
        .withInitializer(
            context -> {
              context
                  .getBeanFactory()
                  .setConversionService(ApplicationConversionService.getSharedInstance());
              for (var dependency : dependencies)
                context
                    .getBeanFactory()
                    .registerSingleton(dependency.getSimpleName(), mock(dependency));
            })
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(RecoverPasswordUseCase.class);
              assertThat(context).hasSingleBean(RefreshSessionUseCase.class);
              assertThat(context).hasSingleBean(ManageUserSessionUseCase.class);
              assertThat(context).hasSingleBean(ManageCancellationPolicyUseCase.class);
              assertThat(context).hasSingleBean(ManageDeliveryRateUseCase.class);
              assertThat(context).hasSingleBean(ManageReservationExtensionUseCase.class);
              assertThat(context).hasSingleBean(QueryRefundUseCase.class);
              assertThat(context).hasSingleBean(ManageReservationTerminationUseCase.class);
            });
  }
}
