module pe.edu.upeu.dakar {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens pe.edu.upeu.dakar to javafx.fxml;
    exports pe.edu.upeu.dakar;
}