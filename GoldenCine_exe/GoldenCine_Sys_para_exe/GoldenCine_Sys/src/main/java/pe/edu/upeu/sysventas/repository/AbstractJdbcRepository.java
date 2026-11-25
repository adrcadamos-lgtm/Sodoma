package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.config.DatabaseConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Small JDBC CRUD base. Table and column identifiers are fixed by each repository. */
public abstract class AbstractJdbcRepository<T, ID> implements ICrudGenericoRepository<T, ID> {
    protected abstract String table();
    protected abstract String idColumn();
    protected abstract List<String> columns();
    protected abstract List<Object> values(T entity);
    protected abstract ID getId(T entity);
    protected abstract void setId(T entity, ID id);
    protected abstract T map(ResultSet result) throws SQLException;

    protected String selectSql() { return "SELECT * FROM sysventas_ventas." + table(); }

    @Override public T save(T entity) {
        List<String> columns = columns();
        String sql = "INSERT INTO sysventas_ventas." + table() + " (" + String.join(",", columns) + ") VALUES (" +
                String.join(",", java.util.Collections.nCopies(columns.size(), "?")) + ") RETURNING " + idColumn();
        try (var connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, values(entity));
            try (ResultSet result = statement.executeQuery()) { result.next(); setId(entity, castId(result.getObject(1))); return entity; }
        } catch (SQLException e) { throw databaseError("guardar en " + table(), e); }
    }

    @Override public T update(T entity) {
        List<String> columns = columns();
        String assignments = columns.stream().map(c -> c + " = ?").collect(java.util.stream.Collectors.joining(","));
        String sql = "UPDATE sysventas_ventas." + table() + " SET " + assignments + " WHERE " + idColumn() + " = ?";
        try (var connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            List<Object> parameters = new ArrayList<>(values(entity)); parameters.add(getId(entity)); bind(statement, parameters);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("No se encontró registro " + getId(entity) + " en " + table());
            return entity;
        } catch (SQLException e) { throw databaseError("actualizar " + table(), e); }
    }

    @Override public Optional<T> findById(ID id) {
        String sql = selectSql() + " WHERE " + idColumn() + " = ?";
        try (var connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? Optional.of(map(result)) : Optional.empty(); }
        } catch (SQLException e) { throw databaseError("buscar en " + table(), e); }
    }

    @Override public List<T> findAll() {
        List<T> entities = new ArrayList<>();
        try (var connection = DatabaseConfig.getConnection(); var statement = connection.createStatement(); ResultSet result = statement.executeQuery(selectSql() + " ORDER BY " + idColumn())) {
            while (result.next()) entities.add(map(result));
            return entities;
        } catch (SQLException e) { throw databaseError("listar " + table(), e); }
    }

    @Override public void deleteById(ID id) {
        try (var connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM sysventas_ventas." + table() + " WHERE " + idColumn() + " = ?")) {
            statement.setObject(1, id);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("No se encontró registro " + id + " en " + table());
        } catch (SQLException e) { throw databaseError("eliminar de " + table(), e); }
    }

    @Override public boolean existsById(ID id) { return findById(id).isPresent(); }

    protected void bind(PreparedStatement statement, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) statement.setObject(i + 1, values.get(i));
    }

    /** Resolves a lookup-table key by its stable business name instead of assuming identity values. */
    protected long lookupId(String lookupTable, String idColumn, String nameColumn, String name) {
        String sql = "SELECT " + idColumn + " FROM sysventas_ventas." + lookupTable + " WHERE " + nameColumn + " = ?";
        try (var connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getLong(1);
                throw new IllegalStateException("No existe " + name + " en sysventas_ventas." + lookupTable);
            }
        } catch (SQLException e) { throw databaseError("buscar referencia " + name + " en " + lookupTable, e); }
    }

    @SuppressWarnings("unchecked")
    private ID castId(Object value) { return (ID) (value instanceof Number n ? n.longValue() : value); }

    protected RuntimeException databaseError(String action, SQLException e) {
        return new IllegalStateException("No se pudo " + action + " en PostgreSQL: " + e.getMessage(), e);
    }
}
