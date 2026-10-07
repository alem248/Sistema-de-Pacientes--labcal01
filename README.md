# Sistema de Pacientes - LabCal01

Sistema de **Registro de Pacientes** desarrollado con **IntelliJ IDEA**, **XAMPP (MySQL)** y **SQLyog**, siguiendo los requerimientos RF-PAC-03 y RF-PAC-04 y los módulos extendidos del sistema hospitalario.

> Repositorio remoto: https://github.com/alem248/Sistema-de-Pacientes--labcal01

---

## 🔐 Evaluación 02 — Seguridad, Auditoría y Pruebas

### RF-PAC-05: Búsqueda de pacientes
- **Por documento**: `GET /api/v1/pacientes/dni/{dni}` y `GET /pacientes?documento=...`
- **Por código**: `GET /api/v1/pacientes/codigo/{codigo}` y `GET /pacientes?codigo=...`
- **Por nombres**: `GET /api/v1/pacientes/buscar-nombres?nombres=...`
- **Por apellidos**: `GET /api/v1/pacientes/buscar-apellidos?apellidos=...`
- **General (un solo texto)**: `GET /api/v1/pacientes/buscar?q=...` (documento, código, nombres, apellidos, teléfono, historia)
- **Avanzada combinada**: `GET /api/v1/pacientes/search?dni=&codigo=&nombres=&apellidos=&telefono=&historia=`
- **Validación de parámetros**: mínimo 2 caracteres, formato de DNI alfanumérico 8-20, respuesta `400` con detalle del campo

### RF-PAC-06: Información completa del paciente
- **API REST**: `GET /api/v1/pacientes/{id}/completo` → `PacienteDetalleResponseDTO` con datos personales, edad calculada, contactos de emergencia, seguros, antecedentes, alergias (con reacción y severidad) e historia clínica
- **Vista web**: `GET /pacientes/{id}` muestra la ficha completa (incluye historia clínica y alergias)

### Seguridad (Spring Security 7)
- **Roles**: `ADMINISTRADOR`, `MEDICO`, `RECEPCIONISTA` (entidades `Rol` y `Usuario` con relación `@ManyToOne`)
- **Autenticación**: formulario de login (`/login`) + HTTP Basic para la API REST
- **Autorización en dos capas** (defensa en profundidad):
  1. Reglas de URL en `SecurityConfig` (matriz de acceso por rol)
  2. `@PreAuthorize` por método en los controladores REST
- **Redirección por rol** tras el login: ADMINISTRADOR → `/auditoria`, MÉDICO/RECEPCIONISTA → `/pacientes`
- **Contraseñas** con hash BCrypt; usuarios demo: `admin/admin123`, `medico/medico123`, `recepcionista/recep123`
- **Respuestas de error de seguridad en JSON** para la API: `401` (no autenticado) y `403` (sin permiso)
- **CSRF** activo para el módulo web (Thymeleaf inyecta el token automáticamente), desactivado para la API REST stateless

### Auditoría (AOP)
- Tabla `auditoria`: **usuario, fecha/hora, operación, entidad, id del registro afectado**
- Anotación `@Auditable` + aspecto `AuditoriaAspect` (`@AfterReturning`): registra automáticamente `REGISTRO`, `MODIFICACION`, `ELIMINACION` y `CONSULTA` sin tocar el código de negocio
- La bitácora es consultable en `GET /auditoria` (solo ADMINISTRADOR)
- La auditoría nunca interrumpe la operación de negocio (fallo aislado con log)

### Gestión de errores
- `GlobalExceptionHandler` centralizado: `400` validación (cuerpo y parámetros), `401`, `403`, `404`, `409` duplicados, `500` genérico — todos con cuerpo JSON estándar `{timestamp, status, error, message}`

### Pruebas automatizadas (30 tests, H2 en memoria)
| Tipo | Clase | Cobertura |
|------|-------|-----------|
| Repositorio | `PacienteRepositoryTest` | Búsquedas RF-PAC-05 (documento, código, nombres, apellidos, paginación, duplicados) |
| Servicio (Mockito) | `PacienteConsultaServiceTest` | Búsquedas, ficha completa RF-PAC-06, errores 404 |
| Controlador + seguridad | `PacienteRestControllerSecurityTest` | 401 sin login, 403 por rol, 200 autorizado, 400 validación, 404 |
| Integración | `AuditoriaIntegrationTest` | El aspecto AOP registra usuario/operación/entidad/id en la bitácora |

```bash
.\mvnw.cmd test        # ejecuta los 30 tests con H2 (no requiere MySQL)
```

---

## 🧩 Requerimientos Implementados

### Registro de Pacientes (RF-PAC-04)
- ID de Paciente como identificador único (PK autoincremental)
- Tipo de documento (DNI, CE, Pasaporte, Otro) + número documento **único**
- Datos personales: nombres, apellido paterno/materno, fecha nacimiento, **edad calculada automáticamente** (`Paciente.getEdad():40`), sexo, estado civil
- Ubicación: teléfono, correo, dirección exacta, distrito, provincia, departamento
- Ocupación, tipo de sangre (A+, A-, B+, ... O-)
- Estado registro: **Activo / Inactivo / Fallecido** (no borrado físico)
- Fotografía opcional (si hospital lo requiere)

### Identificación (RF-PAC-03)
- `RF-PAC-03: Código único autogenerado` -> formato `PAC-000001` incremental (`PacienteService.generarCodigoPaciente():43`)
- Búsqueda por DNI/documento, verificación previa `existsByNumeroDocumento`, evita duplicados (`PacienteServiceImpl.registrarPaciente():54`)
- Consulta rápida por código y detalle
- Colaborador: validación documento `pacienteRepository.existsByNumeroDocumento` en `PacienteController` + `DocumentoDuplicadoException`

### Contacto de Emergencia
- Uno o varios por paciente: nombre completo, parentesco, teléfono, dirección, correo, contacto principal (único principal por paciente)

### Datos del Seguro
- Tipo seguro (SIS, EsSalud, Privado, EPS), empresa, n° póliza, n° afiliación, fecha inicio/vencimiento, estado cobertura

### Antecedentes
- **Personales**: enfermedades previas, cirugías, hospitalizaciones, crónicas
- **Familiares**: diabetes, hipertensión, cardiovasculares, hereditarias
- **Alergias**: medicamentos, alimentos, otras + reacción presentada
- Clasificados por `CategoriaAntecedente` y `tipo` libre

### Búsqueda de Pacientes
- Rápida (`q`): DNI, código paciente (historia clínica), nombres, apellidos, teléfono (`PacienteRepository.buscarGeneral():16`)
- Avanzada: filtros por código, documento, nombres, apellidos, teléfono

### Edición
- Modificar datos personales, dirección, teléfono, correo, contactos, seguro, estado (`PacienteController.actualizar():85`)

### Estado del Paciente
- Activo / Inactivo / Fallecido – cambio lógico sin eliminar historial

---

## 🛠️ Stack

- **Java 21**, **Spring Boot 4.1.1**
- **Spring Data JPA**, **Hibernate**, **Thymeleaf**, **Validation**, **Lombok**
- **Spring Security 7** (autenticación, autorización por roles, BCrypt)
- **Spring AOP** (auditoría automática con `@Auditable`)
- **MySQL 8** (XAMPP) + **SQLyog** para administración · **H2** para pruebas
- **Maven**, **IntelliJ IDEA**

---

## 🚀 Configuración con XAMPP + SQLyog

### 1. XAMPP
1. Instalar XAMPP y arrancar **Apache** y **MySQL** desde el panel (`xampp-control.exe`)
2. Verificar MySQL en `localhost:3306`, usuario `root` sin contraseña (config por defecto)
3. Abrir `http://localhost/phpmyadmin` para verificar

### 2. SQLyog / phpMyAdmin
1. Conectar SQLyog: Host `localhost`, User `root`, Password ``, Port `3306`
2. Ejecutar `src/main/resources/db/schema.sql` (BD `db_pacientes`) o `sql/pacientes.sql` (colaborador usa `sistema_pacientes`):
   ```sql
   CREATE DATABASE db_pacientes CHARACTER SET utf8mb4;
   -- o CREATE DATABASE sistema_pacientes
   ```
   El script crea las 4 tablas: `paciente`, `contacto_emergencia`, `seguro_paciente`, `antecedente` con índices y FKs.

### 3. IntelliJ IDEA
1. Abrir proyecto `paciente/pom.xml` como **Maven Project**
2. Esperar indexado y descarga de dependencias (`/mvnw.cmd - Maven wrapper` incluido)
3. Configurar `application.properties` (ya listo para XAMPP):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/db_pacientes?...
   spring.datasource.username=root
   spring.datasource.password=
   spring.jpa.hibernate.ddl-auto=update
   ```
4. Run `PacienteApplication.java` (Spring Boot) o `Run > Run 'PacienteApplication'`
5. Abrir navegador: `http://localhost:8080/pacientes`

---

## 📁 Estructura

```
src/main/java/com/alex/paciente (paquete principal - lowercase)
  ├── entity/
  │   ├── Paciente.java               # RF-PAC-03/04, edad calculada, fotoUrl
  │   ├── ContactoEmergencia.java
  │   ├── SeguroPaciente.java
  │   ├── Antecedente.java
  │   ├── Rol.java                    # Pregunta 3: roles del sistema
  │   ├── Usuario.java                # Pregunta 3: @ManyToOne Rol, password BCrypt
  │   ├── Auditoria.java              # Pregunta 2: bitácora (usuario, fecha, op, entidad, id)
  │   └── enums/ (8 enums)
  ├── repository/
  │   ├── PacienteRepository.java     # buscarGeneral, busqueda avanzada, paginada
  │   ├── UsuarioRepository.java / RolRepository.java
  │   └── AuditoriaRepository.java
  ├── service/
  │   ├── PacienteService.java        # Interfaz RF-PAC-03/04
  │   ├── PacienteServiceImpl.java    # generarCodigoPaciente, evitar duplicados
  │   └── PacienteConsultaService.java # RF-PAC-05/06: búsquedas + ficha completa
  ├── controller/
  │   ├── PacienteController.java     # MVC + foto upload + AJAX verificar-documento
  │   ├── PacienteRestController.java # API REST RF-PAC-05/06 + @PreAuthorize
  │   ├── AuditoriaController.java    # Bitácora (solo ADMINISTRADOR)
  │   └── AuthController.java         # /login y /acceso-denegado
  ├── security/
  │   ├── SecurityConfig.java         # Matriz de acceso por rol + 401/403 JSON
  │   ├── UsuarioDetailsService.java  # UserDetailsService con ROLE_*
  │   ├── RolAuthenticationSuccessHandler.java # Redirección por rol
  │   └── DatosSeguridadSeeder.java   # Roles + usuarios demo (idempotente)
  ├── audit/
  │   ├── Auditable.java              # Anotación de auditoría
  │   └── AuditoriaAspect.java        # Aspecto AOP @AfterReturning
  ├── dto/
  │   ├── PacienteRequestDTO.java / PacienteResponseDTO.java
  │   └── PacienteDetalleResponseDTO.java  # RF-PAC-06: ficha completa
  ├── exception/
  │   └── GlobalExceptionHandler.java # 400/401/403/404/409/500 estándar
  └── config/
      ├── WebConfig.java              # /uploads/** handler
      └── SeguridadModelAdvice.java   # usuario/roles en las vistas Thymeleaf

src/main/resources/
  ├── application.properties
  ├── db/schema.sql                   # Script SQLyog (incluye rol, usuario, auditoria)
  ├── sql/pacientes.sql               # Script colaborador (sistema_pacientes)
  └── templates/
      ├── login.html / acceso-denegado.html
      ├── paciente/ (lista, formulario, detalle)
      └── auditoria/lista.html

src/test/java/com/alex/paciente/
  ├── repository/PacienteRepositoryTest.java        # @DataJpaTest + H2
  ├── service/PacienteConsultaServiceTest.java      # Mockito
  ├── controller/PacienteRestControllerSecurityTest.java # @WebMvcTest + @WithMockUser
  └── audit/AuditoriaIntegrationTest.java           # @SpringBootTest: AOP real
```

---

## 🔗 Repositorio Remoto (Trabajo Conjunto)

Este proyecto está vinculado al repo:

```bash
git remote add origin https://github.com/alem248/Sistema-de-Pacientes--labcal01.git
git branch -M main
git pull --allow-unrelated-histories  # para integrar trabajo colaborativo
git push -u origin main
```

Cada feature se commiteó de forma descriptiva:

- `feat: configurar datasource MySQL XAMPP/SQLyog y script schema.sql`
- `feat: entidades Paciente/Contacto/Seguro/Antecedente + enums y edad calculada`
- `feat(RF-PAC-03): generación automática código PAC-000001 y validación duplicados`
- `feat(RF-PAC-04): registro personal y contacto + ubicación completa`
- `feat: módulos contacto emergencia, seguros y antecedentes con relaciones`
- `feat: búsqueda por DNI/código/nombres/apellidos/teléfono/HC y listado`
- `feat: edición paciente y gestión estados Activo/Inactivo/Fallecido`
- `feat: controladores MVC Thymeleaf + upload foto + verificación AJAX`
- `merge: integrar trabajo colaborativo remoto (labcal01) con implementacion local`

Para trabajo conjunto: cada integrante hace `git pull`, crea rama feature, commitea y `git push`.

---

## ✅ Verificación

```bash
# Compilar (requiere Java 21)
.\mvnw.cmd clean compile

# Ejecutar (requiere MySQL/XAMPP con la BD creada por schema.sql)
.\mvnw.cmd spring-boot:run
# o Run desde IntelliJ IDEA

# Tests automatizados (H2 en memoria, no requiere MySQL)
.\mvnw.cmd test
```

Flujo de prueba:

1. `GET /pacientes/nuevo` -> registrar paciente DNI `12345678` -> código `PAC-000001` autogenerado
2. Intentar registrar mismo DNI -> error "ya está registrado"
3. `GET /pacientes?q=Juan` -> búsqueda (RF-PAC-05)
4. `GET /pacientes/1` -> detalle completo: contactos, seguros, antecedentes, historia clínica y alergias (RF-PAC-06)
5. `POST /pacientes/1/estado` -> cambiar a Fallecido -> conserva historial
6. Subir foto -> se guarda en `uploads/` y se muestra en lista/detalle

Flujo de seguridad y auditoría (Evaluación 02):

1. `GET /pacientes` sin sesión -> redirige a `/login`
2. Login `admin/admin123` -> redirige a `/auditoria` (bitácora)
3. Login `medico/medico123` -> redirige a `/pacientes`
4. `GET /api/v1/pacientes/buscar?q=perez` con HTTP Basic -> resultados paginados
5. `DELETE /api/v1/pacientes/1` con rol MÉDICO -> `403` JSON
6. Registrar/editar/eliminar pacientes -> aparece en `/auditoria` con usuario, fecha/hora, operación, entidad e id

---

## 📄 Licencia

Proyecto académico LabCal01.
