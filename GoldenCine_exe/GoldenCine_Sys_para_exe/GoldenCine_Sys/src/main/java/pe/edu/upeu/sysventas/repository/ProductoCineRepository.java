package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.ProductoCine;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/** Persists the cinema promotion/catalog entries represented in the Architect model. */
public class ProductoCineRepository extends AbstractJdbcRepository<ProductoCine, Long> {
    @Override protected String table() { return "promociones"; }
    @Override protected String idColumn() { return "idproductocine"; }
    @Override protected List<String> columns() { return List.of("nombremedida", "id_servicio"); }
    @Override protected List<Object> values(ProductoCine p) { return Arrays.asList(p.getNombreMedida(), null); }
    @Override protected Long getId(ProductoCine p) { return p.getIdProductoCine(); }
    @Override protected void setId(ProductoCine p, Long id) { p.setIdProductoCine(id); }
    @Override protected ProductoCine map(ResultSet r) throws SQLException { return new ProductoCine(r.getLong("idproductocine"), r.getString("nombremedida")); }

    public void seedData() {
        if (!findAll().isEmpty()) return;
        for (String name : List.of("Canchita Grande Snacks", "Canchita Mediana Snacks", "Gaseosa Grande Bebidas", "Gaseosa Mediana Bebidas", "Combo Pareja Combos", "Chocolate Dulces", "Agua Bebidas")) {
            save(new ProductoCine(null, name));
        }
    }
}
