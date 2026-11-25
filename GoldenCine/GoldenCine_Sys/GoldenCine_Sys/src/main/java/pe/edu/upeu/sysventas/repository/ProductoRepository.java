package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.enums.TipoFuncion;
import pe.edu.upeu.sysventas.model.Peliculas;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.model.Sala;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/** Persists cinema functions in Architect's cartelera table. */
public class ProductoRepository extends AbstractJdbcRepository<Producto, Long> {
    @Override protected String table() { return "cartelera"; }
    @Override protected String idColumn() { return "id_servicio"; }
    @Override protected List<String> columns() { return List.of("nombre_cartera", "pu", "utilidad", "cantidad", "idpelicula", "idsala", "id_usuario", "hora_inicio", "hora_fin", "tipo_funcion"); }
    @Override protected List<Object> values(Producto p) {
        return Arrays.asList(p.getNombre(), p.getPu(), 0.0, 0, p.getIdPelicula().getIdPeliculas(), p.getIdSala().getIdSala(), null, null, null,
                p.getTiipoProducto() == null ? TipoFuncion.NOCHE.name() : p.getTiipoProducto().name());
    }
    @Override protected Long getId(Producto p) { return p.getIdProducto(); }
    @Override protected void setId(Producto p, Long id) { p.setIdProducto(id); }
    @Override protected String selectSql() {
        return "SELECT c.id_servicio, c.nombre_cartera, c.pu, c.tipo_funcion, c.idpelicula, p.nombre AS pelicula_nombre, " +
                "c.idsala, s.nombre AS sala_nombre, s.libre AS sala_libre FROM sysventas_ventas.cartelera c " +
                "JOIN sysventas_ventas.peliculas p ON p.id_peliculas = c.idpelicula " +
                "JOIN sysventas_ventas.sala s ON s.idsala = c.idsala";
    }
    @Override protected Producto map(ResultSet r) throws SQLException {
        TipoFuncion tipo = r.getString("tipo_funcion") == null ? TipoFuncion.NOCHE : TipoFuncion.valueOf(r.getString("tipo_funcion"));
        return new Producto(r.getLong("id_servicio"), r.getString("nombre_cartera"), tipo, r.getDouble("pu"),
                new Peliculas(r.getLong("idpelicula"), r.getString("pelicula_nombre")),
                new Sala(r.getLong("idsala"), r.getString("sala_nombre"), r.getBoolean("sala_libre")));
    }

    public void seedData() {
        if (!findAll().isEmpty()) return;
        PeliculaRepository peliculas = new PeliculaRepository();
        SalaRepository salas = new SalaRepository();
        peliculas.seedData();
        salas.seedData();
        List<Peliculas> ps = peliculas.findAll();
        List<Sala> ss = salas.findAll();
        if (!ps.isEmpty() && !ss.isEmpty()) save(new Producto(null, "Función de ejemplo", TipoFuncion.NOCHE, 30.0, ps.get(1), ss.get(0)));
    }
}
