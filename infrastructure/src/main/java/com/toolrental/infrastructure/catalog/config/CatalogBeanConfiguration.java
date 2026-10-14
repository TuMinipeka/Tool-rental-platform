package com.toolrental.infrastructure.catalog.config;

import com.toolrental.application.catalog.usecase.ConsultarCatalogoUseCase;
import com.toolrental.application.catalog.usecase.RegistrarCategoriaUseCase;
import com.toolrental.application.catalog.usecase.RegistrarHerramientaUseCase;
import com.toolrental.application.catalog.usecase.RegistrarUnidadUseCase;
import com.toolrental.domain.catalog.port.repository.CategoriaRepository;
import com.toolrental.domain.catalog.port.repository.HerramientaRepository;
import com.toolrental.domain.catalog.port.repository.RegistroUnidadAtomico;
import com.toolrental.domain.catalog.port.repository.UnidadEquipoRepository;
import com.toolrental.domain.common.port.ParticipantesPort;
import com.toolrental.infrastructure.common.config.ClockConfiguration;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(ClockConfiguration.class)
public class CatalogBeanConfiguration {
  @Bean
  @ConditionalOnBean(CategoriaRepository.class)
  public RegistrarCategoriaUseCase registrarCategoriaUseCase(
      CategoriaRepository repository, Clock clock) {
    return new RegistrarCategoriaUseCase(repository, clock);
  }

  @Bean
  @ConditionalOnBean({
    HerramientaRepository.class,
    CategoriaRepository.class,
    ParticipantesPort.class
  })
  public RegistrarHerramientaUseCase registrarHerramientaUseCase(
      HerramientaRepository herramientas,
      CategoriaRepository categorias,
      ParticipantesPort participantes,
      Clock clock) {
    return new RegistrarHerramientaUseCase(herramientas, categorias, participantes, clock);
  }

  @Bean
  @ConditionalOnBean(RegistroUnidadAtomico.class)
  public RegistrarUnidadUseCase registrarUnidadUseCase(
      RegistroUnidadAtomico registro, Clock clock) {
    return new RegistrarUnidadUseCase(registro, clock);
  }

  @Bean
  @ConditionalOnBean({
    CategoriaRepository.class,
    HerramientaRepository.class,
    UnidadEquipoRepository.class
  })
  public ConsultarCatalogoUseCase consultarCatalogoUseCase(
      CategoriaRepository categorias,
      HerramientaRepository herramientas,
      UnidadEquipoRepository unidades) {
    return new ConsultarCatalogoUseCase(categorias, herramientas, unidades);
  }
}
