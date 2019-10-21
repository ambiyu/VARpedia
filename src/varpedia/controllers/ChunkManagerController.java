package varpedia.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import varpedia.main.Chunk;
import varpedia.main.Main;

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
    private TableColumn<Chunk, String> chunkTextCol;

    @FXML
    private TableColumn<Chunk, String> chunkVoiceCol;

    @FXML
    private Button playBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button createBtn;

    @FXML
    private Button upBtn;

    @FXML
    private Button downBtn;

    @FXML
    private Text warningText;

    public ChunkManagerController(String searchTerm, String text, List<Chunk> chunks) {
        _searchTerm = searchTerm;
        _text = text;
        _chunks = chunks;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chunkNumCol.setCellValueFactory(new PropertyValueFactory<>("chunkNumber"));
        chunkTextCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        chunkVoiceCol.setCellValueFactory(new PropertyValueFactory<>("voice"));

        //check if user has any chunks to display at chunkManager
        if (_chunks.isEmpty()) {
            createBtn.setDisable(true);
            warningText.setVisible(true);
        }

        for (Chunk chunk : _chunks) {
            tableView.getItems().add(chunk);
        }
    }

    /**
     * Checks mouse clicks to see whether a chunk is selected or not
     */
    @FXML
    private void handleClick() {
        Chunk selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            playBtn.setDisable(false);
            deleteBtn.setDisable(false);
            upBtn.setDisable(false);
            downBtn.setDisable(false);
        } else {
            playBtn.setDisable(true);
            deleteBtn.setDisable(true);
            upBtn.setDisable(true);
            downBtn.setDisable(true);
        }
    }

    @FXML
    private void handleUp() {
        Chunk selected = tableView.getSelectionModel().getSelectedItem();
        int index = _chunks.indexOf(selected);

        if (index != 0) {
            Chunk above = _chunks.get(index-1);
            swapChunks(selected, above, index, -1);
        }
    }

    @FXML
    private void handleDown() {
        Chunk selected = tableView.getSelectionModel().getSelectedItem();
        int index = _chunks.indexOf(selected);

        if (index != _chunks.size()-1) {
            Chunk below = _chunks.get(index+1);
            swapChunks(selected, below, index, 1);
        }
    }

    private void swapChunks(Chunk selected, Chunk other, int selectedIndex, int d) {
        _chunks.set(selectedIndex, other);
        _chunks.set(selectedIndex+d, selected);

        tableView.getItems().set(selectedIndex, other);
        tableView.getItems().set(selectedIndex+d, selected);
        tableView.getSelectionModel().select(selected);
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
            displayError("No chunk selected");
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
                Main.execCmd("rm .temp/chunks/chunk" + selected.getChunkNumber() + ".wav");
                if (_chunks.isEmpty()) {
                    createBtn.setDisable(true);
                }

                playBtn.setDisable(true);
                deleteBtn.setDisable(true);
            }
        } else {
            displayError("No chunk selected");
        }
    }

    @FXML
    private void toImageSelect() {
        try {
            FXMLLoader newLoader = new FXMLLoader(getClass().getResource("/varpedia/fxml/ImageChoice.fxml"));
            ImageChoiceController imageScene = new ImageChoiceController(_chunks, _searchTerm, this);
            newLoader.setController(imageScene);

            Parent parent = newLoader.load();
            Scene createNewScene = new Scene(parent);
            Stage newWindow = Main.getPrimaryStage();
            newWindow.setScene(createNewScene);
            newWindow.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void back() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/varpedia/fxml/SearchResult.fxml"));
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

    private void displayError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
