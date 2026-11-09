package vn.edu.nct.quanlyphim.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import vn.edu.nct.quanlyphim.config.DatabaseConnection;
import vn.edu.nct.quanlyphim.model.User;

public class UserDAO {

    // Chỉ dùng trong quá trình xác thực, không đưa lên giao diện.
    public static final class LoginData {

        private final User user;
        private final String passwordHash;

        public LoginData(User user, String passwordHash) {
            this.user = user;
            this.passwordHash = passwordHash;
        }

        public User getUser() {
            return user;
        }

        public String getPasswordHash() {
            return passwordHash;
        }
    }

    public Optional<LoginData> findByUsername(String username)
            throws IOException, SQLException {

        String sql = """
            SELECT
                user_id,
                username,
                full_name,
                role,
                is_active,
                password_hash
            FROM public.users
            WHERE username = ?
            """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setQueryTimeout(15);
            statement.setString(1, username);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    User user = mapUser(results);

                    return Optional.of(
                        new LoginData(
                            user,
                            results.getString("password_hash")
                        )
                    );
                }
            }
        }

        return Optional.empty();
    }

    // Tài khoản đăng ký mới luôn mang quyền USER.
    // Tham số passwordHash phải là kết quả của PasswordUtil.hash().
    public User createUser(
            String username,
            String fullName,
            String passwordHash)
            throws IOException, SQLException {

        String sql = """
            INSERT INTO public.users (
                username,
                full_name,
                password_hash,
                role,
                is_active
            )
            VALUES (?, ?, ?, 'USER', TRUE)
            RETURNING
                user_id,
                username,
                full_name,
                role,
                is_active
            """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setQueryTimeout(15);
            statement.setString(1, username);
            statement.setString(2, fullName);
            statement.setString(3, passwordHash);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return mapUser(results);
                }
            }
        }

        throw new SQLException(
            "Không nhận được thông tin tài khoản sau khi tạo."
        );
    }

    private User mapUser(ResultSet results) throws SQLException {
        return new User(
            results.getLong("user_id"),
            results.getString("username"),
            results.getString("full_name"),
            results.getString("role"),
            results.getBoolean("is_active")
        );
    }
}