package softeng206a3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {
    private static Stage _primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        _primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("Menu.fxml"));
        Parent layout = loader.load();
        Scene scene = new Scene(layout);
        primaryStage.setScene(scene);
        primaryStage.setTitle("WikiSpeak");
        primaryStage.show();


        String cmd = "mkdir creations";
        new ProcessBuilder("bash", "-c", cmd).start();
        cmd = "mkdir .temp";
        new ProcessBuilder("bash", "-c", cmd).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void switchScene(URL path) {
        try {
            Parent parent = FXMLLoader.load(path);
            Scene createScene = new Scene(parent);
            Stage window = _primaryStage;
            window.setScene(createScene);
            window.show();
        } catch (IOException ioex) {
            System.out.println("error switching scene");
        }
    }

    public static void execCmd(String cmd) {
        try {
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println("error executing command");
        }
    }
}
