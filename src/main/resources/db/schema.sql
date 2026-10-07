-- ============================================
-- Sistema de Pacientes - labcal01
-- Script SQL para XAMPP / SQLyog
-- BD: db_pacientes
-- Ejecutar en SQLyog o phpMyAdmin (http://localhost/phpmyadmin)
-- ============================================

CREATE DATABASE IF NOT EXISTS db_pacientes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_pacientes;

-- ------------------------------
-- Tabla: paciente
-- RF-PAC-03: codigo unico autogenerado (PAC-000001)
-- RF-PAC-04: informacion personal y contacto + ubicación
-- ------------------------------
CREATE TABLE IF NOT EXISTS paciente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_paciente VARCHAR(20) NOT NULL UNIQUE,
    tipo_documento VARCHAR(20) NOT NULL, -- DNI, CE, PASAPORTE, OTRO
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    sexo VARCHAR(20) NOT NULL, -- MASCULINO, FEMENINO, OTRO
    estado_civil VARCHAR(30) NOT NULL, -- SOLTERO, CASADO, VIUDO, DIVORCIADO, CONVIVIENTE
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(150),
    direccion VARCHAR(255) NOT NULL,
    distrito VARCHAR(100) NOT NULL,
    provincia VARCHAR(100) NOT NULL,
    departamento VARCHAR(100) NOT NULL,
    ocupacion VARCHAR(100),
    tipo_sangre VARCHAR(5), -- A+, A-, B+, B-, AB+, AB-, O+, O-
    estado_registro VARCHAR(20) NOT NULL DEFAULT 'ACTIVO', -- ACTIVO, INACTIVO, FALLECIDO
    foto_url VARCHAR(255),
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME,
    INDEX idx_paciente_nombres (nombres),
    INDEX idx_paciente_apellidos (apellido_paterno, apellido_materno),
    INDEX idx_paciente_telefono (telefono)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------
-- Tabla: contacto_emergencia
-- Uno a muchos: un paciente puede tener varios contactos
-- ------------------------------
CREATE TABLE IF NOT EXISTS contacto_emergencia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    parentesco VARCHAR(50) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    direccion VARCHAR(255),
    correo VARCHAR(150),
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (paciente_id) REFERENCES paciente(id) ON DELETE CASCADE,
    INDEX idx_contacto_paciente (paciente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------
-- Tabla: seguro_paciente
-- ------------------------------
CREATE TABLE IF NOT EXISTS seguro_paciente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    tipo_seguro VARCHAR(30) NOT NULL, -- SIS, ESSALUD, PRIVADO, EPS, OTRO
    empresa_aseguradora VARCHAR(150) NOT NULL,
    numero_poliza VARCHAR(50),
    numero_afiliacion VARCHAR(50),
    fecha_inicio DATE,
    fecha_vencimiento DATE,
    estado_cobertura VARCHAR(20) NOT NULL DEFAULT 'ACTIVO', -- ACTIVO, VENCIDO, SUSPENDIDO, INACTIVO
    FOREIGN KEY (paciente_id) REFERENCES paciente(id) ON DELETE CASCADE,
    INDEX idx_seguro_paciente (paciente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------
-- Tabla: antecedente
-- Incluye: personales, familiares, alergias
-- ------------------------------
CREATE TABLE IF NOT EXISTS antecedente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    categoria VARCHAR(30) NOT NULL, -- PERSONAL, FAMILIAR, ALERGIA
    tipo VARCHAR(50) NOT NULL, -- ENFERMEDAD_PREVIA, CIRUGIA, HOSPITALIZACION, ENFERMEDAD_CRONICA, DIABETES, HIPERTENSION, CARDIOVASCULAR, HEREDITARIA, MEDICAMENTO, ALIMENTO, OTRA
    descripcion TEXT NOT NULL,
    reaccion VARCHAR(255), -- solo para alergias
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paciente_id) REFERENCES paciente(id) ON DELETE CASCADE,
    INDEX idx_antecedente_paciente (paciente_id),
    INDEX idx_antecedente_categoria (categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------
-- Datos de prueba (opcional)
-- ------------------------------
-- INSERT INTO paciente (codigo_paciente, tipo_documento, numero_documento, nombres, apellido_paterno, apellido_materno, fecha_nacimiento, sexo, estado_civil, telefono, correo, direccion, distrito, provincia, departamento, ocupacion, tipo_sangre, estado_registro)
-- VALUES ('PAC-000001', 'DNI', '12345678', 'Juan Carlos', 'Perez', 'Gomez', '1990-05-15', 'MASCULINO', 'SOLTERO', '987654321', 'juan.perez@example.com', 'Av. Las Flores 123', 'Trujillo', 'Trujillo', 'La Libertad', 'Ingeniero', 'O+', 'ACTIVO');

-- ============================================
-- EVALUACION 02 - Seguridad y Auditoria
-- Nota: JPA (ddl-auto=update) crea estas tablas automaticamente
-- al arrancar la app; este script sirve como evidencia/referencia.
-- ============================================

-- ------------------------------
-- Tabla: rol (Pregunta 3 - roles del sistema)
-- ------------------------------
CREATE TABLE IF NOT EXISTS rol (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE, -- ADMINISTRADOR, MEDICO, RECEPCIONISTA
    descripcion VARCHAR(200),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------
-- Tabla: usuario (relacion @ManyToOne con rol)
-- ------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL, -- hash BCrypt, nunca texto plano
    nombre_completo VARCHAR(150) NOT NULL,
    correo VARCHAR(150),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    rol_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso DATETIME,
    FOREIGN KEY (rol_id) REFERENCES rol(id),
    INDEX idx_usuario_rol (rol_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------
-- Tabla: auditoria (Pregunta 2 - bitacora de operaciones)
-- Registrada automaticamente por el aspecto AOP (@Auditable)
-- ------------------------------
CREATE TABLE IF NOT EXISTS auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL,       -- usuario autenticado que realizo la operacion
    fecha_hora DATETIME NOT NULL,       -- fecha y hora exacta
    operacion VARCHAR(20) NOT NULL,     -- REGISTRO, MODIFICACION, ELIMINACION, CONSULTA
    entidad VARCHAR(50) NOT NULL,       -- Paciente, Usuario, Rol, ...
    entidad_id VARCHAR(50),             -- identificador del registro afectado
    detalle VARCHAR(500),
    INDEX idx_auditoria_entidad (entidad, entidad_id),
    INDEX idx_auditoria_fecha (fecha_hora),
    INDEX idx_auditoria_usuario (usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------
-- Datos iniciales de seguridad
-- (los crea automaticamente DatosSeguridadSeeder con BCrypt;
--  estos INSERTs son referencia para carga manual)
-- ------------------------------
-- INSERT INTO rol (nombre, descripcion) VALUES
--   ('ADMINISTRADOR', 'Acceso total: usuarios, roles, auditoria y modulos'),
--   ('MEDICO', 'Pacientes e historias clinicas'),
--   ('RECEPCIONISTA', 'Pacientes y citas');
-- Usuarios demo: admin/admin123, medico/medico123, recepcionista/recep123
