# Sistema de Pacientes - LabCal01

Sistema de **Registro de Pacientes** desarrollado con **IntelliJ IDEA**, **XAMPP (MySQL)** y **SQLyog**, siguiendo los requerimientos RF-PAC-03 y RF-PAC-04 y los módulos extendidos del sistema hospitalario.

> Repositorio remoto: https://github.com/alem248/Sistema-de-Pacientes--labcal01

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
- **MySQL 8** (XAMPP) + **SQLyog** para administración
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
  │   ├── Paciente.java:18            # RF-PAC-03/04, edad calculada, fotoUrl
  │   ├── ContactoEmergencia.java:10
  │   ├── SeguroPaciente.java:10
  │   ├── Antecedente.java:10
  │   └── enums/ (8 enums)
  ├── repository/
  │   ├── PacienteRepository.java:12  # buscarGeneral, busqueda avanzada
  │   └── ...
  ├── service/
  │   ├── PacienteService.java        # Interfaz RF-PAC-03/04
  │   └── PacienteServiceImpl.java:43 # generarCodigoPaciente, evitar duplicados
  ├── controller/
  │   ├── PacienteController.java:32  # MVC + foto upload + AJAX verificar-documento
  │   └── HomeController.java
  └── config/WebConfig.java           # /uploads/** handler

# Paquete colaborador (compatibilidad):
src/main/java/com/alex/Paciente (uppercase - se migrará a lowercase en próximo refactor)
  └── exception/DocumentoDuplicadoException.java

src/main/resources/
  ├── application.properties
  ├── db/schema.sql                   # Script SQLyog principal (db_pacientes)
  ├── sql/pacientes.sql               # Script colaborador (sistema_pacientes)
  └── templates/paciente/ y pacientes/
      ├── lista.html
      ├── formulario.html
      └── detalle.html
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

# Ejecutar
.\mvnw.cmd spring-boot:run
# o Run desde IntelliJ IDEA

# Tests (si aplica)
.\mvnw.cmd test
```

Flujo de prueba:

1. `GET /pacientes/nuevo` -> registrar paciente DNI `12345678` -> código `PAC-000001` autogenerado
2. Intentar registrar mismo DNI -> error "ya está registrado"
3. `GET /pacientes?q=Juan` -> búsqueda
4. `GET /pacientes/1` -> detalle + agregar contacto principal + seguro SIS + antecedente alergia penicilina
5. `POST /pacientes/1/estado` -> cambiar a Fallecido -> conserva historial
6. Subir foto -> se guarda en `uploads/` y se muestra en lista/detalle

---

## 📄 Licencia

Proyecto académico LabCal01.
