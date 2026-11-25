package pe.edu.upeu.sysventas.config;

import pe.edu.upeu.sysventas.model.Usuario;

// Guarda el usuario que inició sesión; la venta lo toma de aquí.
public final class SesionUsuario {
    private static Usuario actual;
    private static boolean invitado;

    private SesionUsuario() {
    }

    public static void iniciar(Usuario usuario) {
        actual = usuario;
        invitado = false;
    }

    public static void iniciarComoInvitado(Usuario usuarioExistente) {
        actual = usuarioExistente;
        invitado = true;
    }

    public static boolean esInvitado() {
        return invitado;
    }

    public static Usuario getUsuario() {
        return actual;
    }

    public static boolean haySesion() {
        return actual != null;
    }

    public static void cerrar() {
        actual = null;
        invitado = false;
    }
}
