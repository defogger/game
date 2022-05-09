package patryk.game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import patryk.game.View.ViewGame;
import java.io.IOException;

public class GameApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        ViewGame view = new ViewGame();

        Group root = new Group();
        root.getChildren().add(view.getCanvas());
        stage.setScene(new Scene(root));
        stage.show();

        new Thread(() -> {
            while (true)
            {
                try
                {
                    Thread.sleep(20);
                }
                catch (Exception e) {}
                Platform.runLater(() -> {
                    synchronized (view)
                    {
                        view.reDraw();
                    }
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}