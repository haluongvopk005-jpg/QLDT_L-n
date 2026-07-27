package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

// ============================================================
//  UIHelper.java — Factory tạo component Swing tái dùng
// ============================================================
public final class UIHelper {

    private UIHelper() {
    }

    public static JLabel label(String t, Font f, Color c, int a) {
        JLabel l=new JLabel(t,a); l.setFont(f); l.setForeground(c); l.setOpaque(false); return l;
    }

    public static JTextField field() {
        JTextField f=new JTextField(); styleInput(f); return f;
    }

    public static JPasswordField passField() {
        JPasswordField f=new JPasswordField(); styleInput(f); return f;
    }

    public static void styleInput(JTextField f) {
        f.setFont(AppConfig.BODY); f.setForeground(AppConfig.TEXT);
        f.setBackground(AppConfig.BG); f.setCaretColor(AppConfig.ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConfig.BORDER,1),
            BorderFactory.createEmptyBorder(8,12,8,12)));
        f.setPreferredSize(new Dimension(9999,38));
        f.setMaximumSize(new Dimension(9999,38));
        f.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    }

    public static JComboBox<String> combo(String... items) {
        JComboBox<String> cb=new JComboBox<>(items);
        cb.setFont(AppConfig.BODY); cb.setForeground(AppConfig.TEXT);
        cb.setBackground(AppConfig.BG);
        cb.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER,1));
        cb.setPreferredSize(new Dimension(9999,38));
        cb.setMaximumSize(new Dimension(9999,38));
        cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return cb;
    }

    public static JButton gradBtn(String text, Color c1, Color c2) {
        JButton btn=new JButton(text) {
            boolean hov=false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,hov?c1.brighter():c1,getWidth(),0,hov?c2.brighter():c2));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setFont(getFont()); g2.setColor(Color.WHITE);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(AppConfig.BTN);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        int w=btn.getFontMetrics(AppConfig.BTN).stringWidth(text)+44;
        btn.setPreferredSize(new Dimension(w,38));
        return btn;
    }

    public static JButton sideBtn(String text, Runnable onClick) {
        JButton btn=new JButton(text) {
            boolean hov=false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if(hov){
                    g2.setColor(AppConfig.CARD);
                    g2.fillRoundRect(10,2,getWidth()-20,getHeight()-4,8,8);
                    g2.setColor(AppConfig.ACCENT); g2.fillRect(10,2,3,getHeight()-4);
                }
                g2.setFont(getFont());
                g2.setColor(hov?AppConfig.TEXT:AppConfig.MUTED);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),26,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(AppConfig.BODY);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(225,44)); btn.setMaximumSize(new Dimension(225,44));
        btn.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        btn.addActionListener(e->onClick.run());
        return btn;
    }

    public static void styleTable(JTable t) {
        t.setFont(AppConfig.BODY); t.setForeground(AppConfig.TEXT);
        t.setBackground(AppConfig.ROW_EVEN); t.setRowHeight(36);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0,1));
        t.setSelectionBackground(AppConfig.ROW_SEL); t.setSelectionForeground(AppConfig.TEXT);
        t.setAutoCreateRowSorter(true);
        JTableHeader h=t.getTableHeader();
        h.setFont(AppConfig.HEADER); h.setForeground(AppConfig.TEXT);
        h.setBackground(AppConfig.CARD); h.setReorderingAllowed(false);
        h.setBorder(BorderFactory.createMatteBorder(0,0,2,0,AppConfig.ACCENT));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl,Object val,boolean sel,boolean foc,int row,int col) {
                Component c=super.getTableCellRendererComponent(tbl,val,sel,foc,row,col);
                c.setFont(AppConfig.BODY);
                if(!sel){
                    c.setBackground(row%2==0?AppConfig.ROW_EVEN:AppConfig.ROW_ODD);
                    String s=val==null?"":val.toString();
                    c.setForeground(
                        s.contains("Het")||s.contains("Hết") ? AppConfig.DANGER :
                        s.contains("Con")||s.contains("Còn") ? AppConfig.SUCCESS :
                        s.contains("Ngung")||s.contains("Ngừng") ? AppConfig.WARNING :
                        AppConfig.TEXT);
                }
                ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
                return c;
            }
        });
    }

    public static void formRow(JPanel p, String lbl, JComponent comp, int row) {
        GridBagConstraints g=new GridBagConstraints();
        g.insets=new Insets(3,0,3,0); g.fill=GridBagConstraints.HORIZONTAL;
        g.weightx=1; g.gridx=0;
        g.gridy=row; p.add(label(lbl,AppConfig.SMALL,AppConfig.MUTED,SwingConstants.LEFT),g);
        g.gridy=row+1; comp.setFont(AppConfig.BODY); p.add(comp,g);
    }

    public static JDialog dialog(JFrame owner, String title, int w, int h) {
        JDialog d=new JDialog(owner,title,true);
        d.setSize(w,h); d.setLocationRelativeTo(owner);
        d.getContentPane().setBackground(AppConfig.CARD);
        d.setLayout(new BorderLayout()); return d;
    }

    public static JPanel badge(String role, String name) {
        JPanel p=new JPanel(new BorderLayout(8,0)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.CARD);
                g2.fillRoundRect(12,4,getWidth()-24,getHeight()-8,12,12); g2.dispose();
            }
        };
        p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(8,18,8,18));
        JLabel dot=new JLabel("●"); dot.setForeground(AppConfig.SUCCESS);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JPanel info=new JPanel(); info.setOpaque(false);
        info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        info.add(label(role,AppConfig.SMALL,AppConfig.ACCENT,SwingConstants.LEFT));
        info.add(label(name,AppConfig.BODY,AppConfig.TEXT,SwingConstants.LEFT));
        p.add(dot,BorderLayout.WEST); p.add(info,BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(225,58)); p.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return p;
    }
}
