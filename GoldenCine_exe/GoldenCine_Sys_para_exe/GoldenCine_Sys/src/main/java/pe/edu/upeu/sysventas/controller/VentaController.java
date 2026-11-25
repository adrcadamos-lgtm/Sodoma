package pe.edu.upeu.sysventas.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.config.SesionUsuario;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.*;
import pe.edu.upeu.sysventas.service.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Set;

@RequiredArgsConstructor
public class VentaController {
    private static final String TIPO_FUNCION = "Función";
    private static final String TIPO_COMIDA = "Comida";

    private final IClienteService clienteService;
    private final IProductoService funcionService;
    private final IPeliculaService peliculaService;
    private final ISalaService salaService;
    private final IComidaService comidaService;
    private final IVentaService ventaService;

    @FXML private TextField txtDniCliente;
    @FXML private ComboBox<ComboBoxOption> cbxFuncion, cbxComida;
    @FXML private TextField txtCantFuncion, txtCantComida;
    @FXML private Label lblVendedor, lblDescuentoCliente, lblInfoComida, lblTotal, lbnMsg;
    @FXML private TableView<VentCarrito> tableView;
    @FXML private AnchorPane miContenedor;

    private ObservableList<VentCarrito> carrito = FXCollections.observableArrayList();
    private long secuenciaCarrito = 1;
    private final ContextMenu sugerenciasClientes = new ContextMenu();
    private List<ComboBoxOption> clientesRegistrados = new ArrayList<>();
    private Cliente clienteActual;
    private String dniClienteActual;
    private boolean actualizandoCampoCliente;
    private final Set<Integer> asientosElegidos = new LinkedHashSet<>();

    @FXML
    public void initialize() {
        Usuario u = SesionUsuario.getUsuario();
        lblVendedor.setText("Vendedor: " + (u == null ? "-" : u.getUsuario()));

        carrito = FXCollections.observableArrayList();
        construirTabla();
        cargarClientes();
        configurarBusquedaCliente();
        cargarFunciones();
        cargarComidas();

        txtDniCliente.textProperty().addListener((o, anterior, texto) -> {
            if (!actualizandoCampoCliente) {
                clienteActual = null;
                dniClienteActual = null;
                actualizarSugerenciasClientes(texto);
            }
            actualizarInfoCliente();
            recalcularCarrito();
        });
        cbxComida.valueProperty().addListener((o, a, n) -> actualizarInfoComida());
        cbxFuncion.valueProperty().addListener((o, anterior, nueva) -> {
            asientosElegidos.clear();
            txtCantFuncion.setText("1");
        });

        txtCantFuncion.setText("1");
        txtCantComida.setText("1");
        actualizarInfoCliente();
        actualizarInfoComida();
        actualizarTotal();
    }

    // ---------- carga de datos ----------
    private void cargarClientes() {
        clientesRegistrados = clienteService.listarCombobox();
    }

    private void configurarBusquedaCliente() {
        txtDniCliente.setOnAction(event -> {
            if (clienteSeleccionado() == null) {
                error("Seleccione un DNI registrado de las sugerencias");
            } else {
                sugerenciasClientes.hide();
                limpiarMensaje();
                actualizarInfoCliente();
                recalcularCarrito();
            }
        });
        txtDniCliente.focusedProperty().addListener((o, teniaFoco, tieneFoco) -> {
            if (!tieneFoco) sugerenciasClientes.hide();
        });
    }

    private void actualizarSugerenciasClientes(String texto) {
        sugerenciasClientes.hide();
        sugerenciasClientes.getItems().clear();
        String prefijo = texto == null ? "" : texto.trim();
        if (prefijo.isEmpty()) return;

        for (ComboBoxOption opcion : clientesRegistrados) {
            String dni = opcion.getKey();
            if (dni != null && dni.startsWith(prefijo)) {
                MenuItem sugerencia = new MenuItem(opcion.getValue());
                sugerencia.setOnAction(event -> seleccionarCliente(opcion));
                sugerenciasClientes.getItems().add(sugerencia);
            }
        }

        if (!sugerenciasClientes.getItems().isEmpty() && txtDniCliente.isFocused()) {
            sugerenciasClientes.show(txtDniCliente, Side.BOTTOM, 0, 0);
        }
    }

    private void seleccionarCliente(ComboBoxOption opcion) {
        sugerenciasClientes.hide();
        actualizandoCampoCliente = true;
        txtDniCliente.setText(opcion.getKey());
        actualizandoCampoCliente = false;
        clienteActual = clienteService.findByid(opcion.getKey());
        dniClienteActual = opcion.getKey();
        limpiarMensaje();
        actualizarInfoCliente();
        recalcularCarrito();
    }

    private void cargarFunciones() {
        peliculaService.lisCategoria(); // asegura los datos de ejemplo de películas
        salaService.findAll();          // y de salas
        List<ComboBoxOption> opciones = new ArrayList<>();
        for (Producto f : funcionService.findAll()) {
            opciones.add(new ComboBoxOption(String.valueOf(f.getIdProducto()), descripcionFuncion(f)));
        }
        cbxFuncion.getItems().setAll(opciones);
    }

    private void cargarComidas() {
        cbxComida.getSelectionModel().clearSelection();
        cbxComida.getItems().setAll(comidaService.listarCombobox());
    }

    private String descripcionFuncion(Producto f) {
        String pelicula;
        String sala;
        try {
            pelicula = peliculaService.findByid(f.getIdPelicula().getIdPeliculas()).getNombre();
        } catch (RuntimeException e) {
            pelicula = "Película";
        }
        try {
            sala = salaService.findByid(f.getIdSala().getIdSala()).getNombre();
        } catch (RuntimeException e) {
            sala = "Sala";
        }
        String tipo = f.getTiipoProducto() == null ? "" : f.getTiipoProducto().getDescripcion();
        return String.format(Locale.US, "%s | %s | %s | S/ %.2f", pelicula, sala, tipo, f.getPu());
    }

    private Cliente clienteSeleccionado() {
        String dni = txtDniCliente.getText() == null ? "" : txtDniCliente.getText().trim();
        if (dni.isEmpty()) return null;
        if (clienteActual != null && dni.equals(dniClienteActual)) return clienteActual;

        boolean registrado = clientesRegistrados.stream()
                .anyMatch(opcion -> dni.equals(opcion.getKey()));
        if (!registrado) return null;

        clienteActual = clienteService.findByid(dni);
        dniClienteActual = dni;
        return clienteActual;
    }

    // ---------- tabla del carrito ----------
    private void construirTabla() {
        tableView.getColumns().clear();
        tableView.getColumns().add(columna("Tipo", 90, l -> l.getTipoItem()));
        tableView.getColumns().add(columna("Descripción", 320, l -> l.getNombreProducto()));
        tableView.getColumns().add(columna("Cant.", 60, l -> String.format("%.0f", l.getCantidad())));
        tableView.getColumns().add(columna("P. Unit.", 80, l -> dinero(l.getPunitario())));
        tableView.getColumns().add(columna("Descuento", 90, l -> dinero(l.getDescuento())));
        tableView.getColumns().add(columna("Subtotal", 90, l -> dinero(l.getPtotal())));
        tableView.getColumns().add(columna("Asientos", 130, l -> l.getAsientos() == null ? "—" : l.getAsientos()));
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tableView.setItems(carrito);
    }

    private TableColumn<VentCarrito, String> columna(String titulo, double ancho,
                                                     java.util.function.Function<VentCarrito, String> valor) {
        TableColumn<VentCarrito, String> c = new TableColumn<>(titulo);
        c.setPrefWidth(ancho);
        c.setCellValueFactory(cd -> new SimpleStringProperty(valor.apply(cd.getValue())));
        return c;
    }

    private String dinero(Double d) {
        return String.format(Locale.US, "%.2f", d == null ? 0.0 : d);
    }

    // ---------- información en pantalla ----------
    private void actualizarInfoCliente() {
        Cliente c = clienteSeleccionado();
        if (c == null) {
            lblDescuentoCliente.setText("Seleccione un cliente");
        } else if (DescuentoEdad.aplica(c.getEdad())) {
            lblDescuentoCliente.setText(String.format(Locale.US, "%d años: descuento %.0f%% en funciones",
                    c.getEdad(), DescuentoEdad.PORCENTAJE));
        } else {
            lblDescuentoCliente.setText(c.getEdad() + " años: sin descuento");
        }
    }

    private void actualizarInfoComida() {
        ComboBoxOption op = cbxComida.getValue();
        if (op == null) {
            lblInfoComida.setText("Seleccione un producto");
            return;
        }
        Comida m = comidaService.findByid(Long.parseLong(op.getKey()));
        lblInfoComida.setText(String.format(Locale.US, "Precio S/ %.2f | Stock disponible: %d",
                m.getPrecio(), m.getStockDisponible()));
    }

    private void actualizarTotal() {
        double total = carrito.stream().mapToDouble(l -> l.getPtotal() == null ? 0.0 : l.getPtotal()).sum();
        lblTotal.setText(String.format(Locale.US, "TOTAL: S/ %.2f", total));
    }

    // ---------- carrito ----------
    @FXML
    public void elegirAsiento() {
        if (cbxFuncion.getValue() == null) {
            error("Seleccione una función antes de elegir asiento");
            return;
        }
        Dialog<Set<Integer>> dialog = new Dialog<>();
        dialog.setTitle("Elegir asiento");
        dialog.setHeaderText("Selecciona tus asientos");
        ButtonType aceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(aceptar, ButtonType.CANCEL);

        VBox contenido = new VBox(14);
        contenido.setPadding(new Insets(12));
        Label pantalla = new Label("PANTALLA");
        pantalla.setMaxWidth(Double.MAX_VALUE);
        pantalla.setAlignment(javafx.geometry.Pos.CENTER);
        pantalla.setStyle("-fx-background-color: #d6a83f; -fx-text-fill: #17130b; -fx-font-weight: bold; -fx-padding: 9; -fx-background-radius: 6;");
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        Set<Integer> temporal = new LinkedHashSet<>(asientosElegidos);
        Set<Integer> ocupados = new LinkedHashSet<>();
        Long funcionId = Long.parseLong(cbxFuncion.getValue().getKey());
        for (VentCarrito linea : carrito) {
            if (linea.getIdProducto() == null || !linea.getIdProducto().getIdProducto().equals(funcionId)) continue;
            if (linea.getAsientos() != null && !linea.getAsientos().isBlank()) {
                for (String valor : linea.getAsientos().split(",\\s*")) {
                    try { ocupados.add(Integer.parseInt(valor)); } catch (NumberFormatException ignored) { }
                }
            }
        }
        for (int numero = 1; numero <= 27; numero++) {
            final int asiento = numero;
            ToggleButton boton = new ToggleButton(String.valueOf(numero));
            boton.setPrefSize(42, 36);
            boolean ocupado = ocupados.contains(numero);
            boton.setDisable(ocupado);
            boton.setSelected(temporal.contains(numero));
            boton.setStyle(estiloAsiento(boton.isSelected(), ocupado));
            boton.setOnAction(e -> {
                if (boton.isSelected()) temporal.add(asiento); else temporal.remove(asiento);
                boton.setStyle(estiloAsiento(boton.isSelected(), ocupado));
            });
            grid.add(boton, (numero - 1) % 9, (numero - 1) / 9);
        }
        contenido.getChildren().addAll(pantalla, grid);
        dialog.getDialogPane().setContent(contenido);
        dialog.setResultConverter(button -> button == aceptar ? temporal : null);
        dialog.showAndWait().ifPresent(seleccion -> {
            asientosElegidos.clear();
            asientosElegidos.addAll(seleccion);
            if (!seleccion.isEmpty()) txtCantFuncion.setText(String.valueOf(seleccion.size()));
            limpiarMensaje();
        });
    }

    private String estiloAsiento(boolean seleccionado, boolean ocupado) {
        String fondo = ocupado ? "#9a3434" : seleccionado ? "#d6a83f" : "#34312a";
        String texto = seleccionado && !ocupado ? "#17130b" : "#f3f0e8";
        return "-fx-background-color: " + fondo + "; -fx-text-fill: " + texto
                + "; -fx-border-color: #b88e32; -fx-border-radius: 5; -fx-background-radius: 5;";
    }

    @FXML
    public void agregarFuncion() {
        limpiarMensaje();
        if (clienteSeleccionado() == null) {
            error("Seleccione primero el cliente");
            return;
        }
        ComboBoxOption op = cbxFuncion.getValue();
        if (op == null) {
            error("Seleccione una función");
            return;
        }
        Integer cant = parseCantidad(txtCantFuncion.getText());
        if (cant == null) {
            error("La cantidad debe ser un número entero mayor a 0");
            return;
        }
        if (asientosElegidos.isEmpty() || asientosElegidos.size() != cant) {
            error("Elige la misma cantidad de asientos que de entradas");
            return;
        }
        Producto f = funcionService.findByid(Long.parseLong(op.getKey()));
        VentCarrito linea = carrito.stream()
                .filter(l -> l.getIdProducto() != null && l.getIdProducto().getIdProducto().equals(f.getIdProducto()))
                .findFirst().orElse(null);
        if (linea == null) {
            carrito.add(VentCarrito.builder()
                    .idCarrito(secuenciaCarrito++)
                    .idProducto(f)
                    .nombreProducto(op.getValue())
                    .asientos(asientosElegidos.stream().sorted().map(String::valueOf).collect(java.util.stream.Collectors.joining(", ")))
                    .cantidad((double) cant)
                    .punitario(f.getPu())
                    .descuento(0.0)
                    .ptotal(0.0)
                    .tipoItem(TIPO_FUNCION)
                    .estado(0)
                    .idUsuario(SesionUsuario.getUsuario())
                    .build());
        } else {
            linea.setCantidad(linea.getCantidad() + cant);
            String nuevosAsientos = asientosElegidos.stream().sorted().map(String::valueOf).collect(java.util.stream.Collectors.joining(", "));
            linea.setAsientos(linea.getAsientos() == null || linea.getAsientos().isBlank()
                    ? nuevosAsientos : linea.getAsientos() + ", " + nuevosAsientos);
        }
        asientosElegidos.clear();
        recalcularCarrito();
    }

    @FXML
    public void agregarComida() {
        limpiarMensaje();
        if (clienteSeleccionado() == null) {
            error("Seleccione primero el cliente");
            return;
        }
        ComboBoxOption op = cbxComida.getValue();
        if (op == null) {
            error("Seleccione un producto de comida");
            return;
        }
        Integer cant = parseCantidad(txtCantComida.getText());
        if (cant == null) {
            error("La cantidad debe ser un número entero mayor a 0");
            return;
        }
        Comida m = comidaService.findByid(Long.parseLong(op.getKey()));
        VentCarrito linea = carrito.stream()
                .filter(l -> l.getIdComida() != null && l.getIdComida().getIdComida().equals(m.getIdComida()))
                .findFirst().orElse(null);
        int enCarrito = linea == null ? 0 : linea.getCantidad().intValue();
        if (enCarrito + cant > m.getStockDisponible()) {
            error("Stock insuficiente de " + m.getNombre() + ". Disponible: " + (m.getStockDisponible() - enCarrito));
            return;
        }
        if (linea == null) {
            carrito.add(VentCarrito.builder()
                    .idCarrito(secuenciaCarrito++)
                    .idComida(m)
                    .nombreProducto(m.getNombre())
                    .cantidad((double) cant)
                    .punitario(m.getPrecio())
                    .descuento(0.0)
                    .ptotal(0.0)
                    .tipoItem(TIPO_COMIDA)
                    .estado(0)
                    .idUsuario(SesionUsuario.getUsuario())
                    .build());
        } else {
            linea.setCantidad(linea.getCantidad() + cant);
        }
        recalcularCarrito();
    }

    @FXML
    public void quitarSeleccion() {
        limpiarMensaje();
        VentCarrito sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            error("Seleccione una fila del carrito");
            return;
        }
        carrito.remove(sel);
        recalcularCarrito();
    }

    @FXML
    public void vaciarCarrito() {
        limpiarMensaje();
        carrito.clear();
        recalcularCarrito();
    }

    // El descuento por edad solo se aplica a las funciones; la comida va a precio normal.
    private void recalcularCarrito() {
        Cliente c = clienteSeleccionado();
        Integer edad = c == null ? null : c.getEdad();
        for (VentCarrito l : carrito) {
            double bruto = l.getPunitario() * l.getCantidad();
            double desc = TIPO_FUNCION.equals(l.getTipoItem()) ? DescuentoEdad.calcular(bruto, edad) : 0.0;
            l.setDescuento(desc);
            l.setPtotal(Math.round((bruto - desc) * 100) / 100.0);
        }
        tableView.refresh();
        actualizarTotal();
    }

    // ---------- confirmar venta ----------
    @FXML
    public void confirmarVenta() {
        limpiarMensaje();
        Cliente cliente = clienteSeleccionado();
        if (cliente == null) {
            error("Seleccione el cliente");
            return;
        }
        if (carrito.isEmpty()) {
            error("El carrito está vacío");
            return;
        }
        if (!SesionUsuario.haySesion()) {
            error("Debe iniciar sesión");
            return;
        }

        Venta venta = new Venta();
        venta.setDniruc(cliente);
        venta.setIdUsuario(SesionUsuario.getUsuario());
        venta.setFechaGener(LocalDateTime.now());

        List<VentaDetalle> detalleFunciones = new ArrayList<>();
        List<VentaDetalleProducto> detalleComida = new ArrayList<>();
        double total = 0.0;
        for (VentCarrito l : carrito) {
            if (TIPO_FUNCION.equals(l.getTipoItem())) {
                VentaDetalle d = VentaDetalle.builder()
                        .pu(l.getPunitario())
                        .cantidad(l.getCantidad())
                        .idProducto(l.getIdProducto())
                        .build();
                d.aplicarDescuentoPorEdad(cliente.getEdad());
                detalleFunciones.add(d);
                total += d.getSubtotal();
            } else {
                double sub = l.getPunitario() * l.getCantidad();
                detalleComida.add(VentaDetalleProducto.builder()
                        .idProducto(l.getIdComida())
                        .cantidad(l.getCantidad().intValue())
                        .pu(l.getPunitario())
                        .subtotal(sub)
                        .build());
                total += sub;
            }
        }
        venta.setDetalleVenta(detalleFunciones);
        venta.setDetalleComida(detalleComida);
        // Montos de la venta. IGV, serie y número quedan para la etapa del comprobante.
        total = Math.round(total * 100) / 100.0;
        venta.setPrecioBase(total);
        venta.setIgv(0.0);
        venta.setPrecioTotal(total);

        try {
            Venta guardada = ventaService.registrarVenta(venta);
            carrito.clear();
            recalcularCarrito();
            cargarComidas(); // refresca el stock mostrado
            actualizarInfoComida();
            Stage stage = (Stage) miContenedor.getScene().getWindow();
            double w = stage.getWidth() / 1.5, h = stage.getHeight() / 2;
            Toast.showToast(stage, "Venta N° " + guardada.getId() + " registrada. Total S/ "
                    + String.format(Locale.US, "%.2f", guardada.getPrecioTotal()), 2500, w, h);
        } catch (RuntimeException e) {
            error(e.getMessage());
        }
    }

    // ---------- utilidades ----------
    private Integer parseCantidad(String texto) {
        try {
            int n = Integer.parseInt(texto == null ? "" : texto.trim());
            return n > 0 ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void error(String mensaje) {
        lbnMsg.setText(mensaje);
        lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
    }

    private void limpiarMensaje() {
        lbnMsg.setText("");
    }
}
