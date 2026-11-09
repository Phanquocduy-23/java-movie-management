package vn.edu.nct.quanlyphim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Locale;

import vn.edu.nct.quanlyphim.config.DatabaseConnection;
import vn.edu.nct.quanlyphim.model.User;
import vn.edu.nct.quanlyphim.service.AuthService;

public class AuthCheck {

    public static void main(String[] args) throws Exception {
        AuthService auth = new AuthService();

        String username = "test_" + java.util.UUID.randomUUID()
            .toString().replace("-", "");

        char[] password = "DemoPassword_123!".toCharArray();
        char[] wrongPassword = "WrongPassword_456!".toCharArray();

        User created = null;

        try {
            created = auth.register(
                username,
                "Tai khoan kiem tra",
                password,
                password
            );

            check(
                "Register creates active USER",
                created.isActive()
                    && "USER".equals(created.getRole())
                    && !created.isAdmin()
            );

            User loggedIn = auth.login(username, password);

            check(
                "Correct login",
                loggedIn.getUserId() == created.getUserId()
            );

            User normalizedLogin = auth.login(
                "  " + username.toUpperCase(Locale.ROOT) + "  ",
                password
            );

            check(
                "Username normalization",
                normalizedLogin.getUserId() == created.getUserId()
            );

            expectRejected(
                "Wrong password rejected",
                () -> auth.login(username, wrongPassword)
            );

            expectRejected(
                "Duplicate username rejected",
                () -> auth.register(
                    username,
                    "Tai khoan trung",
                    password,
                    password
                )
            );

            expectRejected(
                "Password confirmation mismatch rejected",
                () -> auth.register(
                    username + "_new",
                    "Tai khoan khong hop le",
                    password,
                    wrongPassword
                )
            );

            setActive(created.getUserId(), username, false);

            expectRejected(
                "Locked account rejected",
                () -> auth.login(username, password)
            );

            setActive(created.getUserId(), username, true);

            check(
                "Unlocked account can log in",
                auth.login(username, password).isActive()
            );

            System.out.println("ALL AUTH CHECKS PASSED");

        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(wrongPassword, '\0');

            if (created != null) {
                deleteTestUser(created.getUserId(), username);
            }
        }
    }

    private static void setActive(
            long userId,
            String username,
            boolean active) throws Exception {

        String sql = """
            UPDATE public.users
            SET is_active = ?
            WHERE user_id = ? AND username = ?
            """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBoolean(1, active);
            statement.setLong(2, userId);
            statement.setString(3, username);

            check(
                "Update test account status",
                statement.executeUpdate() == 1
            );
        }
    }

    private static void deleteTestUser(
            long userId,
            String username) throws Exception {

        String sql = """
            DELETE FROM public.users
            WHERE user_id = ? AND username = ?
            """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, userId);
            statement.setString(2, username);

            check(
                "Test account cleanup",
                statement.executeUpdate() == 1
            );
        }
    }

    private static void check(String name, boolean passed) {
        if (!passed) {
            throw new IllegalStateException("FAIL: " + name);
        }

        System.out.println("PASS: " + name);
    }

    private static void expectRejected(
            String name,
            CheckedAction action) throws Exception {

        try {
            action.run();

        } catch (IllegalArgumentException expected) {
            System.out.println("PASS: " + name);
            return;
        }

        throw new IllegalStateException("FAIL: " + name);
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }
}