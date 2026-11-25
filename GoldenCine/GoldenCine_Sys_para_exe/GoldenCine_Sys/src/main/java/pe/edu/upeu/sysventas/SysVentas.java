package pe.edu.upeu.sysventas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.geometry.Orientation;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import pe.edu.upeu.sysventas.config.AppContext;
import pe.edu.upeu.sysventas.config.SesionUsuario;
import pe.edu.upeu.sysventas.controller.LoginController;

import java.io.IOException;

public class SysVentas extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        AppContext context=AppContext.getInstance();
        Screen screen= Screen.getPrimary();
        Rectangle2D r2d=screen.getVisualBounds();

        Scene scene = new Scene(new BorderPane(), r2d.getWidth(), r2d.getHeight()-50);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Primero se muestra el login; la pantalla principal solo se abre si el login es correcto.
        mostrarLogin(scene, context);

        stage.setTitle("Goldencineplanet");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/goldencineplanet-icon.png")));
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarLogin(Scene scene, AppContext context) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            fxmlLoader.setControllerFactory(context::getBean);
            Parent loginRaiz = fxmlLoader.load();
            LoginController login = fxmlLoader.getController();
            login.setOnLoginExitoso(() -> mostrarPrincipal(scene, context));
            scene.setRoot(loginRaiz);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void mostrarPrincipal(Scene scene, AppContext context) {
        // Barra superior con un botón por CRUD; abajo se muestra el CRUD elegido.
        BorderPane raiz = new BorderPane();
        ToolBar barra = new ToolBar();
        raiz.setTop(barra);

        agregarCrud(barra, raiz, context, "Taquilla", "/view/taquilla.fxml");
        barra.getItems().add(new Separator(Orientation.VERTICAL));
        agregarCrud(barra, raiz, context, "Ventas", "/view/main_venta.fxml");
        barra.getItems().add(new Separator(Orientation.VERTICAL));
        agregarCrud(barra, raiz, context, "Clientes", "/view/main_cliente.fxml");
        barra.getItems().add(new Separator(Orientation.VERTICAL));
        agregarCrud(barra, raiz, context, "Salas", "/view/main_sala.fxml");
        agregarCrud(barra, raiz, context, "Funciones", "/view/main_produucto.fxml");
        agregarCrud(barra, raiz, context, "Comida", "/view/main_comida.fxml");

        // Usuario que inició sesión + botón para cerrar sesión (a la derecha).
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        Label lblUsuario = new Label("Usuario: " + (SesionUsuario.esInvitado()
                ? "Invitado" : SesionUsuario.getUsuario().getUsuario()));
        lblUsuario.getStyleClass().add("usuario-actual");
        Button btnSalir = new Button("Cerrar sesión");
        btnSalir.getStyleClass().addAll("btn", "btn-danger");
        btnSalir.setOnAction(e -> {
            SesionUsuario.cerrar();
            mostrarLogin(scene, context);
        });
        barra.getItems().addAll(espacio, lblUsuario, btnSalir);

        mostrarCrud(raiz, context, "/view/taquilla.fxml");
        scene.setRoot(raiz);
    }

    private void agregarCrud(ToolBar barra, BorderPane raiz, AppContext context, String titulo, String fxml) {
        Button btn = new Button(titulo);
        btn.getStyleClass().addAll("btn", "btn-primary");
        btn.setOnAction(e -> mostrarCrud(raiz, context, fxml));
        barra.getItems().add(btn);
    }

    private void mostrarCrud(BorderPane raiz, AppContext context, String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
            fxmlLoader.setControllerFactory(context::getBean);
            raiz.setCenter(fxmlLoader.load());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
