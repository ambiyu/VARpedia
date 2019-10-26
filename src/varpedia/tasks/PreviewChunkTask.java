package varpedia.tasks;

import javafx.concurrent.Task;

public class PreviewChunkTask extends Task {
    private Process _process;

    @Override
    protected Object call() throws Exception {
        String cmd = "aplay .temp/preview.wav";
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
