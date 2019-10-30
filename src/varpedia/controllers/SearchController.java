package varpedia.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import varpedia.main.Main;
import varpedia.tasks.WikiSearchTask;

public class SearchController {

    @FXML private TextField textField;
    @FXML private Button searchBtn;
    @FXML private Text textPrompt;
    
    @FXML
    public void back() {
    	Main.switchScene(getClass().getResource("/varpedia/fxml/Menu.fxml"));
    }
    
    @FXML
    private void searchWiki() {
        if (!textField.getText().trim().isEmpty()) {
            searchBtn.setDisable(true);
            String searchTerm = textField.getText();
            textPrompt.setText("Searching...");

            WikiSearchTask task = new WikiSearchTask(searchTerm);

            task.setOnSucceeded(e -> {
                String searchResult = (String) task.getValue();

                try {
                    searchBtn.setDisable(false);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/varpedia/fxml/SearchResult.fxml"));
                    SearchResultController controller = new SearchResultController(searchTerm, searchResult);
                    loader.setController(controller);

                    Parent parent = loader.load();
                    Scene createScene = new Scene(parent);
                    Stage window = Main.getPrimaryStage();
                    window.setScene(createScene);
                    window.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // Search term not found on wikit
            task.setOnFailed(e -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText(null);
                alert.setContentText("\"" + searchTerm + "\" not found :(");
                alert.showAndWait();

                searchBtn.setDisable(false);
                textPrompt.setText("Enter your search term");
            });

            Thread thread = new Thread(task);
            thread.start();

        }
    }

	
}
