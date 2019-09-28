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
        createBtn.setDisable(true);
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

        String creationName = _searchTerm;

        if (!creationName.matches("^[a-zA-Z0-9\\_-]+")) {
            displayError("Invalid character(s) in creation name. Only letters, numbers, hyphens and underscores are allowed.");
        } else {

        	try {
                FXMLLoader newLoader = new FXMLLoader(getClass().getResource("imageSelect.fxml"));
               
                System.out.println(tableView.getSelectionModel().getSelectedItem().getText());
                ImageSelectController imageScene = new ImageSelectController(_searchTerm, tableView.getSelectionModel().getSelectedItem().getText(), this);
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
    private void displayError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
	public void allowCreation() {
		if(tableView.getSelectionModel().equals(null)){
			createBtn.setDisable(true);
			
		}
		else {
			createBtn.setDisable(false);
		}
	}
}
