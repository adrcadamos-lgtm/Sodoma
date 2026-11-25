package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Peliculas;
import pe.edu.upeu.sysventas.repository.PeliculaRepository;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.service.IPeliculaService;

import java.util.ArrayList;
import java.util.List;

public class PeliculaServiceImp extends CrudGenericoServiceImp<Peliculas, Long> implements IPeliculaService {
    private final PeliculaRepository peliculaRepository;

    public PeliculaServiceImp(PeliculaRepository categoriaRepository) {
        this.peliculaRepository = categoriaRepository;
    }

    @Override
    protected ICrudGenericoRepository<Peliculas, Long> getRepo() {
        return peliculaRepository;
    }

    @Override
    public List<ComboBoxOption> lisCategoria() {
        List<ComboBoxOption> listar=new ArrayList<>();
        if(peliculaRepository.findAll().isEmpty()) {
            peliculaRepository.seedData();
        }
        List<Peliculas> peliculas = peliculaRepository.findAll();
        for (Peliculas pelicula : peliculas) {
            if ("Spider-Man".equalsIgnoreCase(pelicula.getNombre())) {
                pelicula.setNombre("SpiderMan 1");
                peliculaRepository.update(pelicula);
            }
        }
        peliculas = peliculaRepository.findAll();
        if (peliculas.stream().noneMatch(p -> "SpiderMan 1".equalsIgnoreCase(p.getNombre()))) {
            peliculaRepository.save(new Peliculas(null, "SpiderMan 1"));
        }
        if (peliculas.stream().noneMatch(p -> "Transformers Rise of the Beasts".equalsIgnoreCase(p.getNombre()))) {
            peliculaRepository.save(new Peliculas(null, "Transformers Rise of the Beasts"));
        }
        for (Peliculas cat : peliculaRepository.findAll()) {
            ComboBoxOption cb = new ComboBoxOption();
            cb.setKey(String.valueOf(cat.getIdPeliculas()));
            cb.setValue(cat.getNombre());
            listar.add(cb);
        }
        return listar;
    }
}
