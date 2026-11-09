package vn.edu.nct.quanlyphim.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import vn.edu.nct.quanlyphim.config.DatabaseConnection;
import vn.edu.nct.quanlyphim.model.Movie;

public class MovieDAO {

    // Phần truy vấn chung cho danh sách và chi tiết phim.
    private static final String SELECT_MOVIES = """
        SELECT
            m.movie_id,
            m.title,
            m.release_year,
            m.duration_minutes,
            m.description,
            m.poster_path,
            COALESCE(
                STRING_AGG(g.name, ', ' ORDER BY g.name),
                'Chưa phân loại'
            ) AS genres
        FROM public.movies m
        LEFT JOIN public.movie_genres mg
            ON mg.movie_id = m.movie_id
        LEFT JOIN public.genres g
            ON g.genre_id = mg.genre_id
        """;

    private static final String GROUP_MOVIES = """
        GROUP BY
            m.movie_id,
            m.title,
            m.release_year,
            m.duration_minutes,
            m.description,
            m.poster_path
        """;

    // Lấy danh sách phim đang hiển thị.
    public List<Movie> findAllActive()
            throws IOException, SQLException {

        String sql = SELECT_MOVIES
            + "\nWHERE m.is_active = TRUE\n"
            + GROUP_MOVIES
            + "\nORDER BY m.movie_id";

        List<Movie> movies = new ArrayList<>();

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setQueryTimeout(15);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    movies.add(mapMovie(results));
                }
            }
        }

        return movies;
    }

    // Lấy một phim đang hiển thị theo mã.
    public Optional<Movie> findActiveById(long movieId)
            throws IOException, SQLException {

        String sql = SELECT_MOVIES
            + "\nWHERE m.is_active = TRUE AND m.movie_id = ?\n"
            + GROUP_MOVIES;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setQueryTimeout(15);
            statement.setLong(1, movieId);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return Optional.of(mapMovie(results));
                }
            }
        }

        return Optional.empty();
    }

    // Chuyển một dòng dữ liệu SQL thành đối tượng Movie.
    private Movie mapMovie(ResultSet results) throws SQLException {
        return new Movie(
            results.getLong("movie_id"),
            results.getString("title"),
            results.getObject("release_year", Integer.class),
            results.getObject("duration_minutes", Integer.class),
            results.getString("genres"),
            results.getString("description"),
            results.getString("poster_path")
        );
    }
}