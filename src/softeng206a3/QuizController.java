package softeng206a3;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class QuizController implements Initializable {

    private MediaPlayer player;
    private List<Creation> _creations;
    private List<Button> _options;

    @FXML
    private Pane welcomePane;

    @FXML
    private Pane quizPane;

    @FXML
    private Button option1;

    @FXML
    private Button option2;

    @FXML
    private Button option3;

    @FXML
    private Button option4;

    @FXML
    private Text status;

    @FXML
    private MediaView mediaView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        _options = new ArrayList<>();
        _options.add(option1);
        _options.add(option2);
        _options.add(option3);
        _options.add(option4);

        try {
            String cmd = "ls -1 creations";
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> output = new ArrayList<>();
            String line;
            while ((line = stdout.readLine()) != null) {
                output.add(line);
            }

            _creations = new ArrayList<>();
            // remove .mp4 at the end of filename and remove non .mp4
            for (int i = output.size()-1; i >= 0; i--) {
                String file = output.get(i);
                if (file.endsWith(".mp4")) {
                    _creations.add(new Creation(_creations.size(), file.substring(0, file.length()-4)));
                }
            }

            String firstCreation = _creations.get(0).getName();
            File fileUrl = new File(".quiz/" + firstCreation + "/" + firstCreation + ".mp4");

            Media video = new Media(fileUrl.toURI().toString());
            player = new MediaPlayer(video);
            mediaView.setMediaPlayer(player);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStart() {
        welcomePane.setVisible(false);
        quizPane.setVisible(true);
        player.play();
        setButtons(_creations.get(0));
    }

    @FXML
    private void replayVideo() {
        player.seek(Duration.seconds(0));
    }

    @FXML
    private void returnToMenu() {
        player.dispose();
        Main.switchScene(getClass().getResource("Menu.fxml"));
    }

    private void setButtons(Creation correct) {
        int correctOption = (int)(Math.random() * 4);
        Button correctButton = _options.get(correctOption);
        String creationName = correct.getName();

        correctButton.setText(getSearchTerm(creationName));
        correctButton.setOnAction(event -> {
            status.setText("Correct!");
            nextCreation();
        });

        // temp fix
        List<Creation> remaining = new ArrayList<>(_creations);
        remaining.remove(correct);
        List<Button> buttons = new ArrayList<>(_options);
        buttons.remove(correctButton);

        // get three other random creations
        for (int i = 0; i < 3; i++) {
            int rand = (int)(Math.random() * remaining.size());
            Button falseButton = buttons.get(0);
            falseButton.setOnAction(event -> {
                status.setText("Incorrect!");
                nextCreation();
            });

            Creation creation = remaining.get(rand);

            falseButton.setText(getSearchTerm(creation.getName()));
            remaining.remove(creation);
            buttons.remove(falseButton);
        }
    }

    private void nextCreation() {
        int nextId = (int)(Math.random() * _creations.size());
        String creationName = _creations.get(nextId).getName();
        File fileUrl = new File(".quiz/" + creationName + "/" + creationName + ".mp4");

        Media video = new Media(fileUrl.toURI().toString());
        player.dispose();
        player = new MediaPlayer(video);
        mediaView.setMediaPlayer(player);
        player.setAutoPlay(true);

        setButtons(_creations.get(nextId));
    }

    private String getSearchTerm(String creationName) {
        try {
            String cmd = "cat .quiz/" + creationName + "/searchTerm.txt";
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return stdout.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
