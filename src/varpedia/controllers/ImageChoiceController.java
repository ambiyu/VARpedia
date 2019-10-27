package varpedia.controllers;


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import varpedia.main.Chunk;
import varpedia.main.ImageDownload;
import varpedia.main.Main;


public class ImageChoiceController extends HelpScene implements Initializable {

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
	private Pane loadingPane;
	@FXML
	private ChoiceBox<String> musicSelection;

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

		//create and position loading wheel and label
		progress.setMinSize(100, 100);;
		progress.setLayoutY(loadingPane.getPrefHeight()/2 - 50);
		progress.setLayoutX(loadingPane.getPrefWidth()/2 - 50);
		progressLabel.setMinSize(100, 100);
		progressLabel.setLayoutY(loadingPane.getPrefHeight()/2 + 25);
		progressLabel.setLayoutX(loadingPane.getPrefWidth()/2 - 60);
		loadingPane.getChildren().add(progress);
		loadingPane.getChildren().add(progressLabel);

		
		
		setUpList();

		
		pane.setVisible(false);

		new Thread(() ->{
			ImageDownload downloader = new ImageDownload();
			downloader.downloadImages(_searchTerm, 15);

			FileInputStream flickrImage;
			int numOfImages = new File(".temp/images").list().length;

			try {
				FileInputStream greenTickFile = new FileInputStream("resources/greenTick.png");
				greenTick = new Image(greenTickFile);
				greenTickFile.close();

				for (int i = 0; i < numOfImages; i++) {
					flickrImage = new FileInputStream(".temp/images/" + _searchTerm + "-" + i + ".jpg");
					Image image = new Image(flickrImage);

					listOfImages.get(i).setImage(image);
					allGreenTicks.get(i).setImage(greenTick);
					flickrImage.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			Platform.runLater(() -> {
				loadingPane.setVisible(false);
				pane.setVisible(true);
			});
		}).start();
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
		
		
		if(selectedImage.getOpacity() == 0.3) {
			selectedImage.setOpacity(1);
			allGreenTicks.get(listOfImages.indexOf(selectedImage)).setOpacity(0);
			if(imagesToMerge.contains(selectedImage.getImage())) {
				imagesToMerge.remove(selectedImage.getImage());
			}
		}
		else {

			if(selectedImage != null) {
				allGreenTicks.get(listOfImages.indexOf(selectedImage)).setOpacity(1);
				selectedImage.setOpacity(0.3);
				imagesToMerge.add(selectedImage.getImage());
				
			}
			
		}
	}

   @FXML
   public void handleNext() {
	   int numOfImages = imagesToMerge.size();
	   if( numOfImages > 10) {
			int exceedingImages = imagesToMerge.size() - 10;
			displayError("Only 10 images may be selected, please unselect " +  exceedingImages + " image(s)");
		}
	   else if(numOfImages == 0) {
		   displayError("No images selected");

	   }
	   else {
		   try {
			   Main.execCmd("rm -r .temp/images");
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/varpedia/fxml/FileAndMusic.fxml"));
				FileAndMusicController scene = new FileAndMusicController(_searchTerm, _previousScene, imagesToMerge, _chunks);
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
   }

	@FXML
	public void handleBack() {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/varpedia/fxml/ChunkManager.fxml"));
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
			Main.switchScene(getClass().getResource("/varpedia/fxml/Menu.fxml"));
		}
	}

	private void displayError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("ERROR");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
	//method to put all immageViews in arrays
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
