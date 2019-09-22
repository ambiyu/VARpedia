package softeng206a3;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SearchResultController implements Initializable {

    private String _searchTerm;
    private String _text;
    private List<Chunk> _chunks;

    @FXML
    private TextArea textArea;

    @FXML
    private Button previewBtn;

    public SearchResultController(String searchTerm, String text) {
        _searchTerm = searchTerm;
        _text = text;
        _chunks = new ArrayList<>();
    }

    public SearchResultController(String searchTerm, String text, List<Chunk> chunks) {
        _searchTerm = searchTerm;
        _text = text;
        _chunks = chunks;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // display search result
        textArea.setText(_text);
    }

    @FXML
    private void preview() {
        if (textArea.getSelectedText().trim().isEmpty()) {
            displayError("No text selected. Please highlight a part of the text.");
        } else {
            new Thread(() -> {
                try {
                    previewBtn.setDisable(true);
                    Main.execCmd("echo \"" + textArea.getSelectedText() + "\" | festival --tts");

                    Platform.runLater(() -> {
                        previewBtn.setDisable(false);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML
    private void saveChunk() {
        if (textArea.getSelectedText().trim().isEmpty()) {
            displayError("No text selected. Please highlight a part of the text.");
        } else {
            new Thread(() -> {
                try {
                    String selectedText = textArea.getSelectedText();
                    // TODO: Temp name... How should we name the chunks audio file?
                    int id = (int)( Math.random()*1000);

                    //Main.execCmd("echo \"" + textArea.getSelectedText() + "\" | text2wave -o .temp/" + (int)(Math.random()*100) + ".wav");
                    String cmd = "echo \"" + selectedText + "\" | text2wave -o .temp/chunk" + id + ".wav";
                    Process process = new ProcessBuilder("bash", "-c", cmd).start();
                    int exitCode = process.waitFor(); // probably not needed
                    if (exitCode == 0) {
                        Chunk chunk = new Chunk(id, selectedText);
                        _chunks.add(chunk);

                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText("Successfully saved chunk");
                            alert.showAndWait();
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        }
    }

    @FXML
    private void chunkManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChunkManager.fxml"));
            ChunkManagerController controller = new ChunkManagerController(_searchTerm, textArea.getText(), _chunks);
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

    private void displayError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
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
