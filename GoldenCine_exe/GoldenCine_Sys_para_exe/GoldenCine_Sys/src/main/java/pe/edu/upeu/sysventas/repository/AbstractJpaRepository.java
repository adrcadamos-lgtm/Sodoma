package pe.edu.upeu.sysventas.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// GENERALIZACIÓN: clase general (T, ID) con el CRUD común que sirve para cualquier entidad (Sala, Producto, etc.).
// Las clases hijas (SalaRepository, ProductoRepository...) heredan de esta y solo agregan lo propio.
public abstract class AbstractJpaRepository<T, ID>implements ICrudGenericoRepository<T, ID>{
    // ENCAPSULAMIENTO: la lista de datos no es pública; solo se usa mediante los métodos save, update, findAll, etc.
    protected final List<T> data=new ArrayList<>();
    protected abstract  ID getId(T entity);
    protected abstract void setId(T entity, ID id);
    protected abstract ID generateId();

    @Override
    public T save(T entity) {
        if (getId(entity)==null){
            setId(entity, generateId());
        }
        data.add(entity);
        return entity;
    }

    @Override
    public T update(T entity) {
        ID id=getId(entity);
        for(int i=0;i<data.size();i++){
            T current=data.get(i);
            if(getId(current).equals(id)){
                data.set(i, entity);
                return entity;
            }
        }
        throw new RuntimeException(
                "No se encontro la cantidad con ID:"+id
        );

    }

    @Override
    public Optional<T> findById(ID id) {

        return data.stream()
                .filter(entity->getId(entity).equals(id))
                .findFirst();
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(data);
    }

    @Override
    public void deleteById(ID id) {
        data.removeIf(entity->getId(entity).equals(id));

    }

    @Override
    public boolean existsById(ID id) {
        return data.stream().anyMatch(entity->getId(entity).equals(id));
    }
}
