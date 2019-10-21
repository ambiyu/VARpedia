package varpedia.main;

public class Creation {
    private int _id;
    private String _name;
    private String _searchTerm;

    public Creation(int id, String name, String searchTerm) {
        _id = id;
        _name = name;
        _searchTerm = searchTerm;
    }

    public Creation(int id, String name) {
        _id = id;
        _name = name;
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public String getSearchTerm() {
        return _searchTerm;
    }
}
