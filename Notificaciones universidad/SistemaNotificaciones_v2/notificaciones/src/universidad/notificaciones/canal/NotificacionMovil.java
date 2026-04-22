package universidad.notificaciones.canal;

import universidad.notificaciones.enums.EstadoNotificacion;
import universidad.notificaciones.enums.TipoNotificacion;
import universidad.notificaciones.modelo.Notificacion;
import universidad.notificaciones.modelo.Usuario;

/**
 * Notificacion push enviada a la aplicacion movil del usuario.
 *
 * <p>Atributos adicionales respecto a {@link Notificacion}:
 * <ul>
 *   <li>{@code tokenDispositivo} — token FCM/APNs del dispositivo (snapshot al crear).</li>
 *   <li>{@code plataforma} — "Android" o "iOS".</li>
 *   <li>{@code icono} — nombre del recurso de icono de la notificacion.</li>
 *   <li>{@code sonidoActivo} — si la notificacion debe emitir sonido.</li>
 * </ul>
 *
 * <p><b>Nota sobre el token:</b> el token se copia del usuario en el momento de
 * construccion (snapshot). Si el usuario actualiza su dispositivo posteriormente,
 * deberá crearse una nueva notificacion.
 *
 * <p>Si el usuario no tiene token registrado, el envío resulta en estado {@code FALLIDA}.
 */
public class NotificacionMovil extends Notificacion {

    private final String  tokenDispositivo;  // snapshot al construir
    private final String  plataforma;
    private       String  icono;
    private       boolean sonidoActivo;

    // ── Constructor ───────────────────────────────────────────
    /**
     * @param codigo       Identificador único.
     * @param destinatario Usuario destinatario.
     * @param mensaje      Cuerpo de la notificacion push.
     * @param tipo         Tipo de notificacion.
     * @param plataforma   Plataforma objetivo ("Android" o "iOS"). No puede ser vacía.
     */
    public NotificacionMovil(String codigo,
                             Usuario destinatario,
                             String mensaje,
                             TipoNotificacion tipo,
                             String plataforma) {
        super(codigo, destinatario, mensaje, tipo);

        if (plataforma == null || plataforma.isBlank()) {
            throw new IllegalArgumentException("La plataforma del dispositivo movil no puede ser vacia.");
        }

        // Snapshot del token: se registra tal como está en este momento
        this.tokenDispositivo = destinatario.tieneTokenMovil()
                ? destinatario.getTokenDispositivoMovil()
                : null;

        this.plataforma   = plataforma.strip();
        this.icono        = "ic_universidad";
        this.sonidoActivo = true;
    }

    // ── enviar() ──────────────────────────────────────────────
    @Override
    public void enviar() {
        System.out.println("===================================================");
        System.out.println("  [MOVIL] ENVIANDO NOTIFICACION PUSH");
        System.out.println("---------------------------------------------------");
        System.out.println("  Token     : " + (tokenDispositivo != null ? tokenDispositivo : "NO REGISTRADO"));
        System.out.println("  Plataforma: " + plataforma);
        System.out.println("  Icono     : " + icono);
        System.out.println("  Sonido    : " + (sonidoActivo ? "Activado" : "Silencioso"));
        System.out.println("  Tipo      : " + getTipo().getDescripcion());
        System.out.println("  Mensaje   : " + getMensaje());
        System.out.println("---------------------------------------------------");

        if (tokenDispositivo == null) {
            registrarEnvio(EstadoNotificacion.FALLIDA);
            System.out.println("  ERROR  Sin token de dispositivo — "
                               + getDestinatario().getNombre() + " no tiene la app registrada.");
        } else if (simularEnvio()) {
            registrarEnvio(EstadoNotificacion.ENVIADA);
            System.out.println("  OK  Notificacion push enviada a " + plataforma);
        } else {
            registrarEnvio(EstadoNotificacion.FALLIDA);
            System.out.println("  ERROR  Fallo en el servicio push (" + plataforma + ").");
        }
        System.out.println("===================================================\n");
    }

    // ── Métodos auxiliares ────────────────────────────────────
    /**
     * Activa o desactiva el sonido de la notificacion.
     * Solo puede configurarse antes de enviar.
     */
    public void configurarSonido(boolean activar) {
        this.sonidoActivo = activar;
        System.out.println("[Movil] Sonido " + (activar ? "activado." : "desactivado."));
    }

    private boolean simularEnvio() {
        // En produccion: invocar Firebase Cloud Messaging (FCM) o APNs
        return tokenDispositivo != null && !tokenDispositivo.isBlank();
    }

    // ── Getters / Setters ─────────────────────────────────────
    /** @return token de dispositivo registrado al crear la notificacion, o null. */
    public String  getTokenDispositivo() { return tokenDispositivo; }
    public String  getPlataforma()       { return plataforma; }
    public String  getIcono()            { return icono; }
    public boolean isSonidoActivo()      { return sonidoActivo; }

    public void setIcono(String icono) {
        if (icono == null || icono.isBlank()) {
            throw new IllegalArgumentException("El nombre del icono no puede ser vacio.");
        }
        this.icono = icono.strip();
    }
}
