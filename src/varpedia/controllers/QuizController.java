package varpedia.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import varpedia.main.Creation;
import varpedia.main.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class QuizController extends HelpScene implements Initializable {

    private MediaPlayer player;
    private List<Creation> _creations;
    private List<Button> _options;
    private int _numQuestions;
    private String _currentAnswer;
    private int _currentQuestion;
    private int _correctCount;

    @FXML private Pane welcomePane;
    @FXML private Pane quizPane;
    @FXML private Pane resultPane;
    @FXML private Spinner<Integer> spinner;
    @FXML private MediaView mediaView;
    @FXML private Button option1;
    @FXML private Button option2;
    @FXML private Button option3;
    @FXML private Button option4;
    @FXML private Text questionText;
    @FXML private Text result;
    @FXML private Text correctAnswerText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 5, 1));

        _options = new ArrayList<>();
        _options.add(option1);
        _options.add(option2);
        _options.add(option3);
        _options.add(option4);

        quizPane.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == KeyCode.DIGIT1) {
                option1.fire();
                ke.consume();
            } else if (ke.getCode() == KeyCode.DIGIT2) {
                option2.fire();
                ke.consume();
            } else if (ke.getCode() == KeyCode.DIGIT3) {
                option3.fire();
                ke.consume();
            } else if (ke.getCode() == KeyCode.DIGIT4) {
                option4.fire();
                ke.consume();
            }
        });

        try {
            // get all files from the creations folder
            String cmd = "ls -1 creations";
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> output = new ArrayList<>();
            String line;
            while ((line = stdout.readLine()) != null) {
                output.add(line);
            }

            // remove .mp4 at the end of filename and remove files that are not .mp4
            _creations = new ArrayList<>();
            for (int i = output.size()-1; i >= 0; i--) {
                String file = output.get(i);
                if (file.endsWith(".mp4")) {
                    _creations.add(new Creation(_creations.size(), file.substring(0, file.length()-4)));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStart() {
        _numQuestions = spinner.getValue();
        _currentQuestion = 1;
        welcomePane.setVisible(false);
        quizPane.setVisible(true);

        // initialise the quiz a random creation
        nextCreation();
    }

    @FXML
    private void replayVideo() {
        player.seek(Duration.seconds(0));
    }

    @FXML
    private void returnToMenu() {
        if (player != null) {
            player.dispose();
        }

        Main.switchScene(getClass().getResource("/varpedia/fxml/Menu.fxml"));
    }

    @FXML
    private void handleContinue() {
        if (_currentQuestion >= _numQuestions) {
            // quiz is finished
            try {
                FXMLLoader newLoader = new FXMLLoader(getClass().getResource("/varpedia/fxml/QuizResult.fxml"));
                QuizResultController controller = new QuizResultController(_correctCount, _numQuestions);
                newLoader.setController(controller);

                Parent parent = newLoader.load();
                Scene createNewScene = new Scene(parent);
                Stage newWindow = Main.getPrimaryStage();
                newWindow.setScene(createNewScene);
                newWindow.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // continue to next question
            questionText.setText("Question " + ++_currentQuestion);
            resultPane.setVisible(false);
            quizPane.setVisible(true);
            nextCreation();
        }
    }

    private void setButtons(Creation correct) {
        // select a random creation to be the correct one
        int correctOption = (int)(Math.random() * 4);
        Button correctButton = _options.get(correctOption);
        String creationName = correct.getName();
        _currentAnswer = getSearchTerm(creationName);

        correctButton.setText(correctOption+1 + ". " + _currentAnswer);
        correctButton.setOnAction(e -> {
            onSelectOption(true);
            _correctCount++;
        });

        // create new temporary lists of all creations and buttons without the correct answer
        // used to select random creations
        List<Creation> remaining = new ArrayList<>(_creations);
        remaining.remove(correct);
        List<Button> buttons = new ArrayList<>(_options);
        buttons.remove(correctButton);

        int buttonNumber = 1;

        // get three other random creations
        for (int i = 0; i < 3; i++) {
            int rand = (int)(Math.random() * remaining.size());
            Button falseButton = buttons.get(0);
            falseButton.setOnAction(e -> onSelectOption(false));

            Creation creation = remaining.get(rand);
            String searchTerm = getSearchTerm(creation.getName());


            if (buttonNumber == correctOption+1) {
                buttonNumber++;
            }

            // set the text of the button to the term
            falseButton.setText(buttonNumber++ + ". " + searchTerm);

            remaining.remove(creation);
            buttons.remove(falseButton);
        }
    }

    private void onSelectOption(boolean correct) {
        player.dispose();
        quizPane.setVisible(false);
        resultPane.setVisible(true);

        if (!correct) {
            result.setText("Incorrect");
            correctAnswerText.setText("Correct answer: " + _currentAnswer);
            correctAnswerText.setVisible(true);
        } else {
            result.setText("Correct!");
            correctAnswerText.setVisible(false);
        }
    }

    private void nextCreation() {
        int nextId = (int)(Math.random() * _creations.size());
        String creationName = _creations.get(nextId).getName();
        File fileUrl = new File(".quiz/" + creationName + "/" + creationName + ".mp4");

        Media video = new Media(fileUrl.toURI().toString());
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
