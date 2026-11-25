package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.model.Cliente;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ClienteRepository extends AbstractJdbcRepository<Cliente, String> {
    @Override protected String table() { return "cliente"; }
    @Override protected String idColumn() { return "id_cliente"; }
    @Override protected List<String> columns() { return List.of("id_cliente", "nombres", "rep_legal", "tipo_documento", "edad"); }
    @Override protected List<Object> values(Cliente c) { return List.of(c.getDniruc(), c.getNombres(), c.getRepLegal() == null ? "" : c.getRepLegal(), c.getTipoDocumento().name(), c.getEdad()); }
    @Override protected String getId(Cliente c) { return c.getDniruc(); }
    @Override protected void setId(Cliente c, String id) { c.setDniruc(id); }
    @Override protected Cliente map(ResultSet r) throws SQLException {
        Cliente c = new Cliente();
        c.setDniruc(r.getString("id_cliente"));
        c.setNombres(r.getString("nombres"));
        c.setRepLegal(r.getString("rep_legal"));
        c.setTipoDocumento(TipoDocumento.valueOf(r.getString("tipo_documento")));
        c.setEdad(r.getInt("edad"));
        return c;
    }
}
