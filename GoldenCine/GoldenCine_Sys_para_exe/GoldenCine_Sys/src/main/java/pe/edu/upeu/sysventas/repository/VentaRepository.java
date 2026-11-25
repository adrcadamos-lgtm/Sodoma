package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.config.DatabaseConfig;
import pe.edu.upeu.sysventas.model.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Persists each sale and its function/food lines in one PostgreSQL transaction. */
public class VentaRepository extends AbstractJdbcRepository<Venta, Long> {
    @Override protected String table() { return "venta"; }
    @Override protected String idColumn() { return "id_venta"; }
    @Override protected List<String> columns() { return List.of("id_cliente", "id_usuario", "fecha_gener", "precio_base", "igv", "precio_total", "serie", "num_doc", "tipo_doc"); }
    @Override protected List<Object> values(Venta v) {
        return List.of(v.getDniruc().getDniruc(), v.getIdUsuario().getIdUsuario(), v.getFechaGener() == null ? LocalDateTime.now() : v.getFechaGener(),
                v.getPrecioBase(), v.getIgv(), v.getPrecioTotal(), v.getSerie() == null ? "" : v.getSerie(),
                v.getNumDoc() == null ? "" : v.getNumDoc(), v.getTipoDoc() == null ? "" : v.getTipoDoc());
    }
    @Override protected Long getId(Venta v) { return v.getId(); }
    @Override protected void setId(Venta v, Long id) { v.setId(id); }

    @Override public Venta save(Venta venta) {
        String insertSale = "INSERT INTO sysventas_ventas.venta (id_cliente,id_usuario,fecha_gener,precio_base,igv,precio_total,serie,num_doc,tipo_doc) VALUES (?,?,?,?,?,?,?,?,?) RETURNING id_venta";
        String insertFunction = "INSERT INTO sysventas_ventas.venta_detalle_service (id_venta,precio,cantidad,precio_total,id_servicio,descuento) VALUES (?,?,?,?,?,?)";
        String insertFood = "INSERT INTO sysventas_ventas.venta_detalle_product (id_producto,id_venta,cantidad,precio,subtotal) VALUES (?,?,?,?,?)";
        try (var connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<Object> fields = values(venta);
                try (PreparedStatement statement = connection.prepareStatement(insertSale)) {
                    for (int i = 0; i < fields.size(); i++) statement.setObject(i + 1, fields.get(i));
                    try (ResultSet result = statement.executeQuery()) { result.next(); venta.setId(result.getLong(1)); }
                }
                if (venta.getDetalleVenta() != null) {
                    try (PreparedStatement statement = connection.prepareStatement(insertFunction)) {
                        for (VentaDetalle d : venta.getDetalleVenta()) {
                            statement.setLong(1, venta.getId()); statement.setDouble(2, d.getPu());
                            statement.setInt(3, d.getCantidad().intValue()); statement.setDouble(4, d.getSubtotal());
                            statement.setLong(5, d.getIdProducto().getIdProducto()); statement.setDouble(6, d.getDescuento());
                            statement.addBatch();
                        }
                        statement.executeBatch();
                    }
                }
                if (venta.getDetalleComida() != null) {
                    try (PreparedStatement stock = connection.prepareStatement("UPDATE sysventas_ventas.producto SET stockDisponible = stockDisponible - ? WHERE id_producto = ? AND stockDisponible >= ?");
                         PreparedStatement detail = connection.prepareStatement(insertFood)) {
                        for (VentaDetalleProducto d : venta.getDetalleComida()) {
                            stock.setInt(1, d.getCantidad()); stock.setLong(2, d.getIdProducto().getIdComida()); stock.setInt(3, d.getCantidad());
                            if (stock.executeUpdate() != 1) throw new IllegalStateException("Stock insuficiente o producto inexistente: " + d.getIdProducto().getNombre());
                            detail.setLong(1, d.getIdProducto().getIdComida()); detail.setLong(2, venta.getId());
                            detail.setInt(3, d.getCantidad()); detail.setDouble(4, d.getPu()); detail.setDouble(5, d.getSubtotal()); detail.addBatch();
                        }
                        detail.executeBatch();
                    }
                }
                connection.commit();
                return venta;
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof RuntimeException runtime) throw runtime;
                throw new IllegalStateException("No se pudo guardar la venta", e);
            } finally { connection.setAutoCommit(true); }
        } catch (SQLException e) { throw databaseError("guardar venta", e); }
    }

    @Override protected Venta map(ResultSet r) throws SQLException {
        Venta v = new Venta();
        v.setId(r.getLong("id_venta"));
        v.setDniruc(new ClienteRepository().findById(r.getString("id_cliente")).orElse(null));
        v.setIdUsuario(new UsuarioRepository().findById(r.getLong("id_usuario")).orElse(null));
        v.setFechaGener(r.getTimestamp("fecha_gener").toLocalDateTime());
        v.setPrecioBase(r.getDouble("precio_base")); v.setIgv(r.getDouble("igv")); v.setPrecioTotal(r.getDouble("precio_total"));
        v.setSerie(r.getString("serie")); v.setNumDoc(r.getString("num_doc")); v.setTipoDoc(r.getString("tipo_doc"));
        v.setDetalleVenta(loadFunctionDetails(v.getId()));
        v.setDetalleComida(loadFoodDetails(v.getId()));
        return v;
    }

    private List<VentaDetalle> loadFunctionDetails(Long idVenta) {
        List<VentaDetalle> details = new ArrayList<>();
        String sql = "SELECT * FROM sysventas_ventas.venta_detalle_service WHERE id_venta = ?";
        try (var connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idVenta);
            try (ResultSet r = statement.executeQuery()) {
                ProductoRepository repo = new ProductoRepository();
                while (r.next()) details.add(new VentaDetalle(r.getLong("id_detalle_servicio"), r.getDouble("precio"), (double) r.getInt("cantidad"), r.getDouble("descuento"), r.getDouble("precio_total"), null, repo.findById(r.getLong("id_servicio")).orElse(null)));
            }
            return details;
        } catch (SQLException e) { throw databaseError("leer detalle de funciones", e); }
    }

    private List<VentaDetalleProducto> loadFoodDetails(Long idVenta) {
        List<VentaDetalleProducto> details = new ArrayList<>();
        String sql = "SELECT * FROM sysventas_ventas.venta_detalle_product WHERE id_venta = ?";
        try (var connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idVenta);
            try (ResultSet r = statement.executeQuery()) {
                ComidaRepository repo = new ComidaRepository();
                while (r.next()) details.add(new VentaDetalleProducto(r.getLong("id_venta_det_prod"), repo.findById(r.getLong("id_producto")).orElse(null), r.getInt("cantidad"), r.getDouble("precio"), r.getDouble("subtotal")));
            }
            return details;
        } catch (SQLException e) { throw databaseError("leer detalle de comida", e); }
    }

    @Override public void deleteById(Long id) { super.deleteById(id); }

    @Override protected RuntimeException databaseError(String action, SQLException e) {
        return new IllegalStateException("No se pudo " + action + " en PostgreSQL: " + e.getMessage(), e);
    }
}
