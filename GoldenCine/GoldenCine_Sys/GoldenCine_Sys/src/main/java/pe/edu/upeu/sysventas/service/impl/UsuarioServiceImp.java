package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.UsuarioRepository;
import pe.edu.upeu.sysventas.service.IUsuarioService;

import java.util.List;

@RequiredArgsConstructor
public class UsuarioServiceImp extends CrudGenericoServiceImp<Usuario, Long> implements IUsuarioService {
    private final UsuarioRepository usuarioRepository;

    @Override
    protected ICrudGenericoRepository<Usuario, Long> getRepo() {
        return usuarioRepository;
    }

    @Override
    public List<Usuario> findAll() {
        usuarioRepository.seedData();
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario login(String usuario, String clave) {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isEmpty()) {
            throw new RuntimeException("Ingrese usuario y clave");
        }
        usuarioRepository.seedData();
        Usuario u = usuarioRepository.findByUsuario(usuario.trim())
                .filter(x -> clave.equals(x.getClave()))
                .orElseThrow(() -> new RuntimeException("Usuario o clave incorrectos"));
        if (!"A".equals(u.getEstado())) {
            throw new RuntimeException("El usuario está inactivo");
        }
        return u;
    }
}
