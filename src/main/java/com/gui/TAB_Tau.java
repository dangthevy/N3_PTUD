package com.gui;

import com.dao.*;
import com.entities.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import com.connectDB.ConnectDB;

public class TAB_Tau extends JPanel {
	private JTable tblTau, tblToa;
	private DefaultTableModel modTau, modToa;
	private JComboBox<String> cbKho;
	private String currentTau = null;
	private int quyDinhSoToa = 0;
	private DAO_Tau dao = new DAO_Tau();
	private DAO_ChiTietTau daoCT = new DAO_ChiTietTau();

	public TAB_Tau() {
		setLayout(new BorderLayout(15, 15));
		setBackground(new Color(0xF4F7FB));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel pLeft = new JPanel(new BorderLayout(0, 10));
		pLeft.setBackground(Color.WHITE);
		pLeft.setPreferredSize(new Dimension(380, 0));
		pLeft.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(10, 10, 10, 10)));

		JPanel pLT = new JPanel(new BorderLayout());
		pLT.setOpaque(false);
		JLabel lblL = new JLabel(" 1. TÀU ĐẦU KÉO");
		lblL.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblL.setForeground(new Color(0x1A5EAB));
		pLT.add(lblL, BorderLayout.WEST);

		JPanel pnlActionLeft = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		pnlActionLeft.setOpaque(false);
		JButton bAddT = makeBtn("+ Thêm", new Color(39, 174, 96));
		bAddT.setPreferredSize(new Dimension(80, 32));
		JButton bDelT = makeBtn("- Xóa", new Color(192, 57, 43));
		bDelT.setPreferredSize(new Dimension(70, 32));
		pnlActionLeft.add(bDelT);
		pnlActionLeft.add(bAddT);
		pLT.add(pnlActionLeft, BorderLayout.EAST);
		pLeft.add(pLT, BorderLayout.NORTH);

		bAddT.addActionListener(e -> {
			Form_Tau f = new Form_Tau((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Tàu Mới");
			f.setVisible(true);
			if (f.isConfirmed()) {
				dao.insertTau(f.getEntity());
				refreshData();
			}
		});

		bDelT.addActionListener(e -> {
			int r = tblTau.getSelectedRow();
			if (r < 0)
				return;
			if (JOptionPane.showConfirmDialog(this, "Xóa tàu này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == 0) {
				try (Connection c = ConnectDB.getConnection();
						PreparedStatement p = c.prepareStatement("DELETE FROM Tau WHERE maTau=?")) {
					p.setString(1, modTau.getValueAt(r, 0).toString());
					p.executeUpdate();
					refreshData();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, "Không thể xóa tàu đã có hóa đơn!");
				}
			}
		});

		modTau = new DefaultTableModel(new String[] { "Mã Tàu", "Tên", "Quy Định", "Sửa" }, 0);
		tblTau = buildTable(modTau);
		tblTau.getColumnModel().getColumn(3).setCellRenderer(new BtnRnd());
		tblTau.getColumnModel().getColumn(3).setCellEditor(new BtnEd(new JCheckBox()));
		pLeft.add(new JScrollPane(tblTau));

		tblTau.getSelectionModel().addListSelectionListener(e -> {
			if (tblTau.getSelectedRow() >= 0) {
				currentTau = modTau.getValueAt(tblTau.getSelectedRow(), 0).toString();
				quyDinhSoToa = Integer.parseInt(modTau.getValueAt(tblTau.getSelectedRow(), 2).toString().split(" ")[0]);
				loadToa();
			}
		});

		JPanel pRight = new JPanel(new BorderLayout(0, 10));
		pRight.setBackground(Color.WHITE);
		pRight.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(10, 10, 10, 10)));

		JPanel pRT = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		pRT.setOpaque(false);
		JLabel lblR = new JLabel("2. LẮP RÁP TOA: ");
		lblR.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblR.setForeground(new Color(0x1A5EAB));
		pRT.add(lblR);

		cbKho = new JComboBox<>();
		cbKho.setPreferredSize(new Dimension(160, 36));
		cbKho.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		pRT.add(cbKho);

		JButton bIn = makeBtn("<< Gắn", new Color(39, 174, 96));
		bIn.setPreferredSize(new Dimension(80, 36));
		JButton bOut = makeBtn("Gỡ >>", new Color(243, 156, 18));
		bOut.setPreferredSize(new Dimension(70, 36));
		JButton bAuto = makeBtn("⚡ Auto Sinh Toa", new Color(142, 68, 173));
		bAuto.setPreferredSize(new Dimension(140, 36));

		bIn.addActionListener(e -> {
			if (currentTau == null) {
				JOptionPane.showMessageDialog(this, "Chọn tàu bên trái trước!");
				return;
			}
			if (modToa.getRowCount() >= quyDinhSoToa) {
				JOptionPane.showMessageDialog(this, "Tàu này đã gắn đủ " + quyDinhSoToa + " toa theo quy định!");
				return;
			}
			if (cbKho.getSelectedItem() != null) {
				try (Connection c = ConnectDB.getConnection();
						PreparedStatement ps = c.prepareStatement("INSERT INTO ChiTietTau VALUES(?,?,?)")) {
					ps.setString(1, currentTau);
					ps.setString(2, cbKho.getSelectedItem().toString().split(" - ")[0]);
					ps.setInt(3, modToa.getRowCount() + 1);
					ps.executeUpdate();
					loadToa();
					refreshData();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, "Lỗi gắn toa!");
				}
			}
		});

		bOut.addActionListener(e -> {
			int r = tblToa.getSelectedRow();
			if (r >= 0) {
				try (Connection c = ConnectDB.getConnection();
						PreparedStatement ps = c.prepareStatement("DELETE FROM ChiTietTau WHERE maTau=? AND maToa=?")) {
					ps.setString(1, currentTau);
					ps.setString(2, modToa.getValueAt(r, 1).toString());
					ps.executeUpdate();
					PreparedStatement p2 = c
							.prepareStatement("UPDATE ChiTietTau SET thuTu=? WHERE maTau=? AND maToa=?");
					for (int i = 0; i < tblToa.getRowCount(); i++) {
						if (i == r)
							continue;
						p2.setInt(1, i < r ? i + 1 : i);
						p2.setString(2, currentTau);
						p2.setString(3, modToa.getValueAt(i, 1).toString());
						p2.executeUpdate();
					}
					loadToa();
					refreshData();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, "Lỗi!");
				}
			} else {
				JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 toa bên dưới để gỡ!");
			}
		});

		bAuto.addActionListener(e -> {
			if (currentTau == null) {
				JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 tàu bên trái trước!");
				return;
			}
			if (modToa.getRowCount() > 0) {
				JOptionPane.showMessageDialog(this, "Chỉ được tạo tự động khi Tàu chưa gắn toa nào!");
				return;
			}
			if (JOptionPane.showConfirmDialog(this,
					"Hệ thống sẽ tự động sản xuất " + quyDinhSoToa + " toa xe mới và gắn lên tàu này. Tiếp tục?",
					"Auto Sinh Toa", JOptionPane.YES_NO_OPTION) == 0) {
				autoGenerateToa();
			}
		});

		pRT.add(bIn);
		pRT.add(bOut);
		pRT.add(bAuto);
		pRight.add(pRT, BorderLayout.NORTH);

		modToa = new DefaultTableModel(new String[] { "Vị Trí Nối", "Mã Toa", "Tên Toa", "Cấu Hình Loại" }, 0);
		tblToa = buildTable(modToa);
		pRight.add(new JScrollPane(tblToa));

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

	private void handleEdit(int r) {
		Tau t = dao.getTauByMa(modTau.getValueAt(r, 0).toString());
		Form_Tau f = new Form_Tau((Frame) SwingUtilities.getWindowAncestor(this), "Sửa Tàu");
		f.setEntity(t);
		f.setVisible(true);
		if (f.isConfirmed()) {
			dao.updateTau(f.getEntity());
			refreshData();
		}
	}

	private void autoGenerateToa() {
		try (Connection c = ConnectDB.getConnection()) {
			c.setAutoCommit(false);
			List<LoaiToa> dsLoai = new DAO_LoaiToa().getAllLoaiToa();
			LoaiToa ltCung = dsLoai.stream().filter(l -> l.getTenLoaiToa().toLowerCase().contains("cứng")).findFirst()
					.orElse(dsLoai.get(0));
			LoaiToa ltMem = dsLoai.stream().filter(l -> l.getTenLoaiToa().toLowerCase().contains("mềm")).findFirst()
					.orElse(dsLoai.get(0));
			LoaiToa ltNam = dsLoai.stream().filter(l -> l.getTenLoaiToa().toLowerCase().contains("nằm")).findFirst()
					.orElse(dsLoai.get(0));

			int part1 = quyDinhSoToa / 3;
			int part2 = quyDinhSoToa / 3;

			PreparedStatement psToa = c.prepareStatement(
					"INSERT INTO Toa (maToa, tenToa, soGhe, maLoaiToa, trangThai) VALUES (?,?,?,?,'SAN_SANG')");
			PreparedStatement psGan = c.prepareStatement("INSERT INTO ChiTietTau (maTau, maToa, thuTu) VALUES (?,?,?)");

			for (int i = 1; i <= quyDinhSoToa; i++) {
				String newMaToa = currentTau + "_T_" + String.format("%02d", i);
				LoaiToa loaiChon = (i <= part1) ? ltCung : (i <= part1 + part2) ? ltMem : ltNam;
				int soGhe = loaiChon.getSoHang() * loaiChon.getSoCot();

				psToa.setString(1, newMaToa);
				psToa.setString(2, "Toa " + i);
				psToa.setInt(3, soGhe);
				psToa.setString(4, loaiChon.getMaLoaiToa());
				psToa.executeUpdate();
				psGan.setString(1, currentTau);
				psGan.setString(2, newMaToa);
				psGan.setInt(3, i);
				psGan.executeUpdate();
			}
			c.commit();
			c.setAutoCommit(true);
			JOptionPane.showMessageDialog(this, "Auto sinh toa thành công!");
			loadToa();
			refreshData();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadToa() {
		modToa.setRowCount(0);
		if (currentTau == null)
			return;
		try (Connection c = ConnectDB.getConnection();
				java.sql.ResultSet rs = c.createStatement().executeQuery(
						"SELECT c.thuTu, t.maToa, t.tenToa, l.tenLoaiToa FROM ChiTietTau c JOIN Toa t ON c.maToa=t.maToa JOIN LoaiToa l ON t.maLoaiToa=l.maLoaiToa WHERE c.maTau='"
								+ currentTau + "' ORDER BY c.thuTu")) {
			while (rs.next())
				modToa.addRow(new Object[] { rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4) });
		} catch (Exception ex) {
		}
	}

	private void refreshData() {
		modTau.setRowCount(0);
		cbKho.removeAllItems();
		for (Tau t : dao.getAllTau())
			modTau.addRow(new Object[] { t.getMaTau(), t.getTenTau(), t.getSoToa() + " toa", "" });
		try (Connection c = ConnectDB.getConnection();
				java.sql.ResultSet rs = c.createStatement().executeQuery(
						"SELECT t.maToa, l.tenLoaiToa FROM Toa t JOIN LoaiToa l ON t.maLoaiToa=l.maLoaiToa WHERE t.maToa NOT IN (SELECT maToa FROM ChiTietTau)")) {
			while (rs.next())
				cbKho.addItem(rs.getString(1) + " - " + rs.getString(2));
		} catch (Exception ex) {
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
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JTable buildTable(DefaultTableModel m) {
		JTable t = new JTable(m) {
			public boolean isCellEditable(int r, int c) {
				return c == 3;
			}
		};
		t.setRowHeight(35);
		t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		t.getTableHeader().setPreferredSize(new Dimension(0, 40));
		t.getTableHeader().setBackground(new Color(0x1A5EAB));
		t.getTableHeader().setForeground(Color.WHITE);
		t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
		return t;
	}

	class BtnRnd extends JPanel implements TableCellRenderer {
		public BtnRnd() {
			setLayout(new BorderLayout());
			JButton b = new JButton("✎ Sửa");
			b.setFont(new Font("Segoe UI", Font.BOLD, 12));
			b.setForeground(new Color(243, 156, 18));
			b.setContentAreaFilled(false);
			b.setBorderPainted(false);
			add(b);
		}

		public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
			setBackground(s ? t.getSelectionBackground() : Color.WHITE);
			return this;
		}
	}

	class BtnEd extends DefaultCellEditor {
		private JPanel p = new JPanel(new BorderLayout());
		private int r;

		public BtnEd(JCheckBox cb) {
			super(cb);
			JButton b = new JButton("✎ Sửa");
			b.setFont(new Font("Segoe UI", Font.BOLD, 12));
			b.setForeground(new Color(243, 156, 18));
			b.setContentAreaFilled(false);
			b.setBorderPainted(false);
			b.setCursor(new Cursor(Cursor.HAND_CURSOR));
			b.addActionListener(e -> {
				fireEditingStopped();
				handleEdit(r);
			});
			p.add(b);
		}

		public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
			this.r = r;
			p.setBackground(t.getSelectionBackground());
			return p;
		}
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