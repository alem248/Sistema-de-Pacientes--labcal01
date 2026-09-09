# Sistema de Pacientes – Lab Calificado 01

## RF-PAC-01 y RF-PAC-02 – Módulo de Pacientes

### Descripción
Implementación exclusiva de **RF-PAC-01 (Registro de pacientes)** y **RF-PAC-02 (Validación del número de documento)** del Sistema de Pacientes.

### Tecnologías obligatorias
- **IntelliJ IDEA 2026.2.1** – desarrollo
- **MySQL 10.4.32-MariaDB** + **SQLyog** – administración BD
- **XAMPP** – servidor MySQL local (puerto 3306)
- **Spring Boot 4.1.1** – Java 21, Thymeleaf, Data JPA, Validation
- **Git/GitHub** – https://github.com/alem248/Sistema-de-Pacientes--labcal01.git

### Base de datos
- **Nombre BD:** `sistema_pacientes`
- **Tabla:** `pacientes` (ver `sql/pacientes.sql`)
- **Conexión XAMPP:** `jdbc:mysql://localhost:3306/sistema_pacientes` usuario `root` sin password
- **Script SQLyog:** Ejecutar `sql/pacientes.sql` en SQLyog conectado a localhost

```sql
CREATE DATABASE sistema_pacientes CHARACTER SET utf8mb4;
USE sistema_pacientes;
--Ver sql/pacientes.sql para DDL completo
```

### RF-PAC-01 – Campos del paciente
- **ID** auto-increment PK (`@GeneratedValue`)
- Tipo documento (DNI, CE, Pasaporte, Otro)
- Número documento (UNIQUE)
- Nombres, Apellido paterno, Apellido materno
- **Fecha nacimiento** + **Edad calculada automáticamente** (`Period.between`, `@Transient` + JS frontend)
- Sexo (Masculino/Femenino/Otro)
- Estado civil, Teléfono, Correo, Dirección, Distrito, Provincia, Departamento
- Ocupación, Tipo sangre (A+/A-/B+/B-/AB+/AB-/O+/O-)
- Estado (Activo/Inactivo/Fallecido) default Activo
- `fecha_registro` TIMESTAMP

Validaciones: `@NotBlank`, `@NotNull`, `@Past`, `@Email`, `@Pattern` + mensajes en formulario. Mensaje de éxito al registrar.

### RF-PAC-02 – Validación documento único
1. `PacienteRepository.existsByNumeroDocumento(String)` verifica en MySQL antes de guardar.
2. `PacienteService.registrarPaciente()` lanza `DocumentoDuplicadoException` si ya existe.
3. `PacienteController` muestra error: *“El número de documento ya está registrado”* y no permite insert.
4. BD garantiza unicidad con `UNIQUE (numero_documento)` + captura `DataIntegrityViolationException` (race condition).
5. Columna `numero_documento VARCHAR(20) UNIQUE`.

**Flujo:**
- Si documento existe → `errorDocumento` en formulario, no guarda.
- Si no existe → `pacienteRepository.save()` + flash `mensajeExito` con ID generado.

### Estructura del proyecto
```
src/main/java/com/alex/Paciente/
 ├── entity/Paciente.java
 ├── repository/PacienteRepository.java
 ├── service/PacienteService.java
 ├── exception/DocumentoDuplicadoException.java
 ├── controller/PacienteController.java
 └── config/WebConfig.java
src/main/resources/
 ├── application.properties (MySQL XAMPP)
 ├── templates/pacientes/formulario.html
 ├── templates/pacientes/lista.html
 ├── static/js/paciente.js (cálculo edad)
 └── static/css/style.css
sql/pacientes.sql
```

### Ejecución
1. Iniciar **XAMPP** → Start MySQL (puerto 3306).
2. En **SQLyog**: conectar `root@localhost` → crear BD `sistema_pacientes` o ejecutar `sql/pacientes.sql`.
3. **IntelliJ IDEA**: abrir proyecto `Paciente/Paciente` → esperar Maven sync → Run `PacienteApplication`.
4. Navegar a `http://localhost:8080/` o `http://localhost:8080/pacientes/nuevo`
5. Llenar formulario → **Edad** se calcula al elegir fecha → Guardar → ver mensaje éxito. Probar duplicar número documento → ver error RF-PAC-02.
6. Verificar en SQLyog: `SELECT * FROM pacientes;`

### Endpoints
- `GET /pacientes/nuevo` – formulario registro
- `POST /pacientes/guardar` – guarda con validación RF-PAC-02
- `GET /pacientes` – lista simple (solo visualización, no búsqueda/edición)
- `GET /` – redirect a `/pacientes/nuevo`

### Commits realizados (ejemplos solicitados)
- `agrego estructura del modulo pacientes`
- `creo tabla pacientes en mysql`
- `agrego campos del paciente`
- `conecto formulario con mysql`
- `agrego calculo de edad`
- `agrego formulario de registro`
- `agrego validacion del documento`
- `evito documentos duplicados`

### Autor
**Nikolai Suarez** – `nikolai.suarez@tecsup.edu.pe`

### Nota
Solo RF-PAC-01 y RF-PAC-02 implementados. No incluye búsqueda, edición, contactos emergencia, seguros ni antecedentes.
