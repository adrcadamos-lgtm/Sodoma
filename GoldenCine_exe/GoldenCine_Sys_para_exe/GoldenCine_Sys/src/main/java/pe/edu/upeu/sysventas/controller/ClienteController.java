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
import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.service.IClienteService;

import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ClienteController {
    private final IClienteService cs;

    @FXML TextField txtDniRuc, txtNombres, txtRepLegal, txtEdad;
    @FXML ComboBox<ComboBoxOption> cbxTipoDoc;
    @FXML private TableView<Cliente> tableView;
    @FXML Label lbnMsg;
    @FXML private AnchorPane miContenedor;

    Cliente formulario;
    // Documento del cliente que se está editando; null = cliente nuevo.
    String dniEnEdicion = null;
    private Validator validator;
    private final ToltipCustom ttc = new ToltipCustom();

    @FXML
    public void initialize() {
        cbxTipoDoc.getItems().addAll(cs.listarTipoDocumento());
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        TableViewHelper<Cliente> helper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("Documento", new ColumnInfo("dniruc", 120.0));
        columns.put("Tipo Doc.", new ColumnInfo("tipoDocumento", 100.0));
        columns.put("Nombres", new ColumnInfo("nombres", 240.0));
        columns.put("Rep. Legal", new ColumnInfo("repLegal", 180.0));
        columns.put("Edad", new ColumnInfo("edad", 70.0));

        Consumer<Cliente> updateAction = this::editForm;
        Consumer<Cliente> deleteAction = c -> {
            cs.delete(c.getDniruc());
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
        formulario = new Cliente();
        formulario.setDniruc(txtDniRuc.getText() == null ? null : txtDniRuc.getText().trim());
        formulario.setNombres(txtNombres.getText() == null ? null : txtNombres.getText().trim());
        formulario.setRepLegal(txtRepLegal.getText());
        ComboBoxOption td = cbxTipoDoc.getSelectionModel().getSelectedItem();
        formulario.setTipoDocumento(td == null ? null : TipoDocumento.valueOf(td.getKey()));
        formulario.setEdad(parseEnteroSafe(txtEdad.getText()));

        List<ConstraintViolation<Cliente>> violaciones = validator.validate(formulario).stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString())).toList();

        if (violaciones.isEmpty()) {
            procesarFormulario();
        } else {
            mostrarErroresValidacion(violaciones);
        }
    }

    private void mostrarErroresValidacion(List<ConstraintViolation<Cliente>> violaciones) {
        limpiarError();
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("dniruc", txtDniRuc);
        campos.put("nombres", txtNombres);
        campos.put("tipoDocumento", cbxTipoDoc);
        campos.put("edad", txtEdad);

        String primerMensaje = null;
        Control primerCtrl = null;
        for (Map.Entry<String, Control> campo : campos.entrySet()) {
            Optional<ConstraintViolation<Cliente>> v = violaciones.stream()
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
        try {
            if (dniEnEdicion != null) {
                cs.update(dniEnEdicion, formulario);
                mensajeToast("Se actualizó correctamente!!");
            } else {
                cs.save(formulario);
                mensajeToast("Se guardó correctamente!!");
            }
        } catch (RuntimeException e) {
            lbnMsg.setText(e.getMessage());
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            return;
        }
        clearForm();
        listar();
    }

    private void mensajeToast(String mensaje) {
        Stage stage = (Stage) miContenedor.getScene().getWindow();
        double w = stage.getWidth() / 1.5, h = stage.getHeight() / 2;
        Toast.showToast(stage, mensaje, 2000, w, h);
    }

    private Integer parseEnteroSafe(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void editForm(Cliente c) {
        limpiarError();
        txtDniRuc.setText(c.getDniruc());
        txtDniRuc.setDisable(true); // el documento es el identificador: no se cambia al editar
        txtNombres.setText(c.getNombres());
        txtRepLegal.setText(c.getRepLegal());
        txtEdad.setText(c.getEdad() == null ? "" : c.getEdad().toString());
        cbxTipoDoc.getSelectionModel().select(
                cbxTipoDoc.getItems().stream()
                        .filter(o -> c.getTipoDocumento() != null && o.getKey().equals(c.getTipoDocumento().name()))
                        .findFirst().orElse(null));
        dniEnEdicion = c.getDniruc();
    }

    public void limpiarError() {
        List.of(txtDniRuc, txtNombres, cbxTipoDoc, txtEdad).forEach(c -> {
            c.getStyleClass().remove("text-field-error");
            ttc.limpiarCampo(c);
        });
    }

    @FXML
    public void clearForm() {
        txtDniRuc.clear();
        txtDniRuc.setDisable(false);
        txtNombres.clear();
        txtRepLegal.clear();
        txtEdad.clear();
        cbxTipoDoc.getSelectionModel().clearSelection();
        dniEnEdicion = null;
        lbnMsg.setText("");
        limpiarError();
    }
}
