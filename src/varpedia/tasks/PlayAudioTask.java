package varpedia.tasks;

import javafx.concurrent.Task;

public class PlayAudioTask extends Task {
    private Process _process;
    private String _filePath;

    public PlayAudioTask(String filePath) {
        _filePath = filePath;
    }

    @Override
    protected Object call() throws Exception {
        String cmd = "aplay " + _filePath;
        _process = new ProcessBuilder("bash", "-c", cmd).start();
        _process.waitFor();
        return null;
    }

    public void destroyProcess() {
        if (_process != null) {
            _process.destroyForcibly();
        }
    }
}
