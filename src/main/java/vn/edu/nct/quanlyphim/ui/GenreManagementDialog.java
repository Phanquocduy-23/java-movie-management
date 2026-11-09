package vn.edu.nct.quanlyphim.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import vn.edu.nct.quanlyphim.model.Genre;
import vn.edu.nct.quanlyphim.model.User;
import vn.edu.nct.quanlyphim.service.GenreService;

public class GenreManagementDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
        Logger.getLogger(GenreManagementDialog.class.getName());

    private final GenreService genreService;
    private final DefaultTableModel tableModel;
    private final JTable genreTable;

    private final JTextField nameField = new JTextField(25);
    private final JLabel statusLabel = new JLabel("Chuẩn bị tải...");

    private final JButton addButton = new JButton("Thêm");
    private final JButton editButton = new JButton("Sửa");
    private final JButton deleteButton = new JButton("Xóa");
    private final JButton clearButton = new JButton("Bỏ chọn");
    private final JButton reloadButton = new JButton("Tải lại");
    private final JButton closeButton = new JButton("Đóng");

    private boolean busy;
    private boolean accessDenied;

    public GenreManagementDialog(Frame owner, User currentUser) {
        super(owner, "Quản lý thể loại", true);

        if (currentUser == null
                || !currentUser.isActive()
                || !currentUser.isAdmin()) {
            throw new SecurityException(
                "Chỉ quản trị viên được mở quản lý thể loại."
            );
        }

        genreService = new GenreService(currentUser);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(760, 520);
        setMinimumSize(new Dimension(680, 420));
        setLocationRelativeTo(owner);

        JPanel content = new JPanel(new BorderLayout(10, 12));
        content.setBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );
        setContentPane(content);

        JPanel inputRow = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );
        inputRow.add(new JLabel("Tên thể loại:"));
        inputRow.add(nameField);
        inputRow.add(clearButton);

        JPanel actionRow = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );
        actionRow.add(addButton);
        actionRow.add(editButton);
        actionRow.add(deleteButton);
        actionRow.add(reloadButton);

        JPanel header = new JPanel(new BorderLayout());
        header.add(inputRow, BorderLayout.NORTH);
        header.add(actionRow, BorderLayout.SOUTH);

        content.add(header, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[] {"Mã thể loại", "Tên thể loại"},
            0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Long.class : String.class;
            }
        };

        genreTable = new JTable(tableModel);
        genreTable.setRowHeight(30);
        genreTable.setFillsViewportHeight(true);
        genreTable.setAutoCreateRowSorter(true);
        genreTable.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        genreTable.getColumnModel().getColumn(0)
            .setPreferredWidth(120);
        genreTable.getColumnModel().getColumn(1)
            .setPreferredWidth(500);

        genreTable.getSelectionModel().addListSelectionListener(
            event -> {
                if (!event.getValueIsAdjusting()) {
                    int viewRow = genreTable.getSelectedRow();

                    if (viewRow >= 0) {
                        int modelRow =
                            genreTable.convertRowIndexToModel(viewRow);

                        nameField.setText(
                            tableModel.getValueAt(modelRow, 1).toString()
                        );
                    }

                    updateControls();
                }
            }
        );

        content.add(
            new JScrollPane(genreTable),
            BorderLayout.CENTER
        );

        JPanel footer = new JPanel(new BorderLayout());
        footer.add(statusLabel, BorderLayout.CENTER);
        footer.add(closeButton, BorderLayout.EAST);

        content.add(footer, BorderLayout.SOUTH);

        addButton.addActionListener(event -> addGenre());
        editButton.addActionListener(event -> editGenre());
        deleteButton.addActionListener(event -> deleteGenre());

        clearButton.addActionListener(event -> {
            clearSelection();
            nameField.requestFocusInWindow();
        });

        reloadButton.addActionListener(
            event -> runTask(() -> {}, "Đã tải lại danh sách.")
        );

        closeButton.addActionListener(event -> dispose());

        runTask(() -> {}, "Đã tải danh sách thể loại.");
    }

    private void updateControls() {
        boolean allowed = !busy && !accessDenied;
        boolean selected = genreTable.getSelectedRow() >= 0;

        nameField.setEnabled(allowed);
        genreTable.setEnabled(allowed);
        addButton.setEnabled(allowed);
        editButton.setEnabled(allowed && selected);
        deleteButton.setEnabled(allowed && selected);
        clearButton.setEnabled(allowed);
        reloadButton.setEnabled(allowed);
        closeButton.setEnabled(!busy);

        setDefaultCloseOperation(
            busy
                ? JDialog.DO_NOTHING_ON_CLOSE
                : JDialog.DISPOSE_ON_CLOSE
        );
    }

    private void clearSelection() {
        genreTable.clearSelection();
        nameField.setText("");
        updateControls();
    }

    private Long getSelectedId() {
        int viewRow = genreTable.getSelectedRow();

        if (viewRow < 0) {
            return null;
        }

        int modelRow = genreTable.convertRowIndexToModel(viewRow);

        return ((Number) tableModel.getValueAt(
            modelRow, 0
        )).longValue();
    }

    private void addGenre() {
        if (busy || accessDenied) {
            return;
        }

        String name = nameField.getText();

        runTask(
            () -> genreService.create(name),
            "Đã thêm thể loại."
        );
    }

    private void editGenre() {
        if (busy || accessDenied) {
            return;
        }

        Long genreId = getSelectedId();

        if (genreId == null) {
            return;
        }

        String name = nameField.getText();

        runTask(
            () -> genreService.update(genreId, name),
            "Đã sửa thể loại."
        );
    }

    private void deleteGenre() {
        if (busy || accessDenied) {
            return;
        }

        Long genreId = getSelectedId();

        if (genreId == null) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
            this,
            "Bạn muốn xóa thể loại đang chọn?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        runTask(
            () -> genreService.delete(genreId),
            "Đã xóa thể loại."
        );
    }

    private void runTask(CheckedAction action, String successMessage) {
        if (busy || accessDenied) {
            return;
        }

        busy = true;
        statusLabel.setText("Đang xử lý...");
        updateControls();

        SwingWorker<List<Genre>, Void> worker = new SwingWorker<>() {

            private boolean actionCompleted;

            @Override
            protected List<Genre> doInBackground() throws Exception {
                action.run();
                actionCompleted = true;
                return genreService.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Genre> genres = get();

                    tableModel.setRowCount(0);

                    for (Genre genre : genres) {
                        tableModel.addRow(new Object[] {
                            genre.getGenreId(),
                            genre.getName()
                        });
                    }

                    clearSelection();

                    statusLabel.setText(
                        successMessage + " Tổng: " + genres.size()
                    );

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Xử lý bị gián đoạn.");

                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();

                    if (cause instanceof SecurityException) {
                        accessDenied = true;
                        tableModel.setRowCount(0);
                        nameField.setText("");
                        statusLabel.setText("Không còn quyền truy cập.");

                        showMessage(
                            cause.getMessage(),
                            JOptionPane.WARNING_MESSAGE
                        );

                    } else if (cause instanceof IllegalArgumentException) {
                        statusLabel.setText("Thao tác chưa hoàn tất.");

                        showMessage(
                            cause.getMessage(),
                            JOptionPane.WARNING_MESSAGE
                        );

                    } else {
                        String message = actionCompleted
                            ? "Thao tác đã hoàn tất nhưng không tải lại "
                                + "được danh sách. Bấm Tải lại để kiểm tra "
                                + "trước khi thực hiện lại thao tác."
                            : "Không xác nhận được kết quả thao tác. "
                                + "Kiểm tra kết nối và bấm Tải lại "
                                + "trước khi thử lại.";

                        statusLabel.setText("Không cập nhật được danh sách.");
                        showMessage(message, JOptionPane.ERROR_MESSAGE);

                        LOGGER.log(
                            Level.SEVERE,
                            "Genre operation failed",
                            cause == null ? e : cause
                        );
                    }

                } finally {
                    busy = false;
                    updateControls();
                }
            }
        };

        worker.execute();
    }

    private void showMessage(String message, int messageType) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Quản lý thể loại",
            messageType
        );
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }
}