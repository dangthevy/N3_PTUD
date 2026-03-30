package com.gui;

import javax.swing.*;
import com.entities.HoaDon;
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

	private final Color SIDEBAR_BG = new Color(0, 125, 250);
	private final Color SIDEBAR_ACTIVE = new Color(0, 90, 200);
	private final Color SUB_MENU_BG = new Color(0, 115, 240);

	public GUI_General(NhanVien nv) {
		initComponents(nv);
	}

	private void initComponents(NhanVien nv) {
		setLayout(new BorderLayout());

		// Khởi tạo các Tab
		tab_Dashboard = new TAB_Dashboard(this);
		tab_BanVe = new TAB_BanVe();
		tab_HoanVe = new TAB_HoanVe();
		tab_ThanhToanLapHD = new TAB_ThanhToanLapHD();
		tab_Tau = new TAB_Tau();
		tab_Toa_ChoNgoi = new TAB_Toa_ChoNgoi();
		tab_Ga_Tuyen = new TAB_Ga_Tuyen();
		tab_LichTrinh_ChuyenTau = new TAB_LichTrinh_ChuyenTau();
		tab_Gia = new TAB_Gia();
		tab_QLNhanVien = new TAB_QLNhanVien();
		tab_QLKhachHang = new TAB_QLKhachHang();
		tab_KhuyenMai = new TAB_KhuyenMai();
		tab_ThongKeDoanhThu = new TAB_ThongKeDoanhThu();
		tab_ThongKeVe = new TAB_ThongKeVe();

		// 1. SIDEBAR PANEL
		JPanel sidebarPanel = new JPanel();
		sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
		sidebarPanel.setBackground(SIDEBAR_BG);
		sidebarPanel.setPreferredSize(new Dimension(280, 0));

		addUserInfoSection(sidebarPanel, nv);
		addTabButtons(sidebarPanel, nv);

		add(sidebarPanel, BorderLayout.WEST);

		// 2. RIGHT PANEL
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(createHeader(nv), BorderLayout.NORTH);

		contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBackground(Color.WHITE);
		showTab(tab_Dashboard);

		rightPanel.add(contentPanel, BorderLayout.CENTER);
		add(rightPanel, BorderLayout.CENTER);
	}

	private void addTabButtons(JPanel sidebar, NhanVien nv) {
		// Kiểm tra chức vụ (phân biệt Quản lý và Nhân viên)
        boolean isAdmin = nv.getChucVu() == ChucVu.QUANLY
                || nv.getChucVu() == ChucVu.ADMIN;

		// CHỨC NĂNG CHÍNH - Cả 2 đều có quyền
		sidebar.add(createSideTitle("CHỨC NĂNG CHÍNH"));
		addTabButton(sidebar, "Màn hình chính", tab_Dashboard, true);
		addTabButton(sidebar, "Bán vé", tab_BanVe, true);
		addTabButton(sidebar, "Hoàn vé", tab_HoanVe, true);
		addTabButton(sidebar, "Thanh toán & Lập hóa đơn", tab_ThanhToanLapHD, true);

		// QUẢN LÝ HỆ THỐNG
		sidebar.add(createSideTitle("QUẢN LÝ HỆ THỐNG"));

		// Đoàn tàu, Lịch trình, Khách hàng, Khuyến mãi: Nhân viên & Quản lý đều được
		// vào
		addDropdownMenu(sidebar, "Quản lý Đoàn tàu", new String[] { "Tàu", "Toa & Chỗ ngồi" },
				new JPanel[] { tab_Tau, tab_Toa_ChoNgoi }, true);

		addDropdownMenu(sidebar, "Lịch trình & Giá", new String[] { "Ga & Tuyến", "Lịch trình & Chuyến", "Bảng Giá" },
				new JPanel[] { tab_Ga_Tuyen, tab_LichTrinh_ChuyenTau, tab_Gia }, true);

		// Quản lý Nhân viên: CHỈ Quản lý được vào
		addTabButton(sidebar, "Quản lý Nhân viên", tab_QLNhanVien, isAdmin);

		addTabButton(sidebar, "Quản lý Khách hàng", tab_QLKhachHang, true);
		addTabButton(sidebar, "Khuyến mãi", tab_KhuyenMai, true);

		// BÁO CÁO - CHỈ Quản lý được vào
		sidebar.add(createSideTitle("BÁO CÁO"));
		addDropdownMenu(sidebar, "Thống kê", new String[] { "Doanh thu", "Lượng vé" },
				new JPanel[] { tab_ThongKeDoanhThu, tab_ThongKeVe }, isAdmin);

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
			btn.setForeground(new Color(200, 200, 200));
			btn.setToolTipText("Chức năng này chỉ dành cho Quản lý");
		}

		sidebar.add(btn);
		if (title.equals("Màn hình chính")) {
			activeButton = btn;
			btn.setBackground(SIDEBAR_ACTIVE);
		}
	}

	private void addDropdownMenu(JPanel sidebar, String title, String[] subTitles, JPanel[] targets,
			boolean canAccess) {
		JButton parentBtn = createStyledButton("▼ " + title, false);
		parentBtn.setEnabled(canAccess);

		JPanel subMenuPanel = new JPanel();
		subMenuPanel.setLayout(new BoxLayout(subMenuPanel, BoxLayout.Y_AXIS));
		subMenuPanel.setOpaque(false);
		subMenuPanel.setVisible(false);

		if (canAccess) {
			for (int i = 0; i < subTitles.length; i++) {
				final int index = i;
				JButton subBtn = createStyledButton("      " + subTitles[i], true);
				subBtn.addActionListener(e -> {
					updateActiveButton(parentBtn);
					showTab(targets[index]);
				});
				subMenuPanel.add(subBtn);
			}

			parentBtn.addActionListener(e -> {
				subMenuPanel.setVisible(!subMenuPanel.isVisible());
				sidebar.revalidate();
			});
		} else {
			parentBtn.setForeground(new Color(200, 200, 200));
		}

		sidebar.add(parentBtn);
		sidebar.add(subMenuPanel);
	}

	private JButton createStyledButton(String text, boolean isSubMenu) {
		JButton btn = new JButton(text);
		btn.setFont(new Font("Segoe UI", isSubMenu ? Font.PLAIN : Font.BOLD, 16));
		btn.setForeground(Color.WHITE);
		btn.setBackground(isSubMenu ? SUB_MENU_BG : SIDEBAR_BG);
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		btn.setMaximumSize(new Dimension(280, 50));
		btn.setPreferredSize(new Dimension(280, 50));
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

		btn.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				if (btn.isEnabled() && btn != activeButton)
					btn.setBackground(SIDEBAR_ACTIVE.brighter());
			}

			public void mouseExited(MouseEvent e) {
				if (btn.isEnabled() && btn != activeButton)
					btn.setBackground(isSubMenu ? SUB_MENU_BG : SIDEBAR_BG);
				else if (btn.isEnabled())
					btn.setBackground(SIDEBAR_ACTIVE);
			}
		});
		return btn;
	}

	private void updateActiveButton(JButton newBtn) {
		if (activeButton != null) {
			activeButton.setBackground(activeButton.getText().startsWith("      ") ? SUB_MENU_BG : SIDEBAR_BG);
		}
		activeButton = newBtn;
		activeButton.setBackground(SIDEBAR_ACTIVE);
	}

	private JLabel createSideTitle(String title) {
		JLabel lbl = new JLabel("  " + title);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lbl.setForeground(new Color(180, 210, 255));
		lbl.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 0));
		return lbl;
	}

	public void showTab(JPanel tabPanel) {
		if (currentTabPanel != null)
			contentPanel.remove(currentTabPanel);
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
		pnlUser.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));

		JLabel lblName = new JLabel(nv.getTenNV());
		lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
		lblName.setForeground(Color.WHITE);

		JLabel lblRole = new JLabel(nv.getChucVu().getLabel());
		lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblRole.setForeground(new Color(220, 230, 255));

		pnlUser.add(lblName);
		pnlUser.add(lblRole);
		sidebar.add(pnlUser);

		JSeparator sep = new JSeparator();
		sep.setMaximumSize(new Dimension(240, 1));
		sidebar.add(sep);
	}

	private JPanel createHeader(NhanVien nv) {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(Color.WHITE);
		header.setPreferredSize(new Dimension(0, 60));
		header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

		JPanel pnlLogoTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		pnlLogoTitle.setOpaque(false);

		try {
			ImageIcon logoIcon = new ImageIcon(getClass().getResource("/com/img/logo.png"));
			Image scaledLogo = logoIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
			JLabel lblLogo = new JLabel(new ImageIcon(scaledLogo));
			pnlLogoTitle.add(lblLogo);
		} catch (Exception e) {
		}

		JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ BÁN VÉ");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
		lblTitle.setForeground(SIDEBAR_BG);
		pnlLogoTitle.add(lblTitle);

		header.add(pnlLogoTitle, BorderLayout.WEST);

		JButton btnLogout = new JButton("Đăng xuất");
		btnLogout.setBackground(new Color(220, 53, 69));
		btnLogout.setForeground(Color.WHITE);
		btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
		btnLogout.setFocusPainted(false);
		btnLogout.addActionListener(e -> {
			if (JOptionPane.showConfirmDialog(this, "Bạn có muốn đăng xuất?") == 0)
				System.exit(0);
		});

		JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 12));
		pnlRight.setOpaque(false);
		pnlRight.add(btnLogout);
		header.add(pnlRight, BorderLayout.EAST);

		return header;
	}
}