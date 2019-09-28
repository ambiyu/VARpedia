package softeng206a3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ImageSelectController implements Initializable {

	private String _creationName;
	private List<Chunk> _audioText;
	private ChunkManagerController _previousScene; 
	private int numOfImages;

	@FXML
	private Button createBtn;

	@FXML
	private Button backBtn;

	@FXML 
	private Spinner<Integer> numberChoice;

	@FXML
	private TextField fileNameInput;

	@FXML
	private Label title;

	@FXML
	private Label spinnerTitle;

	@FXML
	private Label textAreaTitle;

	@FXML
	private Button returnToMenuBtn;


	public ImageSelectController(String searchTerm, List<Chunk> textForAudio, ChunkManagerController reference) {
		_creationName = searchTerm;
		_audioText = textForAudio;
		_previousScene = reference;
	}


	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		SpinnerValueFactory<Integer> spinnerValues = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
		numberChoice.setValueFactory(spinnerValues);	
		createBtn.setDisable(true);
	}


	@FXML
	public void handleCreate() { 
		
			if (isConflicting("creations", fileNameInput.getText(), "mp4")) {
				displayError("Creation with the same name already exists. Please enter another name.");

			} else {

				new Thread(() -> {
					try {
						Main.execCmd("mkdir .temp/" + _creationName);
						
						//Download Images
						ImageDownload downloader = new ImageDownload();
						numOfImages = downloader.downloadImages(_creationName, numberChoice.getValue());
						
						// create text and audio files
						//Main.execCmd("echo \"" + _audioText + "\" > '.temp/" + _creationName + "/text.txt'");
						//Main.execCmd("text2wave '.temp/" + _creationName + "/text.txt' -o '.temp/" + _creationName+ "/audio.wav'");

						// get length of audio
						String combineAudioCmd = "sox .temp/*.wav .temp/allAudio.wav";
						Main.execCmd(combineAudioCmd);
						
						String cmd = "soxi -D '.temp/allAudio.wav'";
						Process process = new ProcessBuilder("bash", "-c", cmd).start();
						process.waitFor();
						BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
						double length = Double.parseDouble(stdout.readLine()) + 1;
						
						double lengthOfImage = 1/(length/numOfImages) ;
													
						// combine images into a video 
						Main.execCmd("ffmpeg -r " + lengthOfImage + " -pattern_type glob -i '.temp/downloads/*.jpg' -c:v libx264 -vf \"scale=-2:min(1080\\,trunc(ih/2)*2)\" .temp/combinedImages.mp4");
						
						// create video and then combine audio/video into one
						Main.execCmd("ffmpeg -i .temp/combinedImages.mp4 -vf drawtext=\"fontfile=myFont.ttf: \\\n" + "text='" + _creationName + "': fontcolor=white: fontsize=32: x=(w-text_w)/2: y=(h-text_h)/2\" -codec:a copy -t " + length + " .temp/vidWithWord.mp4");												
						Main.execCmd("ffmpeg -i \".temp/vidWithWord.mp4\" -i \".temp/allAudio.wav\" -shortest creations/" + fileNameInput.getText() + ".mp4");
						
				Platform.runLater(() -> {
							//create a success alert
							Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText("Successfully created file " + fileNameInput.getText() + ".mp4");
                            alert.showAndWait();
						});

					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();
			}
		}
	


	@FXML
	public void handleBack() {
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ChunkManager.fxml"));
			loader.setController(_previousScene);
			Parent parent = loader.load();
			Scene createScene = new Scene(parent);
			Stage window = Main.getPrimaryStage();
			window.setScene(createScene);
			window.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void returnToMenu() {
		Main.switchScene(getClass().getResource("Menu.fxml"));
	}
	
	@FXML
	public void allowCreation() {
		if(!fileNameInput.getText().trim().equals("")){
			createBtn.setDisable(false);
			
		}
		else {
			createBtn.setDisable(true);
		}
	}

	private boolean isConflicting(String folder, String name, String format) {
		try {
			String cmd = "test -f \"" + folder + "/" + name + "." + format + "\"";
			Process process = new ProcessBuilder("bash", "-c", cmd).start();
			int exitStatus = process.waitFor();

			if (exitStatus == 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	 private void displayError(String message) {
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("ERROR");
	        alert.setHeaderText(null);
	        alert.setContentText(message);
	        alert.showAndWait();
	    }
	
}
