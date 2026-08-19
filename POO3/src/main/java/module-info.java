module pe.edu.upeu.poo3 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens pe.edu.upeu.poo3 to javafx.fxml;
    exports pe.edu.upeu.poo3;
}