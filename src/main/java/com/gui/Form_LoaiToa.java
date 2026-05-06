package com.gui;

import com.entities.LoaiToa;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

public class Form_LoaiToa extends JDialog {
	// --- ĐỒNG BỘ BẢNG MÀU TỪ FORM_TOA ---
	private static final Color ACCENT = new Color(0x1A5EAB);
	private static final Color TEXT_DARK = new Color(0x1E2B3C);
	private static final Color BORDER_CLR = new Color(0xE2EAF4);
	private static final Color DANGER = new Color(0xDC3545);

	private JTextField txtMa, txtTen, txtHang, txtCot, txtSucChua;
	private JComboBox<String> cbKieu;
	private JLabel lblErrMa, lblErrTen, lblErrHang, lblErrCot;
	private JButton btnSave; 

	private boolean confirmed = false;
	private boolean isLocked = false;

	private boolean isEditMode = false;
	private String origMa = "", origTen = "", origKieu = "";
	private int origHang = -1, origCot = -1;

	public Form_LoaiToa(Frame parent, String title) {
		super(parent, title, true);
		setSize(500, 580); 
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
		txtTen = createTextField();
		txtHang = createTextField();
		txtCot = createTextField();
		txtSucChua = createTextField();
		txtSucChua.setEditable(false);
		txtSucChua.setBackground(new Color(0xF4F7FB));
		
		cbKieu = new JComboBox<>(new String[] { "GHE", "GIUONG" });
		cbKieu.setPreferredSize(new Dimension(0, 38)); 
		cbKieu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		cbKieu.setBackground(Color.WHITE);

		lblErrMa = createErrorLabel();
		lblErrTen = createErrorLabel();
		lblErrHang = createErrorLabel();
		lblErrCot = createErrorLabel();

		KeyAdapter capacityCalc = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				calculateCapacity();
			}
		};
		txtHang.addKeyListener(capacityCalc);
		txtCot.addKeyListener(capacityCalc);

		int y = 0;
		addRow(pnlForm, "Mã loại (*):", txtMa, lblErrMa, y++, gc);
		addRow(pnlForm, "Tên loại toa (*):", txtTen, lblErrTen, y++, gc);
		addRow(pnlForm, "Số hàng (Khoang) (*):", txtHang, lblErrHang, y++, gc);
		addRow(pnlForm, "Số cột (Giường) (*):", txtCot, lblErrCot, y++, gc);
		addRow(pnlForm, "Kiểu hiển thị:", cbKieu, null, y++, gc);
		addRow(pnlForm, "Tổng sức chứa:", txtSucChua, null, y++, gc);

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

		// Áp dụng lắng nghe thay đổi thời gian thực
		addLiveValidationListener(txtMa);
		addLiveValidationListener(txtTen);
		addLiveValidationListener(txtHang);
		addLiveValidationListener(txtCot);
		cbKieu.addActionListener(e -> checkSaveButtonState());
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

	private void calculateCapacity() {
		try {
			int h = Integer.parseInt(txtHang.getText().trim());
			int c = Integer.parseInt(txtCot.getText().trim());
			txtSucChua.setText((h * c) + " chỗ");
		} catch (Exception e) {
			txtSucChua.setText("0 chỗ");
		}
		checkSaveButtonState();
	}

	// ================== REAL-TIME VALIDATION (ĐÃ FIX UX) ==================
	private void checkSaveButtonState() {
		boolean isValid = true;

		// 1. Validate Mã Toa
		String ma = txtMa.getText().trim();
		if (ma.isEmpty()) {
			lblErrMa.setText(" "); // Không chửi khi rỗng, chỉ khóa nút lưu
			isValid = false;
		} else if (!ma.matches("^G_[a-zA-Z0-9]+$")) {
			lblErrMa.setText("Mã loại bắt đầu G_ theo sau chỉ được chứa chữ và số");
			isValid = false;
		} else if (ma.length() > 10) {
			lblErrMa.setText("Mã loại tối đa 10 ký tự");
			isValid = false;
		} else {
			lblErrMa.setText(" ");
		}

		// 2. Validate Tên Toa
		String ten = txtTen.getText().trim();
		if (ten.isEmpty()) {
			lblErrTen.setText(" "); // Không chửi khi rỗng
			isValid = false;
		} else {
			lblErrTen.setText(" ");
		}

		// 3. Validate Số Hàng
		String hangStr = txtHang.getText().trim();
		if (hangStr.isEmpty()) {
			lblErrHang.setText(" ");
			isValid = false;
		} else {
			try {
				int currentHang = Integer.parseInt(hangStr);
				if (currentHang <= 0) {
					lblErrHang.setText("Số hàng phải lớn hơn 0");
					isValid = false;
				} else if (currentHang > 20) { 
					lblErrHang.setText("Số hàng không được vượt quá 20");
					isValid = false;
				} else {
					lblErrHang.setText(" ");
				}
			} catch (Exception e) {
				lblErrHang.setText("Số hàng phải là một số nguyên");
				isValid = false;
			}
		}

		// 4. Validate Số Cột
		String cotStr = txtCot.getText().trim();
		if (cotStr.isEmpty()) {
			lblErrCot.setText(" ");
			isValid = false;
		} else {
			int currentCot = -1;
			String kieu = cbKieu.getSelectedItem().toString();
			try {
				currentCot = Integer.parseInt(cotStr);
				
				if (kieu.equals("GHE")) {
					if (currentCot != 4) {
						lblErrCot.setText("Toa Ghế ngồi: Số cột bắt buộc là 4");
						isValid = false;
					} else {
						lblErrCot.setText(" ");
					}
				} else if (kieu.equals("GIUONG")) {
					if (currentCot != 4 && currentCot != 6) {
						lblErrCot.setText("Toa Giường nằm: Khoang chỉ có 4 hoặc 6 giường");
						isValid = false;
					} else {
						lblErrCot.setText(" ");
					}
				}
			} catch (Exception e) {
				lblErrCot.setText("Số cột phải là số nguyên hợp lệ");
				isValid = false;
			}
		}

		// 5. Kiểm tra sự thay đổi dữ liệu (Dirty Check)
		boolean isChanged = true;
		if (isEditMode) {
			String curKieu = cbKieu.getSelectedItem().toString();
			if (ma.equals(origMa) && ten.equals(origTen) && hangStr.equals(String.valueOf(origHang)) 
					&& cotStr.equals(String.valueOf(origCot)) && curKieu.equals(origKieu)) {
				isChanged = false;
			}
		}
		
		btnSave.setEnabled(isValid && isChanged);
	}

	public void lockDimensions() {
		this.isLocked = true;
		txtHang.setEditable(false);
		txtCot.setEditable(false);
		cbKieu.setEnabled(false);
		txtHang.setBackground(new Color(0xF4F7FB));
		txtCot.setBackground(new Color(0xF4F7FB));
		txtTen.setToolTipText("Không thể sửa kích thước vì đã có Toa sử dụng loại này.");
	}

	public void setEditData(String ma, String ten, int h, int c, String kieu) {
		isEditMode = true;
		origMa = ma;
		origTen = ten;
		origHang = h;
		origCot = c;
		origKieu = kieu;

		txtMa.setText(ma);
		txtMa.setEditable(false);
		txtMa.setBackground(new Color(0xF4F7FB));
		txtTen.setText(ten);
		txtHang.setText(String.valueOf(h));
		txtCot.setText(String.valueOf(c));
		cbKieu.setSelectedItem(kieu);
		calculateCapacity();
		checkSaveButtonState();
	}

	private void validateAndSave() {
		if (isLocked) {
			if (JOptionPane.showConfirmDialog(this,
					"Loại toa này đang được sử dụng. Việc đổi tên sẽ áp dụng cho tất cả các toa liên quan. Xác nhận?",
					"Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return;
		}
		confirmed = true;
		dispose();
	}

	// --- ĐỒNG BỘ UI HELPERS TỪ FORM TOA ---
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
			public void insertUpdate(DocumentEvent e) { checkSaveButtonState(); }
			public void removeUpdate(DocumentEvent e) { checkSaveButtonState(); }
			public void changedUpdate(DocumentEvent e) { checkSaveButtonState(); }
		});
	}

	public LoaiToa getEntity() {
		return new LoaiToa(txtMa.getText().trim(), txtTen.getText().trim(), Integer.parseInt(txtHang.getText().trim()),
				Integer.parseInt(txtCot.getText().trim()), cbKieu.getSelectedItem().toString());
	}

	public boolean isConfirmed() {
		return confirmed;
	}
}