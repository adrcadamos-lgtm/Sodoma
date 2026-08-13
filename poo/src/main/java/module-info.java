module pe.edu.upeu.poo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens pe.edu.upeu.poo to javafx.fxml;
    exports pe.edu.upeu.poo;
}