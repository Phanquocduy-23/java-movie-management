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
import vn.edu.nct.quanlyphim.model.Genre;

public class GenreDAO {

    public List<Genre> findAll() throws IOException, SQLException {
        String sql = """
            SELECT genre_id, name
            FROM public.genres
            ORDER BY LOWER(name), genre_id
            """;

        List<Genre> genres = new ArrayList<>();

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setQueryTimeout(15);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    genres.add(mapGenre(results));
                }
            }
        }

        return genres;
    }

    public Genre create(String name) throws IOException, SQLException {
        String sql = """
            INSERT INTO public.genres (name)
            VALUES (?)
            RETURNING genre_id, name
            """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setQueryTimeout(15);
            statement.setString(1, name);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return mapGenre(results);
                }
            }
        }

        throw new SQLException(
            "Không nhận được thông tin thể loại sau khi thêm."
        );
    }

    public Optional<Genre> update(long genreId, String name)
            throws IOException, SQLException {

        String sql = """
            UPDATE public.genres
            SET name = ?
            WHERE genre_id = ?
            RETURNING genre_id, name
            """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setQueryTimeout(15);
            statement.setString(1, name);
            statement.setLong(2, genreId);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return Optional.of(mapGenre(results));
                }
            }
        }

        return Optional.empty();
    }

    public boolean delete(long genreId)
            throws IOException, SQLException {

        String sql = """
            DELETE FROM public.genres
            WHERE genre_id = ?
            """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setQueryTimeout(15);
            statement.setLong(1, genreId);

            return statement.executeUpdate() == 1;
        }
    }

    private Genre mapGenre(ResultSet results) throws SQLException {
        return new Genre(
            results.getLong("genre_id"),
            results.getString("name")
        );
    }
}