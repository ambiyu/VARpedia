package softeng206a3;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

public class SearchController implements Initializable{

    @FXML
    private TextField textField;

    @FXML
    private Button searchBtn;

    @FXML
    private Text text;

    
    @FXML
    public void back() {
    	Main.switchScene(getClass().getResource("Menu.fxml"));
    }
    
    @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		searchBtn.setDisable(true);	
	}
    
    @FXML 
    public void allowSearch() {
    	if(textField.getText().trim().isEmpty()) {
    		searchBtn.setDisable(true);
    	}
    	else {
    		searchBtn.setDisable(false);
    	}
    }
    
    @FXML
    private void searchWiki() {
        if (!textField.getText().trim().isEmpty()) {
            searchBtn.setDisable(true);
            String searchTerm = textField.getText();
            text.setText("Searching...");
            new Thread(() -> {
                try {
                    //String cmd = "wikit \"" + searchTerm + "\" | grep -o '[^ ][^.]*\\.'"; // each sentence on new line
                    String cmd = "wikit \"" + searchTerm + "\" | grep -o '[^ ].*'";
                    Process process = new ProcessBuilder("bash", "-c", cmd).start();
                    process.waitFor();
                    BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = stdout.readLine();
                    if (line.endsWith(" not found :^(")) { // search term not found on wikit
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("ERROR");
                            alert.setHeaderText(null);
                            alert.setContentText("\"" + searchTerm + "\" not found :(");
                            alert.showAndWait();

                            searchBtn.setDisable(false);
                            text.setText("Enter search term:");
                        });
                    } else {

                        do {
                            sb.append(line).append("\n");
                        } while ((line = stdout.readLine()) != null);

                        Platform.runLater(() -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("SearchResult.fxml"));
                                SearchResultController controller = new SearchResultController(searchTerm, sb.toString());
                                loader.setController(controller);

                                Parent parent = loader.load();
                                Scene createScene = new Scene(parent);
                                Stage window = Main.getPrimaryStage();
                                window.setScene(createScene);
                                window.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

	
}
