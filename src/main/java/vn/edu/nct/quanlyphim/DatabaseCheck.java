package vn.edu.nct.quanlyphim;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import vn.edu.nct.quanlyphim.config.DatabaseConnection;

public class DatabaseCheck {

    public static void main(String[] args) {
        System.setOut(new java.io.PrintStream(
            System.out,
            true,
            java.nio.charset.StandardCharsets.UTF_8
        ));

        String sql = """
            SELECT movie_id, title, release_year
            FROM public.movies
            ORDER BY movie_id
            """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet results = statement.executeQuery()
        ) {
            System.out.println("Kết nối PostgreSQL thành công!");

            int count = 0;

            while (results.next()) {
                System.out.printf(
                    "%d | %s | %d%n",
                    results.getLong("movie_id"),
                    results.getString("title"),
                    results.getInt("release_year")
                );
                count++;
            }

            System.out.println("Tổng số phim: " + count);

        } catch (IOException e) {
            System.err.println(
                "Không đọc được cấu hình: " + e.getMessage()
            );
        } catch (SQLException e) {
            System.err.println(
                "Lỗi CSDL [" + e.getSQLState() + "]: " + e.getMessage()
            );
        }
    }
}