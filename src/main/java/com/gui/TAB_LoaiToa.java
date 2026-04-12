package com.gui;

import com.dao.DAO_LoaiToa;
import com.entities.LoaiToa;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class TAB_LoaiToa extends JPanel {
	private DefaultTableModel model;
	private JTable table;
	private DAO_LoaiToa dao = new DAO_LoaiToa();
	private Runnable onBack;
	private static final Color ACCENT = new Color(0x1A5EAB);
	
	public TAB_LoaiToa() {
		this(null);
	}

	public TAB_LoaiToa(Runnable onBack) {
		this.onBack = onBack;
		setLayout(new BorderLayout(10, 15));
		setBackground(new Color(0xF4F7FB));
		setBorder(new EmptyBorder(15, 15, 15, 15));
		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.setOpaque(false);
		JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		pnlTitle.setOpaque(false);

		if (onBack != null) {
			JButton btnBack = makeBtn("<-Quay lại", new Color(108, 122, 137));
			btnBack.setPreferredSize(new Dimension(110, 36));
			btnBack.addActionListener(e -> onBack.run());
			pnlTitle.add(btnBack);
		}

		JLabel lblTitle = new JLabel(" QUẢN LÝ CẤU HÌNH LOẠI TOA (TEMPLATE)");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitle.setForeground(new Color(0x1A5EAB));
		pnlTitle.add(lblTitle);
		JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlAction.setOpaque(false);
		JButton btnAdd = makeBtn("+ Thêm Loại Toa", ACCENT);
		JButton btnDel = makeBtn("- Xóa Loại Toa", new Color(192, 57, 43));

		btnAdd.addActionListener(e -> {
			Form_LoaiToa f = new Form_LoaiToa(JOptionPane.getFrameForComponent(this), "Thêm Loại Toa");
			f.setVisible(true);
			if (f.isConfirmed()) {
				LoaiToa lt = new LoaiToa(f.getMa(), f.getTen(), f.getHang(), f.getCot(), f.getKieu());
				if (dao.insertLoaiToa(lt))
					refreshData();
				else
					JOptionPane.showMessageDialog(this, "Mã loại toa đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		});

		btnDel.addActionListener(e -> {
			int r = table.getSelectedRow();
			if (r < 0) {
				JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng để xóa!", "Thông báo",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (JOptionPane.showConfirmDialog(this, "Xóa loại toa này?", "Xóa", JOptionPane.YES_NO_OPTION) == 0) {
				if (dao.deleteLoaiToa(model.getValueAt(r, 0).toString()))
					refreshData();
				else
					JOptionPane.showMessageDialog(this,
							"Loại toa này đang được sử dụng bởi các Toa trong Kho. Không thể xóa!", "Lỗi ràng buộc",
							JOptionPane.ERROR_MESSAGE);
			}
		});

		pnlAction.add(btnDel);
		pnlAction.add(btnAdd);
		pnlTop.add(pnlTitle, BorderLayout.WEST);
		pnlTop.add(pnlAction, BorderLayout.EAST);
		model = new DefaultTableModel(new String[] { "Mã Loại", "Tên Loại", "Số Hàng", "Số Cột", "Kiểu Hiển Thị" }, 0);
		table = buildTable(model);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int r = table.getSelectedRow();
					if (r >= 0)
						handleEdit(r);
				}
			}
		});

		JPanel pnlTable = new JPanel(new BorderLayout());
		pnlTable.setBackground(Color.WHITE);
		pnlTable.setBorder(new ShadowBorder());
		pnlTable.add(new JScrollPane(table));
		add(pnlTop, BorderLayout.NORTH);
		add(pnlTable, BorderLayout.CENTER);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				refreshData();
			}
		});
		refreshData();
	}

	private void handleEdit(int r) {
		Form_LoaiToa f = new Form_LoaiToa(JOptionPane.getFrameForComponent(this), "Sửa Loại Toa");
		f.setEditData(model.getValueAt(r, 0).toString(), model.getValueAt(r, 1).toString(),
				(int) model.getValueAt(r, 2), (int) model.getValueAt(r, 3), model.getValueAt(r, 4).toString());
		f.setVisible(true);
		if (f.isConfirmed()) {
			LoaiToa lt = new LoaiToa(f.getMa(), f.getTen(), f.getHang(), f.getCot(), f.getKieu());
			if (dao.updateLoaiToa(lt))
				refreshData();
		}
	}

	private void refreshData() {
		model.setRowCount(0);
		for (LoaiToa lt : dao.getAllLoaiToa())
			model.addRow(new Object[] { lt.getMaLoaiToa(), lt.getTenLoaiToa(), lt.getSoHang(), lt.getSoCot(),
					lt.getKieuHienThi() });
	}

	private JButton makeBtn(String t, Color bg) {
		JButton b = new JButton(t) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isRollover() ? bg.darker() : bg);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		b.setForeground(Color.WHITE);
		b.setFont(new Font("Segoe UI", Font.BOLD, 13));
		b.setPreferredSize(new Dimension(140, 36));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JTable buildTable(DefaultTableModel m) {
		JTable t = new JTable(m) {
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		t.setRowHeight(40);
		t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		t.getTableHeader().setPreferredSize(new Dimension(0, 42));
		t.getTableHeader().setBackground(new Color(0x1A5EAB));
		t.getTableHeader().setForeground(Color.WHITE);
		t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
		t.setSelectionBackground(new Color(221, 238, 255));
		return t;
	}

	private static class ShadowBorder extends AbstractBorder {
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(new Color(226, 234, 244));
			g2.drawRoundRect(x, y, w - 1, h - 1, 10, 10);
			g2.dispose();
		}

		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(1, 1, 1, 1);
		}
	}
}