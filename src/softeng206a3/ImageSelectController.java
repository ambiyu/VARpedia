package softeng206a3;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.net.URL;

import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ImageSelectController implements Initializable {

    private String _searchTerm;
    private ChunkManagerController _previousScene;
    private int numOfImages;

    @FXML
    private Button createBtn;

    @FXML
    private Spinner<Integer> numberChoice;

    @FXML
    private TextField fileNameInput;

    @FXML
    private Label progress;


    public ImageSelectController(String searchTerm, ChunkManagerController reference) {
        _searchTerm = searchTerm;
        _previousScene = reference;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SpinnerValueFactory<Integer> spinnerValues = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        numberChoice.setValueFactory(spinnerValues);
        createBtn.setDisable(true);
        progress.setVisible(false);
    }


    @FXML
    public void handleCreate() {
        if (!fileNameInput.getText().matches("^[a-zA-Z0-9\\_-]+")) {
            displayError("Invalid character(s) in creation name. Only letters, numbers, hyphens and underscores are allowed.");
        } else if (isConflicting("creations", fileNameInput.getText(), "mp4")) {
            displayError("Creation with the same name already exists. Please enter another name.");
        } else {

            progress.setVisible(true);
            createBtn.setDisable(true);
            new Thread(() -> {
                try {

                    //Main.execCmd("mkdir .temp/" + _creationName);

                    //Download Images
                    ImageDownload downloader = new ImageDownload();
                    numOfImages = downloader.downloadImages(_searchTerm, numberChoice.getValue());

                    // combine chunks into a single audio file
                    String combineAudioCmd = "sox .temp/chunks/*.wav .temp/combinedAudio.wav";
                    Main.execCmd(combineAudioCmd);

                    // get length of audio
                    String cmd = "soxi -D '.temp/combinedAudio.wav'";
                    Process process = new ProcessBuilder("bash", "-c", cmd).start();
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        double length = Double.parseDouble(stdout.readLine()) + 1;

                        double lengthOfImage = numOfImages/length;

                        // combine images into a video
                        //Main.execCmd("cat .temp/images/*.jpg | ffmpeg -f image2pipe -framerate 1 -i - -i .temp/combinedAudio.wav -r " + lengthOfImage + " -pattern_type glob -c:v libx264 -pix_fmt yuv420p -vf \"scale=-2:min(1080\\,trunc(ih/2)*2)\" -r 25 -max_muxing_queue_size 1024 -y creations/" + fileNameInput.getText() + ".mp4");
                        Main.execCmd("ffmpeg -framerate " + lengthOfImage + " -pattern_type glob -i '.temp/images/*.jpg' -c:v libx264 -vf \"scale=-2:min(1080\\,trunc(ih/2)*2)\" -r 25 .temp/combinedImages.mp4");

                        // create video and then combine audio/video into one
                        Main.execCmd("ffmpeg -i .temp/combinedImages.mp4 -vf drawtext=\"fontfile=resources/myFont.ttf: text='" + _searchTerm + "': fontcolor=white: fontsize=50: x=(w-text_w)/2: y=(h-text_h)/2\" -codec:a copy -t " + length + " -r 25 .temp/vidWithWord.mp4");
                        Main.execCmd("ffmpeg -i \".temp/vidWithWord.mp4\" -i \".temp/combinedAudio.wav\" -shortest creations/" + fileNameInput.getText() + ".mp4");

                        Platform.runLater(() -> {

                            progress.setVisible(false);
                            createBtn.setDisable(false);
                            //create a success alert
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText("Successfully created creation \"" + fileNameInput.getText() + "\"");
                            alert.showAndWait();
                        });
                    } else {
                        Platform.runLater(() -> {
                            displayError("An error occurred while attempting to generate creation");
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }



    @FXML
    public void handleBack() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChunkManager.fxml"));
            loader.setController(_previousScene);
            Parent parent = loader.load();
            Scene createScene = new Scene(parent);
            Stage window = Main.getPrimaryStage();
            window.setScene(createScene);
            window.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void returnToMenu() {
        Main.switchScene(getClass().getResource("Menu.fxml"));
    }

    @FXML
    public void allowCreation() {
        if(fileNameInput.getText().trim().isEmpty()){
            createBtn.setDisable(true);

        }
        else {
            createBtn.setDisable(false);
        }
    }

    private boolean isConflicting(String folder, String name, String format) {
        try {
            String cmd = "test -f \"" + folder + "/" + name + "." + format + "\"";
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            int exitStatus = process.waitFor();

            if (exitStatus == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void displayError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
