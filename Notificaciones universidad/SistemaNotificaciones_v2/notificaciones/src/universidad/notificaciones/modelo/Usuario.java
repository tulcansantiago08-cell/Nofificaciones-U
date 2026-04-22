package universidad.notificaciones.modelo;

import java.util.Objects;

/**
 * Representa a un usuario del sistema universitario (estudiante, docente, etc.).
 *
 * Los campos de identidad (id, nombre, email, telefono) son inmutables.
 * El token del dispositivo móvil es opcional y mutable, ya que el usuario
 * puede registrar o cambiar su dispositivo en cualquier momento.
 */
public class Usuario {

    // Campos de identidad: inmutables tras la construcción
    private final String id;
    private final String nombre;
    private final String email;
    private final String telefono;

    // Token opcional para notificaciones push; mutable por diseño
    private String tokenDispositivoMovil;

    /**
     * @param id       Identificador único del usuario. No puede ser nulo ni vacío.
     * @param nombre   Nombre completo. No puede ser nulo ni vacío.
     * @param email    Correo electrónico. No puede ser nulo ni vacío.
     * @param telefono Teléfono de contacto. No puede ser nulo ni vacío.
     */
    public Usuario(String id, String nombre, String email, String telefono) {
        this.id       = validarCampo(id,       "id");
        this.nombre   = validarCampo(nombre,   "nombre");
        this.email    = validarCampo(email,     "email");
        this.telefono = validarCampo(telefono, "telefono");
        this.tokenDispositivoMovil = null;
    }

    // ── Validación interna ────────────────────────────────────
    private static String validarCampo(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                "El campo '" + campo + "' del Usuario no puede ser nulo ni vacío.");
        }
        return valor.trim();
    }

    // ── Getters ───────────────────────────────────────────────
    public String getId()       { return id; }
    public String getNombre()   { return nombre; }
    public String getEmail()    { return email; }
    public String getTelefono() { return telefono; }

    public String getTokenDispositivoMovil() { return tokenDispositivoMovil; }

    /**
     * Registra o actualiza el token del dispositivo móvil.
     * Pasar null elimina el token (usuario sin app registrada).
     */
    public void setTokenDispositivoMovil(String token) {
        this.tokenDispositivoMovil = (token != null && !token.isBlank()) ? token.trim() : null;
    }

    /** @return true si el usuario tiene un token móvil registrado. */
    public boolean tieneTokenMovil() {
        return tokenDispositivoMovil != null && !tokenDispositivoMovil.isBlank();
    }

    // ── Información legible ───────────────────────────────────
    public String getDatos() {
        return String.format("Usuario[%s] %-20s | email: %-25s | tel: %s",
                             id, nombre, email, telefono);
    }

    // ── equals y hashCode basados en el id ───────────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        return id.equals(((Usuario) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getDatos();
    }
}
