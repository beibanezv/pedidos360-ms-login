# ms-login — Pedidos360

Microservicio de sesión: su único rol es **decodificar el JWT de Azure AD**
(ya validado por el resource server contra el JWKS de Azure) y devolver los
datos del usuario: `oid`, nombre, correo, roles (Cliente/Admin) y scopes.

Es el tercero de los microservicios del enunciado (Login, Productos, Carrito).
No usa base de datos: la identidad vive en el token, emitido por Azure AD IDaaS.

## Requisitos
- Java 17, Maven 3.9+

## Desarrollo local

```bash
mvn spring-boot:run           # app en http://localhost:8083
```

Variables opcionales:
- `AZURE_TENANT_JWKS_URI` — issuer de Azure AD. Ej: `https://login.microsoftonline.com/<tenant-id>/v2.0`
  Si no se define, el decoder es perezoso (sin discovery al arrancar) y `GET /login/me`
  sin token responde 401 igual.

## Endpoints
- `GET /login/me` — requiere JWT (`Authorization: Bearer ...`). Respuesta:

```json
{
  "oid": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "nombre": "Ana Soto",
  "correo": "ana.soto@ejemplo.cl",
  "roles": ["Cliente"],
  "scopes": ["api://<clientId>/Carrito.ReadWrite"]
}
```

La ruta vive bajo `/login/` para que el API Gateway enrute `/login/*` hacia
este servicio sin reescritura de path.

## Producción (EC2)
Activa perfil `prod`:
```bash
SPRING_PROFILES_ACTIVE=prod \
AZURE_TENANT_JWKS_URI=https://login.microsoftonline.com/<tenant-id>/v2.0 \
java -jar target/ms-login-*.jar
```

## Tests
```bash
mvn test
```
