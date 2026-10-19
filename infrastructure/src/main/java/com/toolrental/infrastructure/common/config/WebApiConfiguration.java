package com.toolrental.infrastructure.common.config;

import com.toolrental.infrastructure.common.web.LegacyApiDeprecationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class WebApiConfiguration implements WebMvcConfigurer {
  private final LegacyApiDeprecationInterceptor legacy;

  public WebApiConfiguration(LegacyApiDeprecationInterceptor legacy) {
    this.legacy = legacy;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(legacy).addPathPatterns("/api/**");
  }
}
