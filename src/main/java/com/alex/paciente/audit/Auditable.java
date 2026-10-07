package com.alex.paciente.audit;

import java.lang.annotation.*;

/**
 * Marca un método de servicio para que el aspecto de auditoría
 * registre automáticamente la operación en la tabla auditoria.
 *
 * Ejemplo:
 * <pre>
 * {@code @Auditable(operacion = "REGISTRO", entidad = "Paciente")}
 * public Paciente registrarPaciente(Paciente paciente) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /** Operación realizada: REGISTRO, MODIFICACION, ELIMINACION, CONSULTA. */
    String operacion();

    /** Nombre de la entidad afectada (Paciente, Usuario, Rol, ...). */
    String entidad();

    /**
     * Índice (base 0) del argumento del método que contiene el identificador
     * del registro afectado. Si es negativo, el aspecto intentará extraer el id
     * del objeto retornado (getId()).
     */
    int idArgIndex() default -1;
}
