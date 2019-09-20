package softeng206a3;

import javafx.fxml.FXML;

public class MenuController {

    @FXML
    private void handleMyCreations() {
        Main.switchScene(getClass().getResource("CreationsList.fxml"));
    }

    @FXML
    private void handleCreate() {
        Main.switchScene(getClass().getResource("Create.fxml"));
    }
}
