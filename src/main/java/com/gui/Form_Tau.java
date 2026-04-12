package com.gui;

import com.dao.DAO_Tau;
import com.entities.Tau;
import com.enums.TrangThaiTau;
import javax.swing.*;
import java.awt.*;

public class Form_Tau extends JDialog {
	private JTextField txtMa, txtTen, txtSoToa;
	private JComboBox<TrangThaiTau> cbTrangThai;
	private boolean confirmed = false;
	private Tau entity;

	public Form_Tau(Frame parent, String title) {
		super(parent, title, true);
		setSize(450, 350);
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
		txtMa = new JTextField(new DAO_Tau().phatSinhMaTau());
		txtMa.setEditable(false);
		txtMa.setBackground(new Color(0xEEF2F8));
		txtTen = new JTextField();
		txtSoToa = new JTextField();
		cbTrangThai = new JComboBox<>(TrangThaiTau.values());
		cbTrangThai.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
				super.getListCellRendererComponent(l, v, i, s, f);
				if (v instanceof TrangThaiTau)
					setText(((TrangThaiTau) v).getMoTa());
				return this;
			}
		});

		addRow(pnlForm, "Mã tàu (Tự động):", txtMa, 0, gc);
		addRow(pnlForm, "Tên mác tàu (*):", txtTen, 1, gc);
		addRow(pnlForm, "Số toa quy định:", txtSoToa, 2, gc);
		addRow(pnlForm, "Trạng thái:", cbTrangThai, 3, gc);
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
		btnSave.addActionListener(e -> {
			if (txtTen.getText().trim().isEmpty() || txtSoToa.getText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các trường bắt buộc (*)", "Cảnh báo",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!txtTen.getText().trim().matches("^(SE|TN|SN|SPT)\\d+$")) {
				JOptionPane.showMessageDialog(this,
						"Tên tàu không hợp lệ! Vui lòng nhập theo định dạng SE1, TN2, SN3...", "Lỗi định dạng",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				int soToa = Integer.parseInt(txtSoToa.getText().trim());
				if (soToa < 5 || soToa > 20)
					throw new Exception();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Số toa phải là số nguyên từ 5 đến 20!", "Lỗi nhập liệu",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			entity = new Tau(txtMa.getText().trim(), txtTen.getText().trim(),
					Integer.parseInt(txtSoToa.getText().trim()), (TrangThaiTau) cbTrangThai.getSelectedItem());
			confirmed = true;
			dispose();
		});
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

	public void setEntity(Tau t) {
		txtMa.setText(t.getMaTau());
		txtTen.setText(t.getTenTau());
		txtSoToa.setText(t.getSoToa() + "");
		cbTrangThai.setSelectedItem(t.getTrangThaiTau());
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public Tau getEntity() {
		return entity;
	}
}