package varpedia.tasks;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WikiSearchTask extends Task {
    private String _searchTerm;

    public WikiSearchTask(String searchTerm) {
        _searchTerm = searchTerm;
    }

    @Override
    protected String call() throws Exception {
        String cmd = "wikit \"" + _searchTerm + "\" | grep -o '[^ ].*'";
        Process process = new ProcessBuilder("bash", "-c", cmd).start();
        process.waitFor();
        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line = stdout.readLine();

        if (line.endsWith(" not found :^(")) { // search term not found on wikit
            failed();
            return null;
        } else {

            do {
                sb.append(line).append("\n");
            } while ((line = stdout.readLine()) != null);

            return sb.toString();
        }
    }
}
