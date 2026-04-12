package com.gui.banve;

import com.connectDB.ConnectDB;
import com.dao.DAO_KhuyenMaiDetail;
import com.entities.*;
import com.enums.LoaiKhuyenMai;
import com.enums.TrangThaiVe;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Step4_ThanhToan extends JPanel {

    // ── Enum phương thức thanh toán ──────────────────────────────────────────
    public enum PhuongThucThanhToan {
        TIEN_MAT("💵  Tiền mặt"),
        CHUYEN_KHOAN("🏦  Chuyển khoản");

        public final String label;
        PhuongThucThanhToan(String label) { this.label = label; }
    }

    private final TAB_BanVe mainTab;
    private final Connection conn;
    private final DAO_KhuyenMaiDetail daoKMD;

    // ── Dữ liệu ──────────────────────────────────────────────────────────────
    private NhanVien nv;
    private KhachHang kh;
    private KhachHang kh2;

    Tuyen tuyen;
    /**
     * VeEntry = 1 vé + KMDetail đang được chọn cho vé đó.
     * Dùng thay cho CTietHoaDon trong quá trình soạn hóa đơn.
     */
    private static class VeEntry {
        Ve ve;
        KhuyenMaiDetail kmChon; // null = không áp dụng KM

        // Tính toán từ ve.giaVe + kmChon
        long tienGoc()    { return (long) ve.getGiaVe(); }
        long tienGiam() {
            if (kmChon == null) return 0;
            if (kmChon.getLoaiKM() == LoaiKhuyenMai.GIAM_PHAN_TRAM)
                return Math.round(tienGoc() * kmChon.getGiaTri() / 100.0);
            if (kmChon.getLoaiKM() == LoaiKhuyenMai.GIAM_TIEN)
                return Math.min(tienGoc(), (long) kmChon.getGiaTri());
            return 0;
        }
        long thanhTien()  { return tienGoc() - tienGiam(); }
    }

    private final List<VeEntry> dsVe = new ArrayList<>(); // danh sách vé trong phiên này

    // ── UI refs để refresh ───────────────────────────────────────────────────
    private JPanel pnlReceiptList;  // danh sách receipt bên TRÁI
    private JPanel pnlVeList;       // danh sách vé + combobox KM bên PHẢI
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
        this.conn    = ConnectDB.getConnection();
        this.daoKMD  = new DAO_KhuyenMaiDetail(conn);
        mockData();
        initUI();
        refreshAll();
    }

    // =========================================================================
    // MOCK DATA – 2 vé Sài Gòn – Hà Nội 01/04/2026
    // =========================================================================
    private void mockData() {
        // NV + KH
        nv = new NhanVien(); nv.setMaNV("NV0001"); nv.setTenNV("Nguyễn Văn A");
        kh = new KhachHang(); kh.setMaKH("KH01");  kh.setHoTen("Trần Văn An");
        kh2 = new KhachHang(); kh2.setMaKH("KH02");  kh2.setHoTen("Trần Văn Bê");

        // Tuyến, Tàu, Toa, Loại vé, Lịch trình
        this.tuyen = new Tuyen("T01", "Sài Gòn – Hà Nội");

        LoaiToa loaiToaCung = new LoaiToa(); loaiToaCung.setMaLoaiToa("G_CUNG"); loaiToaCung.setTenLoaiToa("Ghế cứng");
        LoaiToa loaiToaMem  = new LoaiToa(); loaiToaMem.setMaLoaiToa("G_MEM");   loaiToaMem.setTenLoaiToa("Ghế mềm");

        LoaiVe loaiVeNL = new LoaiVe(); loaiVeNL.setMaLoai("LV01"); loaiVeNL.setTenLoai("Người lớn");

        Toa toa1 = new Toa(); toa1.setMaToa("SE01_T01"); toa1.setTenToa("Toa 1"); toa1.setLoaiToa(loaiToaCung);
        Toa toa5 = new Toa(); toa5.setMaToa("SE01_T05"); toa5.setTenToa("Toa 5"); toa5.setLoaiToa(loaiToaMem);

        ChoNgoi cho1 = new ChoNgoi(); cho1.setMaCho("SE01_T01_S09"); cho1.setTenCho("9"); cho1.setToa(toa1);
        ChoNgoi cho2 = new ChoNgoi(); cho2.setMaCho("SE01_T05_S13"); cho2.setTenCho("13"); cho2.setToa(toa5);

        LichTrinh lt = new LichTrinh(); lt.setMaLT("LT01");
        try {
            lt.setNgayKhoiHanh(LocalDate.of(2026, 4, 1));
            lt.setGioKhoiHanh(LocalTime.of(8, 0));   // 08:00
        }
        catch (Exception ignored) {}

        // Vé 1 – Ghế cứng – 500,000đ
        Ve ve1 = new Ve();
        ve1.setMaVe("V001"); ve1.setKhachHang(kh); ve1.setChoNgoi(cho1);
        ve1.setLichTrinh(lt); ve1.setLoaiVe(loaiVeNL); ve1.setGiaVe(500_000);
        ve1.setTrangThaiVe(TrangThaiVe.CHUA_SU_DUNG);

        // Vé 2 – Ghế mềm – 800,000đ
        Ve ve2 = new Ve();
        ve2.setMaVe("V002"); ve2.setKhachHang(kh2); ve2.setChoNgoi(cho2);
        ve2.setLichTrinh(lt); ve2.setLoaiVe(loaiVeNL); ve2.setGiaVe(800_000);
        ve2.setTrangThaiVe(TrangThaiVe.CHUA_SU_DUNG);

        VeEntry e1 = new VeEntry(); e1.ve = ve1;
        VeEntry e2 = new VeEntry(); e2.ve = ve2;
        dsVe.add(e1); dsVe.add(e2);
    }

    // =========================================================================
    // INIT UI
    // =========================================================================
    private void initUI() {
        setLayout(new BorderLayout(0, 16)); setOpaque(false);
        add(UIHelper.createPageTitle(
                        "THANH TOÁN & HOÀN TẤT",
                        "Kiểm tra lại thông tin hóa đơn, chỉnh sửa khuyến mãi và xác nhận"),
                BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout()); body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH; gc.weighty = 1.0;

        gc.gridx = 0; gc.weightx = 0.55; gc.insets = new Insets(0, 0, 0, 10);
        body.add(buildHoaDonPanel(), gc);

        gc.gridx = 1; gc.weightx = 0.45; gc.insets = new Insets(0, 10, 0, 0);
        body.add(buildVePanel(), gc);

        add(body, BorderLayout.CENTER);
    }

    // =========================================================================
    // PANEL TRÁI – Hóa đơn tổng hợp (read-only)
    // =========================================================================
    private JPanel buildHoaDonPanel() {
        JPanel card = UIHelper.makeCard(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                new UIHelper.ShadowBorder(),
                BorderFactory.createEmptyBorder(24, 24, 20, 24)));

        // Tiêu đề + thông tin HoaDon
        JLabel title = new JLabel("HÓA ĐƠN");
        title.setFont(UIHelper.F_H2); title.setForeground(UIHelper.ACCENT);

        JPanel pnlMeta = new JPanel(new GridLayout(3, 2, 6, 5)); pnlMeta.setOpaque(false);
        pnlMeta.add(makeInfoLbl("Ngày lập:",     true));
        pnlMeta.add(makeInfoLbl(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), false));
        pnlMeta.add(makeInfoLbl("Nhân viên:",    true));
        pnlMeta.add(makeInfoLbl(nv != null ? nv.getTenNV() + " (" + nv.getMaNV() + ")" : "—", false));
        pnlMeta.add(makeInfoLbl("Khách hàng:",   true));
        pnlMeta.add(makeInfoLbl(kh != null ? kh.getHoTen() + " (" + kh.getMaKH() + ")" : "—", false));

        JPanel north = new JPanel(new BorderLayout(0, 8)); north.setOpaque(false);
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
        lblTienGiam  = makeSumLbl("- 0 đ", false);
        lblTongTien  = makeSumLbl("0 đ", true);
        lblTongTien.setForeground(UIHelper.DANGER);
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JPanel pnlSum = new JPanel(); pnlSum.setLayout(new BoxLayout(pnlSum, BoxLayout.Y_AXIS)); pnlSum.setOpaque(false);
        pnlSum.add(Box.createVerticalStrut(8)); pnlSum.add(new JSeparator()); pnlSum.add(Box.createVerticalStrut(8));
        pnlSum.add(makeSumRow("Thành tiền:",      lblThanhTien, false));
        pnlSum.add(Box.createVerticalStrut(5));
        pnlSum.add(makeSumRow("Giảm giá (KM):",   lblTienGiam,  false));
        pnlSum.add(Box.createVerticalStrut(8));
        pnlSum.add(new JSeparator()); pnlSum.add(Box.createVerticalStrut(8));
        pnlSum.add(makeSumRow("TỔNG THANH TOÁN:", lblTongTien,  true));
        card.add(pnlSum, BorderLayout.SOUTH);

        return card;
    }

    // =========================================================================
    // PANEL PHẢI – Danh sách vé + ComboBox KM từng vé, nút xóa
    // =========================================================================
    private JPanel buildVePanel() {
        JPanel card = UIHelper.makeCard(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                new UIHelper.ShadowBorder(),
                BorderFactory.createEmptyBorder(24, 24, 20, 24)));

        // Tiêu đề
        JPanel northBar = new JPanel(new BorderLayout()); northBar.setOpaque(false);
        JLabel title = new JLabel("CHI TIẾT VÉ");
        title.setFont(UIHelper.F_H2); title.setForeground(UIHelper.ACCENT);
        JLabel hint = new JLabel("Chọn khuyến mãi hoặc xóa từng vé");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12)); hint.setForeground(UIHelper.TEXT_LIGHT);
        northBar.add(title, BorderLayout.NORTH);
        northBar.add(hint,  BorderLayout.SOUTH);
        card.add(northBar, BorderLayout.NORTH);

        // Danh sách vé có thể scroll
        pnlVeList = new JPanel();
        pnlVeList.setLayout(new BoxLayout(pnlVeList, BoxLayout.Y_AXIS));
        pnlVeList.setOpaque(false);

        JScrollPane scroll = new JScrollPane(pnlVeList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // ── Phương thức thanh toán (thay cho btn xác nhận) ──
        JPanel pnlPTTT = buildPhuongThucPanel();
        card.add(pnlPTTT, BorderLayout.SOUTH);

        return card;
    }

    // =========================================================================
    // REFRESH – build lại cả 2 panel từ dsVe
    // =========================================================================
    private void refreshAll() {
        refreshVeList();
        refreshReceipt();
    }

    /** Panel phải: mỗi vé là 1 card có thông tin + combobox KM + nút xóa */
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

        pnlVeList.revalidate(); pnlVeList.repaint();
    }

    /** 1 card vé bên phải */
    private JPanel buildVeCard(VeEntry entry, int idx) {
        Ve ve = entry.ve;

        JPanel card = new JPanel(new BorderLayout(0, 8)); card.setOpaque(true);
        card.setBackground(new Color(0xF7FAFF));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIHelper.BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        // ── HEADER: mã vé + nút xóa ──
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel lblMaVe = new JLabel(ve.getMaVe());
        lblMaVe.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblMaVe.setForeground(UIHelper.ACCENT);

        JButton btnXoa = new JButton("✕ Xóa vé");
        btnXoa.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnXoa.setForeground(UIHelper.DANGER); btnXoa.setBackground(new Color(0xFFF0F0));
        btnXoa.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xFFC8C8), 1, true),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        btnXoa.setFocusPainted(false); btnXoa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXoa.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xóa vé " + ve.getMaVe() + " khỏi hóa đơn?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dsVe.remove(idx);
                refreshAll();
            }
        });
        header.add(lblMaVe, BorderLayout.WEST); header.add(btnXoa, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // ── THÔNG TIN VÉ (5 hàng: KH, Ngày+Giờ, Tuyến, Toa+Ghế, Loại) ──
        JPanel info = new JPanel(new GridLayout(5, 2, 8, 3)); info.setOpaque(false);

        String tenKH = (ve.getKhachHang() != null) ? ve.getKhachHang().getHoTen() : "—";

        String ngayGio = "—";
        if (ve.getLichTrinh() != null) {
            String ngay = ve.getLichTrinh().getNgayKhoiHanh() != null
                    ? ve.getLichTrinh().getNgayKhoiHanh().toString() : "?";
            String gio  = ve.getLichTrinh().getGioKhoiHanh() != null
                    ? String.format("%02d:%02d", ve.getLichTrinh().getGioKhoiHanh().getHour(),
                    ve.getLichTrinh().getGioKhoiHanh().getMinute()) : "?";
            ngayGio = ngay + "  " + gio;
        }

        String tenTuyen = (tuyen != null) ? tuyen.getTenTuyen() : "—";
        String cho  = ve.getChoNgoi() != null
                ? ve.getChoNgoi().getToa().getTenToa() + " – Ghế " + ve.getChoNgoi().getTenCho() : "—";
        String loaiVeStr  = ve.getLoaiVe()  != null ? ve.getLoaiVe().getTenLoai()  : "—";
        String loaiToaStr = (ve.getChoNgoi() != null && ve.getChoNgoi().getToa().getLoaiToa() != null)
                ? ve.getChoNgoi().getToa().getLoaiToa().getTenLoaiToa() : "—";

        info.add(makeVeInfoLbl("Hành khách:", true));  info.add(makeVeInfoLbl(tenKH,     false));
        info.add(makeVeInfoLbl("Khởi hành:",  true));  info.add(makeVeInfoLbl(ngayGio,   false));
        info.add(makeVeInfoLbl("Tuyến:",      true));  info.add(makeVeInfoLbl(tenTuyen,  false));
        info.add(makeVeInfoLbl("Chỗ:",        true));  info.add(makeVeInfoLbl(cho,       false));
        info.add(makeVeInfoLbl("Loại:",       true));  info.add(makeVeInfoLbl(loaiVeStr + " – " + loaiToaStr, false));
        card.add(info, BorderLayout.CENTER);

        // ── COMBOBOX KM + GIÁ ──
        JPanel bottom = new JPanel(new BorderLayout(8, 0)); bottom.setOpaque(false);

        // Load KMD khả dụng cho vé này (theo ngày hôm nay; có thể filter thêm theo tuyến/loại toa)
        List<KhuyenMaiDetail> dsKMD = daoKMD.getKhuyenMaiDetailKhaDung(new Date(), ve.getLoaiVe(), ve.getChoNgoi().getToa().getLoaiToa(), tuyen);
        KhuyenMaiDetail[] kmArr = dsKMD.toArray(new KhuyenMaiDetail[0]);
        JComboBox<KhuyenMaiDetail> cbKM = makeKMComboBox(kmArr);

        // Nếu entry đang có KM đã chọn trước → set lại
        if (entry.kmChon != null) {
            for (int j = 1; j < cbKM.getItemCount(); j++) {
                KhuyenMaiDetail item = cbKM.getItemAt(j);
                if (item != null && item.getMaKMDetail().equals(entry.kmChon.getMaKMDetail())) {
                    cbKM.setSelectedIndex(j); break;
                }
            }
        }

        // Label giá (cập nhật khi đổi KM)
        JLabel lblGia = new JLabel(buildGiaHtml(entry));
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGia.setHorizontalAlignment(SwingConstants.RIGHT);

        cbKM.addActionListener(e -> {
            entry.kmChon = (KhuyenMaiDetail) cbKM.getSelectedItem();
            lblGia.setText(buildGiaHtml(entry));
            refreshReceipt(); // cập nhật bên trái
        });

        bottom.add(cbKM,    BorderLayout.CENTER);
        bottom.add(lblGia,  BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    /** HTML giá hiển thị trong card vé */
    private String buildGiaHtml(VeEntry e) {
        if (e.tienGiam() > 0) {
            return "<html><s style='color:gray;font-size:11px'>" + formatTien(e.tienGoc()) + "</s>"
                    + " <b style='color:#16A34A'>" + formatTien(e.thanhTien()) + "</b></html>";
        }
        return "<html><b>" + formatTien(e.thanhTien()) + "</b></html>";
    }

    /** Panel trái: cập nhật receipt và tổng tiền */
    private void refreshReceipt() {
        pnlReceiptList.removeAll();

        long tongGoc = 0, tongGiam = 0;

        for (VeEntry entry : dsVe) {
            pnlReceiptList.add(buildReceiptRow(entry));
            pnlReceiptList.add(Box.createVerticalStrut(8));
            tongGoc  += entry.tienGoc();
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
        if (tongGiam > 0) lblTienGiam.setForeground(UIHelper.SUCCESS);

        pnlReceiptList.revalidate(); pnlReceiptList.repaint();
    }

    /** 1 dòng trong receipt bên trái */
    private JPanel buildReceiptRow(VeEntry entry) {
        Ve ve = entry.ve;
        JPanel row = new JPanel(new BorderLayout(10, 4)); row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIHelper.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        // ── Trái: mã vé / KH / chi tiết / KM ──
        JPanel left = new JPanel(new GridLayout(5, 1, 0, 2)); left.setOpaque(false);

        // Dòng 1: mã vé
        JLabel lVe = new JLabel(ve.getMaVe());
        lVe.setFont(new Font("Segoe UI", Font.BOLD, 14)); lVe.setForeground(UIHelper.ACCENT);

        // Dòng 2: tên khách hàng
        String tenKH = ve.getKhachHang() != null ? ve.getKhachHang().getHoTen() + (ve.getKhachHang().getSdt() != null? " - " + ve.getKhachHang().getSdt() : "") : "—";
        JLabel lKH = new JLabel(tenKH);
        lKH.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lKH.setForeground(UIHelper.TEXT_DARK);

        // Dòng 3: toa + loại + ngày/giờ
        String tenTuyen = (tuyen != null) ? tuyen.getTenTuyen() : "—";
        JLabel lblTuyen = new JLabel(tenTuyen);
        lblTuyen.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lKH.setForeground(UIHelper.TEXT_DARK);

        String cho = ve.getChoNgoi() != null
                ? ve.getChoNgoi().getToa().getTenToa() + " – Ghế " + ve.getChoNgoi().getTenCho() : "—";
        String loaiToa = (ve.getChoNgoi() != null && ve.getChoNgoi().getToa().getLoaiToa() != null)
                ? ve.getChoNgoi().getToa().getLoaiToa().getTenLoaiToa() : "—";
        String ngayGio = "—";
        if (ve.getLichTrinh() != null) {
            String ngay = ve.getLichTrinh().getNgayKhoiHanh() != null
                    ? ve.getLichTrinh().getNgayKhoiHanh().toString() : "?";
            String gio  = ve.getLichTrinh().getGioKhoiHanh() != null
                    ? String.format("%02d:%02d", ve.getLichTrinh().getGioKhoiHanh().getHour(),
                    ve.getLichTrinh().getGioKhoiHanh().getMinute()) : "?";
            ngayGio = ngay + " " + gio;
        }
        JLabel lDetail = new JLabel(cho + "  |  " + loaiToa + "  |  " + ngayGio);
        lDetail.setFont(new Font("Segoe UI", Font.PLAIN, 13)); lDetail.setForeground(UIHelper.TEXT_MID);

        // Dòng 4: KM
        String kmText = entry.kmChon != null
                ? "KM: " + entry.kmChon.getKhuyenMai().getTenKM() + "  " + formatGiaTri(entry.kmChon)
                : "Không áp dụng KM";
        JLabel lKM = new JLabel(kmText);
        lKM.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lKM.setForeground(entry.kmChon != null ? UIHelper.SUCCESS : UIHelper.TEXT_LIGHT);

        left.add(lVe); left.add(lKH); left.add(lblTuyen); left.add(lDetail); left.add(lKM);

        // ── Phải: giá gốc + thành tiền ──
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 4)); right.setOpaque(false);

        JLabel lGoc = new JLabel(entry.tienGiam() > 0
                ? "<html><s>" + formatTien(entry.tienGoc()) + "</s></html>"
                : formatTien(entry.tienGoc()));
        lGoc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lGoc.setForeground(UIHelper.TEXT_LIGHT); lGoc.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lTT = new JLabel(formatTien(entry.thanhTien()));
        lTT.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lTT.setForeground(entry.tienGiam() > 0 ? UIHelper.SUCCESS : UIHelper.TEXT_DARK);
        lTT.setHorizontalAlignment(SwingConstants.RIGHT);

        right.add(lGoc); right.add(lTT);

        row.add(left,  BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    // =========================================================================
    // PHƯƠNG THỨC THANH TOÁN
    // =========================================================================
    private JPanel buildPhuongThucPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8)); pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIHelper.BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JLabel lbl = new JLabel("PHƯƠNG THỨC THANH TOÁN");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UIHelper.TEXT_MID);
        pnl.add(lbl, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0)); btnRow.setOpaque(false);

        btnTienMat    = makePaymentBtn(PhuongThucThanhToan.TIEN_MAT,    true);
        btnChuyenKhoan = makePaymentBtn(PhuongThucThanhToan.CHUYEN_KHOAN, false);

        ButtonGroup bg = new ButtonGroup(); bg.add(btnTienMat); bg.add(btnChuyenKhoan);

        btnTienMat.addActionListener(e -> phuongThuc = PhuongThucThanhToan.TIEN_MAT);
        btnChuyenKhoan.addActionListener(e -> phuongThuc = PhuongThucThanhToan.CHUYEN_KHOAN);

        btnRow.add(btnTienMat); btnRow.add(btnChuyenKhoan);
        pnl.add(btnRow, BorderLayout.SOUTH);
        return pnl;
    }

    private JToggleButton makePaymentBtn(PhuongThucThanhToan pt, boolean selected) {
        JToggleButton btn = new JToggleButton(pt.label, selected) {
            private static final Color CLR_SEL_BG  = new Color(0xEEFCF6);
            private static final Color CLR_SEL_BD  = new Color(0x00A676);
            private static final Color CLR_IDLE_BG = Color.WHITE;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 10;
                if (isSelected()) {
                    g2.setColor(CLR_SEL_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                    g2.setColor(CLR_SEL_BD);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc);
                } else {
                    g2.setColor(getModel().isRollover() ? new Color(0xF5F5F5) : CLR_IDLE_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                    g2.setColor(UIHelper.BORDER);
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(selected ? new Color(0x00A676) : UIHelper.TEXT_MID);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addItemListener(e -> btn.setForeground(btn.isSelected() ? new Color(0x00A676) : UIHelper.TEXT_MID));
        return btn;
    }

    // =========================================================================
    // UI HELPERS
    // =========================================================================

    /** ComboBox KM: null ở index 0 = không áp dụng */
    private JComboBox<KhuyenMaiDetail> makeKMComboBox(KhuyenMaiDetail[] items) {
        KhuyenMaiDetail[] all = new KhuyenMaiDetail[items.length + 1];
        all[0] = null;
        System.arraycopy(items, 0, all, 1, items.length);

        @SuppressWarnings("unchecked")
        JComboBox<KhuyenMaiDetail> cb = new JComboBox<>(all);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIHelper.BORDER, 1, true),
                BorderFactory.createEmptyBorder(0, 4, 0, 4)));

        cb.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(); lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            if (value == null) {
                lbl.setText("— Không áp dụng KM —");
                lbl.setForeground(UIHelper.TEXT_LIGHT);
            } else {
                KhuyenMaiDetail k = value;
                lbl.setText(k.getKhuyenMai().getTenKM() + " | " + k.getLoaiKM().getLabel()
                        + " " + formatGiaTri(k)
                        + (k.getLoaiToa() != null ? "  [" + k.getLoaiToa().getTenLoaiToa() + "]" : "")
                        + (k.getLoaiVe() != null ? "  [" + k.getLoaiVe().getTenLoai() + "]" : ""));
                lbl.setForeground(UIHelper.TEXT_DARK);
            }
            lbl.setBackground(isSelected ? new Color(0xDDEEFF) : Color.WHITE);
            return lbl;
        });
        return cb;
    }

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
        JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 14));
        lbl.setForeground(isBold ? UIHelper.TEXT_DARK : UIHelper.TEXT_MID);
        row.add(lbl, BorderLayout.WEST); row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JLabel makeSumLbl(String text, boolean isBold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 14));
        l.setForeground(UIHelper.TEXT_DARK); l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private String formatGiaTri(KhuyenMaiDetail k) {
        if (k == null) return "";
        if (k.getLoaiKM() == LoaiKhuyenMai.GIAM_PHAN_TRAM)
            return String.format("(%.0f%%)", k.getGiaTri());
        if (k.getLoaiKM() == LoaiKhuyenMai.GIAM_TIEN)
            return String.format("(-%s đ)", FMT.format((long) k.getGiaTri()));
        return "";
    }

    private String formatTien(long amount) { return FMT.format(amount) + " đ"; }
}