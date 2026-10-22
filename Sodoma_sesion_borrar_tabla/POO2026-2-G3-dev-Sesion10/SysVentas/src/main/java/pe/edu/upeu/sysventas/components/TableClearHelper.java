package pe.edu.upeu.sysventas.components;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.function.IntSupplier;

/**
 * Componente reutilizable para borrar TODOS los datos que muestra una TableView.
 *
 * Flujo ordenado (siempre en este orden):
 *   1. Validar     -> si la tabla está vacía avisa con un Toast y termina.
 *   2. Confirmar   -> Alert con el detalle (qué se borra y cuántos registros).
 *   3. Borrar      -> ejecuta la acción de borrado que le pasa el controlador
 *                     (normalmente un método del servicio, ej: daoC.deleteCarAll).
 *   4. Limpiar UI  -> vacía la TableView.
 *   5. Notificar   -> Toast con el detalle de lo borrado.
 *   6. Refrescar   -> ejecuta el callback (ej: listar() para recalcular totales).
 *
 * Uso igual que TableViewHelper: se instancia con el tipo de la tabla.
 */
public class TableClearHelper<T> {

    /**
     * @param stage          ventana dueña del diálogo y del Toast
     * @param tableView      tabla cuyos datos se van a borrar
     * @param titulo         título del diálogo (ej: "Vaciar carrito de venta")
     * @param detalle        texto que explica qué se va a borrar (cliente, módulo, etc.)
     * @param accionBorrado  acción que borra en la BD y devuelve la cantidad de filas borradas
     * @param alTerminar     callback posterior al borrado (puede ser null)
     * @return true si se borró, false si se canceló, no había datos o hubo error
     */
    public boolean confirmarYBorrar(Stage stage,
                                    TableView<T> tableView,
                                    String titulo,
                                    String detalle,
                                    IntSupplier accionBorrado,
                                    Runnable alTerminar) {

        // 1. VALIDAR
        int cantidad = tableView.getItems().size();
        if (cantidad == 0) {
            mostrarToast(stage, "No hay datos en la tabla para borrar");
            return false;
        }

        // 2. CONFIRMAR
        if (!confirmar(stage, titulo, detalle, cantidad)) {
            return false;
        }

        try {
            // 3. BORRAR
            int borrados = accionBorrado.getAsInt();

            // 4. LIMPIAR UI
            tableView.getItems().clear();

            // 5. NOTIFICAR
            mostrarToast(stage, mensajeExito(borrados));

            // 6. REFRESCAR
            if (alTerminar != null) alTerminar.run();
            return true;

        } catch (Exception e) {
            mostrarError(stage, titulo, e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------- privados

    private boolean confirmar(Stage stage, String titulo, String detalle, int cantidad) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (stage != null) alert.initOwner(stage);
        alert.setTitle("Confirmación");
        alert.setHeaderText(titulo);
        alert.setContentText(
                detalle + "\n\n"
                + "Registros a borrar: " + cantidad + "\n\n"
                + "Esta acción no se puede deshacer. ¿Deseas continuar?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private String mensajeExito(int borrados) {
        return borrados == 1
                ? "Se borró 1 registro correctamente!!"
                : "Se borraron " + borrados + " registros correctamente!!";
    }

    private void mostrarError(Stage stage, String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if (stage != null) alert.initOwner(stage);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText("No se pudieron borrar los datos.\n" + mensaje);
        alert.showAndWait();
    }

    /** Toast centrado horizontalmente en la parte superior de la ventana. */
    private void mostrarToast(Stage stage, String mensaje) {
        if (stage == null) return;
        double x = stage.getX() + stage.getWidth() / 2 - 150;
        double y = stage.getY() + 80;
        Toast.showToast(stage, mensaje, 2500, x, y);
    }
}
