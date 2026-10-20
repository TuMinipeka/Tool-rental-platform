package com.toolrental.infrastructure.common.config;

import com.toolrental.infrastructure.common.web.PrincipalInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
  private final PrincipalInterceptor principal;

  public WebConfiguration(PrincipalInterceptor principal) {
    this.principal = principal;
  }

  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(principal)
        .addPathPatterns("/api/**")
        .excludePathPatterns(
            "/api/auth/**",
            "/api/public/**",
            "/api/payments/sandbox/webhooks",
            "/api/v1/auth/**",
            "/api/v1/public/**",
            "/api/v1/payments/sandbox/webhooks");
  }
}
