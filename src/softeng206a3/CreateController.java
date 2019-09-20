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
    private List<String> _sentences;
    private int _numSentences;

    @FXML
    private Text textPrompt;

    @FXML
    private TextArea textArea;

    @FXML
    private TextField textField;

    @FXML
    private Button searchBtn;

    @FXML
    private Button sentencesBtn;

    @FXML
    private Button createBtn;

    @FXML
    private void searchWiki() {
        if (!textField.getText().trim().isEmpty()) {
            searchBtn.setDisable(true); // need to enable back later if error
            _searchTerm = textField.getText();
            textArea.setText("Searching...");
            new Thread(() -> {
                try {
                    String cmd = "wikit \"" + _searchTerm + "\" | grep -o '[^ ][^.]*\\.'";
                    Process process = new ProcessBuilder("bash", "-c", cmd).start();
                    process.waitFor();
                    BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    _sentences = new ArrayList<>();
                    String line = stdout.readLine();
                    if (line != null) {
                        do {
                            _sentences.add(line);
                        } while ((line = stdout.readLine()) != null);

                        Platform.runLater(() -> {
                            // display search result
                            textArea.clear();
                            for (int i = 1; i <= _sentences.size(); i++) {
                                textArea.appendText(i + ". " + _sentences.get(i - 1) + "\n");
                            }

                            textField.clear();
                            textField.setPromptText("Sentences");
                            textPrompt.setText("Enter number of sentences (1-" + _sentences.size() + ")");
                            searchBtn.setVisible(false);
                            sentencesBtn.setVisible(true);
                        });

                    } else {
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
    private void handleSelectSentences() {
        try {
            _numSentences = Integer.parseInt(textField.getText().trim());
            if (_numSentences > 0 && _numSentences <= _sentences.size()) {

                // reprint text with the number of sentences inputted
                textArea.clear();
                for (int i = 1; i <= _numSentences; i++) {
                    textArea.appendText(i + ". " + _sentences.get(i - 1) + "\n");
                }

                textField.clear();
                textField.setPromptText("Creation name");
                textPrompt.setText("Enter a name for this creation");
                sentencesBtn.setVisible(false);
                createBtn.setVisible(true);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText(null);
                alert.setContentText("Invalid input. Please try again.");
                alert.showAndWait();
            }
        } catch (NumberFormatException nfex) {
            nfex.printStackTrace();
        }

        // error handling

    }

    @FXML
    private void generateCreation() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        String creationName = textField.getText();

        // check for conflicting name
        try {
            String cmd = "test -f creations/" + creationName + ".mp4";
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            int exitStatus = process.waitFor();

            if (exitStatus == 0) {
                alert.setContentText("Creation with the same name already exists. Please enter another name.");
                alert.showAndWait();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!creationName.matches("^[a-zA-Z0-9\\_-]+")) {
            alert.setContentText("Invalid character(s) in creation name. Only letters, numbers, hyphens and underscores are allowed. Please try again.");
            alert.showAndWait();
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < _numSentences; i++) {
                sb.append(_sentences.get(i));
                sb.append("\n");
            }
            String fullText = sb.toString();

            textPrompt.setText("Generating creation...");
            createBtn.setDisable(true);
            new Thread(() -> {
                try {
                    //execCmd("echo \"" + fullText + "\" > text.txt");
                    // create audio
                    Main.execCmd("mkdir .temp/" + creationName);
                    Main.execCmd("echo \"" + fullText + "\" | text2wave -o '.temp/" + creationName + "/audio.wav'");
                    String cmd = "soxi -D '.temp/" + creationName + "/audio.wav'";

                    // get length of audio
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
}