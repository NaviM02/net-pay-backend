# Integración frontend: reportes XLS y login con 2FA por correo

## Reportes

Los siguientes endpoints requieren JWT y el permiso `reports:export`:

| Método | Endpoint | Parámetros |
| --- | --- | --- |
| GET | `/api/reports/collections/export` | `format=xls` (predeterminado) o `pdf`; `fromDate`, `toDate` (`YYYY-MM-DD`), `collectorLimit` |
| GET | `/api/reports/overdue-clients/export` | `format=xls` (predeterminado) o `pdf`; los mismos filtros de la consulta de clientes en mora |

La descarga Excel es un **archivo binario XLS real**, con extensión `.xls` y
`Content-Type: application/vnd.ms-excel`. Los importes y cantidades son celdas
numéricas. Los reportes grandes se dividen en hojas por el límite de filas de XLS.
El frontend debe usar `responseType: 'blob'`. El nombre viene en
`Content-Disposition`, ahora expuesto por CORS.

`format=csv` devuelve HTTP 400 con `unsupported_report_format`. Actualizar las
opciones de exportación del frontend a XLS/PDF. La importación CSV sigue disponible.

## Preferencia de doble verificación

El campo JSON `twoFactorEnabled` está disponible en:

- `POST /api/users`: creación (permiso `users:create`); predeterminado `false`.
- `PUT /api/users/{hashId}`: edición (permiso `users:update`). Omitirlo o enviar
  `null` conserva el valor; `false` desactiva y `true` activa.
- `PUT /api/users/me`: el usuario autenticado puede modificar su preferencia.
- Respuestas de usuarios, incluido `GET /api/users/me`: devuelven el booleano.

Ejemplo de creación:

```json
{
  "fullName": "María Pérez",
  "email": "maria@example.com",
  "password": "contraseña-del-usuario",
  "status": 49,
  "twoFactorEnabled": true
}
```

Ejemplo de preferencia desde el perfil:

```http
PUT /api/users/me
Authorization: Bearer <JWT>
Content-Type: application/json

{"twoFactorEnabled":true}
```

La preferencia se aplica a los próximos inicios de sesión. No cierra sesiones ya abiertas.

## Login

### 1. Validar correo y contraseña

Endpoint público: `POST /api/auth/login`. El body existente se mantiene:

```json
{
  "email": "maria@example.com",
  "password": "contraseña-del-usuario",
  "fingerprint": "identificador-del-dispositivo"
}
```

**Sin 2FA**, HTTP 200:

```json
{
  "authc": "<JWT>",
  "twoFactorRequired": false,
  "challengeId": null,
  "expiresAt": null
}
```

También se devuelve `Authorization: Bearer <JWT>`, como antes.

**Con 2FA**, HTTP 200 y envío de un código de seis dígitos al correo del usuario:

```json
{
  "authc": null,
  "twoFactorRequired": true,
  "challengeId": "<UUID-del-desafío>",
  "expiresAt": "2026-10-26T18:05:00"
}
```

En este paso no se genera JWT ni se devuelve la cabecera Authorization, y no se
actualiza el último acceso. El frontend debe conservar `challengeId`, mostrar el
formulario del código y esperar la verificación antes de navegar a la aplicación.
`expiresAt` es fecha/hora local del servidor, sin zona; el servidor valida la expiración.

### 2. Verificar el código

Endpoint público: `POST /api/auth/2fa/verify`.

```json
{
  "challengeId": "<UUID-del-desafío>",
  "code": "012345"
}
```

Enviar el código como **string** para conservar ceros iniciales. Si es válido,
HTTP 200 con la misma respuesta de login exitoso (`authc`,
`twoFactorRequired: false`) y cabecera Authorization. La sesión conserva el
fingerprint y User-Agent de la primera solicitud.

- Vencimiento predeterminado: 5 minutos.
- Máximo 5 intentos por desafío; los intentos fallidos persisten.
- Cada desafío se consume una sola vez, con bloqueo transaccional para evitar reutilización concurrente.
- El código se almacena como hash; no se devuelve por API.
- Se rechaza si el usuario está inactivo/eliminado, desactivó 2FA o cambió correo/contraseña.
- Para solicitar otro código, repetir el login con correo y contraseña. Hay una
  espera mínima de 60 segundos entre envíos por usuario.
- Código incorrecto, expirado, agotado, utilizado o desafío inválido: HTTP 401,
  cuerpo de texto `invalid_two_factor_code`. Volver al login cuando sea necesario.
- Envío demasiado frecuente: HTTP 400, `two_factor_retry_later`.
- Si falla el correo, no se crea una sesión y se revierte el desafío. Los errores
  de configuración/envío del servicio Gmail se propagan con HTTP 400.

## Configuración y base de datos

Flyway aplica `V21__email_two_factor_authentication.sql`: agrega
`app_user.two_factor_enabled` con valor inicial `false` y la tabla de desafíos.
Los usuarios existentes mantienen su login habitual hasta activar la bandera.

Reutiliza la configuración existente de Gmail API OAuth2:

```properties
GMAIL_CLIENT_ID=
GMAIL_CLIENT_SECRET=
GMAIL_REFRESH_TOKEN=
GMAIL_FROM_EMAIL=
TWO_FACTOR_EXPIRATION_MINUTES=5
```

Las pruebas de integración simulan el envío de correo; la entrega real necesita
las credenciales Gmail del entorno.
