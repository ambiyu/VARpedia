package varpedia.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class Main extends Application {
    private static Stage _primaryStage;
    private static Process _currentProcess;

    @Override
    public void start(Stage primaryStage) throws Exception {
        _primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/varpedia/fxml/Menu.fxml"));
        Parent layout = loader.load();
        Scene scene = new Scene(layout);
        primaryStage.setScene(scene);
        primaryStage.setTitle("VARpedia");
        primaryStage.show();

        String cmd = "mkdir creations";
        new ProcessBuilder("bash", "-c", cmd).start();
        cmd = "mkdir .temp";
        new ProcessBuilder("bash", "-c", cmd).start();
        cmd = "mkdir .quiz";
        new ProcessBuilder("bash", "-c", cmd).start();

        // remove .temp folder and destroy process (if there is a playing audio) on exit
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            execCmd("rm -r .temp");

            if (_currentProcess != null) {
                _currentProcess.destroyForcibly();
            }
        });
    }

    public static Stage getPrimaryStage() {
        return _primaryStage;
    }

    public static Process getCurrentProcess() {
        return _currentProcess;
    }

    public static void createNewProcess(String cmd) {
        try {
            _currentProcess = new ProcessBuilder("bash", "-c", cmd).start();
            _currentProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static int execCmd(String cmd) {
        try {
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println("error executing command");
        }
        return 1;
    }

    public static boolean returnToMenuWarning() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to return to the main menu? Current progress will be lost.");
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == ButtonType.OK;
    }
}
