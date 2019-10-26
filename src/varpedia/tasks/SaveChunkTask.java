package varpedia.tasks;

import javafx.concurrent.Task;
import varpedia.main.Main;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SaveChunkTask extends Task {
    private String _text;
    private String _voice;
    private int _id;
    private boolean _isPreview;

    public SaveChunkTask(String text, String voice, int id, boolean isPreview) {
        _text = text;
        _voice = voice;
        _id = id;
        _isPreview = isPreview;
    }

    @Override
    protected Integer call() throws Exception {
        String cmd;

        if (_isPreview) {
            // create .wav audio file with selected voice
            Main.execCmd("echo \"" + _text + "\" | text2wave -eval '(voice_" + _voice + ")' -o .temp/preview.wav");
            cmd = "wc -c < .temp/preview.wav";
        } else {
            Main.execCmd("echo \"" + _text + "\" | text2wave -eval '(voice_" + _voice + ")' -o \".temp/chunks/chunk" + _id + ".wav\"");
            cmd = "wc -c < .temp/chunks/chunk" + _id + ".wav";
        }

        // check file size in case of any errors
        Process process = new ProcessBuilder("bash", "-c", cmd).start();
        process.waitFor();
        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // return size of file
        return Integer.parseInt(stdout.readLine());
    }
}
