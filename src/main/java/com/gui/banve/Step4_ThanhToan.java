package com.gui.banve;

import com.connectDB.ConnectDB;
import com.dao.DAO_ChuyenTau;
import com.dao.DAO_Tuyen;
import com.dao.DAO_KhuyenMaiDetail;
import com.entities.*;
import com.enums.LoaiKhuyenMai;
import com.enums.TrangThaiVe;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    // Cache tuyến theo mã chuyến để tránh lookup lặp lại khi hiển thị nhiều vé
    private final Map<String, Tuyen> tuyenByMaChuyenCache = new HashMap<>();

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
        initUI();
        refreshAll();
    }

    // =========================================================================
    // MOCK DATA – 2 vé Sài Gòn – Hà Nội 01/04/2026
    // =========================================================================
    // =========================================================================
    // LẤY DỮ LIỆU THỰC TẾ TỪ CÁC BƯỚC TRƯỚC
    // =========================================================================
    public void loadDataFromSession() {
        dsVe.clear(); // Xóa dữ liệu cũ
        tuyenByMaChuyenCache.clear();

        // Khởi tạo Nhân viên (Lấy từ tài khoản đang đăng nhập, ở đây tôi fix cứng làm ví dụ)
        this.nv = new NhanVien();
        this.nv.setMaNV("NV0001");
        this.nv.setTenNV("Nguyễn Văn A");

        // Lấy danh sách Map từ session
        List<Map<String, String>> sessionData = mainTab.getSelectedSeatsData();
        if (sessionData.isEmpty()) return;

        // Dùng DAO để lấy đối tượng thật từ Database
        com.dao.DAO_KhachHang daoKH = new com.dao.DAO_KhachHang();
        com.dao.DAO_ChoNgoi daoCho = new com.dao.DAO_ChoNgoi();
        com.dao.DAO_LichTrinh daoLT = new com.dao.DAO_LichTrinh();

        for (Map<String, String> map : sessionData) {
            String maLT = map.get("maLT");
            String maCho = map.get("maCho");
            String maKH = map.get("maKH");
            String maLoaiVe = map.get("maLoaiVe");

            // Tạo các đối tượng
            KhachHang khach = daoKH.getKhachHangByMa(maKH);
            ChoNgoi cho = daoCho.getChoNgoiByMa(maCho);
            LichTrinh lichTrinh = daoLT.getLichTrinhByMa(maLT);

            LoaiVe lv = new LoaiVe();
            lv.setMaLoai(maLoaiVe);
            lv.setTenLoai(maLoaiVe.equals("LV01") ? "Người lớn" : "Trẻ em/SV"); // Tạm gán tên

            // Lấy giá vé thực tế từ Database dựa vào LichTrinh, Toa và LoaiVe
            long giaGoc = getGiaVeTuDatabase(maLT, cho.getToa().getLoaiToa().getMaLoaiToa(), maLoaiVe);

            // Gán vào đối tượng Vé
            Ve ve = new Ve();
            ve.setMaVe("V" + System.currentTimeMillis() + (int)(Math.random()*100)); // Mã vé tạm thời
            ve.setKhachHang(khach);
            ve.setChoNgoi(cho);
            ve.setLichTrinh(lichTrinh);
            ve.setLoaiVe(lv);
            ve.setGiaVe(giaGoc);
            ve.setTrangThaiVe(TrangThaiVe.CHUA_SU_DUNG);

            VeEntry entry = new VeEntry();
            entry.ve = ve;
            entry.kmChon = null; // Mặc định chưa chọn KM

            dsVe.add(entry);

            // Lưu lại thông tin Tuyến và KH đặt vé (dùng chung cho hóa đơn)
            if (this.tuyen == null) {
                // Tuyen được lấy thông qua maChuyen của LichTrinh
                String maChuyen = lichTrinh.getMaChuyen();
                if (maChuyen != null) {
                    com.dao.DAO_ChuyenTau daoChuyen = new com.dao.DAO_ChuyenTau();
                    List<com.dao.DAO_ChuyenTau.ChuyenTauRow> ctList = daoChuyen.getAll();
                    for (com.dao.DAO_ChuyenTau.ChuyenTauRow row : ctList) {
                        if (row.maChuyen.equals(maChuyen)) {
                            this.tuyen = new com.dao.DAO_Tuyen().getTuyenByMa(row.maTuyen);
                            break;
                        }
                    }
                }
            }
            if (this.kh == null) this.kh = khach;
        }

        refreshAll(); // Vẽ lại toàn bộ giao diện
    }

    private Tuyen getTuyenByMaChuyen(String maChuyen) {
        if (maChuyen == null || maChuyen.isEmpty()) return null;
        if (tuyenByMaChuyenCache.containsKey(maChuyen)) return tuyenByMaChuyenCache.get(maChuyen);

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
        if (ve == null || ve.getLichTrinh() == null) return "—";
        Tuyen tuyenVe = getTuyenByMaChuyen(ve.getLichTrinh().getMaChuyen());
        return tuyenVe != null ? tuyenVe.getTenTuyen() : "—";
    }

    // Hàm gọi DB lấy giá (Bạn có thể viết hàm này bên DAO_Gia)
    private long getGiaVeTuDatabase(String maLT, String maLoaiToa, String maLoaiVe) {
        long gia = 0;
        String sql = "SELECT gd.gia FROM GiaDetail gd JOIN GiaHeader gh ON gd.maGia = gh.maGia " +
                "WHERE gh.maLT = ? AND gd.maLoaiToa = ? AND gd.maLoaiVe = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLT); ps.setString(2, maLoaiToa); ps.setString(3, maLoaiVe);
            var rs = ps.executeQuery();
            if (rs.next()) gia = rs.getLong("gia");
        } catch (Exception e) { e.printStackTrace(); }
        return gia == 0 ? 500000 : gia; // Nếu không có giá, trả về mặc định 500k
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

        // Chỉ giữ 1 cụm thao tác ở dưới: PTTT + nút xác nhận
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

        String tenTuyen = getTenTuyenForVe(ve);
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
        Tuyen tuyenVe = (ve.getLichTrinh() != null) ? getTuyenByMaChuyen(ve.getLichTrinh().getMaChuyen()) : null;
        List<KhuyenMaiDetail> dsKMD = daoKMD.getKhuyenMaiDetailKhaDung(new Date(), ve.getLoaiVe(), ve.getChoNgoi().getToa().getLoaiToa(), tuyenVe);
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
        String tenTuyen = getTenTuyenForVe(ve);
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
    // XỬ LÝ LƯU DATABASE (TRANSACTION)
    // =========================================================================
    private void xuLyThanhToanDatabase() {
        if (dsVe.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có vé nào để thanh toán!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận thanh toán và in vé?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Sinh mã Hóa Đơn (Ví dụ: HD20260412xxxx)
        String maHD = "HD" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        long tongTienCuoiCung = 0;
        for (VeEntry e : dsVe) tongTienCuoiCung += e.thanhTien();

        try {
            // TẮT AUTO COMMIT ĐỂ BẢO VỆ DỮ LIỆU
            conn.setAutoCommit(false);

            // 1. INSERT VÀO BẢNG HOADON
            String sqlHD = "INSERT INTO HoaDon (maHD, maNV, maKH, tongTien) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psHD = conn.prepareStatement(sqlHD)) {
                psHD.setString(1, maHD);
                psHD.setString(2, nv.getMaNV());
                psHD.setString(3, kh.getMaKH());
                psHD.setLong(4, tongTienCuoiCung);
                psHD.executeUpdate();
            }

            // LOOP TỪNG VÉ
            String sqlVe = "INSERT INTO Ve (maVe, maKH, maLT, maToa, viTriGhe, maLoaiVe, giaVe, trangThaiVe) VALUES (?, ?, ?, ?, ?, ?, ?, 'CHUASUDUNG')";
            String sqlCTHD = "INSERT INTO ChiTietHoaDon (maHD, maVe, MaKMDetail, tienGoc, tienGiam, thanhTien) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlChoNgoi = "UPDATE GheLichTrinh SET trangThai = 'DADAT' WHERE maLT = ? AND maToa = ? AND viTri = ?";
            String sqlEnsureGhe = "IF NOT EXISTS (SELECT 1 FROM GheLichTrinh WHERE maLT = ? AND maToa = ? AND viTri = ?) " +
                    "INSERT INTO GheLichTrinh(maLT, maToa, viTri, trangThai) VALUES (?, ?, ?, 'TRONG')";


            try (PreparedStatement psVe = conn.prepareStatement(sqlVe);
                 PreparedStatement psCTHD = conn.prepareStatement(sqlCTHD);
                 PreparedStatement psCho = conn.prepareStatement(sqlChoNgoi);
                 PreparedStatement psEnsureGhe = conn.prepareStatement(sqlEnsureGhe)) {

                for (int i = 0; i < dsVe.size(); i++) {
                    VeEntry entry = dsVe.get(i);
                    String maVeReal = maHD + "_V" + (i + 1); // Sinh mã vé: HDxxxx_V1, HDxxxx_V2

                    String maLT = entry.ve.getLichTrinh().getMaLT();
                    String maToa = entry.ve.getChoNgoi().getToa().getMaToa();
                    String viTri = entry.ve.getChoNgoi().getTenCho();

                    // 2.0 Đảm bảo ghế tồn tại trong GheLichTrinh trước khi insert Vé (tránh lỗi FK)
                    psEnsureGhe.setString(1, maLT);
                    psEnsureGhe.setString(2, maToa);
                    psEnsureGhe.setString(3, viTri);
                    psEnsureGhe.setString(4, maLT);
                    psEnsureGhe.setString(5, maToa);
                    psEnsureGhe.setString(6, viTri);
                    psEnsureGhe.executeUpdate();

                    // 2. INSERT VÀO BẢNG VE
                    psVe.setString(1, maVeReal);
                    psVe.setString(2, entry.ve.getKhachHang().getMaKH());
                    psVe.setString(3, maLT);
                    psVe.setString(4, maToa);
                    psVe.setString(5, viTri);
                    psVe.setString(6, entry.ve.getLoaiVe().getMaLoai());
                    psVe.setLong(7, entry.tienGoc());
                    psVe.executeUpdate();

                    // 3. INSERT VÀO CHI TIẾT HÓA ĐƠN
                    psCTHD.setString(1, maHD);
                    psCTHD.setString(2, maVeReal);
                    if (entry.kmChon != null) psCTHD.setString(3, entry.kmChon.getMaKMDetail());
                    else psCTHD.setNull(3, java.sql.Types.VARCHAR);
                    psCTHD.setLong(4, entry.tienGoc());
                    psCTHD.setLong(5, entry.tienGiam());
                    psCTHD.setLong(6, entry.thanhTien());
                    psCTHD.executeUpdate();

                    // 4. UPDATE TRẠNG THÁI GHẾ (DADAT)
                    psCho.setString(1, maLT);
                    psCho.setString(2, maToa);
                    psCho.setString(3, viTri);
                    psCho.executeUpdate();
                }
            }

            // COMMIT TOÀN BỘ DỮ LIỆU LÊN SERVER
            conn.commit();
            conn.setAutoCommit(true);

            JOptionPane.showMessageDialog(this, "Thanh toán thành công! Mã Hóa Đơn: " + maHD);

            // Chuyển sang Step 5 (Thành công / In vé)
            mainTab.nextStep();

        } catch (Exception ex) {
            try {
                conn.rollback(); // NẾU CÓ LỖI, HỦY BỎ TOÀN BỘ QUÁ TRÌNH LƯU
                conn.setAutoCommit(true);
            } catch (Exception rbe) { rbe.printStackTrace(); }

            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi thanh toán! Đã hoàn tác dữ liệu.\n" + ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
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
