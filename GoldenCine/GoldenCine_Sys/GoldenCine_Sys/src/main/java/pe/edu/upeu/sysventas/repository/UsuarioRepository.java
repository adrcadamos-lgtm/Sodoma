package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Usuario;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository extends AbstractJdbcRepository<Usuario, Long> {
    @Override protected String table() { return "usuario"; }
    @Override protected String idColumn() { return "id_usuario"; }
    @Override protected List<String> columns() { return List.of("usuario", "clave", "estado"); }
    @Override protected List<Object> values(Usuario u) { return List.of(u.getUsuario(), u.getClave(), u.getEstado() == null ? "A" : u.getEstado()); }
    @Override protected Long getId(Usuario u) { return u.getIdUsuario(); }
    @Override protected void setId(Usuario u, Long id) { u.setIdUsuario(id); }
    @Override protected Usuario map(ResultSet r) throws SQLException {
        return Usuario.builder().idUsuario(r.getLong("id_usuario")).usuario(r.getString("usuario"))
                .clave(r.getString("clave")).estado(r.getString("estado")).build();
    }

    public Optional<Usuario> findByUsuario(String usuario) {
        try (var connection = pe.edu.upeu.sysventas.config.DatabaseConfig.getConnection();
             var statement = connection.prepareStatement("SELECT * FROM sysventas_ventas.usuario WHERE LOWER(usuario) = LOWER(?)")) {
            statement.setString(1, usuario);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? Optional.of(map(result)) : Optional.empty(); }
        } catch (SQLException e) { throw databaseError("buscar usuario", e); }
    }

    public void seedData() {
        if (findAll().isEmpty()) {
            save(Usuario.builder().usuario("admin").clave("admin123").estado("A").build());
            save(Usuario.builder().usuario("vendedor").clave("1234").estado("A").build());
        }
    }
}
