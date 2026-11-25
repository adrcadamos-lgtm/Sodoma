package pe.edu.upeu.sysventas.repository;

import java.util.List;
import java.util.Optional;

// GENERALIZACIÓN: contrato general del CRUD (T, ID) que usan todos los repositorios.
public interface ICrudGenericoRepository<T,ID> {
    T save(T entity);
    T update(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void  deleteById(ID id);
    boolean existsById(ID id);

}
