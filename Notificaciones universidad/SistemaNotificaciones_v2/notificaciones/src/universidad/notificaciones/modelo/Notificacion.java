package universidad.notificaciones.modelo;

import universidad.notificaciones.enums.EstadoNotificacion;
import universidad.notificaciones.enums.TipoNotificacion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Clase abstracta base para todas las notificaciones del sistema universitario.
 *
 * <p>Encapsula los datos comunes (código, destinatario, mensaje, tipo, estado)
 * y delega el mecanismo concreto de envío a cada subclase mediante {@link #enviar()}.
 *
 * <p><b>Extensibilidad:</b> para agregar un nuevo canal (WhatsApp, Teams, etc.)
 * basta con crear una subclase e implementar {@code enviar()} sin modificar
 * esta clase ni ninguna otra existente.
 *
 * <p><b>Ciclo de estado válido:</b>
 * <pre>
 *   PENDIENTE → ENVIADA
 *   PENDIENTE → FALLIDA
 *   PENDIENTE → CANCELADA
 *   FALLIDA   → PENDIENTE  (reintento)
 * </pre>
 */
public abstract class Notificacion {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ── Atributos comunes (todos final salvo estado) ──────────
    private final String             codigo;
    private final Usuario            destinatario;
    private final String             mensaje;
    private final TipoNotificacion   tipo;
    private final LocalDateTime      fechaCreacion;

    // Estado mutable: refleja el resultado del envío
    private EstadoNotificacion estado;

    // Fecha efectiva de envío: null hasta que enviar() se complete
    private LocalDateTime fechaEnvio;

    // ── Constructor ───────────────────────────────────────────
    /**
     * @param codigo        Identificador único de la notificación.
     * @param destinatario  Usuario que recibirá la notificación.
     * @param mensaje       Cuerpo del mensaje. No puede ser vacío.
     * @param tipo          Situación que origina la notificación.
     */
    protected Notificacion(String codigo,
                           Usuario destinatario,
                           String mensaje,
                           TipoNotificacion tipo) {

        this.codigo       = Objects.requireNonNull(codigo,       "codigo no puede ser null")
                                   .strip();
        this.destinatario = Objects.requireNonNull(destinatario, "destinatario no puede ser null");
        this.tipo         = Objects.requireNonNull(tipo,         "tipo no puede ser null");

        if (codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo no puede estar vacio.");
        }

        String mensajeLimpio = (mensaje != null) ? mensaje.strip() : "";
        if (mensajeLimpio.isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacio.");
        }
        this.mensaje       = mensajeLimpio;
        this.fechaCreacion = LocalDateTime.now();
        this.estado        = EstadoNotificacion.PENDIENTE;
        this.fechaEnvio    = null;
    }

    // ── Método abstracto ──────────────────────────────────────
    /**
     * Realiza el envío concreto según el canal.
     * Las subclases <b>deben</b> llamar a {@link #registrarEnvio(EstadoNotificacion)}
     * al finalizar para actualizar estado y fecha de envío.
     */
    public abstract void enviar();

    // ── Registro de resultado de envío ────────────────────────
    /**
     * Actualiza el estado tras un intento de envío y registra la fecha.
     * Solo acepta transiciones válidas; lanza excepción si la transición
     * no está permitida.
     *
     * @param nuevoEstado Estado resultante: ENVIADA, FALLIDA o CANCELADA.
     */
    protected void registrarEnvio(EstadoNotificacion nuevoEstado) {
        Objects.requireNonNull(nuevoEstado, "nuevoEstado no puede ser null");
        validarTransicion(this.estado, nuevoEstado);
        this.estado    = nuevoEstado;
        this.fechaEnvio = LocalDateTime.now();
    }

    /**
     * Permite reencolar una notificación FALLIDA para reintento.
     */
    public void reintentar() {
        if (this.estado != EstadoNotificacion.FALLIDA) {
            throw new IllegalStateException(
                "Solo se puede reintentar una notificacion en estado FALLIDA. Estado actual: " + estado);
        }
        this.estado    = EstadoNotificacion.PENDIENTE;
        this.fechaEnvio = null;
    }

    // ── Validación de transición de estado ────────────────────
    private static void validarTransicion(EstadoNotificacion actual,
                                          EstadoNotificacion siguiente) {
        boolean valida = switch (actual) {
            case PENDIENTE -> siguiente == EstadoNotificacion.ENVIADA
                           || siguiente == EstadoNotificacion.FALLIDA
                           || siguiente == EstadoNotificacion.CANCELADA;
            case FALLIDA   -> siguiente == EstadoNotificacion.PENDIENTE; // reintento
            default        -> false; // ENVIADA y CANCELADA son estados terminales
        };
        if (!valida) {
            throw new IllegalStateException(
                "Transicion de estado no permitida: " + actual + " -> " + siguiente);
        }
    }

    // ── Resumen legible ───────────────────────────────────────
    /**
     * Devuelve una línea descriptiva de la notificación con todos sus datos clave.
     */
    public String getInfo() {
        String fechaStr = (fechaEnvio != null)
                ? fechaEnvio.format(FORMATO_FECHA)
                : "Pendiente de envio";
        return String.format(
            "[%-8s] %-36s | %-20s | Fecha: %-19s | %s",
            codigo,
            tipo.getDescripcion(),
            destinatario.getNombre(),
            fechaStr,
            estado.name()
        );
    }

    // ── Getters públicos ─────────────────────────────────────
    public String             getCodigo()       { return codigo; }
    public Usuario            getDestinatario() { return destinatario; }
    public String             getMensaje()      { return mensaje; }
    public TipoNotificacion   getTipo()         { return tipo; }
    public EstadoNotificacion getEstado()       { return estado; }
    public LocalDateTime      getFechaCreacion(){ return fechaCreacion; }

    /** @return fecha efectiva de envío, o null si aún no se ha enviado. */
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }

    @Override
    public String toString() {
        return getInfo();
    }
}
