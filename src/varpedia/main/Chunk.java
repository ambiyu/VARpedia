package varpedia.main;

public class Chunk {
    private int _chunkNumber;
    private String _text;
    private String _voice;

    public Chunk(int chunkNumber, String text, String voice) {
        _chunkNumber = chunkNumber;
        _text = text;
        _voice = voice;
    }

    public int getChunkNumber() {
        return _chunkNumber;
    }

    public String getText() {
        return _text;
    }

    public String getVoice() {
        return _voice;
    }
}
