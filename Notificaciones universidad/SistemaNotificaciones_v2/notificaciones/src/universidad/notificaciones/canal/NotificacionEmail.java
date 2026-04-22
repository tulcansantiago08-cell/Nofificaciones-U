package universidad.notificaciones.canal;

import universidad.notificaciones.enums.EstadoNotificacion;
import universidad.notificaciones.enums.TipoNotificacion;
import universidad.notificaciones.modelo.Notificacion;
import universidad.notificaciones.modelo.Usuario;

import java.util.Objects;

/**
 * Notificacion enviada por correo electronico.
 *
 * <p>Atributos adicionales respecto a {@link Notificacion}:
 * <ul>
 *   <li>{@code asunto} — línea de asunto del correo (inmutable).</li>
 *   <li>{@code emailDestino} — dirección destino (tomada del usuario, inmutable).</li>
 *   <li>{@code nombreAdjunto} — nombre del archivo adjunto (opcional, null si no hay).</li>
 * </ul>
 */
public class NotificacionEmail extends Notificacion {

    private final String asunto;
    private final String emailDestino;

    // Adjunto opcional; null significa "sin adjunto"
    private String nombreAdjunto;

    // ── Constructor ───────────────────────────────────────────
    /**
     * @param codigo       Identificador único.
     * @param destinatario Usuario destinatario; su email no puede ser nulo.
     * @param mensaje      Cuerpo del correo.
     * @param tipo         Tipo de notificacion.
     * @param asunto       Línea de asunto. No puede ser nulo ni vacío.
     */
    public NotificacionEmail(String codigo,
                             Usuario destinatario,
                             String mensaje,
                             TipoNotificacion tipo,
                             String asunto) {
        super(codigo, destinatario, mensaje, tipo);

        if (asunto == null || asunto.isBlank()) {
            throw new IllegalArgumentException("El asunto del email no puede ser vacio.");
        }
        // Validar que el usuario tenga email registrado antes de crear la notificacion
        if (destinatario.getEmail() == null || destinatario.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                "El usuario '" + destinatario.getNombre() + "' no tiene email registrado.");
        }

        this.asunto        = asunto.strip();
        this.emailDestino  = destinatario.getEmail();
        this.nombreAdjunto = null;
    }

    // ── enviar() ──────────────────────────────────────────────
    @Override
    public void enviar() {
        System.out.println("===================================================");
        System.out.println("  [EMAIL] ENVIANDO CORREO ELECTRONICO");
        System.out.println("---------------------------------------------------");
        System.out.println("  Para    : " + emailDestino);
        System.out.println("  Asunto  : " + asunto);
        System.out.println("  Tipo    : " + getTipo().getDescripcion());
        System.out.println("  Mensaje : " + getMensaje());
        if (tieneAdjunto()) {
            System.out.println("  Adjunto : " + nombreAdjunto);
        }
        System.out.println("---------------------------------------------------");

        if (simularEnvio()) {
            registrarEnvio(EstadoNotificacion.ENVIADA);
            System.out.println("  OK  Email enviado exitosamente a " + emailDestino);
        } else {
            registrarEnvio(EstadoNotificacion.FALLIDA);
            System.out.println("  ERROR  Fallo al enviar email a " + emailDestino);
        }
        System.out.println("===================================================\n");
    }

    // ── Adjunto ───────────────────────────────────────────────
    /**
     * Agrega un archivo adjunto a este correo.
     *
     * @param nombreArchivo Nombre del archivo. No puede ser nulo ni vacío.
     * @throws IllegalArgumentException si el nombre es nulo o vacío.
     */
    public void adjuntarArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo adjunto no puede estar vacio.");
        }
        this.nombreAdjunto = nombreArchivo.strip();
        System.out.println("[Email] Archivo adjuntado: " + this.nombreAdjunto);
    }

    /** Elimina el archivo adjunto si existiera. */
    public void quitarAdjunto() {
        this.nombreAdjunto = null;
    }

    // ── Simulación de envío ───────────────────────────────────
    /**
     * En produccion aqui se invocaría JavaMail / SendGrid / AWS SES.
     * Devuelve true si las precondiciones están cumplidas.
     */
    private boolean simularEnvio() {
        return emailDestino != null && !emailDestino.isBlank();
    }

    // ── Getters ───────────────────────────────────────────────
    public String  getAsunto()       { return asunto; }
    public String  getEmailDestino() { return emailDestino; }
    public boolean tieneAdjunto()    { return nombreAdjunto != null; }

    /**
     * @return nombre del adjunto, o null si no hay ninguno.
     */
    public String getNombreAdjunto() { return nombreAdjunto; }
}
