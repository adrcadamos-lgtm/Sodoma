package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Sala;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.SalaRepository;
import pe.edu.upeu.sysventas.service.ISalaService;

import java.util.ArrayList;
import java.util.List;

// HERENCIA: SalaServiceImp extends CrudGenericoServiceImp, hereda el CRUD del servicio.
// Además implements ISalaService (cumple el contrato de la interfaz).
public class SalaServiceImp extends CrudGenericoServiceImp<Sala, Long> implements ISalaService {
    // ENCAPSULAMIENTO: el repositorio es private y final; fuera de la clase solo se accede por los métodos del servicio.
    private final SalaRepository salaRepository;

    public SalaServiceImp(SalaRepository marcaRepository) {
        this.salaRepository = marcaRepository;
    }


    @Override
    protected ICrudGenericoRepository<Sala, Long> getRepo() {
        return salaRepository;
    }

    // POLIMORFISMO (sobreescritura): esta clase redefine findAll() del padre y se ejecuta su versión.
    @Override
    public List<Sala> findAll() {
        if (salaRepository.findAll().isEmpty()) {
            salaRepository.seedData();
        }
        return salaRepository.findAll();
    }

    @Override
    public List<ComboBoxOption> listarCombobox() {
        if(salaRepository.findAll().isEmpty()) {
            salaRepository.seedData();
        }
        List<ComboBoxOption> listar = new ArrayList<>();
        for (Sala m : salaRepository.findAll()) {
            ComboBoxOption cb = new ComboBoxOption();
            cb.setKey(String.valueOf(m.getIdSala()));
            cb.setValue(m.getNombre());
            listar.add(cb);
        }
        return listar;
    }
}
