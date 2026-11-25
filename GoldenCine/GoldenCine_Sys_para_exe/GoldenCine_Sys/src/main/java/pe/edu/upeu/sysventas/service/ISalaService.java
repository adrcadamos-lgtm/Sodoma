package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Sala;

import java.util.List;

// HERENCIA entre interfaces: ISalaService extends ICrudGenericoService, hereda el contrato del CRUD.
public interface ISalaService extends ICrudGenericoService<Sala, Long> {
    List<ComboBoxOption> listarCombobox();
}

