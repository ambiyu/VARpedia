package softeng206a3;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class ImageChoiceController implements Initializable {

	@FXML
	private ImageView imageView0,imageView1,imageView2,imageView3,imageView4;
	@FXML	
	private ImageView imageView5,imageView6,imageView7,imageView8,imageView9;
	@FXML
	private ImageView imageView10,imageView11,imageView12,imageView13,imageView14;
	@FXML
	private ImageView greenTickView0, greenTickView1, greenTickView2, greenTickView3, greenTickView4, greenTickView5, greenTickView6, greenTickView7, greenTickView8, greenTickView9, greenTickView10, greenTickView11, greenTickView12, greenTickView13, greenTickView14;

	@FXML
	private Button createBtn;
	@FXML
	private TextField fileNameInput;
	@FXML
	private Pane pane;
	@FXML
	private AnchorPane anchor;
	@FXML
	private CheckBox musicOption;

	private ProgressIndicator progress = new ProgressIndicator();
	private Label progressLabel = new Label("Retrieving Images...");

	private ArrayList<ImageView> listOfImages = new ArrayList<>();
	private ArrayList<Image> imagesToMerge = new ArrayList<>();
	private ArrayList<ImageView> allGreenTicks = new ArrayList<>();

	private String _searchTerm;
	private List<Chunk> _chunks;
	private ChunkManagerController _previousScene;


	private Image greenTick;

	public ImageChoiceController(List<Chunk> allChunks, String name, ChunkManagerController scene) {
		_searchTerm = name;
		_chunks = allChunks;
		_previousScene = scene;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("START");



		//create spinning loading thing and label
		progress.setMinSize(100, 100);;
		progress.setLayoutY(anchor.getPrefHeight()/2 - 50);
		progress.setLayoutX(anchor.getPrefWidth()/2 - 50);
		progressLabel.setMinSize(100, 100);
		progressLabel.setLayoutY(anchor.getPrefHeight()/2 + 25);
		progressLabel.setLayoutX(anchor.getPrefWidth()/2 - 60);
		anchor.getChildren().add(progress);
		anchor.getChildren().add(progressLabel);

		setUpList();
		
		createBtn.setDisable(true);
		pane.setVisible(false);

		new Thread(() ->{
			ImageDownload downloader = new ImageDownload();
			downloader.downloadImages(_searchTerm, 15);

			FileInputStream file;
			int numOfImages = new File(".temp/images").list().length;

			try {
//				greenTick = new Image(new FileInputStream("resources/greenTick.png"));
//				greenTickView.setImage(greenTick);

				for (int i = 0; i < numOfImages; i++) {
					file = new FileInputStream(".temp/images/" + _searchTerm + "-" + i + ".jpg");
					Image image = new Image(file);

					listOfImages.get(i).setImage(image);
					
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			Platform.runLater(() -> {
				progress.setVisible(false);
				progressLabel.setVisible(false);
				pane.setVisible(true);
			});
		}).start();
	}

	@FXML
	public void handleCreate() {
		createBtn.setDisable(true);

		if(imagesToMerge.size() > 10) {
			int exceedingImages = imagesToMerge.size() - 10;
			displayError("Only 10 images may be selected, please unselect " +  exceedingImages + " image(s)");
			createBtn.setDisable(false);
		}
		else if (!fileNameInput.getText().matches("^[a-zA-Z0-9\\_-]+")) {
			displayError("Invalid character(s) in creation name. Only letters, numbers, hyphens and underscores are allowed.");
			createBtn.setDisable(false);
		} 
		else if (isConflicting("creations", fileNameInput.getText(), "mp4")) {
			displayError("Creation with the same name already exists. Please enter another name.");
			createBtn.setDisable(false);
		} 
		else {
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
					if (musicOption.isSelected()) {
						String combineMusic = "ffmpeg -y -i .temp/combinedAudio.wav -i resources/destinazione_altrove_-_Billions_of_stars_1.mp3 -filter_complex amix=inputs=2:duration=shortest .temp/combinedAudio.mp3";
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
						if (musicOption.isSelected()) {
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
							createBtn.setDisable(false);
							//create a success alert
							Alert alert = new Alert(Alert.AlertType.INFORMATION);
							alert.setTitle("Success");
							alert.setHeaderText(null);
							alert.setContentText("Successfully created creation \"" + fileNameInput.getText() + "\"");
							alert.showAndWait();
						});
					} else {
						Platform.runLater(() -> {
							displayError("An error occurred while attempting to generate creation");
							createBtn.setDisable(false);
						});
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}).start();
		}
	}

	@FXML
	public void handleClick(MouseEvent event) {
		ImageView selectedImage = null;
		

		//finds what image was clicked
		for(ImageView im : listOfImages) {
			if(event.getSource().equals(im)) {
				selectedImage = im;
			}
			else if(event.getSource().equals(allGreenTicks.get(listOfImages.indexOf(im)))){
				selectedImage = im;
			}
		}

		if(selectedImage.getOpacity() == 0.2) {
			selectedImage.setOpacity(1);
			allGreenTicks.get(listOfImages.indexOf(selectedImage)).setOpacity(0);
			if(imagesToMerge.contains(selectedImage.getImage())) {
				imagesToMerge.remove(selectedImage.getImage());
			}
		}
		else {

			if(selectedImage != null) {
			allGreenTicks.get(listOfImages.indexOf(selectedImage)).setOpacity(1);
			selectedImage.setOpacity(0.2);
			}
			imagesToMerge.add(selectedImage.getImage());
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
private void returnToMenu() {
	if (Main.returnToMenuWarning()) {
		Main.switchScene(getClass().getResource("Menu.fxml"));
	}
}

private void displayError(String message) {
	Alert alert = new Alert(Alert.AlertType.ERROR);
	alert.setTitle("ERROR");
	alert.setHeaderText(null);
	alert.setContentText(message);
	alert.showAndWait();
}

@FXML
public void enableCreate() {
	if(!fileNameInput.getText().trim().isEmpty() && imagesToMerge.size() > 0) {
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

private void setUpList() {
	
	listOfImages.add(imageView0); allGreenTicks.add(greenTickView0); 
	listOfImages.add(imageView1); allGreenTicks.add(greenTickView1);
	listOfImages.add(imageView2); allGreenTicks.add(greenTickView2);
	listOfImages.add(imageView3); allGreenTicks.add(greenTickView3);
	listOfImages.add(imageView4); allGreenTicks.add(greenTickView4);
	listOfImages.add(imageView5); allGreenTicks.add(greenTickView5);
	listOfImages.add(imageView6); allGreenTicks.add(greenTickView6);
	listOfImages.add(imageView7); allGreenTicks.add(greenTickView7);
	listOfImages.add(imageView8); allGreenTicks.add(greenTickView8);
	listOfImages.add(imageView9); allGreenTicks.add(greenTickView9);
	listOfImages.add(imageView10); allGreenTicks.add(greenTickView10);
	listOfImages.add(imageView11); allGreenTicks.add(greenTickView11);
	listOfImages.add(imageView12); allGreenTicks.add(greenTickView12);
	listOfImages.add(imageView13); allGreenTicks.add(greenTickView13);
	listOfImages.add(imageView14); allGreenTicks.add(greenTickView14);
}

}
