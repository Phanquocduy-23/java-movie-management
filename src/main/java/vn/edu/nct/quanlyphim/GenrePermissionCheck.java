package vn.edu.nct.quanlyphim;

import vn.edu.nct.quanlyphim.dao.UserDAO;
import vn.edu.nct.quanlyphim.model.User;
import vn.edu.nct.quanlyphim.service.GenreService;

public class GenrePermissionCheck {

    public static void main(String[] args) throws Exception {
        UserDAO userDAO = new UserDAO();

        User user = userDAO.findByUsername("quocduy")
            .orElseThrow(() -> new IllegalStateException(
                "Khong tim thay tai khoan quocduy."
            ))
            .getUser();

        if (!user.isActive() || !"USER".equals(user.getRole())) {
            throw new IllegalStateException(
                "Can tai khoan quocduy dang hoat dong, quyen USER."
            );
        }

        GenreService service = new GenreService(user);

        expectDenied(
            "USER cannot list management data",
            () -> service.findAll()
        );

        expectDenied(
            "USER cannot create genre",
            () -> service.create("")
        );

        expectDenied(
            "USER cannot update genre",
            () -> service.update(-1, "")
        );

        expectDenied(
            "USER cannot delete genre",
            () -> service.delete(-1)
        );

        System.out.println("ALL PERMISSION CHECKS PASSED");
    }

    private static void expectDenied(
            String name,
            CheckedAction action) throws Exception {

        try {
            action.run();

        } catch (SecurityException expected) {
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