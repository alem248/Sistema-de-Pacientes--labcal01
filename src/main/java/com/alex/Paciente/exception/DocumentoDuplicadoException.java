package com.alex.Paciente.exception;

/**
 * RF-PAC-02: Excepcion para documento duplicado
 */
public class DocumentoDuplicadoException extends RuntimeException {
    public DocumentoDuplicadoException(String message) {
        super(message);
    }
}
