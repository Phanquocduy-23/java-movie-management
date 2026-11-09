package vn.edu.nct.quanlyphim.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import vn.edu.nct.quanlyphim.model.User;
import vn.edu.nct.quanlyphim.service.AuthService;

public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
        Logger.getLogger(LoginFrame.class.getName());

    private final AuthService authService = new AuthService();
    private final Consumer<User> onLoginSuccess;

    private final JTextField usernameField = new JTextField(22);
    private final JPasswordField passwordField =
        new JPasswordField(22);

    private final JButton loginButton = new JButton("Đăng nhập");
    private final JButton registerButton =
        new JButton("Tạo tài khoản");

    private final JTextArea statusArea = new JTextArea(3, 30);

    private boolean busy;

    public LoginFrame(Consumer<User> onLoginSuccess) {
        this.onLoginSuccess = java.util.Objects.requireNonNull(
            onLoginSuccess
        );

        setTitle("Đăng nhập — Quản lý phim");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(580, 370);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBorder(
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        );
        setContentPane(content);

        JLabel title = new JLabel(
            "ĐĂNG NHẬP",
            SwingConstants.CENTER
        );
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 5, 8, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = 0;
        constraints.gridy = 0;
        form.add(new JLabel("Tên đăng nhập:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(usernameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;
        form.add(new JLabel("Mật khẩu:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(passwordField, constraints);

        JPanel buttons = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        buttons.add(registerButton);
        buttons.add(loginButton);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        form.add(buttons, constraints);

        content.add(form, BorderLayout.CENTER);

        statusArea.setEditable(false);
        statusArea.setOpaque(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusArea.setText("Nhập thông tin tài khoản để tiếp tục.");

        content.add(statusArea, BorderLayout.SOUTH);

        loginButton.addActionListener(event -> login());
        registerButton.addActionListener(event -> openRegistration());

        getRootPane().setDefaultButton(loginButton);
    }

    private void openRegistration() {
        if (busy) {
            return;
        }

        passwordField.setText("");

        RegisterDialog dialog = new RegisterDialog(this);
        dialog.setVisible(true);

        User registeredUser = dialog.getRegisteredUser();

        if (registeredUser != null) {
            usernameField.setText(registeredUser.getUsername());
            statusArea.setText(
                "Đăng ký thành công. Nhập mật khẩu để đăng nhập."
            );
            passwordField.requestFocusInWindow();
        }
    }

    private void setBusy(boolean value) {
        busy = value;

        usernameField.setEnabled(!value);
        passwordField.setEnabled(!value);
        loginButton.setEnabled(!value);
        registerButton.setEnabled(!value);

        loginButton.setText(
            value ? "Đang đăng nhập..." : "Đăng nhập"
        );
    }

    private void login() {
        if (busy) {
            return;
        }

        String username = usernameField.getText();
        char[] password = passwordField.getPassword();

        if (username.isBlank() || password.length == 0) {
            Arrays.fill(password, '\0');
            statusArea.setText(
                "Vui lòng nhập tên đăng nhập và mật khẩu."
            );
            return;
        }

        setBusy(true);
        statusArea.setText("Đang kiểm tra tài khoản...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {

            @Override
            protected User doInBackground() throws Exception {
                try {
                    return authService.login(username, password);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                User authenticatedUser = null;

                try {
                    authenticatedUser = get();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    statusArea.setText("Đăng nhập bị gián đoạn.");

                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();

                    if (cause instanceof IllegalArgumentException) {
                        statusArea.setText(
                            "Sai thông tin đăng nhập "
                                + "hoặc tài khoản bị khóa."
                        );
                    } else {
                        statusArea.setText(
                            "Không thể đăng nhập. "
                                + "Kiểm tra kết nối CSDL."
                        );

                        LOGGER.log(
                            Level.SEVERE,
                            "Login failed",
                            cause == null ? e : cause
                        );
                    }

                } finally {
                    passwordField.setText("");
                    setBusy(false);
                }

                if (!LoginFrame.this.isDisplayable()) {
                    return;
                }

                if (authenticatedUser != null) {
                    onLoginSuccess.accept(authenticatedUser);
                    dispose();
                } else {
                    passwordField.requestFocusInWindow();
                }
            }
        };

        worker.execute();
    }
}