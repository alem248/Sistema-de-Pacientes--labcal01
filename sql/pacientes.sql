-- =============================================
-- RF-PAC-01 y RF-PAC-02 - Modulo de Pacientes
-- Base de datos: sistema_pacientes
-- Herramientas: XAMPP + MySQL + SQLyog
-- =============================================

CREATE DATABASE IF NOT EXISTS sistema_pacientes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sistema_pacientes;

-- Elimina tabla si existe (solo para desarrollo)
DROP TABLE IF EXISTS pacientes;

CREATE TABLE pacientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(20) NOT NULL COMMENT 'DNI, CE, Pasaporte, etc.',
    numero_documento VARCHAR(20) NOT NULL UNIQUE COMMENT 'Unico - RF-PAC-02',
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    sexo VARCHAR(20) NOT NULL COMMENT 'Masculino, Femenino, Otro',
    estado_civil VARCHAR(30) COMMENT 'Soltero, Casado, Divorciado, Viudo, etc.',
    telefono VARCHAR(20),
    correo_electronico VARCHAR(100),
    direccion VARCHAR(255),
    distrito VARCHAR(100),
    provincia VARCHAR(100),
    departamento VARCHAR(100),
    ocupacion VARCHAR(100),
    tipo_sangre VARCHAR(10) COMMENT 'A+, A-, B+, B-, AB+, AB-, O+, O-',
    estado VARCHAR(20) NOT NULL DEFAULT 'Activo' COMMENT 'Activo, Inactivo, Fallecido',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_numero_documento UNIQUE (numero_documento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indices
CREATE INDEX idx_paciente_nombres ON pacientes(nombres, apellido_paterno, apellido_materno);
CREATE INDEX idx_paciente_tipo_documento ON pacientes(tipo_documento);

-- Datos de prueba (opcional)
-- INSERT INTO pacientes (tipo_documento, numero_documento, nombres, apellido_paterno, apellido_materno, fecha_nacimiento, sexo, estado_civil, telefono, correo_electronico, direccion, distrito, provincia, departamento, ocupacion, tipo_sangre, estado)
-- VALUES ('DNI', '12345678', 'Juan Carlos', 'Perez', 'Gomez', '1990-05-15', 'Masculino', 'Soltero', '987654321', 'juan.perez@email.com', 'Av. Lima 123', 'Miraflores', 'Lima', 'Lima', 'Ingeniero', 'O+', 'Activo');

SELECT 'Tabla pacientes creada correctamente' AS mensaje;
