package varpedia.tasks;

import javafx.concurrent.Task;
import varpedia.main.Main;

public class PlayAudioTask extends Task {
    private String _filePath;

    public PlayAudioTask(String filePath) {
        _filePath = filePath;
    }

    @Override
    protected Object call() throws Exception {
        String cmd = "aplay " + _filePath;
        Main.createNewProcess(cmd);
        return null;
    }

    public void destroyProcess() {
        Process process = Main.getCurrentProcess();
        if (process != null) {
            process.destroy();
        }
    }
}
