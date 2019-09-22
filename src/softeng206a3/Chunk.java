package softeng206a3;

public class Chunk {
    private int _chunkNumber;
    private String _description;
    //private String _voice;

    public Chunk(int chunkNumber, String description) {
        _chunkNumber = chunkNumber;
        _description = description;
        //_voice = voice;
    }

    public int getChunkNumber() {
        return _chunkNumber;
    }

    public String getDescription() {
        return _description;
    }

/*    public String getVoice() {
        return _voice;
    }*/
}
