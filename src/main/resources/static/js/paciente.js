/**
 * RF-PAC-01: Calculo automatico de edad a partir de fecha de nacimiento
 */
document.addEventListener('DOMContentLoaded', function () {
    const fechaInput = document.getElementById('fechaNacimiento');
    const edadInput = document.getElementById('edad');
    const edadBadge = document.getElementById('edadBadge');

    if (fechaInput && edadInput) {
        fechaInput.addEventListener('change', calcularEdad);
        fechaInput.addEventListener('input', calcularEdad);
        // Calcular al cargar si ya hay fecha
        if (fechaInput.value) calcularEdad();
    }

    function calcularEdad() {
        const fechaNac = new Date(fechaInput.value);
        if (!fechaInput.value || isNaN(fechaNac.getTime())) {
            edadInput.value = '';
            if (edadBadge) edadBadge.textContent = '-';
            return;
        }
        const hoy = new Date();
        let edad = hoy.getFullYear() - fechaNac.getFullYear();
        const mes = hoy.getMonth() - fechaNac.getMonth();
        if (mes < 0 || (mes === 0 && hoy.getDate() < fechaNac.getDate())) {
            edad--;
        }
        if (edad < 0) edad = 0;
        edadInput.value = edad;
        if (edadBadge) edadBadge.textContent = edad + ' años';
    }
});
