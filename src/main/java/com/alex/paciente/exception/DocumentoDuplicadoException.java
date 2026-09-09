package com.alex.paciente.exception;

/**
 * RF-PAC-02 / RF-PAC-04: Excepción para documento duplicado
 * Integrado desde colaboración remoto (com.alex.Paciente -> com.alex.paciente)
 */
public class DocumentoDuplicadoException extends RuntimeException {
    public DocumentoDuplicadoException(String message) {
        super(message);
    }
}
