//package com.gui;
//
//import com.dao.DAO_NhanVien;
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import java.awt.*;
//import java.awt.event.*;
//
//public class GUI_Register extends JPanel {
//	private JTextField txtFullName, txtSDT, txtEmail, txtUsername;
//	private JPasswordField txtPassword, txtConfirmPass;
//	private JRadioButton rbAdmin, rbStaff;
//	private JButton btnRegister;
//	private Image backgroundImage;
//	private DAO_NhanVien nv_dao;
//
//	public GUI_Register() {
//		// 1. Tải ảnh nền
//		try {
//			java.net.URL imgURL = getClass().getResource("/com/img/doantau.png");
//			if (imgURL != null) {
//				backgroundImage = new ImageIcon(imgURL).getImage();
//			}
//		} catch (Exception e) {
//			System.err.println("Không tìm thấy ảnh: /com/img/doantau.png");
//		}
//
//		this.nv_dao = new DAO_NhanVien(com.connectDB.ConnectDB.getConnection());
//		setLayout(new GridBagLayout());
//
//		// ================= FORM PANEL (Mờ trong suốt) =================
//		JPanel registerPanel = new JPanel(new GridBagLayout()) {
//			@Override
//			protected void paintComponent(Graphics g) {
//				Graphics2D g2d = (Graphics2D) g;
//				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//				g2d.setColor(new Color(0, 0, 0, 190)); 
//				g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
//				super.paintComponent(g);
//			}
//		};
//		registerPanel.setOpaque(false);
//		registerPanel.setPreferredSize(new Dimension(420, 680)); // Đã chỉnh lại kích thước cho gọn
//		registerPanel.setBorder(new EmptyBorder(25, 40, 25, 40));
//
//		GridBagConstraints gc = new GridBagConstraints();
//		gc.fill = GridBagConstraints.HORIZONTAL;
//		gc.insets = new Insets(2, 0, 2, 0);
//		gc.gridx = 0;
//
//		// --- TIÊU ĐỀ ---
//		JLabel lblTitle = new JLabel("GA TÀU SÀI GÒN", SwingConstants.CENTER);
//		lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
//		lblTitle.setForeground(new Color(255, 193, 7));
//
//		JLabel lblSub = new JLabel("ĐĂNG KÝ NHÂN VIÊN MỚI", SwingConstants.CENTER);
//		lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
//		lblSub.setForeground(Color.WHITE);
//
//		// --- KHỞI TẠO FIELDS (Đã bỏ txtMaNV) ---
//		txtFullName = createStyledField();
//		txtSDT = createStyledField();
//		txtEmail = createStyledField();
//		txtUsername = createStyledField();
//		txtPassword = createStyledPassField();
//		txtConfirmPass = createStyledPassField();
//
//		// --- RÀNG BUỘC FOCUS ---
//		txtFullName.addFocusListener(new FocusAdapter() {
//			public void focusLost(FocusEvent e) {
//				txtFullName.setText(capitalizeWords(txtFullName.getText()));
//			}
//		});
//		txtEmail.addFocusListener(new FocusAdapter() {
//			public void focusLost(FocusEvent e) {
//				String mail = txtEmail.getText().trim();
//				if (!mail.isEmpty() && !mail.contains("@"))
//					txtEmail.setText(mail + "@gmail.com");
//			}
//		});
//
//		// Điều hướng Enter
//		setupNavigation();
//
//		// --- LAYOUT ---
//		gc.gridy = 0;
//		registerPanel.add(lblTitle, gc);
//		gc.gridy = 1;
//		gc.insets = new Insets(0, 0, 25, 0);
//		registerPanel.add(lblSub, gc);
//
//		gc.insets = new Insets(5, 0, 2, 0);
//		addComp(registerPanel, "Họ tên nhân viên:", txtFullName, gc, 2);
//		addComp(registerPanel, "Số điện thoại:", txtSDT, gc, 3);
//		addComp(registerPanel, "Email:", txtEmail, gc, 4);
//		addComp(registerPanel, "Tên tài khoản:", txtUsername, gc, 5);
//		addComp(registerPanel, "Mật khẩu:", txtPassword, gc, 6);
//		addComp(registerPanel, "Xác nhận mật khẩu:", txtConfirmPass, gc, 7);
//
//		// Chức vụ
//		rbAdmin = new JRadioButton("Quản lý");
//		rbStaff = new JRadioButton("Nhân viên", true);
//		rbAdmin.setForeground(Color.WHITE);
//		rbStaff.setForeground(Color.WHITE);
//		rbAdmin.setOpaque(false);
//		rbStaff.setOpaque(false);
//		ButtonGroup group = new ButtonGroup();
//		group.add(rbAdmin);
//		group.add(rbStaff);
//		JPanel pnlRole = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		pnlRole.setOpaque(false);
//		pnlRole.add(rbAdmin);
//		pnlRole.add(rbStaff);
//		gc.gridy = 16;
//		registerPanel.add(pnlRole, gc);
//
//		// Nút Đăng ký
//		btnRegister = new JButton("ĐĂNG KÝ");
//		styleButton(btnRegister);
//		btnRegister.addActionListener(e -> handleRegister());
//		gc.gridy = 17;
//		gc.insets = new Insets(15, 0, 10, 0);
//		registerPanel.add(btnRegister, gc);
//
//		add(registerPanel);
//	}
//
//	@Override
//	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		if (backgroundImage != null) {
//			g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
//			g.setColor(new Color(0, 0, 0, 60)); 
//			g.fillRect(0, 0, getWidth(), getHeight());
//		}
//	}
//
//	private void handleRegister() {
//		String name = txtFullName.getText().trim();
//		String sdt = txtSDT.getText().trim();
//		String email = txtEmail.getText().trim();
//		String user = txtUsername.getText().trim();
//		String pass = new String(txtPassword.getPassword());
//		String confirm = new String(txtConfirmPass.getPassword());
//
//		// Kiểm tra trống
//		if (name.isEmpty() || user.isEmpty() || sdt.isEmpty()) {
//			showError("Vui lòng điền đầy đủ các thông tin bắt buộc!");
//			return;
//		}
//		if (!sdt.matches("\\d{10}")) {
//			showError("Số điện thoại phải đúng 10 chữ số!");
//			return;
//		}
//		if (!pass.equals(confirm)) {
//			showError("Xác nhận mật khẩu không khớp!");
//			return;
//		}
//
//		java.sql.Date ngayHT = new java.sql.Date(System.currentTimeMillis());
//
//		// GỌI DAO: Đảm bảo hàm create trong DAO nhận 9 tham số (BỎ MA)
//		if (nv_dao.create(name, sdt, email, user, pass, rbAdmin.isSelected() ? "Quản lý" : "Nhân viên", 1, 0, ngayHT)) {
//			JOptionPane.showMessageDialog(this, "Đăng ký thành công nhân viên: " + name);
//			clearFields();
//		} else {
//			showError("Thất bại! Tài khoản đã tồn tại hoặc lỗi kết nối.");
//		}
//	}
//
//	private void setupNavigation() {
//		JTextField[] fields = { txtFullName, txtSDT, txtEmail, txtUsername, txtPassword, txtConfirmPass };
//		for (int i = 0; i < fields.length; i++) {
//			final int index = i;
//			fields[i].addActionListener(e -> {
//				if (index < fields.length - 1)
//					fields[index + 1].requestFocus();
//				else
//					btnRegister.doClick();
//			});
//		}
//	}
//
//	private void clearFields() {
//		txtFullName.setText("");
//		txtSDT.setText("");
//		txtEmail.setText("");
//		txtUsername.setText("");
//		txtPassword.setText("");
//		txtConfirmPass.setText("");
//	}
//
//	private void addComp(JPanel p, String title, JComponent comp, GridBagConstraints gc, int row) {
//		JLabel lbl = new JLabel(title);
//		lbl.setForeground(new Color(180, 180, 180));
//		lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
//		gc.gridy = row * 2;
//		p.add(lbl, gc);
//		gc.gridy = row * 2 + 1;
//		p.add(comp, gc);
//	}
//
//	private JTextField createStyledField() {
//		JTextField tf = new JTextField(20);
//		tf.setBackground(new Color(255, 255, 255, 240));
//		tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 193, 7)),
//				BorderFactory.createEmptyBorder(8, 10, 8, 10)));
//		return tf;
//	}
//
//	private JPasswordField createStyledPassField() {
//		JPasswordField pf = new JPasswordField(20);
//		pf.setBackground(new Color(255, 255, 255, 240));
//		pf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 193, 7)),
//				BorderFactory.createEmptyBorder(8, 10, 8, 10)));
//		return pf;
//	}
//
//	private void styleButton(JButton btn) {
//		btn.setBackground(new Color(255, 193, 7));
//		btn.setForeground(Color.BLACK);
//		btn.setFont(new Font("SansSerif", Font.BOLD, 14));
//		btn.setPreferredSize(new Dimension(0, 45));
//		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
//	}
//
//	private String capitalizeWords(String str) {
//		if (str == null || str.isEmpty()) return str;
//		String[] words = str.toLowerCase().split("\\s+");
//		StringBuilder sb = new StringBuilder();
//		for (String w : words)
//			if (w.length() > 0)
//				sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
//		return sb.toString().trim();
//	}
//
//	private void showError(String msg) {
//		JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
//	}
//
//	public static void main(String[] args) {
//		SwingUtilities.invokeLater(() -> {
//			JFrame f = new JFrame("Hệ Thống Ga Tàu Sài Gòn - Đăng Ký");
//			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			f.add(new GUI_Register());
//			f.pack(); 
//			f.setLocationRelativeTo(null);
//			f.setVisible(true);
//		});
//	}
//}