package com.gui;

import com.dao.Dao_HoaDon;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

public class TAB_ThanhToanLapHD extends JPanel {

	// ─── DAO ───
	private final Dao_HoaDon daoHD = new Dao_HoaDon();

	// ─── Model / Table ───
	private DefaultTableModel modelHD;
	private JTable tableHD;

	// ─── Bộ lọc ───
	private JTextField txtTimKiemTenKH;
	private DatePickerField dateTuNgay, dateToiNgay;
	private JComboBox<Object> cbNhanVienLoc;

	// ─── Stat ───
	private final JLabel lblTongHD = new JLabel("0");

	// ─── Format ───
	private final DecimalFormat df = new DecimalFormat("#,### VNĐ");
	private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	private static final String DATE_FMT = "dd/MM/yyyy";

	// ═══ Màu sắc & Font ═══
	private static final Color BG_PAGE = new Color(0xF4F7FB);
	private static final Color BG_CARD = Color.WHITE;
	private static final Color ACCENT = new Color(0x1A5EAB);
	private static final Color ACCENT_HVR = new Color(0x2270CC);
	private static final Color TEXT_DARK = new Color(0x1E2B3C);
	private static final Color TEXT_MID = new Color(0x5A6A7D);
	private static final Color BORDER = new Color(0xE2EAF4);
	private static final Color ROW_ALT = new Color(0xF7FAFF);
	private static final Color ROW_SEL = new Color(0xDDEEFF);
	private static final Color BTN_GREEN = new Color(0x16A34A);
	private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
	private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
	private static final Font F_CELL = new Font("Segoe UI", Font.PLAIN, 13);
	private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

	private enum BtnStyle {
		PRIMARY, SUCCESS, SECONDARY
	}

	// ═══ Constructor ═══
	public TAB_ThanhToanLapHD() {
		setLayout(new BorderLayout(0, 16));
		setBackground(BG_PAGE);
		setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
		initTable();
		initUI();
		loadNhanVienToCombo();
		loadDanhSachHoaDon();
	}

	// ─── Khởi tạo bảng ───
	private void initTable() {
		modelHD = new DefaultTableModel(new String[] { "Mã HD", "Ngày lập", "Khách hàng", "Nhân viên", "Tổng tiền" },
				0) {
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		tableHD = new JTable(modelHD) {
			public Component prepareRenderer(TableCellRenderer r, int row, int col) {
				Component c = super.prepareRenderer(r, row, col);
				if (!isRowSelected(row))
					c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
				return c;
			}
		};
		tableHD.setRowHeight(38);
		tableHD.setFont(F_CELL);
		tableHD.setGridColor(BORDER);
		tableHD.setSelectionBackground(ROW_SEL);
		tableHD.setSelectionForeground(TEXT_DARK);
		tableHD.setShowVerticalLines(false);
		tableHD.setFillsViewportHeight(true);
		tableHD.getTableHeader().setDefaultRenderer(new HeaderRenderer());
		tableHD.getTableHeader().setPreferredSize(new Dimension(0, 42));
		tableHD.getTableHeader().setReorderingAllowed(false);

		// Căn phải cột Tổng tiền
		DefaultTableCellRenderer ra = new DefaultTableCellRenderer();
		ra.setHorizontalAlignment(JLabel.RIGHT);
		tableHD.getColumnModel().getColumn(4).setCellRenderer(ra);

		// Double-click → dialog chi tiết
		tableHD.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = tableHD.getSelectedRow();
					if (row >= 0)
						openChiTietDialog(modelHD.getValueAt(row, 0).toString());
				}
			}
		});
	}

	// ─── Build UI ───
	private void initUI() {
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.setOpaque(false);
		top.add(buildStatsBar());
		top.add(Box.createVerticalStrut(6));
		top.add(buildHeader());
		top.add(Box.createVerticalStrut(10));
		top.add(buildFilterCard());
		add(top, BorderLayout.NORTH);
		add(buildTableCard(), BorderLayout.CENTER);
	}

	private JPanel buildStatsBar() {
		JPanel bar = new JPanel(new GridLayout(1, 1, 12, 0));
		bar.setOpaque(false);
		bar.add(createStatCard("TỔNG HÓA ĐƠN", lblTongHD, ACCENT));
		return bar;
	}

	private JPanel buildHeader() {
		JPanel p = new JPanel(new BorderLayout());
		p.setOpaque(false);
		JLabel l = new JLabel("QUẢN LÝ & TRA CỨU HÓA ĐƠN");
		l.setFont(F_TITLE);
		l.setForeground(ACCENT);
		p.add(l, BorderLayout.WEST);
		return p;
	}

	// ─── Filter card ───
	private JPanel buildFilterCard() {
		JPanel card = new JPanel(new GridBagLayout());
		card.setBackground(BG_CARD);
		card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true),
				BorderFactory.createEmptyBorder(10, 14, 10, 14)));

		GridBagConstraints g = new GridBagConstraints();
		g.insets = new Insets(0, 6, 0, 6);
		g.fill = GridBagConstraints.HORIZONTAL;
		g.gridy = 0;

		txtTimKiemTenKH = makeTextField(160);
		txtTimKiemTenKH.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				loadDanhSachHoaDon();
			}
		});

		dateTuNgay = new DatePickerField(null);
		dateToiNgay = new DatePickerField(null);
		dateTuNgay.setPreferredSize(new Dimension(155, 34));
		dateToiNgay.setPreferredSize(new Dimension(155, 34));
		dateTuNgay.addPropertyChangeListener("date", ev -> loadDanhSachHoaDon());
		dateToiNgay.addPropertyChangeListener("date", ev -> loadDanhSachHoaDon());

		cbNhanVienLoc = new JComboBox<>();
		cbNhanVienLoc.setPreferredSize(new Dimension(160, 34));
		cbNhanVienLoc.setFont(F_CELL);
		cbNhanVienLoc.addActionListener(e -> loadDanhSachHoaDon());

		JButton btnLamMoi = makeBtn("Làm mới", BtnStyle.SECONDARY);
		btnLamMoi.setPreferredSize(new Dimension(90, 34));
		btnLamMoi.addActionListener(e -> xoaBoLoc());

		g.gridx = 0;
		g.weightx = 0;
		card.add(makeLabel("Khách hàng:"), g);
		g.gridx = 1;
		g.weightx = 1;
		card.add(txtTimKiemTenKH, g);
		g.gridx = 2;
		g.weightx = 0;
		card.add(makeLabel("Từ:"), g);
		g.gridx = 3;
		g.weightx = 0;
		card.add(dateTuNgay, g);
		g.gridx = 4;
		g.weightx = 0;
		card.add(makeLabel("Tới:"), g);
		g.gridx = 5;
		g.weightx = 0;
		card.add(dateToiNgay, g);
		g.gridx = 6;
		g.weightx = 0;
		card.add(makeLabel("NV:"), g);
		g.gridx = 7;
		g.weightx = 0.8;
		card.add(cbNhanVienLoc, g);
		g.gridx = 8;
		g.weightx = 0;
		card.add(btnLamMoi, g);
		return card;
	}

	// ─── Table card (toàn bộ chiều rộng) ───
	private JPanel buildTableCard() {
		JPanel card = buildCard(new BorderLayout());

		JPanel titleBar = new JPanel(new BorderLayout());
		titleBar.setOpaque(false);
		titleBar.setBorder(BorderFactory.createEmptyBorder(12, 18, 8, 18));
		JLabel lblTitle = new JLabel("Danh sách hóa đơn");
		lblTitle.setFont(F_LABEL);
		JLabel lblHint = new JLabel("  ← Double-click để xem chi tiết & in hóa đơn");
		lblHint.setFont(F_SMALL);
		lblHint.setForeground(TEXT_MID);
		titleBar.add(lblTitle, BorderLayout.WEST);
		titleBar.add(lblHint, BorderLayout.CENTER);

		JScrollPane scroll = new JScrollPane(tableHD);
		scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
		scroll.getViewport().setBackground(BG_CARD);
		styleScrollBar(scroll.getVerticalScrollBar());
		styleScrollBar(scroll.getHorizontalScrollBar());

		JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10));
		bottomBar.setBackground(new Color(0xF8FAFC));
		bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
		JButton btnIn = makeBtn("📊 In hóa đơn Excel", BtnStyle.PRIMARY);
		btnIn.setPreferredSize(new Dimension(185, 36));
		btnIn.addActionListener(e -> chonThangInDanhSach());
		bottomBar.add(btnIn);

		card.add(titleBar, BorderLayout.NORTH);
		card.add(scroll, BorderLayout.CENTER);
		card.add(bottomBar, BorderLayout.SOUTH);
		return card;
	}

	// ═══ DIALOG CHI TIẾT HÓA ĐƠN ═══
	private void openChiTietDialog(String maHD) {
		JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết hóa đơn  —  " + maHD, true);
		dlg.setLayout(new BorderLayout());
		dlg.setResizable(true);

		// Header xanh
		JPanel pHdr = new JPanel(new GridLayout(2, 1, 0, 2));
		pHdr.setBackground(ACCENT);
		pHdr.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
		JLabel lT = new JLabel("CHI TIẾT HÓA ĐƠN");
		lT.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lT.setForeground(Color.WHITE);
		JLabel lS = new JLabel("Hệ Thống Bán Vé Tàu Hỏa Việt Nam");
		lS.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		lS.setForeground(new Color(0xA8C8EE));
		pHdr.add(lT);
		pHdr.add(lS);
		dlg.add(pHdr, BorderLayout.NORTH);

		// Labels thông tin
		JLabel valMaHD = dlgVal("-");
		valMaHD.setFont(F_LABEL);
		JLabel valNgayLap = dlgVal("-");
		JLabel valNhanVien = dlgVal("-");
		JLabel valKhachHang = dlgVal("-");
		JLabel valTongGiam = dlgVal("-");
		JLabel valTongTien = dlgVal("0 VNĐ");
		valTongTien.setFont(new Font("Segoe UI", Font.BOLD, 18));
		valTongTien.setForeground(new Color(0xDC2626));

		final double[] tongGiam = { 0 };
		final String[] tenKHRef = { "" };
		final String[] ngayRef = { "" };
		final String[] tenNVRef = { "" };
		final String[] tongTRef = { "0 VNĐ" };

		try {
			ResultSet ri = daoHD.getThongTinHoaDon(maHD);
			if (ri != null && ri.next()) {
				valMaHD.setText(ri.getString("maHD"));
				ngayRef[0] = sdf.format(ri.getTimestamp("ngayLap"));
				tenNVRef[0] = orDef(ri.getString("tenNV"), "-");
				tenKHRef[0] = ri.getString("tenKH") != null ? ri.getString("tenKH") : "Khách lẻ";
				tongTRef[0] = df.format(ri.getDouble("tongTien"));
				valNgayLap.setText(ngayRef[0]);
				valNhanVien.setText(tenNVRef[0]);
				valKhachHang.setText(tenKHRef[0]);
				valTongTien.setText(tongTRef[0]);
			}
		} catch (Exception ignored) {
		}

		// Info panel (2 cột)
		JPanel pInfo = new JPanel(new GridBagLayout());
		pInfo.setBackground(BG_CARD);
		pInfo.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));
		GridBagConstraints gi = new GridBagConstraints();
		gi.fill = GridBagConstraints.HORIZONTAL;
		gi.insets = new Insets(3, 4, 3, 10);
		addInfoRow2Col(pInfo, "Mã hóa đơn:", valMaHD, "Ngày lập:", valNgayLap, 0, gi);
		addInfoRow2Col(pInfo, "Nhân viên:", valNhanVien, "Khách hàng:", valKhachHang, 1, gi);

		// Bảng chi tiết
		DefaultTableModel mCT = new DefaultTableModel(
				new String[] { "Mã Vé", "Loại vé", "Giá gốc", "Giảm", "Thành tiền" }, 0) {
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		JTable tCT = buildStyledTable(mCT);
		DefaultTableCellRenderer ra = new DefaultTableCellRenderer();
		ra.setHorizontalAlignment(JLabel.RIGHT);
		for (int ci = 2; ci <= 4; ci++)
			tCT.getColumnModel().getColumn(ci).setCellRenderer(ra);

		try {
			ResultSet rd = daoHD.getChiTietHoaDon(maHD);
			while (rd != null && rd.next()) {
				double tg = rd.getDouble("tienGiam");
				tongGiam[0] += tg;
				mCT.addRow(new Object[] { rd.getString("maVe"), orDef(rd.getString("maLoaiVe"), "-"),
						df.format(rd.getDouble("tienGoc")), df.format(tg), df.format(rd.getDouble("thanhTien")) });
			}
		} catch (Exception ignored) {
		}
		valTongGiam.setText(df.format(tongGiam[0]));

		JScrollPane sCT = new JScrollPane(tCT);
		sCT.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER));
		sCT.getViewport().setBackground(BG_CARD);
		styleScrollBar(sCT.getVerticalScrollBar());

		// Total panel
		JPanel pTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
		pTotal.setBackground(new Color(0xF8FAFC));
		pTotal.add(dlgLbl("Tổng giảm:"));
		pTotal.add(valTongGiam);
		pTotal.add(Box.createHorizontalStrut(16));
		pTotal.add(dlgLbl("TỔNG THÀNH TIỀN:"));
		pTotal.add(valTongTien);

		JPanel pCenter = new JPanel(new BorderLayout());
		pCenter.setBackground(BG_CARD);
		pCenter.add(pInfo, BorderLayout.NORTH);
		pCenter.add(sCT, BorderLayout.CENTER);
		pCenter.add(pTotal, BorderLayout.SOUTH);
		dlg.add(pCenter, BorderLayout.CENTER);

		// Buttons
		JPanel pAct = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
		pAct.setBackground(new Color(0xF8FAFC));
		pAct.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

		final DefaultTableModel mCTFinal = mCT;
		JButton btnIn = makeBtn("🖨 In hóa đơn", BtnStyle.SUCCESS);
		JButton btnDong = makeBtn("Đóng", BtnStyle.SECONDARY);
		btnIn.setPreferredSize(new Dimension(150, 36));
		btnDong.setPreferredSize(new Dimension(90, 36));

		btnIn.addActionListener(e -> {
			dlg.dispose();
			inHoaDon(maHD, tenKHRef[0], ngayRef[0], tenNVRef[0], tongTRef[0], mCTFinal);
		});
		btnDong.addActionListener(e -> dlg.dispose());

		pAct.add(btnIn);
		pAct.add(btnDong);
		dlg.add(pAct, BorderLayout.SOUTH);

		dlg.setSize(720, 520);
		dlg.setMinimumSize(new Dimension(580, 400));
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
	}

	private void addInfoRow2Col(JPanel p, String l1, JLabel v1, String l2, JLabel v2, int row, GridBagConstraints g) {
		g.gridy = row;
		g.gridx = 0;
		g.weightx = 0;
		p.add(dlgLbl(l1), g);
		g.gridx = 1;
		g.weightx = 0.5;
		p.add(v1, g);
		g.gridx = 2;
		g.weightx = 0;
		p.add(dlgLbl(l2), g);
		g.gridx = 3;
		g.weightx = 0.5;
		p.add(v2, g);
	}

	private JLabel dlgLbl(String t) {
		JLabel l = new JLabel(t);
		l.setFont(F_CELL);
		l.setForeground(TEXT_MID);
		return l;
	}

	private JLabel dlgVal(String t) {
		JLabel l = new JLabel(t);
		l.setFont(F_CELL);
		l.setForeground(TEXT_DARK);
		return l;
	}

	private String orDef(String s, String d) {
		return (s == null || s.isEmpty()) ? d : s;
	}

	// ═══ LOAD DATA ═══
	private void loadDanhSachHoaDon() {
		modelHD.setRowCount(0);
		try {
			String tenKH = txtTimKiemTenKH != null ? txtTimKiemTenKH.getText().trim() : "";
			String maNV = null;
			if (cbNhanVienLoc != null && cbNhanVienLoc.getSelectedIndex() > 0) {
				String item = cbNhanVienLoc.getSelectedItem().toString();
				if (item.contains(" - "))
					maNV = item.split(" - ")[0];
			}

			Date dTu = dateTuNgay != null ? dateTuNgay.getSelectedDate() : null;
			Date dToi = dateToiNgay != null ? dateToiNgay.getSelectedDate() : null;

			if (dTu != null && dToi != null && dTu.after(dToi)) {
				JOptionPane.showMessageDialog(this, "Ngày 'Từ' không được lớn hơn ngày 'Tới'!", "Lỗi chọn ngày",
						JOptionPane.WARNING_MESSAGE);
				dateToiNgay.resetDate();
				return;
			}

			Timestamp ts1 = null, ts2 = null;
			if (dTu != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(dTu);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				ts1 = new Timestamp(c.getTimeInMillis());
			}
			if (dToi != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(dToi);
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);
				c.set(Calendar.SECOND, 59);
				ts2 = new Timestamp(c.getTimeInMillis());
			} else if (dTu != null) {
				ts2 = new Timestamp(System.currentTimeMillis());
			}

			ResultSet rs = daoHD.getDanhSachHoaDon(tenKH, maNV, ts1, ts2);
			int count = 0;
			while (rs != null && rs.next()) {
				modelHD.addRow(new Object[] { rs.getString("maHD"), sdf.format(rs.getTimestamp("ngayLap")),
						orDef(rs.getString("tenKH"), "Khách lẻ"), orDef(rs.getString("tenNV"), "-"),
						df.format(rs.getDouble("tongTien")) });
				count++;
			}
			lblTongHD.setText(String.valueOf(count));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void loadNhanVienToCombo() {
		if (cbNhanVienLoc == null)
			return;
		cbNhanVienLoc.removeAllItems();
		cbNhanVienLoc.addItem("--- Tất cả ---");
		try {
			ResultSet rs = daoHD.getAllNhanVien();
			while (rs != null && rs.next())
				cbNhanVienLoc.addItem(rs.getString(1) + " - " + rs.getString(2));
		} catch (SQLException ignored) {
		}
	}

	private void xoaBoLoc() {
		if (txtTimKiemTenKH != null)
			txtTimKiemTenKH.setText("");
		if (dateTuNgay != null)
			dateTuNgay.resetDate();
		if (dateToiNgay != null)
			dateToiNgay.resetDate();
		if (cbNhanVienLoc != null && cbNhanVienLoc.getItemCount() > 0)
			cbNhanVienLoc.setSelectedIndex(0);
		loadDanhSachHoaDon();
	}

	// ═══ IN DANH SÁCH (chọn tháng) ═══
	private void chonThangInDanhSach() {
		JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn tháng xuất Excel", true);
		dlg.setLayout(new BorderLayout());
		dlg.setResizable(false);

		String[] months = { "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
				"Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12" };
		JComboBox<String> cbMonth = new JComboBox<>(months);
		cbMonth.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH));
		cbMonth.setFont(F_CELL);

		JSpinner spYear = new JSpinner(
				new SpinnerNumberModel(Calendar.getInstance().get(Calendar.YEAR), 2000, 2100, 1));
		spYear.setFont(F_CELL);
		((JSpinner.DefaultEditor) spYear.getEditor()).getTextField().setColumns(5);

		JPanel form = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 16));
		form.setBackground(BG_CARD);
		form.add(makeLabel("Tháng:"));
		form.add(cbMonth);
		form.add(makeLabel("Năm:"));
		form.add(spYear);

		JButton btnOk = makeBtn("📊 Xuất Excel", BtnStyle.SUCCESS);
		JButton btnHuy = makeBtn("Hủy", BtnStyle.SECONDARY);
		btnOk.setPreferredSize(new Dimension(100, 34));
		btnHuy.setPreferredSize(new Dimension(80, 34));

		JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		pBtn.setBackground(new Color(0xF8FAFC));
		pBtn.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
		pBtn.add(btnHuy);
		pBtn.add(btnOk);

		btnHuy.addActionListener(e -> dlg.dispose());
		btnOk.addActionListener(e -> {
			int m = cbMonth.getSelectedIndex() + 1;
			int y = (Integer) spYear.getValue();
			dlg.dispose();
			inDanhSachTheoThang(m, y);
		});

		dlg.add(form, BorderLayout.CENTER);
		dlg.add(pBtn, BorderLayout.SOUTH);
		dlg.pack();
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
	}

	private void inDanhSachTheoThang(int month, int year) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Lưu danh sách hóa đơn Excel");
		fc.setSelectedFile(new File("DanhSach_HoaDon_" + String.format("%02d", month) + "_" + year + ".xlsx"));
		javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
				"Excel Files (*.xlsx)", "xlsx");
		fc.setFileFilter(filter);
		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return;

		try {
			java.util.List<Object[]> rows = new ArrayList<>();
			double tongTatCa = 0;
			try {
				String sql = "SELECT h.maHD, h.ngayLap, k.tenKH, n.tenNV, h.tongTien " + "FROM HoaDon h "
						+ "LEFT JOIN KhachHang k ON h.maKH = k.maKH " + "LEFT JOIN NhanVien  n ON h.maNV = n.maNV "
						+ "WHERE MONTH(h.ngayLap)=? AND YEAR(h.ngayLap)=? ORDER BY h.ngayLap";
				ResultSet rs = daoHD.queryRaw(sql, month, year);
				while (rs != null && rs.next()) {
					double tt = rs.getDouble("tongTien");
					tongTatCa += tt;
					rows.add(new Object[] { rs.getString("maHD"), sdf.format(rs.getTimestamp("ngayLap")),
							orDef(rs.getString("tenKH"), "Khách lẻ"), orDef(rs.getString("tenNV"), "-"), tt // giữ
																											// double để
																											// Excel
																											// format số
					});
				}
			} catch (Exception ignored) {
				for (int i = 0; i < modelHD.getRowCount(); i++)
					rows.add(new Object[] { modelHD.getValueAt(i, 0), modelHD.getValueAt(i, 1),
							modelHD.getValueAt(i, 2), modelHD.getValueAt(i, 3), modelHD.getValueAt(i, 4) });
			}

			String path = fc.getSelectedFile().getAbsolutePath();
			if (!path.toLowerCase().endsWith(".xlsx"))
				path += ".xlsx";

			// ── Tạo workbook Excel ──
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sh = wb.createSheet("Hóa Đơn " + month + "-" + year);

			// Màu header xanh
			byte[] accentRgb = { 0x1A, 0x5E, (byte) 0xAB };
			XSSFColor accentColor = new XSSFColor(accentRgb, null);

			// ─ Style tiêu đề ─
			CellStyle styleTitle = wb.createCellStyle();
			org.apache.poi.ss.usermodel.Font fTitle = wb.createFont();
			fTitle = wb.createFont();
			fTitle.setBold(true);
			fTitle.setFontHeightInPoints((short) 14);
			fTitle.setColor(IndexedColors.WHITE.getIndex());
			styleTitle.setFont(fTitle);
			styleTitle.setFillForegroundColor(accentColor);
			styleTitle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			styleTitle.setAlignment(HorizontalAlignment.CENTER);
			styleTitle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);

			// ─ Style sub-title ─
			CellStyle styleSub = wb.createCellStyle();
			org.apache.poi.ss.usermodel.Font fSub = wb.createFont();
			fSub = wb.createFont();
			fSub.setItalic(true);
			fSub.setFontHeightInPoints((short) 10);
			fSub.setColor(IndexedColors.WHITE.getIndex());
			styleSub.setFont(fSub);
			styleSub.setFillForegroundColor(accentColor);
			styleSub.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			styleSub.setAlignment(HorizontalAlignment.CENTER);

			// ─ Style header cột ─
			CellStyle styleHeader = wb.createCellStyle();
			org.apache.poi.ss.usermodel.Font fHeader = wb.createFont();
			fHeader = wb.createFont();
			fHeader.setBold(true);
			fHeader.setFontHeightInPoints((short) 11);
			fHeader.setColor(IndexedColors.WHITE.getIndex());
			styleHeader.setFont(fHeader);
			styleHeader.setFillForegroundColor(accentColor);
			styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			styleHeader.setAlignment(HorizontalAlignment.CENTER);
			styleHeader.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
			styleHeader.setBorderBottom(BorderStyle.THIN);
			styleHeader.setBottomBorderColor(IndexedColors.WHITE.getIndex());

			// ─ Style data thường ─
			CellStyle styleData = wb.createCellStyle();
			org.apache.poi.ss.usermodel.Font fData = wb.createFont();
			fData = wb.createFont();
			fData.setFontHeightInPoints((short) 11);
			styleData.setFont(fData);
			styleData.setBorderBottom(BorderStyle.THIN);
			((XSSFCellStyle) styleData)
					.setBottomBorderColor(new XSSFColor(new byte[] { (byte) 0xE2, (byte) 0xEA, (byte) 0xF4 }, null));

			// ─ Style data xen kẽ ─
			CellStyle styleDataAlt = wb.createCellStyle();
			styleDataAlt.cloneStyleFrom(styleData);
			styleDataAlt
					.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 0xF7, (byte) 0xFA, (byte) 0xFF }, null));
			styleDataAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			// ─ Style số tiền ─
			CellStyle styleMoney = wb.createCellStyle();
			styleMoney.cloneStyleFrom(styleData);
			styleMoney.setAlignment(HorizontalAlignment.RIGHT);
			DataFormat fmt = wb.createDataFormat();
			styleMoney.setDataFormat(fmt.getFormat("#,##0"));

			CellStyle styleMoneyAlt = wb.createCellStyle();
			styleMoneyAlt.cloneStyleFrom(styleDataAlt);
			styleMoneyAlt.setAlignment(HorizontalAlignment.RIGHT);
			styleMoneyAlt.setDataFormat(fmt.getFormat("#,##0"));

			// ─ Style tổng ─
			CellStyle styleSum = wb.createCellStyle();
			org.apache.poi.ss.usermodel.Font fSum = wb.createFont();
			fSum = wb.createFont();
			fSum.setBold(true);
			fSum.setFontHeightInPoints((short) 11);
			styleSum.setFont(fSum);
			styleSum.setAlignment(HorizontalAlignment.RIGHT);
			styleSum.setDataFormat(fmt.getFormat("#,##0"));
			styleSum.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 0xEB, (byte) 0xF3, (byte) 0xFF }, null));
			styleSum.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			styleSum.setBorderTop(BorderStyle.MEDIUM);

			CellStyle styleSumLabel = wb.createCellStyle();
			org.apache.poi.ss.usermodel.Font fSumLbl = wb.createFont();
			fSumLbl = wb.createFont();
			fSumLbl.setBold(true);
			fSumLbl.setFontHeightInPoints((short) 11);
			styleSumLabel.setFont(fSumLbl);
			styleSumLabel.setAlignment(HorizontalAlignment.RIGHT);
			styleSumLabel
					.setFillForegroundColor(new XSSFColor(new byte[] { (byte) 0xEB, (byte) 0xF3, (byte) 0xFF }, null));
			styleSumLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			styleSumLabel.setBorderTop(BorderStyle.MEDIUM);

			// ─ Style ngày in ─
			CellStyle styleFooter = wb.createCellStyle();
			org.apache.poi.ss.usermodel.Font fFooter = wb.createFont();
			fFooter = wb.createFont();
			fFooter.setItalic(true);
			fFooter.setFontHeightInPoints((short) 9);
			fFooter.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
			styleFooter.setFont(fFooter);
			styleFooter.setAlignment(HorizontalAlignment.RIGHT);

			int colCount = 6;

			// ── Row 0: Tiêu đề chính (merge A1:F1) ──
			Row rTitle = sh.createRow(0);
			rTitle.setHeightInPoints(28);
			Cell cTitle = rTitle.createCell(0);
			cTitle.setCellValue("DANH SÁCH HÓA ĐƠN THÁNG " + month + "/" + year);
			cTitle.setCellStyle(styleTitle);
			for (int c = 1; c < colCount; c++)
				rTitle.createCell(c).setCellStyle(styleTitle);
			sh.addMergedRegion(new CellRangeAddress(0, 0, 0, colCount - 1));

			// ── Row 1: Sub-title ──
			Row rSub = sh.createRow(1);
			rSub.setHeightInPoints(18);
			Cell cSub = rSub.createCell(0);
			cSub.setCellValue("Hệ Thống Bán Vé Tàu Hỏa Việt Nam");
			cSub.setCellStyle(styleSub);
			for (int c = 1; c < colCount; c++)
				rSub.createCell(c).setCellStyle(styleSub);
			sh.addMergedRegion(new CellRangeAddress(1, 1, 0, colCount - 1));

			// ── Row 2: Header cột ──
			Row rHeader = sh.createRow(2);
			rHeader.setHeightInPoints(24);
			String[] headers = { "STT", "Mã HD", "Khách hàng", "Nhân viên", "Ngày lập", "Thành tiền" };
			for (int c = 0; c < headers.length; c++) {
				Cell cell = rHeader.createCell(c);
				cell.setCellValue(headers[c]);
				cell.setCellStyle(styleHeader);
			}

			// ── Rows dữ liệu (bắt đầu từ row 3) ──
			int dataStart = 3;
			int stt = 1;
			for (Object[] row : rows) {
				Row r = sh.createRow(dataStart + stt - 1);
				r.setHeightInPoints(20);
				boolean alt = (stt % 2 == 0);
				CellStyle csNorm = alt ? styleDataAlt : styleData;
				CellStyle csMon = alt ? styleMoneyAlt : styleMoney;

				Cell c0 = r.createCell(0);
				c0.setCellValue(stt);
				c0.setCellStyle(csNorm);
				Cell c1 = r.createCell(1);
				c1.setCellValue(row[0].toString());
				c1.setCellStyle(csNorm);
				Cell c2 = r.createCell(2);
				c2.setCellValue(row[2].toString());
				c2.setCellStyle(csNorm);
				Cell c3 = r.createCell(3);
				c3.setCellValue(row[3].toString());
				c3.setCellStyle(csNorm);
				Cell c4 = r.createCell(4);
				c4.setCellValue(row[1].toString());
				c4.setCellStyle(csNorm);
				Cell c5 = r.createCell(5);
				if (row[4] instanceof Double)
					c5.setCellValue((Double) row[4]);
				else {
					try {
						c5.setCellValue(Double.parseDouble(row[4].toString().replaceAll("[^\\d.]", "")));
					} catch (Exception ex) {
						c5.setCellValue(row[4].toString());
					}
				}
				c5.setCellStyle(csMon);
				stt++;
			}

			// ── Row tổng ──
			int sumRow = dataStart + rows.size();
			Row rSum = sh.createRow(sumRow);
			rSum.setHeightInPoints(22);
			// Merge cột 0-4 cho label
			for (int c = 0; c < colCount - 1; c++) {
				Cell cell = rSum.createCell(c);
				cell.setCellStyle(styleSumLabel);
			}
			rSum.getCell(0).setCellValue("Tổng: " + rows.size() + " hóa đơn");
			sh.addMergedRegion(new CellRangeAddress(sumRow, sumRow, 0, colCount - 2));
			Cell cSumVal = rSum.createCell(colCount - 1);
			cSumVal.setCellValue(tongTatCa);
			cSumVal.setCellStyle(styleSum);

			// ── Row ngày in ──
			Row rFooter = sh.createRow(sumRow + 2);
			Cell cFooter = rFooter.createCell(0);
			cFooter.setCellValue("Ngày xuất: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
			cFooter.setCellStyle(styleFooter);
			sh.addMergedRegion(new CellRangeAddress(sumRow + 2, sumRow + 2, 0, colCount - 1));

			// ── Chiều rộng cột (auto tương đối) ──
			sh.setColumnWidth(0, 8 * 256); // STT
			sh.setColumnWidth(1, 16 * 256); // Mã HD
			sh.setColumnWidth(2, 28 * 256); // Khách hàng
			sh.setColumnWidth(3, 24 * 256); // Nhân viên
			sh.setColumnWidth(4, 22 * 256); // Ngày lập
			sh.setColumnWidth(5, 20 * 256); // Thành tiền

			// Freeze header rows
			sh.createFreezePane(0, 3);

			// ── Ghi file ──
			try (FileOutputStream fos = new FileOutputStream(path)) {
				wb.write(fos);
			}
			wb.close();

			int opt = JOptionPane.showConfirmDialog(this, "Xuất Excel thành công!\nMở file?", "Thành công",
					JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
			if (opt == JOptionPane.YES_OPTION)
				Desktop.getDesktop().open(new File(path));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi khi xuất Excel: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// ═══ IN HÓA ĐƠN PDF ═══
	private void inHoaDon(String maHD, String tenKH, String ngayLap, String tenNV, String tongTien,
			DefaultTableModel mCT) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Lưu hóa đơn PDF");
		fc.setSelectedFile(new File("HoaDon_" + maHD + ".pdf"));
		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		try {
			java.util.List<Object[]> rows = new ArrayList<>();
			for (int i = 0; i < mCT.getRowCount(); i++)
				rows.add(new Object[] { mCT.getValueAt(i, 0), mCT.getValueAt(i, 1), mCT.getValueAt(i, 2),
						mCT.getValueAt(i, 3), mCT.getValueAt(i, 4) });

			String path = fc.getSelectedFile().getAbsolutePath();
			if (!path.toLowerCase().endsWith(".pdf"))
				path += ".pdf";

			Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
			PdfWriter.getInstance(doc, new FileOutputStream(path));
			doc.open();

			BaseFont bf;
			try {
				bf = BaseFont.createFont("C:/Windows/Fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			} catch (Exception e) {
				bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			}

			com.itextpdf.text.Font fS = new com.itextpdf.text.Font(bf, 7);
			com.itextpdf.text.Font fN = new com.itextpdf.text.Font(bf, 9);
			com.itextpdf.text.Font fB = new com.itextpdf.text.Font(bf, 9, com.itextpdf.text.Font.BOLD);
			com.itextpdf.text.Font fT = new com.itextpdf.text.Font(bf, 14, com.itextpdf.text.Font.BOLD,
					new BaseColor(0x1A, 0x5E, 0xAB));
			com.itextpdf.text.Font fH = new com.itextpdf.text.Font(bf, 8, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
			com.itextpdf.text.Font fC = new com.itextpdf.text.Font(bf, 8);
			com.itextpdf.text.Font fIL = new com.itextpdf.text.Font(bf, 8, com.itextpdf.text.Font.BOLD);

			Paragraph pTop = new Paragraph("Đơn vị cung cấp: CÔNG TY CỔ PHẦN VẬN TẢI ĐƯỜNG SẮT VIỆT NAM", fS);
			pTop.setAlignment(Element.ALIGN_CENTER);
			doc.add(pTop);
			doc.add(new Paragraph(" ", fS));

			PdfPTable tT = new PdfPTable(new float[] { 2f, 5f, 2f });
			tT.setWidthPercentage(100);
			PdfPCell cL = new PdfPCell();
			cL.setBorder(PdfPCell.NO_BORDER);
			cL.addElement(new Paragraph("ĐƯỜNG SẮT\nVIỆT NAM",
					new com.itextpdf.text.Font(bf, 10, com.itextpdf.text.Font.BOLD, new BaseColor(0x1A, 0x5E, 0xAB))));
			tT.addCell(cL);
			PdfPCell cC = new PdfPCell();
			cC.setBorder(PdfPCell.NO_BORDER);
			Paragraph ph = new Paragraph("HÓA ĐƠN GIÁ TRỊ GIA TĂNG", fT);
			ph.setAlignment(Element.ALIGN_CENTER);
			Paragraph pd = new Paragraph("Ngày " + ngayLap, fN);
			pd.setAlignment(Element.ALIGN_CENTER);
			cC.addElement(ph);
			cC.addElement(pd);
			tT.addCell(cC);
			PdfPCell cR = new PdfPCell();
			cR.setBorder(PdfPCell.NO_BORDER);
			cR.addElement(new Paragraph("Ký hiệu: 1K23THK", fN));
			cR.addElement(new Paragraph("Số: " + maHD, fN));
			tT.addCell(cR);
			doc.add(tT);
			doc.add(new LineSeparator(1f, 100f, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2));
			doc.add(new Paragraph(" ", fS));

			addInfoLine(doc, "Đơn vị bán hàng:", "CÔNG TY CỔ PHẦN VẬN TẢI ĐƯỜNG SẮT VIỆT NAM", fIL, fN);
			addInfoLine(doc, "MST:", "0100106264", fIL, fN);
			addInfoLine(doc, "Nhân viên lập:", tenNV, fIL, fN);
			doc.add(new Paragraph(" ", fS));
			addInfoLine(doc, "Họ tên người mua:", tenKH, fIL, fN);
			addInfoLine(doc, "Hình thức thanh toán:", " Tiền mặt / Chuyển khoản", fIL, fN);
			doc.add(new Paragraph(" ", fS));

			PdfPTable tI = new PdfPTable(new float[] { 0.5f, 1.5f, 3f, 1f, 0.8f, 1.2f, 0.8f, 1f, 1.2f });
			tI.setWidthPercentage(100);
			tI.setSpacingBefore(4);
			BaseColor hBg = new BaseColor(0x1A, 0x5E, 0xAB);
			for (String h : new String[] { "STT", "Mã vé", "Tên DV", "ĐVT", "SL", "Đơn giá", "Thuế suất", "Thuế GTGT",
					"TT thuế" }) {
				PdfPCell hc = new PdfPCell(new Phrase(h, fH));
				hc.setBackgroundColor(hBg);
				hc.setHorizontalAlignment(Element.ALIGN_CENTER);
				hc.setPadding(5);
				tI.addCell(hc);
			}
			double tCT = 0, tTh = 0, tCoTh = 0;
			int stt = 1;
			for (Object[] row : rows) {
				double goc = parseNum(row[2].toString()), coTh = parseNum(row[4].toString()), th = coTh - goc;
				tCT += goc;
				tTh += th;
				tCoTh += coTh;
				addPdfCell(tI, String.valueOf(stt++), fC, Element.ALIGN_CENTER);
				addPdfCell(tI, row[0].toString(), fC, Element.ALIGN_LEFT);
				addPdfCell(tI, "Vé: " + row[1], fC, Element.ALIGN_LEFT);
				addPdfCell(tI, "Vé", fC, Element.ALIGN_CENTER);
				addPdfCell(tI, "1", fC, Element.ALIGN_CENTER);
				addPdfCell(tI, row[2].toString(), fC, Element.ALIGN_RIGHT);
				addPdfCell(tI, "10%", fC, Element.ALIGN_CENTER);
				addPdfCell(tI, df.format(th), fC, Element.ALIGN_RIGHT);
				addPdfCell(tI, row[4].toString(), fC, Element.ALIGN_RIGHT);
			}
			addPdfCell(tI, String.valueOf(stt), fC, Element.ALIGN_CENTER);
			for (int i = 0; i < 2; i++)
				addPdfCell(tI, "", fC, Element.ALIGN_LEFT);
			addPdfCell(tI, "Phí bảo hiểm HK", fC, Element.ALIGN_LEFT);
			addPdfCell(tI, "Người", fC, Element.ALIGN_CENTER);
			addPdfCell(tI, "1", fC, Element.ALIGN_CENTER);
			addPdfCell(tI, "1.000 VNĐ", fC, Element.ALIGN_RIGHT);
			addPdfCell(tI, "KCT", fC, Element.ALIGN_CENTER);
			addPdfCell(tI, "", fC, Element.ALIGN_RIGHT);
			addPdfCell(tI, "1.000 VNĐ", fC, Element.ALIGN_RIGHT);
			doc.add(tI);

			BaseColor sumBg = new BaseColor(0xF0, 0xF6, 0xFF);
			PdfPTable tS = new PdfPTable(new float[] { 6f, 1f, 1f, 1.2f });
			tS.setWidthPercentage(100);
			PdfPCell cTL = new PdfPCell(new Phrase("Tổng cộng:", fB));
			cTL.setBorder(PdfPCell.BOX);
			cTL.setBackgroundColor(sumBg);
			cTL.setPadding(5);
			tS.addCell(cTL);
			addSumCell(tS, df.format(tCT), fB, sumBg);
			addSumCell(tS, df.format(tTh), fB, sumBg);
			addSumCell(tS, tongTien,
					new com.itextpdf.text.Font(bf, 9, com.itextpdf.text.Font.BOLD, new BaseColor(0xDC, 0x26, 0x26)),
					sumBg);
			doc.add(tS);
			doc.add(new Paragraph(" ", fS));
			doc.add(new Paragraph("Số tiền bằng chữ: " + tongTien.replace(" VNĐ", "") + " đồng", fB));
			doc.add(new Paragraph(" ", fS));

			PdfPTable tSign = new PdfPTable(2);
			tSign.setWidthPercentage(100);
			tSign.setSpacingBefore(10);
			PdfPCell cM = new PdfPCell();
			cM.setBorder(PdfPCell.NO_BORDER);
			Paragraph pM = new Paragraph("Người mua hàng\n(Ký, ghi rõ họ tên)", fN);
			pM.setAlignment(Element.ALIGN_CENTER);
			cM.addElement(pM);
			PdfPCell cBn = new PdfPCell();
			cBn.setBorder(PdfPCell.NO_BORDER);
			Paragraph pBn = new Paragraph("Người bán hàng\n(Ký, ghi rõ họ tên)", fN);
			pBn.setAlignment(Element.ALIGN_CENTER);
			cBn.addElement(pBn);
			tSign.addCell(cM);
			tSign.addCell(cBn);
			doc.add(tSign);
			doc.add(new Paragraph("Mã tra cứu: " + maHD, fS));
			doc.close();

			int opt = JOptionPane.showConfirmDialog(this, "In hóa đơn thành công!\nMở file PDF?", "Thành công",
					JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
			if (opt == JOptionPane.YES_OPTION)
				Desktop.getDesktop().open(new File(path));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi khi in: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	// ═══ PDF helpers ═══
	private void addInfoLine(Document doc, String lbl, String val, com.itextpdf.text.Font fL, com.itextpdf.text.Font fV)
			throws DocumentException {
		Paragraph p = new Paragraph();
		p.add(new Chunk(lbl + " ", fL));
		p.add(new Chunk(val, fV));
		p.setSpacingBefore(2);
		doc.add(p);
	}

	private void addPdfCell(PdfPTable t, String txt, com.itextpdf.text.Font f, int align) {
		PdfPCell c = new PdfPCell(new Phrase(txt, f));
		c.setHorizontalAlignment(align);
		c.setPadding(4);
		t.addCell(c);
	}

	private void addSumCell(PdfPTable t, String txt, com.itextpdf.text.Font f, BaseColor bg) {
		PdfPCell c = new PdfPCell(new Phrase(txt, f));
		c.setHorizontalAlignment(Element.ALIGN_RIGHT);
		c.setPadding(5);
		c.setBackgroundColor(bg);
		t.addCell(c);
	}

	private double parseNum(String s) {
		try {
			return Double.parseDouble(s.replace(" VNĐ", "").replace(",", "").trim());
		} catch (Exception e) {
			return 0;
		}
	}

	// ═══ UI helpers ═══
	private JTable buildStyledTable(DefaultTableModel model) {
		JTable t = new JTable(model) {
			public Component prepareRenderer(TableCellRenderer r, int row, int col) {
				Component c = super.prepareRenderer(r, row, col);
				if (!isRowSelected(row))
					c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
				return c;
			}
		};
		t.setRowHeight(34);
		t.setFont(F_CELL);
		t.setGridColor(BORDER);
		t.setSelectionBackground(ROW_SEL);
		t.setSelectionForeground(TEXT_DARK);
		t.setShowVerticalLines(false);
		t.getTableHeader().setDefaultRenderer(new HeaderRenderer());
		t.getTableHeader().setPreferredSize(new Dimension(0, 38));
		t.getTableHeader().setReorderingAllowed(false);
		return t;
	}

	private JPanel createStatCard(String title, JLabel v, Color accent) {
		JPanel p = new JPanel(new BorderLayout(5, 5));
		p.setBackground(BG_CARD);
		p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(226, 232, 240), 1, true),
				new EmptyBorder(15, 20, 15, 20)));
		JLabel t = new JLabel(title);
		t.setForeground(new Color(100, 116, 139));
		t.setFont(new Font("Segoe UI", Font.BOLD, 12));
		v.setForeground(accent);
		v.setFont(new Font("Segoe UI", Font.BOLD, 26));
		p.add(t, BorderLayout.NORTH);
		p.add(v, BorderLayout.CENTER);
		return p;
	}

	private JPanel buildCard(LayoutManager layout) {
		JPanel p = new JPanel(layout);
		p.setBackground(BG_CARD);
		p.setBorder(new LineBorder(BORDER, 1, true));
		return p;
	}

	private JLabel makeLabel(String text) {
		JLabel l = new JLabel(text);
		l.setFont(F_LABEL);
		l.setForeground(TEXT_MID);
		return l;
	}

	private JTextField makeTextField(int w) {
		JTextField tf = new JTextField();
		tf.setFont(F_CELL);
		tf.setPreferredSize(new Dimension(w, 34));
		tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true),
				BorderFactory.createEmptyBorder(4, 8, 4, 8)));
		return tf;
	}

	private JButton makeBtn(String text, BtnStyle style) {
		JButton btn = new JButton(text) {
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Color c;
				switch (style) {
				case SUCCESS:
					c = getModel().isRollover() ? new Color(0x22A060) : BTN_GREEN;
					break;
				case SECONDARY:
					c = getModel().isRollover() ? new Color(0xE5ECF6) : Color.WHITE;
					break;
				default:
					c = getModel().isRollover() ? ACCENT_HVR : ACCENT;
					break;
				}
				g2.setColor(c);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				if (style == BtnStyle.SECONDARY) {
					g2.setColor(BORDER);
					g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
				}
				g2.dispose();
				super.paintComponent(g);
			}
		};
		btn.setFont(F_LABEL);
		btn.setForeground(style == BtnStyle.SECONDARY ? TEXT_DARK : Color.WHITE);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return btn;
	}

	private void styleScrollBar(JScrollBar sb) {
		sb.setUI(new BasicScrollBarUI() {
			protected void configureScrollBarColors() {
				thumbColor = new Color(0x5B9BD5);
				trackColor = new Color(0xF0F5FF);
			}

			protected JButton createDecreaseButton(int o) {
				return zBtn();
			}

			protected JButton createIncreaseButton(int o) {
				return zBtn();
			}

			private JButton zBtn() {
				JButton b = new JButton();
				b.setPreferredSize(new Dimension(0, 0));
				return b;
			}

			protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(isDragging ? new Color(0x1A5EAB) : new Color(0x5B9BD5));
				g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
				g2.dispose();
			}

			protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
				g.setColor(new Color(0xF0F5FF));
				g.fillRect(r.x, r.y, r.width, r.height);
			}
		});
		sb.setPreferredSize(new Dimension(10, 10));
	}

	private static class HeaderRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
			JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
			l.setBackground(new Color(0x1A5EAB));
			l.setForeground(Color.WHITE);
			l.setFont(new Font("Segoe UI", Font.BOLD, 13));
			l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
			return l;
		}
	}

	// ═══════════════════════════════════════════════════════
	// DATE PICKER FIELD — style TAB_LichTrinh_ChuyenTau
	// JWindow popup, icon lịch vẽ tay, combo Tháng/Năm, grid ngày
	// ═══════════════════════════════════════════════════════
	private class DatePickerField extends JPanel {
		private final JTextField txt;
		private final Calendar cal;
		private JPanel pnlGrid;
		private JComboBox<String> cbThang;
		private JComboBox<Integer> cbNam;
		private JWindow popup;
		private Date selectedDate = null;

		private static final String[] TEN_THANG = { "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
				"Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12" };
		private static final String[] TEN_THU = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };

		DatePickerField(String init) {
			setLayout(new BorderLayout());
			setOpaque(false);
			cal = Calendar.getInstance();
			if (init != null && !init.isEmpty()) {
				try {
					cal.setTime(new SimpleDateFormat(DATE_FMT).parse(init));
				} catch (Exception ignored) {
				}
			}

			txt = new JTextField(init != null ? init : "");
			txt.setFont(F_CELL);
			txt.setForeground(TEXT_DARK);
			txt.setBackground(new Color(0xF8FAFD));
			txt.setEditable(false);
			txt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true),
					BorderFactory.createEmptyBorder(5, 10, 5, 34)));

			// Icon lịch vẽ tay (đồng bộ với LichTrinh)
			JLabel ico = new JLabel() {
				private boolean hov = false;
				{
					addMouseListener(new MouseAdapter() {
						public void mouseEntered(MouseEvent e) {
							hov = true;
							repaint();
						}

						public void mouseExited(MouseEvent e) {
							hov = false;
							repaint();
						}
					});
				}

				protected void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					Color ic = hov ? ACCENT : TEXT_MID;
					g2.setColor(ic);
					int cx = getWidth() / 2, cy = getHeight() / 2;
					g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g2.drawRoundRect(cx - 8, cy - 7, 16, 14, 3, 3);
					g2.drawLine(cx - 8, cy - 3, cx + 8, cy - 3);
					g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g2.drawLine(cx - 4, cy - 11, cx - 4, cy - 5);
					g2.drawLine(cx + 4, cy - 11, cx + 4, cy - 5);
					g2.setStroke(new BasicStroke(1f));
					int[] dx = { cx - 5, cx, cx + 5, cx - 5, cx }, dy = { cy, cy, cy, cy + 4, cy + 4 };
					for (int i = 0; i < 5; i++)
						g2.fillOval(dx[i] - 2, dy[i] - 2, 4, 4);
					g2.dispose();
				}
			};
			ico.setPreferredSize(new Dimension(30, 34));
			ico.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			ico.setToolTipText("Chọn ngày");

			JPanel wrap = new JPanel(new BorderLayout());
			wrap.setOpaque(false);
			wrap.add(txt, BorderLayout.CENTER);
			wrap.add(ico, BorderLayout.EAST);
			add(wrap, BorderLayout.CENTER);

			MouseAdapter ma = new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					toggle();
				}
			};
			txt.addMouseListener(ma);
			ico.addMouseListener(ma);
		}

		public Date getSelectedDate() {
			return selectedDate;
		}

		public String getDate() {
			return txt.getText();
		}

		public void resetDate() {
			selectedDate = null;
			txt.setText("");
			cal.setTime(new Date());
			firePropertyChange("date", selectedDate, null);
		}

		private void toggle() {
			if (popup != null && popup.isVisible()) {
				popup.dispose();
				popup = null;
				return;
			}
			showPop();
		}

		private void showPop() {
			popup = new JWindow(SwingUtilities.getWindowAncestor(this));
			popup.setLayout(new BorderLayout());

			JPanel p = new JPanel(new BorderLayout(0, 8));
			p.setBackground(BG_CARD);
			p.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(),
					BorderFactory.createEmptyBorder(10, 10, 10, 10)));

			p.add(navBar(), BorderLayout.NORTH);

			JSeparator sep = new JSeparator();
			sep.setForeground(BORDER);
			p.add(sep, BorderLayout.CENTER);

			pnlGrid = new JPanel(new GridLayout(0, 7, 2, 2));
			pnlGrid.setBackground(BG_CARD);
			pnlGrid.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

			JPanel south = new JPanel(new BorderLayout());
			south.setOpaque(false);
			south.add(pnlGrid, BorderLayout.CENTER);

			JButton btnToday = new JButton("Hôm nay") {
				protected void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					if (getModel().isRollover()) {
						g2.setColor(new Color(0xEBF3FF));
						g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
					}
					g2.dispose();
					super.paintComponent(g);
				}
			};
			btnToday.setFont(F_SMALL);
			btnToday.setForeground(ACCENT);
			btnToday.setContentAreaFilled(false);
			btnToday.setBorderPainted(false);
			btnToday.setFocusPainted(false);
			btnToday.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			btnToday.addActionListener(e -> {
				cal.setTime(new Date());
				cbThang.setSelectedIndex(cal.get(Calendar.MONTH));
				cbNam.setSelectedItem(cal.get(Calendar.YEAR));
				selectedDate = cal.getTime();
				txt.setText(new SimpleDateFormat(DATE_FMT).format(selectedDate));
				fillGrid();
				if (popup != null) {
					popup.dispose();
					popup = null;
				}
				firePropertyChange("date", null, selectedDate);
			});

			JPanel todayBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
			todayBar.setBackground(BG_CARD);
			todayBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
			todayBar.add(btnToday);
			south.add(todayBar, BorderLayout.SOUTH);
			p.add(south, BorderLayout.SOUTH);

			fillGrid();
			popup.add(p);
			popup.pack();
			popup.setSize(Math.max(240, popup.getWidth()), popup.getHeight());
			try {
				Point loc = txt.getLocationOnScreen();
				popup.setLocation(loc.x, loc.y + txt.getHeight() + 4);
			} catch (Exception ex) {
				/* chưa visible */ }
			popup.setVisible(true);
			popup.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
				public void windowGainedFocus(java.awt.event.WindowEvent e) {
				}

				public void windowLostFocus(java.awt.event.WindowEvent e) {
					if (popup != null) {
						popup.dispose();
						popup = null;
					}
				}
			});
		}

		private JPanel navBar() {
			JPanel nav = new JPanel(new BorderLayout(6, 0));
			nav.setBackground(BG_CARD);
			nav.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

			JButton prev = navBtn("\u2039");
			JButton next = navBtn("\u203a");

			cbThang = new JComboBox<>(TEN_THANG);
			cbThang.setFont(new Font("Segoe UI", Font.BOLD, 12));
			cbThang.setSelectedIndex(cal.get(Calendar.MONTH));
			cbThang.setPreferredSize(new Dimension(90, 28));
			cbThang.addActionListener(e -> {
				cal.set(Calendar.MONTH, cbThang.getSelectedIndex());
				fillGrid();
			});

			int y = Calendar.getInstance().get(Calendar.YEAR);
			Integer[] yrs = new Integer[16];
			for (int i = 0; i < 16; i++)
				yrs[i] = y - 5 + i;
			cbNam = new JComboBox<>(yrs);
			cbNam.setFont(new Font("Segoe UI", Font.BOLD, 12));
			cbNam.setSelectedItem(cal.get(Calendar.YEAR));
			cbNam.setPreferredSize(new Dimension(64, 28));
			cbNam.addActionListener(e -> {
				if (cbNam.getSelectedItem() != null) {
					cal.set(Calendar.YEAR, (Integer) cbNam.getSelectedItem());
					fillGrid();
				}
			});

			prev.addActionListener(e -> {
				cal.add(Calendar.MONTH, -1);
				cbThang.setSelectedIndex(cal.get(Calendar.MONTH));
				cbNam.setSelectedItem(cal.get(Calendar.YEAR));
				fillGrid();
			});
			next.addActionListener(e -> {
				cal.add(Calendar.MONTH, 1);
				cbThang.setSelectedIndex(cal.get(Calendar.MONTH));
				cbNam.setSelectedItem(cal.get(Calendar.YEAR));
				fillGrid();
			});

			JPanel ctr = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
			ctr.setBackground(BG_CARD);
			ctr.add(cbThang);
			ctr.add(cbNam);
			nav.add(prev, BorderLayout.WEST);
			nav.add(ctr, BorderLayout.CENTER);
			nav.add(next, BorderLayout.EAST);
			return nav;
		}

		private void fillGrid() {
			pnlGrid.removeAll();
			for (int i = 0; i < TEN_THU.length; i++) {
				JLabel l = new JLabel(TEN_THU[i], SwingConstants.CENTER);
				l.setFont(new Font("Segoe UI", Font.BOLD, 11));
				l.setPreferredSize(new Dimension(28, 22));
				l.setForeground(i == 6 ? new Color(0xEF4444) : TEXT_MID);
				pnlGrid.add(l);
			}
			Calendar tmp = (Calendar) cal.clone();
			tmp.set(Calendar.DAY_OF_MONTH, 1);
			int first = (tmp.get(Calendar.DAY_OF_WEEK) + 5) % 7;

			Calendar today = Calendar.getInstance();
			int todayD = today.get(Calendar.DAY_OF_MONTH);
			boolean sameM = today.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
					&& today.get(Calendar.YEAR) == cal.get(Calendar.YEAR);

			int chosen = -1;
			if (selectedDate != null) {
				Calendar sc = Calendar.getInstance();
				sc.setTime(selectedDate);
				if (sc.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
						&& sc.get(Calendar.YEAR) == cal.get(Calendar.YEAR))
					chosen = sc.get(Calendar.DAY_OF_MONTH);
			}
			for (int i = 0; i < first; i++)
				pnlGrid.add(new JLabel());
			int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			final int fc = chosen;
			for (int d = 1; d <= days; d++) {
				final int nd = d;
				boolean isT = sameM && d == todayD;
				boolean isSel = d == fc;
				boolean isSun = (first + d - 1) % 7 == 6;
				JButton b = new JButton(String.valueOf(d)) {
					protected void paintComponent(Graphics g) {
						Graphics2D g2 = (Graphics2D) g.create();
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						if (isSel) {
							g2.setColor(ACCENT);
							g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
						} else if (getModel().isRollover()) {
							g2.setColor(new Color(0xDDEEFF));
							g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
						} else if (isT) {
							g2.setColor(new Color(0xEBF5FF));
							g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
							g2.setColor(ACCENT);
							g2.setStroke(new BasicStroke(1.2f));
							g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 8, 8);
						}
						g2.dispose();
						super.paintComponent(g);
					}
				};
				b.setFont(new Font("Segoe UI", isT ? Font.BOLD : Font.PLAIN, 11));
				b.setForeground(isSel ? Color.WHITE : isSun ? new Color(0xEF4444) : isT ? ACCENT : TEXT_DARK);
				b.setPreferredSize(new Dimension(28, 28));
				b.setContentAreaFilled(false);
				b.setBorderPainted(false);
				b.setFocusPainted(false);
				b.setMargin(new Insets(0, 0, 0, 0));
				b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				b.addActionListener(e -> {
					cal.set(Calendar.DAY_OF_MONTH, nd);
					selectedDate = cal.getTime();
					txt.setText(new SimpleDateFormat(DATE_FMT).format(selectedDate));
					if (popup != null) {
						popup.dispose();
						popup = null;
					}
					firePropertyChange("date", null, selectedDate);
				});
				pnlGrid.add(b);
			}
			pnlGrid.revalidate();
			pnlGrid.repaint();
		}

		private JButton navBtn(String t) {
			JButton b = new JButton(t);
			b.setFont(new Font("Segoe UI", Font.PLAIN, 18));
			b.setForeground(ACCENT);
			b.setContentAreaFilled(false);
			b.setBorderPainted(false);
			b.setFocusPainted(false);
			b.setMargin(new Insets(0, 0, 0, 0));
			b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			b.setPreferredSize(new Dimension(26, 26));
			return b;
		}
	}

	// ═══ SHADOW BORDER ═══
	private static class ShadowBorder extends AbstractBorder {
		private static final int S = 4;

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (int i = S; i > 0; i--) {
				g2.setColor(new Color(100, 140, 200, (int) (20.0 * (S - i) / S)));
				g2.drawRoundRect(x + i, y + i, w - 2 * i - 1, h - 2 * i - 1, 12, 12);
			}
			g2.setColor(new Color(0xE2EAF4));
			g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
			g2.setColor(BG_CARD);
			g2.setClip(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, 12, 12));
			g2.fillRect(x + 1, y + 1, w - 2, h - 2);
			g2.dispose();
		}

		public Insets getBorderInsets(Component c) {
			return new Insets(S, S, S, S);
		}

		public Insets getBorderInsets(Component c, Insets ins) {
			ins.set(S, S, S, S);
			return ins;
		}
	}
}