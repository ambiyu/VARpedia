package varpedia.tasks;

import javafx.concurrent.Task;
import varpedia.main.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class CreateCreationTask extends Task {

    private String _searchTerm;
    private String _creationName;
    private String _chunkFileList; // paths to each chunk audio file separated by spaces\
    private String _music;
    private int _numImages;

    public CreateCreationTask(String searchTerm, String creationName, String chunkFileList, String music,
                              int numImages) {
        _searchTerm = searchTerm;
        _creationName = creationName;
        _chunkFileList = chunkFileList;
        _music = music;
        _numImages = numImages;
    }

    @Override
    protected Object call() throws Exception {
        // combine chunks into a single audio file
        Main.execCmd("sox " + _chunkFileList + " .temp/combinedAudio.wav");

        // add background music
        if (!_music.equals("None")) {
            Main.execCmd("ffmpeg -y -i .temp/combinedAudio.wav -i resources/"+ _music +".mp3 -filter_complex amix=inputs=2:duration=shortest .temp/combinedAudioWithMusic.wav");
        }

        // get duration of combined audio
        String cmd = "soxi -D '.temp/combinedAudio.wav'";
        Process process = new ProcessBuilder("bash", "-c", cmd).start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            double totalDuration = Double.parseDouble(stdout.readLine());
            double imageDuration = _numImages / totalDuration;

            // combine images into a video
            //Main.execCmd("ffmpeg -framerate " + imageDuration + " -pattern_type glob -i '.temp/selectedImages/*.jpg' -c:v libx264 -vf \"scale=-2:min(1080\\,trunc(ih/2)*2)\" -r 25 .temp/combinedImages.mp4");
            Main.execCmd("cat .temp/selectedImages/*.jpg | ffmpeg -f image2pipe -framerate " + imageDuration + " -pattern_type glob -i - -c:v libx264 -vf \"scale=-2:min(1080\\,trunc(ih/2)*2)\" -r 25 .temp/combinedImages.mp4");

            // create video with the search term on top
            Main.execCmd("ffmpeg -i .temp/combinedImages.mp4 -vf drawtext=\"fontfile=resources/BubblegumSans-Regular.ttf: text='" + _searchTerm + "': fontcolor=white: fontsize=100: x=(w-text_w)/2: y=(h-text_h)/2\" -codec:a copy -t " + totalDuration + " -r 25 .temp/vidWithWord.mp4");

            // combine audio/video into one
            if (_music.equals("None")) {
                Main.execCmd("ffmpeg -i \".temp/vidWithWord.mp4\" -i \".temp/combinedAudio.wav\" -shortest creations/" + _creationName + ".mp4");
            } else {
                Main.execCmd("ffmpeg -i \".temp/vidWithWord.mp4\" -i \".temp/combinedAudioWithMusic.wav\" -shortest creations/" + _creationName + ".mp4");
            }

            // create additional video without search term on top, along with a text file containing the search term for the quiz
            File dir = new File(".quiz/" + _creationName);
            dir.mkdir();
            Main.execCmd("ffmpeg -i \".temp/combinedImages.mp4\" -i \".temp/combinedAudio.wav\" -shortest .quiz/" + _creationName + "/" + _creationName + ".mp4");
            Main.execCmd("echo \"" + _searchTerm + "\" > .quiz/" + _creationName + "/searchTerm.txt");

        } else {
            failed();
        }

        return null;
    }
}
