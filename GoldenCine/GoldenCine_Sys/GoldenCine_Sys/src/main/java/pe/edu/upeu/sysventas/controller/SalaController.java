package pe.edu.upeu.sysventas.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.components.ToltipCustom;
import pe.edu.upeu.sysventas.model.Sala;
import pe.edu.upeu.sysventas.service.ISalaService;

import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
// OBJETO: AppContext crea un objeto SalaController con "new SalaController(...)".
public class SalaController {
    // POLIMORFISMO: el tipo es la interfaz ISalaService, pero el objeto real que llega es un SalaServiceImp.
    // ENCAPSULAMIENTO: atributo private; el controlador solo usa los métodos del servicio.
    private final ISalaService ss;

    @FXML TextField txtNombreSala, txtFiltroDato;
    @FXML ToggleButton tglEstado;

    @FXML
    private TableView<Sala> tableView;
    ObservableList<Sala> listarSala;

    Sala formulario;
    Long idSalaCE = 0L;

    @FXML Label lbnMsg;
    @FXML private AnchorPane miContenedor;
    Stage stage;
    private Validator validator;
    private final ToltipCustom ttc = new ToltipCustom();

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            stage = (Stage) miContenedor.getScene().getWindow();
            System.out.println("El título del stage es: " + stage.getTitle());
        });

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        TableViewHelper<Sala> tableViewHelper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID Sala", new ColumnInfo("idSala", 80.0));
        columns.put("Nombre", new ColumnInfo("nombre", 300.0));
        columns.put("Estado", new ColumnInfo("estado", 120.0));

        Consumer<Sala> updateAction = s -> {
            editForm(s);
            idSalaCE = s.getIdSala();
        };
        Consumer<Sala> deleteActtion = s -> {
            System.out.println(s.getIdSala());
            ss.delete(s.getIdSala());
            Stage stage = (Stage) miContenedor.getScene().getWindow();
            double w = stage.getWidth() / 1.5, h = stage.getHeight() / 2;
            Toast.showToast(stage, "Se eliminó correctamente!!", 2000, w, h);
            listar();
        };

        tableViewHelper.addColumnsInOrderWithSize(tableView, columns, updateAction, deleteActtion);
        tableView.setTableMenuButtonVisible(true);

        // Colorea la columna Estado: verde = Libre, rojo = Ocupada.
        @SuppressWarnings("unchecked")
        TableColumn<Sala, Object> colEstado = (TableColumn<Sala, Object>) tableView.getColumns().stream()
                .filter(c -> "Estado".equals(c.getText())).findFirst().orElse(null);
        if (colEstado != null) {
            colEstado.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    getStyleClass().removeAll("estado-libre", "estado-ocupada");
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());
                        getStyleClass().add("Libre".equals(item.toString()) ? "estado-libre" : "estado-ocupada");
                    }
                }
            });
        }

        // Modificador de estado: seleccionado = Libre, no seleccionado = Ocupada.
        tglEstado.selectedProperty().addListener((obs, anterior, nuevo) -> actualizarTextoEstado());
        actualizarTextoEstado();
        listar();
    }

    private void actualizarTextoEstado() {
        tglEstado.setText(tglEstado.isSelected() ? "Libre" : "Ocupada");
    }

    public void listar() {
        try {
            tableView.getItems().clear();
            listarSala = FXCollections.observableArrayList(ss.findAll());
            tableView.getItems().addAll(listarSala);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        System.out.println("Llego" + stage.getTitle());
    }

    @FXML
    public void validarFormulario() {
        // OBJETO: se crea un objeto Sala con los datos que escribe el usuario en el formulario.
        formulario = new Sala();
        formulario.setNombre(txtNombreSala.getText());
        formulario.setLibre(tglEstado.isSelected());

        Set<ConstraintViolation<Sala>> violaciones = validator.validate(formulario);
        List<ConstraintViolation<Sala>> violacionesOrdenadas = violaciones.stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString())).toList();

        if (violacionesOrdenadas.isEmpty()) {
            procesarFormulario();
        } else {
            mostrarErroresValidacion(violacionesOrdenadas);
        }
    }

    private void mostrarErroresValidacion(List<ConstraintViolation<Sala>> violaciones) {
        limpiarError();
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("nombre", txtNombreSala);

        LinkedHashMap<String, String> erroresOrdenados = new LinkedHashMap<>();
        final Control[] primerCtrl = {null};
        for (String campo : campos.keySet()) {
            violaciones.stream()
                    .filter(v -> v.getPropertyPath().toString().equals(campo))
                    .findFirst().ifPresent(v -> {

                        erroresOrdenados.put(campo, v.getMessage());

                        Control c = campos.get(campo);
                        if (c != null && !c.getStyleClass().contains("text-field-error")) {
                            if (c != null) ttc.marcarError(c, v.getMessage().trim());
                        }
                        if (primerCtrl[0] == null) primerCtrl[0] = c;
                    });
        }
        if (!erroresOrdenados.isEmpty()) {
            lbnMsg.setText(erroresOrdenados.entrySet().iterator().next().getValue());
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            if (primerCtrl[0] != null) Platform.runLater(primerCtrl[0]::requestFocus);
        }
    }

    private void procesarFormulario() {
        lbnMsg.setText("Formulario válido");
        lbnMsg.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
        Stage stage = (Stage) miContenedor.getScene().getWindow();
        limpiarError();
        double w = stage.getWidth() / 1.5, h = stage.getHeight() / 2;
        if (idSalaCE > 0L) {
            formulario.setIdSala(idSalaCE);
            ss.update(idSalaCE, formulario);
            Toast.showToast(stage, "Se actualizó correctamente!!", 2000, w, h);
        } else {
            ss.save(formulario);
            Toast.showToast(stage, "Se guardó correctamente!!", 2000, w, h);
        }
        clearForm();
        listar();
    }

    public void editForm(Sala sala) {
        txtNombreSala.setText(sala.getNombre());
        tglEstado.setSelected(Boolean.TRUE.equals(sala.getLibre()));
        idSalaCE = sala.getIdSala();
        limpiarError();
    }

    public void limpiarError() {
        List.of(txtNombreSala)
                .forEach(c -> {
                    c.getStyleClass().remove("text-field-error");
                    ttc.limpiarCampo(c);
                });
    }

    public void clearForm() {
        txtNombreSala.clear();
        tglEstado.setSelected(true);
        idSalaCE = 0L;
        limpiarError();
    }

}
