package varpedia.controllers;

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
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import varpedia.main.Chunk;
import varpedia.main.Main;
import varpedia.main.Voice;
import varpedia.tasks.PlayAudioTask;
import varpedia.tasks.SaveAudioTask;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchResultController extends HelpScene implements Initializable {

    private String _searchTerm;
    private String _text;
    private List<Chunk> _chunks;
    private PlayAudioTask _previewTask;

    @FXML private TextArea textArea;
    @FXML private Button previewBtn;
    @FXML private Button saveBtn;
    @FXML private ComboBox<String> comboBox;
    @FXML private Button manageBtn;
    @FXML private Text termText;
    @FXML private Pane helpPane;

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
        termText.setText("Search Term: " + _searchTerm);

        // Creating an array list of all voices from enum
        // Code snippet from: https://stackoverflow.com/questions/29465943/get-enum-values-as-list-of-string-in-java-8
        ObservableList<String> voices = FXCollections.observableArrayList(
                Stream.of(Voice.values())
                        .map(Voice::getName)
                        .collect(Collectors.toList()));

        comboBox.setItems(voices);
        comboBox.setValue(Voice.kal_diphone.getName());

        int exitCode = Main.execCmd("[ ! -e .temp/chunks ]");
        if (exitCode == 0) { // if there is no chunk folder, then create one
            Main.execCmd("mkdir .temp/chunks");
        }
    }

    @FXML
    private void preview() {
        if (_previewTask != null && previewBtn.getText().equals("Stop Preview")) {
            _previewTask.destroyProcess();
            _previewTask.cancel();
            previewBtn.setText("Preview Chunk");
        } else if (validChunk()){
            String selectedText = "\\\"" + textArea.getSelectedText().replace("\"", "") + "\\\"";
            String voice = Voice.fromString(comboBox.getValue()).toString();

            previewBtn.setDisable(true);
            SaveAudioTask saveTask = new SaveAudioTask(selectedText, voice, 0, true);
            saveTask.setOnSucceeded(evt -> {
                int fileSize = (int) saveTask.getValue();

                if (fileSize == 0) { // check empty/invalid file due to invalid chunk selected
                    previewBtn.setText("Preview Chunk");
                    displayError("An error occurred when previewing the chunk. Please try another chunk of text or use the voice \"US Male\"");
                } else {
                    _previewTask = new PlayAudioTask(".temp/preview.wav");
                    _previewTask.setOnRunning(e -> {
                        previewBtn.setDisable(false);
                        previewBtn.setText("Stop Preview");
                    });
                    _previewTask.setOnSucceeded(e -> previewBtn.setText("Preview Chunk"));
                }

                Thread previewThread = new Thread(_previewTask);
                previewThread.start();
            });

            Thread saveThread = new Thread(saveTask);
            saveThread.start();
        }
    }

    @FXML
    private void saveChunk() {
        if (validChunk()) {
            saveBtn.setDisable(true);
            saveBtn.setText("Saving...");

            String voice = Voice.fromString(comboBox.getValue()).toString();
            String selectedText = textArea.getSelectedText().replace("\"", ""); // remove all double quotes to prevent some errors
            int id = 1;
            int numChunks = _chunks.size();
            if (numChunks != 0) {
                id = _chunks.get(numChunks-1).getChunkNumber()+1; // last chunk number + 1
            }

            SaveAudioTask task = new SaveAudioTask(selectedText, voice, id, false);
            int finalId = id;
            task.setOnSucceeded(e -> {
                saveBtn.setText("Save Chunk");
                saveBtn.setDisable(false);

                // size of the audio file created
                int size = (int) task.getValue();

                if (size > 0) {
                    Chunk chunk = new Chunk(finalId, selectedText, Voice.valueOf(voice));
                    _chunks.add(chunk);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully saved chunk");
                    alert.showAndWait();

                } else {
                    // remove invalid/empty audio file
                    Main.execCmd("rm .temp/chunks/chunk" + finalId +".wav");
                    displayError("An error occurred when saving the chunk. Please try another chunk of text or use the voice \"US Male\"");
                }
            });

            Thread thread = new Thread(task);
            thread.start();
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
        if (_chunks.size() == 0) {
            Main.switchScene(getClass().getResource("/varpedia/fxml/Menu.fxml"));
        } else {
            if (Main.returnToMenuWarning()) {
                Main.switchScene(getClass().getResource("/varpedia/fxml/Menu.fxml"));
            }
        }
    }

    private boolean validChunk() {
        String[] words = textArea.getSelectedText().split("\\s+");

        if (textArea.getSelectedText().trim().isEmpty()) {
            displayError("No text selected. Please highlight a part of the text.");
            return false;
        } else if (words.length > 30) {
            displayError("Selected text exceeds the maximum number of words (30). Please select a smaller chunk.");
            return false;
        }

        return true;
    }

    private void displayError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
