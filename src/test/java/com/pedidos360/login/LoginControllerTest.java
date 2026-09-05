package com.pedidos360.login;

import com.pedidos360.login.config.SecurityConfig;
import com.pedidos360.login.controller.LoginController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@Import(SecurityConfig.class)
class LoginControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean JwtDecoder jwtDecoder;

  private Jwt jwtDeEjemplo() {
    return Jwt.withTokenValue("token-de-prueba")
        .header("alg", "RS256")
        .subject("sub-123")
        .claim("oid", "oid-abc")
        .claim("name", "Ana Soto")
        .claim("email", "ana.soto@ejemplo.cl")
        .claim("preferred_username", "ana.soto@ejemplo.cl")
        .claim("roles", java.util.List.of("Cliente"))
        .claim("scp", "Carrito.ReadWrite Productos.Read")
        .build();
  }

  @Test
  void loginMe_sinJwt_retorna401() throws Exception {
    mockMvc.perform(get("/login/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginMe_conJwt_retornaDatosUsuario() throws Exception {
    mockMvc.perform(get("/login/me").with(jwt().jwt(jwtDeEjemplo())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.oid").value("oid-abc"))
        .andExpect(jsonPath("$.nombre").value("Ana Soto"))
        .andExpect(jsonPath("$.correo").value("ana.soto@ejemplo.cl"))
        .andExpect(jsonPath("$.roles[0]").value("Cliente"))
        .andExpect(jsonPath("$.scopes.length()").value(2))
        .andExpect(jsonPath("$.scopes[0]").value("Carrito.ReadWrite"));
  }

  @Test
  void loginMe_conJwt_sinName_usaPreferredUsername() throws Exception {
    Jwt sinNombre = Jwt.withTokenValue("token-de-prueba")
        .header("alg", "RS256")
        .subject("sub-123")
        .claim("oid", "oid-abc")
        .claim("preferred_username", "ana.soto@ejemplo.cl")
        .build();

    mockMvc.perform(get("/login/me").with(jwt().jwt(sinNombre)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("ana.soto@ejemplo.cl"))
        .andExpect(jsonPath("$.correo").value("ana.soto@ejemplo.cl"));
  }

  @Test
  void loginMe_conJwt_minimo_retornaListasVacias() throws Exception {
    Jwt minimo = Jwt.withTokenValue("token-de-prueba")
        .header("alg", "RS256")
        .subject("solo-sub")
        .build();

    mockMvc.perform(get("/login/me").with(jwt().jwt(minimo)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.oid").value("solo-sub"))
        .andExpect(jsonPath("$.roles.length()").value(0))
        .andExpect(jsonPath("$.scopes.length()").value(0));
  }
}
