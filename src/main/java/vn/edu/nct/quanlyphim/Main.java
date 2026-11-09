package vn.edu.nct.quanlyphim;

import javax.swing.SwingUtilities;

import vn.edu.nct.quanlyphim.ui.LoginFrame;
import vn.edu.nct.quanlyphim.ui.MovieListFrame;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::showLogin);
    }

    private static void showLogin() {
        LoginFrame loginFrame = new LoginFrame(user -> {
            MovieListFrame movieWindow = new MovieListFrame(
                user,
                Main::showLogin
            );

            movieWindow.setVisible(true);
        });

        loginFrame.setVisible(true);
    }
}