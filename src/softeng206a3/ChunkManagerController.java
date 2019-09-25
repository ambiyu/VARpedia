package softeng206a3;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChunkManagerController implements Initializable {

    private String _searchTerm;
    private String _text;
    private List<Chunk> _chunks;

    @FXML
    private TableView<Chunk> tableView;

    @FXML
    private TableColumn<Chunk, String> chunkNumCol;

    @FXML
    private TableColumn<Chunk, String> chunkDescCol;

    @FXML
    private TableColumn<Chunk, String> chunkVoiceCol;

    @FXML
    private Button playBtn;

    @FXML
    private Button createBtn;

    public ChunkManagerController(String searchTerm, String text, List<Chunk> chunks) {
        _searchTerm = searchTerm;
        _text = text;
        _chunks = chunks;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chunkNumCol.setCellValueFactory(new PropertyValueFactory<>("chunkNumber"));
        chunkDescCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        chunkVoiceCol.setCellValueFactory(new PropertyValueFactory<>("voice"));

        for (Chunk chunk : _chunks) {
            tableView.getItems().add(chunk);
        }
    }

    @FXML
    private void handlePlay() {
        Chunk selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            playBtn.setDisable(true);
            new Thread(() -> {
                try {
                    Main.execCmd("echo \"(voice_" + selected.getVoice() + ")\" > .temp/voice.scm");
                    Main.execCmd("echo \"(SayText \\\"" + selected.getText() + "\\\")\" >> .temp/voice.scm");
                    Main.execCmd("festival -b .temp/voice.scm");

                    Platform.runLater(() -> {
                        playBtn.setDisable(false);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            dispSelectionError();
        }
    }

    @FXML
    private void handleDelete() {
        Chunk selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete Chunk " + selected.getChunkNumber() + "?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                tableView.getItems().removeAll(selected);
                _chunks.remove(selected);
                Main.execCmd("rm .temp/chunk" + selected.getChunkNumber() + ".wav");
            }
        } else {
            dispSelectionError();
        }
    }

    // maybe make a new scene for selecting number of images and display images?
    @FXML
    private void generateCreation() {
/*        String creationName = textField.getText();

        if (isConflicting("creations", creationName, "mp4")) {
            displayError("Creation with the same name already exists. Please enter another name.");
        } else if (!creationName.matches("^[a-zA-Z0-9\\_-]+")) {
            displayError("Invalid character(s) in creation name. Only letters, numbers, hyphens and underscores are allowed.");
        } else {

            textPrompt.setText("Generating creation...");
            createBtn.setDisable(true);
            new Thread(() -> {
                try {
                    Main.execCmd("mkdir .temp/" + creationName);

                    // create text and audio files
                    Main.execCmd("echo \"" + textArea.getText() + "\" > '.temp/" + creationName + "/text.txt'");
                    Main.execCmd("text2wave '.temp/" + creationName + "/text.txt' -o '.temp/" + creationName + "/audio.wav'");

                    // get length of audio
                    String cmd = "soxi -D '.temp/" + creationName + "/audio.wav'";
                    Process process = new ProcessBuilder("bash", "-c", cmd).start();
                    process.waitFor();
                    BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    double length = Double.parseDouble(stdout.readLine()) + 1;

                    // create video and then combine audio/video into one
                    Main.execCmd("ffmpeg -f lavfi -i color=c=blue:s=320x240:d=" + length + " -vf \"drawtext=fontfile='pwmas.ttf':fontsize=50:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _searchTerm + "'\" .temp/" + creationName + "/video.mp4");
                    Main.execCmd("ffmpeg -i \".temp/" + creationName + "/video.mp4\" -i \".temp/" + creationName + "/audio.wav\" -shortest creations/" + creationName + ".mp4");

                    Platform.runLater(() -> {
                        textField.setVisible(false);
                        createBtn.setVisible(false);
                        textPrompt.setText("Successfully created \"" + creationName + "\"");
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }*/
    }

    @FXML
    private void back() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SearchResult.fxml"));
            SearchResultController controller = new SearchResultController(_searchTerm, _text, _chunks);
            loader.setController(controller);

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
    private void returnToMenu() {
        Main.switchScene(getClass().getResource("Menu.fxml"));
    }

    private void dispSelectionError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText("No chunk selected");
        alert.showAndWait();
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
}
