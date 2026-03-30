package com.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.entities.Tau;
import com.enums.TrangThaiTau;

import java.awt.*;

public class Form_Tau extends JDialog {
	private JTextField txtMa, txtTen, txtSoToa;
	private JComboBox<TrangThaiTau> cbTrangThai;
	private JButton btnSave, btnCancel;
	private boolean isEditMode = false;
	private boolean confirmed = false;

	public Form_Tau(Frame parent, String title) {
		super(parent, title, true); // Modal = true để chặn tương tác với cửa sổ chính
		setSize(450, 300);
		setLocationRelativeTo(parent);
		setLayout(new BorderLayout());

		JPanel pnlContent = new JPanel(new GridLayout(4, 2, 10, 15));
		pnlContent.setBorder(new EmptyBorder(20, 20, 20, 20));

		pnlContent.add(new JLabel("Mã Tàu:"));
		txtMa = new JTextField();
		pnlContent.add(txtMa);

		pnlContent.add(new JLabel("Tên Tàu:"));
		txtTen = new JTextField();
		pnlContent.add(txtTen);

		pnlContent.add(new JLabel("Số Toa:"));
		txtSoToa = new JTextField();
		pnlContent.add(txtSoToa);

		pnlContent.add(new JLabel("Trạng thái:"));
		cbTrangThai = new JComboBox<>(TrangThaiTau.values());
		// Hiển thị tiếng Việt trong ComboBox
		cbTrangThai.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof TrangThaiTau) {
					setText(((TrangThaiTau) value).getMoTa());
				}
				return this;
			}
		});
		pnlContent.add(cbTrangThai);

		add(pnlContent, BorderLayout.CENTER);

		// Nút bấm
		JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnSave = new JButton("Lưu");
		btnCancel = new JButton("Hủy");
		pnlButtons.add(btnSave);
		pnlButtons.add(btnCancel);
		add(pnlButtons, BorderLayout.SOUTH);

		// Sự kiện
		btnCancel.addActionListener(e -> dispose());
		btnSave.addActionListener(e -> {
			if (validateInput()) {
				confirmed = true;
				dispose();
			}
		});
	}

	private boolean validateInput() {
		if (txtMa.getText().isEmpty() || txtTen.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
			return false;
		}
		try {
			Integer.parseInt(txtSoToa.getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Số toa phải là số nguyên!");
			return false;
		}
		return true;
	}

	// Gán dữ liệu khi Sửa
	public void setEntity(Tau t) {
		isEditMode = true;
		txtMa.setText(t.getMaTau());
		txtMa.setEditable(false); // Không cho sửa khóa chính
		txtTen.setText(t.getTenTau());
		txtSoToa.setText(String.valueOf(t.getSoToa()));
		cbTrangThai.setSelectedItem(t.getTrangThaiTau());
	}

	// Lấy dữ liệu ra
	public Tau getEntity() {
		return new Tau(
				txtMa.getText(),
				txtTen.getText(),
				Integer.parseInt(txtSoToa.getText()),
				(TrangThaiTau) cbTrangThai.getSelectedItem()
		);
	}

	public boolean isConfirmed() { return confirmed; }
}