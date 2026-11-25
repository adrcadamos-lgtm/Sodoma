package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Sala;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SalaRepository extends AbstractJdbcRepository<Sala, Long> {
    @Override protected String table() { return "sala"; }
    @Override protected String idColumn() { return "idsala"; }
    @Override protected List<String> columns() { return List.of("nombre", "id_tipo_sala", "cantidad", "libre"); }
    @Override protected List<Object> values(Sala s) { return List.of(s.getNombre(), tipoSalaId(s.getNombre()), 100, Boolean.TRUE.equals(s.getLibre())); }
    @Override protected Long getId(Sala s) { return s.getIdSala(); }
    @Override protected void setId(Sala s, Long id) { s.setIdSala(id); }
    @Override protected Sala map(ResultSet r) throws SQLException { return new Sala(r.getLong("idsala"), r.getString("nombre"), r.getBoolean("libre")); }

    private long tipoSalaId(String nombre) {
        String n = nombre == null ? "" : nombre.toUpperCase();
        String tipo = n.contains("VIP") ? "VIP" : n.contains("3D") ? "3D" : "2D";
        return lookupId("tipo_sala", "id_tipo_sala", "nombre", tipo);
    }

    public void seedData() {
        if (findAll().isEmpty()) {
            save(new Sala(null, "Sala 1 2D", true));
            save(new Sala(null, "Sala 2 2D", false));
            save(new Sala(null, "Sala 3 3D", true));
            save(new Sala(null, "Sala 4 2D", true));
            save(new Sala(null, "Sala VIP", false));
        }
    }
}
