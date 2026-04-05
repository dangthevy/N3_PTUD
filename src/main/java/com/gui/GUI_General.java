package com.gui;

import javax.swing.*;
import javax.swing.border.MatteBorder;

import com.entities.NhanVien;
import com.enums.ChucVu;

import java.awt.*;
import java.awt.event.*;

public class GUI_General extends JPanel {
    // Khai báo các Tab
    private TAB_Dashboard tab_Dashboard;
    private TAB_BanVe tab_BanVe;
    private TAB_HoanVe tab_HoanVe;
    private TAB_ThanhToanLapHD tab_ThanhToanLapHD;
    private TAB_Tau tab_Tau;
    private TAB_Toa_ChoNgoi tab_Toa_ChoNgoi;
    private TAB_Ga_Tuyen tab_Ga_Tuyen;
    private TAB_LichTrinh_ChuyenTau tab_LichTrinh_ChuyenTau;
    private TAB_Gia tab_Gia;
    private TAB_QLNhanVien tab_QLNhanVien;
    private TAB_QLKhachHang tab_QLKhachHang;
    private TAB_KhuyenMai tab_KhuyenMai;
    private TAB_ThongKeDoanhThu tab_ThongKeDoanhThu;
    private TAB_ThongKeVe tab_ThongKeVe;

    private JPanel contentPanel;
    private JPanel currentTabPanel;
    private JButton activeButton;

    // --- BẢNG MÀU ĐỒNG BỘ ---
    private final Color BG_PAGE        = new Color(0xF4F7FB);
    private final Color SIDEBAR_BG     = new Color(0x1A5EAB);
    private final Color SIDEBAR_HOVER  = new Color(0x2270CC);
    private final Color SIDEBAR_ACTIVE = new Color(0x4D9DE0);
    private final Color SUB_MENU_BG    = new Color(0x155096);
    private final Color TEXT_LIGHT     = new Color(0xA0AEC0);
    private final Color BORDER_COLOR   = new Color(0xE2EAF4);

    public GUI_General(NhanVien nv) {
        initComponents(nv);
    }

    private void initComponents(NhanVien nv) {
        setLayout(new BorderLayout());
        setBackground(BG_PAGE);

        // Khởi tạo các Tab
        tab_Dashboard = new TAB_Dashboard(this, nv.getChucVu().name());
        tab_BanVe = new TAB_BanVe();
        tab_HoanVe = new TAB_HoanVe();
        tab_ThanhToanLapHD = new TAB_ThanhToanLapHD();
        tab_Tau = new TAB_Tau();
        tab_Toa_ChoNgoi = new TAB_Toa_ChoNgoi();
        tab_Ga_Tuyen = new TAB_Ga_Tuyen();
        tab_LichTrinh_ChuyenTau = new TAB_LichTrinh_ChuyenTau();
        tab_Gia = new TAB_Gia();
        tab_QLNhanVien = new TAB_QLNhanVien();
        tab_QLKhachHang = new TAB_QLKhachHang(nv);
        tab_KhuyenMai = new TAB_KhuyenMai();
        tab_ThongKeDoanhThu = new TAB_ThongKeDoanhThu();
        tab_ThongKeVe = new TAB_ThongKeVe();

        // ================= 1. SIDEBAR PANEL =================
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(280, 0));

        // Thêm khoảng trống padding trên cùng
        sidebarPanel.add(Box.createVerticalStrut(10));
        addUserInfoSection(sidebarPanel, nv);
        addTabButtons(sidebarPanel, nv);

        add(sidebarPanel, BorderLayout.WEST);

        // ================= 2. RIGHT PANEL =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_PAGE);
        rightPanel.add(createHeader(nv), BorderLayout.NORTH);

        // Nội dung Tab chính, bọc trong một panel có padding
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(BG_PAGE);
        // Padding cách điệu để Tab bên trong nổi bật lên
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_PAGE);
        showTab(tab_Dashboard);

        contentWrapper.add(contentPanel, BorderLayout.CENTER);
        rightPanel.add(contentWrapper, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);
    }

    private void addTabButtons(JPanel sidebar, NhanVien nv) {
        boolean isAdmin = (nv.getChucVu() == ChucVu.ADMIN);
        boolean isQuanLy = (nv.getChucVu() == ChucVu.QUANLY);
        boolean canAccessMain = (nv.getChucVu() == ChucVu.NHANVIEN || isAdmin);
        boolean canAccessAdminTools = (isQuanLy || isAdmin);

        // Menu chính bám sát đường kẻ ngang
        sidebar.add(createSideTitle("CHỨC NĂNG CHÍNH"));
        addTabButton(sidebar, "Màn hình chính", tab_Dashboard, true);
        addTabButton(sidebar, "Bán vé", tab_BanVe, canAccessMain);
        addTabButton(sidebar, "Hoàn vé", tab_HoanVe, canAccessMain);
        addTabButton(sidebar, "Tra cứu hóa đơn", tab_ThanhToanLapHD, canAccessMain);

        // Thêm khoảng trống 10px để tách biệt các nhóm
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createSideTitle("QUẢN LÝ HỆ THỐNG"));
        addDropdownMenu(sidebar, "Quản lý Đoàn tàu", new String[] { "Tàu", "Toa & Chỗ ngồi" },
                new JPanel[] { tab_Tau, tab_Toa_ChoNgoi }, canAccessAdminTools);
        addDropdownMenu(sidebar, "Lịch trình & Giá", new String[] { "Ga & Tuyến", "Lịch trình & Chuyến", "Bảng Giá" },
                new JPanel[] { tab_Ga_Tuyen, tab_LichTrinh_ChuyenTau, tab_Gia }, canAccessAdminTools);
        addTabButton(sidebar, "Quản lý Nhân viên", tab_QLNhanVien, canAccessAdminTools);
        addTabButton(sidebar, "Quản lý Khách hàng", tab_QLKhachHang, canAccessAdminTools);
        addTabButton(sidebar, "Khuyến mãi", tab_KhuyenMai, canAccessAdminTools);

        // Thêm khoảng trống 10px để tách biệt các nhóm
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createSideTitle("BÁO CÁO"));
        addDropdownMenu(sidebar, "Thống kê", new String[] { "Doanh thu", "Lượng vé" },
                new JPanel[] { tab_ThongKeDoanhThu, tab_ThongKeVe }, canAccessAdminTools);

        sidebar.add(Box.createVerticalGlue());
    }

    private void addTabButton(JPanel sidebar, String title, JPanel target, boolean canAccess) {
        JButton btn = createStyledButton(title, false);
        btn.setEnabled(canAccess);
        if (canAccess) {
            btn.addActionListener(e -> {
                updateActiveButton(btn);
                showTab(target);
            });
        } else {
            btn.setForeground(TEXT_LIGHT);
            btn.setToolTipText("Chức năng này chỉ dành cho Quản lý");
        }
        sidebar.add(btn);

        // Khởi tạo trạng thái active cho nút đầu tiên
        if (title.equals("Màn hình chính")) {
            activeButton = btn;
            btn.repaint();
        }
    }

    private void addDropdownMenu(JPanel sidebar, String title, String[] subTitles, JPanel[] targets, boolean canAccess) {
        JButton parentBtn = createStyledButton("▼  " + title, false);
        parentBtn.setEnabled(canAccess);

        JPanel subMenuPanel = new JPanel();
        subMenuPanel.setLayout(new BoxLayout(subMenuPanel, BoxLayout.Y_AXIS));
        subMenuPanel.setOpaque(false);
        subMenuPanel.setVisible(false);

        if (canAccess) {
            for (int i = 0; i < subTitles.length; i++) {
                final int index = i;
                JButton subBtn = createStyledButton("•  " + subTitles[i], true);
                subBtn.addActionListener(e -> {
                    updateActiveButton(subBtn);
                    showTab(targets[index]);
                });
                subMenuPanel.add(subBtn);
            }
            parentBtn.addActionListener(e -> {
                boolean isVisible = !subMenuPanel.isVisible();
                subMenuPanel.setVisible(isVisible);
                parentBtn.setText((isVisible ? "▲  " : "▼  ") + title);
                sidebar.revalidate();
            });
        } else {
            parentBtn.setForeground(TEXT_LIGHT);
        }
        sidebar.add(parentBtn);
        sidebar.add(subMenuPanel);
    }

    // =====================================================================
    // HÀM TẠO NÚT SIDEBAR BO GÓC (CUSTOM PAINT)
    // =====================================================================
    private JButton createStyledButton(String text, boolean isSubMenu) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Hiệu ứng bo góc nổi bật khi chọn (Active) hoặc Hover
                if (this == activeButton) {
                    g2.setColor(SIDEBAR_ACTIVE);
                    g2.fillRoundRect(12, 2, getWidth() - 24, getHeight() - 4, 12, 12);
                } else if (getModel().isRollover() && isEnabled()) {
                    g2.setColor(SIDEBAR_HOVER);
                    g2.fillRoundRect(12, 2, getWidth() - 24, getHeight() - 4, 12, 12);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", isSubMenu ? Font.PLAIN : Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(280, 42));
        btn.setPreferredSize(new Dimension(280, 42));

        // Căn lề text thục vào trong
        btn.setBorder(BorderFactory.createEmptyBorder(0, isSubMenu ? 45 : 25, 0, 0));

        // Tắt fill nền mặc định để custom paint hoạt động
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private void updateActiveButton(JButton newBtn) {
        JButton oldBtn = activeButton;
        activeButton = newBtn;

        // Gọi repaint để kích hoạt hiệu ứng vẽ lại màu nút
        if (oldBtn != null) oldBtn.repaint();
        if (activeButton != null) activeButton.repaint();
    }

    private JLabel createSideTitle(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_LIGHT);
        // Đã ép padding top về 0 để ôm sát với separator
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 20, 8, 0));
        return lbl;
    }

    public void showTab(JPanel tabPanel) {
        if (currentTabPanel != null) {
            contentPanel.remove(currentTabPanel);
        }
        currentTabPanel = tabPanel;
        contentPanel.add(currentTabPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public TAB_BanVe getTabBanVe() {
        return this.tab_BanVe;
    }

    private void addUserInfoSection(JPanel sidebar, NhanVien nv) {
        JPanel pnlUser = new JPanel();
        pnlUser.setLayout(new BoxLayout(pnlUser, BoxLayout.Y_AXIS));
        pnlUser.setOpaque(false);
        pnlUser.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 10));

        JLabel lblName = new JLabel(nv.getTenNV());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblName.setForeground(Color.WHITE);
        lblName.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(GUI_General.this);
                GD_ThongTinCaNhan dialog = new GD_ThongTinCaNhan(parentFrame, nv);
                dialog.setVisible(true);
                lblName.setText(nv.getTenNV());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblName.setForeground(SIDEBAR_ACTIVE); // Highlight nhẹ khi trỏ chuột
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblName.setForeground(Color.WHITE);
            }
        });

        JLabel lblRole = new JLabel(nv.getChucVu().getLabel());
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRole.setForeground(new Color(200, 215, 240));

        pnlUser.add(lblName);
        pnlUser.add(Box.createVerticalStrut(4));
        pnlUser.add(lblRole);
        sidebar.add(pnlUser);

        // Kẻ vạch ngăn cách User và Menu với chiều cao ép cứng
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 50));
        sep.setBackground(new Color(0, 0, 0, 0));
        sep.setMaximumSize(new Dimension(240, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(sep);

        // Tạo khoảng cách đúng 10px từ vạch tới dòng CHỨC NĂNG CHÍNH
        sidebar.add(Box.createVerticalStrut(10));
    }

    // =====================================================================
    // HÀM TẠO HEADER (Viền mờ, nút đăng xuất bo góc đẹp)
    // =====================================================================
    private JPanel createHeader(NhanVien nv) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 65));

        // Viền nhạt dưới header
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JPanel pnlLogoTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        pnlLogoTitle.setOpaque(false);

        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/com/img/logo.png"));
            Image scaledLogo = logoIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            JLabel lblLogo = new JLabel(new ImageIcon(scaledLogo));
            pnlLogoTitle.add(lblLogo);
        } catch (Exception e) {
            // Fallback nếu không tải được logo
        }

        JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ BÁN VÉ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(0x1E2B3C)); // TEXT_DARK
        pnlLogoTitle.add(lblTitle);
        header.add(pnlLogoTitle, BorderLayout.WEST);

        // --- NÚT ĐĂNG XUẤT BO GÓC ---
        JButton btnLogout = new JButton("Đăng xuất") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(200, 35, 51) : new Color(220, 53, 69));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // Bo góc mềm 10px
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogout.setPreferredSize(new Dimension(110, 38));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất không?",
                    "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                Window currentWindow = SwingUtilities.getWindowAncestor(this);
                if (currentWindow != null) {
                    currentWindow.dispose();
                }

                SwingUtilities.invokeLater(() -> {
                    JFrame loginFrame = new JFrame("Hệ thống quản lý bán vé ga tàu - Đăng nhập");
                    GUI_Login loginPanel = new GUI_Login();
                    loginPanel.setParentFrame(loginFrame);
                    loginFrame.setContentPane(loginPanel);
                    loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    loginFrame.pack();
                    loginFrame.setResizable(false);
                    loginFrame.setLocationRelativeTo(null);
                    loginFrame.setVisible(true);
                });
            }
        });

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 13));
        pnlRight.setOpaque(false);
        pnlRight.add(btnLogout);
        header.add(pnlRight, BorderLayout.EAST);

        return header;
    }
}