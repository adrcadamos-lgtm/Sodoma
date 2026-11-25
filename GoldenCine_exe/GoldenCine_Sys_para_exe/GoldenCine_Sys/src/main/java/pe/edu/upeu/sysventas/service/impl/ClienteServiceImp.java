package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.repository.ClienteRepository;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.service.IClienteService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ClienteServiceImp extends CrudGenericoServiceImp<Cliente, String> implements IClienteService {
    private final ClienteRepository clienteRepository;

    @Override
    protected ICrudGenericoRepository<Cliente, String> getRepo() {
        return clienteRepository;
    }

    @Override
    public Cliente save(Cliente cliente) {
        if (clienteRepository.existsById(cliente.getDniruc())) {
            throw new RuntimeException("Ya existe un cliente con el documento " + cliente.getDniruc());
        }
        return super.save(cliente);
    }

    @Override
    public List<ComboBoxOption> listarCombobox() {
        List<ComboBoxOption> listar = new ArrayList<>();
        for (Cliente c : clienteRepository.findAll()) {
            listar.add(new ComboBoxOption(c.getDniruc(), c.getDniruc() + " - " + c.getNombres()));
        }
        return listar;
    }

    @Override
    public List<ComboBoxOption> listarTipoDocumento() {
        List<ComboBoxOption> listar = new ArrayList<>();
        for (TipoDocumento td : TipoDocumento.values()) {
            listar.add(new ComboBoxOption(td.name(), td.name()));
        }
        return listar;
    }
}
