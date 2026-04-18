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
	private JTextField txtMa, txtTen, txtHang, txtCot, txtSucChua;
	private JComboBox<String> cbKieu;
	private JLabel lblErrMa, lblErrTen, lblErrHang, lblErrCot;
	private JButton btnSave; // Đưa ra biến toàn cục để khóa/mở

	private boolean confirmed = false;
	private boolean isLocked = false;

	// Dirty Check
	private boolean isEditMode = false;
	private String origMa = "", origTen = "", origKieu = "";
	private int origHang = -1, origCot = -1;

	public Form_LoaiToa(Frame parent, String title) {
		super(parent, title, true);
		setSize(500, 550);
		setLocationRelativeTo(parent);
		getContentPane().setBackground(Color.WHITE);

		JPanel pnlMain = new JPanel(new BorderLayout(10, 15));
		pnlMain.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
		pnlMain.setBackground(Color.WHITE);

		JLabel lblTitle = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitle.setForeground(new Color(0x1A5EAB));
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
		txtSucChua.setBackground(new Color(0xEEF2F8));
		cbKieu = new JComboBox<>(new String[] { "GHE", "GIUONG" });
		cbKieu.setPreferredSize(new Dimension(0, 35));

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
		addRow(pnlForm, "Số hàng ghế (*):", txtHang, lblErrHang, y++, gc);
		addRow(pnlForm, "Số cột ghế (*):", txtCot, lblErrCot, y++, gc);
		addRow(pnlForm, "Kiểu hiển thị:", cbKieu, null, y++, gc);
		addRow(pnlForm, "Tổng sức chứa:", txtSucChua, null, y++, gc);

		pnlMain.add(pnlForm, BorderLayout.CENTER);

		JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlBottom.setOpaque(false);
		JButton btnCancel = createButton("Hủy bỏ", new Color(149, 165, 166));
		btnSave = createButton("Lưu", new Color(0x1A5EAB));
		btnSave.setEnabled(false); // Khóa nút lưu ban đầu

		btnCancel.addActionListener(e -> dispose());
		btnSave.addActionListener(e -> validateAndSave());

		pnlBottom.add(btnCancel);
		pnlBottom.add(btnSave);
		pnlMain.add(pnlBottom, BorderLayout.SOUTH);
		add(pnlMain);

		// Thay đổi thành Live Validation Listener
		addLiveValidationListener(txtMa, lblErrMa);
		addLiveValidationListener(txtTen, lblErrTen);
		addLiveValidationListener(txtHang, lblErrHang);
		addLiveValidationListener(txtCot, lblErrCot);
		cbKieu.addActionListener(e -> checkSaveButtonState());
	}

	private void addRow(JPanel p, String l, JComponent c, JLabel err, int y, GridBagConstraints gc) {
		gc.gridy = y * 2;
		gc.gridx = 0;
		gc.weightx = 0.35;
		JLabel lbl = new JLabel(l);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
		p.add(lbl, gc);
		gc.gridx = 1;
		gc.weightx = 0.65;
		p.add(c, gc);

		if (err != null) {
			gc.gridy = y * 2 + 1;
			gc.gridx = 1;
			gc.insets = new Insets(0, 5, 8, 5);
			p.add(err, gc);
			gc.insets = new Insets(5, 5, 0, 5);
		} else {
			gc.gridy = y * 2 + 1;
			p.add(Box.createVerticalStrut(10), gc);
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

	// ================== REAL-TIME VALIDATION ==================
	private void checkSaveButtonState() {
		boolean isValid = true;

		String ma = txtMa.getText().trim();
		String ten = txtTen.getText().trim();
		if (ma.isEmpty() || ten.isEmpty())
			isValid = false;

		int currentHang = -1, currentCot = -1;
		try {
			currentHang = Integer.parseInt(txtHang.getText().trim());
			if (currentHang <= 0)
				isValid = false;
		} catch (Exception e) {
			isValid = false;
		}

		try {
			currentCot = Integer.parseInt(txtCot.getText().trim());
			if (currentCot <= 0)
				isValid = false;
		} catch (Exception e) {
			isValid = false;
		}

		boolean isChanged = true;
		if (isEditMode) {
			String curKieu = cbKieu.getSelectedItem().toString();
			if (ma.equals(origMa) && ten.equals(origTen) && currentHang == origHang && currentCot == origCot
					&& curKieu.equals(origKieu)) {
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
		txtHang.setBackground(new Color(0xEEF2F8));
		txtCot.setBackground(new Color(0xEEF2F8));
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
		txtMa.setBackground(new Color(0xEEF2F8));
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

	// --- Helpers ---
	private JTextField createTextField() {
		JTextField tf = new JTextField();
		tf.setPreferredSize(new Dimension(0, 35));
		tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200)),
				new EmptyBorder(0, 8, 0, 8)));
		return tf;
	}

	private JLabel createErrorLabel() {
		JLabel lbl = new JLabel(" ");
		lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
		lbl.setForeground(new Color(0xE74C3C));
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
		b.setPreferredSize(new Dimension(100, 38));
		b.setForeground(Color.WHITE);
		b.setFont(new Font("Segoe UI", Font.BOLD, 14));
		b.setFocusPainted(false);
		b.setBorderPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private void addLiveValidationListener(JTextField tf, JLabel lbl) {
		tf.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				lbl.setText(" ");
				checkSaveButtonState();
			}

			public void removeUpdate(DocumentEvent e) {
				lbl.setText(" ");
				checkSaveButtonState();
			}

			public void changedUpdate(DocumentEvent e) {
				lbl.setText(" ");
				checkSaveButtonState();
			}
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