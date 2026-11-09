package vn.edu.nct.quanlyphim.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import vn.edu.nct.quanlyphim.dao.GenreDAO;
import vn.edu.nct.quanlyphim.dao.UserDAO;
import vn.edu.nct.quanlyphim.model.Genre;
import vn.edu.nct.quanlyphim.model.User;

public class GenreService {

    private final User currentUser;
    private final GenreDAO genreDAO = new GenreDAO();
    private final UserDAO userDAO = new UserDAO();

    public GenreService(User currentUser) {
        this.currentUser = java.util.Objects.requireNonNull(
            currentUser
        );
    }

    public List<Genre> findAll() throws IOException, SQLException {
        requireAdmin();
        return genreDAO.findAll();
    }

    public Genre create(String name) throws IOException, SQLException {
        requireAdmin();
        String normalizedName = validateName(name);

        try {
            return genreDAO.create(normalizedName);

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                    "Tên thể loại đã tồn tại.",
                    e
                );
            }

            throw e;
        }
    }

    public Genre update(long genreId, String name)
            throws IOException, SQLException {

        requireAdmin();
        validateId(genreId);
        String normalizedName = validateName(name);

        try {
            return genreDAO.update(genreId, normalizedName)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Thể loại không còn tồn tại. "
                        + "Vui lòng tải lại danh sách."
                ));

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                    "Tên thể loại đã được sử dụng.",
                    e
                );
            }

            throw e;
        }
    }

    public void delete(long genreId)
            throws IOException, SQLException {

        requireAdmin();
        validateId(genreId);

        try {
            boolean deleted = genreDAO.delete(genreId);

            if (!deleted) {
                throw new IllegalArgumentException(
                    "Thể loại không còn tồn tại. "
                        + "Vui lòng tải lại danh sách."
                );
            }

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                    "Không thể xóa thể loại đang được sử dụng cho phim.",
                    e
                );
            }

            throw e;
        }
    }

    private void requireAdmin() throws IOException, SQLException {
        UserDAO.LoginData account = userDAO.findByUsername(
            currentUser.getUsername()
        ).orElseThrow(() -> new SecurityException(
            "Tài khoản không còn tồn tại. Vui lòng đăng nhập lại."
        ));

        User latestUser = account.getUser();

        if (latestUser.getUserId() != currentUser.getUserId()
                || !latestUser.isActive()
                || !latestUser.isAdmin()) {
            throw new SecurityException(
                "Bạn không có quyền quản lý thể loại "
                    + "hoặc tài khoản đã bị khóa."
            );
        }
    }

    private String validateName(String name) {
        String normalizedName = name == null ? "" : name.strip();

        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException(
                "Tên thể loại không được để trống."
            );
        }

        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException(
                "Tên thể loại tối đa 100 ký tự."
            );
        }

        return normalizedName;
    }

    private void validateId(long genreId) {
        if (genreId <= 0) {
            throw new IllegalArgumentException(
                "Mã thể loại không hợp lệ."
            );
        }
    }
}