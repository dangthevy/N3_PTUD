package com.gui;

import com.service.TabStyler;
import com.dao.DAO_NhanVien;
import com.entities.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class GUI_Login extends JPanel implements ActionListener {

	private JFrame parentFrame;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JButton btnLogin;

	private DAO_NhanVien nv_dao = new DAO_NhanVien();

	private JLabel lblUserError;
	private JLabel lblPassError;
	private Image backgroundImage;

	private int failedAttempts = 0;
	private boolean isPermanentlyLocked = false;
	private Timer countdownTimer;

	public GUI_Login() {
		try {
			java.net.URL imgURL = getClass().getResource("/com/img/doantau.png");
			if (imgURL != null) {
				backgroundImage = new ImageIcon(imgURL).getImage();
			}
		} catch (Exception e) {
			System.err.println("Không tìm thấy ảnh: /com/img/doantau.png");
		}

		setLayout(new GridBagLayout());

		JPanel loginPanel = new JPanel(new GridBagLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(new Color(0, 0, 0, 190));
				g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
				super.paintComponent(g);
			}
		};
		loginPanel.setOpaque(false);
		loginPanel.setPreferredSize(new Dimension(420, 520));
		loginPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(5, 0, 5, 0);
		gc.gridx = 0;

		JLabel lblApp = new JLabel("GA TÀU SÀI GÒN", SwingConstants.CENTER);
		lblApp.setFont(new Font("SansSerif", Font.BOLD, 26));
		lblApp.setForeground(new Color(255, 193, 7));

		JLabel lblLoginNow = new JLabel("LOGIN NOW", SwingConstants.CENTER);
		lblLoginNow.setFont(new Font("SansSerif", Font.PLAIN, 14));
		lblLoginNow.setForeground(Color.WHITE);

		txtUsername = createStyledTextField();
		txtPassword = createStyledPasswordField();
		lblUserError = createErrorLabel();
		lblPassError = createErrorLabel();

		// ================= TIỆN ÍCH KIỂM TRA USERNAME NGAY LẬP TỨC =================
		txtUsername.addKeyListener(new KeyAdapter() {
		    @Override
		    public void keyReleased(KeyEvent e) {
		        String user = txtUsername.getText().trim();
		        
		        // Nếu ô trống thì hiện nhắc nhở bình thường
		        if (user.isEmpty()) {
		            lblUserError.setText("Username không được để trống!");
		            updateFieldBorder(txtUsername, Color.RED);
		            return;
		        }

		        // Kiểm tra trực tiếp trong DB khi đang gõ
		        // Lưu ý: Chỉ nên kiểm tra khi độ dài User đủ lớn (ví dụ > 3 ký tự) để tránh truy vấn DB quá nhiều
		        if (user.length() >= 3) { 
		            if (!nv_dao.isIdExists(user)) {
		                lblUserError.setText("Tài khoản này không tồn tại!");
		                lblUserError.setForeground(new Color(255, 110, 110));
		                updateFieldBorder(txtUsername, Color.RED);
		            } else {
		                lblUserError.setText("Tài khoản hợp lệ.");
		                lblUserError.setForeground(new Color(100, 255, 100));
		                updateFieldBorder(txtUsername, new Color(100, 255, 100));
		            }
		        }
		    }
		});
		// Xóa thông báo lỗi khi bắt đầu gõ lại
		txtUsername.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				lblUserError.setText(" ");
				updateFieldBorder(txtUsername, new Color(255, 193, 7));
			}
		});

		btnLogin = new JButton("ĐĂNG NHẬP");
		styleButton(btnLogin);
		btnLogin.addActionListener(e -> handleLogin());

		txtUsername.addActionListener(e -> txtPassword.requestFocus());
		txtPassword.addActionListener(e -> btnLogin.doClick());

		gc.gridy = 0;
		loginPanel.add(lblApp, gc);
		gc.gridy = 1;
		gc.insets = new Insets(0, 0, 30, 0);
		loginPanel.add(lblLoginNow, gc);

		gc.gridy = 2;
		gc.insets = new Insets(5, 0, 2, 0);
		JLabel uLabel = new JLabel("Username");
		uLabel.setForeground(Color.LIGHT_GRAY);
		loginPanel.add(uLabel, gc);
		gc.gridy = 3;
		loginPanel.add(txtUsername, gc);
		gc.gridy = 4;
		loginPanel.add(lblUserError, gc);

		gc.gridy = 5;
		JLabel pLabel = new JLabel("Password");
		pLabel.setForeground(Color.LIGHT_GRAY);
		loginPanel.add(pLabel, gc);
		gc.gridy = 6;
		loginPanel.add(txtPassword, gc);
		gc.gridy = 7;
		loginPanel.add(lblPassError, gc);

		gc.gridy = 8;
		gc.insets = new Insets(20, 0, 10, 0);
		loginPanel.add(btnLogin, gc);

		add(loginPanel);
	}

	private void handleLogin() {
	    // 1. Kiểm tra trạng thái khóa hệ thống trước tiên
	    if (isPermanentlyLocked) {
	        showPermanentLockAlert();
	        return;
	    }

	    String username = txtUsername.getText().trim();
	    String password = new String(txtPassword.getPassword()).trim();

	    // 2. Kiểm tra các trường để trống (Validation)
	    if (username.isEmpty()) {
	        lblUserError.setText("Username không được để trống!");
	        updateFieldBorder(txtUsername, Color.RED);
	        txtUsername.requestFocus();
	        return;
	    }
	    
	    if (password.isEmpty()) {
	        lblPassError.setText("Password không được để trống!");
	        updateFieldBorder(txtPassword, Color.RED);
	        txtPassword.requestFocus();
	        return;
	    }

	    // 3. Kiểm tra nhãn lỗi ID (Nếu focusLost hoặc KeyListener đã báo lỗi trước đó)
	    // Điều này ngăn chặn việc gửi yêu cầu đăng nhập khi ID chắc chắn sai
	    if (lblUserError.getText().equals("Tài khoản này không tồn tại!")) {
	        txtUsername.requestFocus();
	        return;
	    }

	    // 4. Thực hiện xác thực với Database
	    if (nv_dao.checkLogin(username, password)) {
	        // ĐĂNG NHẬP THÀNH CÔNG
	        NhanVien nv = nv_dao.getNhanVienById(username);
	        failedAttempts = 0; // Reset số lần sai về 0
	        
	        // Hiển thị thông báo chào mừng nhẹ nhàng nếu muốn
	        // JOptionPane.showMessageDialog(this, "Chào mừng " + nv.getTenNV() + " quay trở lại!");
	        
	        openMainWindow(nv);
	    } else {
	        // ĐĂNG NHẬP THẤT BẠI
	        failedAttempts++; // Tăng số lần thử sai
	        processFailedAttempt(); // Gọi hàm xử lý thông báo lỗi hoặc đếm ngược khóa
	    }
	}

	// Các hàm xử lý Khóa và Timer giữ nguyên như cũ
	private void processFailedAttempt() {
		if (failedAttempts == 3) {
			JOptionPane.showMessageDialog(this, "Sai 3 lần! Hệ thống sẽ khóa tạm thời 5 phút.", "Cảnh báo",
					JOptionPane.WARNING_MESSAGE);
			startCountdown(5);
		} else if (failedAttempts >= 6) {
			isPermanentlyLocked = true;
			if (countdownTimer != null)
				countdownTimer.stop();
			showPermanentLockAlert();
		} else {
			lblPassError.setText("Sai tài khoản hoặc mật khẩu! (Lần " + failedAttempts + ")");
			txtPassword.setText("");
			txtPassword.requestFocus();
		}
	}

	private void startCountdown(int minutes) {
		btnLogin.setEnabled(false);
		txtUsername.setEditable(false);
		txtPassword.setEditable(false);
		final int[] secondsLeft = { minutes * 60 };
		countdownTimer = new Timer(1000, e -> {
			secondsLeft[0]--;
			if (secondsLeft[0] > 0) {
				int m = secondsLeft[0] / 60;
				int s = secondsLeft[0] % 60;
				btnLogin.setText(String.format("Thử lại sau (%02d:%02d)", m, s));
			} else {
				((Timer) e.getSource()).stop();
				btnLogin.setEnabled(true);
				btnLogin.setText("ĐĂNG NHẬP");
				txtUsername.setEditable(true);
				txtPassword.setEditable(true);
				lblPassError.setText(" ");
			}
		});
		countdownTimer.start();
	}

	private void showPermanentLockAlert() {
		JOptionPane.showMessageDialog(this,
				"Màn hình đã bị khóa vĩnh viễn!\nHãy liên hệ quản lý để được mở khóa.\nSĐT: 0859495852",
				"KHÓA HỆ THỐNG", JOptionPane.ERROR_MESSAGE);
		txtUsername.setEnabled(false);
		txtPassword.setEnabled(false);
		btnLogin.setEnabled(false);
	}

	// Hàm Helper để cập nhật Border nhanh
	private void updateFieldBorder(JTextField field, Color color) {
		field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color, 1),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
	}

	private JTextField createStyledTextField() {
		JTextField tf = new JTextField(15);
		tf.setBackground(new Color(255, 255, 255, 240));
		updateFieldBorder(tf, new Color(255, 193, 7));
		return tf;
	}

	private JPasswordField createStyledPasswordField() {
		JPasswordField pf = new JPasswordField(15);
		pf.setBackground(new Color(255, 255, 255, 240));
		updateFieldBorder(pf, new Color(255, 193, 7));
		return pf;
	}

	private JLabel createErrorLabel() {
		JLabel lbl = new JLabel(" ");
		lbl.setForeground(new Color(255, 100, 100));
		lbl.setFont(new Font("Arial", Font.ITALIC, 12));
		return lbl;
	}

	private void styleButton(JButton btn) {
		btn.setFont(new Font("SansSerif", Font.BOLD, 14));
		btn.setBackground(new Color(255, 193, 7));
		btn.setForeground(Color.BLACK);
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.setPreferredSize(new Dimension(0, 45));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (backgroundImage != null) {
			g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
			g.setColor(new Color(0, 0, 0, 60));
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}

	private void openMainWindow(NhanVien nv) {
		Window w = SwingUtilities.getWindowAncestor(this);
		SwingUtilities.invokeLater(() -> {
			JFrame f = new JFrame("Hệ thống quản lý bán vé ga tàu");
			f.setContentPane(new GUI_General(nv)); // Truyền nhân viên vào đây
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setExtendedState(JFrame.MAXIMIZED_BOTH);
			f.setVisible(true);
			if (w instanceof JFrame)
				w.dispose();
		});
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Ga Tàu Sài Gòn - Đăng nhập");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(1000, 700);
			frame.setContentPane(new GUI_Login());
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}

	public void setParentFrame(JFrame frame) {
		this.parentFrame = frame;
	}
}