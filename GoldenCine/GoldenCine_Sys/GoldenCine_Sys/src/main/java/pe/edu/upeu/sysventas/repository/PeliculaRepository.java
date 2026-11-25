package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Peliculas;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PeliculaRepository extends AbstractJdbcRepository<Peliculas, Long> {
    @Override protected String table() { return "peliculas"; }
    @Override protected String idColumn() { return "id_peliculas"; }
    @Override protected List<String> columns() { return List.of("nombre", "id_genero"); }
    @Override protected List<Object> values(Peliculas p) { return List.of(p.getNombre(), lookupId("genero", "id_genero", "nombre", "Sin clasificar")); }
    @Override protected Long getId(Peliculas p) { return p.getIdPeliculas(); }
    @Override protected void setId(Peliculas p, Long id) { p.setIdPeliculas(id); }
    @Override protected Peliculas map(ResultSet r) throws SQLException { return new Peliculas(r.getLong("id_peliculas"), r.getString("nombre")); }

    public void seedData() {
        if (findAll().isEmpty()) {
            for (String name : List.of("Avengers", "Spider-Man", "Toy Story 5", "Jurassic World", "Mario Bros 2", "Transformers", "Kung Fu Panda 3", "Intensamente 2", "Deadpool", "El Conjuro")) save(new Peliculas(null, name));
        }
    }
}
