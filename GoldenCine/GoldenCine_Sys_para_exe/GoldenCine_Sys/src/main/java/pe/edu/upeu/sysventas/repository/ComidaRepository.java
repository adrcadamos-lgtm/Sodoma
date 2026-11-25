package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.enums.TipoComida;
import pe.edu.upeu.sysventas.model.Comida;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ComidaRepository extends AbstractJdbcRepository<Comida, Long> {//herencia
    @Override protected String table() { return "producto"; }
    @Override protected String idColumn() { return "id_producto"; }
    @Override protected List<String> columns() { return List.of("nombre", "tipo", "precio", "stockDisponible", "id_tipo"); }
    @Override protected List<Object> values(Comida c) { return List.of(c.getNombre(), c.getTipo().name(), c.getPrecio(), c.getStockDisponible(), lookupId("tipo", "id_tipo", "nombre", c.getTipo().name())); }
    @Override protected Long getId(Comida c) { return c.getIdComida(); }
    @Override protected void setId(Comida c, Long id) { c.setIdComida(id); }
    @Override protected Comida map(ResultSet r) throws SQLException {
        return new Comida(r.getLong("id_producto"), r.getString("nombre"), TipoComida.valueOf(r.getString("tipo")), r.getDouble("precio"), r.getInt("stockDisponible"));
    }

    public void seedData() {
        if (!findAll().isEmpty()) return;
        save(new Comida(null, "Canchita Grande", TipoComida.SNACKS, 18.00, 50));
        save(new Comida(null, "Canchita Mediana", TipoComida.SNACKS, 14.00, 50));
        save(new Comida(null, "Gaseosa Grande", TipoComida.BEBIDAS, 10.00, 40));
        save(new Comida(null, "Gaseosa Mediana", TipoComida.BEBIDAS, 8.00, 40));
        save(new Comida(null, "Combo Pareja", TipoComida.COMBOS, 35.00, 30));
        save(new Comida(null, "Chocolate", TipoComida.DULCES, 5.00, 60));
        save(new Comida(null, "Agua", TipoComida.BEBIDAS, 4.00, 60));
    }
}
