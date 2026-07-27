package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.NguoiDungDAO;
import com.phonemanager.model.NguoiDung;
import com.phonemanager.util.UiTaskRunner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// ============================================================
// LoginPanel.java — Màn hình đăng nhập, phần biểu mẫu căn giữa
// ============================================================
public class LoginPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int FORM_WIDTH = 300;

    private final MainFrame mainFrame;
    private final NguoiDungDAO userDAO = new NguoiDungDAO();

    private JComboBox<String> roleBox;
    private JTextField userField;
    private JPasswordField passField;
    private JLabel errorLabel;
    private JButton loginButton;
    private JButton registerButton;
    private JProgressBar loginProgress;
    private boolean loginInProgress;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        add(buildCard(), gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, AppConfig.BG, getWidth(), getHeight(), new Color(0x0D1025)));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
        g2.setColor(AppConfig.ACCENT);
        g2.fillOval(-150, -150, 550, 550);
        g2.setColor(AppConfig.ACCENT2);
        g2.fillOval(getWidth() - 350, getHeight() - 350, 550, 550);
        g2.dispose();
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setPaint(new GradientPaint(0, 0, AppConfig.ACCENT, getWidth(), 0, AppConfig.ACCENT2));
                g2.fillRoundRect(0, 0, getWidth(), 5, 5, 5);
                g2.setColor(AppConfig.BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(400, 520));
        card.setMaximumSize(new Dimension(400, 520));
        card.setMinimumSize(new Dimension(400, 520));
        card.setBorder(BorderFactory.createEmptyBorder(36, 44, 36, 44));

        JLabel title = lbl("Quản lý điện thoại", new Font("Segoe UI", Font.BOLD, 20), AppConfig.TEXT, SwingConstants.CENTER);
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = lbl("Đăng nhập để tiếp tục", AppConfig.SMALL, AppConfig.MUTED, SwingConstants.CENTER);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(AppConfig.BORDER);
        sep.setMaximumSize(new Dimension(9999, 1));

        roleBox = UIHelper.combo("Quản trị viên", "Nhân viên");
        userField = UIHelper.field();
        passField = UIHelper.passField();
        styleCenteredFormInput(roleBox);
        styleCenteredFormInput(userField);
        styleCenteredFormInput(passField);
        userField.setHorizontalAlignment(SwingConstants.CENTER);
        passField.setHorizontalAlignment(SwingConstants.CENTER);
        centerComboText(roleBox);

        errorLabel = lbl("", AppConfig.SMALL, AppConfig.DANGER, SwingConstants.CENTER);
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);
        errorLabel.setMaximumSize(new Dimension(FORM_WIDTH, 16));

        loginButton = UIHelper.gradBtn("Đăng nhập", AppConfig.ACCENT, AppConfig.ACCENT2);
        loginButton.setAlignmentX(CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(FORM_WIDTH, 46));
        loginButton.setPreferredSize(new Dimension(FORM_WIDTH, 46));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 13));

        loginProgress = new JProgressBar();
        loginProgress.setIndeterminate(false);
        loginProgress.setBorderPainted(false);
        loginProgress.setForeground(AppConfig.ACCENT);
        loginProgress.setBackground(AppConfig.SURFACE);
        loginProgress.setAlignmentX(CENTER_ALIGNMENT);
        loginProgress.setMaximumSize(new Dimension(FORM_WIDTH, 4));
        loginProgress.setPreferredSize(new Dimension(FORM_WIDTH, 4));

        JLabel registerHint = lbl("Chưa có tài khoản?", AppConfig.SMALL, AppConfig.MUTED, SwingConstants.CENTER);
        registerHint.setAlignmentX(CENTER_ALIGNMENT);
        registerButton = UIHelper.gradBtn("Tạo tài khoản mới", new Color(0x3F4675), new Color(0x59619B));
        registerButton.setAlignmentX(CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(FORM_WIDTH, 40));
        registerButton.setPreferredSize(new Dimension(FORM_WIDTH, 40));

        ActionListener login = e -> handleLogin();
        loginButton.addActionListener(login);
        registerButton.addActionListener(e -> openRegisterDialog());
        passField.addActionListener(login);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(16));
        card.add(sep);
        card.add(Box.createVerticalStrut(20));

        addCenteredRow(card, "Vai trò đăng nhập", roleBox);
        card.add(Box.createVerticalStrut(12));
        addCenteredRow(card, "Tên đăng nhập", userField);
        card.add(Box.createVerticalStrut(12));
        addCenteredRow(card, "Mật khẩu", passField);

        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(loginProgress);
        card.add(Box.createVerticalStrut(8));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(16));
        card.add(registerHint);
        card.add(Box.createVerticalStrut(6));
        card.add(registerButton);
        return card;
    }

    private void addCenteredRow(JPanel card, String labelText, JComponent input) {
        JLabel label = lbl(labelText, AppConfig.SMALL, AppConfig.MUTED, SwingConstants.CENTER);
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setPreferredSize(new Dimension(FORM_WIDTH, 16));
        label.setMaximumSize(new Dimension(FORM_WIDTH, 16));

        input.setAlignmentX(CENTER_ALIGNMENT);
        card.add(label);
        card.add(Box.createVerticalStrut(4));
        card.add(input);
    }

    private void styleCenteredFormInput(JComponent input) {
        input.setAlignmentX(CENTER_ALIGNMENT);
        input.setPreferredSize(new Dimension(FORM_WIDTH, 38));
        input.setMaximumSize(new Dimension(FORM_WIDTH, 38));
        input.setMinimumSize(new Dimension(FORM_WIDTH, 38));
    }

    private void centerComboText(JComboBox<String> comboBox) {
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return label;
            }
        });
    }

    private JLabel lbl(String text, Font font, Color color, int alignment) {
        JLabel label = new JLabel(text, alignment);
        label.setFont(font);
        label.setForeground(color);
        label.setOpaque(false);
        return label;
    }

    private void handleLogin() {
        if (loginInProgress) return;

        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        String role = roleBox.getSelectedIndex() == 0 ? "admin" : "nhanvien";

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        setLoginInProgress(true);
        UiTaskRunner.run(
            () -> userDAO.login(username, password, role),
            user -> {
                setLoginInProgress(false);
                if (user != null) {
                    errorLabel.setText("");
                    mainFrame.showMain(user);
                } else {
                    showError("Thông tin đăng nhập hoặc vai trò không chính xác.");
                }
            },
            error -> {
                setLoginInProgress(false);
                showError("Không thể kết nối cơ sở dữ liệu: " + error.getMessage());
            }
        );
    }

    private void setLoginInProgress(boolean inProgress) {
        loginInProgress = inProgress;
        roleBox.setEnabled(!inProgress);
        userField.setEnabled(!inProgress);
        passField.setEnabled(!inProgress);
        loginButton.setEnabled(!inProgress);
        registerButton.setEnabled(!inProgress);
        loginButton.setText(inProgress ? "Đang xác thực..." : "Đăng nhập");
        loginProgress.setIndeterminate(inProgress);
        if (!inProgress) loginProgress.setValue(0);
        errorLabel.setForeground(inProgress ? AppConfig.MUTED : AppConfig.DANGER);
        errorLabel.setText(inProgress ? "Đang kết nối đến hệ thống" : "");
    }

    private void openRegisterDialog() {
        new RegisterAccountDialog(mainFrame, user -> {
            userField.setText(user.getUsername());
            passField.setText("");
            roleBox.setSelectedIndex("admin".equals(user.getVaiTro()) ? 0 : 1);
            errorLabel.setForeground(AppConfig.SUCCESS);
            errorLabel.setText("Đã tạo tài khoản. Nhập mật khẩu để đăng nhập.");
            passField.requestFocusInWindow();
        }).setVisible(true);
    }

    private void showError(String message) {
        errorLabel.setForeground(AppConfig.DANGER);
        errorLabel.setText(message);
    }
}
