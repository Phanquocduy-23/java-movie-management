package vn.edu.nct.quanlyphim.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import vn.edu.nct.quanlyphim.model.Movie;

public class MovieDetailDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public MovieDetailDialog(Frame owner, Movie movie) {
        super(owner, "Chi tiết phim", true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(720, 500);
        setMinimumSize(new Dimension(550, 400));

        JPanel content = new JPanel(new BorderLayout(12, 16));
        content.setBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );
        setContentPane(content);

        JTextArea titleArea = new JTextArea(movie.getTitle());
        titleArea.setEditable(false);
        titleArea.setLineWrap(true);
        titleArea.setWrapStyleWord(true);
        titleArea.setOpaque(false);
        titleArea.setFont(
            new Font("SansSerif", Font.BOLD, 24)
        );

        content.add(titleArea, BorderLayout.NORTH);

        JPanel information = new JPanel(new GridLayout(0, 1, 0, 10));

        information.add(createLabel(
            "Mã phim: " + movie.getMovieId()
        ));

        information.add(createLabel(
            "Năm phát hành: "
                + displayNumber(movie.getReleaseYear())
        ));

        String duration = movie.getDurationMinutes() == null
            ? "Chưa cập nhật"
            : movie.getDurationMinutes() + " phút";

        information.add(createLabel("Thời lượng: " + duration));

        JTextArea genreArea = new JTextArea(
            "Thể loại: " + displayText(movie.getGenres())
        );
        genreArea.setEditable(false);
        genreArea.setLineWrap(true);
        genreArea.setWrapStyleWord(true);
        genreArea.setOpaque(false);
        genreArea.setFont(
            new Font("SansSerif", Font.PLAIN, 14)
        );
        information.add(genreArea);

        JTextArea descriptionArea = new JTextArea(
            displayText(movie.getDescription())
        );
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(
            new Font("SansSerif", Font.PLAIN, 15)
        );
        descriptionArea.setMargin(
            new java.awt.Insets(10, 10, 10, 10)
        );
        descriptionArea.setCaretPosition(0);

        JScrollPane descriptionScroll =
            new JScrollPane(descriptionArea);

        descriptionScroll.setBorder(
            BorderFactory.createTitledBorder("Nội dung phim")
        );

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.add(information, BorderLayout.NORTH);
        body.add(descriptionScroll, BorderLayout.CENTER);

        content.add(body, BorderLayout.CENTER);

        JButton closeButton = new JButton("Đóng");
        closeButton.addActionListener(event -> dispose());

        JPanel footer = new JPanel(
            new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT)
        );
        footer.add(closeButton);

        content.add(footer, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(closeButton);

        getRootPane().registerKeyboardAction(
            event -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setLocationRelativeTo(owner);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel();
        label.setText("<html>" + escapeHtml(text) + "</html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return label;
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    private String displayNumber(Integer value) {
        return value == null ? "Chưa cập nhật" : value.toString();
    }

    private String displayText(String value) {
        return value == null || value.isBlank()
            ? "Chưa cập nhật"
            : value;
    }
}