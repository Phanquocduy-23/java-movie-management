package vn.edu.nct.quanlyphim.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import vn.edu.nct.quanlyphim.model.User;
import vn.edu.nct.quanlyphim.service.AuthService;

public class RegisterDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
        Logger.getLogger(RegisterDialog.class.getName());

    private final AuthService authService = new AuthService();

    private final JTextField usernameField = new JTextField(22);
    private final JTextField fullNameField = new JTextField(22);
    private final JPasswordField passwordField =
        new JPasswordField(22);
    private final JPasswordField confirmField =
        new JPasswordField(22);

    private final JButton registerButton = new JButton("Đăng ký");
    private final JButton closeButton = new JButton("Đóng");
    private final JTextArea statusArea = new JTextArea(3, 30);

    private boolean busy;
    private User registeredUser;

    public RegisterDialog(Frame owner) {
        super(owner, "Đăng ký tài khoản", true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(570, 440);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBorder(
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        );
        setContentPane(content);

        JLabel title = new JLabel(
            "ĐĂNG KÝ TÀI KHOẢN",
            SwingConstants.CENTER
        );
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());

        addField(form, 0, "Tên đăng nhập:", usernameField);
        addField(form, 1, "Họ tên:", fullNameField);
        addField(form, 2, "Mật khẩu:", passwordField);
        addField(form, 3, "Xác nhận mật khẩu:", confirmField);

        content.add(form, BorderLayout.CENTER);

        statusArea.setEditable(false);
        statusArea.setOpaque(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusArea.setText(
            "Tên đăng nhập: 3–50 ký tự, gồm chữ không dấu, số và _. "
                + "Mật khẩu: 8–128 ký tự."
        );

        JPanel buttons = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );
        buttons.add(closeButton);
        buttons.add(registerButton);

        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.add(statusArea, BorderLayout.CENTER);
        footer.add(buttons, BorderLayout.SOUTH);

        content.add(footer, BorderLayout.SOUTH);

        registerButton.addActionListener(event -> register());
        closeButton.addActionListener(event -> dispose());

        getRootPane().setDefaultButton(registerButton);
        setLocationRelativeTo(owner);
    }

    public User getRegisteredUser() {
        return registeredUser;
    }

    private void addField(
            JPanel form,
            int row,
            String label,
            JTextField field) {

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 5, 8, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = row;

        constraints.gridx = 0;
        constraints.weightx = 0;
        form.add(new JLabel(label), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(field, constraints);
    }

    private void setBusy(boolean value) {
        busy = value;

        usernameField.setEnabled(!value);
        fullNameField.setEnabled(!value);
        passwordField.setEnabled(!value);
        confirmField.setEnabled(!value);
        registerButton.setEnabled(!value);
        closeButton.setEnabled(!value);

        registerButton.setText(
            value ? "Đang tạo tài khoản..." : "Đăng ký"
        );

        // Không đóng giữa lúc đang lưu tài khoản.
        setDefaultCloseOperation(
            value
                ? JDialog.DO_NOTHING_ON_CLOSE
                : JDialog.DISPOSE_ON_CLOSE
        );
    }

    private void register() {
        if (busy) {
            return;
        }

        String username = usernameField.getText();
        String fullName = fullNameField.getText();
        char[] password = passwordField.getPassword();
        char[] confirmation = confirmField.getPassword();

        setBusy(true);
        statusArea.setText("Đang kiểm tra và tạo tài khoản...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {

            @Override
            protected User doInBackground() throws Exception {
                try {
                    return authService.register(
                        username,
                        fullName,
                        password,
                        confirmation
                    );
                } finally {
                    Arrays.fill(password, '\0');
                    Arrays.fill(confirmation, '\0');
                }
            }

            @Override
            protected void done() {
                try {
                    registeredUser = get();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    statusArea.setText("Đăng ký bị gián đoạn.");

                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();

                    if (cause instanceof IllegalArgumentException) {
                        statusArea.setText(cause.getMessage());
                    } else {
                        statusArea.setText(
                            "Không thể hoàn tất đăng ký. "
                                + "Kiểm tra kết nối CSDL."
                        );

                        LOGGER.log(
                            Level.SEVERE,
                            "Registration failed",
                            cause == null ? e : cause
                        );
                    }

                } finally {
                    passwordField.setText("");
                    confirmField.setText("");
                    setBusy(false);
                }

                if (registeredUser != null) {
                    dispose();
                } else {
                    passwordField.requestFocusInWindow();
                }
            }
        };

        worker.execute();
    }
}