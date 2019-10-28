package varpedia.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import varpedia.main.Creation;
import varpedia.main.Main;
import varpedia.tasks.PlayAudioTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CreationsListController implements Initializable {
    private List<Creation> _creations;
    private PlayAudioTask _playAudioTask;

    @FXML private TableView<Creation> tableView;
    @FXML private TableColumn<Creation, String> creationIdCol;
    @FXML private TableColumn<Creation, String> creationNameCol;
    @FXML private TableColumn<Creation, String> searchTermCol;
    @FXML private Button playAudioBtn;
    @FXML private AnchorPane pane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        creationIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        creationNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        searchTermCol.setCellValueFactory(new PropertyValueFactory<>("searchTerm"));

        try {
            // get all files from the creations folder
            String cmd = "ls -1 creations";
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> files = new ArrayList<>();
            String line;
            while ((line = stdout.readLine()) != null) {
                files.add(line);
            }

            _creations = new ArrayList<>();
            // filter out all files that are not .mp4
            for (String file : files) {
                if (file.endsWith(".mp4")) {
                    try {
                        file = file.substring(0, file.length()-4);

                        cmd = "cat .quiz/" + file + "/searchTerm.txt";
                        process = new ProcessBuilder("bash", "-c", cmd).start();
                        int exitCode = process.waitFor();
                        String searchTerm = "";
                        if (exitCode == 0) {
                            stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            searchTerm = stdout.readLine();
                        }

                        _creations.add(new Creation(_creations.size()+1, file, searchTerm));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            populateTable();

            // Keyboard shortcuts for play and delete
            // Code snippet from: https://stackoverflow.com/questions/25397742/javafx-keyboard-event-shortcut-key
            pane.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
                if (ke.getCode() == KeyCode.DELETE) {
                    handleDelete();
                    ke.consume();
                } else if (ke.getCode() == KeyCode.ENTER) {
                    handlePlay();
                    ke.consume();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePlay() {
        Creation selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/varpedia/fxml/MediaPlayer.fxml"));
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
    private void handlePlayAudio() {
        Creation selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            if (_playAudioTask != null && playAudioBtn.getText().equals("Stop")) {
                _playAudioTask.destroyProcess();
                _playAudioTask.cancel();
                playAudioBtn.setText("Play Audio");
            } else {

                String creationName = selected.getName();

                // check if audio file already exists, if so then delete it
                int exitCode = Main.execCmd("test -f .temp/preview.wav");
                if (exitCode == 0) {
                    Main.execCmd("rm .temp/preview.wav");
                }

                // create an audio file with that contains the audio of the creation
                Main.execCmd("ffmpeg -i creations/" + creationName + ".mp4 -f wav -ab 192000 -vn .temp/preview.wav");

                _playAudioTask = new PlayAudioTask(".temp/preview.wav");
                _playAudioTask.setOnSucceeded(e -> playAudioBtn.setText("Play Audio"));
                _playAudioTask.setOnRunning(e -> playAudioBtn.setText("Stop"));

                Thread thread = new Thread(_playAudioTask);
                thread.start();
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
                _creations.remove(selected);

                // repopulate table to reset ids
                tableView.getItems().clear();
                populateTable();

                Main.execCmd("rm creations/" + selected.getName() + ".mp4");
                Main.execCmd("rm -r .quiz/" + selected.getName());
            }
        } else {
            displaySelectionError();
        }

    }

    @FXML
    private void returnToMenu() {
        Main.switchScene(getClass().getResource("/varpedia/fxml/Menu.fxml"));
    }

    private void populateTable() {
        for (Creation creation : _creations) {
            tableView.getItems().add(creation);
        }
    }

    private void displaySelectionError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText("No creation selected. Please select a creation from the list");
        alert.showAndWait();
    }
}
