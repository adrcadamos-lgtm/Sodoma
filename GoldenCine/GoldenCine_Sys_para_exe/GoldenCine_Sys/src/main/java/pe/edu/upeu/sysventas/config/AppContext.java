package pe.edu.upeu.sysventas.config;

import pe.edu.upeu.sysventas.controller.*;
import pe.edu.upeu.sysventas.repository.*;
import pe.edu.upeu.sysventas.service.*;
import pe.edu.upeu.sysventas.service.impl.*;
import java.util.HashMap;
import java.util.Map;

public class AppContext {

    // Singleton: una sola instancia en toda la app
    private static AppContext instance;

    public static synchronized AppContext getInstance() {
        if (instance == null) instance = new AppContext();
        return instance;
    }

    // El "directorio": Clase → Objeto
    private final Map<Class<?>, Object> contenedor = new HashMap<>();

    // Constructor privado: aquí se arma toda la aplicación
    private AppContext() {
        registrarRepositorios();
        registrarServicios();
        registrarControladores();
    }

    // CAPA 1 — REPOSITORIOS
    // Cada repositorio sabe hablar con una tabla de la base de datos.
    // No reciben dependencias: solo necesitan la conexión (DatabaseConfig).
    private void registrarRepositorios() {
        //registrar(CategoriaRepository.class, new CategoriaRepository());


        registrar(PeliculaRepository.class,     new PeliculaRepository());
        // OBJETO: new SalaRepository() crea el objeto repositorio de Sala.
        registrar(SalaRepository.class,         new SalaRepository());
        registrar(ProductoCineRepository.class,  new ProductoCineRepository());
        registrar(ProductoRepository.class,      new ProductoRepository());
        registrar(ClienteRepository.class,       new ClienteRepository());
        registrar(UsuarioRepository.class,       new UsuarioRepository());
        registrar(ComidaRepository.class,        new ComidaRepository());
        registrar(VentaRepository.class,         new VentaRepository());

    }

    // CAPA 2 — SERVICIOS
    // Cada servicio recibe su repositorio por constructor.
    // Usamos getBean() para buscarlo en el directorio: no creamos nada nuevo.
    private void registrarServicios() {

        registrar(IPeliculaService.class,    new PeliculaServiceImp(   getBean(PeliculaRepository.class)));
        // OBJETO + POLIMORFISMO: se guarda como ISalaService (interfaz) un objeto SalaServiceImp.
        registrar(ISalaService.class,        new SalaServiceImp(       getBean(SalaRepository.class)));
        registrar(IProductoService.class,     new ProductoServiceImp(    getBean(ProductoRepository.class)));
        registrar(IProductoCineService.class, new ProductoCineServiceImp(getBean(ProductoCineRepository.class)));
        registrar(IClienteService.class,      new ClienteServiceImp(     getBean(ClienteRepository.class)));
        registrar(IUsuarioService.class,      new UsuarioServiceImp(     getBean(UsuarioRepository.class)));
        registrar(IComidaService.class,       new ComidaServiceImp(      getBean(ComidaRepository.class)));
        registrar(IVentaService.class,        new VentaServiceImp(       getBean(VentaRepository.class),
                                                                         getBean(IComidaService.class)));


    }

    // CAPA 3 — CONTROLADORES JavaFX
    // Cada controlador recibe los servicios que necesita por constructor.
    // El FXMLLoader los busca aquí a través de setControllerFactory().
    private void registrarControladores() {
        registrar(LoginController.class,   new LoginController(getBean(IUsuarioService.class)));
        registrar(ClienteController.class, new ClienteController(getBean(IClienteService.class)));
        registrar(ComidaController.class,  new ComidaController(getBean(IComidaService.class)));
        registrar(VentaController.class,
                new VentaController(
                        getBean(IClienteService.class),
                        getBean(IProductoService.class),
                        getBean(IPeliculaService.class),
                        getBean(ISalaService.class),
                        getBean(IComidaService.class),
                        getBean(IVentaService.class)));
        registrar(ProductoController.class,
                new ProductoController(
                        getBean(ISalaService.class),
                        getBean(IPeliculaService.class),
                        getBean(IProductoService.class)));

        // OBJETO: new SalaController(...) recibe el servicio por constructor.
        registrar(SalaController.class,
                new SalaController(getBean(ISalaService.class)));

    }

    // API del contenedor — estos dos métodos son todo lo que hace la DI
    /** Guarda un objeto en el directorio, indexado por su tipo o interfaz. */
    private void registrar(Class<?> tipo, Object bean) {
        contenedor.put(tipo, bean);
    }

    /**
     * Busca y devuelve un objeto por su tipo o interfaz.
     * Equivale a lo que hace Spring/Micronaut con @Inject automáticamente.
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> tipo) {
        Object bean = contenedor.get(tipo);
        if (bean == null) {
            // Búsqueda por compatibilidad: sirve cuando se pide una interfaz
            // y el objeto guardado es su implementación concreta.
            bean = contenedor.values().stream()
                    .filter(b -> tipo.isAssignableFrom(b.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Bean no encontrado: " + tipo.getName() +
                                    "\n→ ¿Lo registraste en AppContext?"));
        }
        return (T) bean;
    }
}