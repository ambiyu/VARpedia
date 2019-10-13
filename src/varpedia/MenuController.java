package varpedia;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MenuController {

    @FXML
    private void toMyCreations() {
        Main.switchScene(getClass().getResource("CreationsList.fxml"));
    }

    @FXML
    private void toCreate() {
        // remove previous chunks/audio files if there are any
        Main.execCmd("rm -r .temp/*");

        Main.switchScene(getClass().getResource("Search.fxml"));
    }

    @FXML
    private void toQuiz() {
        try {
            String cmd = "ls creations/*.mp4 | wc -l";
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int numCreations = Integer.parseInt(stdout.readLine());

            if (numCreations < 4) {
                int required = 4 - numCreations;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText(null);
                alert.setContentText("You need at least 4 creations to start the quiz. Please create " + required + " more creation(s).");
                alert.showAndWait();
            } else {
                Main.switchScene(getClass().getResource("Quiz.fxml"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
