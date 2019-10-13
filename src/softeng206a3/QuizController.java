package softeng206a3;

import javafx.fxml.FXML;

public class QuizController {

    @FXML
    private void returnToMenu() {
        Main.switchScene(getClass().getResource("Menu.fxml"));
    }
}
