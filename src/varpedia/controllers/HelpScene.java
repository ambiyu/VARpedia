package varpedia.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public abstract class HelpScene {
    @FXML private Pane helpPane;

    @FXML
    private void helpEntered() {
        helpPane.setVisible(true);
    }

    @FXML
    private void helpExited() {
        helpPane.setVisible(false);
    }
}
