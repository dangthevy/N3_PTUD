package com.gui;

import com.dao.DAO_NhanVien;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class GUI_Register extends JPanel {
    private JTextField txtUsername, txtFullName;
    private JPasswordField txtPassword, txtConfirmPass;
    private JRadioButton rbAdmin, rbStaff;
    private JButton btnRegister;
    private JLabel lblUserError, lblPassError, lblConfirmError, lblNameError;
    private Image backgroundImage;

    private DAO_NhanVien nv_dao;

    public GUI_Register() {
        // 1. Tải ảnh nền
        try {
            java.net.URL imgURL = getClass().getResource("/com/img/DoanTau.jpg");
            if (imgURL != null) {
                backgroundImage = new ImageIcon(imgURL).getImage();
            }
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh nền!");
        }
        this.nv_dao = new DAO_NhanVien(com.connectDB.ConnectDB.getConnection());

        setLayout(new GridBagLayout());

        // ================= FORM ĐĂNG KÝ (MÀU TỐI TRONG SUỐT) =================
        JPanel registerPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 200)); // Độ mờ tối để nổi bật chữ trắng
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                super.paintComponent(g);
            }
        };
        registerPanel.setOpaque(false);
        registerPanel.setPreferredSize(new Dimension(450, 680));
        registerPanel.setBorder(new EmptyBorder(30, 45, 30, 45));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(2, 0, 2, 0);
        gc.gridx = 0;

        // --- TIÊU ĐỀ ---
        JLabel lblTitle = new JLabel("GA TÀU SÀI GÒN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitle.setForeground(new Color(255, 193, 7)); // Màu vàng nhấn

        JLabel lblSub = new JLabel("ĐĂNG KÝ TÀI KHOẢN MỚI", SwingConstants.CENTER);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(Color.WHITE);

        // --- KHỞI TẠO CÁC Ô NHẬP LIỆU & TIỆN ÍCH ---
        txtFullName = createStyledField();
        txtUsername = createStyledField();
        txtPassword = createStyledPassField();
        txtConfirmPass = createStyledPassField();

        lblNameError = createErrorLabel();
        lblUserError = createErrorLabel();
        lblPassError = createErrorLabel();
        lblConfirmError = createErrorLabel();

        // --- TIỆN ÍCH: TỰ ĐỘNG CHUYỂN Ô KHI NHẤN ENTER ---
        txtFullName.addActionListener(e -> txtUsername.requestFocus());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());
        txtPassword.addActionListener(e -> txtConfirmPass.requestFocus());
        txtConfirmPass.addActionListener(e -> btnRegister.doClick());

        // --- TIỆN ÍCH: XÓA THÔNG BÁO LỖI KHI ĐANG GÕ ---
        addKeyReleaseListener(txtFullName, lblNameError);
        addKeyReleaseListener(txtUsername, lblUserError);
        addKeyReleaseListener(txtPassword, lblPassError);
        addKeyReleaseListener(txtConfirmPass, lblConfirmError);

        // --- CHỨC VỤ ---
        rbAdmin = new JRadioButton("Quản lý");
        rbStaff = new JRadioButton("Nhân viên", true);
        rbAdmin.setForeground(Color.WHITE);
        rbAdmin.setOpaque(false);
        rbStaff.setForeground(Color.WHITE);
        rbStaff.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        group.add(rbAdmin);
        group.add(rbStaff);

        JPanel pnlRole = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlRole.setOpaque(false);
        pnlRole.add(rbAdmin);
        pnlRole.add(rbStaff);

        // --- NÚT ĐĂNG KÝ ---
        btnRegister = new JButton("ĐĂNG KÝ NGAY");
        btnRegister.setBackground(new Color(255, 193, 7));
        btnRegister.setForeground(Color.BLACK);
        btnRegister.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setPreferredSize(new Dimension(0, 45));
        btnRegister.addActionListener(e -> handleRegister());

        // --- THÊM THÀNH PHẦN VÀO FORM ---
        gc.gridy = 0;
        registerPanel.add(lblTitle, gc);
        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 25, 0);
        registerPanel.add(lblSub, gc);

        gc.insets = new Insets(2, 0, 2, 0);
        addHeaderLabel(registerPanel, "Họ và tên", gc, 2);
        gc.gridy = 3;
        registerPanel.add(txtFullName, gc);
        gc.gridy = 4;
        registerPanel.add(lblNameError, gc);

        addHeaderLabel(registerPanel, "Mã nhân viên (ID)", gc, 5);
        gc.gridy = 6;
        registerPanel.add(txtUsername, gc);
        gc.gridy = 7;
        registerPanel.add(lblUserError, gc);

        addHeaderLabel(registerPanel, "Mật khẩu", gc, 8);
        gc.gridy = 9;
        registerPanel.add(txtPassword, gc);
        gc.gridy = 10;
        registerPanel.add(lblPassError, gc);

        addHeaderLabel(registerPanel, "Xác nhận mật khẩu", gc, 11);
        gc.gridy = 12;
        registerPanel.add(txtConfirmPass, gc);
        gc.gridy = 13;
        registerPanel.add(lblConfirmError, gc);

        addHeaderLabel(registerPanel, "Chức vụ", gc, 14);
        gc.gridy = 15;
        registerPanel.add(pnlRole, gc);

        gc.gridy = 16;
        gc.insets = new Insets(20, 0, 10, 0);
        registerPanel.add(btnRegister, gc);

        add(registerPanel);
    }

    // Vẽ nền full màn hình
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // --- HELPER METHODS ---
    private void addHeaderLabel(JPanel p, String text, GridBagConstraints gc, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(180, 180, 180));
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gc.gridy = y;
        p.add(lbl, gc);
    }

    private JTextField createStyledField() {
        JTextField tf = new JTextField(20);
        tf.setBackground(new Color(255, 255, 255, 245));
        tf.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return tf;
    }

    private JPasswordField createStyledPassField() {
        JPasswordField pf = new JPasswordField(20);
        pf.setBackground(new Color(255, 255, 255, 245));
        pf.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        return pf;
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setForeground(new Color(255, 110, 110));
        lbl.setFont(new Font("Arial", Font.ITALIC, 11));
        return lbl;
    }

    private void addKeyReleaseListener(JTextField field, JLabel errorLabel) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                errorLabel.setText(" ");
            }
        });
    }

    // --- LOGIC XỬ LÝ (Kết nối SQL qua DAO) ---
    private void handleRegister() {
        String name = txtFullName.getText().trim();
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPass.getPassword());
        String role = rbAdmin.isSelected() ? "Quản lý" : "Nhân viên";

        boolean hasError = false;

        if (name.isEmpty()) {
            lblNameError.setText("Họ tên không được để trống!");
            hasError = true;
        }
        if (user.isEmpty()) {
            lblUserError.setText("ID không được để trống!");
            hasError = true;
        }
        if (pass.length() < 3) {
            lblPassError.setText("Mật khẩu phải từ 3 ký tự!");
            hasError = true;
        }
        if (!pass.equals(confirm)) {
            lblConfirmError.setText("Xác nhận mật khẩu không khớp!");
            hasError = true;
        }

        if (hasError)
            return;

        // Gọi DAO để lưu vào SQL Server
        if (nv_dao.create(user, name, pass, role)) {
            JOptionPane.showMessageDialog(this, "Đã đăng ký thành công nhân viên: " + name);
            clearForm();
        } else {
            // Lỗi trùng ID sẽ rơi vào đây
            JOptionPane.showMessageDialog(this, "Mã nhân viên '" + user + "' đã được sử dụng. Vui lòng kiểm tra lại!",
                    "Lỗi đăng ký", JOptionPane.WARNING_MESSAGE);
            txtUsername.requestFocus();
            txtUsername.selectAll();
        }
    }

    private void clearForm() {
        txtFullName.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtConfirmPass.setText("");
        rbStaff.setSelected(true);
        txtFullName.requestFocus();
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Hệ thống Ga Tàu Sài Gòn");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1000, 800);
        f.add(new GUI_Register());
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}