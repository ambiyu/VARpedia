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

        double percentage = (double)_numCorrect / _numQuestions;

        if (percentage == 1) {
            message.setText("Perfect score! Great work");
        } else if (percentage >= 0.7) {
            message.setText("Well done, keep up the good work!");
        } else if (percentage >= 0.3) {
            message.setText("Good effort. Keep practicing!");
        } else {
            message.setText("Don't give up, keep practicing!");
        }
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
