package universidad.notificaciones.enums;

/**
 * Estados posibles de una notificación durante su ciclo de vida.
 */
public enum EstadoNotificacion {
    PENDIENTE("Pendiente de envío"),
    ENVIADA("Enviada exitosamente"),
    FALLIDA("Error en el envío"),
    CANCELADA("Cancelada por el sistema");

    private final String descripcion;

    EstadoNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return name() + " — " + descripcion;
    }
}
