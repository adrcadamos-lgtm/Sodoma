package pe.edu.upeu.sysventas.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.config.SesionUsuario;
import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.service.IUsuarioService;

@RequiredArgsConstructor
public class LoginController {
    private final IUsuarioService us;

    @FXML TextField txtUsuario;
    @FXML PasswordField pwdClave;
    @FXML Label lbnMsg;

    // Lo define SysVentas: qué hacer cuando el login es correcto (abrir la pantalla principal).
    private Runnable onLoginExitoso;

    public void setOnLoginExitoso(Runnable onLoginExitoso) {
        this.onLoginExitoso = onLoginExitoso;
    }

    @FXML
    public void iniciarSesion() {
        try {
            Usuario u = us.login(txtUsuario.getText(), pwdClave.getText());
            SesionUsuario.iniciar(u);
            pwdClave.clear();
            lbnMsg.setText("");
            if (onLoginExitoso != null) {
                onLoginExitoso.run();
            }
        } catch (RuntimeException e) {
            lbnMsg.setText(e.getMessage());
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            pwdClave.clear();
            pwdClave.requestFocus();
        }
    }

    @FXML
    public void accederDirectamente() {
        Usuario usuarioOperativo = us.findAll().stream()
                .filter(usuario -> usuario.getEstado() == null || "A".equalsIgnoreCase(usuario.getEstado()))
                .filter(usuario -> "vendedor".equalsIgnoreCase(usuario.getUsuario()))
                .findFirst()
                .orElseGet(() -> us.findAll().stream()
                        .filter(usuario -> usuario.getEstado() == null || "A".equalsIgnoreCase(usuario.getEstado()))
                        .findFirst()
                        .orElse(null));
        if (usuarioOperativo == null) {
            lbnMsg.setText("No hay usuarios activos para habilitar el acceso directo.");
            lbnMsg.setStyle("-fx-text-fill: #e2b64e; -fx-font-size: 14px;");
            return;
        }
        SesionUsuario.iniciarComoInvitado(usuarioOperativo);
        pwdClave.clear();
        lbnMsg.setText("");
        if (onLoginExitoso != null) {
            onLoginExitoso.run();
        }
    }
}
