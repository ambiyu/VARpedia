package varpedia.main;

public enum Voice {
    kal_diphone("US Male"),
    akl_nz_jdt_diphone("NZ Male"),
    akl_nz_cw_cg_cg("NZ Female");

    private String _name;

    Voice(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public static Voice fromString(String name) {
        for (Voice voice : Voice.values()) {
            if (voice._name.equals(name)) {
                return voice;
            }
        }
        throw new IllegalArgumentException("No voice with name: " + name);
    }

}
