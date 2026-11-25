package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Comida;

import java.util.List;

public interface IComidaService extends ICrudGenericoService<Comida, Long> {
    List<ComboBoxOption> listarTipos();
    List<ComboBoxOption> listarCombobox();
}
