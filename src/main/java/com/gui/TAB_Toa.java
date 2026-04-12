package com.gui;

import com.dao.DAO_Tau;
import com.dao.DAO_Toa;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class TAB_Toa extends JPanel {
	private DefaultTableModel model;
	private JTable table;
	private DAO_Toa dao = new DAO_Toa();
	private DAO_Tau daoTau = new DAO_Tau();
	private Runnable onBack;

	private JTextField txtSearch;
	private JComboBox<String> cbLocTrangThai, cbLocTau;
	private TableRowSorter<DefaultTableModel> sorter;
	private static final Color ACCENT = new Color(0x1A5EAB);
	public TAB_Toa() {
		this(null);
	}

	public TAB_Toa(Runnable onBack) {
		this.onBack = onBack;
		setLayout(new BorderLayout(10, 15));
		setBackground(new Color(0xF4F7FB));
		setBorder(new EmptyBorder(15, 15, 15, 15));

		JPanel pnlTop = new JPanel(new BorderLayout(0, 10));
		pnlTop.setOpaque(false);
		JPanel pnlRow1 = new JPanel(new BorderLayout());
		pnlRow1.setOpaque(false);
		JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		pnlTitle.setOpaque(false);
		if (onBack != null) {
			JButton btnBack = makeBtn("<-Quay lại", new Color(108, 122, 137));
			btnBack.setPreferredSize(new Dimension(110, 36));
			btnBack.addActionListener(e -> onBack.run());
			pnlTitle.add(btnBack);
		}
		JLabel lblTitle = new JLabel(" QUẢN LÝ TOA VẬT LÝ (KHO)");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitle.setForeground(new Color(0x1A5EAB));
		pnlTitle.add(lblTitle);
		pnlRow1.add(pnlTitle, BorderLayout.WEST);

		JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlAction.setOpaque(false);
		JButton btnStatus = makeBtn("Update Trạng Thái", ACCENT);

		// ĐÃ ĐỔI MÀU NÚT THÊM THÀNH XANH TIÊU ĐỀ BẢNG
		JButton btnAdd = makeBtn("+ Thêm Toa", ACCENT);

		JButton btnDel = makeBtn("- Xóa Toa", new Color(192, 57, 43));

		btnStatus.addActionListener(e -> updateStatus());
		btnAdd.addActionListener(e -> {
			Form_Toa f = new Form_Toa(JOptionPane.getFrameForComponent(this), "Sản Xuất Toa Mới");
			f.setupForAdd();
			f.setVisible(true);
			if (f.isConfirmed() && dao.insertToa(f.getEntity())) {
				refreshData();
				JOptionPane.showMessageDialog(this, "Đã lưu Toa vào kho thành công!");
			}
		});
		btnDel.addActionListener(e -> {
			int r = table.getSelectedRow();
			if (r < 0) {
				JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 toa để xóa!", "Thông báo",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			int modelRow = table.convertRowIndexToModel(r);
			if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn xóa toa này khỏi hệ thống?", "Xác nhận xóa",
					JOptionPane.YES_NO_OPTION) == 0) {
				if (dao.deleteToa(model.getValueAt(modelRow, 0).toString()))
					refreshData();
				else
					JOptionPane.showMessageDialog(this,
							"Lỗi: Toa này đang được lắp ráp trên một Đoàn tàu. Vui lòng tháo dỡ trước khi xóa!",
							"Ràng buộc", JOptionPane.ERROR_MESSAGE);
			}
		});

		pnlAction.add(btnStatus);
		pnlAction.add(btnDel);
		pnlAction.add(btnAdd);
		pnlRow1.add(pnlAction, BorderLayout.EAST);

		JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
		pnlFilter.setOpaque(false);
		pnlFilter.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 0, 0, new Color(0xDDE6F5)),
				new EmptyBorder(10, 0, 5, 0)));
		txtSearch = new JTextField();
		txtSearch.setPreferredSize(new Dimension(200, 32));
		txtSearch.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xDDE6F5), 1, true),
				new EmptyBorder(2, 6, 2, 6)));
		cbLocTrangThai = new JComboBox<>(new String[] { "Tất cả trạng thái", "Sẵn sàng", "Bảo trì" });
		cbLocTrangThai.setPreferredSize(new Dimension(150, 32));
		cbLocTrangThai.setBackground(Color.WHITE);
		cbLocTau = new JComboBox<>();
		cbLocTau.setPreferredSize(new Dimension(200, 32));
		cbLocTau.setBackground(Color.WHITE);
		pnlFilter.add(new JLabel("🔍 Tìm kiếm:"));
		pnlFilter.add(txtSearch);
		pnlFilter.add(new JLabel("📌 Trạng thái:"));
		pnlFilter.add(cbLocTrangThai);
		pnlFilter.add(new JLabel("📍 Vị trí:"));
		pnlFilter.add(cbLocTau);

		pnlTop.add(pnlRow1, BorderLayout.NORTH);
		pnlTop.add(pnlFilter, BorderLayout.CENTER);

		model = new DefaultTableModel(
				new String[] { "Mã Toa", "Tên Toa", "Loại Toa", "Sức Chứa", "Trạng Thái", "Vị Trí Hiện Tại" }, 0);
		table = buildTable(model);
		sorter = new TableRowSorter<>(model);
		table.setRowSorter(sorter);

		Runnable applyFilter = () -> {
			List<RowFilter<Object, Object>> filters = new ArrayList<>();
			String text = txtSearch.getText().trim();
			if (!text.isEmpty())
				filters.add(RowFilter.regexFilter("(?i)" + text, 0, 1));
			int idxTT = cbLocTrangThai.getSelectedIndex();
			if (idxTT == 1)
				filters.add(RowFilter.regexFilter("^Sẵn sàng$", 4));
			else if (idxTT == 2)
				filters.add(RowFilter.regexFilter("^Bảo trì$", 4));
			int idxTau = cbLocTau.getSelectedIndex();
			if (idxTau == 1)
				filters.add(RowFilter.regexFilter("KHO TRỐNG", 5));
			else if (idxTau > 1)
				filters.add(RowFilter.regexFilter(cbLocTau.getSelectedItem().toString(), 5));
			if (filters.isEmpty())
				sorter.setRowFilter(null);
			else
				sorter.setRowFilter(RowFilter.andFilter(filters));
		};

		txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				applyFilter.run();
			}

			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				applyFilter.run();
			}

			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				applyFilter.run();
			}
		});
		cbLocTrangThai.addActionListener(e -> applyFilter.run());
		cbLocTau.addActionListener(e -> applyFilter.run());
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

	private void updateStatus() {
		int r = table.getSelectedRow();
		if (r < 0) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 toa để cập nhật!", "Thông báo",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		int modelRow = table.convertRowIndexToModel(r);
		String maToa = model.getValueAt(modelRow, 0).toString();
		String currentStatus = model.getValueAt(modelRow, 4).toString();
		String viTri = model.getValueAt(modelRow, 5).toString();
		if (!viTri.contains("KHO TRỐNG")) {
			JOptionPane.showMessageDialog(this,
					"Toa đang được gắn trên tàu!\nVui lòng gỡ toa về kho trước khi đem đi bảo trì.",
					"Ràng buộc hệ thống", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String newStatusStr = currentStatus.equals("Bảo trì") ? "SAN_SANG" : "BAO_TRI";
		String msg = currentStatus.equals("Bảo trì") ? "Chuyển toa này sang trạng thái [SẴN SÀNG]?"
				: "Chuyển toa này đi [BẢO TRÌ]?";
		if (JOptionPane.showConfirmDialog(this, msg, "Cập nhật trạng thái", JOptionPane.YES_NO_OPTION) == 0) {
			if (dao.updateTrangThai(maToa, newStatusStr)) {
				refreshData();
				JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thành công!");
			}
		}
	}

	private void handleEdit(int r) {
		int modelRow = table.convertRowIndexToModel(r);
		Form_Toa f = new Form_Toa(JOptionPane.getFrameForComponent(this), "Sửa Thông Tin Toa");
		f.setEntity(dao.getToaById(model.getValueAt(modelRow, 0).toString()));
		f.setVisible(true);
		if (f.isConfirmed() && dao.updateToa(f.getEntity()))
			refreshData();
	}

	private void refreshData() {
		String oldLocTau = cbLocTau.getSelectedItem() != null ? cbLocTau.getSelectedItem().toString() : "Tất cả vị trí";
		cbLocTau.removeAllItems();
		cbLocTau.addItem("Tất cả vị trí");
		cbLocTau.addItem("Kho trống (Chưa gắn)");
		for (com.entities.Tau t : daoTau.getAllTau())
			cbLocTau.addItem("Gắn trên " + t.getMaTau());
		cbLocTau.setSelectedItem(oldLocTau);
		model.setRowCount(0);
		for (Object[] obj : dao.getAllToaWithViTri()) {
			String viTri = obj[5] == null ? "🏭 KHO TRỐNG" : "🚂 Gắn trên " + obj[5].toString();
			String trangThai = obj[4].toString().equals("BAO_TRI") ? "Bảo trì" : "Sẵn sàng";
			model.addRow(new Object[] { obj[0], obj[1], obj[2], obj[3] + " chỗ", trangThai, viTri });
		}
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
		t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				String status = table.getValueAt(row, 4).toString();
				if (!isSelected) {
					c.setForeground(status.equals("Bảo trì") ? new Color(192, 57, 43) : new Color(0x1E2B3C));
				}
				if (column == 4)
					setFont(new Font("Segoe UI", Font.BOLD, 13));
				return c;
			}
		});
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