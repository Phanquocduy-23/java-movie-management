package vn.edu.nct.quanlyphim.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import vn.edu.nct.quanlyphim.dao.MovieDAO;
import vn.edu.nct.quanlyphim.model.Movie;

public class MovieListFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String ALL = "Tất cả";

    private final MovieDAO movieDAO = new MovieDAO();
    private final DefaultTableModel tableModel;
    private final JTable movieTable;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final JTextField searchField;
    private final JComboBox<String> genreCombo;
    private final JComboBox<String> yearCombo;
    private final JLabel statusLabel;
    private final JButton reloadButton;
    private final JButton detailButton;

    private boolean loading;
    private boolean loadFailed;
    private boolean updatingFilters;
    private boolean loadingDetail;
    private final vn.edu.nct.quanlyphim.model.User currentUser;

        public MovieListFrame(
            vn.edu.nct.quanlyphim.model.User currentUser,
            Runnable onLogout) {

        this.currentUser = java.util.Objects.requireNonNull(currentUser);
        java.util.Objects.requireNonNull(onLogout);

        if (!currentUser.isActive()) {
            throw new IllegalArgumentException(
                "Tài khoản đã bị khóa."
            );
        }

        setTitle(
            "Quản lý phim — "
                + this.currentUser.getFullName()
                + " — Quyền: "
                + this.currentUser.getRole()
        );
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 650);
        setMinimumSize(new Dimension(1000, 450));
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );
        setContentPane(content);

        JLabel titleLabel = new JLabel("DANH SÁCH PHIM");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));

        detailButton = new JButton("Xem chi tiết");
        detailButton.setEnabled(false);
        detailButton.addActionListener(event -> showSelectedMovie());

        reloadButton = new JButton("Tải lại");
        reloadButton.addActionListener(event -> loadMovies());

        JPanel actions = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        actions.add(detailButton);
        actions.add(reloadButton);
        JButton logoutButton = new JButton("Đăng xuất");

        logoutButton.addActionListener(event -> {
            int answer = JOptionPane.showConfirmDialog(
                MovieListFrame.this,
                "Bạn muốn đăng xuất khỏi tài khoản hiện tại?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (answer == JOptionPane.YES_OPTION) {
                dispose();
                onLogout.run();
            }
        });

        actions.add(logoutButton);
                if (this.currentUser.isAdmin()) {
            JButton manageGenresButton =
                new JButton("Quản lý thể loại");

            manageGenresButton.addActionListener(event -> {
                if (loading || loadingDetail) {
                    JOptionPane.showMessageDialog(
                        MovieListFrame.this,
                        "Vui lòng đợi tải dữ liệu hoàn tất."
                    );
                    return;
                }

                try {
                    GenreManagementDialog dialog =
                        new GenreManagementDialog(
                            MovieListFrame.this,
                            this.currentUser
                        );

                    dialog.setVisible(true);

                    // Cập nhật tên thể loại trên bảng phim sau khi đóng.
                    loadMovies();

                } catch (SecurityException e) {
                    JOptionPane.showMessageDialog(
                        MovieListFrame.this,
                        e.getMessage(),
                        "Không có quyền",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            });

            actions.add(manageGenresButton);
        }

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.add(titleLabel, BorderLayout.WEST);
        titleRow.add(actions, BorderLayout.EAST);

        searchField = new JTextField(18);

        genreCombo = new JComboBox<>(new String[] {ALL});
        genreCombo.setPreferredSize(new Dimension(190, 30));

        yearCombo = new JComboBox<>(new String[] {ALL});
        yearCombo.setPreferredSize(new Dimension(95, 30));

        JButton clearButton = new JButton("Xóa bộ lọc");
        clearButton.addActionListener(event -> clearFilters());

        JPanel filterRow = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 8, 0)
        );
        filterRow.add(new JLabel("Tên phim:"));
        filterRow.add(searchField);
        filterRow.add(new JLabel("Thể loại:"));
        filterRow.add(genreCombo);
        filterRow.add(new JLabel("Năm:"));
        filterRow.add(yearCombo);
        filterRow.add(clearButton);

        JPanel header = new JPanel(new BorderLayout(0, 16));
        header.add(titleRow, BorderLayout.NORTH);
        header.add(filterRow, BorderLayout.SOUTH);
        content.add(header, BorderLayout.NORTH);

        String[] columns = {
            "Mã phim", "Tên phim", "Năm phát hành",
            "Thời lượng (phút)", "Thể loại"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Long.class;
                    case 2, 3 -> Integer.class;
                    default -> String.class;
                };
            }
        };

        movieTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        movieTable.setRowSorter(sorter);
        movieTable.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
        movieTable.setRowHeight(32);
        movieTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        movieTable.getTableHeader().setFont(
            new Font("SansSerif", Font.BOLD, 14)
        );
        movieTable.setFillsViewportHeight(true);

        int[] widths = {70, 230, 120, 140, 440};
        for (int i = 0; i < widths.length; i++) {
            movieTable.getColumnModel().getColumn(i)
                .setPreferredWidth(widths[i]);
        }

        movieTable.getSelectionModel().addListSelectionListener(
            event -> {
                if (!event.getValueIsAdjusting()) {
                    updateDetailButton();
                }
            }
        );

        movieTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1
                        && event.getClickCount() == 2) {
                    int row = movieTable.rowAtPoint(event.getPoint());

                    if (row >= 0) {
                        movieTable.setRowSelectionInterval(row, row);
                        showSelectedMovie();
                    }
                }
            }
        });

        content.add(new JScrollPane(movieTable), BorderLayout.CENTER);

        statusLabel = new JLabel("Chuẩn bị tải dữ liệu...");
        content.add(statusLabel, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(
            new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    applyFilters();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    applyFilters();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    applyFilters();
                }
            }
        );

        genreCombo.addActionListener(event -> applyFilters());
        yearCombo.addActionListener(event -> applyFilters());

        loadMovies();
    }

    private void updateDetailButton() {
        detailButton.setEnabled(
            !loading
            && !loadingDetail
            && movieTable.getSelectedRow() >= 0
        );
    }

    private void showSelectedMovie() {
        if (loading || loadingDetail) {
            return;
        }

        int viewRow = movieTable.getSelectedRow();

        if (viewRow < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Mời chọn một phim trong bảng.",
                "Chưa chọn phim",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Đổi vị trí dòng đang hiển thị sang dòng dữ liệu gốc.
        // Cần thiết khi bảng đã được lọc hoặc sắp xếp.
        int modelRow = movieTable.convertRowIndexToModel(viewRow);
        long movieId = ((Number) tableModel.getValueAt(
            modelRow, 0
        )).longValue();

        loadingDetail = true;
        detailButton.setText("Đang tải...");
        updateDetailButton();

        SwingWorker<Optional<Movie>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Optional<Movie> doInBackground()
                    throws Exception {
                return movieDAO.findActiveById(movieId);
            }

            @Override
            protected void done() {
                try {
                    Optional<Movie> result = get();

                    if (!MovieListFrame.this.isDisplayable()) {
                        return;
                    }

                    if (result.isPresent()) {
                        MovieDetailDialog dialog = new MovieDetailDialog(
                            MovieListFrame.this,
                            result.get()
                        );
                        detailButton.setText("Xem chi tiết");
                        dialog.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(
                            MovieListFrame.this,
                            "Phim không còn tồn tại hoặc đã ngừng hiển thị.\n"
                                + "Bấm Tải lại để cập nhật danh sách.",
                            "Không tìm thấy phim",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException e) {
                    showLoadError("Không tải được chi tiết phim.", e);

                } finally {
                    loadingDetail = false;
                    detailButton.setText("Xem chi tiết");
                    updateDetailButton();
                }
            }
        };

        worker.execute();
    }

    private void clearFilters() {
        updatingFilters = true;

        try {
            searchField.setText("");
            genreCombo.setSelectedItem(ALL);
            yearCombo.setSelectedItem(ALL);
        } finally {
            updatingFilters = false;
        }

        applyFilters();
        searchField.requestFocusInWindow();
    }

    private void applyFilters() {
        if (updatingFilters) {
            return;
        }

        String keyword = searchField.getText().trim();
        String selectedGenre = (String) genreCombo.getSelectedItem();
        String selectedYear = (String) yearCombo.getSelectedItem();

        List<RowFilter<DefaultTableModel, Integer>> filters =
            new ArrayList<>();

        if (!keyword.isEmpty()) {
            filters.add(
                RowFilter.regexFilter(
                    "(?iu)" + Pattern.quote(keyword), 1
                )
            );
        }

        if (selectedGenre != null && !ALL.equals(selectedGenre)) {
            filters.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(
                        Entry<? extends DefaultTableModel,
                        ? extends Integer> entry) {

                    return Arrays.stream(
                        entry.getStringValue(4).split(", ")
                    ).anyMatch(selectedGenre::equals);
                }
            });
        }

        if (selectedYear != null && !ALL.equals(selectedYear)) {
            filters.add(
                RowFilter.regexFilter(
                    "^" + Pattern.quote(selectedYear) + "$", 2
                )
            );
        }

        sorter.setRowFilter(
            filters.isEmpty() ? null : RowFilter.andFilter(filters)
        );

        updateStatus();
        updateDetailButton();
    }

    private void refreshFilterOptions(List<Movie> movies) {
        String previousGenre = (String) genreCombo.getSelectedItem();
        String previousYear = (String) yearCombo.getSelectedItem();

        Set<String> genres = new TreeSet<>();
        Set<Integer> years = new TreeSet<>(Collections.reverseOrder());

        for (Movie movie : movies) {
            String movieGenres = movie.getGenres();

            if (movieGenres != null
                    && !"Chưa phân loại".equals(movieGenres)) {
                genres.addAll(Arrays.asList(movieGenres.split(", ")));
            }

            if (movie.getReleaseYear() != null) {
                years.add(movie.getReleaseYear());
            }
        }

        updatingFilters = true;

        try {
            genreCombo.removeAllItems();
            genreCombo.addItem(ALL);
            for (String genre : genres) {
                genreCombo.addItem(genre);
            }

            yearCombo.removeAllItems();
            yearCombo.addItem(ALL);
            for (Integer year : years) {
                yearCombo.addItem(year.toString());
            }

            if (genres.contains(previousGenre)) {
                genreCombo.setSelectedItem(previousGenre);
            }

            if (previousYear != null && years.stream()
                    .anyMatch(year -> year.toString().equals(previousYear))) {
                yearCombo.setSelectedItem(previousYear);
            }
        } finally {
            updatingFilters = false;
        }
    }

    private void updateStatus() {
        if (loading) {
            statusLabel.setText("Đang tải dữ liệu từ PostgreSQL...");
            return;
        }

        String counts = "Hiển thị: " + movieTable.getRowCount()
            + " / Tổng số phim đã tải: " + tableModel.getRowCount();

        statusLabel.setText(
            loadFailed
                ? "Tải thất bại — dữ liệu chưa được cập nhật. " + counts
                : counts
        );
    }

    private void showLoadError(String title, ExecutionException error) {
        Throwable cause = error.getCause();

        JOptionPane.showMessageDialog(
            this,
            title + "\n"
                + (cause == null ? error.toString() : cause.toString()),
            "Lỗi tải dữ liệu",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private void loadMovies() {
        loading = true;
        reloadButton.setEnabled(false);
        updateDetailButton();
        updateStatus();

        SwingWorker<List<Movie>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Movie> doInBackground() throws Exception {
                return movieDAO.findAllActive();
            }

            @Override
            protected void done() {
                try {
                    List<Movie> movies = get();
                    tableModel.setRowCount(0);

                    for (Movie movie : movies) {
                        tableModel.addRow(new Object[] {
                            movie.getMovieId(),
                            movie.getTitle(),
                            movie.getReleaseYear(),
                            movie.getDurationMinutes(),
                            movie.getGenres()
                        });
                    }

                    refreshFilterOptions(movies);
                    loadFailed = false;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    loadFailed = true;

                } catch (ExecutionException e) {
                    loadFailed = true;
                    showLoadError("Không tải được danh sách phim.", e);

                } finally {
                    loading = false;
                    reloadButton.setEnabled(true);
                    applyFilters();
                }
            }
        };

        worker.execute();
    }
}