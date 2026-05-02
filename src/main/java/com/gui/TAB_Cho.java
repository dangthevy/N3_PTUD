package com.gui;

import com.connectDB.ConnectDB;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TAB_Cho extends JPanel {
	private JTree tree;
	private DefaultTreeModel treeModel;
	private JPanel pnlMap;
	private JLabel lblMapTitle;

	public TAB_Cho() {
		setLayout(new BorderLayout(15, 15));
		setBackground(new Color(0xF4F7FB));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel pLeft = new JPanel(new BorderLayout(0, 10));
		pLeft.setBackground(Color.WHITE);
		pLeft.setPreferredSize(new Dimension(280, 0));
		pLeft.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(10, 10, 10, 10)));

		JLabel lblL = new JLabel(" 1. CẤU TRÚC ĐOÀN TÀU");
		lblL.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblL.setForeground(new Color(0x1A5EAB));
		pLeft.add(lblL, BorderLayout.NORTH);

		treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Đường Sắt VN"));
		tree = new JTree(treeModel);
		tree.setRowHeight(35);
		tree.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		tree.addTreeSelectionListener(e -> {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (n == null || !n.isLeaf() || n.getParent() == treeModel.getRoot()) {
				pnlMap.removeAll();
				pnlMap.repaint();
				lblMapTitle.setText("Vui lòng chọn Toa để xem trước sơ đồ");
				return;
			}
			generateSeatMap(n.getUserObject().toString().split(" - ")[0].replace("🎫 ", "").trim());
		});
		pLeft.add(new JScrollPane(tree), BorderLayout.CENTER);

		JPanel pRight = new JPanel(new BorderLayout(0, 15));
		pRight.setBackground(Color.WHITE);
		pRight.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(20, 20, 20, 20)));

		lblMapTitle = new JLabel("Vui lòng chọn Toa để xem trước sơ đồ", SwingConstants.CENTER);
		lblMapTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblMapTitle.setForeground(new Color(0x1A5EAB));

		pnlMap = new JPanel(new BorderLayout());
		pnlMap.setBackground(Color.WHITE);

		pRight.add(lblMapTitle, BorderLayout.NORTH);
		pRight.add(new JScrollPane(pnlMap), BorderLayout.CENTER);

		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pLeft, pRight);
		sp.setBorder(null);
		sp.setOpaque(false);
		add(sp);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				refreshData();
			}
		});

		// ==========================================
		// FIX: GỌI HÀM NÀY ĐỂ LOAD DATA NGAY KHI MỞ
		// ==========================================
		refreshData();
	}

	private void generateSeatMap(String maToa) {
		pnlMap.removeAll();
		lblMapTitle.setText("SƠ ĐỒ TRỰC QUAN: " + maToa);
		try (Connection c = ConnectDB.getConnection();
				ResultSet rs = c.createStatement().executeQuery(
						"SELECT l.soHang, l.soCot, l.kieuHienThi FROM Toa t JOIN LoaiToa l ON t.maLoaiToa=l.maLoaiToa WHERE t.maToa='"
								+ maToa + "'")) {
			if (rs.next()) {
				JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
				wrap.setBackground(Color.WHITE);
				if (rs.getString(3).equals("GIUONG"))
					wrap.add(drawSleeper(rs.getInt(1), rs.getInt(2)));
				else
					wrap.add(drawSeater(rs.getInt(1), rs.getInt(2)));
				pnlMap.add(wrap, BorderLayout.CENTER);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		pnlMap.revalidate();
		pnlMap.repaint();
	}

	private JPanel drawSleeper(int r, int c) {
		JPanel map = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		map.setBorder(new LineBorder(new Color(52, 73, 94), 3, true));
		map.setBackground(Color.WHITE);
		int khoang = (r * c) / 4;
		int idx = 1;
		for (int k = 1; k <= khoang; k++) {
			JPanel p = new JPanel(new BorderLayout(0, 5));
			p.setOpaque(false);
			p.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(0, 0, 0, k < khoang ? 3 : 0, new Color(52, 73, 94)),
					new EmptyBorder(10, 10, 10, 10)));
			JLabel lbl = new JLabel("Khoang " + k, SwingConstants.CENTER);
			lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
			p.add(lbl, BorderLayout.NORTH);
			JPanel g = new JPanel(new GridLayout(2, 2, 8, 8));
			g.setOpaque(false);
			g.add(btn(idx + 2, false));
			g.add(btn(idx + 3, false));
			g.add(btn(idx, true));
			g.add(btn(idx + 1, true));
			idx += 4;
			p.add(g);
			map.add(p);
		}
		return map;
	}

	private JPanel drawSeater(int r, int c) {
		JPanel map = new JPanel();
		map.setLayout(new BoxLayout(map, BoxLayout.X_AXIS));
		map.setBorder(new LineBorder(new Color(52, 73, 94), 3, true));
		map.setBackground(Color.WHITE);
		int half = r / 2;
		JPanel pL = new JPanel(new GridLayout(5, half, 6, 6));
		pL.setOpaque(false);
		pL.setBorder(new EmptyBorder(15, 15, 15, 10));
		JPanel door = new JPanel();
		door.setBackground(Color.LIGHT_GRAY);
		door.setPreferredSize(new Dimension(25, 200));
		door.setMaximumSize(new Dimension(25, 200));
		JPanel pR = new JPanel(new GridLayout(5, half, 6, 6));
		pR.setOpaque(false);
		pR.setBorder(new EmptyBorder(15, 10, 15, 15));
		int slT = 1, slB = 3, srT = (half * 4) + 1, srB = srT + 2;
		for (int i = 0; i < 5; i++) {
			if (i == 2) {
				for (int j = 0; j < half; j++) {
					pL.add(new JLabel());
					pR.add(new JLabel());
				}
				continue;
			}
			for (int j = 0; j < half; j++) {
				if (i < 2) {
					pL.add(btn(slT + (j * 4) + i, true));
					pR.add(btn(srT + (j * 4) + i, true));
				} else {
					pL.add(btn(slB + (j * 4) + (i - 3), true));
					pR.add(btn(srB + (j * 4) + (i - 3), true));
				}
			}
		}
		map.add(pL);
		map.add(door);
		map.add(pR);
		return map;
	}

	private JButton btn(int i, boolean isOrange) {
		JButton b = new JButton(i + "");
		b.setPreferredSize(new Dimension(45, 45));
		b.setFont(new Font("Segoe UI", Font.BOLD, 14));
		b.setFocusPainted(false);
		b.setBorder(new LineBorder(Color.GRAY, 1, true));
		if (isOrange) {
			b.setBackground(new Color(225, 80, 40));
			b.setForeground(Color.WHITE);
		} else {
			b.setBackground(Color.WHITE);
			b.setForeground(Color.BLACK);
		}
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private void refreshData() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
		root.removeAllChildren();

		List<String[]> listTau = new ArrayList<>();
		try (Connection c = ConnectDB.getConnection();
				Statement st = c.createStatement();
				ResultSet rs1 = st.executeQuery("SELECT maTau, tenTau FROM Tau")) {
			while (rs1.next()) {
				listTau.add(new String[] { rs1.getString(1), rs1.getString(2) });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try (Connection c = ConnectDB.getConnection()) {
			for (String[] tau : listTau) {
				DefaultMutableTreeNode n = new DefaultMutableTreeNode("🚂 " + tau[0] + " - " + tau[1]);
				try (Statement st = c.createStatement();
						ResultSet rs2 = st.executeQuery(
								"SELECT t.maToa, t.tenToa FROM ChiTietTau c JOIN Toa t ON c.maToa=t.maToa WHERE c.maTau='"
										+ tau[0] + "' ORDER BY c.thuTu")) {
					while (rs2.next())
						n.add(new DefaultMutableTreeNode("🎫 " + rs2.getString(1) + " - " + rs2.getString(2)));
				}
				root.add(n);
			}

			DefaultMutableTreeNode kho = new DefaultMutableTreeNode("🏭 KHO TOA TRỐNG");
			try (Statement st = c.createStatement();
					ResultSet rs3 = st.executeQuery(
							"SELECT maToa, tenToa FROM Toa WHERE maToa NOT IN (SELECT maToa FROM ChiTietTau)")) {
				while (rs3.next())
					kho.add(new DefaultMutableTreeNode("🎫 " + rs3.getString(1) + " - " + rs3.getString(2)));
			}
			root.add(kho);
		} catch (Exception e) {
			e.printStackTrace();
		}

		treeModel.reload();
		for (int i = 0; i < tree.getRowCount(); i++)
			tree.expandRow(i);
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