-- ============================================================
-- Evaluacion 02 - Modulo de Citas (rol RECEPCIONISTA)
-- Relacion: Paciente 1 --- N Cita  (fk_cita_paciente)
-- Base de datos: sistema_pacientes (MySQL / XAMPP)
-- ============================================================

CREATE TABLE IF NOT EXISTS cita (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    paciente_id    BIGINT       NOT NULL,
    fecha_hora     DATETIME     NOT NULL,
    motivo         VARCHAR(255) NOT NULL,
    medico         VARCHAR(150) NULL,
    estado         VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    fecha_registro DATETIME     NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_cita_paciente FOREIGN KEY (paciente_id) REFERENCES paciente (id),
    CONSTRAINT ck_cita_estado  CHECK (estado IN ('PENDIENTE', 'ATENDIDA', 'CANCELADA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_cita_paciente ON cita (paciente_id);
CREATE INDEX idx_cita_fecha    ON cita (fecha_hora);
