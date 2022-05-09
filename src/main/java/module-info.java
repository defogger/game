module patryk.game {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens patryk.game to javafx.fxml;
    exports patryk.game;
}