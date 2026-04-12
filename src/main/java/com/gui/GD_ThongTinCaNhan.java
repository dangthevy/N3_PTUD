package com.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import com.entities.NhanVien;
import com.dao.DAO_NhanVien;
import com.connectDB.ConnectDB;

public class GD_ThongTinCaNhan extends JDialog {

    // ================= COLOR (đồng bộ TAB_QLNhanVien) =================
    private static final Color BG_PAGE    = new Color(0xF4F7FB);
    private static final Color BG_CARD    = Color.WHITE;
    private static final Color ACCENT     = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR = new Color(0x2270CC);
    private static final Color TEXT_DARK  = new Color(0x1E2B3C);
    private static final Color TEXT_MID   = new Color(0x5A6A7D);
    private static final Color BORDER     = new Color(0xE2EAF4);
    private static final Color BTN_RED    = new Color(0xC0392B);
    private static final Color BTN_GREEN  = new Color(0x16A34A);
    private static final Color BTN_GREEN_HVR = new Color(0x22C55E);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);

    // ================= FONT (đồng bộ TAB_QLNhanVien) =================
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_MUTED = new Font("Segoe UI", Font.BOLD, 12);

    private enum BtnStyle { PRIMARY, SUCCESS, DANGER }

    private NhanVien nv;
    private DAO_NhanVien daoNV;

    private JTextField txtTen, txtSdt, txtEmail;
    private JLabel lblMa, lblTaiKhoan, lblChucVu, lblNgayVao, lblTrangThai, lblHeaderName;
    private JButton btnEditSave, btnLogout;
    private boolean isEditMode = false;

    public GD_ThongTinCaNhan(JFrame parent, NhanVien nv) {
        super(parent, "Thông tin cá nhân", true);
        this.nv    = nv;
        this.daoNV = new DAO_NhanVien(ConnectDB.getConnection());

        setSize(780, 560);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BG_PAGE);
        setLayout(new BorderLayout());

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);

        setEditMode(false);
        initEvents(parent);
    }

    // ─── HEADER (giống table header của QLNhanVien: ACCENT đậm) ──────────────
    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(ACCENT);
        pnl.setBorder(new EmptyBorder(14, 24, 14, 24));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel lblAvatar = new JLabel("👤");
        lblAvatar.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 36));
        lblAvatar.setForeground(Color.WHITE);
        lblHeaderName = new JLabel(nv.getTenNV().toUpperCase());
        lblHeaderName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeaderName.setForeground(Color.WHITE);
        left.add(lblAvatar);
        left.add(lblHeaderName);

        btnLogout = makeBtn("Đăng xuất", BtnStyle.DANGER);
        btnLogout.setPreferredSize(new Dimension(110, 34));

        pnl.add(left,      BorderLayout.WEST);
        pnl.add(btnLogout, BorderLayout.EAST);
        return pnl;
    }

    // ─── CONTENT: card trắng chứa 2 cột thông tin ────────────────────────────
    private JPanel buildContent() {
        // Wrapper nền xám
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_PAGE);
        wrapper.setBorder(new EmptyBorder(20, 24, 10, 24));

        // Card trắng
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                new EmptyBorder(24, 30, 24, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(10, 12, 10, 12);
        gbc.weightx = 0.5;

        // Fields
        lblMa        = makeReadOnly(nv.getMaNV());
        txtTen       = makeStyledField(nv.getTenNV());
        txtSdt       = makeStyledField(nv.getSdt());

        String emailPfx = nv.getEmail().contains("@")
                ? nv.getEmail().split("@")[0] : nv.getEmail();
        txtEmail = makeStyledField(emailPfx);

        // Email + suffix
        JPanel pnlEmail = new JPanel(new BorderLayout(6, 0));
        pnlEmail.setOpaque(false);
        pnlEmail.add(txtEmail, BorderLayout.CENTER);
        JLabel lblSfx = new JLabel("@gmail.com");
        lblSfx.setFont(F_CELL);
        lblSfx.setForeground(TEXT_MID);
        pnlEmail.add(lblSfx, BorderLayout.EAST);

        lblTaiKhoan  = makeReadOnly(nv.getTaiKhoan());
        lblChucVu    = makeReadOnly(nv.getChucVu().name());
        lblNgayVao   = makeReadOnly(nv.getNgayVaoLam().toString());

        lblTrangThai = new JLabel(nv.getTrangThai().name());
        lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTrangThai.setForeground(new Color(0x16A34A));

        addCell(card, "Mã nhân viên",  lblMa,       0, 0, gbc);
        addCell(card, "Họ và tên",     txtTen,      0, 1, gbc);
        addCell(card, "Số điện thoại", txtSdt,      1, 0, gbc);
        addCell(card, "Email",         pnlEmail,    1, 1, gbc);
        addCell(card, "Tài khoản",     lblTaiKhoan, 2, 0, gbc);
        addCell(card, "Chức vụ",       lblChucVu,   2, 1, gbc);
        addCell(card, "Ngày vào làm",  lblNgayVao,  3, 0, gbc);
        addCell(card, "Trạng thái",    lblTrangThai,3, 1, gbc);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    /** Mỗi ô: label nhỏ màu muted phía trên, component phía dưới */
    private void addCell(JPanel panel, String title, JComponent comp,
                         int row, int col, GridBagConstraints gbc) {
        JPanel cell = new JPanel(new BorderLayout(0, 4));
        cell.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(F_MUTED);
        lbl.setForeground(new Color(100, 116, 139)); // COLOR_TEXT_MUTED
        cell.add(lbl,  BorderLayout.NORTH);
        cell.add(comp, BorderLayout.CENTER);
        gbc.gridx = col;
        gbc.gridy = row;
        panel.add(cell, gbc);
    }

    // ─── FOOTER ───────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 14));
        pnl.setBackground(BG_PAGE);
        pnl.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
        btnEditSave = makeBtn("Chỉnh sửa thông tin", BtnStyle.PRIMARY);
        btnEditSave.setPreferredSize(new Dimension(180, 36));
        pnl.add(btnEditSave);
        return pnl;
    }

    // ─── EVENTS ───────────────────────────────────────────────────────────────
    private void initEvents(JFrame parent) {
        btnLogout.addActionListener(e -> {
            if (isEditMode) {
                JOptionPane.showMessageDialog(this, "Đang chỉnh sửa, hãy lưu trước!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                if (parent != null) parent.dispose();
                JFrame loginFrame = new JFrame("Đăng nhập");
                GUI_Login loginPanel = new GUI_Login();
                loginPanel.setParentFrame(loginFrame);
                loginFrame.setContentPane(loginPanel);
                loginFrame.pack();
                loginFrame.setLocationRelativeTo(null);
                loginFrame.setVisible(true);
            }
        });

        btnEditSave.addActionListener(e -> {
            if (!isEditMode) {
                setEditMode(true);
                btnEditSave.setText("Lưu thay đổi");
            } else {
                handleSave();
            }
        });
    }

    private void handleSave() {
        String ten  = capitalizeWords(txtTen.getText().trim());
        String sdt  = txtSdt.getText().trim();
        String mail = txtEmail.getText().trim();

        if (!ten.matches("^[A-Za-zÀ-ỹ\\s]+$")) {
            JOptionPane.showMessageDialog(this, "Tên chỉ chứa chữ!"); return;
        }
        if (!sdt.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "SĐT phải 10 số!"); return;
        }
        if (!mail.matches("^[a-zA-Z0-9]+$")) {
            JOptionPane.showMessageDialog(this, "Email chỉ gồm chữ và số!"); return;
        }

        nv.setTenNV(ten);
        nv.setSdt(sdt);
        nv.setEmail(mail + "@gmail.com");

        if (daoNV.updateProfile(nv)) {
            lblHeaderName.setText(ten.toUpperCase());
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            setEditMode(false);
            btnEditSave.setText("Chỉnh sửa thông tin");
        }
    }

    private void setEditMode(boolean editable) {
        this.isEditMode = editable;
        for (JTextField tf : new JTextField[]{txtTen, txtSdt, txtEmail}) {
            tf.setEditable(editable);
            tf.setBackground(editable ? Color.WHITE : new Color(0xF8FAFC));
            tf.setBorder(editable
                    ? new LineBorder(ACCENT, 1, true)
                    : new LineBorder(BORDER, 1, true));
        }
        btnLogout.setEnabled(!editable);
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────
    /** Field chỉ đọc (hiển thị như label nhưng dạng JLabel căn lề trái) */
    private JLabel makeReadOnly(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_CELL);
        l.setForeground(TEXT_DARK);
        return l;
    }

    /** JTextField có style đồng nhất */
    private JTextField makeStyledField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(F_CELL);
        tf.setBorder(new LineBorder(BORDER, 1, true));
        tf.setPreferredSize(new Dimension(200, 34));
        return tf;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                switch (style) {
                    case PRIMARY:
                        g2.setColor(getModel().isPressed()  ? new Color(0x0F3F8C)
                                  : getModel().isRollover() ? ACCENT_HVR : ACCENT);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        break;
                    case SUCCESS:
                        g2.setColor(getModel().isPressed()  ? new Color(0x166534)
                                  : getModel().isRollover() ? BTN_GREEN_HVR : BTN_GREEN);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        break;
                    case DANGER:
                        g2.setColor(getModel().isPressed()  ? new Color(0x922B21)
                                  : getModel().isRollover() ? new Color(0xE74C3C) : BTN_RED);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        break;
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_LABEL);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(140, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words)
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        return sb.toString().trim();
    }

    private static class ShadowBorder extends AbstractBorder {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0xE2EAF4));
            g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
        }
    }
}