# Evaluacion 02 - Usuarios, Roles, Auditoria y Seguridad

Autor: **Nikolai Suarez** - `nikolai.suarez@tecsup.edu.pe`

Complementa la primera parte (RF-PAC-01 registro de pacientes y RF-PAC-02
validacion de documento duplicado), que se conserva sin cambios.

## 1. Modelo JPA y relaciones

| Relacion | Cardinalidad | Implementacion |
|----------|--------------|----------------|
| Usuario - Rol | N a 1 | `Usuario.rol` con `@ManyToOne` + `@JoinColumn(rol_id)` y FK `fk_usuario_rol` |
| Rol - Usuario | 1 a N | lado inverso mediante `countByRolId` al eliminar |
| Paciente - HistoriaClinica | 1 a N | `HistoriaClinica.paciente` `@ManyToOne` |
| Paciente - Cita | 1 a N | `Cita.paciente` `@ManyToOne` |

Integridad referencial garantizada con `@ForeignKey` y claves unicas
(`uk_usuario_username`, `uk_rol_nombre`).

## 2. Auditoria

`@Auditable(operacion, entidad)` marca los metodos de servicio y el aspecto
`AuditoriaAspect` (`@AfterReturning`) registra automaticamente:

* usuario autenticado (o `sistema` si no hay sesion)
* fecha/hora
* operacion: `REGISTRO`, `MODIFICACION`, `ELIMINACION`, `CONSULTA`
* entidad e identificador del registro afectado
* detalle

Bitacora consultable por el ADMINISTRADOR en `GET /auditoria`.

## 3. Usuarios y roles (backend)

`UsuarioService` y `RolService` exponen el CRUD con `@Auditable`.
Las claves se almacenan con `BCryptPasswordEncoder` (nunca en texto plano).
Un usuario desactivado no puede iniciar sesion (`enabled = false` en
`UsuarioDetailsService`).

`RolService.eliminar` impide borrar un rol con usuarios asignados.

## 4. Frontend

| Vista | URL |
|-------|-----|
| Listado de usuarios | `GET /usuarios` |
| Nuevo / editar usuario | `GET /usuarios/nuevo`, `GET /usuarios/{id}/editar` |
| Activar / desactivar | `POST /usuarios/{id}/estado` |
| Eliminar usuario | `POST /usuarios/{id}/eliminar` |
| Listado de roles | `GET /roles` |
| Nuevo / editar rol | `GET /roles/nuevo`, `GET /roles/{id}/editar` |
| Activar / desactivar rol | `POST /roles/{id}/estado` |
| Eliminar rol | `POST /roles/{id}/eliminar` |

Todo se gestiona desde la aplicacion; no se editan filas directamente en la BD.

## 5. Spring Security

Roles minimos sembrados por `DatosSeguridadSeeder` (idempotente):

| Usuario | Clave | Rol |
|---------|-------|-----|
| admin | admin123 | ADMINISTRADOR |
| medico | medico123 | MEDICO |
| recepcionista | recep123 | RECEPCIONISTA |

Matriz de acceso:

| Recurso | ADMINISTRADOR | MEDICO | RECEPCIONISTA |
|---------|:-------------:|:------:|:-------------:|
| Usuarios y roles (`/usuarios/**`, `/roles/**`) | X | | |
| Auditoria (`/auditoria/**`) | X | | |
| Pacientes (`/pacientes/**`) | X | X | X |
| Historias clinicas | X | X | |

El control se aplica en dos capas: reglas de URL en `SecurityFilterChain`
y `@PreAuthorize` en los controladores.

## 6. Pruebas realizadas

* Registro de paciente (RF-PAC-01): `201`.
* Documento duplicado (RF-PAC-02): `409`.
* CRUD usuarios y roles desde las vistas.
* Usuario desactivado no puede iniciar sesion.
* Acceso denegado (`403`) a `/usuarios` con rol MEDICO o RECEPCIONISTA.
* Bitacora con REGISTRO, MODIFICACION y ELIMINACION.
* Pruebas unitarias: `UsuarioServiceTest` y `RolServiceTest` (Mockito).

Evidencia SQL de las tablas: `sql/seguridad_auditoria.sql`.
