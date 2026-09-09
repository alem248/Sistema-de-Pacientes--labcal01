# Guía de Uso - IntelliJ IDEA + XAMPP + SQLyog

## IntelliJ IDEA

1. **Abrir proyecto**: `File > Open > seleccionar carpeta paciente/paciente/pom.xml`
2. **SDK**: Configurar `File > Project Structure > SDK 21`
3. **Maven**: View > Tool Windows > Maven > Reload Project
4. **Run**: Click derecho en `src/main/java/com/alex/paciente/PacienteApplication.java:7` > Run
5. **Debug**: Breakpoints en `PacienteServiceImpl.registrarPaciente():54`

## XAMPP

1. Ejecutar `C:\xampp\xampp-control.exe` como administrador
2. Start MySQL y Apache
3. Ver logs: `C:\xampp\mysql\data\` y `C:\xampp\apache\logs\`
4. Si puerto 3306 ocupado: `Config > my.ini > port=3307` y actualizar `application.properties`

## SQLyog

1. **Nueva conexión**: 
   - Host: localhost
   - User: root
   - Password: (vacío)
   - Port: 3306
   - Database: db_pacientes
2. **Importar script**: File > Open > `src/main/resources/db/schema.sql` > F5 ejecutar
3. **Verificar**: 
   ```sql
   SHOW TABLES;
   SELECT * FROM paciente;
   DESCRIBE paciente;
   ```
4. **Backup**: Tools > Backup Database as SQL Dump

## Flujo recomendado para el equipo

1. Clonar: `git clone https://github.com/alem248/Sistema-de-Pacientes--labcal01.git`
2. Crear rama: `git checkout -b feat/nombre-feature`
3. Desarrollar en IntelliJ
4. Probar en `http://localhost:8080/pacientes` y verificar en SQLyog
5. Commit descriptivo: `git commit -m "feat: descripción clara"`
6. Push: `git push origin feat/nombre-feature`
7. Pull Request en GitHub

## Troubleshooting

- **Access denied for user 'root'**: Reset password en `phpMyAdmin > Cuentas > root`
- **Communications link failure**: Asegurar XAMPP MySQL está en verde
- **Foto no se muestra**: Verificar `paciente.upload-dir=uploads` existe y `WebConfig.java:12`
- **DNI duplicado**: Ver `PacienteServiceImpl.registrarPaciente():57` lanza IllegalArgumentException
