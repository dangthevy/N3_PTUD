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
	private static final Color BG_PAGE = new Color(0xF4F7FB);
	private static final Color BG_CARD = Color.WHITE;
	private static final Color ACCENT = new Color(0x1A5EAB);
	private static final Color TEXT_DARK = new Color(0x1E2B3C);
	private static final Color TEXT_MID = new Color(0x5A6A7D);
	private static final Color BORDER_CLR = new Color(0xE2EAF4);
	private static final Color SUCCESS = new Color(0x27AE60);
	private static final Color DANGER = new Color(0xDC3545);

	private DefaultTableModel model;
	private JTable table;
	private DAO_Toa dao = new DAO_Toa();
	private DAO_Tau daoTau = new DAO_Tau();
	private Runnable onBack;

	private JTextField txtSearch;
	private JComboBox<String> cbLocTrangThai, cbLocTau;
	private TableRowSorter<DefaultTableModel> sorter;

	public TAB_Toa() {
		this(null);
	}

	public TAB_Toa(Runnable onBack) {
		this.onBack = onBack;
		setLayout(new BorderLayout(15, 20));
		setBackground(BG_PAGE);
		setBorder(new EmptyBorder(20, 20, 20, 20));

		JPanel pnlTop = new JPanel(new BorderLayout(0, 15));
		pnlTop.setOpaque(false);

		// Header
		JPanel pnlHeaderRow = new JPanel(new BorderLayout());
		pnlHeaderRow.setOpaque(false);

		JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pnlTitle.setOpaque(false);
		if (onBack != null) {
			JButton btnBack = makeBtn("<- Quay lại", TEXT_MID, Color.WHITE);
			btnBack.setPreferredSize(new Dimension(110, 36));
			btnBack.addActionListener(e -> onBack.run());
			pnlTitle.add(btnBack);
			pnlTitle.add(Box.createHorizontalStrut(10));
		}
		JLabel lblTitle = new JLabel("QUẢN LÝ TOA VẬT LÝ (KHO)");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
		lblTitle.setForeground(ACCENT);
		pnlTitle.add(lblTitle);
		pnlHeaderRow.add(pnlTitle, BorderLayout.WEST);

		JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlAction.setOpaque(false);
		JButton btnStatus = makeBtn("Đổi Trạng Thái", ACCENT, Color.WHITE);
		JButton btnAdd = makeBtn("Thêm Toa", ACCENT, Color.WHITE);
		JButton btnDel = makeBtn("Xóa Toa", DANGER, Color.WHITE);

		btnStatus.addActionListener(e -> updateStatus());
		btnAdd.addActionListener(e -> {
			Form_Toa f = new Form_Toa(JOptionPane.getFrameForComponent(this), "Thêm Toa Vật Lý Mới");
			f.setupForAdd();
			f.setVisible(true);
			if (f.isConfirmed() && dao.insertToa(f.getEntity())) {
				refreshData();
				JOptionPane.showMessageDialog(this, "Thêm Toa thành công!");
			}
		});
		btnDel.addActionListener(e -> {
			int r = table.getSelectedRow();
			if (r < 0) {
				JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 toa để xóa!");
				return;
			}
			int modelRow = table.convertRowIndexToModel(r);
			if (JOptionPane.showConfirmDialog(this, "Xóa toa này khỏi hệ thống?", "Xác nhận",
					JOptionPane.YES_NO_OPTION) == 0) {
				if (dao.deleteToa(model.getValueAt(modelRow, 0).toString()))
					refreshData();
				else
					JOptionPane.showMessageDialog(this,
							"Lỗi: Toa đang được lắp trên tàu. Vui lòng tháo dỡ trước khi xóa!", "Ràng buộc",
							JOptionPane.ERROR_MESSAGE);
			}
		});

		pnlAction.add(btnStatus);
		pnlAction.add(btnDel);
		pnlAction.add(btnAdd);
		pnlHeaderRow.add(pnlAction, BorderLayout.EAST);

		// Filter
		JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		pnlFilter.setBackground(BG_CARD);
		pnlFilter.setBorder(new ShadowBorder());

		txtSearch = new JTextField();
		txtSearch.setPreferredSize(new Dimension(250, 36));
		txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		txtSearch.setBorder(
				BorderFactory.createCompoundBorder(new LineBorder(BORDER_CLR), new EmptyBorder(0, 10, 0, 10)));
		txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Mã, Tên Toa...");

		cbLocTrangThai = new JComboBox<>(new String[] { "Tất cả trạng thái", "Sẵn sàng", "Bảo trì" });
		cbLocTrangThai.setPreferredSize(new Dimension(160, 36));
		cbLocTrangThai.setBackground(Color.WHITE);
		cbLocTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		cbLocTau = new JComboBox<>();
		cbLocTau.setPreferredSize(new Dimension(200, 36));
		cbLocTau.setBackground(Color.WHITE);
		cbLocTau.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		JLabel l1 = new JLabel("Tìm kiếm:");
		l1.setFont(new Font("Segoe UI", Font.BOLD, 13));
		l1.setForeground(TEXT_MID);
		JLabel l2 = new JLabel("Trạng thái:");
		l2.setFont(new Font("Segoe UI", Font.BOLD, 13));
		l2.setForeground(TEXT_MID);
		JLabel l3 = new JLabel("Vị trí:");
		l3.setFont(new Font("Segoe UI", Font.BOLD, 13));
		l3.setForeground(TEXT_MID);

		pnlFilter.add(l1);
		pnlFilter.add(txtSearch);
		pnlFilter.add(l2);
		pnlFilter.add(cbLocTrangThai);
		pnlFilter.add(l3);
		pnlFilter.add(cbLocTau);

		pnlTop.add(pnlHeaderRow, BorderLayout.NORTH);
		pnlTop.add(pnlFilter, BorderLayout.CENTER);

		// Table
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
			if (cbLocTrangThai.getSelectedIndex() == 1)
				filters.add(RowFilter.regexFilter("^Sẵn sàng$", 4));
			else if (cbLocTrangThai.getSelectedIndex() == 2)
				filters.add(RowFilter.regexFilter("^Bảo trì$", 4));
			if (cbLocTau.getSelectedIndex() == 1)
				filters.add(RowFilter.regexFilter("KHO TRỐNG", 5));
			else if (cbLocTau.getSelectedIndex() > 1)
				filters.add(RowFilter.regexFilter(cbLocTau.getSelectedItem().toString(), 5));
			sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
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
		pnlTable.setBackground(BG_CARD);
		pnlTable.setBorder(new ShadowBorder());
		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(BG_CARD);
		pnlTable.add(scroll);

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

	// --- LOGIC GIỮ NGUYÊN ---
	private void updateStatus() {
		int r = table.getSelectedRow();
		if (r < 0) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 toa để cập nhật!");
			return;
		}
		int modelRow = table.convertRowIndexToModel(r);
		String maToa = model.getValueAt(modelRow, 0).toString();
		String currentStatus = model.getValueAt(modelRow, 4).toString();
		if (!model.getValueAt(modelRow, 5).toString().contains("KHO TRỐNG")) {
			JOptionPane.showMessageDialog(this,
					"Toa đang được gắn trên tàu!\nVui lòng gỡ toa về kho trước khi đem đi bảo trì.", "Ràng buộc",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		String newStatusStr = currentStatus.equals("Bảo trì") ? "SAN_SANG" : "BAO_TRI";
		String msg = currentStatus.equals("Bảo trì") ? "Chuyển toa này sang trạng thái [SẴN SÀNG]?"
				: "Chuyển toa này đi [BẢO TRÌ]?";
		if (JOptionPane.showConfirmDialog(this, msg, "Cập nhật", JOptionPane.YES_NO_OPTION) == 0) {
			if (dao.updateTrangThai(maToa, newStatusStr)) {
				refreshData();
				JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
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
			String viTri = obj[5] == null ? "KHO TRỐNG" : "Gắn trên " + obj[5].toString();
			String trangThai = obj[4].toString().equals("BAO_TRI") ? "Bảo trì" : "Sẵn sàng";
			model.addRow(new Object[] { obj[0], obj[1], obj[2], obj[3] + " chỗ", trangThai, viTri });
		}
	}

	// --- UI HELPERS ---
	private JButton makeBtn(String text, Color bg, Color fg) {
		JButton b = new JButton(text) {
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
		b.setForeground(fg);
		b.setFont(new Font("Segoe UI", Font.BOLD, 13));
		b.setPreferredSize(new Dimension(140, 38));
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
		t.setShowVerticalLines(false);
		t.setShowHorizontalLines(true);
		t.setGridColor(BORDER_CLR);
		t.setSelectionBackground(new Color(226, 234, 244));
		t.setSelectionForeground(TEXT_DARK);
		t.setFocusable(false);
		t.setIntercellSpacing(new Dimension(0, 0));

		// ĐÃ FIX: Thiết lập kích thước Header
		t.getTableHeader().setPreferredSize(new Dimension(0, 42));
		t.getTableHeader().setDefaultRenderer(new HeaderRenderer());

		t.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
		return t;
	}

	private static class HeaderRenderer extends DefaultTableCellRenderer {
		HeaderRenderer() {
			setHorizontalAlignment(LEFT);
		}

		@Override
		public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
			JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
			l.setOpaque(true);
			l.setBackground(ACCENT);
			l.setForeground(Color.WHITE);
			l.setFont(new Font("Segoe UI", Font.BOLD, 13));
			l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			return l;
		}
	}

	private static class StatusRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (!isSelected) {
				if ("Sẵn sàng".equals(value)) {
					l.setForeground(SUCCESS);
				} else if ("Bảo trì".equals(value)) {
					l.setForeground(DANGER);
				} else {
					l.setForeground(TEXT_DARK);
				}
			}
			l.setFont(new Font("Segoe UI", Font.BOLD, 13));
			return l;
		}
	}

	private static class ShadowBorder extends AbstractBorder {
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(BORDER_CLR);
			g2.drawRoundRect(x, y, w - 1, h - 1, 10, 10);
		}

		public Insets getBorderInsets(Component c) {
			return new Insets(1, 1, 1, 1);
		}
	}
}