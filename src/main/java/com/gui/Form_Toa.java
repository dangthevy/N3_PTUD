package com.gui;

import com.dao.DAO_LoaiToa;
import com.dao.DAO_Toa;
import com.entities.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Form_Toa extends JDialog {
	private JTextField txtMaToa, txtTenToa, txtSoGhe;
	private JComboBox<LoaiToaWrapper> cbLoaiToa;
	private boolean confirmed = false;
	private Toa toaEntity;
	private List<LoaiToa> dsLoaiToa;

	class LoaiToaWrapper {
		LoaiToa lt;
		int ghe;

		public LoaiToaWrapper(LoaiToa lt, int ghe) {
			this.lt = lt;
			this.ghe = ghe;
		}

		@Override
		public String toString() {
			return lt.getTenLoaiToa() + " - " + ghe + " chỗ (" + lt.getMaLoaiToa() + ")";
		}
	}

	public Form_Toa(Frame parent, String title) {
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
		txtMaToa = new JTextField();
		txtTenToa = new JTextField();
		txtSoGhe = new JTextField();
		txtSoGhe.setEditable(false);
		txtSoGhe.setBackground(new Color(0xEEF2F8));

		cbLoaiToa = new JComboBox<>();
		dsLoaiToa = new DAO_LoaiToa().getAllLoaiToa();
		for (LoaiToa lt : dsLoaiToa)
			cbLoaiToa.addItem(new LoaiToaWrapper(lt, lt.getSoHang() * lt.getSoCot()));

		cbLoaiToa.addActionListener(e -> {
			LoaiToaWrapper w = (LoaiToaWrapper) cbLoaiToa.getSelectedItem();
			if (w != null) {
				txtSoGhe.setText(w.ghe + "");
				String tenLoai = w.lt.getTenLoaiToa().toLowerCase();
				if (tenLoai.contains("nằm"))
					txtTenToa.setText("Toa giường nằm điều hòa");
				else if (tenLoai.contains("mềm"))
					txtTenToa.setText("Toa ghế ngồi mềm chất lượng cao");
				else
					txtTenToa.setText("Toa ghế ngồi cứng");
			}
		});
		if (cbLoaiToa.getItemCount() > 0) {
			txtSoGhe.setText(((LoaiToaWrapper) cbLoaiToa.getSelectedItem()).ghe + "");
			txtTenToa.setText("Toa ghế ngồi cứng");
		}

		addRow(pnlForm, "Mã toa (Tự động):", txtMaToa, 0, gc);
		addRow(pnlForm, "Loại cấu hình:", cbLoaiToa, 1, gc);
		addRow(pnlForm, "Tên mô tả toa (*):", txtTenToa, 2, gc);
		addRow(pnlForm, "Sức chứa:", txtSoGhe, 3, gc);
		pnlMain.add(pnlForm, BorderLayout.CENTER);

		JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlBottom.setOpaque(false);
		JButton btnCancel = new JButton("Hủy");
		JButton btnSave = new JButton("Lưu");
		btnSave.setBackground(new Color(0x1A5EAB));
		btnSave.setForeground(Color.WHITE);
		btnCancel.addActionListener(e -> dispose());
		btnSave.addActionListener(e -> {
			if (txtTenToa.getText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Vui lòng nhập tên mô tả toa!");
				return;
			}
			toaEntity = new Toa(txtMaToa.getText().trim(), txtTenToa.getText().trim(),
					Integer.parseInt(txtSoGhe.getText().trim()), ((LoaiToaWrapper) cbLoaiToa.getSelectedItem()).lt,
					"SAN_SANG");
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
		gc.weightx = 0.35;
		JLabel lbl = new JLabel(l);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
		p.add(lbl, gc);
		gc.gridx = 1;
		gc.weightx = 0.65;
		c.setPreferredSize(new Dimension(0, 32));
		p.add(c, gc);
	}

	public void setupForAdd() {
		txtMaToa.setText(new DAO_Toa().phatSinhMaToa());
		txtMaToa.setEditable(false);
		txtMaToa.setBackground(new Color(0xEEF2F8));
	}

	public void setEntity(Toa t) {
		txtMaToa.setText(t.getMaToa());
		txtMaToa.setEditable(false);
		txtMaToa.setBackground(new Color(0xEEF2F8));
		txtTenToa.setText(t.getTenToa());
		for (int i = 0; i < cbLoaiToa.getItemCount(); i++)
			if (cbLoaiToa.getItemAt(i).lt.getMaLoaiToa().equals(t.getLoaiToa().getMaLoaiToa()))
				cbLoaiToa.setSelectedIndex(i);
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public Toa getEntity() {
		return toaEntity;
	}
}