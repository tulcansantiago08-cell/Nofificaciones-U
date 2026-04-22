package universidad.notificaciones.demo;

import universidad.notificaciones.canal.NotificacionEmail;
import universidad.notificaciones.canal.NotificacionMovil;
import universidad.notificaciones.canal.NotificacionSMS;
import universidad.notificaciones.enums.EstadoNotificacion;
import universidad.notificaciones.enums.TipoNotificacion;
import universidad.notificaciones.gestor.GestorNotificaciones;
import universidad.notificaciones.modelo.Notificacion;
import universidad.notificaciones.modelo.Usuario;

import java.util.List;

/**
 * Punto de entrada del sistema de notificaciones universitarias.
 *
 * Cubre los 4 tipos de situacion x los 3 canales de comunicacion,
 * gestion de errores (usuario sin token movil), reintento de envio
 * y filtrado del historial.
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("======================================================");
        System.out.println("  SISTEMA DE NOTIFICACIONES -- UNIVERSIDAD DEMO");
        System.out.println("======================================================\n");

        try {
            ejecutarDemo();
        } catch (Exception e) {
            System.err.println("[MAIN] Error inesperado en la demo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Logica principal separada para claridad ───────────────
    private static void ejecutarDemo() {

        // ── 1. Crear usuarios ─────────────────────────────────
        Usuario estudiante1 = new Usuario(
                "U001", "Santiago Garcia", "santiago@uni.edu.co", "+573001234567");
        estudiante1.setTokenDispositivoMovil("TOKEN_ANDROID_SGarcia_XYZ");

        // Laura no tiene token movil: simula usuario sin app instalada
        Usuario estudiante2 = new Usuario(
                "U002", "Laura Martinez", "laura@uni.edu.co", "+573109876543");

        Usuario docente = new Usuario(
                "D001", "Prof. Carlos Ruiz", "cruiz@uni.edu.co", "+573157654321");
        docente.setTokenDispositivoMovil("TOKEN_IOS_CRuiz_ABC");

        System.out.println("Usuarios creados:");
        System.out.println("  " + estudiante1.getDatos());
        System.out.println("  " + estudiante2.getDatos());
        System.out.println("  " + docente.getDatos());
        System.out.println();

        // ── 2. Crear gestor ───────────────────────────────────
        GestorNotificaciones gestor = new GestorNotificaciones();

        // ── 3. Construir notificaciones ───────────────────────

        // Situacion 1: Publicacion de calificaciones
        NotificacionEmail emailCalificaciones = new NotificacionEmail(
                "NTF-001", estudiante1,
                "Estimado Santiago, sus calificaciones del periodo 2024-2 ya estan " +
                "disponibles en el portal academico. Ingrese a https://portal.uni.edu.co",
                TipoNotificacion.PUBLICACION_CALIFICACIONES,
                "Calificaciones 2024-2 disponibles");
        emailCalificaciones.adjuntarArchivo("reporte_calificaciones_2024_2.pdf");

        NotificacionSMS smsCalificaciones = new NotificacionSMS(
                "NTF-002", estudiante1,
                "UNI: Sus calificaciones 2024-2 estan disponibles. Ingrese al portal.",
                TipoNotificacion.PUBLICACION_CALIFICACIONES,
                "Claro Colombia");

        // Situacion 2: Recordatorio de pago de matricula
        NotificacionEmail emailPago = new NotificacionEmail(
                "NTF-003", estudiante2,
                "Estimada Laura, recuerde que el plazo para el pago de matricula " +
                "del proximo semestre vence el 15 de enero de 2025. Valor: $4.200.000 COP.",
                TipoNotificacion.RECORDATORIO_PAGO,
                "Recordatorio: Pago de matricula - Vence 15 Ene 2025");

        // Laura no tiene token: este envio resultara FALLIDA (comportamiento esperado)
        NotificacionMovil movilPago = new NotificacionMovil(
                "NTF-004", estudiante2,
                "RECORDATORIO: pago de matricula vence en 5 dias.",
                TipoNotificacion.RECORDATORIO_PAGO,
                "Android");

        // Situacion 3: Cancelacion de clase (SMS a ambos estudiantes)
        NotificacionSMS smsCancelacion1 = new NotificacionSMS(
                "NTF-005", estudiante1,
                "AVISO: Clase de Programacion III - martes 10 Ene 8am CANCELADA. " +
                "Nueva fecha: jueves 12 Ene.",
                TipoNotificacion.CANCELACION_CLASE,
                "Tigo Colombia");

        NotificacionSMS smsCancelacion2 = new NotificacionSMS(
                "NTF-006", estudiante2,
                "AVISO: Clase de Programacion III - martes 10 Ene 8am CANCELADA. " +
                "Nueva fecha: jueves 12 Ene.",
                TipoNotificacion.CANCELACION_CLASE,
                "Movistar Colombia");

        // Situacion 4: Confirmacion de evento academico
        NotificacionMovil movilEvento = new NotificacionMovil(
                "NTF-007", estudiante1,
                "CONFIRMADO: Congreso Nacional de Ingenieria de Software - " +
                "15 Feb 2025, Auditorio Principal.",
                TipoNotificacion.CONFIRMACION_EVENTO,
                "Android");
        movilEvento.configurarSonido(true);

        NotificacionEmail emailEvento = new NotificacionEmail(
                "NTF-008", docente,
                "Estimado Prof. Ruiz, confirmamos su participacion como ponente en el " +
                "Congreso Nacional de Ingenieria de Software, 15 de febrero de 2025.",
                TipoNotificacion.CONFIRMACION_EVENTO,
                "Confirmacion: Ponente Congreso Nacional - 15 Feb 2025");
        emailEvento.adjuntarArchivo("programa_congreso_2025.pdf");

        // ── 4. Registrar en el gestor ─────────────────────────
        System.out.println("-- Registrando notificaciones --\n");
        gestor.registrar(emailCalificaciones);
        gestor.registrar(smsCalificaciones);
        gestor.registrar(emailPago);
        gestor.registrar(movilPago);
        gestor.registrar(smsCancelacion1);
        gestor.registrar(smsCancelacion2);
        gestor.registrar(movilEvento);
        gestor.registrar(emailEvento);

        // Verificar que no se permiten codigos duplicados
        System.out.println("\n-- Intentando registrar codigo duplicado (debe lanzar excepcion) --");
        try {
            gestor.registrar(new NotificacionSMS(
                    "NTF-001",  // codigo ya existente
                    estudiante1,
                    "Mensaje duplicado",
                    TipoNotificacion.CANCELACION_CLASE,
                    "Claro Colombia"));
        } catch (IllegalArgumentException e) {
            System.out.println("  Excepcion capturada correctamente: " + e.getMessage());
        }

        // ── 5. Enviar todas las pendientes ────────────────────
        gestor.enviarTodas();

        // ── 6. Historial completo ─────────────────────────────
        gestor.imprimirHistorial();

        // ── 7. Filtrar fallidas ───────────────────────────────
        System.out.println("\n-- Notificaciones FALLIDAS --");
        List<Notificacion> fallidas = gestor.filtrarPorEstado(EstadoNotificacion.FALLIDA);
        if (fallidas.isEmpty()) {
            System.out.println("  (Ninguna)");
        } else {
            fallidas.forEach(n -> System.out.println("  FALLIDA: " + n.getInfo()));
        }

        // ── 8. Reintentar la fallida (NTF-004, Laura sin token) ──
        System.out.println("\n-- Reintentando NTF-004 (Laura registra su dispositivo) --");
        estudiante2.setTokenDispositivoMovil("TOKEN_ANDROID_Laura_NUEVO");
        // Se debe crear nueva notificacion porque el token es snapshot al construir
        NotificacionMovil movilPagoReintento = new NotificacionMovil(
                "NTF-009", estudiante2,
                "RECORDATORIO: pago de matricula vence en 5 dias.",
                TipoNotificacion.RECORDATORIO_PAGO,
                "Android");
        gestor.registrar(movilPagoReintento);
        gestor.enviar("NTF-009");

        // ── 9. Filtrar por tipo ───────────────────────────────
        System.out.println("\n-- Todas las cancelaciones de clase --");
        gestor.filtrarPorTipo(TipoNotificacion.CANCELACION_CLASE)
              .forEach(n -> System.out.println("  " + n.getInfo()));

        // ── 10. Resumen estadistico final ─────────────────────
        System.out.println("\n======================================================");
        System.out.printf("  Exitosos: %d  |  Fallidos: %d  |  Total: %d%n",
                gestor.getEnviosExitosos(),
                gestor.getEnviosFallidos(),
                gestor.getTotalRegistrado());
        System.out.println("======================================================");
    }
}
