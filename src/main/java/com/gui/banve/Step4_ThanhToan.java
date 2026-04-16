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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

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
     * VeEntry = 1 vé + danh sách khuyến mãi đã chọn cho chính vé đó.
     * Mỗi khuyến mãi được biểu diễn bằng entity ChiTiet_KhuyenMai để UI và lưu DB đồng nhất.
     * Quy tắc áp dụng: giảm TIỀN trước -> giảm % sau.
     */
    private static class VeEntry {
        Ve ve;
        final List<ChiTiet_KhuyenMai> dsChiTietKM = new ArrayList<>();

        long tienGoc() { return ve != null ? (long) ve.getGiaVe() : 0L; }

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
                if (km == null) continue;

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
            if (km == null || containsKhuyenMai(km.getMaKMDetail())) return;
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

        long thanhTien() { return Math.max(0L, tienGoc() - tienGiam()); }
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
    // LẤY DỮ LIỆU THỰC TẾ TỪ CÁC BƯỚC TRƯỚC
    // =========================================================================
    public void loadDataFromSession() {
        dsVe.clear(); // Xóa dữ liệu cũ
        tuyenByMaChuyenCache.clear();
        List<LoaiVe> allLoaiVe = getAllLoaiVe();

        // Khởi tạo Nhân viên (Lấy từ tài khoản đang đăng nhập, ở đây tôi fix cứng làm ví dụ)
        this.nv = new NhanVien();
        this.nv.setMaNV("NV0001");
        this.nv.setTenNV("Nguyễn Văn A");

        // Lấy danh sách Map từ session
        List<Map<String, String>> sessionData = mainTab.getSelectedSeatsData();
        if (sessionData.isEmpty()) return;

        // Dùng DAO để lấy đối tượng thật từ Database
        com.dao.DAO_KhachHang daoKH = new com.dao.DAO_KhachHang();
        com.dao.DAO_LichTrinh daoLT = new com.dao.DAO_LichTrinh();
        DAO_Toa daoToa = new DAO_Toa();

        for (Map<String, String> map : sessionData) {
            String maLT = map.get("maLT");
            String maKH = map.get("maKH");
            String maLoaiVe = map.get("maLoaiVe");
            String maToa = map.get("maToa");
            String viTriGhe = map.get("viTriGhe");

            // Tạo các đối tượng
            KhachHang khach = daoKH.getKhachHangByMa(maKH);
            LichTrinh lichTrinh = daoLT.getLichTrinhByMa(maLT);
            Toa toa = daoToa.getToaById(maToa);
            LoaiVe loaiVe = allLoaiVe.stream()
                    .filter(lv -> Objects.equals(lv.getMaLoai(), maLoaiVe))
                    .findFirst()
                    .orElse(null);

            // Lấy giá vé thực tế từ Database dựa vào LichTrinh, Toa và LoaiVe
            long giaGoc = getGiaVeTuDatabase(maLT, toa.getLoaiToa().getMaLoaiToa());
            // Gán vào đối tượng Vé
            Ve ve = new Ve();
            ve.setMaVe("V" + System.currentTimeMillis() + (int)(Math.random()*100)); // Mã vé tạm thời
            ve.setKhachHang(khach);
            ve.setLichTrinh(lichTrinh);
            ve.setToa(toa);
            ve.setViTriGhe(viTriGhe);
            ve.setLoaiVe(loaiVe);
            ve.setGiaVe(giaGoc);
            ve.setTrangThaiVe(TrangThaiVe.CHUA_SU_DUNG);

            VeEntry entry = new VeEntry();
            entry.ve = ve;
            entry.dsChiTietKM.clear(); // Mặc định chưa chọn KM

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
    private long getGiaVeTuDatabase(String maLT, String maLoaiToa) {
        long gia = 0;
        String sql = "SELECT gd.gia FROM GiaDetail gd JOIN GiaHeader gh ON gd.maGia = gh.maGia " +
                "WHERE gh.maLT = ? AND gd.maLoaiToa = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLT); ps.setString(2, maLoaiToa);
            var rs = ps.executeQuery();
            if (rs.next()) gia = rs.getLong("gia");
        } catch (Exception e) { e.printStackTrace(); }
        return gia == 0 ? 500000 : gia; // Nếu không có giá, trả về mặc định 500k
    }

    private List<LoaiVe> getAllLoaiVe() {
        List<LoaiVe> list = new ArrayList<>();
        String sql = "SELECT * FROM LoaiVe";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            var rs = ps.executeQuery();
            while (rs.next()) {
                LoaiVe lv = new LoaiVe();
                lv.setMaLoai(rs.getString("MaLoai"));
                lv.setTenLoai(rs.getString("TenLoai"));
                list.add(lv);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================================
    // INIT UI
    // =========================================================================
    private void initUI() {
        setLayout(new BorderLayout(0, 16)); setOpaque(false);
        add(UIHelper.createPageTitle(
                        "THANH TOÁN & HOÀN TẤT",
                        ""),
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
        pnlMeta.add(makeInfoLbl(kh != null ? kh.getHoTen()  : "—", false));

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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
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

        // ── THÔNG TIN VÉ (6 hàng: KH, Ngày+Giờ, Tuyến, Toa+Ghế, Loại) ──
        JPanel info = new JPanel(new GridLayout(6, 2, 8, 3)); info.setOpaque(false);

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
        String viTriGhe  = ve.getViTriGhe() != null
                ? ve.getToa().getLoaiToa().getTenLoaiToa() + " – Ghế " + ve.getViTriGhe() : "—";
        String loaiVeStr  = ve.getLoaiVe()  != null ? ve.getLoaiVe().getTenLoai()  : "—";
        JLabel lblGia = new JLabel(buildGiaHtml(entry));
        info.add(makeVeInfoLbl("Hành khách:", true));  info.add(makeVeInfoLbl(tenKH,     false));
        info.add(makeVeInfoLbl("Khởi hành:",  true));  info.add(makeVeInfoLbl(ngayGio,   false));
        info.add(makeVeInfoLbl("Tuyến:",      true));  info.add(makeVeInfoLbl(tenTuyen,  false));
        info.add(makeVeInfoLbl("Chỗ:",        true));  info.add(makeVeInfoLbl(viTriGhe,       false));
        info.add(makeVeInfoLbl("Loại:",       true));  info.add(makeVeInfoLbl(loaiVeStr, false));
        info.add(makeVeInfoLbl("Giá:",        true));  info.add(lblGia);
        card.add(info, BorderLayout.CENTER);

        // ── MULTI-SELECT KM + CHIPS ──
        JPanel bottom = new JPanel(new BorderLayout(0, 6)); bottom.setOpaque(false);

        Tuyen tuyenVe = (ve.getLichTrinh() != null) ? getTuyenByMaChuyen(ve.getLichTrinh().getMaChuyen()) : null;
        List<KhuyenMaiDetail> dsKMD = daoKMD.getKhuyenMaiDetailKhaDung(new Date(), ve.getLoaiVe(),
                ve.getToa() != null ? ve.getToa().getLoaiToa() : null, tuyenVe);

        // Panel chips hiển thị KM đã chọn
        JPanel pnlChips = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 4)); pnlChips.setOpaque(false);

        // Label giá realtime
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

        // Dropdown chọn KM
        JPanel dropdownRow = new JPanel(new BorderLayout(6, 0)); dropdownRow.setOpaque(false);
        JButton btnDropdown = makeKMDropdownBtn(dsKMD, entry, () -> {
            refreshTicketPromoUI.run();
            refreshReceipt();
            card.revalidate();
            card.repaint();
        });

        dropdownRow.add(btnDropdown, BorderLayout.CENTER);

        bottom.add(dropdownRow, BorderLayout.NORTH);
        bottom.add(pnlChips,    BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    /** HTML giá hiển thị trong card vé (compact) */
    private String buildGiaHtml(VeEntry e) {
        if (e.tienGiam() > 0) {
            return "<html><s style='color:#A0AEC0;font-size:10px'>" + formatTien(e.tienGoc()) + "</s>"
                    + "&nbsp;<b style='color:#16A34A'>" + formatTien(e.thanhTien()) + "</b></html>";
        }
        return "<html><b style='color:#1E2B3C'>" + formatTien(e.thanhTien()) + "</b></html>";
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

    /** 1 card receipt bên trái — hiển thị giảm từng bước theo từng KM */
    private JPanel buildReceiptRow(VeEntry entry) {
        Ve ve = entry.ve;

        // Tính giảm từng bước
        List<ChiTiet_KhuyenMai> sortedKMs = entry.getChiTietKMSortedAndCalculated();
        long[] giamMoi = entry.tinhGiamTungBuoc();

        JPanel row = new JPanel(); row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIHelper.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        // Không set maxHeight cứng vì số KM thay đổi

        // ── Dòng header: mã vé + tên KH ──
        JPanel hdr = new JPanel(new BorderLayout(8, 0)); hdr.setOpaque(false);
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel lVe = new JLabel(ve.getMaVe());
        lVe.setFont(new Font("Segoe UI", Font.BOLD, 13)); lVe.setForeground(UIHelper.ACCENT);
        String tenKH = ve.getKhachHang() != null ? ve.getKhachHang().getHoTen() : "";
        JLabel lKH = new JLabel(tenKH);
        lKH.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lKH.setForeground(UIHelper.TEXT_MID);
        hdr.add(lVe, BorderLayout.WEST); hdr.add(lKH, BorderLayout.EAST);
        row.add(hdr);
        row.add(Box.createVerticalStrut(2));

        // ── Dòng chi tiết: toa + ngày/giờ ──
        String tuyen = getTenTuyenForVe(ve);
        String viTriGhe = ve.getViTriGhe() != null
                ? ve.getToa().getLoaiToa().getTenLoaiToa() + " – Ghế " + ve.getViTriGhe() : "—";
        String ngayGio = "—";
        String loaiVeStr = ve.getLoaiVe() != null ? ve.getLoaiVe().getTenLoai() : "—";

        if (ve.getLichTrinh() != null) {
            String ngay = ve.getLichTrinh().getNgayKhoiHanh() != null ? ve.getLichTrinh().getNgayKhoiHanh().toString() : "?";
            String gio  = ve.getLichTrinh().getGioKhoiHanh()  != null
                    ? String.format("%02d:%02d", ve.getLichTrinh().getGioKhoiHanh().getHour(), ve.getLichTrinh().getGioKhoiHanh().getMinute()) : "?";
            ngayGio = ngay + " " + gio;
        }
        JLabel lDetail = new JLabel(tuyen + " | " + ngayGio  + " | " +viTriGhe + " | " + loaiVeStr);
        lDetail.setFont(new Font("Segoe UI", Font.PLAIN, 11)); lDetail.setForeground(UIHelper.TEXT_MID);
        lDetail.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(lDetail);
        row.add(Box.createVerticalStrut(6));

        // ── Separator mảnh ──
        JSeparator sep = new JSeparator(); sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UIHelper.BORDER); row.add(sep); row.add(Box.createVerticalStrut(6));

        // ── Giá gốc (gạch ngang nếu có KM) ──
        JPanel rowGoc = makeReceiptPriceLine(
                "Giá gốc",
                formatTien(entry.tienGoc()),
                false,//!sortedKMs.isEmpty(),   // gạch ngang nếu có ít nhất 1 KM
                UIHelper.TEXT_DARK, true);
        row.add(rowGoc);

        // ── Từng KM giảm ──
        long conLai = entry.tienGoc();
        for (int i = 0; i < sortedKMs.size(); i++) {
            ChiTiet_KhuyenMai ctKM = sortedKMs.get(i);
            KhuyenMaiDetail k = ctKM.getKhuyenMaiDetail();
            long giam = giamMoi[i];
            conLai -= giam;

            row.add(Box.createVerticalStrut(3));

            // Dòng KM: tên + loại + giaTri → số tiền giảm
            String kmLabel = formatKhuyenMaiLine(k);
//            boolean isLast = (i == sortedKMs.size() - 1);

            JPanel rowKM = makeReceiptPriceLine(
                    "  ↳ " + kmLabel,
                    "- " + formatTien(giam),
                    false,   // gạch ngang giá còn lại nếu còn KM tiếp
                    UIHelper.DANGER, false);
            row.add(rowKM);
        }

        // ── Dòng cuối: thành tiền ──
        row.add(Box.createVerticalStrut(5));
        JSeparator sep2 = new JSeparator(); sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep2.setForeground(UIHelper.BORDER); row.add(sep2); row.add(Box.createVerticalStrut(5));

        JPanel rowTT = makeReceiptPriceLine(
                "Thành tiền",
                formatTien(entry.thanhTien()),
                false,
                entry.tienGiam() > 0 ? UIHelper.SUCCESS : UIHelper.TEXT_DARK,
                entry.tienGiam() > 0);
        row.add(rowTT);

        return row;
    }

    /** 1 dòng price trong receipt: [label bên trái] [giá bên phải, optional gạch ngang] */
    private JPanel makeReceiptPriceLine(String label, String price, boolean strikePrice,
                                        Color priceColor, boolean boldPrice) {
        JPanel line = new JPanel(new BorderLayout(4, 0)); line.setOpaque(false);
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

        line.add(lLbl,   BorderLayout.WEST);
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
            String sqlCTHD = "INSERT INTO ChiTietHoaDon (maHD, maVe, tienGoc, tienGiam, thanhTien) VALUES (?, ?, ?, ?, ?)";
            String sqlCTKM = "INSERT INTO ChiTiet_KhuyenMai (maHD, maVe, maKMDetail, tienGiamCuaKM) VALUES (?, ?, ?, ?)";
            String sqlChoNgoi = "UPDATE GheLichTrinh SET trangThai = 'DADAT' WHERE maLT = ? AND maToa = ? AND viTri = ?";
            String sqlEnsureGhe = "IF NOT EXISTS (SELECT 1 FROM GheLichTrinh WHERE maLT = ? AND maToa = ? AND viTri = ?) " +
                    "INSERT INTO GheLichTrinh(maLT, maToa, viTri, trangThai) VALUES (?, ?, ?, 'TRONG')";


            try (PreparedStatement psVe = conn.prepareStatement(sqlVe);
                 PreparedStatement psCTHD = conn.prepareStatement(sqlCTHD);
                 PreparedStatement psCTKM = conn.prepareStatement(sqlCTKM);
                 PreparedStatement psCho = conn.prepareStatement(sqlChoNgoi);
                 PreparedStatement psEnsureGhe = conn.prepareStatement(sqlEnsureGhe)) {

                for (int i = 0; i < dsVe.size(); i++) {
                    VeEntry entry = dsVe.get(i);
                    String maVeReal = maHD + "_V" + (i + 1); // Sinh mã vé: HDxxxx_V1, HDxxxx_V2

                    String maLT = entry.ve.getLichTrinh().getMaLT();
                    String maToa = entry.ve.getToa().getMaToa();
                    String viTri = entry.ve.getViTriGhe();

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

                    // 3. INSERT 1 DÒNG CTHD / VÉ
                    psCTHD.setString(1, maHD);
                    psCTHD.setString(2, maVeReal);
                    psCTHD.setLong(3, entry.tienGoc());
                    psCTHD.setLong(4, entry.tienGiam());
                    psCTHD.setLong(5, entry.thanhTien());
                    psCTHD.executeUpdate();

                    // 4. INSERT CHI TIẾT KHUYẾN MÃI (nếu có)
                    for (ChiTiet_KhuyenMai ctKM : entry.getChiTietKMSortedAndCalculated()) {
                        KhuyenMaiDetail km = ctKM.getKhuyenMaiDetail();
                        if (km == null) continue;
                        psCTKM.setString(1, maHD);
                        psCTKM.setString(2, maVeReal);
                        psCTKM.setString(3, km.getMaKMDetail());
                        psCTKM.setLong(4, Math.round(ctKM.getTienGiamCuaKM()));
                        psCTKM.executeUpdate();
                    }

                    // 5. UPDATE TRẠNG THÁI GHẾ (DADAT)
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

    // ── WrapLayout: FlowLayout tự xuống dòng khi overflow ────────────────────
    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
                int x = 0, y = insets.top + vgap, rowH = 0;
                for (Component c : target.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x + d.width > maxWidth) { y += rowH + vgap; x = 0; rowH = 0; }
                    x += d.width + hgap; rowH = Math.max(rowH, d.height);
                }
                return new Dimension(targetWidth, y + rowH + insets.bottom + vgap);
            }
        }
    }

    /** Chip hiển thị 1 KM đã chọn, có nút ✕ để bỏ */
    private JPanel makeKMChip(ChiTiet_KhuyenMai ctKM, Runnable onRemove) {
        KhuyenMaiDetail k = ctKM.getKhuyenMaiDetail();
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        chip.setOpaque(true);
        chip.setBackground(new Color(0xDCFCE7));
        chip.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x86EFAC), 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 4)));

        String label = formatKhuyenMaiLine(k);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(0x166534));

        JButton btnX = new JButton("✕");
        btnX.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnX.setForeground(new Color(0x166534));
        btnX.setPreferredSize(new Dimension(16, 16));
        btnX.setContentAreaFilled(false); btnX.setBorderPainted(false); btnX.setFocusPainted(false);
        btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(e -> onRemove.run());

        chip.add(lbl); chip.add(btnX);
        return chip;
    }

    /** Nút dropdown mở popup checklist chọn nhiều KM */
    private JButton makeKMDropdownBtn(List<KhuyenMaiDetail> dsKMD, VeEntry entry, Runnable onClose) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF8FAFD));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(UIHelper.BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(UIHelper.TEXT_MID);
        btn.setText("+ Chọn khuyến mãi ▾");
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 32));

        btn.addActionListener(e -> {
            if (dsKMD.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có KM khả dụng cho vé này!"); return;
            }

            JPopupMenu popup = new JPopupMenu();
            popup.setLayout(new BorderLayout());
            popup.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIHelper.BORDER, 1, true),
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
                    if (cb.isSelected()) entry.addKhuyenMai(k);
                    else entry.removeKhuyenMai(k.getMaKMDetail());
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
                @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent ev) { onClose.run(); }
                @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent ev) { onClose.run(); }
                @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent ev) {}
            });

            popup.show(btn, 0, btn.getHeight());
        });

        return btn;
    }

    private String formatKhuyenMaiLine(KhuyenMaiDetail k) {
        if (k == null) return "";
        String tenKM = k.getKhuyenMai() != null ? k.getKhuyenMai().getTenKM() : k.getMaKMDetail();
        String loai = k.getLoaiKM() != null ? k.getLoaiKM().getLabel() : "KM";
        return tenKM + " | " + loai + " | " + formatGiaTri(k);
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