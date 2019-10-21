package varpedia.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.util.Duration;
import varpedia.main.Main;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MediaPlayerController implements Initializable {

    private File fileUrl;
    private MediaPlayer player;

    @FXML
    private BorderPane borderPane;

    @FXML
    private MediaView mediaView;

    @FXML
    private Button playPauseBtn;

    @FXML
    private Text currentTime;

    public MediaPlayerController(String filePath) {
        fileUrl = new File(filePath);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Media video = new Media(fileUrl.toURI().toString());
        player = new MediaPlayer(video);
        player.setAutoPlay(true);
        mediaView.setMediaPlayer(player);
        mediaView.fitHeightProperty().bind(borderPane.heightProperty());
        mediaView.fitWidthProperty().bind(borderPane.widthProperty());
        
        player.setOnEndOfMedia(() -> {
        	playPauseBtn.setText("Replay");
        });
        
        player.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            currentTime.setText(String.format("%.1f", newTime.toSeconds()));
        });
    }

    @FXML
    private void handleBack() {
        player.dispose();
        Main.switchScene(getClass().getResource("/varpedia/fxml/CreationsList.fxml"));
    }

    @FXML
    private void playPause() {
        if (playPauseBtn.getText().equals("Replay")) {
            player.seek(new Duration(0));
            playPauseBtn.setText("Pause");
        }
        else if(player.getStatus() == MediaPlayer.Status.PLAYING) {
        	player.pause();
            playPauseBtn.setText("Play");
        }
        else {
            player.play();
            playPauseBtn.setText("Pause");
        }
    }

    @FXML
    private void handleMute() {
        player.setMute(!player.isMute());
    }

    @FXML
    private void handleBackward() {
        Duration time = player.getCurrentTime();
        player.seek(time.subtract(Duration.seconds(3)));
    }

    @FXML
    private void handleForward() {

        Duration time = player.getCurrentTime();
        player.seek(time.add(Duration.seconds(3)));
    }
}
