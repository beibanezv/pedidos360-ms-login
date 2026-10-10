package com.pedidos360.login.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

  private Jwt jwtCognito() {
    return Jwt.withTokenValue("id-token-cognito")
        .header("alg", "RS256")
        .subject("143884e8-a0e1-7060-ac41-708a2bb66381")
        .claim("iss", "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_A8lrPsDta")
        .claim("token_use", "id")
        .claim("email", "cliente@pedidos360.cl")
        .build();
  }

  @Test
  void decodeDual_usaPrimarioCuandoValida() {
    Jwt esperado = jwtCognito();
    JwtDecoder primario = mock(JwtDecoder.class);
    JwtDecoder respaldo = mock(JwtDecoder.class);
    when(primario.decode("t")).thenReturn(esperado);

    assertEquals(esperado, SecurityConfig.decodeDual(primario, respaldo, "t"));
    verifyNoInteractions(respaldo);
  }

  @Test
  void decodeDual_recurreARespaldoCuandoPrimarioFalla() {
    Jwt esperado = jwtCognito();
    JwtDecoder primario = mock(JwtDecoder.class);
    JwtDecoder respaldo = mock(JwtDecoder.class);
    when(primario.decode(anyString())).thenThrow(new JwtException("firma no calza"));
    when(respaldo.decode("t")).thenReturn(esperado);

    assertEquals(esperado, SecurityConfig.decodeDual(primario, respaldo, "t"));
  }
}
