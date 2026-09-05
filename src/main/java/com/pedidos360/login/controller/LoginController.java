package com.pedidos360.login.controller;

import com.pedidos360.login.model.UsuarioDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Único rol de este microservicio: decodificar el JWT (ya validado por el
 * resource server contra Azure) y devolver los datos del usuario.
 *
 * La ruta vive bajo /login/ para que el API Gateway pueda enrutar /login/*
 * hacia este servicio sin reescritura de path.
 */
@RestController
@RequestMapping("/login")
public class LoginController {

  @GetMapping("/me")
  public UsuarioDto me(@AuthenticationPrincipal Jwt jwt) {
    return UsuarioDto.desdeClaims(jwt.getClaims());
  }
}
