package pe.edu.upeu.sysventas.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.components.ToltipCustom;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.enums.TipoComida;
import pe.edu.upeu.sysventas.model.Comida;
import pe.edu.upeu.sysventas.service.IComidaService;

import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ComidaController {
    private final IComidaService cs;

    @FXML TextField txtNombre, txtPrecio, txtStock;
    @FXML ComboBox<ComboBoxOption> cbxTipo;
    @FXML private TableView<Comida> tableView;
    @FXML Label lbnMsg;
    @FXML private AnchorPane miContenedor;

    Comida formulario;
    Long idComidaCE = 0L;
    private Validator validator;
    private final ToltipCustom ttc = new ToltipCustom();

    @FXML
    public void initialize() {
        cbxTipo.getItems().addAll(cs.listarTipos());
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        TableViewHelper<Comida> helper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID", new ColumnInfo("idComida", 60.0));
        columns.put("Nombre", new ColumnInfo("nombre", 220.0));
        columns.put("Tipo", new ColumnInfo("tipo", 110.0));
        columns.put("Precio", new ColumnInfo("precio", 100.0));
        columns.put("Stock", new ColumnInfo("stockDisponible", 90.0));

        Consumer<Comida> updateAction = this::editForm;
        Consumer<Comida> deleteAction = c -> {
            cs.delete(c.getIdComida());
            mensajeToast("Se eliminó correctamente!!");
            clearForm();
            listar();
        };
        helper.addColumnsInOrderWithSize(tableView, columns, updateAction, deleteAction);
        tableView.setTableMenuButtonVisible(true);
        listar();
    }

    public void listar() {
        tableView.getItems().clear();
        tableView.getItems().addAll(FXCollections.observableArrayList(cs.findAll()));
    }

    @FXML
    public void validarFormulario() {
        formulario = new Comida();
        formulario.setNombre(txtNombre.getText() == null ? null : txtNombre.getText().trim());
        ComboBoxOption t = cbxTipo.getSelectionModel().getSelectedItem();
        formulario.setTipo(t == null ? null : TipoComida.valueOf(t.getKey()));
        formulario.setPrecio(parseDoubleSafe(txtPrecio.getText()));
        formulario.setStockDisponible(parseEnteroSafe(txtStock.getText()));

        List<ConstraintViolation<Comida>> violaciones = validator.validate(formulario).stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString())).toList();

        if (violaciones.isEmpty()) {
            procesarFormulario();
        } else {
            mostrarErroresValidacion(violaciones);
        }
    }

    private void mostrarErroresValidacion(List<ConstraintViolation<Comida>> violaciones) {
        limpiarError();
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("nombre", txtNombre);
        campos.put("tipo", cbxTipo);
        campos.put("precio", txtPrecio);
        campos.put("stockDisponible", txtStock);

        String primerMensaje = null;
        Control primerCtrl = null;
        for (Map.Entry<String, Control> campo : campos.entrySet()) {
            Optional<ConstraintViolation<Comida>> v = violaciones.stream()
                    .filter(x -> x.getPropertyPath().toString().equals(campo.getKey())).findFirst();
            if (v.isPresent()) {
                ttc.marcarError(campo.getValue(), v.get().getMessage().trim());
                if (primerMensaje == null) {
                    primerMensaje = v.get().getMessage();
                    primerCtrl = campo.getValue();
                }
            }
        }
        if (primerMensaje != null) {
            lbnMsg.setText(primerMensaje);
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            Control ctrl = primerCtrl;
            Platform.runLater(ctrl::requestFocus);
        }
    }

    private void procesarFormulario() {
        lbnMsg.setText("");
        limpiarError();
        if (idComidaCE > 0L) {
            formulario.setIdComida(idComidaCE);
            cs.update(idComidaCE, formulario);
            mensajeToast("Se actualizó correctamente!!");
        } else {
            cs.save(formulario);
            mensajeToast("Se guardó correctamente!!");
        }
        clearForm();
        listar();
    }

    private void mensajeToast(String mensaje) {
        Stage stage = (Stage) miContenedor.getScene().getWindow();
        double w = stage.getWidth() / 1.5, h = stage.getHeight() / 2;
        Toast.showToast(stage, mensaje, 2000, w, h);
    }

    private double parseDoubleSafe(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Integer parseEnteroSafe(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void editForm(Comida c) {
        limpiarError();
        txtNombre.setText(c.getNombre());
        txtPrecio.setText(c.getPrecio().toString());
        txtStock.setText(c.getStockDisponible().toString());
        cbxTipo.getSelectionModel().select(
                cbxTipo.getItems().stream()
                        .filter(o -> o.getKey().equals(c.getTipo().name()))
                        .findFirst().orElse(null));
        idComidaCE = c.getIdComida();
    }

    public void limpiarError() {
        List.of(txtNombre, cbxTipo, txtPrecio, txtStock).forEach(c -> {
            c.getStyleClass().remove("text-field-error");
            ttc.limpiarCampo(c);
        });
    }

    @FXML
    public void clearForm() {
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        cbxTipo.getSelectionModel().clearSelection();
        idComidaCE = 0L;
        lbnMsg.setText("");
        limpiarError();
    }
}
