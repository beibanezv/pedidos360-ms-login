package com.pedidos360.login.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
  private String issuerUri;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Este servicio no tiene endpoints publicos: decodificar el JWT es su unico rol.
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    // SIMPLIFICADO: mismo decoder perezoso de ms-productos. En local/test sin tenant
    // real NO hacer discovery contra Azure al arrancar (falla con "common").
    // Solo con un tenant UUID real se hace withIssuerLocation (valida firma contra JWKS).
    String issuer = (issuerUri != null && !issuerUri.isBlank())
        ? issuerUri.trim()
        : "";

    boolean esPlaceholder = issuer.isEmpty()
        || issuer.contains("{")
        || issuer.contains("tenantid")
        || issuer.contains("__")
        || issuer.contains("common")
        || issuer.contains("REEMPLAZA")
        || issuer.contains("TENANT_ID");

    boolean pareceUuid = issuer.matches(".*[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}.*");

    if (esPlaceholder || !pareceUuid) {
      // Local/dev: sin fetch en startup. GET /login/me sin token -> 401 sin tocar red.
      String jwkSetUri = "https://login.microsoftonline.com/common/discovery/v2.0/keys";
      return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    return NimbusJwtDecoder.withIssuerLocation(issuer).build();
  }
}
