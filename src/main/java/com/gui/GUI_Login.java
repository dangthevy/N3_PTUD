package com.gui;

import com.dao.DAO_NhanVien;
import com.entities.NhanVien;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.Properties;
import java.util.Random;
import java.util.prefs.Preferences;
import javax.mail.*;
import java.security.MessageDigest;
import javax.mail.internet.*;

public class GUI_Login extends JPanel implements ActionListener {

	// ── Màu đồng bộ toàn hệ thống (giống TAB_ThanhToanLapHD, TAB_TraCuuVe, v.v.) ──
	private static final Color ACCENT      = new Color(0x1A5EAB);
	private static final Color ACCENT_HVR  = new Color(0x154D8F);
	private static final Color ACCENT_DARK = new Color(0x0D3570);
	private static final Color BG_PAGE     = new Color(0xF1F5FB);   // nền phải
	private static final Color BG_CARD     = Color.WHITE;
	private static final Color BORDER_CLR  = new Color(0xCBDCF0);
	private static final Color TEXT_DARK   = new Color(0x1A2B45);
	private static final Color TEXT_MID    = new Color(0x5A7499);
	private static final Color TEXT_MUTED  = new Color(0xADB5BD);
	private static final Color ERROR_CLR   = new Color(0xDC2626);
	private static final Color ROW_SEL     = new Color(0xDCEAF8);

	// Fonts đồng bộ
	private static final Font F_TITLE  = new Font("Segoe UI", Font.BOLD, 30);
	private static final Font F_LABEL  = new Font("Segoe UI", Font.BOLD, 13);
	private static final Font F_CELL   = new Font("Segoe UI", Font.PLAIN, 14);
	private static final Font F_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);

	private JFrame parentFrame;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JToggleButton btnShowPass;
	private JButton btnLogin;

	private DAO_NhanVien nv_dao;
	private JLabel lblUserError;
	private JLabel lblPassError;
	private Image backgroundImage;

	private int failedAttempts = 0;
	private boolean isPermanentlyLocked = false;
	private Timer countdownTimer;

	private Preferences prefs = Preferences.userNodeForPackage(GUI_Login.class);
	private final String HISTORY_KEY = "login_history";

	private JPopupMenu suggestionMenu;
	private JList<String> listSuggestions;
	private String currentOtpGenerated;

	// ── Các hình thoi trang trí (vẽ trên panel trái) ──
	private static final int NUM_SHAPES = 14;
	private final float[] shapeX   = new float[NUM_SHAPES];
	private final float[] shapeY   = new float[NUM_SHAPES];
	private final float[] shapeS   = new float[NUM_SHAPES];   // kích thước
	private final float[] shapeA   = new float[NUM_SHAPES];   // góc xoay
	private final int[]   shapeAlp = new int[NUM_SHAPES];

	public GUI_Login() {
		// Load ảnh nền
		try {
			java.net.URL imgURL = getClass().getResource("/com/img/doantau2.png");
			if (imgURL != null)
				backgroundImage = new ImageIcon(imgURL).getImage();
		} catch (Exception e) {
			System.err.println("Không tìm thấy ảnh: /com/img/doantau2.png");
		}

		// Khởi tạo hình trang trí
		Random rng = new Random(42);
		for (int i = 0; i < NUM_SHAPES; i++) {
			shapeX[i]   = rng.nextFloat();
			shapeY[i]   = rng.nextFloat();
			shapeS[i]   = 30 + rng.nextFloat() * 90;
			shapeA[i]   = rng.nextFloat() * 45;
			shapeAlp[i] = 18 + rng.nextInt(30);
		}

		this.nv_dao = new DAO_NhanVien(com.connectDB.ConnectDB.getConnection());

		// Layout 50/50 — khong gap, khong vien
		setLayout(new GridLayout(1, 2, 0, 0));
		setPreferredSize(new Dimension(800, 460));

		// ════════════════════════════════════════
		// PANEL TRÁI — ảnh full + overlay thông tin
		// ════════════════════════════════════════
		JPanel leftPanel = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
				int pw = getWidth(), ph = getHeight();
				if (backgroundImage != null) {
					int iw = backgroundImage.getWidth(this);
					int ih = backgroundImage.getHeight(this);
					double scale = Math.max((double) pw / iw, (double) ph / ih);
					int dw = (int)(iw * scale), dh = (int)(ih * scale);
					int dx = (pw - dw) / 2,     dy = (ph - dh) / 2;
					g2.drawImage(backgroundImage, dx, dy, dw, dh, this);
					// Overlay toi
					g2.setColor(new Color(8, 24, 58, 170));
					g2.fillRect(0, 0, pw, ph);
					// Gradient day len
					GradientPaint gpBottom = new GradientPaint(
						0, ph * 0.45f, new Color(13, 53, 112, 0),
						0, ph,         new Color(13, 53, 112, 210));
					g2.setPaint(gpBottom);
					g2.fillRect(0, 0, pw, ph);
				} else {
					GradientPaint gp = new GradientPaint(0, 0, ACCENT_DARK, pw, ph, ACCENT);
					g2.setPaint(gp); g2.fillRect(0, 0, pw, ph);
				}
				// Hinh thoi trang tri
				for (int i = 0; i < NUM_SHAPES; i++) {
					g2.setColor(new Color(255, 255, 255, shapeAlp[i]));
					int x = (int)(shapeX[i] * pw), y = (int)(shapeY[i] * ph), s = (int) shapeS[i];
					java.awt.geom.AffineTransform old = g2.getTransform();
					g2.rotate(Math.toRadians(shapeA[i] + 45), x, y);
					g2.setStroke(new BasicStroke(1.2f));
					g2.drawRoundRect(x - s/2, y - s/2, s, s, 6, 6);
					g2.setTransform(old);
				}
				g2.dispose();
			}
		};
		leftPanel.setOpaque(false);

		// Overlay noi dung phan trai
		JPanel overlayInfo = new JPanel();
		overlayInfo.setLayout(new BoxLayout(overlayInfo, BoxLayout.Y_AXIS));
		overlayInfo.setOpaque(false);
		overlayInfo.setBorder(new EmptyBorder(0, 36, 36, 36));

		JLabel lblTrainIcon = new JLabel() {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(255, 255, 255, 35));
				g2.fillOval(0, 0, getWidth(), getHeight());
				g2.setColor(Color.WHITE);
				g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				int cx = getWidth()/2, cy = getHeight()/2;
				g2.drawRoundRect(cx-24, cy-8, 48, 20, 6, 6);
				int[] headX = {cx+24,cx+34,cx+34,cx+24};
				int[] headY = {cy-8, cy-2, cy+12,cy+12};
				g2.drawPolygon(headX, headY, 4);
				g2.fillOval(cx-18,cy+10,10,10); g2.fillOval(cx-2,cy+10,10,10); g2.fillOval(cx+16,cy+10,10,10);
				g2.drawRoundRect(cx-18,cy-4,9,9,2,2); g2.drawRoundRect(cx-6,cy-4,9,9,2,2); g2.drawRoundRect(cx+6,cy-4,9,9,2,2);
				g2.drawLine(cx-14,cy-8,cx-14,cy-16); g2.drawLine(cx-11,cy-8,cx-11,cy-14);
				g2.dispose();
			}
		};
		lblTrainIcon.setPreferredSize(new Dimension(80, 80));
		lblTrainIcon.setMaximumSize(new Dimension(80, 80));
		lblTrainIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel lblStation = new JLabel("GA TÀU SÀI GÒN");
		lblStation.setFont(new Font("Segoe UI", Font.BOLD, 34));
		lblStation.setForeground(Color.WHITE);
		lblStation.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel lblSub = new JLabel("Hệ thống quản lý bán vé tàu hỏa");
		lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		lblSub.setForeground(new Color(180, 210, 245));
		lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel divider = new JPanel() {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(new Color(0x4DA6FF));
				g2.fillRoundRect(0, 0, 56, 4, 4, 4);
				g2.dispose();
			}
		};
		divider.setOpaque(false);
		divider.setMaximumSize(new Dimension(56, 8));
		divider.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel lblDesc = new JLabel(
			"<html><div style='width:300px;line-height:1.7;color:#B4D2F5;font-family:Segoe UI;font-size:13px'>"
			+ "Quản lý lịch trình, bán vé, theo dõi doanh thu và vận hành toàn bộ hệ thống ga tàu một cách hiệu quả và chuyên nghiệp."
			+ "</div></html>");
		lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel featuresPanel = new JPanel();
		featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
		featuresPanel.setOpaque(false);
		featuresPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		String[] features = {"Quản lý vé & hành khách","Lịch trình chuyến tàu thực tế","Thống kê doanh thu tức thì"};
		for (String feat : features) {
			JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
			row.setOpaque(false);
			JLabel ico = new JLabel() {
				@Override protected void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setColor(new Color(0x4DA6FF));
					g2.fillOval(2, 2, 16, 16);
					g2.setColor(Color.WHITE);
					g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g2.drawLine(5,10,8,13); g2.drawLine(8,13,14,7);
					g2.dispose();
				}
			};
			ico.setPreferredSize(new Dimension(22, 22));
			JLabel lbl = new JLabel("  " + feat);
			lbl.setForeground(new Color(200, 225, 250));
			lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
			row.add(ico); row.add(lbl);
			featuresPanel.add(row);
		}

		overlayInfo.add(Box.createVerticalGlue());
		overlayInfo.add(lblTrainIcon);
		overlayInfo.add(Box.createVerticalStrut(16));
		overlayInfo.add(lblStation);
		overlayInfo.add(Box.createVerticalStrut(6));
		overlayInfo.add(lblSub);
		overlayInfo.add(Box.createVerticalStrut(14));
		overlayInfo.add(divider);
		leftPanel.add(overlayInfo, BorderLayout.SOUTH);

		// ════════════════════════════════════════
		// PANEL PHẢI — chinh la form, fill 100%, nen trang
		// ════════════════════════════════════════
		JPanel rightPanel = new JPanel(new GridBagLayout()) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(BG_CARD);   // trang tuyen, khong co vien hay card wrapper
				g2.fillRect(0, 0, getWidth(), getHeight());
				// Chấm lưới mờ trang trí
				g2.setColor(new Color(26, 94, 171, 10));
				for (int x = 0; x < getWidth(); x += 28)
					for (int y = 0; y < getHeight(); y += 28)
						g2.fillOval(x, y, 3, 3);
				// Duong ke accent tren cung (thay the card accent bar)
				g2.setColor(ACCENT);
				g2.fillRect(0, 0, getWidth(), 5);
				g2.dispose();
			}
		};
		rightPanel.setOpaque(true);
		// Padding trong de noi dung khong sat vien
		rightPanel.setBorder(new EmptyBorder(0, 44, 0, 44));

		// ── Header form ──

		JLabel lblTitle = new JLabel("Đăng nhập");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
		lblTitle.setForeground(TEXT_DARK);

		JPanel accentLine = new JPanel() {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				GradientPaint gp = new GradientPaint(0,0,ACCENT,80,0,new Color(ACCENT.getRed(),ACCENT.getGreen(),ACCENT.getBlue(),0));
				g2.setPaint(gp);
				g2.fillRoundRect(0, 0, 80, 4, 4, 4);
				g2.dispose();
			}
		};
		accentLine.setOpaque(false);
		accentLine.setPreferredSize(new Dimension(0, 8));

		JLabel uLabel = makeFieldLabel("Tài khoản");
		JLabel pLabel = makeFieldLabel("Mật khẩu");

		txtUsername = createStyledTextField();
		lblUserError = createErrorLabel();
		lblPassError = createErrorLabel();
		setupSuggestionMenu();

		txtUsername.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (txtUsername.isEditable() && txtUsername.getText().isEmpty()) updateSuggestions("");
			}
		});
		txtUsername.addActionListener(e -> { if (!suggestionMenu.isVisible()) txtPassword.requestFocus(); });

		JLabel lblForgotPass = new JLabel("Quên mật khẩu?");
		lblForgotPass.setForeground(ACCENT);
		lblForgotPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblForgotPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lblForgotPass.setHorizontalAlignment(SwingConstants.RIGHT);
		lblForgotPass.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) { handleForgotPassword(); }
			@Override public void mouseEntered(MouseEvent e) { lblForgotPass.setForeground(ACCENT_HVR); }
			@Override public void mouseExited(MouseEvent e)  { lblForgotPass.setForeground(ACCENT); }
		});

		btnLogin = new JButton("ĐĂNG NHẬP") {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Color bg = !isEnabled()           ? new Color(0xBDBDBD)
						 : getModel().isPressed() ? new Color(0x0A2F65)
						 : getModel().isRollover()? ACCENT_HVR : ACCENT;
				g2.setColor(bg);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				if (isEnabled()) {
					g2.setColor(new Color(255,255,255,160));
					g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					int rx = getWidth()-28, ry = getHeight()/2;
					g2.drawLine(rx,ry,rx+8,ry); g2.drawLine(rx+5,ry-4,rx+8,ry); g2.drawLine(rx+5,ry+4,rx+8,ry);
				}
				g2.dispose(); super.paintComponent(g);
			}
		};
		btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setContentAreaFilled(false); btnLogin.setBorderPainted(false); btnLogin.setFocusPainted(false);
		btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnLogin.setPreferredSize(new Dimension(0, 50));
		btnLogin.addActionListener(e -> handleLogin());

		JLabel lblFooter = new JLabel("2025 Ga Tàu Sài Gòn", SwingConstants.CENTER);
		lblFooter.setFont(F_SMALL);
		lblFooter.setForeground(TEXT_MUTED);

		// ── Add trực tiếp vào rightPanel (không qua formCard) ──
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 0; gc.weightx = 1.0;

		// Spacer tren
		GridBagConstraints gcTop = new GridBagConstraints();
		gcTop.gridx=0; gcTop.gridy=0; gcTop.weighty=1.0; gcTop.fill=GridBagConstraints.VERTICAL;
		rightPanel.add(Box.createVerticalGlue(), gcTop);
		
		gc.gridy=1;  gc.insets=new Insets(0,0,4,0);   rightPanel.add(lblTitle, gc);
		gc.gridy=2;  gc.insets=new Insets(0,0,28,0);  rightPanel.add(accentLine, gc);
		gc.gridy=3;  gc.insets=new Insets(0,0,6,0);   rightPanel.add(uLabel, gc);
		gc.gridy=4;  gc.insets=new Insets(0,0,4,0);   rightPanel.add(txtUsername, gc);
		gc.gridy=5;  gc.insets=new Insets(0,0,16,0);  rightPanel.add(lblUserError, gc);
		gc.gridy=6;  gc.insets=new Insets(0,0,6,0);   rightPanel.add(pLabel, gc);
		gc.gridy=7;  gc.insets=new Insets(0,0,4,0);   rightPanel.add(createPasswordPanel(), gc);
		gc.gridy=8;  gc.insets=new Insets(0,0,2,0);   rightPanel.add(lblPassError, gc);
		gc.gridy=9; gc.insets=new Insets(0,0,28,0);  rightPanel.add(lblForgotPass, gc);
		gc.gridy=10; gc.insets=new Insets(0,0,20,0);  rightPanel.add(btnLogin, gc);
		gc.gridy=11; gc.insets=new Insets(0,0,0,0);   rightPanel.add(lblFooter, gc);

		// Spacer duoi
		GridBagConstraints gcBot = new GridBagConstraints();
		gcBot.gridx=0; gcBot.gridy=13; gcBot.weighty=1.0; gcBot.fill=GridBagConstraints.VERTICAL;
		rightPanel.add(Box.createVerticalGlue(), gcBot);

		add(leftPanel);
		add(rightPanel);
	}

	// ════════════════════════════════════════
	// HELPERS
	// ════════════════════════════════════════
	private JLabel makeFieldLabel(String text) {
		JLabel l = new JLabel(text);
		l.setFont(F_LABEL);
		l.setForeground(TEXT_DARK);
		return l;
	}

	private JTextField createStyledTextField() {
		JTextField tf = new JTextField(15) {
			@Override protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (getText().isEmpty() && !(FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setColor(TEXT_MUTED);
					g2.setFont(getFont().deriveFont(Font.ITALIC));
					Insets ins = getInsets();
					g2.drawString("Nhập tên tài khoản", ins.left, g.getFontMetrics().getAscent() + ins.top);
					g2.dispose();
				}
			}
		};
		tf.setFont(F_CELL);
		tf.setBackground(BG_CARD);
		tf.setForeground(TEXT_DARK);
		tf.setCaretColor(ACCENT);
		tf.setPreferredSize(new Dimension(0, 46));
		updateFieldBorder(tf, BORDER_CLR);
		return tf;
	}

	private void updateFieldBorder(JTextField field, Color color) {
		field.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(color, 1, true),
				BorderFactory.createEmptyBorder(10, 14, 10, 14)));
	}

	private JLabel createErrorLabel() {
		JLabel lbl = new JLabel(" ");
		lbl.setForeground(ERROR_CLR);
		lbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
		return lbl;
	}

	// ── Suggestion menu ──
	private void setupSuggestionMenu() {
		suggestionMenu = new JPopupMenu();
		suggestionMenu.setFocusable(false);
		listSuggestions = new JList<>();
		listSuggestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listSuggestions.setBackground(BG_CARD);
		listSuggestions.setFont(F_CELL);
		listSuggestions.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) { confirmSelection(); }
		});
		listSuggestions.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) confirmSelection();
			}
		});
		JScrollPane scroll = new JScrollPane(listSuggestions);
		scroll.setBorder(null);
		suggestionMenu.add(scroll);

		txtUsername.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && suggestionMenu.isVisible()) {
					confirmSelection(); e.consume();
				}
			}
			@Override public void keyReleased(KeyEvent e) {
				String input = txtUsername.getText().trim();
				if (input.isEmpty()) {
					lblUserError.setText("Tài khoản không được để trống!");
					updateFieldBorder(txtUsername, ERROR_CLR);
				} else {
					lblUserError.setText(" ");
					updateFieldBorder(txtUsername, ACCENT);
				}
				if (e.getKeyCode() == KeyEvent.VK_DOWN && suggestionMenu.isVisible()) {
					listSuggestions.requestFocus();
					listSuggestions.setSelectedIndex(0);
				} else if (e.getKeyCode() != KeyEvent.VK_ENTER && e.getKeyCode() != KeyEvent.VK_UP) {
					updateSuggestions(input);
				}
			}
		});
		txtUsername.addFocusListener(new FocusAdapter() {
			@Override public void focusGained(FocusEvent e) { updateFieldBorder(txtUsername, ACCENT); }
			@Override public void focusLost(FocusEvent e)   { updateFieldBorder(txtUsername, BORDER_CLR); }
		});
	}

	private void confirmSelection() {
		String selected = listSuggestions.getSelectedValue();
		if (selected != null) {
			txtUsername.setText(selected);
			suggestionMenu.setVisible(false);
			lblUserError.setText(" ");
			updateFieldBorder(txtUsername, ACCENT);
			txtPassword.requestFocus();
		}
	}

	private void updateSuggestions(String input) {
		String history = prefs.get(HISTORY_KEY, "");
		if (history.isEmpty()) return;
		String[] allUsers = history.split(",");
		DefaultListModel<String> model = new DefaultListModel<>();
		boolean hasMatch = false;
		for (String user : allUsers) {
			if (input.isEmpty() || user.toLowerCase().startsWith(input.toLowerCase())) {
				model.addElement(user); hasMatch = true;
			}
		}
		if (hasMatch) {
			listSuggestions.setModel(model);
			listSuggestions.setSelectedIndex(0);
			suggestionMenu.setPreferredSize(new Dimension(txtUsername.getWidth(), Math.min(model.getSize() * 30, 150)));
			if (!suggestionMenu.isVisible())
				suggestionMenu.show(txtUsername, 0, txtUsername.getHeight());
		} else {
			suggestionMenu.setVisible(false);
		}
	}

	private void saveToHistory(String user) {
		String history = prefs.get(HISTORY_KEY, "");
		LinkedHashSet<String> userSet = new LinkedHashSet<>();
		userSet.add(user);
		if (!history.isEmpty()) Collections.addAll(userSet, history.split(","));
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (String s : userSet) {
			if (count > 0) sb.append(",");
			sb.append(s);
			if (++count >= 5) break;
		}
		prefs.put(HISTORY_KEY, sb.toString());
	}

	// ── Password panel ──
	private JPanel createPasswordPanel() {
		JPanel pnl = new JPanel(new BorderLayout()) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(BG_CARD);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
				g2.dispose();
			}
		};
		pnl.setOpaque(false);
		pnl.setPreferredSize(new Dimension(0, 46));

		txtPassword = new JPasswordField(15) {
			@Override protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (getPassword().length == 0 && !(FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					g2.setColor(TEXT_MUTED);
					g2.setFont(getFont().deriveFont(Font.ITALIC));
					Insets ins = getInsets();
					g2.drawString("Nhập mật khẩu...", ins.left, g.getFontMetrics().getAscent() + ins.top);
					g2.dispose();
				}
			}
		};
		txtPassword.setFont(F_CELL);
		txtPassword.setBackground(BG_CARD);
		txtPassword.setForeground(TEXT_DARK);
		txtPassword.setCaretColor(ACCENT);
		txtPassword.setEchoChar('•');
		txtPassword.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 0, BORDER_CLR),
				BorderFactory.createEmptyBorder(10, 14, 10, 10)));

		txtPassword.addFocusListener(new FocusAdapter() {
			@Override public void focusGained(FocusEvent e) {
				txtPassword.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(1, 1, 1, 0, ACCENT),
						BorderFactory.createEmptyBorder(10, 14, 10, 10)));
			}
			@Override public void focusLost(FocusEvent e) {
				txtPassword.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(1, 1, 1, 0, BORDER_CLR),
						BorderFactory.createEmptyBorder(10, 14, 10, 10)));
			}
		});

		// Nút hiện/ẩn mật khẩu — icon con mắt vẽ tay
		btnShowPass = new JToggleButton() {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(isSelected() ? ROW_SEL : BG_CARD);
				g2.fillRect(0, 0, getWidth(), getHeight());
				// Viền phải + trên + dưới
				g2.setColor(isSelected() ? ACCENT : BORDER_CLR);
				g2.setStroke(new BasicStroke(1f));
				g2.drawLine(0, 0, getWidth(), 0);
				g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
				g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight()-1);
				// Icon mắt
				g2.setColor(isSelected() ? ACCENT : TEXT_MID);
				g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				int cx = getWidth()/2, cy = getHeight()/2;
				if (isSelected()) {
					// Mat mo: elipse day du
					g2.drawOval(cx-8, cy-5, 16, 10);
					g2.fillOval(cx-3, cy-3, 6, 6);
				} else {
					// Mat nhap: elipse + gach cheo
					g2.drawArc(cx-8, cy-6, 16, 12, 30, 120);
					g2.drawArc(cx-8, cy-6, 16, 12, 210, 120);
					g2.fillOval(cx-3, cy-3, 6, 6);
					g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g2.drawLine(cx-7, cy+5, cx+7, cy-5);
				}
				g2.dispose();
			}
		};
		btnShowPass.setPreferredSize(new Dimension(46, 0));
		btnShowPass.setFocusPainted(false);
		btnShowPass.setContentAreaFilled(false);
		btnShowPass.setBorderPainted(false);
		btnShowPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnShowPass.addActionListener(e ->
			txtPassword.setEchoChar(btnShowPass.isSelected() ? (char) 0 : '•'));

		txtPassword.addActionListener(e -> btnLogin.doClick());

		pnl.add(txtPassword,  BorderLayout.CENTER);
		pnl.add(btnShowPass,  BorderLayout.EAST);
		return pnl;
	}

	// ── Login logic ──
	private void handleLogin() {
		if (isPermanentlyLocked) { showPermanentLockAlert(); return; }
		String username = txtUsername.getText().trim();
		String password = new String(txtPassword.getPassword()).trim();
		if (username.isEmpty()) {
			lblUserError.setText("Tài khoản không được để trống!");
			updateFieldBorder(txtUsername, ERROR_CLR);
			txtUsername.requestFocus(); return;
		}
		if (password.isEmpty()) {
			lblPassError.setText("Mật khẩu không được để trống!");
			txtPassword.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(1, 1, 1, 0, ERROR_CLR),
					BorderFactory.createEmptyBorder(10, 14, 10, 10)));
			txtPassword.requestFocus(); return;
		}
		NhanVien nv = nv_dao.checkLogin(username, hashPassword(password));
		if (nv != null) {
			saveToHistory(username);
			failedAttempts = 0;
			openMainWindow(nv);
		} else {
			failedAttempts++;
			processFailedAttempt();
		}
	}

	private void openMainWindow(NhanVien nv) {
		Window currentWindow = SwingUtilities.getWindowAncestor(this);
		SwingUtilities.invokeLater(() -> {
			JFrame mainFrame = new JFrame("HỆ THỐNG QUẢN LÝ GA TÀU SÀI GÒN");
			GUI_General mainContent = new GUI_General(nv);
			mainFrame.setContentPane(mainContent);
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			mainFrame.setVisible(true);
			if (currentWindow != null) currentWindow.dispose();
		});
	}

	private void processFailedAttempt() {
		if (failedAttempts == 3) {
			JOptionPane.showMessageDialog(this, "Sai 3 lần! Tạm khóa 5 phút.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			startCountdown(5);
		} else if (failedAttempts >= 6) {
			isPermanentlyLocked = true;
			if (countdownTimer != null) countdownTimer.stop();
			showPermanentLockAlert();
		} else {
			lblPassError.setText("Sai mật khẩu! (Lần " + failedAttempts + ")");
			txtPassword.setText("");
			txtPassword.requestFocus();
		}
	}

	private void startCountdown(int minutes) {
		btnLogin.setEnabled(false);
		txtPassword.setEditable(false);
		btnShowPass.setEnabled(false);
		txtUsername.setEditable(true);
		final int[] secondsLeft = { minutes * 60 };
		if (countdownTimer != null && countdownTimer.isRunning()) countdownTimer.stop();
		countdownTimer = new Timer(1000, e -> {
			secondsLeft[0]--;
			if (secondsLeft[0] > 0) {
				int m = secondsLeft[0] / 60, s = secondsLeft[0] % 60;
				btnLogin.setText(String.format("Thử lại sau (%02d:%02d)", m, s));
			} else {
				((Timer) e.getSource()).stop();
				btnLogin.setEnabled(true);
				btnLogin.setText("ĐĂNG NHẬP");
				txtPassword.setEditable(true);
				btnShowPass.setEnabled(true);
				lblPassError.setText(" ");
				failedAttempts = 0;
			}
		});
		countdownTimer.setInitialDelay(0);
		countdownTimer.start();
	}

	private void showPermanentLockAlert() {
		JOptionPane.showMessageDialog(this, "Hệ thống bị khóa vĩnh viễn! LH Quản lý: 0859495852", "KHÓA",
				JOptionPane.ERROR_MESSAGE);
		txtUsername.setEnabled(false);
		txtPassword.setEnabled(false);
		btnLogin.setEnabled(false);
	}

	private String hashPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = md.digest(password.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (byte b : hashBytes) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException("Không thể băm mật khẩu", e);
		}
	}

	// ── Quên mật khẩu & OTP ──
	private void handleForgotPassword() {
		JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
		JTextField txtResetUser  = new JTextField();
		JTextField txtResetEmail = new JTextField();
		txtResetUser.addActionListener(e -> txtResetEmail.requestFocus());
		panel.add(new JLabel("Nhập tài khoản:"));
		panel.add(txtResetUser);
		panel.add(new JLabel("Nhập Email đăng ký:"));
		panel.add(txtResetEmail);
		int result = JOptionPane.showConfirmDialog(this, panel, "Khôi phục mật khẩu",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String taiKhoan = txtResetUser.getText().trim();
			String email    = txtResetEmail.getText().trim();
			if (nv_dao.verifyUserByEmail(taiKhoan, email)) {
				showOtpVerificationDialog(taiKhoan, email);
			} else {
				JOptionPane.showMessageDialog(this, "Thông tin tài khoản hoặc email không đúng!", "Lỗi",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void showOtpVerificationDialog(String taiKhoan, String email) {
		currentOtpGenerated = String.format("%04d", new Random().nextInt(10000));
		new Thread(() -> sendEmail(email, currentOtpGenerated)).start();
		JDialog otpDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Xác thực OTP", true);
		otpDialog.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 15, 8, 15);
		gbc.fill   = GridBagConstraints.HORIZONTAL;
		JLabel lblMsg = new JLabel("<html><center>Mã OTP đã được gửi đến:<br><b>" + email + "</b></center></html>");
		lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
		JTextField txtOtpInput = new JTextField(6);
		txtOtpInput.setFont(new Font("Segoe UI", Font.BOLD, 18));
		txtOtpInput.setHorizontalAlignment(JTextField.CENTER);
		JButton btnConfirm = new JButton("Xác nhận");
		JButton btnResend  = new JButton("Gửi lại (30s)");
		btnResend.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		btnResend.setEnabled(false);
		final int[] timeLeft = { 30 };
		Timer resendTimer = new Timer(1000, null);
		resendTimer.addActionListener(e -> {
			timeLeft[0]--;
			if (timeLeft[0] > 0) btnResend.setText("Gửi lại (" + timeLeft[0] + "s)");
			else { btnResend.setText("Gửi lại mã"); btnResend.setEnabled(true); ((Timer) e.getSource()).stop(); }
		});
		resendTimer.start();
		otpDialog.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) { resendTimer.stop(); }
		});
		btnResend.addActionListener(e -> {
			currentOtpGenerated = String.format("%04d", new Random().nextInt(10000));
			new Thread(() -> sendEmail(email, currentOtpGenerated)).start();
			JOptionPane.showMessageDialog(otpDialog, "Đã gửi lại mã mới!");
			timeLeft[0] = 30; btnResend.setEnabled(false); resendTimer.start();
		});
		btnConfirm.addActionListener(e -> {
			if (txtOtpInput.getText().trim().equals(currentOtpGenerated)) {
				resendTimer.stop(); otpDialog.dispose();
				String newPass = JOptionPane.showInputDialog(this, "Nhập mật khẩu mới:");
				if (newPass != null && !newPass.isEmpty()) {
					if (nv_dao.updatePassword(taiKhoan, hashPassword(newPass)))
						JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
				}
			} else {
				JOptionPane.showMessageDialog(otpDialog, "Mã OTP không chính xác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		});
		gbc.gridy = 0; otpDialog.add(lblMsg, gbc);
		gbc.gridy = 1; gbc.insets = new Insets(5, 50, 5, 50); otpDialog.add(txtOtpInput, gbc);
		gbc.gridy = 2; gbc.insets = new Insets(10, 10, 10, 10);
		JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		pnlBtns.add(btnConfirm); pnlBtns.add(btnResend);
		otpDialog.add(pnlBtns, gbc);
		otpDialog.pack();
		otpDialog.setResizable(false);
		otpDialog.setLocationRelativeTo(this);
		otpDialog.setVisible(true);
	}

	private void sendEmail(String recipientEmail, String otp) {
		final String myEmail       = "ngbathien3101@gmail.com";
		final String myAppPassword = "dhbeqfkcunlpgzoj";
		Properties props = new Properties();
		props.put("mail.smtp.auth",            "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host",            "smtp.gmail.com");
		props.put("mail.smtp.port",            "587");
		props.put("mail.smtp.ssl.protocols",   "TLSv1.2");
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			@Override protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(myEmail, myAppPassword);
			}
		});
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(myEmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
			message.setSubject("MÃ OTP KHÔI PHỤC MẬT KHẨU - GA TÀU SÀI GÒN");
			message.setText("Mã OTP của bạn là: " + otp + "\n\nTrân trọng!");
			Transport.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {}

	public void setParentFrame(JFrame frame) {
		this.parentFrame = frame;
	}
}