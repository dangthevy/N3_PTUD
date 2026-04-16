package com.gui;

import com.dao.DAO_LoaiToa;
import com.entities.LoaiToa;
import com.connectDB.ConnectDB;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;

public class TAB_LoaiToa extends JPanel {
	// ================= COLOR =================
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
	private DAO_LoaiToa dao = new DAO_LoaiToa();
	private Runnable onBack;

	public TAB_LoaiToa() {
		this(null);
	}

	public TAB_LoaiToa(Runnable onBack) {
		this.onBack = onBack;
		setLayout(new BorderLayout(15, 20));
		setBackground(BG_PAGE);
		setBorder(new EmptyBorder(20, 20, 20, 20));

		// --- TOP PANEL ---
		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.setOpaque(false);

		JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		pnlTitle.setOpaque(false);

		if (onBack != null) {
			JButton btnBack = makeBtn("<- Quay lại", TEXT_MID, Color.WHITE);
			btnBack.setPreferredSize(new Dimension(110, 36));
			btnBack.addActionListener(e -> onBack.run());
			pnlTitle.add(btnBack);
		}

		JLabel lblTitle = new JLabel("QUẢN LÝ KHUÔN MẪU LOẠI TOA");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
		lblTitle.setForeground(ACCENT);
		pnlTitle.add(lblTitle);

		JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlActions.setOpaque(false);

		JButton btnAdd = makeBtn("Thêm loại mới", ACCENT, Color.WHITE);
		JButton btnDelete = makeBtn("Xóa loại toa", DANGER, Color.WHITE);

		btnAdd.addActionListener(e -> handleAdd());
		btnDelete.addActionListener(e -> handleDelete());

		pnlActions.add(btnDelete);
		pnlActions.add(btnAdd);

		pnlTop.add(pnlTitle, BorderLayout.WEST);
		pnlTop.add(pnlActions, BorderLayout.EAST);

		// --- TABLE CARD ---
		String[] cols = { "Mã Loại", "Tên Loại Toa", "Số Hàng", "Số Cột", "Kiểu Hiển Thị", "Sức Chứa" };
		model = new DefaultTableModel(cols, 0);
		table = buildTable(model);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2)
					handleEdit();
			}
		});

		JPanel pnlTableCard = new JPanel(new BorderLayout());
		pnlTableCard.setBackground(BG_CARD);
		pnlTableCard.setBorder(new ShadowBorder());

		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(BG_CARD);
		pnlTableCard.add(scroll, BorderLayout.CENTER);

		add(pnlTop, BorderLayout.NORTH);
		add(pnlTableCard, BorderLayout.CENTER);

		loadData();
	}

	// --- LOGIC GIỮ NGUYÊN ---
	private void handleAdd() {
		Form_LoaiToa f = new Form_LoaiToa(JOptionPane.getFrameForComponent(this), "Thêm Khuôn Mẫu Mới");
		f.setVisible(true);
		if (f.isConfirmed()) {
			if (dao.insertLoaiToa(f.getEntity())) {
				loadData();
				JOptionPane.showMessageDialog(this, "Thêm loại toa thành công!");
			} else
				JOptionPane.showMessageDialog(this, "Thêm thất bại (Trùng mã hoặc lỗi CSDL)!", "Lỗi",
						JOptionPane.ERROR_MESSAGE);
		}
	}

	private void handleEdit() {
		int r = table.getSelectedRow();
		if (r < 0)
			return;
		String maLT = table.getValueAt(r, 0).toString();
		boolean isUsed = checkLoaiToaIsUsed(maLT);

		Form_LoaiToa f = new Form_LoaiToa(JOptionPane.getFrameForComponent(this), "Sửa Khuôn Mẫu Loại Toa");
		f.setEditData(maLT, table.getValueAt(r, 1).toString(), Integer.parseInt(table.getValueAt(r, 2).toString()),
				Integer.parseInt(table.getValueAt(r, 3).toString()), table.getValueAt(r, 4).toString());
		if (isUsed)
			f.lockDimensions();
		f.setVisible(true);
		if (f.isConfirmed()) {
			if (dao.updateLoaiToa(f.getEntity())) {
				loadData();
				JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
			} else
				JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void handleDelete() {
		int r = table.getSelectedRow();
		if (r < 0) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 Loại Toa trong bảng để xóa!", "Thông báo",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		String maLT = table.getValueAt(r, 0).toString();
		String tenLT = table.getValueAt(r, 1).toString();
		if (checkLoaiToaIsUsed(maLT)) {
			JOptionPane.showMessageDialog(this, "⚠️ KHÔNG THỂ XÓA!\n\nLoại toa [" + tenLT
					+ "] này đang được sử dụng bởi các Toa vật lý trong hệ thống.\nVui lòng xóa các Toa thuộc loại này trước.",
					"Lỗi Ràng Buộc Dữ Liệu", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(this, "Xóa vĩnh viễn khuôn mẫu: " + tenLT + "?", "Xác nhận xóa",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
			if (dao.deleteLoaiToa(maLT)) {
				loadData();
				JOptionPane.showMessageDialog(this, "Xóa thành công!");
			} else
				JOptionPane.showMessageDialog(this, "Xóa thất bại! Lỗi CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}

	private boolean checkLoaiToaIsUsed(String maLoaiToa) {
		try (Connection c = ConnectDB.getConnection();
				PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM Toa WHERE maLoaiToa = ?")) {
			ps.setString(1, maLoaiToa);
			ResultSet rs = ps.executeQuery();
			if (rs.next() && rs.getInt(1) > 0)
				return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public void loadData() {
		model.setRowCount(0);
		for (LoaiToa lt : dao.getAllLoaiToa()) {
			model.addRow(new Object[] { lt.getMaLoaiToa(), lt.getTenLoaiToa(), lt.getSoHang(), lt.getSoCot(),
					lt.getKieuHienThi(), (lt.getSoHang() * lt.getSoCot()) + " chỗ" });
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

		// ĐÃ FIX: Thiết lập kích thước Header
		t.getTableHeader().setPreferredSize(new Dimension(0, 42));
		t.getTableHeader().setDefaultRenderer(new HeaderRenderer());

		t.setSelectionBackground(new Color(226, 234, 244));
		t.setSelectionForeground(TEXT_DARK);
		t.setIntercellSpacing(new Dimension(0, 0));
		t.setFocusable(false);
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