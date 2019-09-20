package softeng206a3;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MediaPlayerController implements Initializable {

    private File fileUrl;
    private MediaPlayer player;

    @FXML
    private MediaView mediaView;

    @FXML
    private Button playPauseBtn;

    public MediaPlayerController(String filePath) {
        fileUrl = new File(filePath);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Media video = new Media(fileUrl.toURI().toString());
        player = new MediaPlayer(video);
        player.setAutoPlay(true);
        mediaView.setMediaPlayer(player);
    }

    @FXML
    private void handleBack() {
        player.dispose();
        Main.switchScene(getClass().getResource("CreationsList.fxml"));
    }

    @FXML
    private void playPause() {
        if (player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.pause();
            playPauseBtn.setText("Play");
        } else {
            player.play();
            playPauseBtn.setText("Pause");
        }
    }

    @FXML
    private void handleMute() {
        player.setMute(!player.isMute());
    }
}
