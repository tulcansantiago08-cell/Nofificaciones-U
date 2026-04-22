package universidad.notificaciones.canal;

import universidad.notificaciones.enums.EstadoNotificacion;
import universidad.notificaciones.enums.TipoNotificacion;
import universidad.notificaciones.modelo.Notificacion;
import universidad.notificaciones.modelo.Usuario;

/**
 * Notificacion enviada por mensaje de texto (SMS).
 *
 * <p>Atributos adicionales respecto a {@link Notificacion}:
 * <ul>
 *   <li>{@code numeroTelefono} — número destino (tomado del usuario, inmutable).</li>
 *   <li>{@code limiteCaracteres} — máximo de caracteres permitidos (default 160).</li>
 *   <li>{@code operadora} — proveedor del servicio SMS.</li>
 * </ul>
 *
 * <p>Si el mensaje supera el límite, se trunca automáticamente antes de enviar.
 * Si el número de teléfono es nulo o vacío, el envío falla con estado FALLIDA.
 */
public class NotificacionSMS extends Notificacion {

    /** Límite estándar de un SMS simple (GSM 03.38). */
    private static final int LIMITE_DEFAULT    = 160;
    /** Sufijo visible cuando el mensaje fue truncado. */
    private static final String SUFIJO_TRUNCADO = "...";

    private final String numeroTelefono;
    private final String operadora;
    private       int    limiteCaracteres;

    // ── Constructor ───────────────────────────────────────────
    /**
     * @param codigo       Identificador único.
     * @param destinatario Usuario destinatario; su teléfono no puede ser nulo.
     * @param mensaje      Cuerpo del SMS (se trunca si supera el límite).
     * @param tipo         Tipo de notificacion.
     * @param operadora    Nombre de la operadora. No puede ser nulo ni vacío.
     * @throws IllegalArgumentException si el usuario no tiene teléfono registrado.
     */
    public NotificacionSMS(String codigo,
                           Usuario destinatario,
                           String mensaje,
                           TipoNotificacion tipo,
                           String operadora) {
        super(codigo, destinatario, mensaje, tipo);

        if (operadora == null || operadora.isBlank()) {
            throw new IllegalArgumentException("La operadora del SMS no puede ser vacia.");
        }
        // Validar que el usuario tenga teléfono antes de crear la notificacion
        if (destinatario.getTelefono() == null || destinatario.getTelefono().isBlank()) {
            throw new IllegalArgumentException(
                "El usuario '" + destinatario.getNombre() + "' no tiene telefono registrado.");
        }

        this.numeroTelefono   = destinatario.getTelefono();
        this.operadora        = operadora.strip();
        this.limiteCaracteres = LIMITE_DEFAULT;
    }

    // ── enviar() ──────────────────────────────────────────────
    @Override
    public void enviar() {
        System.out.println("===================================================");
        System.out.println("  [SMS] ENVIANDO MENSAJE DE TEXTO");
        System.out.println("---------------------------------------------------");
        System.out.println("  Numero   : " + numeroTelefono);
        System.out.println("  Operadora: " + operadora);
        System.out.println("  Tipo     : " + getTipo().getDescripcion());

        String mensajeAEnviar = prepararMensaje();
        System.out.println("  Mensaje  : " + mensajeAEnviar);
        System.out.println("  Chars    : " + mensajeAEnviar.length() + "/" + limiteCaracteres);
        System.out.println("---------------------------------------------------");

        if (simularEnvio()) {
            registrarEnvio(EstadoNotificacion.ENVIADA);
            System.out.println("  OK  SMS enviado a " + numeroTelefono);
        } else {
            registrarEnvio(EstadoNotificacion.FALLIDA);
            System.out.println("  ERROR  Fallo al enviar SMS a " + numeroTelefono);
        }
        System.out.println("===================================================\n");
    }

    // ── Lógica de preparación del mensaje ────────────────────
    /**
     * Devuelve el mensaje listo para enviar.
     * Si supera el límite, lo trunca dejando espacio para el sufijo "...".
     */
    private String prepararMensaje() {
        String msg = getMensaje();
        if (!validarLongitud()) {
            // Garantiza que el truncado nunca genere índice negativo
            int corte = Math.max(0, limiteCaracteres - SUFIJO_TRUNCADO.length());
            String truncado = msg.substring(0, corte) + SUFIJO_TRUNCADO;
            System.out.println("  AVISO: Mensaje truncado de "
                               + msg.length() + " a " + limiteCaracteres + " caracteres.");
            return truncado;
        }
        return msg;
    }

    /**
     * @return true si el mensaje cabe dentro del límite configurado.
     */
    public boolean validarLongitud() {
        return getMensaje().length() <= limiteCaracteres;
    }

    private boolean simularEnvio() {
        // En produccion: invocar API de Twilio, AWS SNS, etc.
        return numeroTelefono != null && !numeroTelefono.isBlank();
    }

    // ── Getters / Setters ─────────────────────────────────────
    public String getNumeroTelefono()   { return numeroTelefono; }
    public String getOperadora()        { return operadora; }
    public int    getLimiteCaracteres() { return limiteCaracteres; }

    /**
     * Permite ajustar el límite (útil para SMS concatenados o MMS).
     *
     * @param limite Debe ser al menos {@value #SUFIJO_TRUNCADO}.length() + 1.
     */
    public void setLimiteCaracteres(int limite) {
        if (limite < SUFIJO_TRUNCADO.length() + 1) {
            throw new IllegalArgumentException(
                "El limite minimo de caracteres es " + (SUFIJO_TRUNCADO.length() + 1) + ".");
        }
        this.limiteCaracteres = limite;
    }
}
