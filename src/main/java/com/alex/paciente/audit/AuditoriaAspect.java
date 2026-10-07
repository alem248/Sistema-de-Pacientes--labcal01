package com.alex.paciente.audit;

import com.alex.paciente.entity.Auditoria;
import com.alex.paciente.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspecto AOP de auditoría (Pregunta 2 - Evaluación 02).
 * Intercepta los métodos anotados con @Auditable y registra en la tabla
 * auditoria: usuario autenticado, fecha/hora, operación, entidad e id afectado.
 *
 * El registro se hace DESPUÉS de la ejecución exitosa (@AfterReturning):
 * si el método lanza excepción, la operación no se considera realizada.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditoriaAspect {

    private final AuditoriaRepository auditoriaRepository;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "resultado")
    public void auditar(JoinPoint joinPoint, Auditable auditable, Object resultado) {
        String usuario = usuarioActual();
        try {
            String entidadId = (auditable.idArgIndex() >= 0)
                    ? extraerIdDeArgumento(joinPoint.getArgs(), auditable.idArgIndex())
                    : extraerId(resultado);
            auditoriaRepository.save(Auditoria.builder()
                    .usuario(usuario)
                    .operacion(auditable.operacion())
                    .entidad(auditable.entidad())
                    .entidadId(entidadId)
                    .detalle("Operación " + auditable.operacion() + " ejecutada correctamente")
                    .build());
        } catch (Exception e) {
            // La auditoría NUNCA debe interrumpir la operación de negocio
            log.error("[AUDITORIA] No se pudo registrar la auditoría: {}", e.getMessage());
        }
        log.info("[AUDITORIA] {} sobre {} por {}", auditable.operacion(), auditable.entidad(), usuario);
    }

    private String extraerIdDeArgumento(Object[] args, int index) {
        if (args == null || index >= args.length || args[index] == null) return null;
        return String.valueOf(args[index]);
    }

    /** Usuario autenticado; "sistema" si no hay contexto de seguridad (seeder, tests). */
    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "sistema";
        }
        return auth.getName();
    }

    /** Extrae el id del objeto retornado vía reflexión (getId()). */
    private String extraerId(Object resultado) {
        if (resultado == null) return null;
        if (resultado instanceof Number n) return n.toString();
        try {
            Method getId = resultado.getClass().getMethod("getId");
            Object id = getId.invoke(resultado);
            return id == null ? null : id.toString();
        } catch (NoSuchMethodException e) {
            // DTO record: intentar acceso al componente "id"
            try {
                Method id = resultado.getClass().getMethod("id");
                Object val = id.invoke(resultado);
                return val == null ? null : val.toString();
            } catch (Exception ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
