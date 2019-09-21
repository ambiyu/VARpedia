package softeng206a3;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CreateController {

    private String _searchTerm;

    @FXML
    private Text textPrompt;

    @FXML
    private TextArea textArea;

    @FXML
    private TextField textField;

    @FXML
    private Button searchBtn;

    @FXML
    private Button previewBtn;

    @FXML
    private Button saveToAudioBtn;

    @FXML
    private Button createBtn;

    @FXML
    private void searchWiki() {
        if (!textField.getText().trim().isEmpty()) {
            searchBtn.setDisable(true);
            _searchTerm = textField.getText();
            textArea.setText("Searching...");
            new Thread(() -> {
                try {
                    String cmd = "wikit \"" + _searchTerm + "\" | grep -o '[^ ][^.]*\\.'";
                    Process process = new ProcessBuilder("bash", "-c", cmd).start();
                    process.waitFor();
                    BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    List<String> sentences = new ArrayList<>();
                    String line = stdout.readLine();
                    if (line != null) {
                        do {
                            sentences.add(line);
                        } while ((line = stdout.readLine()) != null);

                        Platform.runLater(() -> {
                            // display search result
                            textArea.clear();
                            for (int i = 1; i <= sentences.size(); i++) {
                                textArea.appendText(i + ". " + sentences.get(i - 1) + "\n");
                            }

                            searchBtn.setVisible(false);
                            previewBtn.setVisible(true);
                            saveToAudioBtn.setVisible(true);
                            textField.setPromptText("File name");

                            textPrompt.setVisible(false);
                            //textPrompt.setText("Select/highlight parts of the text to preview/save as audio, or generate creation");
                        });
                    } else { // search term not found on wikit
                        Platform.runLater(() -> {
                            textArea.setText("\"" + _searchTerm + "\" not found :(");
                            searchBtn.setDisable(false);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
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
    private void saveToAudio() {
        if (textArea.getSelectedText().trim().isEmpty()) {
            displayError("No text selected. Please highlight a part of the text.");
        } else if (textField.getText().isEmpty()) {
            displayError("Please enter a name for the audio file");
        } else if (!textField.getText().matches("^[a-zA-Z0-9\\_-]+")) {
            displayError("Invalid character(s) in audio file name. Only letters, numbers, hyphens and underscores are allowed.");
        } else if (isConflicting("audio", textField.getText(), "wav")) {
            displayError("Audio file with the same name already exists. Please enter another name.");
        } else {
            try {
                //Main.execCmd( "echo \"" + textArea.getSelectedText() + "\" | text2wave -o audio/" + textField.getText() + ".wav");
                String cmd = "echo \"" + textArea.getSelectedText() + "\" | text2wave -o audio/" + textField.getText() + ".wav";
                Process process = new ProcessBuilder("bash", "-c", cmd).start();
                int exitCode = process.waitFor(); // probably not needed

                if (exitCode == 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully created audio file: audio/" + textField.getText() + ".wav");
                    alert.showAndWait();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: Should get all text from text area.
    @FXML
    private void generateCreation() {
        String creationName = textField.getText();

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
