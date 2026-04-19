package com.gui.banve;

import com.connectDB.ConnectDB;
import com.dao.DAO_ChuyenTau;
import com.dao.DAO_Toa;
import com.dao.DAO_Tuyen;
import com.dao.DAO_KhuyenMaiDetail;
import com.entities.*;
import com.enums.LoaiKhuyenMai;
import com.enums.TrangThaiVe;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

public class Step4_ThanhToan extends JPanel {

	// ── Enum phương thức thanh toán ──────────────────────────────────────────
	public enum PhuongThucThanhToan {
		TIEN_MAT("💵  Tiền mặt"), CHUYEN_KHOAN("🏦  Chuyển khoản");

		public final String label;

		PhuongThucThanhToan(String label) {
			this.label = label;
		}
	}

	private final TAB_BanVe mainTab;
	private final Connection conn;
	private final DAO_KhuyenMaiDetail daoKMD;

	// ── Dữ liệu ──────────────────────────────────────────────────────────────
	private NhanVien nv;
	private KhachHang kh;

	private Tuyen tuyen;
	// Cache tuyến theo mã chuyến để tránh lookup lặp lại khi hiển thị nhiều vé
	private final Map<String, Tuyen> tuyenByMaChuyenCache = new HashMap<>();

	// Lưu mã hóa đơn của phiên giao dịch hiện tại để đồng bộ UI và DB
	private String currentMaHD;
	private boolean isProcessingPayment = false;

	/**
	 * VeEntry = 1 vé + danh sách khuyến mãi đã chọn cho chính vé đó.
	 */
	private static class VeEntry {
		Ve ve;
		final List<ChiTiet_KhuyenMai> dsChiTietKM = new ArrayList<>();

		long tienGoc() {
			return ve != null ? (long) ve.getGiaVe() : 0L;
		}

		private List<ChiTiet_KhuyenMai> sortedChiTietKM() {
			List<ChiTiet_KhuyenMai> sorted = new ArrayList<>(dsChiTietKM);
			sorted.sort((a, b) -> {
				KhuyenMaiDetail k1 = a.getKhuyenMaiDetail();
				KhuyenMaiDetail k2 = b.getKhuyenMaiDetail();
				boolean aLaTien = k1 != null && k1.getLoaiKM() == LoaiKhuyenMai.GIAM_TIEN;
				boolean bLaTien = k2 != null && k2.getLoaiKM() == LoaiKhuyenMai.GIAM_TIEN;
				return Boolean.compare(bLaTien, aLaTien);
			});
			return sorted;
		}

		long[] tinhGiamTungBuoc() {
			List<ChiTiet_KhuyenMai> sorted = sortedChiTietKM();
			long[] giamMoi = new long[sorted.size()];
			long conLai = tienGoc();

			for (int i = 0; i < sorted.size(); i++) {
				KhuyenMaiDetail km = sorted.get(i).getKhuyenMaiDetail();
				if (km == null)
					continue;

				long giam;
				if (km.getLoaiKM() == LoaiKhuyenMai.GIAM_TIEN) {
					giam = Math.min(conLai, Math.round(km.getGiaTri()));
				} else {
					giam = Math.round(conLai * km.getGiaTri() / 100.0);
				}
				giam = Math.min(giam, conLai);
				giamMoi[i] = giam;
				sorted.get(i).setTienGiamCuaKM(giam);
				conLai -= giam;
			}
			return giamMoi;
		}

		List<ChiTiet_KhuyenMai> getChiTietKMSortedAndCalculated() {
			tinhGiamTungBuoc();
			return sortedChiTietKM();
		}

		boolean containsKhuyenMai(String maKMDetail) {
			return dsChiTietKM.stream().anyMatch(ct -> {
				KhuyenMaiDetail km = ct.getKhuyenMaiDetail();
				return km != null && km.getMaKMDetail().equals(maKMDetail);
			});
		}

		void addKhuyenMai(KhuyenMaiDetail km) {
			if (km == null || containsKhuyenMai(km.getMaKMDetail()))
				return;
			ChiTiet_KhuyenMai ct = new ChiTiet_KhuyenMai();
			ct.setVe(ve);
			ct.setKhuyenMaiDetail(km);
			ct.setTienGiamCuaKM(0);
			dsChiTietKM.add(ct);
			tinhGiamTungBuoc();
		}

		void removeKhuyenMai(String maKMDetail) {
			dsChiTietKM.removeIf(ct -> {
				KhuyenMaiDetail km = ct.getKhuyenMaiDetail();
				return km != null && km.getMaKMDetail().equals(maKMDetail);
			});
			tinhGiamTungBuoc();
		}

		long tienGiam() {
			tinhGiamTungBuoc();
			long sum = 0;
			for (ChiTiet_KhuyenMai ct : dsChiTietKM) {
				sum += Math.round(ct.getTienGiamCuaKM());
			}
			return sum;
		}

		long thanhTien() {
			return Math.max(0L, tienGoc() - tienGiam());
		}
	}

	private final List<VeEntry> dsVe = new ArrayList<>();

	// ── UI refs để refresh ───────────────────────────────────────────────────
	private JPanel pnlReceiptList;
	private JPanel pnlVeList;
	private JLabel lblThanhTien;
	private JLabel lblTienGiam;
	private JLabel lblTongTien;

	// ── Phương thức thanh toán đang chọn ─────────────────────────────────────
	private PhuongThucThanhToan phuongThuc = PhuongThucThanhToan.TIEN_MAT;
	private JToggleButton btnTienMat;
	private JToggleButton btnChuyenKhoan;

	private static final NumberFormat FMT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
	private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

	// =========================================================================
	// CONSTRUCTOR
	// =========================================================================
	public Step4_ThanhToan(TAB_BanVe mainTab) {
		this.mainTab = mainTab;
		this.conn = ConnectDB.getConnection();
		this.daoKMD = new DAO_KhuyenMaiDetail(conn);
		initUI();
	}

	// =========================================================================
	// LẤY DỮ LIỆU THỰC TẾ TỪ CÁC BƯỚC TRƯỚC
	// =========================================================================
	public void loadDataFromSession() {
		dsVe.clear();
		tuyenByMaChuyenCache.clear();
		List<LoaiVe> allLoaiVe = getAllLoaiVe();
		List<Map<String, String>> sessionData = mainTab.getSelectedSeatsData();

		// 1. Lấy Nhân Viên hiện tại
		this.nv = mainTab.getNhanVienHienTai();
		if (this.nv == null) {
			this.nv = new NhanVien();
			this.nv.setMaNV("NV001");
			this.nv.setTenNV("Nhân viên Demo");
		}

		// 2. Lấy NGƯỜI ĐẶT VÉ (Booker) làm người đứng tên Hóa Đơn
		Map<String, String> bookerMap = mainTab.getPassengerDataMap().get("BOOKER");
		if (bookerMap != null) {
			this.kh = new KhachHang();
			this.kh.setMaKH(bookerMap.get("maKH"));
			this.kh.setHoTen(bookerMap.get("ten"));
			this.kh.setSdt(bookerMap.get("sdt"));
		} else {
			this.kh = new KhachHang();
			this.kh.setHoTen("Khách vãng lai");
		}

		if (sessionData == null || sessionData.isEmpty())
			return;

		if (isBlank(this.kh.getMaKH())) {
			for (Map<String, String> seat : sessionData) {
				String seatMaKH = seat.get("maKH");
				if (!isBlank(seatMaKH)) {
					this.kh.setMaKH(seatMaKH);
					break;
				}
			}
		}

		com.dao.DAO_KhachHang daoKH = new com.dao.DAO_KhachHang();
		com.dao.DAO_LichTrinh daoLT = new com.dao.DAO_LichTrinh();
		com.dao.DAO_Toa daoToa = new com.dao.DAO_Toa();

		// Sinh mã HĐ và Mã Vé ngay lúc này để UI và DB đồng nhất
		this.currentMaHD = createMaHD();
		int veCounter = 1;

		for (Map<String, String> map : sessionData) {
			String maLT = map.get("maLT");
			String maCho = map.get("maCho");
			String maKH = map.get("maKH");
			String maLoaiVe = map.get("maLoaiVe");
			String maToa = map.get("maToa");
			String viTriGhe = map.get("viTriGhe");
			if (isBlank(maToa) && !isBlank(maCho) && maCho.contains("_")) {
				maToa = maCho.substring(0, maCho.indexOf('_'));
			}

			// Xử lý Giá Vé
			long giaGoc = 0;
			try {
				String strGia = map.get("giaVe");
				if (strGia != null && !strGia.isEmpty()) {
					strGia = strGia.split("\\.")[0].replaceAll("[^0-9]", "");
					giaGoc = Long.parseLong(strGia);
				}
			} catch (Exception e) {
				System.err.println("Lỗi xử lý giá vé cho ghế: " + maCho);
			}

			// Xử lý Hành Khách
			KhachHang passenger = null;
			if (maKH != null && !maKH.isEmpty()) {
				passenger = daoKH.getKhachHangByMa(maKH);
			}
			if (passenger == null) {
				passenger = new KhachHang();
				passenger.setMaKH(null);
				passenger.setHoTen("Khách vãng lai");
			}

			LichTrinh lichTrinh = daoLT.getLichTrinhByMa(maLT);
			Toa toa = daoToa.getToaById(maToa);
			LoaiVe loaiVe = allLoaiVe.stream().filter(lv -> lv.getMaLoai().equals(maLoaiVe)).findFirst().orElse(null);

			// Tạo Đối tượng Vé
			Ve ve = new Ve();
			ve.setMaVe(createMaVe(veCounter++)); // Mã vé đồng nhất, đảm bảo <= 20 ký tự
			ve.setKhachHang(passenger);
			ve.setLichTrinh(lichTrinh);
			ve.setToa(toa);
			ve.setViTriGhe(viTriGhe);
			ve.setLoaiVe(loaiVe);
			ve.setGiaVe(giaGoc);
			ve.setTrangThaiVe(TrangThaiVe.CHUA_SU_DUNG);

			VeEntry entry = new VeEntry();
			entry.ve = ve;
			dsVe.add(entry);

			// Lấy Tuyến Đường
			if (this.tuyen == null && lichTrinh != null) {
				this.tuyen = getTuyenByMaChuyen(lichTrinh.getMaChuyen());
			}
		}

		refreshAll();
	}

	private Tuyen getTuyenByMaChuyen(String maChuyen) {
		if (maChuyen == null || maChuyen.isEmpty())
			return null;
		if (tuyenByMaChuyenCache.containsKey(maChuyen))
			return tuyenByMaChuyenCache.get(maChuyen);

		Tuyen result = null;
		DAO_ChuyenTau daoChuyen = new DAO_ChuyenTau();
		for (DAO_ChuyenTau.ChuyenTauRow row : daoChuyen.getAll()) {
			if (maChuyen.equals(row.maChuyen)) {
				result = new DAO_Tuyen().getTuyenByMa(row.maTuyen);
				break;
			}
		}
		tuyenByMaChuyenCache.put(maChuyen, result);
		return result;
	}

	private String getTenTuyenForVe(Ve ve) {
		if (ve == null || ve.getLichTrinh() == null)
			return "—";
		Tuyen tuyenVe = getTuyenByMaChuyen(ve.getLichTrinh().getMaChuyen());
		return tuyenVe != null ? tuyenVe.getTenTuyen() : "—";
	}

	private List<LoaiVe> getAllLoaiVe() {
		List<LoaiVe> list = new ArrayList<>();
		String sql = "SELECT * FROM LoaiVe";
		try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				LoaiVe lv = new LoaiVe();
				lv.setMaLoai(rs.getString("MaLoai"));
				lv.setTenLoai(rs.getString("TenLoai"));
				list.add(lv);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	// =========================================================================
	// INIT UI
	// =========================================================================
	private void initUI() {
		setLayout(new BorderLayout(0, 16));
		setOpaque(false);
		add(UIHelper.createPageTitle("THANH TOÁN & HOÀN TẤT", ""), BorderLayout.NORTH);

		JPanel body = new JPanel(new GridBagLayout());
		body.setOpaque(false);
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.BOTH;
		gc.weighty = 1.0;

		gc.gridx = 0;
		gc.weightx = 0.55;
		gc.insets = new Insets(0, 0, 0, 10);
		body.add(buildHoaDonPanel(), gc);

		gc.gridx = 1;
		gc.weightx = 0.45;
		gc.insets = new Insets(0, 10, 0, 0);
		body.add(buildVePanel(), gc);

		add(body, BorderLayout.CENTER);
	}

	// =========================================================================
	// PANEL TRÁI – Hóa đơn tổng hợp (read-only)
	// =========================================================================
	private JPanel buildHoaDonPanel() {
		JPanel card = UIHelper.makeCard(new BorderLayout(0, 12));
		card.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(),
				BorderFactory.createEmptyBorder(24, 24, 20, 24)));

		// Tiêu đề + thông tin HoaDon
		JLabel title = new JLabel("HÓA ĐƠN");
		title.setFont(UIHelper.F_H2);
		title.setForeground(UIHelper.ACCENT);

		JPanel pnlMeta = new JPanel(new GridLayout(3, 2, 6, 5));
		pnlMeta.setOpaque(false);
		pnlMeta.add(makeInfoLbl("Ngày lập:", true));
		pnlMeta.add(makeInfoLbl(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), false));
		pnlMeta.add(makeInfoLbl("Nhân viên:", true));
		pnlMeta.add(makeInfoLbl(nv != null ? nv.getTenNV() + " (" + nv.getMaNV() + ")" : "—", false));
		pnlMeta.add(makeInfoLbl("Khách hàng:", true));
		pnlMeta.add(makeInfoLbl(kh != null ? kh.getHoTen() : "—", false));

		JPanel north = new JPanel(new BorderLayout(0, 8));
		north.setOpaque(false);
		north.add(title, BorderLayout.NORTH);
		north.add(new JSeparator(), BorderLayout.CENTER);
		north.add(pnlMeta, BorderLayout.SOUTH);
		card.add(north, BorderLayout.NORTH);

		// Danh sách receipt
		pnlReceiptList = new JPanel();
		pnlReceiptList.setLayout(new BoxLayout(pnlReceiptList, BoxLayout.Y_AXIS));
		pnlReceiptList.setOpaque(false);
		pnlReceiptList.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

		JScrollPane scroll = new JScrollPane(pnlReceiptList);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(Color.WHITE);
		card.add(scroll, BorderLayout.CENTER);

		// Tổng tiền
		lblThanhTien = makeSumLbl("0 đ", false);
		lblTienGiam = makeSumLbl("- 0 đ", false);
		lblTongTien = makeSumLbl("0 đ", true);
		lblTongTien.setForeground(UIHelper.DANGER);
		lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 24));

		JPanel pnlSum = new JPanel();
		pnlSum.setLayout(new BoxLayout(pnlSum, BoxLayout.Y_AXIS));
		pnlSum.setOpaque(false);
		pnlSum.add(Box.createVerticalStrut(8));
		pnlSum.add(new JSeparator());
		pnlSum.add(Box.createVerticalStrut(8));
		pnlSum.add(makeSumRow("Thành tiền:", lblThanhTien, false));
		pnlSum.add(Box.createVerticalStrut(5));
		pnlSum.add(makeSumRow("Giảm giá (KM):", lblTienGiam, false));
		pnlSum.add(Box.createVerticalStrut(8));
		pnlSum.add(new JSeparator());
		pnlSum.add(Box.createVerticalStrut(8));
		pnlSum.add(makeSumRow("TỔNG THANH TOÁN:", lblTongTien, true));
		card.add(pnlSum, BorderLayout.SOUTH);

		return card;
	}

	// =========================================================================
	// PANEL PHẢI – Danh sách vé + ComboBox KM từng vé, nút xóa
	// =========================================================================
	private JPanel buildVePanel() {
		JPanel card = UIHelper.makeCard(new BorderLayout(0, 12));
		card.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(),
				BorderFactory.createEmptyBorder(24, 24, 20, 24)));

		// Tiêu đề
		JPanel northBar = new JPanel(new BorderLayout());
		northBar.setOpaque(false);
		JLabel title = new JLabel("CHI TIẾT VÉ");
		title.setFont(UIHelper.F_H2);
		title.setForeground(UIHelper.ACCENT);
		JLabel hint = new JLabel("Chọn khuyến mãi hoặc xóa từng vé");
		hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
		hint.setForeground(UIHelper.TEXT_LIGHT);
		northBar.add(title, BorderLayout.NORTH);
		northBar.add(hint, BorderLayout.SOUTH);
		card.add(northBar, BorderLayout.NORTH);

		// Danh sách vé có thể scroll
		pnlVeList = new JPanel();
		pnlVeList.setLayout(new BoxLayout(pnlVeList, BoxLayout.Y_AXIS));
		pnlVeList.setOpaque(false);

		JScrollPane scroll = new JScrollPane(pnlVeList);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(Color.WHITE);
		card.add(scroll, BorderLayout.CENTER);

		JPanel pnlBottom = new JPanel(new BorderLayout());
		pnlBottom.setOpaque(false);
		pnlBottom.add(buildPhuongThucPanel(), BorderLayout.CENTER);

		JButton btnThanhToan = UIHelper.makeBtn("XÁC NHẬN THANH TOÁN", UIHelper.BtnStyle.PRIMARY);
		btnThanhToan.setPreferredSize(new Dimension(0, 50));
		btnThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 16));
		btnThanhToan.addActionListener(e -> xuLyThanhToanDatabase());
		pnlBottom.add(btnThanhToan, BorderLayout.SOUTH);

		card.add(pnlBottom, BorderLayout.SOUTH);
		return card;
	}

	// =========================================================================
	// REFRESH – build lại cả 2 panel từ dsVe
	// =========================================================================
	private void refreshAll() {
		// Cập nhật lại UI Meta (Tên NV, Tên KH) trên Hóa đơn nếu loadDataFromSession đã
		// chạy
		try {
			Component[] comps = ((JPanel) ((JPanel) getComponent(1)).getComponent(0)).getComponents();
			JPanel northCard = (JPanel) comps[0];
			JPanel pnlMeta = (JPanel) northCard.getComponent(2);
			((JLabel) pnlMeta.getComponent(3)).setText(nv != null ? nv.getTenNV() + " (" + nv.getMaNV() + ")" : "—");
			((JLabel) pnlMeta.getComponent(5)).setText(kh != null ? kh.getHoTen() : "—");
		} catch (Exception e) {
		}

		refreshVeList();
		refreshReceipt();
	}

	private void refreshVeList() {
		pnlVeList.removeAll();

		if (dsVe.isEmpty()) {
			JLabel empty = new JLabel("Không có vé nào");
			empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
			empty.setForeground(UIHelper.TEXT_LIGHT);
			empty.setAlignmentX(Component.CENTER_ALIGNMENT);
			empty.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
			pnlVeList.add(empty);
		}

		for (int i = 0; i < dsVe.size(); i++) {
			final int idx = i;
			final VeEntry entry = dsVe.get(i);
			pnlVeList.add(buildVeCard(entry, idx));
			pnlVeList.add(Box.createVerticalStrut(10));
		}

		pnlVeList.revalidate();
		pnlVeList.repaint();
	}

	private JPanel buildVeCard(VeEntry entry, int idx) {
		Ve ve = entry.ve;

		JPanel card = new JPanel(new BorderLayout(0, 8));
		card.setOpaque(true);
		card.setBackground(new Color(0xF7FAFF));
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(UIHelper.BORDER, 1, true),
				BorderFactory.createEmptyBorder(12, 14, 12, 14)));

		// ── HEADER: mã vé + nút xóa ──
		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		JLabel lblMaVe = new JLabel(ve.getMaVe());
		lblMaVe.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblMaVe.setForeground(UIHelper.ACCENT);

		JButton btnXoa = new JButton("✕ Xóa vé");
		btnXoa.setFont(new Font("Segoe UI", Font.BOLD, 11));
		btnXoa.setForeground(UIHelper.DANGER);
		btnXoa.setBackground(new Color(0xFFF0F0));
		btnXoa.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xFFC8C8), 1, true),
				BorderFactory.createEmptyBorder(3, 8, 3, 8)));
		btnXoa.setFocusPainted(false);
		btnXoa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnXoa.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(this, "Xóa vé " + ve.getMaVe() + " khỏi hóa đơn?", "Xác nhận",
					JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				// Xóa khỏi giỏ hàng
				mainTab.getSelectedSeatsData().removeIf(seat -> seat.get("viTriGhe").equals(ve.getViTriGhe())
						&& seat.get("maLT").equals(ve.getLichTrinh().getMaLT()));
				dsVe.remove(idx);
				refreshAll();
			}
		});
		header.add(lblMaVe, BorderLayout.WEST);
		header.add(btnXoa, BorderLayout.EAST);
		card.add(header, BorderLayout.NORTH);

		// ── THÔNG TIN VÉ ──
		JPanel info = new JPanel(new GridLayout(6, 2, 8, 3));
		info.setOpaque(false);

		String tenKH = (ve.getKhachHang() != null) ? ve.getKhachHang().getHoTen() : "—";

		String ngayGio = "—";
		if (ve.getLichTrinh() != null) {
			String ngay = ve.getLichTrinh().getNgayKhoiHanh() != null ? ve.getLichTrinh().getNgayKhoiHanh().toString()
					: "?";
			String gio = ve.getLichTrinh().getGioKhoiHanh() != null ? String.format("%02d:%02d",
					ve.getLichTrinh().getGioKhoiHanh().getHour(), ve.getLichTrinh().getGioKhoiHanh().getMinute()) : "?";
			ngayGio = ngay + "  " + gio;
		}

		String tenTuyen = getTenTuyenForVe(ve);
		String viTriGhe = ve.getViTriGhe() != null
				? ve.getToa().getLoaiToa().getTenLoaiToa() + " – Ghế " + ve.getViTriGhe()
				: "—";
		String loaiVeStr = ve.getLoaiVe() != null ? ve.getLoaiVe().getTenLoai() : "—";
		JLabel lblGia = new JLabel(buildGiaHtml(entry));

		info.add(makeVeInfoLbl("Hành khách:", true));
		info.add(makeVeInfoLbl(tenKH, false));
		info.add(makeVeInfoLbl("Khởi hành:", true));
		info.add(makeVeInfoLbl(ngayGio, false));
		info.add(makeVeInfoLbl("Tuyến:", true));
		info.add(makeVeInfoLbl(tenTuyen, false));
		info.add(makeVeInfoLbl("Chỗ:", true));
		info.add(makeVeInfoLbl(viTriGhe, false));
		info.add(makeVeInfoLbl("Loại:", true));
		info.add(makeVeInfoLbl(loaiVeStr, false));
		info.add(makeVeInfoLbl("Giá:", true));
		info.add(lblGia);
		card.add(info, BorderLayout.CENTER);

		// ── MULTI-SELECT KM + CHIPS ──
		JPanel bottom = new JPanel(new BorderLayout(0, 6));
		bottom.setOpaque(false);

		Tuyen tuyenVe = (ve.getLichTrinh() != null) ? getTuyenByMaChuyen(ve.getLichTrinh().getMaChuyen()) : null;
		List<KhuyenMaiDetail> dsKMD = daoKMD.getKhuyenMaiDetailKhaDung(new Date(), ve.getLoaiVe(),
				ve.getToa() != null ? ve.getToa().getLoaiToa() : null, tuyenVe);

		JPanel pnlChips = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 4));
		pnlChips.setOpaque(false);

		lblGia.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lblGia.setHorizontalAlignment(SwingConstants.RIGHT);

		Runnable refreshTicketPromoUI = new Runnable() {
			@Override
			public void run() {
				pnlChips.removeAll();
				List<ChiTiet_KhuyenMai> selected = entry.getChiTietKMSortedAndCalculated();
				for (ChiTiet_KhuyenMai ct : selected) {
					pnlChips.add(makeKMChip(ct, () -> {
						KhuyenMaiDetail km = ct.getKhuyenMaiDetail();
						if (km != null) {
							entry.removeKhuyenMai(km.getMaKMDetail());
							this.run();
							refreshReceipt();
							card.revalidate();
							card.repaint();
						}
					}));
				}
				if (selected.isEmpty()) {
					JLabel none = new JLabel("Chưa chọn KM");
					none.setFont(new Font("Segoe UI", Font.ITALIC, 11));
					none.setForeground(UIHelper.TEXT_LIGHT);
					pnlChips.add(none);
				}
				lblGia.setText(buildGiaHtml(entry));
				pnlChips.revalidate();
				pnlChips.repaint();
			}
		};
		refreshTicketPromoUI.run();

		JPanel dropdownRow = new JPanel(new BorderLayout(6, 0));
		dropdownRow.setOpaque(false);
		JButton btnDropdown = makeKMDropdownBtn(dsKMD, entry, () -> {
			refreshTicketPromoUI.run();
			refreshReceipt();
			card.revalidate();
			card.repaint();
		});

		dropdownRow.add(btnDropdown, BorderLayout.CENTER);

		bottom.add(dropdownRow, BorderLayout.NORTH);
		bottom.add(pnlChips, BorderLayout.CENTER);
		card.add(bottom, BorderLayout.SOUTH);

		return card;
	}

	private String buildGiaHtml(VeEntry e) {
		if (e.tienGiam() > 0) {
			return "<html><s style='color:#A0AEC0;font-size:10px'>" + formatTien(e.tienGoc()) + "</s>"
					+ "&nbsp;<b style='color:#16A34A'>" + formatTien(e.thanhTien()) + "</b></html>";
		}
		return "<html><b style='color:#1E2B3C'>" + formatTien(e.thanhTien()) + "</b></html>";
	}

	private void refreshReceipt() {
		pnlReceiptList.removeAll();

		long tongGoc = 0, tongGiam = 0;

		for (VeEntry entry : dsVe) {
			pnlReceiptList.add(buildReceiptRow(entry));
			pnlReceiptList.add(Box.createVerticalStrut(8));
			tongGoc += entry.tienGoc();
			tongGiam += entry.tienGiam();
		}

		if (dsVe.isEmpty()) {
			JLabel empty = new JLabel("Chưa có vé nào");
			empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
			empty.setForeground(UIHelper.TEXT_LIGHT);
			empty.setAlignmentX(Component.LEFT_ALIGNMENT);
			pnlReceiptList.add(Box.createVerticalStrut(10));
			pnlReceiptList.add(empty);
		}

		lblThanhTien.setText(formatTien(tongGoc));
		lblTienGiam.setText("- " + formatTien(tongGiam));
		lblTongTien.setText(formatTien(tongGoc - tongGiam));
		if (tongGiam > 0)
			lblTienGiam.setForeground(UIHelper.SUCCESS);

		pnlReceiptList.revalidate();
		pnlReceiptList.repaint();
	}

	private JPanel buildReceiptRow(VeEntry entry) {
		Ve ve = entry.ve;

		List<ChiTiet_KhuyenMai> sortedKMs = entry.getChiTietKMSortedAndCalculated();
		long[] giamMoi = entry.tinhGiamTungBuoc();

		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
		row.setOpaque(false);
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(UIHelper.BORDER, 1, true),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));

		JPanel hdr = new JPanel(new BorderLayout(8, 0));
		hdr.setOpaque(false);
		hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		JLabel lVe = new JLabel(ve.getMaVe());
		lVe.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lVe.setForeground(UIHelper.ACCENT);
		String tenKH = ve.getKhachHang() != null ? ve.getKhachHang().getHoTen() : "";
		JLabel lKH = new JLabel(tenKH);
		lKH.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lKH.setForeground(UIHelper.TEXT_MID);
		hdr.add(lVe, BorderLayout.WEST);
		hdr.add(lKH, BorderLayout.EAST);
		row.add(hdr);
		row.add(Box.createVerticalStrut(2));

		String tuyen = getTenTuyenForVe(ve);
		String viTriGhe = ve.getViTriGhe() != null
				? ve.getToa().getLoaiToa().getTenLoaiToa() + " – Ghế " + ve.getViTriGhe()
				: "—";
		String ngayGio = "—";
		String loaiVeStr = ve.getLoaiVe() != null ? ve.getLoaiVe().getTenLoai() : "—";

		if (ve.getLichTrinh() != null) {
			String ngay = ve.getLichTrinh().getNgayKhoiHanh() != null ? ve.getLichTrinh().getNgayKhoiHanh().toString()
					: "?";
			String gio = ve.getLichTrinh().getGioKhoiHanh() != null ? String.format("%02d:%02d",
					ve.getLichTrinh().getGioKhoiHanh().getHour(), ve.getLichTrinh().getGioKhoiHanh().getMinute()) : "?";
			ngayGio = ngay + " " + gio;
		}
		JLabel lDetail = new JLabel(tuyen + " | " + ngayGio + " | " + viTriGhe + " | " + loaiVeStr);
		lDetail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		lDetail.setForeground(UIHelper.TEXT_MID);
		lDetail.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.add(lDetail);
		row.add(Box.createVerticalStrut(6));

		JSeparator sep = new JSeparator();
		sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		sep.setForeground(UIHelper.BORDER);
		row.add(sep);
		row.add(Box.createVerticalStrut(6));

		JPanel rowGoc = makeReceiptPriceLine("Giá gốc", formatTien(entry.tienGoc()), false, UIHelper.TEXT_DARK, true);
		row.add(rowGoc);

		long conLai = entry.tienGoc();
		for (int i = 0; i < sortedKMs.size(); i++) {
			ChiTiet_KhuyenMai ctKM = sortedKMs.get(i);
			KhuyenMaiDetail k = ctKM.getKhuyenMaiDetail();
			long giam = giamMoi[i];
			conLai -= giam;

			row.add(Box.createVerticalStrut(3));
			String kmLabel = formatKhuyenMaiLine(k);
			JPanel rowKM = makeReceiptPriceLine("  ↳ " + kmLabel, "- " + formatTien(giam), false, UIHelper.DANGER,
					false);
			row.add(rowKM);
		}

		row.add(Box.createVerticalStrut(5));
		JSeparator sep2 = new JSeparator();
		sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		sep2.setForeground(UIHelper.BORDER);
		row.add(sep2);
		row.add(Box.createVerticalStrut(5));

		JPanel rowTT = makeReceiptPriceLine("Thành tiền", formatTien(entry.thanhTien()), false,
				entry.tienGiam() > 0 ? UIHelper.SUCCESS : UIHelper.TEXT_DARK, entry.tienGiam() > 0);
		row.add(rowTT);

		return row;
	}

	private JPanel makeReceiptPriceLine(String label, String price, boolean strikePrice, Color priceColor,
			boolean boldPrice) {
		JPanel line = new JPanel(new BorderLayout(4, 0));
		line.setOpaque(false);
		line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		line.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel lLbl = new JLabel(label);
		lLbl.setFont(new Font("Segoe UI", boldPrice ? Font.BOLD : Font.PLAIN, 12));
		lLbl.setForeground(UIHelper.TEXT_MID);

		String priceHtml;
		if (strikePrice)
			priceHtml = "<html><s style='color:#A0AEC0'>" + price + "</s></html>";
		else if (boldPrice)
			priceHtml = "<html><b>" + price + "</b></html>";
		else
			priceHtml = price;

		JLabel lPrice = new JLabel(priceHtml);
		lPrice.setFont(new Font("Segoe UI", boldPrice ? Font.BOLD : Font.PLAIN, 12));
		lPrice.setForeground(priceColor);
		lPrice.setHorizontalAlignment(SwingConstants.RIGHT);

		line.add(lLbl, BorderLayout.WEST);
		line.add(lPrice, BorderLayout.EAST);
		return line;
	}

	// =========================================================================
	// XỬ LÝ LƯU DATABASE (TRANSACTION)
	// =========================================================================
	private void xuLyThanhToanDatabase() {
		if (dsVe.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Không có vé nào để thanh toán!");
			return;
		}
		if (isProcessingPayment) {
			return;
		}
		if (conn == null) {
			JOptionPane.showMessageDialog(this, "Không thể kết nối CSDL. Vui lòng kiểm tra SQL Server.", "Lỗi kết nối",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String validationError = validatePaymentData();
		if (validationError != null) {
			JOptionPane.showMessageDialog(this, validationError, "Dữ liệu thanh toán chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận thanh toán?", "Xác nhận",
				JOptionPane.YES_NO_OPTION);
		if (confirm != JOptionPane.YES_OPTION)
			return;
		isProcessingPayment = true;

		long tongTienCuoiCung = 0;
		for (VeEntry e : dsVe)
			tongTienCuoiCung += e.thanhTien();

		try {
			// TẮT AUTO COMMIT ĐỂ BẢO VỆ DỮ LIỆU
			conn.setAutoCommit(false);

			// 1. INSERT VÀO BẢNG HOADON (Dùng currentMaHD đồng bộ với Mã Vé)
			String sqlHD = "INSERT INTO HoaDon (maHD, maNV, maKH, tongTien) VALUES (?, ?, ?, ?)";
			try (PreparedStatement psHD = conn.prepareStatement(sqlHD)) {
			    psHD.setString(1, this.currentMaHD);
			    psHD.setString(2, nv.getMaNV());
			    if (isBlank(kh.getMaKH())) {
			    	psHD.setNull(3, Types.VARCHAR);
			    } else {
			    	psHD.setString(3, kh.getMaKH());
			    }
			    psHD.setLong(4, tongTienCuoiCung);
			    psHD.executeUpdate();
			}
			// LOOP TỪNG VÉ
			String sqlVe = "INSERT INTO Ve (maVe, maKH, maLT, maToa, viTriGhe, maLoaiVe, giaVe, trangThaiVe) VALUES (?, ?, ?, ?, ?, ?, ?, 'CHUASUDUNG')";
			String sqlCTHD = "INSERT INTO ChiTietHoaDon (maHD, maVe, tienGoc, tienGiam, thanhTien) VALUES (?, ?, ?, ?, ?)";
			String sqlCTKM = "INSERT INTO ChiTiet_KhuyenMai (maHD, maVe, maKMDetail, tienGiamCuaKM) VALUES (?, ?, ?, ?)";
			String sqlChoNgoi = "UPDATE GheLichTrinh SET trangThai = 'DADAT' WHERE maLT = ? AND maToa = ? AND viTri = ?";
			String sqlEnsureGhe = "IF NOT EXISTS (SELECT 1 FROM GheLichTrinh WHERE maLT = ? AND maToa = ? AND viTri = ?) "
					+ "INSERT INTO GheLichTrinh(maLT, maToa, viTri, trangThai) VALUES (?, ?, ?, 'TRONG')";

			try (PreparedStatement psVe = conn.prepareStatement(sqlVe);
					PreparedStatement psCTHD = conn.prepareStatement(sqlCTHD);
					PreparedStatement psCTKM = conn.prepareStatement(sqlCTKM);
					PreparedStatement psCho = conn.prepareStatement(sqlChoNgoi);
					PreparedStatement psEnsureGhe = conn.prepareStatement(sqlEnsureGhe)) {

				for (VeEntry entry : dsVe) {
					// Lấy mã vé đã sinh đồng nhất trên UI
					String maVeReal = entry.ve.getMaVe();

					String maLT = entry.ve.getLichTrinh().getMaLT();
					String maToa = entry.ve.getToa().getMaToa();
					String viTri = entry.ve.getViTriGhe();

					// Đảm bảo ghế tồn tại trong GheLichTrinh trước khi insert Vé (tránh lỗi FK)
					psEnsureGhe.setString(1, maLT);
					psEnsureGhe.setString(2, maToa);
					psEnsureGhe.setString(3, viTri);
					psEnsureGhe.setString(4, maLT);
					psEnsureGhe.setString(5, maToa);
					psEnsureGhe.setString(6, viTri);
					psEnsureGhe.executeUpdate();

					// INSERT VÀO BẢNG VE
					psVe.setString(1, maVeReal);
					String maKHVe = (entry.ve.getKhachHang() != null) ? entry.ve.getKhachHang().getMaKH() : null;
					if (isBlank(maKHVe)) {
						psVe.setNull(2, Types.VARCHAR);
					} else {
						psVe.setString(2, maKHVe);
					}
					psVe.setString(3, maLT);
					psVe.setString(4, maToa);
					psVe.setString(5, viTri);
					psVe.setString(6, entry.ve.getLoaiVe().getMaLoai());
					psVe.setLong(7, entry.tienGoc());
					psVe.executeUpdate();

					// INSERT CTHD
					psCTHD.setString(1, this.currentMaHD);
					psCTHD.setString(2, maVeReal);
					psCTHD.setLong(3, entry.tienGoc());
					psCTHD.setLong(4, entry.tienGiam());
					psCTHD.setLong(5, entry.thanhTien());
					psCTHD.executeUpdate();

					// INSERT CHI TIẾT KHUYẾN MÃI
					for (ChiTiet_KhuyenMai ctKM : entry.getChiTietKMSortedAndCalculated()) {
						KhuyenMaiDetail km = ctKM.getKhuyenMaiDetail();
						if (km == null)
							continue;
						psCTKM.setString(1, this.currentMaHD);
						psCTKM.setString(2, maVeReal);
						psCTKM.setString(3, km.getMaKMDetail());
						psCTKM.setLong(4, Math.round(ctKM.getTienGiamCuaKM()));
						psCTKM.executeUpdate();
					}

					// UPDATE TRẠNG THÁI GHẾ
					psCho.setString(1, maLT);
					psCho.setString(2, maToa);
					psCho.setString(3, viTri);
					psCho.executeUpdate();
				}
			}

			// COMMIT TOÀN BỘ DỮ LIỆU
			conn.commit();

			String printNote = "";
			if (phuongThuc == PhuongThucThanhToan.TIEN_MAT) {
				String printPath = inHoaDonTienMat();
				if (printPath != null && !printPath.isEmpty()) {
					printNote = "\nĐã lưu hóa đơn: " + printPath;
				}
			}

			JOptionPane.showMessageDialog(this,
					"Thanh toán thành công! Mã Hóa Đơn: " + this.currentMaHD + printNote);

			// Chuyển step sau khi dialog đóng để tránh cảm giác giật UI.
			SwingUtilities.invokeLater(() -> {
				mainTab.getSelectedSeatsData().clear();
				mainTab.nextStep();
			});

		} catch (Exception ex) {
			try {
				conn.rollback();
			} catch (Exception rbe) {
				rbe.printStackTrace();
			}
			ex.printStackTrace();
			String extra = "";
			if (ex instanceof SQLException sqlEx) {
				extra = "\nSQLState=" + sqlEx.getSQLState() + " | Code=" + sqlEx.getErrorCode();
			}
			JOptionPane.showMessageDialog(this,
					"Lỗi thanh toán! Đã hoàn tác dữ liệu.\n" + getRootMessage(ex) + extra,
					"Lỗi SQL", JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (Exception ignored) {
			}
			isProcessingPayment = false;
		}
	}

	private String validatePaymentData() {
		if (isBlank(this.currentMaHD)) {
			return "Chưa tạo được mã hóa đơn. Vui lòng quay lại bước trước và thử lại.";
		}
		if (nv == null || isBlank(nv.getMaNV())) {
			return "Thiếu thông tin nhân viên đăng nhập. Vui lòng đăng nhập lại.";
		}
		if (!existsById("SELECT 1 FROM NhanVien WHERE maNV = ?", nv.getMaNV())) {
			return "Mã nhân viên " + nv.getMaNV() + " không tồn tại trong CSDL.";
		}
		if (!isBlank(kh != null ? kh.getMaKH() : null)
				&& !existsById("SELECT 1 FROM KhachHang WHERE maKH = ?", kh.getMaKH())) {
			return "Khách hàng hóa đơn (" + kh.getMaKH() + ") không tồn tại trong CSDL.";
		}

		for (int i = 0; i < dsVe.size(); i++) {
			VeEntry entry = dsVe.get(i);
			if (entry == null || entry.ve == null) {
				return "Dữ liệu vé thứ " + (i + 1) + " bị rỗng.";
			}
			Ve ve = entry.ve;
			if (isBlank(ve.getMaVe())) {
				return "Vé thứ " + (i + 1) + " chưa có mã vé.";
			}
			if (ve.getMaVe().length() > 20) {
				return "Mã vé " + ve.getMaVe() + " vượt quá 20 ký tự.";
			}
			if (ve.getLichTrinh() == null || isBlank(ve.getLichTrinh().getMaLT())) {
				return "Vé " + ve.getMaVe() + " chưa có mã lịch trình (maLT).";
			}
			if (!existsById("SELECT 1 FROM LichTrinh WHERE maLT = ?", ve.getLichTrinh().getMaLT())) {
				return "Lịch trình " + ve.getLichTrinh().getMaLT() + " của vé " + ve.getMaVe() + " không tồn tại.";
			}
			if (ve.getToa() == null || isBlank(ve.getToa().getMaToa())) {
				return "Vé " + ve.getMaVe() + " chưa có mã toa (maToa).";
			}
			if (!existsById("SELECT 1 FROM Toa WHERE maToa = ?", ve.getToa().getMaToa())) {
				return "Toa " + ve.getToa().getMaToa() + " của vé " + ve.getMaVe() + " không tồn tại.";
			}
			if (isBlank(ve.getViTriGhe())) {
				return "Vé " + ve.getMaVe() + " chưa có vị trí ghế.";
			}
			if (ve.getLoaiVe() == null || isBlank(ve.getLoaiVe().getMaLoai())) {
				return "Vé " + ve.getMaVe() + " chưa có loại vé (maLoaiVe).";
			}
			if (!existsById("SELECT 1 FROM LoaiVe WHERE maLoai = ?", ve.getLoaiVe().getMaLoai())) {
				return "Loại vé " + ve.getLoaiVe().getMaLoai() + " của vé " + ve.getMaVe() + " không tồn tại.";
			}
			String maKHVe = ve.getKhachHang() != null ? ve.getKhachHang().getMaKH() : null;
			if (!isBlank(maKHVe) && !existsById("SELECT 1 FROM KhachHang WHERE maKH = ?", maKHVe)) {
				return "Khách hàng " + maKHVe + " của vé " + ve.getMaVe() + " không tồn tại.";
			}
		}
		return null;
	}

	private boolean existsById(String sql, String value) {
		if (isBlank(value)) {
			return false;
		}
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, value);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	private String getRootMessage(Throwable t) {
		Throwable root = t;
		while (root.getCause() != null) {
			root = root.getCause();
		}
		return root.getMessage() != null ? root.getMessage() : t.getMessage();
	}

	private String createMaHD() {
		return "HD" + new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());
	}

	private String createMaVe(int counter) {
		String stamp = (currentMaHD != null && currentMaHD.length() > 2) ? currentMaHD.substring(2)
				: new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());
		return "V" + stamp + String.format("%03d", counter);
	}

	// =========================================================================
	// PHƯƠNG THỨC THANH TOÁN
	// =========================================================================
	private JPanel buildPhuongThucPanel() {
		JPanel pnl = new JPanel(new BorderLayout(0, 8));
		pnl.setOpaque(false);
		pnl.setBorder(BorderFactory.createCompoundBorder(new LineBorder(UIHelper.BORDER, 1, true),
				BorderFactory.createEmptyBorder(12, 14, 12, 14)));

		JLabel lbl = new JLabel("PHƯƠNG THỨC THANH TOÁN");
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lbl.setForeground(UIHelper.TEXT_MID);
		pnl.add(lbl, BorderLayout.NORTH);

		JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
		btnRow.setOpaque(false);

		btnTienMat = makePaymentBtn(PhuongThucThanhToan.TIEN_MAT, true);
		btnChuyenKhoan = makePaymentBtn(PhuongThucThanhToan.CHUYEN_KHOAN, false);

		ButtonGroup bg = new ButtonGroup();
		bg.add(btnTienMat);
		bg.add(btnChuyenKhoan);

		btnTienMat.addActionListener(e -> phuongThuc = PhuongThucThanhToan.TIEN_MAT);
		btnChuyenKhoan.addActionListener(e -> phuongThuc = PhuongThucThanhToan.CHUYEN_KHOAN);

		btnRow.add(btnTienMat);
		btnRow.add(btnChuyenKhoan);
		pnl.add(btnRow, BorderLayout.SOUTH);
		return pnl;
	}

	private JToggleButton makePaymentBtn(PhuongThucThanhToan pt, boolean selected) {
		JToggleButton btn = new JToggleButton(pt.label, selected) {
			private static final Color CLR_SEL_BG = new Color(0xEEFCF6);
			private static final Color CLR_SEL_BD = new Color(0x00A676);
			private static final Color CLR_IDLE_BG = Color.WHITE;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int arc = 10;
				if (isSelected()) {
					g2.setColor(CLR_SEL_BG);
					g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
					g2.setColor(CLR_SEL_BD);
					g2.setStroke(new BasicStroke(2));
					g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
				} else {
					g2.setColor(getModel().isRollover() ? new Color(0xF5F5F5) : CLR_IDLE_BG);
					g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
					g2.setColor(UIHelper.BORDER);
					g2.setStroke(new BasicStroke(1));
					g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
				}
				g2.dispose();
				super.paintComponent(g);
			}
		};
		btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		btn.setForeground(selected ? new Color(0x00A676) : UIHelper.TEXT_MID);
		btn.setHorizontalAlignment(SwingConstants.CENTER);
		btn.setPreferredSize(new Dimension(0, 44));
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.addItemListener(e -> btn.setForeground(btn.isSelected() ? new Color(0x00A676) : UIHelper.TEXT_MID));
		return btn;
	}

	// =========================================================================
	// UI HELPERS
	// =========================================================================
	private JLabel makeInfoLbl(String text, boolean isBold) {
		JLabel l = new JLabel(text);
		l.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 14));
		l.setForeground(isBold ? UIHelper.TEXT_MID : UIHelper.TEXT_DARK);
		return l;
	}

	private JLabel makeVeInfoLbl(String text, boolean isBold) {
		JLabel l = new JLabel(text);
		l.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 12));
		l.setForeground(isBold ? UIHelper.TEXT_MID : UIHelper.TEXT_DARK);
		return l;
	}

	private JPanel makeSumRow(String label, JLabel valueLabel, boolean isBold) {
		JPanel row = new JPanel(new BorderLayout());
		row.setOpaque(false);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
		JLabel lbl = new JLabel(label);
		lbl.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 14));
		lbl.setForeground(isBold ? UIHelper.TEXT_DARK : UIHelper.TEXT_MID);
		row.add(lbl, BorderLayout.WEST);
		row.add(valueLabel, BorderLayout.EAST);
		return row;
	}

	private JLabel makeSumLbl(String text, boolean isBold) {
		JLabel l = new JLabel(text);
		l.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 14));
		l.setForeground(UIHelper.TEXT_DARK);
		l.setHorizontalAlignment(SwingConstants.RIGHT);
		return l;
	}

	private static class WrapLayout extends FlowLayout {
		WrapLayout(int align, int hgap, int vgap) {
			super(align, hgap, vgap);
		}

		@Override
		public Dimension preferredLayoutSize(Container target) {
			return layoutSize(target, true);
		}

		@Override
		public Dimension minimumLayoutSize(Container target) {
			return layoutSize(target, false);
		}

		private Dimension layoutSize(Container target, boolean preferred) {
			synchronized (target.getTreeLock()) {
				int targetWidth = target.getSize().width;
				if (targetWidth == 0)
					targetWidth = Integer.MAX_VALUE;
				int hgap = getHgap(), vgap = getVgap();
				Insets insets = target.getInsets();
				int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
				int x = 0, y = insets.top + vgap, rowH = 0;
				for (Component c : target.getComponents()) {
					if (!c.isVisible())
						continue;
					Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
					if (x + d.width > maxWidth) {
						y += rowH + vgap;
						x = 0;
						rowH = 0;
					}
					x += d.width + hgap;
					rowH = Math.max(rowH, d.height);
				}
				return new Dimension(targetWidth, y + rowH + insets.bottom + vgap);
			}
		}
	}

	private JPanel makeKMChip(ChiTiet_KhuyenMai ctKM, Runnable onRemove) {
		KhuyenMaiDetail k = ctKM.getKhuyenMaiDetail();
		JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		chip.setOpaque(true);
		chip.setBackground(new Color(0xDCFCE7));
		chip.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0x86EFAC), 1, true),
				BorderFactory.createEmptyBorder(2, 6, 2, 4)));

		String label = formatKhuyenMaiLine(k);
		JLabel lbl = new JLabel(label);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
		lbl.setForeground(new Color(0x166534));

		JButton btnX = new JButton("✕");
		btnX.setFont(new Font("Segoe UI", Font.PLAIN, 10));
		btnX.setForeground(new Color(0x166534));
		btnX.setPreferredSize(new Dimension(16, 16));
		btnX.setContentAreaFilled(false);
		btnX.setBorderPainted(false);
		btnX.setFocusPainted(false);
		btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnX.addActionListener(e -> onRemove.run());

		chip.add(lbl);
		chip.add(btnX);
		return chip;
	}

	private JButton makeKMDropdownBtn(List<KhuyenMaiDetail> dsKMD, VeEntry entry, Runnable onClose) {
		JButton btn = new JButton() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(0xF8FAFD));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(UIHelper.BORDER);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		btn.setForeground(UIHelper.TEXT_MID);
		btn.setText("+ Chọn khuyến mãi ▾");
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.setPreferredSize(new Dimension(0, 32));

		btn.addActionListener(e -> {
			if (dsKMD.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Không có KM khả dụng cho vé này!");
				return;
			}

			JPopupMenu popup = new JPopupMenu();
			popup.setLayout(new BorderLayout());
			popup.setBorder(BorderFactory.createCompoundBorder(new LineBorder(UIHelper.BORDER, 1, true),
					BorderFactory.createEmptyBorder(6, 6, 6, 6)));

			JPanel listPanel = new JPanel();
			listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
			listPanel.setOpaque(false);

			for (KhuyenMaiDetail k : dsKMD) {
				boolean alreadyChosen = entry.containsKhuyenMai(k.getMaKMDetail());
				JCheckBox cb = new JCheckBox(formatKhuyenMaiLine(k), alreadyChosen);
				cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
				cb.setOpaque(false);
				cb.setFocusPainted(false);
				cb.addActionListener(ae -> {
					if (cb.isSelected())
						entry.addKhuyenMai(k);
					else
						entry.removeKhuyenMai(k.getMaKMDetail());
					onClose.run();
				});
				listPanel.add(cb);
			}

			JScrollPane scrollPane = new JScrollPane(listPanel);
			scrollPane.setPreferredSize(new Dimension(360, Math.min(220, Math.max(90, dsKMD.size() * 34))));
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			popup.add(scrollPane, BorderLayout.CENTER);

			JButton btnDone = new JButton("Xong");
			btnDone.setFont(new Font("Segoe UI", Font.BOLD, 12));
			btnDone.addActionListener(ae -> popup.setVisible(false));

			JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
			footer.setOpaque(false);
			footer.add(btnDone);
			popup.add(footer, BorderLayout.SOUTH);

			popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent ev) {
					onClose.run();
				}

				@Override
				public void popupMenuCanceled(javax.swing.event.PopupMenuEvent ev) {
					onClose.run();
				}

				@Override
				public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent ev) {
				}
			});

			popup.show(btn, 0, btn.getHeight());
		});

		return btn;
	}

	private String formatKhuyenMaiLine(KhuyenMaiDetail k) {
		if (k == null)
			return "";
		String tenKM = k.getKhuyenMai() != null ? k.getKhuyenMai().getTenKM() : k.getMaKMDetail();
		String loai = k.getLoaiKM() != null ? k.getLoaiKM().getLabel() : "KM";
		return tenKM + " | " + loai + " | " + formatGiaTri(k);
	}

	private String formatGiaTri(KhuyenMaiDetail k) {
		if (k == null)
			return "";
		if (k.getLoaiKM() == LoaiKhuyenMai.GIAM_PHAN_TRAM)
			return String.format("(%.0f%%)", k.getGiaTri());
		if (k.getLoaiKM() == LoaiKhuyenMai.GIAM_TIEN)
			return String.format("(-%s đ)", FMT.format((long) k.getGiaTri()));
		return "";
	}

	private String formatTien(long amount) {
		return FMT.format(amount) + " đ";
	}

	public String getCurrentMaHD() {
		return currentMaHD;
	}

	public List<String> getCurrentMaVeList() {
		List<String> list = new ArrayList<>();
		for (VeEntry entry : dsVe) {
			if (entry != null && entry.ve != null && entry.ve.getMaVe() != null) {
				list.add(entry.ve.getMaVe());
			}
		}
		return list;
	}

	private File getHoaDonOutputDir() {
		File dir = new File(System.getProperty("user.dir"), "src/main/resources/HoaDon");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	private String inHoaDonTienMat() {
		File outputDir = getHoaDonOutputDir();
		File outFile = new File(outputDir, "HoaDon_" + this.currentMaHD + ".pdf");
		String path = outFile.getAbsolutePath();

		try {
			Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
			PdfWriter.getInstance(doc, new FileOutputStream(path));
			doc.open();

			final java.text.DecimalFormat dfPdf = new java.text.DecimalFormat("#,##0");
			dfPdf.setDecimalFormatSymbols(new java.text.DecimalFormatSymbols(new java.util.Locale("vi", "VN")));

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

			String ngayLap = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
			String tenNV = nv != null ? nv.getTenNV() : "-";
			String tenKH = kh != null ? kh.getHoTen() : "Khách lẻ";

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
			cR.addElement(new Paragraph("Số: " + this.currentMaHD, fN));
			tT.addCell(cR);
			doc.add(tT);
			doc.add(new LineSeparator(1f, 100f, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2));
			doc.add(new Paragraph(" ", fS));

			addInfoLine(doc, "Đơn vị bán hàng:", "CÔNG TY CỔ PHẦN VẬN TẢI ĐƯỜNG SẮT VIỆT NAM", fIL, fN);
			addInfoLine(doc, "MST:", "0100106264", fIL, fN);
			addInfoLine(doc, "Nhân viên lập:", tenNV, fIL, fN);
			doc.add(new Paragraph(" ", fS));
			addInfoLine(doc, "Họ tên người mua:", tenKH, fIL, fN);
			addInfoLine(doc, "Hình thức thanh toán:", " "
					+ (phuongThuc == PhuongThucThanhToan.TIEN_MAT ? "Tiền mặt" : "Chuyển khoản"), fIL, fN);
			doc.add(new Paragraph(" ", fS));

			float[] colWidths = { 28f, 95f, 115f, 35f, 25f, 85f, 90f, 85f };
			PdfPTable tI = new PdfPTable(colWidths);
			tI.setWidthPercentage(100);
			tI.setSpacingBefore(4);
			BaseColor hBg = new BaseColor(0x1A, 0x5E, 0xAB);
			for (String h : new String[] { "STT", "Mã vé", "Tên DV", "ĐVT", "SL", "Đơn giá", "Khuyến mãi", "Tổng" }) {
				PdfPCell hc = new PdfPCell(new Phrase(h, fH));
				hc.setBackgroundColor(hBg);
				hc.setHorizontalAlignment(Element.ALIGN_CENTER);
				hc.setVerticalAlignment(Element.ALIGN_MIDDLE);
				hc.setNoWrap(true);
				hc.setPadding(5);
				tI.addCell(hc);
			}

			double tongGoc = 0;
			double tongGiam = 0;
			double tongThanhTien = 0;
			int stt = 1;

			for (VeEntry entry : dsVe) {
				Ve ve = entry.ve;
				long tienGoc = entry.tienGoc();
				long tienGiam = entry.tienGiam();
				long thanhTien = entry.thanhTien();

				tongGoc += tienGoc;
				tongGiam += tienGiam;
				tongThanhTien += thanhTien;

				StringBuilder sbKM = new StringBuilder();
				for (ChiTiet_KhuyenMai ct : entry.getChiTietKMSortedAndCalculated()) {
					KhuyenMaiDetail km = ct.getKhuyenMaiDetail();
					if (km == null)
						continue;
					if (sbKM.length() > 0)
						sbKM.append("; ");
					sbKM.append(formatKhuyenMaiPdf(km));
				}

				String loaiVe = ve.getLoaiVe() != null ? ve.getLoaiVe().getTenLoai() : "-";

				addPdfCell(tI, String.valueOf(stt++), fC, Element.ALIGN_CENTER);
				addPdfCell(tI, ve.getMaVe(), fC, Element.ALIGN_LEFT);
				addPdfCell(tI, "Vé: " + loaiVe, fC, Element.ALIGN_LEFT);
				addPdfCell(tI, "Vé", fC, Element.ALIGN_CENTER);
				addPdfCell(tI, "1", fC, Element.ALIGN_CENTER);
				addPdfCellNoWrap(tI, dfPdf.format((long) tienGoc) + " VNĐ", fC, Element.ALIGN_RIGHT);

				if (tienGiam > 0) {
					PdfPCell cKM = new PdfPCell();
					cKM.setPadding(4);
					com.itextpdf.text.Font fKMName = new com.itextpdf.text.Font(bf, 7f, com.itextpdf.text.Font.NORMAL,
							BaseColor.DARK_GRAY);
					com.itextpdf.text.Font fKMVal = new com.itextpdf.text.Font(bf, 8f, com.itextpdf.text.Font.BOLD,
							new BaseColor(0x16, 0x63, 0x34));
					if (sbKM.length() > 0) {
						Paragraph pName = new Paragraph(sbKM.toString(), fKMName);
						pName.setAlignment(Element.ALIGN_LEFT);
						cKM.addElement(pName);
					}
					Paragraph pGiam = new Paragraph("-" + dfPdf.format((long) tienGiam) + " VNĐ", fKMVal);
					pGiam.setAlignment(Element.ALIGN_RIGHT);
					cKM.addElement(pGiam);
					tI.addCell(cKM);
				} else {
					addPdfCell(tI, "-", fC, Element.ALIGN_CENTER);
				}

				addPdfCellNoWrap(tI, dfPdf.format((long) thanhTien) + " VNĐ", fC, Element.ALIGN_RIGHT);
			}

			doc.add(tI);

			BaseColor sumBg = new BaseColor(0xF0, 0xF6, 0xFF);
			com.itextpdf.text.Font fRed = new com.itextpdf.text.Font(bf, 9, com.itextpdf.text.Font.BOLD,
					new BaseColor(0xDC, 0x26, 0x26));
			com.itextpdf.text.Font fGreen = new com.itextpdf.text.Font(bf, 9, com.itextpdf.text.Font.BOLD,
					new BaseColor(0x16, 0x63, 0x34));

			PdfPTable tS = new PdfPTable(colWidths);
			tS.setWidthPercentage(100);
			PdfPCell cTL = new PdfPCell(new Phrase("Tổng cộng:", fB));
			cTL.setColspan(5);
			cTL.setBorder(PdfPCell.BOX);
			cTL.setBackgroundColor(sumBg);
			cTL.setPadding(5);
			tS.addCell(cTL);
			addSumCell(tS, dfPdf.format((long) tongGoc) + " VNĐ", fB, sumBg);
			addSumCell(tS, tongGiam > 0 ? "-" + dfPdf.format((long) tongGiam) + " VNĐ" : "-", fGreen, sumBg);
			addSumCell(tS, dfPdf.format((long) tongThanhTien) + " VNĐ", fRed, sumBg);
			doc.add(tS);

			doc.add(new Paragraph(" ", fS));
			doc.add(new Paragraph("Số tiền bằng chữ: " + docSoTienBangChu((long) tongThanhTien), fB));
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
			doc.add(new Paragraph("Mã tra cứu: " + this.currentMaHD, fS));
			doc.close();

			return path;
		} catch (Exception e) {
			return null;
		}
	}

	private void addInfoLine(Document doc, String lbl, String val, com.itextpdf.text.Font fL, com.itextpdf.text.Font fV)
			throws DocumentException {
		Paragraph p = new Paragraph();
		p.add(new Chunk(lbl + " ", fL));
		p.add(new Chunk(val, fV));
		p.setSpacingBefore(2);
		doc.add(p);
	}

	private void addPdfCell(PdfPTable t, String text, com.itextpdf.text.Font f, int align) {
		PdfPCell c = new PdfPCell(new Phrase(text, f));
		c.setHorizontalAlignment(align);
		c.setPadding(4);
		t.addCell(c);
	}

	private void addPdfCellNoWrap(PdfPTable t, String txt, com.itextpdf.text.Font f, int align) {
		PdfPCell c = new PdfPCell(new Phrase(txt, f));
		c.setHorizontalAlignment(align);
		c.setPadding(4);
		c.setNoWrap(true);
		t.addCell(c);
	}

	private void addSumCell(PdfPTable t, String txt, com.itextpdf.text.Font f, BaseColor bg) {
		PdfPCell c = new PdfPCell(new Phrase(txt, f));
		c.setHorizontalAlignment(Element.ALIGN_RIGHT);
		c.setPadding(5);
		c.setNoWrap(true);
		c.setBackgroundColor(bg);
		t.addCell(c);
	}

	private String formatKhuyenMaiPdf(KhuyenMaiDetail km) {
		String tenKM = km.getKhuyenMai() != null ? km.getKhuyenMai().getTenKM() : km.getMaKMDetail();
		if (km.getLoaiKM() == LoaiKhuyenMai.GIAM_PHAN_TRAM) {
			return tenKM + " (" + String.format("%.0f%%", km.getGiaTri()) + ")";
		}
		return tenKM;
	}

	private String docSoTienBangChu(long so) {
		if (so == 0)
			return "không đồng";
		if (so < 0)
			return "âm " + docSoTienBangChu(-so);

		String[] donvi = { "", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín" };
		String[] hanguc = { "", "nghìn", "triệu", "tỷ" };

		java.util.List<Integer> nhom = new java.util.ArrayList<>();
		long tmp = so;
		while (tmp > 0) {
			nhom.add((int) (tmp % 1000));
			tmp /= 1000;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = nhom.size() - 1; i >= 0; i--) {
			int n = nhom.get(i);
			if (n == 0)
				continue;
			String chu = docNhom(n, donvi);
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(chu);
			if (!hanguc[i].isEmpty())
				sb.append(" ").append(hanguc[i]);
		}

		String result = sb.toString().trim();
		if (!result.isEmpty()) {
			result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
		}
		return result + " đồng";
	}

	private String docNhom(int n, String[] donvi) {
		int tram = n / 100;
		int chuc = (n % 100) / 10;
		int dvi = n % 10;
		StringBuilder sb = new StringBuilder();
		if (tram > 0) {
			sb.append(donvi[tram]).append(" trăm");
			if (chuc == 0 && dvi > 0)
				sb.append(" linh");
		}
		if (chuc > 1) {
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(donvi[chuc]).append(" mươi");
			if (dvi == 1)
				sb.append(" mốt");
			else if (dvi == 5)
				sb.append(" lăm");
			else if (dvi > 0)
				sb.append(" ").append(donvi[dvi]);
		} else if (chuc == 1) {
			if (sb.length() > 0)
				sb.append(" ");
			sb.append("mười");
			if (dvi == 5)
				sb.append(" lăm");
			else if (dvi > 0)
				sb.append(" ").append(donvi[dvi]);
		} else if (dvi > 0) {
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(donvi[dvi]);
		}
		return sb.toString();
	}
}
