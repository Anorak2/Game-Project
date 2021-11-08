module com.example.gameproject {
    opens com.Main.Games to javafx.fxml ;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;

    opens com.Main to javafx.fxml;
    exports com.Main;
}