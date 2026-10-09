-- ============================================================
-- Evaluacion 02 - Seguridad y Auditoria
-- Tablas: rol, usuario, auditoria (evidencia SQLyog / XAMPP)
-- Autor: Nikolai Suarez <nikolai.suarez@tecsup.edu.pe>
-- ============================================================
CREATE DATABASE IF NOT EXISTS sistema_pacientes CHARACTER SET utf8mb4;
USE sistema_pacientes;

-- ------------------------------------------------------------
-- Rol: ADMINISTRADOR, MEDICO, RECEPCIONISTA
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rol (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(30)  NOT NULL,
    descripcion    VARCHAR(200) NULL,
    activo         BIT(1)       NOT NULL DEFAULT b'1',
    fecha_creacion DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Usuario: relacion N->1 con Rol (fk_usuario_rol)
-- La clave se almacena con hash BCrypt
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)  NOT NULL,
    password        VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    correo          VARCHAR(150) NULL,
    activo          BIT(1)       NOT NULL DEFAULT b'1',
    rol_id          BIGINT       NOT NULL,
    fecha_creacion  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ultimo_acceso   DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol (id)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Auditoria: usuario, fecha/hora, operacion, entidad e id afectado
-- Se llena automaticamente desde el aspecto @Auditable (AOP)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auditoria (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    usuario    VARCHAR(50)  NOT NULL,
    fecha_hora DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    operacion  VARCHAR(20)  NOT NULL,
    entidad    VARCHAR(50)  NOT NULL,
    entidad_id VARCHAR(50)  NULL,
    detalle    VARCHAR(500) NULL,
    PRIMARY KEY (id),
    INDEX idx_auditoria_entidad (entidad, entidad_id),
    INDEX idx_auditoria_fecha (fecha_hora),
    INDEX idx_auditoria_usuario (usuario)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Datos de demostracion (roles minimos). Las claves BCrypt de
-- los usuarios demo las inserta DatosSeguridadSeeder al arrancar.
-- ------------------------------------------------------------
INSERT INTO rol (nombre, descripcion, activo)
SELECT 'ADMINISTRADOR', 'Acceso total: usuarios, roles, auditoria y modulos', b'1'
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ADMINISTRADOR');

INSERT INTO rol (nombre, descripcion, activo)
SELECT 'MEDICO', 'Acceso a pacientes e historias clinicas', b'1'
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'MEDICO');

INSERT INTO rol (nombre, descripcion, activo)
SELECT 'RECEPCIONISTA', 'Acceso a pacientes y citas', b'1'
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'RECEPCIONISTA');
