package universidad.notificaciones.gestor;

import universidad.notificaciones.enums.EstadoNotificacion;
import universidad.notificaciones.enums.TipoNotificacion;
import universidad.notificaciones.modelo.Notificacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Gestiona el ciclo de vida de las notificaciones universitarias.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Registrar notificaciones evitando duplicados de codigo.</li>
 *   <li>Enviar una notificacion especifica o todas las pendientes.</li>
 *   <li>Filtrar el historial por estado o tipo.</li>
 *   <li>Generar reportes de historial.</li>
 * </ul>
 *
 * <p>Esta clase trabaja exclusivamente con {@link Notificacion} (abstraccion),
 * sin conocer los canales concretos. Agregar un nuevo canal no requiere modificar
 * este gestor.
 *
 * <p><b>Nota de concurrencia:</b> esta implementacion no es thread-safe.
 * En un contexto multi-hilo se deberia usar {@code Collections.synchronizedList}
 * o {@code CopyOnWriteArrayList}.
 */
public class GestorNotificaciones {

    private final List<Notificacion> historial;
    private int enviosExitosos;
    private int enviosFallidos;

    public GestorNotificaciones() {
        this.historial      = new ArrayList<>();
        this.enviosExitosos = 0;
        this.enviosFallidos = 0;
    }

    // ── Registro ──────────────────────────────────────────────
    /**
     * Registra una notificacion en el sistema.
     *
     * @param notificacion Notificacion a registrar. No puede ser null.
     * @throws IllegalArgumentException si ya existe una notificacion con el mismo codigo.
     */
    public void registrar(Notificacion notificacion) {
        Objects.requireNonNull(notificacion, "La notificacion no puede ser null.");

        if (existeCodigo(notificacion.getCodigo())) {
            throw new IllegalArgumentException(
                "Ya existe una notificacion con el codigo: " + notificacion.getCodigo());
        }

        historial.add(notificacion);
        System.out.printf("[Gestor] Registrada %-8s -> %s%n",
                notificacion.getCodigo(),
                notificacion.getDestinatario().getNombre());
    }

    // ── Envío individual ─────────────────────────────────────
    /**
     * Envía la notificacion identificada por {@code codigo}.
     *
     * @param codigo Codigo de la notificacion a enviar.
     * @return {@code true} si se encontró y se intentó el envío; {@code false} si no existe.
     */
    public boolean enviar(String codigo) {
        Objects.requireNonNull(codigo, "El codigo no puede ser null.");

        Optional<Notificacion> encontrada = historial.stream()
                .filter(n -> n.getCodigo().equals(codigo))
                .findFirst();

        if (encontrada.isEmpty()) {
            System.out.println("[Gestor] No se encontro la notificacion con codigo: " + codigo);
            return false;
        }

        intentarEnvio(encontrada.get());
        return true;
    }

    // ── Envío masivo ─────────────────────────────────────────
    /**
     * Envía todas las notificaciones en estado PENDIENTE.
     * Si una falla, el error se registra en el estado de esa notificacion
     * y el proceso continua con las siguientes.
     */
    public void enviarTodas() {
        System.out.println("\n=====================================================");
        System.out.println("   ENVIANDO TODAS LAS NOTIFICACIONES PENDIENTES");
        System.out.println("=====================================================\n");

        List<Notificacion> pendientes = filtrarPorEstado(EstadoNotificacion.PENDIENTE);

        if (pendientes.isEmpty()) {
            System.out.println("[Gestor] No hay notificaciones pendientes.");
            return;
        }

        for (Notificacion n : pendientes) {
            intentarEnvio(n);
        }

        System.out.printf("[Gestor] Envio masivo completado: %d intentados, " +
                          "%d exitosos, %d fallidos%n",
                pendientes.size(), enviosExitosos, enviosFallidos);
    }

    // ── Núcleo de envío con manejo de excepciones ─────────────
    /**
     * Ejecuta {@code enviar()} de una notificacion capturando cualquier
     * excepcion inesperada para que no interrumpa el flujo del gestor.
     */
    private void intentarEnvio(Notificacion n) {
        try {
            n.enviar();
            if (n.getEstado() == EstadoNotificacion.ENVIADA) {
                enviosExitosos++;
            } else {
                enviosFallidos++;
            }
        } catch (Exception e) {
            enviosFallidos++;
            System.err.println("[Gestor] ERROR inesperado enviando " +
                               n.getCodigo() + ": " + e.getMessage());
        }
    }

    // ── Filtrado ──────────────────────────────────────────────
    /**
     * @param estado Estado por el que filtrar. No puede ser null.
     * @return Lista (puede estar vacía) de notificaciones con ese estado.
     */
    public List<Notificacion> filtrarPorEstado(EstadoNotificacion estado) {
        Objects.requireNonNull(estado, "El estado no puede ser null.");
        return historial.stream()
                        .filter(n -> n.getEstado() == estado)
                        .toList();
    }

    /**
     * @param tipo Tipo por el que filtrar. No puede ser null.
     * @return Lista (puede estar vacía) de notificaciones de ese tipo.
     */
    public List<Notificacion> filtrarPorTipo(TipoNotificacion tipo) {
        Objects.requireNonNull(tipo, "El tipo no puede ser null.");
        return historial.stream()
                        .filter(n -> n.getTipo() == tipo)
                        .toList();
    }

    // ── Reporte ───────────────────────────────────────────────
    /**
     * Imprime el historial completo con estado de cada notificacion y estadisticas finales.
     */
    public void imprimirHistorial() {
        System.out.println("\n=====================================================");
        System.out.println("           HISTORIAL DE NOTIFICACIONES");
        System.out.println("=====================================================");

        if (historial.isEmpty()) {
            System.out.println("  (Sin registros)");
            return;
        }

        historial.forEach(n -> System.out.println("  " + n.getInfo()));

        System.out.println("-----------------------------------------------------");
        System.out.printf("  Total: %d | Enviadas: %d | Fallidas: %d | Pendientes: %d%n",
                historial.size(),
                filtrarPorEstado(EstadoNotificacion.ENVIADA).size(),
                filtrarPorEstado(EstadoNotificacion.FALLIDA).size(),
                filtrarPorEstado(EstadoNotificacion.PENDIENTE).size());
    }

    // ── Utilidades internas ───────────────────────────────────
    private boolean existeCodigo(String codigo) {
        return historial.stream().anyMatch(n -> n.getCodigo().equals(codigo));
    }

    // ── Getters ───────────────────────────────────────────────
    /** @return copia defensiva del historial; no modifica el interno. */
    public List<Notificacion> getHistorial()      { return new ArrayList<>(historial); }
    public int                getEnviosExitosos() { return enviosExitosos; }
    public int                getEnviosFallidos() { return enviosFallidos; }
    public int                getTotalRegistrado(){ return historial.size(); }
}
