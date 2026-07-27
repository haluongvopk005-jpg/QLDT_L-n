package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.model.NguoiDung;
import javax.swing.*;
import java.awt.*;

// ============================================================
//  MainFrame.java — Cửa sổ chính tiếng Việt hoàn chỉnh
// ============================================================
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private CardLayout     cardLayout;
    private JPanel         cardPanel;
    private DashboardPanel dashPanel;
    private PhonePanel     phonePanel;
    private UserPanel      userPanel;
    private BaoCaoPanel    baoCaoPanel;
    private KhachHangPanel  khachHangPanel;
    private NhaCungCapPanel nhaCungCapPanel;
    private NhapHangPanel   nhapHangPanel;
    private BanHangPanel    banHangPanel;
    private HoaDonPanel     hoaDonPanel;

    public MainFrame() {
        setTitle("Hệ thống quản lý điện thoại");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200,700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppConfig.BG);
        cardLayout=new CardLayout();
        cardPanel=new JPanel(cardLayout);
        cardPanel.setBackground(AppConfig.BG);
        cardPanel.add(new LoginPanel(this),"LOGIN");
        add(cardPanel);
        cardLayout.show(cardPanel,"LOGIN");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public void showMain(NguoiDung user) {
        // Loại bỏ nội dung của phiên đăng nhập trước.
        for(int i=cardPanel.getComponentCount()-1;i>=0;i--){
            if("MAIN".equals(cardPanel.getComponent(i).getName())){cardPanel.remove(i);break;}
        }
        JPanel main=buildMain(user,"admin".equals(user.getVaiTro()));
        main.setName("MAIN");
        cardPanel.add(main,"MAIN");
        cardLayout.show(cardPanel,"MAIN");
        setTitle("Quản lý điện thoại  —  " + user.getHoTen()
            + "  [" + ("admin".equals(user.getVaiTro()) ? "Quản trị viên" : "Nhân viên bán hàng") + "]");
    }

    public void showLogin() {
        setTitle("Hệ thống quản lý điện thoại");
        cardLayout.show(cardPanel,"LOGIN");
    }

    private JPanel buildMain(NguoiDung user, boolean isAdmin) {
        JPanel screen=new JPanel(new BorderLayout());
        screen.setBackground(AppConfig.BG);

        CardLayout inner=new CardLayout();
        JPanel content=new JPanel(inner);
        content.setBackground(AppConfig.BG);

        dashPanel  = new DashboardPanel();
        phonePanel = new PhonePanel(this,isAdmin);
        baoCaoPanel= new BaoCaoPanel(isAdmin);
        khachHangPanel = new KhachHangPanel(this);
        // Nhân viên bán hàng không được vào phần nhập hàng/nhà cung cấp vì các phần này có giá nhập (tiền vốn).
        nhaCungCapPanel = isAdmin ? new NhaCungCapPanel(this) : null;
        nhapHangPanel = isAdmin ? new NhapHangPanel(user.getId()) : null;
        banHangPanel = new BanHangPanel(user.getId());
        hoaDonPanel = new HoaDonPanel();
        userPanel  = isAdmin ? new UserPanel(this,user.getUsername()) : null;

        content.add(dashPanel, "DASH");
        content.add(phonePanel,"PHONE");
        content.add(khachHangPanel,"KHACH");
        if (isAdmin) {
            content.add(nhaCungCapPanel,"NCC");
            content.add(nhapHangPanel,"NHAP");
        }
        content.add(banHangPanel,"BAN");
        content.add(hoaDonPanel,"HOADON");
        content.add(baoCaoPanel,"BAOCAO");
        if(isAdmin) content.add(userPanel,"USER");

        JPanel sidebar=buildSidebar(user,isAdmin,inner,content);

        if(isAdmin){dashPanel.refresh();inner.show(content,"DASH");}
        else       {phonePanel.loadData();inner.show(content,"PHONE");}

        screen.add(sidebar,BorderLayout.WEST);
        screen.add(content,BorderLayout.CENTER);
        return screen;
    }

    private JPanel buildSidebar(NguoiDung user, boolean isAdmin,
                                 CardLayout inner, JPanel content) {
        JPanel sb=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                g.setColor(AppConfig.SURFACE); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(AppConfig.BORDER); g.drawLine(getWidth()-1,0,getWidth()-1,getHeight());
            }
        };
        sb.setLayout(new BoxLayout(sb,BoxLayout.Y_AXIS));
        sb.setOpaque(false);
        sb.setPreferredSize(new Dimension(225,0));
        sb.setBorder(BorderFactory.createEmptyBorder(18,0,18,0));

        // Logo
        JLabel logo=new JLabel("  PhoneManager");
        logo.setFont(new Font("Segoe UI",Font.BOLD,13));
        logo.setForeground(AppConfig.ACCENT);
        logo.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(0,16,14,16));

        // Badge
        String roleVN=isAdmin ? "Quản trị viên" : "Nhân viên bán hàng";
        JPanel badge=UIHelper.badge(roleVN,user.getHoTen());

        sb.add(logo); sb.add(badge);
        sb.add(Box.createVerticalStrut(6)); sb.add(sep());
        sb.add(Box.createVerticalStrut(6));

        // === NÚT MENU TIẾNG VIỆT ===
        if(isAdmin){
            sb.add(UIHelper.sideBtn("Bảng điều khiển",()->{
                dashPanel.refresh(); inner.show(content,"DASH");
            }));
        }
        sb.add(UIHelper.sideBtn("Điện thoại",()->{
            phonePanel.loadData(); inner.show(content,"PHONE");
        }));
        if(isAdmin){
            sb.add(UIHelper.sideBtn("Người dùng",()->{
                userPanel.loadData(); inner.show(content,"USER");
            }));
        }
        sb.add(UIHelper.sideBtn("Khách hàng",()->{
            khachHangPanel.loadData(); inner.show(content,"KHACH");
        }));
        if (isAdmin) {
            sb.add(UIHelper.sideBtn("Nhà cung cấp",()->{
                nhaCungCapPanel.loadData(); inner.show(content,"NCC");
            }));
            sb.add(UIHelper.sideBtn("Nhập hàng",()->{
                nhapHangPanel.loadCombos(); nhapHangPanel.loadHistory(); inner.show(content,"NHAP");
            }));
        }
        sb.add(UIHelper.sideBtn("Bán hàng",()->{
            banHangPanel.loadCombos(); inner.show(content,"BAN");
        }));
        sb.add(UIHelper.sideBtn("Hóa đơn",()->{
            hoaDonPanel.loadData(); inner.show(content,"HOADON");
        }));
        String tenBaoCao = isAdmin ? "Báo cáo doanh thu" : "Báo cáo bán hàng";
        sb.add(UIHelper.sideBtn(tenBaoCao,()->{
            baoCaoPanel.loadData(); inner.show(content,"BAOCAO");
        }));

        sb.add(Box.createVerticalGlue());
        sb.add(sep()); sb.add(Box.createVerticalStrut(6));
        sb.add(UIHelper.sideBtn("Đăng xuất",this::showLogin));
        return sb;
    }

    private JSeparator sep(){
        JSeparator s=new JSeparator(); s.setForeground(AppConfig.BORDER);
        s.setMaximumSize(new Dimension(9999,1));
        s.setAlignmentX(JComponent.LEFT_ALIGNMENT); return s;
    }
}
