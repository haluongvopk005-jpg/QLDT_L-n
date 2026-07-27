package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.NguoiDungDAO;
import com.phonemanager.model.NguoiDung;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

// ============================================================
// RegisterAccountDialog.java — Tạo tài khoản từ màn đăng nhập
// ============================================================
public class RegisterAccountDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private static final int DIALOG_WIDTH = 500;
    private static final int DIALOG_HEIGHT = 600;
    private static final int INPUT_HEIGHT = 34;

    private final NguoiDungDAO dao = new NguoiDungDAO();
    private final Consumer<NguoiDung> onAccountCreated;

    private JTextField fUser;
    private JTextField fTen;
    private JTextField fSdt;
    private JTextField fEmail;
    private JPasswordField fPass;
    private JPasswordField fPass2;
    private JComboBox<String> fVaiTro;

    public RegisterAccountDialog(JFrame owner, Consumer<NguoiDung> onAccountCreated) {
        super(owner, "Tạo tài khoản mới", true);
        this.onAccountCreated = onAccountCreated;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(AppConfig.CARD);
        setLayout(new BorderLayout());

        buildUI();

        // pack() lấy đúng chiều cao cần dùng cho toàn bộ 6 ô nhập liệu,
        // tránh lỗi label đầu tiên bị che khi độ phân giải hoặc DPI khác nhau.
        pack();
        Dimension packed = getSize();
        setSize(Math.max(packed.width, DIALOG_WIDTH), Math.max(packed.height, DIALOG_HEIGHT));
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(AppConfig.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppConfig.BORDER),
            BorderFactory.createEmptyBorder(15, 28, 13, 28)
        ));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Tạo tài khoản mới");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(AppConfig.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Tạo tài khoản Nhân viên hoặc Quản trị viên để đăng nhập hệ thống.");
        sub.setFont(AppConfig.SMALL);
        sub.setForeground(AppConfig.MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(3));
        header.add(sub);
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setBackground(AppConfig.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(14, 28, 10, 28));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        fUser = UIHelper.field();
        fPass = UIHelper.passField();
        fPass2 = UIHelper.passField();
        fTen = UIHelper.field();
        fSdt = UIHelper.field();
        fEmail = UIHelper.field();
        fVaiTro = UIHelper.combo("Nhân viên", "Quản trị viên");

        styleDialogControl(fUser);
        styleDialogControl(fPass);
        styleDialogControl(fPass2);
        styleDialogControl(fTen);
        styleDialogControl(fSdt);
        styleDialogControl(fEmail);
        styleDialogControl(fVaiTro);

        fUser.setToolTipText("Chỉ dùng chữ, số, dấu chấm, gạch dưới hoặc gạch ngang");
        fSdt.setToolTipText("Chỉ nhập số, đúng 9 hoặc 10 chữ số");
        fEmail.setToolTipText("Có thể để trống");

        addFormField(form, "Tên đăng nhập *", fUser);
        addFormField(form, "Mật khẩu *", fPass);
        addFormField(form, "Xác nhận mật khẩu *", fPass2);
        addFormField(form, "Họ và tên *", fTen);
        addFormField(form, "Số điện thoại *", fSdt);
        addFormField(form, "Email", fEmail);
        addFormField(form, "Vai trò *", fVaiTro);

        JLabel note = new JLabel("(*) Trường bắt buộc  •  Mật khẩu tối thiểu 6 ký tự");
        note.setFont(AppConfig.SMALL);
        note.setForeground(AppConfig.MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Box.createVerticalStrut(0));
        form.add(note);

        return form;
    }

    private void addFormField(JPanel form, String labelText, JComponent control) {
        JPanel fieldGroup = new JPanel();
        fieldGroup.setOpaque(false);
        fieldGroup.setLayout(new BoxLayout(fieldGroup, BoxLayout.Y_AXIS));
        fieldGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldGroup.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(AppConfig.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        control.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldGroup.add(label);
        fieldGroup.add(Box.createVerticalStrut(3));
        fieldGroup.add(control);

        form.add(fieldGroup);
        form.add(Box.createVerticalStrut(6));
    }

    private void styleDialogControl(JComponent control) {
        control.setPreferredSize(new Dimension(440, INPUT_HEIGHT));
        control.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        control.setMinimumSize(new Dimension(120, INPUT_HEIGHT));
    }

    private JPanel buildFooter() {
        JButton btnCancel = UIHelper.gradBtn("Hủy", new Color(0x444870), new Color(0x333660));
        JButton btnCreate = UIHelper.gradBtn("Tạo tài khoản", AppConfig.ACCENT, AppConfig.ACCENT2);

        btnCancel.addActionListener(e -> dispose());
        btnCreate.addActionListener(e -> createAccount());
        getRootPane().setDefaultButton(btnCreate);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(AppConfig.CARD);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppConfig.BORDER));
        footer.add(btnCancel);
        footer.add(btnCreate);
        return footer;
    }

    private void createAccount() {
        String username = fUser.getText().trim();
        String password = new String(fPass.getPassword());
        String confirmPassword = new String(fPass2.getPassword());
        String fullName = fTen.getText().trim();
        String sdt = fSdt.getText().trim();
        String email = fEmail.getText().trim();
        String role = fVaiTro.getSelectedIndex() == 0 ? "nhanvien" : "admin";

        if (!username.matches("[A-Za-z0-9._-]{3,50}")) {
            warn("Tên đăng nhập có 3–50 ký tự và chỉ dùng chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.");
            return;
        }
        if (password.length() < 6) {
            warn("Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            warn("Mật khẩu xác nhận không khớp!");
            return;
        }
        if (fullName.isEmpty()) {
            warn("Họ và tên không được để trống!");
            return;
        }
        if (!isValidPhone(sdt)) {
            warn("Số điện thoại chỉ được nhập số và phải đúng 9 hoặc 10 chữ số!");
            return;
        }
        if (!email.isEmpty() && (!email.contains("@") || email.startsWith("@") || email.endsWith("@"))) {
            warn("Email chưa đúng định dạng!");
            return;
        }

        try {
            if (dao.existsUsername(username)) {
                warn("Tên đăng nhập \"" + username + "\" đã tồn tại!");
                return;
            }

            NguoiDung account = new NguoiDung();
            account.setUsername(username);
            account.setMatKhau(password);
            account.setHoTen(fullName);
            account.setSdt(sdt);
            account.setEmail(email);
            account.setVaiTro(role);
            account.setTrangThai("Hoat dong");

            if (dao.insert(account)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Tạo tài khoản thành công. Bạn có thể đăng nhập ngay.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
                onAccountCreated.accept(account);
            } else {
                warn("Không thể tạo tài khoản. Vui lòng thử lại!");
            }
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (message != null && message.toLowerCase().contains("unique")) {
                warn("Tên đăng nhập \"" + username + "\" đã tồn tại!");
            } else {
                warn("Lỗi khi tạo tài khoản: " + message);
            }
        }
    }

    private boolean isValidPhone(String sdt) {
        return sdt != null && sdt.matches("\\d{9,10}");
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }
}
