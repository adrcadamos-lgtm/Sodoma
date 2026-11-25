package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.enums.TipoComida;
import pe.edu.upeu.sysventas.model.Comida;
import pe.edu.upeu.sysventas.repository.ComidaRepository;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.service.IComidaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class ComidaServiceImp extends CrudGenericoServiceImp<Comida, Long> implements IComidaService {//herencia
    private final ComidaRepository comidaRepository;

    @Override
    protected ICrudGenericoRepository<Comida, Long> getRepo() {
        return comidaRepository;
    }

    @Override
    public List<Comida> findAll() {//polomorfismo
        if (comidaRepository.findAll().isEmpty()) {
            comidaRepository.seedData();
        }
        return comidaRepository.findAll();
    }

    @Override
    public List<ComboBoxOption> listarTipos() {
        List<ComboBoxOption> listar = new ArrayList<>();
        for (TipoComida t : TipoComida.values()) {
            listar.add(new ComboBoxOption(t.name(), t.getDescripcion()));
        }
        return listar;
    }

    @Override
    public List<ComboBoxOption> listarCombobox() {
        List<ComboBoxOption> listar = new ArrayList<>();
        for (Comida c : findAll()) {
            listar.add(new ComboBoxOption(String.valueOf(c.getIdComida()),
                    String.format(Locale.US, "%s - S/ %.2f (stock %d)",
                            c.getNombre(), c.getPrecio(), c.getStockDisponible())));
        }
        return listar;
    }
}
