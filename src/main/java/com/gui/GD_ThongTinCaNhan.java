package com.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

import com.entities.NhanVien;
import com.dao.DAO_NhanVien;
import com.connectDB.ConnectDB;

public class GD_ThongTinCaNhan extends JDialog {
    private NhanVien nv;
    private DAO_NhanVien daoNV;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(236, 240, 241);
    private final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private final Color DANGER_COLOR = new Color(192, 57, 43);

    private JTextField txtTen, txtSdt, txtEmail;
    private JLabel lblMa, lblTaiKhoan, lblChucVu, lblNgayVao, lblTrangThai, lblHeaderName;
    private JButton btnEditSave, btnChangePass;
    private boolean isEditMode = false;

    public GD_ThongTinCaNhan(JFrame parent, NhanVien nv) {
        super(parent, "Thông tin cá nhân", true);
        this.nv = nv;
        this.daoNV = new DAO_NhanVien(ConnectDB.getConnection());

        setSize(750, 600);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // --- 1. HEADER PANEL ---
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 20));
        pnlHeader.setBackground(PRIMARY_COLOR);

        JLabel lblAvatar = new JLabel();
        // Cập nhật đường dẫn ảnh mới theo yêu cầu
        java.net.URL imgURL = getClass().getResource("/com/img/avatar.jpg");
        if (imgURL != null) {
            lblAvatar.setIcon(
                    new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
        } else {
            lblAvatar.setText("👤");
            lblAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 40));
            lblAvatar.setForeground(Color.WHITE);
        }

        lblHeaderName = new JLabel(nv.getTenNV().toUpperCase());
        lblHeaderName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeaderName.setForeground(Color.WHITE);

        pnlHeader.add(lblAvatar);
        pnlHeader.add(lblHeaderName);
        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. CENTER PANEL ---
        JPanel pnlContent = new JPanel(new GridBagLayout());
        pnlContent.setOpaque(false);
        pnlContent.setBorder(new EmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.weightx = 0.5;

        lblMa = new JLabel(nv.getMaNV());
        txtTen = createInfoField(nv.getTenNV());
        txtSdt = createInfoField(nv.getSdt());
        txtEmail = createInfoField(nv.getEmail());
        lblTaiKhoan = new JLabel(nv.getTaiKhoan());
        lblChucVu = new JLabel(nv.getChucVu() != null ? nv.getChucVu().name() : "N/A");
        lblTrangThai = new JLabel(nv.getTrangThai() != null ? nv.getTrangThai().name() : "N/A");
        lblNgayVao = new JLabel(nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().toString() : "N/A");

        addInfoComponent(pnlContent, "Mã nhân viên", lblMa, 0, 0, gbc);
        addInfoComponent(pnlContent, "Họ và tên", txtTen, 0, 1, gbc);
        addInfoComponent(pnlContent, "Số điện thoại", txtSdt, 1, 0, gbc);
        addInfoComponent(pnlContent, "Email", txtEmail, 1, 1, gbc);
        addInfoComponent(pnlContent, "Tài khoản", lblTaiKhoan, 2, 0, gbc);
        addInfoComponent(pnlContent, "Chức vụ", lblChucVu, 2, 1, gbc);
        addInfoComponent(pnlContent, "Ngày vào làm", lblNgayVao, 3, 0, gbc);
        addInfoComponent(pnlContent, "Trạng thái", lblTrangThai, 3, 1, gbc);

        add(pnlContent, BorderLayout.CENTER);

        // --- 3. FOOTER PANEL ---
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setBackground(SECONDARY_COLOR);

        btnChangePass = createBottomButton("Đổi mật khẩu", DANGER_COLOR);
        btnEditSave = createBottomButton("Chỉnh sửa", PRIMARY_COLOR);

        pnlFooter.add(btnChangePass);
        pnlFooter.add(btnEditSave);
        add(pnlFooter, BorderLayout.SOUTH);

        setEditMode(false); // Khởi tạo ở chế độ xem

        // --- EVENTS ---
        btnEditSave.addActionListener(e -> {
            if (!isEditMode) {
                setEditMode(true);
                btnEditSave.setText("Lưu thông tin");
                btnEditSave.setBackground(SUCCESS_COLOR);
                btnChangePass.setEnabled(false); // Khóa nút đổi mật khẩu khi đang sửa
            } else {
                handleSave();
            }
        });

        btnChangePass.addActionListener(e -> {
            if (!isEditMode) {
                openChangePasswordDialog();
            }
        });
    }

    private void setEditMode(boolean editable) {
        this.isEditMode = editable;

        JTextField[] fields = { txtTen, txtSdt, txtEmail };
        // Khi không sửa: dùng màu trong suốt. Khi sửa: dùng màu trắng.
        Color bgColor = editable ? Color.WHITE : new Color(0, 0, 0, 0);
        Border border = editable ? new LineBorder(Color.LIGHT_GRAY, 1) : new EmptyBorder(1, 1, 1, 1);

        for (JTextField tf : fields) {
            tf.setEditable(editable);
            tf.setBackground(bgColor);
            tf.setBorder(border);
            tf.setFocusable(editable);

            // QUAN TRỌNG: Sửa lỗi đè chữ
            // setOpaque(false) giúp Swing vẽ lại vùng nền phía dưới khi thành phần thay đổi
            tf.setOpaque(editable);

            tf.getCaret().setVisible(editable);
        }

        if (!editable) {
            btnChangePass.setEnabled(true);
        }

        // Ép giao diện vẽ lại toàn bộ để xóa bỏ "rác" đồ họa cũ
        revalidate();
        repaint();
    }

    private void handleSave() {
        String ten = txtTen.getText().trim();
        String sdt = txtSdt.getText().trim();
        String email = txtEmail.getText().trim();

        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên không được để trống!");
            return;
        }

        nv.setTenNV(ten);
        nv.setSdt(sdt);
        nv.setEmail(email);

        if (daoNV.updateProfile(nv)) {
            lblHeaderName.setText(ten.toUpperCase());
            JOptionPane.showMessageDialog(this, "Đã lưu thay đổi!");
            setEditMode(false);
            btnEditSave.setText("Chỉnh sửa");
            btnEditSave.setBackground(PRIMARY_COLOR);
        }
    }

    private void addInfoComponent(JPanel panel, String title, JComponent comp, int row, int col,
                                  GridBagConstraints gbc) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(Color.GRAY);

        comp.setFont(new Font("Segoe UI", Font.BOLD, 15));
        if (comp instanceof JLabel) {
            comp.setForeground(new Color(44, 62, 80));
        }

        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);

        gbc.gridx = col;
        gbc.gridy = row;
        panel.add(p, gbc);
    }

    private JTextField createInfoField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tf.setMargin(new Insets(5, 5, 5, 5));
        return tf;
    }

    private JButton createBottomButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(130, 35));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(5, 5, 5, 5));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void openChangePasswordDialog() {
        JPasswordField pfOld = new JPasswordField();
        JPasswordField pfNew = new JPasswordField();
        JPasswordField pfConfirm = new JPasswordField();
        Object[] message = { "Mật khẩu hiện tại:", pfOld, "Mật khẩu mới:", pfNew, "Xác nhận mật khẩu mới:", pfConfirm };

        int option = JOptionPane.showConfirmDialog(this, message, "Đổi mật khẩu", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String oldP = new String(pfOld.getPassword()).trim();
            String newP = new String(pfNew.getPassword()).trim();
            String confP = new String(pfConfirm.getPassword()).trim();

            if (!oldP.equals(nv.getMatKhau())) {
                JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không chính xác!");
                return;
            }
            if (newP.isEmpty() || newP.length() < 3) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới phải từ 3 ký tự trở lên!");
                return;
            }
            if (!newP.equals(confP)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
                return;
            }
            if (daoNV.updatePassword(nv.getTaiKhoan(), newP)) { // Truyền TaiKhoan thay vì MaNV
                nv.setMatKhau(newP);
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại!");
            }
        }
    }
}