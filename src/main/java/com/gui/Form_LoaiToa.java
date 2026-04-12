package com.gui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class Form_LoaiToa extends JDialog {
	private JTextField txtMa, txtTen, txtHang, txtCot, txtSucChua;
	private JComboBox<String> cbKieu;
	private boolean confirmed = false;

	public Form_LoaiToa(Frame parent, String title) {
		super(parent, title, true);
		setSize(450, 420);
		setLocationRelativeTo(parent);
		getContentPane().setBackground(Color.WHITE);
		JPanel pnlMain = new JPanel(new BorderLayout(10, 20));
		pnlMain.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
		pnlMain.setBackground(Color.WHITE);
		JLabel lblTitle = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
		lblTitle.setForeground(new Color(0x1A5EAB));
		pnlMain.add(lblTitle, BorderLayout.NORTH);

		JPanel pnlForm = new JPanel(new GridBagLayout());
		pnlForm.setOpaque(false);
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(10, 5, 10, 5);
		txtMa = new JTextField();
		txtTen = new JTextField();
		txtHang = new JTextField();
		txtCot = new JTextField();
		txtSucChua = new JTextField();
		txtSucChua.setEditable(false);
		txtSucChua.setBackground(new Color(0xEEF2F8));
		txtSucChua.setFont(new Font("Segoe UI", Font.BOLD, 13));
		txtSucChua.setForeground(new Color(0x1A5EAB));
		cbKieu = new JComboBox<>(new String[] { "GHE", "GIUONG" });

		addRow(pnlForm, "Mã loại toa (*):", txtMa, 0, gc);
		addRow(pnlForm, "Tên loại toa (*):", txtTen, 1, gc);
		JPanel pSize = new JPanel(new GridLayout(1, 4, 10, 0));
		pSize.setOpaque(false);
		pSize.add(new JLabel("Số hàng:"));
		pSize.add(txtHang);
		pSize.add(new JLabel("Số cột:"));
		pSize.add(txtCot);
		addRow(pnlForm, "Kích thước (*):", pSize, 2, gc);
		addRow(pnlForm, "Sức chứa:", txtSucChua, 3, gc);
		addRow(pnlForm, "Kiểu vẽ UI:", cbKieu, 4, gc);

		KeyAdapter calc = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					int h = Integer.parseInt(txtHang.getText().trim());
					int c = Integer.parseInt(txtCot.getText().trim());
					txtSucChua.setText((h * c) + " chỗ");
				} catch (Exception ex) {
					txtSucChua.setText("0 chỗ");
				}
			}
		};
		txtHang.addKeyListener(calc);
		txtCot.addKeyListener(calc);

		pnlMain.add(pnlForm, BorderLayout.CENTER);
		JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlBottom.setOpaque(false);
		JButton btnCancel = new JButton("Hủy");
		btnCancel.setPreferredSize(new Dimension(90, 35));
		JButton btnSave = new JButton("Lưu");
		btnSave.setPreferredSize(new Dimension(90, 35));
		btnSave.setBackground(new Color(0x1A5EAB));
		btnSave.setForeground(Color.WHITE);
		btnCancel.addActionListener(e -> dispose());
		btnSave.addActionListener(e -> validateAndSave());
		pnlBottom.add(btnCancel);
		pnlBottom.add(btnSave);
		pnlMain.add(pnlBottom, BorderLayout.SOUTH);
		add(pnlMain);
	}

	private void addRow(JPanel p, String l, JComponent c, int y, GridBagConstraints gc) {
		gc.gridy = y;
		gc.gridx = 0;
		gc.weightx = 0.3;
		JLabel lbl = new JLabel(l);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
		p.add(lbl, gc);
		gc.gridx = 1;
		gc.weightx = 0.7;
		c.setPreferredSize(new Dimension(0, 32));
		p.add(c, gc);
	}

	private void validateAndSave() {
		if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty() || txtHang.getText().trim().isEmpty()
				|| txtCot.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các trường bắt buộc (*)", "Cảnh báo",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			int h = Integer.parseInt(txtHang.getText().trim());
			int c = Integer.parseInt(txtCot.getText().trim());
			if (h <= 0 || c <= 0)
				throw new Exception();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Số hàng và số cột phải là số nguyên dương!", "Lỗi nhập liệu",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		confirmed = true;
		dispose();
	}

	public void setEditData(String ma, String ten, int h, int c, String kieu) {
		txtMa.setText(ma);
		txtMa.setEditable(false);
		txtMa.setBackground(new Color(0xEEF2F8));
		txtTen.setText(ten);
		txtHang.setText(h + "");
		txtCot.setText(c + "");
		txtSucChua.setText((h * c) + " chỗ");
		cbKieu.setSelectedItem(kieu);
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public String getMa() {
		return txtMa.getText().trim();
	}

	public String getTen() {
		return txtTen.getText().trim();
	}

	public int getHang() {
		return Integer.parseInt(txtHang.getText().trim());
	}

	public int getCot() {
		return Integer.parseInt(txtCot.getText().trim());
	}

	public String getKieu() {
		return cbKieu.getSelectedItem().toString();
	}
}