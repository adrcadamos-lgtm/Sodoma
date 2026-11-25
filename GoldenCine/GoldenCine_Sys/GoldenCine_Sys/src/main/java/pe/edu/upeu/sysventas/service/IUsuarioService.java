package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.model.Usuario;

public interface IUsuarioService extends ICrudGenericoService<Usuario, Long> {
    // Devuelve el usuario si las credenciales son correctas; si no, lanza una excepción con el motivo.
    Usuario login(String usuario, String clave);
}
