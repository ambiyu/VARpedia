package softeng206a3;

import javafx.fxml.FXML;

public class MenuController {

    @FXML
    private void handleMyCreations() {
        Main.switchScene(getClass().getResource("CreationsList.fxml"));
    }

    @FXML
    private void handleCreate() {
        // remove previous chunks/audio files if there are any
        Main.execCmd("rm -r .temp/*");

        Main.switchScene(getClass().getResource("Search.fxml"));
    }
}
