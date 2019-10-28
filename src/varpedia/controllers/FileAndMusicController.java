package varpedia.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import varpedia.main.Chunk;
import varpedia.main.Main;
import varpedia.tasks.CreateCreationTask;

public class FileAndMusicController implements Initializable {

	private String _searchTerm;
	private List<Chunk> _chunks;
	private ArrayList<Image> imagesToMerge;
	private ChunkManagerController _previousScene;

	@FXML private Button createBtn;
	@FXML private TextField fileNameInput;
	@FXML private ComboBox<String> musicSelection;
	@FXML private AnchorPane anchor;
	@FXML private Pane pane;
	private ProgressIndicator progress = new ProgressIndicator();
	private Label progressLabel = new Label("Making creation...");

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
		if (!fileNameInput.getText().matches("^[a-zA-Z0-9\\_-]+")) {
			displayError("Invalid character(s) in creation name. Only letters, numbers, hyphens and underscores are allowed.");
		} 
		else if (isConflicting(fileNameInput.getText().trim())) {
			displayError("Creation with the same name already exists. Please enter another name.");
		} 
		else {
			createBtn.setDisable(true);
			fileNameInput.setDisable(true);

			progressLabel.setText("Making creation...");
			progress.setVisible(true);
			progressLabel.setVisible(true);

			pane.setVisible(false);
			anchor.setVisible(true);

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

				// get chunk files in the correct order and put them all into a string separated by spaces
				StringBuilder chunkList = new StringBuilder();
				for (Chunk chunk : _chunks) {
					chunkList.append(".temp/chunks/chunk").append(chunk.getChunkNumber()).append(".wav ");
				}

				int numImages = imagesToMerge.size();
				String creationName = fileNameInput.getText();

				CreateCreationTask task = new CreateCreationTask(_searchTerm, creationName, chunkList.toString(),
						musicSelection.getValue(), numImages);

				task.setOnSucceeded(e -> {
					progress.setVisible(false);
					progressLabel.setVisible(false);

					//create a success alert
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Success");
					alert.setHeaderText(null);
					alert.setContentText("\"" + fileNameInput.getText() + "\" has been successfully created");
					alert.showAndWait();
					Main.switchScene(getClass().getResource("/varpedia/fxml/Menu.fxml"));
				});

				task.setOnFailed(e -> {
					displayError("An error occurred while attempting to generate creation");
					createBtn.setDisable(false);
					fileNameInput.setDisable(false);
				});

				Thread thread = new Thread(task);
				thread.start();

			} catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}

	@FXML
	public void enableCreate() {
		if (!fileNameInput.getText().trim().isEmpty()) {
			createBtn.setDisable(false);
		} else {
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
	
	private boolean isConflicting(String name) {
		try {
			String cmd = "test -f \"creations/" + name + ".mp4\"";
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
