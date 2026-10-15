package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.toolrental.infrastructure.common.config.ClockConfiguration;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class BeanConfigurationTest {
  @Test
  void clockCanBeOverridden() {
    var clock = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC);
    new ApplicationContextRunner()
        .withBean(Clock.class, () -> clock)
        .withUserConfiguration(ClockConfiguration.class)
        .run(context -> assertThat(context.getBean(Clock.class)).isSameAs(clock));
  }
}
