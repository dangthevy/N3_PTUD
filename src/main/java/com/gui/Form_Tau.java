package com.gui;

import com.dao.DAO_Tau;
import com.entities.Tau;
import com.enums.TrangThaiTau;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class Form_Tau extends JDialog {
	private static final Color ACCENT = new Color(0x1A5EAB);
	private static final Color TEXT_DARK = new Color(0x1E2B3C);
	private static final Color BORDER_CLR = new Color(0xE2EAF4);
	private static final Color DANGER = new Color(0xDC3545);

	private JTextField txtMa, txtTen, txtSoToa;
	private JComboBox<TrangThaiTau> cbTrangThai;
	private JLabel lblErrTen, lblErrSoToa;
	private JButton btnSave;

	private boolean confirmed = false;
	private Tau entity;

	// Biến lưu trạng thái ban đầu để so sánh sự thay đổi
	private boolean isEditMode = false;
	private String origTen = "";
	private String origSoToaStr = "";
	private String origTrangThaiStr = "";

	public Form_Tau(Frame parent, String title) {
		super(parent, title, true);
		setSize(500, 450);
		setLocationRelativeTo(parent);
		getContentPane().setBackground(Color.WHITE);

		JPanel pnlMain = new JPanel(new BorderLayout(15, 20));
		pnlMain.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
		pnlMain.setBackground(Color.WHITE);

		JLabel lblTitle = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitle.setForeground(ACCENT);
		pnlMain.add(lblTitle, BorderLayout.NORTH);

		JPanel pnlForm = new JPanel(new GridBagLayout());
		pnlForm.setOpaque(false);
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(5, 5, 0, 5);

		txtMa = createTextField();
		txtMa.setText(new DAO_Tau().phatSinhMaTau());
		txtMa.setEditable(false);
		txtMa.setBackground(new Color(0xF4F7FB));
		txtTen = createTextField();
		txtSoToa = createTextField();
		cbTrangThai = new JComboBox<>(TrangThaiTau.values());
		cbTrangThai.setPreferredSize(new Dimension(0, 38));
		cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		cbTrangThai.setBackground(Color.WHITE);
		cbTrangThai.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
				super.getListCellRendererComponent(l, v, i, s, f);
				if (v instanceof TrangThaiTau)
					setText(((TrangThaiTau) v).getMoTa());
				setBorder(new EmptyBorder(5, 10, 5, 10));
				return this;
			}
		});

		lblErrTen = createErrorLabel();
		lblErrSoToa = createErrorLabel();

		int y = 0;
		addRow(pnlForm, "Mã đoàn tàu :", txtMa, null, y++, gc);
		addRow(pnlForm, "Tên tàu (*):", txtTen, lblErrTen, y++, gc);
		addRow(pnlForm, "Số toa  (*):", txtSoToa, lblErrSoToa, y++, gc);
		addRow(pnlForm, "Trạng thái vận hành:", cbTrangThai, null, y++, gc);

		pnlMain.add(pnlForm, BorderLayout.CENTER);

		JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
		pnlBottom.setOpaque(false);
		JButton btnCancel = createButton("Hủy Bỏ", new Color(108, 122, 137));
		btnSave = createButton("Lưu Dữ Liệu", ACCENT);
		btnSave.setEnabled(false);

		btnCancel.addActionListener(e -> dispose());
		btnSave.addActionListener(e -> validateAndSave());
		pnlBottom.add(btnCancel);
		pnlBottom.add(btnSave);
		pnlMain.add(pnlBottom, BorderLayout.SOUTH);
		add(pnlMain);

		// Gắn bộ lắng nghe sự kiện gõ phím để check Real-time
		addLiveValidationListener(txtTen);
		addLiveValidationListener(txtSoToa);
		cbTrangThai.addActionListener(e -> liveValidate());
	}

	private void addRow(JPanel p, String l, JComponent c, JLabel err, int y, GridBagConstraints gc) {
		gc.gridy = y * 2;
		gc.gridx = 0;
		gc.weightx = 0.35;
		JLabel lbl = new JLabel(l);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lbl.setForeground(TEXT_DARK);
		p.add(lbl, gc);
		gc.gridx = 1;
		gc.weightx = 0.65;
		p.add(c, gc);
		if (err != null) {
			gc.gridy = y * 2 + 1;
			gc.gridx = 1;
			gc.insets = new Insets(0, 5, 10, 5);
			p.add(err, gc);
			gc.insets = new Insets(5, 5, 0, 5);
		} else {
			gc.gridy = y * 2 + 1;
			p.add(Box.createVerticalStrut(12), gc);
		}
	}

	// ================== REAL-TIME VALIDATION TỐI ƯU ==================
		private void liveValidate() {
			boolean isValid = true;
			
			// 1. Validate Tên Tàu
			String ten = txtTen.getText().trim();
			if (ten.isEmpty()) { 
				// FIX: Không bôi đỏ khi đang trống, chỉ ngầm báo Form chưa hợp lệ
				clearErrorState(txtTen, lblErrTen); 
				isValid = false; 
			} else if (!ten.matches("^(SE|TN|SN|SPT)\\d+$")) { 
				setErrorState(txtTen, lblErrTen, "Định dạng sai (VD: SE1, TN2)!"); 
				isValid = false; 
			} else {
				clearErrorState(txtTen, lblErrTen);
			}

			// 2. Validate Số Toa
			String soToaStr = txtSoToa.getText().trim();
			int currentSoToa = -1;
			if (soToaStr.isEmpty()) { 
				// FIX: Không bôi đỏ khi đang trống
				clearErrorState(txtSoToa, lblErrSoToa); 
				isValid = false; 
			} else {
				try {
					currentSoToa = Integer.parseInt(soToaStr);
					if (currentSoToa < 5 || currentSoToa > 20) { 
						setErrorState(txtSoToa, lblErrSoToa, "Số toa từ 5 - 20!"); 
						isValid = false; 
					} else {
						clearErrorState(txtSoToa, lblErrSoToa);
					}
				} catch (Exception ex) { 
					setErrorState(txtSoToa, lblErrSoToa, "Phải là số nguyên!"); 
					isValid = false; 
				}
			}

			// 3. Dirty Check (Kiểm tra có sự thay đổi thực sự không)
			boolean isChanged = true;
			if (isEditMode) {
				String curTrangThaiStr = ((TrangThaiTau) cbTrangThai.getSelectedItem()).name();
				if (ten.equals(origTen) && soToaStr.equals(origSoToaStr) && curTrangThaiStr.equals(origTrangThaiStr)) {
					isChanged = false;
				}
			}

			// Nút lưu chỉ sáng lên khi TẤT CẢ các ô đã nhập đúng và có sự thay đổi
			btnSave.setEnabled(isValid && isChanged);
		}

	// Đổi viền ô Text thành màu Đỏ và hiện thông báo
	private void setErrorState(JTextField tf, JLabel lbl, String msg) {
		tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(DANGER), new EmptyBorder(0, 10, 0, 10)));
		lbl.setText(msg);
	}

	// Hủy viền Đỏ và xóa thông báo
	private void clearErrorState(JTextField tf, JLabel lbl) {
		tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_CLR), new EmptyBorder(0, 10, 0, 10)));
		lbl.setText(" ");
	}

	private void validateAndSave() {
		entity = new Tau(txtMa.getText().trim(), txtTen.getText().trim(), Integer.parseInt(txtSoToa.getText().trim()),
				(TrangThaiTau) cbTrangThai.getSelectedItem());
		confirmed = true;
		dispose();
	}

	// --- UI HELPERS ---
	private JTextField createTextField() {
		JTextField tf = new JTextField();
		tf.setPreferredSize(new Dimension(0, 38));
		tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_CLR), new EmptyBorder(0, 10, 0, 10)));
		return tf;
	}

	private JLabel createErrorLabel() {
		JLabel lbl = new JLabel(" ");
		lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
		lbl.setForeground(DANGER);
		return lbl;
	}

	private JButton createButton(String text, Color bg) {
		JButton b = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (!isEnabled()) {
					g2.setColor(new Color(200, 200, 200));
				} else {
					g2.setColor(getModel().isRollover() ? bg.darker() : bg);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		b.setPreferredSize(new Dimension(130, 40));
		b.setForeground(Color.WHITE);
		b.setFont(new Font("Segoe UI", Font.BOLD, 14));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private void addLiveValidationListener(JTextField tf) {
		tf.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				liveValidate();
			}

			public void removeUpdate(DocumentEvent e) {
				liveValidate();
			}

			public void changedUpdate(DocumentEvent e) {
				liveValidate();
			}
		});
	}

	public void setEntity(Tau t) {
		isEditMode = true;
		origTen = t.getTenTau();
		origSoToaStr = String.valueOf(t.getSoToa());
		origTrangThaiStr = t.getTrangThaiTau().name();

		txtMa.setText(t.getMaTau());
		txtTen.setText(origTen);
		txtSoToa.setText(origSoToaStr);
		cbTrangThai.setSelectedItem(t.getTrangThaiTau());

		liveValidate(); // Chạy kiểm tra ngay khi nạp dữ liệu lên form
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public Tau getEntity() {
		return entity;
	}
}