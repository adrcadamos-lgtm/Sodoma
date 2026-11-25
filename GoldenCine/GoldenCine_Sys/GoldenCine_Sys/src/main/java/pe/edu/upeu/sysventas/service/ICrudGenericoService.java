package pe.edu.upeu.sysventas.service;

import java.util.List;

// GENERALIZACIÓN: contrato general del CRUD (T, ID) que usan todos los servicios.
public interface ICrudGenericoService <T, ID>{
    T save(T t);
    T update(ID id, T t);
    List<T> findAll();
    T findByid(ID id);
    void delete(ID id);
}
