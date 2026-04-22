package universidad.notificaciones.enums;

/**
 * Tipos de situación que generan notificaciones en la universidad.
 * Para agregar un nuevo tipo, basta con añadir un valor aquí.
 */
public enum TipoNotificacion {
    PUBLICACION_CALIFICACIONES("Publicación de calificaciones"),
    RECORDATORIO_PAGO("Recordatorio de pago de matrícula"),
    CANCELACION_CLASE("Aviso de cancelación de clase"),
    CONFIRMACION_EVENTO("Confirmación de inscripción a evento académico");

    private final String descripcion;

    TipoNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
