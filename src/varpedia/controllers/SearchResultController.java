package varpedia.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import varpedia.main.Chunk;
import varpedia.main.Main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    @FXML
    private Button saveBtn;

    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private Button manageBtn;

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

        ObservableList<String> voices = FXCollections.observableArrayList(
                "kal_diphone",
                "akl_nz_jdt_diphone",
                "akl_nz_cw_cg_cg");
        comboBox.setItems(voices);
        comboBox.setValue("kal_diphone");

        int exitCode = Main.execCmd("[ ! -e .temp/chunks ]");
        if (exitCode == 0) { // if there is no chunk folder, then create one
            Main.execCmd("mkdir .temp/chunks");
        }
    }

    @FXML
    private void preview() {
        String[] words = textArea.getSelectedText().split("\\s+");

        if (textArea.getSelectedText().trim().isEmpty()) {
            displayError("No text selected. Please highlight a part of the text.");
        } else if (words.length > 30) {
            displayError("Selected text exceeds the maximum number of words (30). Please select a smaller chunk.");
        } else {
            previewBtn.setDisable(true);
            new Thread(() -> {
                try {
                    String selectedText = "\\\"" + textArea.getSelectedText().replace("\"", "") + "\\\"";

                    // create .scm file in order to use festival to play different voices
                    Main.execCmd("echo \"(voice_" + comboBox.getValue() + ")\" > .temp/voice.scm");
                    Main.execCmd("echo \"(SayText " + selectedText + ")\" >> .temp/voice.scm");
                    int exitCode = Main.execCmd("festival -b .temp/voice.scm");

                    if (exitCode != 0) {
                        Platform.runLater(() -> {
                            previewBtn.setDisable(false);
                            displayError("An error occurred when previewing the chunk. Please try another chunk of text or use the voice \"kal_diphone\"");
                        });
                    } else {
                        Platform.runLater(() -> {
                            previewBtn.setDisable(false);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML
    private void saveChunk() {
        String[] words = textArea.getSelectedText().split("\\s+");

        if (textArea.getSelectedText().trim().isEmpty()) {
            displayError("No text selected. Please highlight a part of the text.");
        } else if (words.length > 30) {
            displayError("Selected text exceeds the maximum number of words (30). Please select a smaller chunk.");
        } else {
            saveBtn.setDisable(true);
            new Thread(() -> {
                try {
                    String voice = comboBox.getValue();
                    String selectedText = textArea.getSelectedText().replace("\"", ""); // remove all double quotes to prevent some errors
                    int id = 1;
                    int numChunks = _chunks.size();
                    if (numChunks != 0) {
                        id = _chunks.get(numChunks-1).getChunkNumber()+1; // last chunk number + 1
                    }

                    // create .wav audio file with selected voice
                    Main.execCmd("echo \"" + selectedText + "\" | text2wave -eval '(voice_" + voice + ")' -o \".temp/chunks/chunk" + id + ".wav\"");

                    // check file size in case of any errors
                    String cmd = "wc -c < .temp/chunks/chunk" + id + ".wav";
                    Process process = new ProcessBuilder("bash", "-c", cmd).start();
                    process.waitFor();
                    BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    int size = Integer.parseInt(stdout.readLine());
                    if (size > 0) {
                        Chunk chunk = new Chunk(id, selectedText, voice);
                        _chunks.add(chunk);

                        Platform.runLater(() -> {
                            manageBtn.setDisable(false);
                            saveBtn.setDisable(false);
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText("Successfully saved chunk");
                            alert.showAndWait();
                        });

                    } else {
                        // remove invalid audio file
                        Main.execCmd("rm .temp/chunks/chunk" + id +".wav");
                        Platform.runLater(() -> {
                            saveBtn.setDisable(false);
                            displayError("An error occurred when previewing the chunk. Please try another chunk of text or use the voice \"kal_diphone\"");
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        }
    }

    @FXML
    private void toChunkManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/varpedia/fxml/ChunkManager.fxml"));
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
        if (Main.returnToMenuWarning()) {
            Main.switchScene(getClass().getResource("/varpedia/fxml/Menu.fxml"));
        }
    }

    private void displayError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
