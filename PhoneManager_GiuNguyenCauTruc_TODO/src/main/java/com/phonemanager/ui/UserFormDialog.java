package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.NguoiDungDAO;
import com.phonemanager.model.NguoiDung;

import javax.swing.*;
import java.awt.*;

// ============================================================
// UserFormDialog.java — Dialog Thêm Người Dùng (Admin)
// Bố cục được làm lại để đủ ô nhập, không che nhãn hoặc chồng ô nhập.
// ============================================================
public class UserFormDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private static final int DIALOG_WIDTH = 500;
    private static final int DIALOG_HEIGHT = 600;
    private static final int INPUT_HEIGHT = 34;

    private final NguoiDungDAO dao = new NguoiDungDAO();
    private final Runnable onSaved;

    private JTextField fUser, fTen, fSdt, fEmail;
    private JPasswordField fPass, fPass2;
    private JComboBox<String> fVaiTro;

    public UserFormDialog(JFrame owner, Runnable onSaved) {
        super(owner, "Thêm người dùng mới", true);
        this.onSaved = onSaved;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(AppConfig.CARD);
        setLayout(new BorderLayout());

        buildUI();
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

        JLabel title = new JLabel("Thêm người dùng mới");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(AppConfig.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Tạo tài khoản Nhân viên hoặc Quản trị viên.");
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
        // Mặc định thêm Nhân viên để tránh chọn nhầm quyền Admin.
        fVaiTro = UIHelper.combo("Nhân viên", "Quản trị viên");

        styleControl(fUser);
        styleControl(fPass);
        styleControl(fPass2);
        styleControl(fTen);
        styleControl(fSdt);
        styleControl(fEmail);
        styleControl(fVaiTro);

        fUser.setToolTipText("Chỉ dùng chữ, số, dấu chấm, gạch dưới hoặc gạch ngang");
        fSdt.setToolTipText("Chỉ nhập số, đúng 9 hoặc 10 chữ số");
        fEmail.setToolTipText("Có thể để trống");

        addField(form, "Tên đăng nhập *", fUser);
        addField(form, "Mật khẩu *", fPass);
        addField(form, "Xác nhận mật khẩu *", fPass2);
        addField(form, "Họ và tên *", fTen);
        addField(form, "Số điện thoại *", fSdt);
        addField(form, "Email", fEmail);
        addField(form, "Vai trò *", fVaiTro);

        JLabel note = new JLabel("(*) Trường bắt buộc  •  Mật khẩu tối thiểu 6 ký tự");
        note.setFont(AppConfig.SMALL);
        note.setForeground(AppConfig.MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(note);
        return form;
    }

    private void addField(JPanel form, String labelText, JComponent control) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(AppConfig.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        control.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(label);
        group.add(Box.createVerticalStrut(3));
        group.add(control);

        form.add(group);
        form.add(Box.createVerticalStrut(6));
    }

    private void styleControl(JComponent control) {
        control.setPreferredSize(new Dimension(440, INPUT_HEIGHT));
        control.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        control.setMinimumSize(new Dimension(120, INPUT_HEIGHT));
    }

    private JPanel buildFooter() {
        JButton btnCancel = UIHelper.gradBtn("Hủy", new Color(0x444870), new Color(0x333660));
        JButton btnSave = UIHelper.gradBtn("Lưu người dùng", AppConfig.ACCENT, AppConfig.ACCENT2);
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());
        getRootPane().setDefaultButton(btnSave);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(AppConfig.CARD);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppConfig.BORDER));
        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    private void save() {
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

            NguoiDung user = new NguoiDung();
            user.setUsername(username);
            user.setMatKhau(password);
            user.setHoTen(fullName);
            user.setSdt(sdt);
            user.setEmail(email);
            user.setVaiTro(role);
            user.setTrangThai("Hoat dong");

            if (dao.insert(user)) {
                dispose();
                onSaved.run();
            } else {
                warn("Không thể lưu, vui lòng thử lại!");
            }
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (message != null && message.toLowerCase().contains("unique")) {
                warn("Tên đăng nhập \"" + username + "\" đã tồn tại!");
            } else {
                warn("Lỗi khi lưu người dùng: " + message);
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
