package vn.edu.nct.quanlyphim.model;

public class Genre {

    private final long genreId;
    private final String name;

    public Genre(long genreId, String name) {
        this.genreId = genreId;
        this.name = name;
    }

    public long getGenreId() {
        return genreId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}