package vn.edu.nct.quanlyphim.model;

public class Movie {

    private final long movieId;
    private final String title;
    private final Integer releaseYear;
    private final Integer durationMinutes;
    private final String genres;
    private final String description;
    private final String posterPath;

    // Dùng khi lấy danh sách phim.
    public Movie(
            long movieId,
            String title,
            Integer releaseYear,
            Integer durationMinutes,
            String genres) {

        this(
            movieId,
            title,
            releaseYear,
            durationMinutes,
            genres,
            null,
            null
        );
    }

    // Dùng khi lấy đầy đủ thông tin chi tiết.
    public Movie(
            long movieId,
            String title,
            Integer releaseYear,
            Integer durationMinutes,
            String genres,
            String description,
            String posterPath) {

        this.movieId = movieId;
        this.title = title;
        this.releaseYear = releaseYear;
        this.durationMinutes = durationMinutes;
        this.genres = genres;
        this.description = description;
        this.posterPath = posterPath;
    }

    public long getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public String getGenres() {
        return genres;
    }

    public String getDescription() {
        return description;
    }

    public String getPosterPath() {
        return posterPath;
    }
}