package softeng206a3;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CreationsListController implements Initializable {
    private List<String> _creations;

    @FXML
    private TableView<Creation> tableView;

    @FXML
    private TableColumn<Creation, String> creationIdCol;

    @FXML
    private TableColumn<Creation, String> creationNameCol;

    @FXML
    private Button playBtn;

    @FXML
    private Button deleteBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        creationIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        creationNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        try {
            String cmd = "ls -1 creations";
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

            _creations = new ArrayList<>();
            String line;
            while ((line = stdout.readLine()) != null) {
                _creations.add(line);
            }

            // remove .mp4 at the end of filename and remove non .mp4
            for (int i = _creations.size()-1; i >= 0; i--) {
                String creation = _creations.get(i);
                if (creation.endsWith(".mp4")) {
                    _creations.set(i, creation.substring(0, creation.length()-4));
                } else {
                    _creations.remove(i);
                }
            }

            populateTable();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks mouse clicks and see whether a creation is selected or not
     */
    @FXML
    private void handleClick() {
        Creation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            playBtn.setDisable(false);
            deleteBtn.setDisable(false);
        } else {
            playBtn.setDisable(true);
            deleteBtn.setDisable(true);
        }
    }

    @FXML
    private void handlePlay() {
        Creation selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MediaPlayer.fxml"));
                MediaPlayerController controller = new MediaPlayerController("creations/" + selected.getName() + ".mp4");
                loader.setController(controller);

                Parent parent = loader.load();
                Scene createScene = new Scene(parent);
                Stage window = Main.getPrimaryStage();
                window.setScene(createScene);
                window.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            displaySelectionError();
        }

    }

    @FXML
    private void handleDelete() {
        Creation selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete \"" + selected.getName() + "\"?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                tableView.getItems().removeAll(selected);
                _creations.remove(selected.getName());

                // repopulate table to reset ids
                tableView.getItems().clear();
                populateTable();

                Main.execCmd("rm creations/" + selected.getName() + ".mp4");

                playBtn.setDisable(true);
                deleteBtn.setDisable(true);
            }
        } else {
            displaySelectionError();
        }

    }

    @FXML
    private void returnToMenu() {
        Main.switchScene(getClass().getResource("Menu.fxml"));
    }

    private void populateTable() {
        for (int i = 0; i < _creations.size(); i++) {
            Creation creation = new Creation(i+1, _creations.get(i));
            tableView.getItems().add(creation);
        }
    }

    private void displaySelectionError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText("No chunk selected");
        alert.showAndWait();
    }
}
