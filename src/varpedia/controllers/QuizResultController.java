package varpedia.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import varpedia.main.Main;

import java.net.URL;
import java.util.ResourceBundle;

public class QuizResultController implements Initializable {

    private int _numCorrect;
    private int _numQuestions;

    @FXML private Text message;
    @FXML private Text score;

    public QuizResultController(int numCorrect, int numQuestions) {
        _numCorrect = numCorrect;
        _numQuestions = numQuestions;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        score.setText("Score: " + _numCorrect + "/" + _numQuestions);
        // TODO: add messages depending on score
    }

    @FXML
    private void returnToMenu() {
        Main.switchScene(getClass().getResource("/varpedia/fxml/Menu.fxml"));
    }

    @FXML
    private void tryAgain() {
        Main.switchScene(getClass().getResource("/varpedia/fxml/Quiz.fxml"));
    }
}
