package com.pedidos360.login.model;

import java.util.List;
import java.util.Map;

/** Datos del usuario extraidos del JWT de Azure AD. */
public record UsuarioDto(
    String oid,
    String nombre,
    String correo,
    List<String> roles,
    List<String> scopes) {

  public static UsuarioDto desdeClaims(Map<String, Object> claims) {
    String preferredUsername = texto(claims, "preferred_username", null);
    return new UsuarioDto(
        texto(claims, "oid", texto(claims, "sub", null)),
        texto(claims, "name", preferredUsername != null ? preferredUsername : "Usuario"),
        texto(claims, "email", preferredUsername),
        lista(claims, "roles"),
        lista(claims, "scp"));
  }

  private static String texto(Map<String, Object> claims, String clave, String fallback) {
    Object valor = claims.get(clave);
    if (valor == null || String.valueOf(valor).isBlank()) {
      return fallback;
    }
    return String.valueOf(valor);
  }

  /** Acepta lista (roles) o string separado por espacios (scp de Azure). */
  private static List<String> lista(Map<String, Object> claims, String clave) {
    Object valor = claims.get(clave);
    if (valor instanceof List<?> l) {
      return l.stream().map(String::valueOf).toList();
    }
    if (valor instanceof String s && !s.isBlank()) {
      return List.of(s.trim().split("\\s+"));
    }
    return List.of();
  }
}
