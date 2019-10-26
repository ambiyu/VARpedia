package varpedia.controllers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import varpedia.main.Chunk;
import varpedia.main.Main;

public class FileAndMusicController implements Initializable {

	@FXML
	private Button createBtn;
	@FXML
	private TextField fileNameInput;
	@FXML
	private ChoiceBox<String> musicSelection;
	@FXML
	private AnchorPane anchor;
	@FXML
	private Pane pane;
	private ProgressIndicator progress = new ProgressIndicator();
	private Label progressLabel = new Label("Making creation...");
	
	private String _searchTerm;
	private List<Chunk> _chunks;
	
	private ArrayList<Image> imagesToMerge;
	private ChunkManagerController _previousScene;
	
	public FileAndMusicController( String name, ChunkManagerController scene, ArrayList<Image> images, List<Chunk> chunks) {
		_searchTerm = name;
		_previousScene = scene;
		imagesToMerge = images;
		_chunks = chunks;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// setup music choice
	    ObservableList<String> listOfMusic =  FXCollections.observableArrayList("None","Classical", "Jazz", "Techno");
	    musicSelection.setItems(listOfMusic);
	    musicSelection.setValue("None");
	    
	  //create and position loading wheel and label
	  		progress.setMinSize(100, 100);;
	  		progress.setLayoutY(anchor.getPrefHeight()/2 - 50);
	  		progress.setLayoutX(anchor.getPrefWidth()/2 - 50);
	  		progressLabel.setMinSize(100, 100);
	  		progressLabel.setLayoutY(anchor.getPrefHeight()/2 + 25);
	  		progressLabel.setLayoutX(anchor.getPrefWidth()/2 - 60);
	  		anchor.getChildren().add(progress);
	  		anchor.getChildren().add(progressLabel);
	  		progress.setVisible(false);
	  		progressLabel.setVisible(false);
		
	}
	
	
	@FXML
	public void handleCreate() {
		createBtn.setDisable(true);
		fileNameInput.setDisable(true);

		progress.setVisible(true);
  		progressLabel.setVisible(true);
		
		if (!fileNameInput.getText().matches("^[a-zA-Z0-9\\_-]+")) {
			displayError("Invalid character(s) in creation name. Only letters, numbers, hyphens and underscores are allowed.");
			createBtn.setDisable(false);
			fileNameInput.setDisable(false);
		} 
		else if (isConflicting("creations", fileNameInput.getText(), "mp4")) {
			displayError("Creation with the same name already exists. Please enter another name.");
			createBtn.setDisable(false);
			fileNameInput.setDisable(false);
		} 
		else {
			pane.setVisible(false);
			progressLabel.setText("Making creation...");
			anchor.setVisible(true);
			
			
			

			new Thread(() -> {
				try {
					int count = 0;

					//put selected images in a temporary folder
					for(Image im : imagesToMerge) {

						File dir = new File(".temp/selectedImages");
						dir.mkdir();

						File outputFile = new File(".temp/selectedImages", _searchTerm + count + ".jpg");
						try {
							BufferedImage bim = SwingFXUtils.fromFXImage(im, null);
							ImageIO.write(bim, "jpg",outputFile);
							count++;

						} catch(IOException e) {
							e.printStackTrace();
						}
					}

					// get chunks in the correct order
					StringBuilder chunkList = new StringBuilder();
					for (Chunk chunk : _chunks) {
						chunkList.append(".temp/chunks/chunk").append(chunk.getChunkNumber()).append(".wav ");
					}

					// combine chunks into a single audio file
					String combineAudioCmd = "sox " + chunkList.toString() + " .temp/combinedAudio.wav";
					Main.execCmd(combineAudioCmd);

					//adds background music
					if (!musicSelection.getValue().equals("None")) {
						String combineMusic = "ffmpeg -y -i .temp/combinedAudio.wav -i resources/"+ musicSelection.getValue() +".mp3 -filter_complex amix=inputs=2:duration=shortest .temp/combinedAudio.mp3";
						
						Main.execCmd(combineMusic);
					}

					String cmd = "soxi -D '.temp/combinedAudio.wav'";
					Process process = new ProcessBuilder("bash", "-c", cmd).start();
					int exitCode = process.waitFor();

					if (exitCode == 0) {
						BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
						double length = Double.parseDouble(stdout.readLine()) + 1;

						int numOfImages = imagesToMerge.size();
						double lengthOfImage = numOfImages/length;

						String creationName = fileNameInput.getText();

						// combine images into a video
						//Main.execCmd("cat .temp/images/*.jpg | ffmpeg -f image2pipe -framerate 1 -i - -i .temp/combinedAudio.wav -r " + lengthOfImage + " -pattern_type glob -c:v libx264 -pix_fmt yuv420p -vf \"scale=-2:min(1080\\,trunc(ih/2)*2)\" -r 25 -max_muxing_queue_size 1024 -y creations/" + fileNameInput.getText() + ".mp4");
						Main.execCmd("ffmpeg -framerate " + lengthOfImage + " -pattern_type glob -i '.temp/selectedImages/*.jpg' -c:v libx264 -vf \"scale=-2:min(1080\\,trunc(ih/2)*2)\" -r 25 .temp/combinedImages.mp4");

						// create video and then combine audio/video into one
						Main.execCmd("ffmpeg -i .temp/combinedImages.mp4 -vf drawtext=\"fontfile=resources/myFont.ttf: text='" + _searchTerm + "': fontcolor=white: fontsize=50: x=(w-text_w)/2: y=(h-text_h)/2\" -codec:a copy -t " + length + " -r 25 .temp/vidWithWord.mp4");

						//checks if it needs to combine .mp3 or .wav
						if (!musicSelection.getValue().equals("None")) {
							Main.execCmd("ffmpeg -i \".temp/vidWithWord.mp4\" -i \".temp/combinedAudio.mp3\" -shortest creations/" + creationName + ".mp4");
						} else {
							Main.execCmd("ffmpeg -i \".temp/vidWithWord.mp4\" -i \".temp/combinedAudio.wav\" -shortest creations/" + creationName + ".mp4");
						}

						// QUIZ stuff
						File dir = new File(".quiz/" + creationName);
						dir.mkdir();
						Main.execCmd("ffmpeg -i \".temp/combinedImages.mp4\" -i \".temp/combinedAudio.wav\" -shortest .quiz/" + creationName + "/" + creationName + ".mp4");
						Main.execCmd("echo \"" + _searchTerm + "\" > .quiz/" + creationName + "/searchTerm.txt");


						Platform.runLater(() -> {


							progress.setVisible(false);
							progressLabel.setVisible(false);

							//create a success alert
							Alert alert = new Alert(Alert.AlertType.INFORMATION);
							alert.setTitle("Success");
							alert.setHeaderText(null);
							alert.setContentText("Successfully created creation \"" + fileNameInput.getText() + "\"");
							alert.showAndWait();
							Main.switchScene(getClass().getResource("/varpedia/fxml/Menu.fxml"));
						});
					} else {
						Platform.runLater(() -> {
							displayError("An error occurred while attempting to generate creation");
							createBtn.setDisable(false);
							fileNameInput.setDisable(false);
						});
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}).start();
		}		
	}
	@FXML
	public void enableCreate() {
		if(!fileNameInput.getText().trim().isEmpty()) {
			createBtn.setDisable(false);
		}
		else {
			createBtn.setDisable(true);
		}
	}
	@FXML
	private void returnToMenu() {
		if (Main.returnToMenuWarning()) {
			Main.switchScene(getClass().getResource("/varpedia/fxml/Menu.fxml"));
		}
	}
	
	@FXML
	public void handleBack() {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/varpedia/fxml/ImageChoice.fxml"));
			ImageChoiceController scene = new ImageChoiceController(_chunks, _searchTerm, _previousScene);
			
			loader.setController(scene);

			Parent parent = loader.load();
			Scene createScene = new Scene(parent);
			Stage window = Main.getPrimaryStage();
			window.setScene(createScene);
			window.show();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	private void displayError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("ERROR");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
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

	
}
