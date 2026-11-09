package vn.edu.nct.quanlyphim.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import vn.edu.nct.quanlyphim.dao.UserDAO;
import vn.edu.nct.quanlyphim.model.User;
import vn.edu.nct.quanlyphim.util.PasswordUtil;

public class AuthService {

    private static final String LOGIN_FAILED =
        "Tên đăng nhập, mật khẩu không đúng hoặc tài khoản đã bị khóa.";

    private final UserDAO userDAO = new UserDAO();

    public User register(
            String username,
            String fullName,
            char[] password,
            char[] confirmPassword)
            throws IOException, SQLException {

        String normalizedUsername = normalizeUsername(username);

        if (!normalizedUsername.matches("^[a-z0-9_]{3,50}$")) {
            throw new IllegalArgumentException(
                "Tên đăng nhập phải có 3–50 ký tự, "
                    + "chỉ gồm chữ không dấu, số và dấu gạch dưới."
            );
        }

        String normalizedName = fullName == null
            ? ""
            : fullName.strip();

        if (normalizedName.isEmpty() || normalizedName.length() > 100) {
            throw new IllegalArgumentException(
                "Họ tên không được để trống và tối đa 100 ký tự."
            );
        }

        if (password == null
                || password.length < 8
                || password.length > 128) {
            throw new IllegalArgumentException(
                "Mật khẩu phải có từ 8 đến 128 ký tự."
            );
        }

        if (confirmPassword == null
                || !Arrays.equals(password, confirmPassword)) {
            throw new IllegalArgumentException(
                "Mật khẩu xác nhận không khớp."
            );
        }

        String passwordHash = PasswordUtil.hash(password);

        try {
            return userDAO.createUser(
                normalizedUsername,
                normalizedName,
                passwordHash
            );

        } catch (SQLException e) {
            // PostgreSQL: vi phạm ràng buộc UNIQUE.
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                    "Tên đăng nhập đã được sử dụng.",
                    e
                );
            }

            throw e;
        }
    }

    public User login(String username, char[] password)
            throws IOException, SQLException {

        String normalizedUsername = normalizeUsername(username);

        if (!normalizedUsername.matches("^[a-z0-9_]{3,50}$")
                || password == null
                || password.length == 0
                || password.length > 128) {
            throw new IllegalArgumentException(LOGIN_FAILED);
        }

        Optional<UserDAO.LoginData> result =
            userDAO.findByUsername(normalizedUsername);

        if (result.isEmpty()) {
            throw new IllegalArgumentException(LOGIN_FAILED);
        }

        UserDAO.LoginData loginData = result.get();
        User user = loginData.getUser();

        boolean passwordMatches = PasswordUtil.verify(
            password,
            loginData.getPasswordHash()
        );

        if (!passwordMatches || !user.isActive()) {
            throw new IllegalArgumentException(LOGIN_FAILED);
        }

        if (!"ADMIN".equals(user.getRole())
                && !"USER".equals(user.getRole())) {
            throw new IllegalArgumentException(LOGIN_FAILED);
        }

        return user;
    }

    private String normalizeUsername(String username) {
        return username == null
            ? ""
            : username.strip().toLowerCase(Locale.ROOT);
    }
}