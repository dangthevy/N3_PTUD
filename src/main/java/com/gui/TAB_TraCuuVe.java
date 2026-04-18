package com.gui;

import com.dao.DAO_Ve;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TAB_TraCuuVe extends JPanel {

    // ================= DAO =================
    private final DAO_Ve daoVe = new DAO_Ve();

    // ================= STYLE =================
    private static final Color BG_PAGE     = new Color(0xF4F7FB);
    private static final Color BG_CARD     = Color.WHITE;
    private static final Color ACCENT      = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR  = new Color(0x2270CC);
    private static final Color TEXT_DARK   = new Color(0x1E2B3C);
    private static final Color TEXT_MID    = new Color(0x5A6A7D);
    private static final Color BORDER      = new Color(0xE2EAF4);
    private static final Color ROW_ALT     = new Color(0xF7FAFF);
    private static final Color ROW_SEL     = new Color(0xDDEEFF);
    private static final Color BTN_RED     = new Color(0xC0392B);
    private static final Color BTN_RED_HVR = new Color(0xE74C3C);
    private static final Color BTN_ORANGE  = new Color(0xD97706);
    private static final Color BTN_ORANGE_HVR = new Color(0xF59E0B);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_MUTED  = new Color(100, 116, 139);

    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER, WARNING }

    // ================= STAT LABELS =================
    private JLabel lblTongVe      = new JLabel("0");
    private JLabel lblChuaSuDung  = new JLabel("0");
    private JLabel lblDaSuDung    = new JLabel("0");
    private JLabel lblDaHoan      = new JLabel("0");

    // ================= FILTER =================
    private JTextField txtMaVe;

    // ================= TABLE =================
    private DefaultTableModel tableModel;
    private JTable table;

    private final DecimalFormat df = new DecimalFormat("#,##0 VND");

    private static final String[] COLS = {
        "Mã Vé", "Mã KH", "Tên KH", "Mã LT", "Toa", "Ghế", "Loại Vé", "Giá Vé", "Trạng Thái"
    };

    // ================= CONSTRUCTOR =================
    public TAB_TraCuuVe() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        initTable();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        topPanel.add(buildStatsBar());
        topPanel.add(Box.createVerticalStrut(14));
        topPanel.add(buildHeader());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildFilterCard());
        topPanel.add(Box.createVerticalStrut(14));

        add(topPanel, BorderLayout.NORTH);
        add(buildDanhSachCard(), BorderLayout.CENTER);

        loadData(null);
    }

    // ================= TABLE INIT =================
    private void initTable() {
        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                    ((JLabel) c).setForeground(TEXT_DARK);
                } else {
                    c.setBackground(ROW_SEL);
                    ((JLabel) c).setForeground(TEXT_DARK);
                }
                if (col == 8 && !isRowSelected(row)) {
                    String st = getValueAt(row, col).toString().toUpperCase();
                    switch (st) {
                        case "DAHOAN":
                            c.setBackground(new Color(0xFFF3CD));
                            ((JLabel) c).setForeground(new Color(0x856404)); break;
                        case "DASUDUNG":
                            c.setBackground(new Color(0xD1FAE5));
                            ((JLabel) c).setForeground(new Color(0x065F46)); break;
                        case "HETHAN":
                            c.setBackground(new Color(0xFEE2E2));
                            ((JLabel) c).setForeground(new Color(0x991B1B)); break;
                        default:
                            c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                            ((JLabel) c).setForeground(new Color(0x16A34A));
                    }
                }
                return c;
            }
        };
        table.setFont(F_CELL);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.getTableHeader().setPreferredSize(new Dimension(0, 44));
        table.getTableHeader().setReorderingAllowed(false);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                        showChiTietDialog(row);
                    }
                }
            }
        });
    }

    // ================= DATA =================
    private void loadData(String maVeFilter) {
        tableModel.setRowCount(0);
        try {
            ResultSet rs = daoVe.getDanhSachVe(maVeFilter, null);
            while (rs != null && rs.next()) {
                String status = rs.getString("trangThaiVe");
                // thanhTien = giá sau ap dung KM (từ ChiTietHoaDon), fallback về giaVe nếu chưa có HĐ
                double thanhTien = rs.getDouble("thanhTien");
                tableModel.addRow(new Object[]{
                    rs.getString("maVe"),
                    rs.getString("maKH")     != null ? rs.getString("maKH")     : "",
                    rs.getString("tenKH")    != null ? rs.getString("tenKH")    : "Khách lẻ",
                    rs.getString("maLT"),
                    rs.getString("maToa"),
                    rs.getString("viTriGhe"),
                    rs.getString("tenLoaiVe") != null ? rs.getString("tenLoaiVe") : "",
                    df.format(thanhTien),   // ← giá SAU khuyến mãi
                    status != null ? status : "CHUASUDUNG"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi kết nối CSDL: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        refreshStats();
    }

    private void refreshStats() {
        int[] counts = daoVe.demTongVeVaVeHoan();
        int tong = counts[0], daHoan = counts[1];
        int chuaSuDung = 0, daSuDung = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String st = tableModel.getValueAt(i, 8).toString().toUpperCase();
            if ("CHUASUDUNG".equals(st)) chuaSuDung++;
            else if ("DASUDUNG".equals(st)) daSuDung++;
        }
        lblTongVe.setText(String.valueOf(tong));
        lblChuaSuDung.setText(String.valueOf(chuaSuDung));
        lblDaSuDung.setText(String.valueOf(daSuDung));
        lblDaHoan.setText(String.valueOf(daHoan));
    }

    // ================= CHI TIẾT VÉ DIALOG =================
    private void showChiTietDialog(int row) {
        String maVe     = tableModel.getValueAt(row, 0).toString();
        String maKH     = tableModel.getValueAt(row, 1).toString();
        String tenKH    = tableModel.getValueAt(row, 2).toString();
        String maLT     = tableModel.getValueAt(row, 3).toString();
        String maToa    = tableModel.getValueAt(row, 4).toString();
        String viTri    = tableModel.getValueAt(row, 5).toString();
        String loaiVe   = tableModel.getValueAt(row, 6).toString();
        String giaVe    = tableModel.getValueAt(row, 7).toString();
        String trangThai = tableModel.getValueAt(row, 8).toString().toUpperCase();

        // Lấy thêm chi tiết từ DB (tuyến, tàu, ngày giờ, v.v.)
        String gaKhoiHanh = "", gaDen = "", tenTau = "", ngayDi = "", gioDi = "";
        String tenLoaiToa = "", cccd = "", sdt = "", email = "";
        double giaVeRaw = 0; // giá gốc dạng số, dùng để tính giảm KM
        try {
            ResultSet rs = daoVe.getChiTietVe(maVe);
            if (rs != null && rs.next()) {
                // Tên tuyến = "ga đi → ga đến" (lấy từ tenTuyen nếu có)
                String tenTuyen = rs.getString("tenTuyen");
                if (tenTuyen != null && tenTuyen.contains("-")) {
                    String[] parts = tenTuyen.split("-", 2);
                    gaKhoiHanh = parts[0].trim();
                    gaDen      = parts[1].trim();
                } else if (tenTuyen != null) {
                    gaKhoiHanh = tenTuyen;
                }
                String tenChuyenFull = rs.getString("tenChuyen") != null ? rs.getString("tenChuyen") : "";
                // Lấy mã tàu thôi: "SE1: Sài Gòn - Hà Nội" → "SE1"
                tenTau = tenChuyenFull.contains(":") 
                       ? tenChuyenFull.split(":")[0].trim() 
                       : tenChuyenFull;
                java.sql.Date ngay = rs.getDate("ngayKhoiHanh");
                java.sql.Time gio  = rs.getTime("gioKhoiHanh");
                ngayDi = ngay != null ? new SimpleDateFormat("dd/MM/yyyy").format(ngay) : "";
                gioDi  = gio  != null ? new SimpleDateFormat("HH:mm").format(gio)       : "";
                tenLoaiToa = rs.getString("tenLoaiToa") != null ? rs.getString("tenLoaiToa") : "";
                cccd       = rs.getString("cccd")  != null ? rs.getString("cccd")  : "";
                sdt        = rs.getString("sdt")   != null ? rs.getString("sdt")   : "";
                email      = rs.getString("email") != null ? rs.getString("email") : "";
                giaVeRaw   = rs.getDouble("giaVe");
                if (tenKH.isEmpty() || tenKH.equals("Khách lẻ"))
                    tenKH = rs.getString("tenKH") != null ? rs.getString("tenKH") : "Khách lẻ";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi Tiết Vé — " + maVe, true);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        // Status badge colors (dùng lại bên dưới)
        String displayStatus = trangThaiToDisplay(trangThai);
        Color badgeBg, badgeFg;
        switch (trangThai) {
            case "DAHOAN":  badgeBg = new Color(0xFFF3CD); badgeFg = new Color(0x856404); break;
            case "DASUDUNG":badgeBg = new Color(0xD1FAE5); badgeFg = new Color(0x065F46); break;
            case "HETHAN":  badgeBg = new Color(0xFEE2E2); badgeFg = new Color(0x991B1B); break;
            default:        badgeBg = new Color(0xDCFCE7); badgeFg = new Color(0x16A34A);
        }

        // Lấy danh sách khuyến mãi đã áp dụng cho vé
        List<DAO_Ve.KhuyenMaiInfo> dsKM = daoVe.getKhuyenMaiCuaVe(maVe);

        // ---- BODY (dùng JScrollPane để chứa nội dung dài) ----
        JPanel pBody = new JPanel(new GridBagLayout());
        pBody.setBackground(BG_CARD);
        pBody.setBorder(BorderFactory.createEmptyBorder(22, 28, 18, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(7, 0, 7, 10);

        Object[][] fields = {
            { "Mã vé",           maVe,       false },
            { "Mã khách hàng",   maKH,       false },
            { "Họ tên",          tenKH,      true  },
            { "CCCD/Passport",   cccd,       false },
            { "Điện thoại",      sdt,        false },
            { "Tàu/Train",       tenTau,     false },
            { "Tuyến",           (gaKhoiHanh.isEmpty() ? "" : gaKhoiHanh + " → " + gaDen), false },
            { "Ngày đi/Date",    ngayDi,     false },
            { "Giờ đi/Time",     gioDi,      false },
            { "Toa/Coach",       maToa,      false },
            { "Chỗ/Seat",        viTri,      false },
            { "Loại chỗ/Class",  tenLoaiToa.isEmpty() ? loaiVe : tenLoaiToa, true },
            { "Loại vé/Ticket",  loaiVe,     true  },
        };

        int r = 0;
        for (Object[] field : fields) {
            g.gridy = r; g.gridx = 0; g.weightx = 0.38;
            JLabel lKey = new JLabel(field[0] + ":");
            lKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lKey.setForeground(TEXT_MID);
            pBody.add(lKey, g);
            g.gridx = 1; g.weightx = 0.62;
            JLabel lVal = new JLabel((String) field[1]);
            lVal.setFont((boolean) field[2] ? new Font("Segoe UI", Font.BOLD, 13) : F_CELL);
            lVal.setForeground(TEXT_DARK);
            pBody.add(lVal, g);
            r++;
        }

        // Separator giá
        g.gridy = r++; g.gridx = 0; g.gridwidth = 2; g.weightx = 1;
        g.insets = new Insets(4, 0, 4, 0);
        JSeparator sep = new JSeparator(); sep.setForeground(BORDER);
        pBody.add(sep, g);
        g.gridwidth = 1; g.insets = new Insets(6, 0, 6, 10);

        // ---- KHUYẾN MÃI (luôn hiển thị, phía trên giá) ----
        g.gridy = r; g.gridx = 0; g.weightx = 0.38;
        JLabel lKMKey = new JLabel("Khuyến mãi:");
        lKMKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lKMKey.setForeground(TEXT_MID);
        pBody.add(lKMKey, g);
        g.gridx = 1; g.weightx = 0.62;
        if (dsKM.isEmpty()) {
            JLabel lKMNone = new JLabel("Không có");
            lKMNone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lKMNone.setForeground(TEXT_MID);
            pBody.add(lKMNone, g);
            r++;
        } else {
            // Dòng đầu tiên thẳng hàng với label "Khuyến mãi:"
            DAO_Ve.KhuyenMaiInfo km0 = dsKM.get(0);
            String loai0 = km0.loaiKM.equals("GIAM_PHAN_TRAM")
                ? String.format("%.0f%%", km0.giaTri)
                : String.format("%,.0f VND", km0.giaTri);
            JLabel lKM0 = new JLabel("" + km0.tenKM + "  (Giảm " + loai0 + ")");
            lKM0.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lKM0.setForeground(new Color(0x0369A1));
            pBody.add(lKM0, g);
            r++;
            // Các KM tiếp theo (nếu có nhiều)
            for (int i = 1; i < dsKM.size(); i++) {
                DAO_Ve.KhuyenMaiInfo kmi = dsKM.get(i);
                String loaii = kmi.loaiKM.equals("GIAM_PHAN_TRAM")
                    ? String.format("%.0f%%", kmi.giaTri)
                    : String.format("%,.0f VND", kmi.giaTri);
                g.gridy = r; g.gridx = 1; g.weightx = 0.62;
                g.insets = new Insets(2, 0, 2, 10);
                JLabel lKMi = new JLabel("" + kmi.tenKM + "  (Giảm " + loaii + ")");
                lKMi.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lKMi.setForeground(new Color(0x0369A1));
                pBody.add(lKMi, g);
                r++;
                g.insets = new Insets(6, 0, 6, 10);
            }
        }

        // ---- GIÁ / PRICE ----
        double tongGiam = dsKM.stream().mapToDouble(k -> k.tienGiamThucTe).sum();
        g.gridy = r; g.gridx = 0; g.weightx = 0.38;
        g.insets = new Insets(6, 0, 6, 10);
        JLabel lGiaKey = new JLabel("Giá / Price:");
        lGiaKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lGiaKey.setForeground(TEXT_MID);
        pBody.add(lGiaKey, g);
        g.gridx = 1; g.weightx = 0.62;
        if (tongGiam > 0) {
            JPanel pGia = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            pGia.setOpaque(false);
            JLabel lGocStrike = new JLabel("<html><strike>" + String.format("%,.0f VND", giaVeRaw) + "</strike></html>");
            lGocStrike.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lGocStrike.setForeground(TEXT_MID);
            JLabel lGiaSau = new JLabel(String.format("%,.0f VND", giaVeRaw - tongGiam));
            lGiaSau.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lGiaSau.setForeground(new Color(0xDC2626));
            pGia.add(lGocStrike);
            pGia.add(lGiaSau);
            pBody.add(pGia, g);
        } else {
            JLabel lGiaVal = new JLabel(String.format("%,.0f VND", giaVeRaw));
            lGiaVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lGiaVal.setForeground(TEXT_DARK);
            pBody.add(lGiaVal, g);
        }
        r++;

        // Trạng thái
        g.gridy = r; g.gridx = 0; g.weightx = 0.38;
        JLabel lTTKey = new JLabel("Trạng thái:");
        lTTKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lTTKey.setForeground(TEXT_MID);
        pBody.add(lTTKey, g);
        g.gridx = 1; g.weightx = 0.62;
        JLabel lTTVal = new JLabel(displayStatus);
        lTTVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lTTVal.setForeground(badgeFg);
        pBody.add(lTTVal, g);

        // ---- FOOTER BUTTONS ----
        JPanel pFooter = new JPanel(new GridLayout(1, 3, 12, 0));
        pFooter.setBackground(new Color(0xF8FAFC));
        pFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
            BorderFactory.createEmptyBorder(14, 22, 14, 22)));

        JButton btnDong   = makeBtn("Đóng",       BtnStyle.SECONDARY);
        JButton btnHoanVe = makeBtn("Hoàn vé",    BtnStyle.WARNING);
        JButton btnInLai  = makeBtn("In lại vé", BtnStyle.PRIMARY);
        btnDong.setPreferredSize(new Dimension(0, 40));
        btnHoanVe.setPreferredSize(new Dimension(0, 40));
        btnInLai.setPreferredSize(new Dimension(0, 40));

        boolean coTheHoan = "CHUASUDUNG".equals(trangThai);
        btnHoanVe.setEnabled(coTheHoan);
        if (!coTheHoan) btnHoanVe.setToolTipText("Chỉ hoàn được vé ở trạng thái 'CHUASUDUNG'");

        // Capture final cho lambda
        final String fMaVe = maVe, fMaKH = maKH, fTenKH = tenKH, fMaLT = maLT;
        final String fMaToa = maToa, fViTri = viTri, fLoaiVe = loaiVe, fGiaVe = giaVe;
        final String fTrangThai = trangThai, fGaKH = gaKhoiHanh, fGaDen = gaDen;
        final String fTenTau = tenTau, fNgayDi = ngayDi, fGioDi = gioDi;
        final String fLoaiToa = tenLoaiToa, fCccd = cccd;
        final List<DAO_Ve.KhuyenMaiInfo> fDsKM = dsKM;
        final double fGiaVeRaw = giaVeRaw;

        btnDong.addActionListener(e -> dialog.dispose());
        btnHoanVe.addActionListener(e -> confirmHoanVe(fMaVe, dialog));
        btnInLai.addActionListener(e ->
            inVePDF(dialog, fMaVe, fMaKH, fTenKH, fCccd, fTenTau,
                    fGaKH, fGaDen, fNgayDi, fGioDi,
                    fMaToa, fViTri, fLoaiToa, fLoaiVe, fGiaVeRaw, fDsKM, fTrangThai));

        pFooter.add(btnHoanVe);
        pFooter.add(btnInLai);
        pFooter.add(btnDong);

        dialog.add(pBody,   BorderLayout.CENTER);
        dialog.add(pFooter, BorderLayout.SOUTH);
        dialog.setMinimumSize(new Dimension(500, 0));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ================= IN VÉ PDF (iText 7) =================
    /**
     * Xuất PDF boarding pass trông giống vé tàu VR Sài Gòn rồi mở / lưu.
     */
    private void inVePDF(JDialog parentDialog,
                         String maVe, String maKH, String tenKH, String cccd,
                         String tenTau, String gaKhoiHanh, String gaDen,
                         String ngayDi, String gioDi,
                         String maToa, String viTri,
                         String loaiChoDon, String loaiVe,
                         double giaVeRaw, List<DAO_Ve.KhuyenMaiInfo> dsKM,
                         String trangThai) {

        // -- Chọn nơi lưu file --
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu PDF vé tàu");
        fc.setSelectedFile(new File("Ve_" + maVe + ".pdf"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF files (*.pdf)", "pdf"));
        if (fc.showSaveDialog(parentDialog) != JFileChooser.APPROVE_OPTION) return;

        File dest = fc.getSelectedFile();
        if (!dest.getName().toLowerCase().endsWith(".pdf"))
            dest = new File(dest.getAbsolutePath() + ".pdf");

        try {
            buildBoardingPassPDF(dest, maVe, maKH, tenKH, cccd,
                tenTau, gaKhoiHanh, gaDen, ngayDi, gioDi,
                maToa, viTri, loaiChoDon, loaiVe, giaVeRaw, dsKM, trangThai);

            JOptionPane.showMessageDialog(parentDialog,
                "Đã xuất PDF thành công!\n" + dest.getAbsolutePath(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);

            // Mở PDF bằng trình xem mặc định
            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().open(dest);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentDialog,
                "Lỗi xuất PDF: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Tạo file PDF boarding pass theo layout VR Sài Gòn.
     * Khổ giấy A5 ngang (148 × 210 mm) để vừa 1 trang thẻ lên tàu.
     */
    private void buildBoardingPassPDF(File dest,
            String maVe, String maKH, String tenKH, String cccd,
            String tenTau, String gaKhoiHanh, String gaDen,
            String ngayDi, String gioDi,
            String maToa, String viTri,
            String loaiChoClass, String loaiVe,
            double giaVeRaw, List<DAO_Ve.KhuyenMaiInfo> dsKM,
            String trangThai) throws Exception {

        // iText color
        DeviceRgb BLACK  = new DeviceRgb(0,   0,   0);
        DeviceRgb GRAY   = new DeviceRgb(80,  80,  80);
        DeviceRgb LGRAY  = new DeviceRgb(180, 180, 180);

        // -- Khổ A5 đứng (148 × 210 mm) --
        PageSize ps = PageSize.A5;  // 419.53 × 595.28 pt

        PdfWriter  writer  = new PdfWriter(new FileOutputStream(dest));
        PdfDocument pdf    = new PdfDocument(writer);
        Document   doc     = new Document(pdf, ps);
        doc.setMargins(28, 28, 28, 28);

        // Font hỗ trợ tiếng Việt: tự động tìm theo hệ điều hành
        PdfFont fontBold  = loadVietnameseFont(true);
        PdfFont fontPlain = loadVietnameseFont(false);

        float pageW  = ps.getWidth();
        float pageH  = ps.getHeight();
        float margin = 28f;
        float cw     = pageW - margin * 2;   // usable width

        PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());

        // ============================================================
        // 1. TIÊU ĐỀ công ty (căn giữa)
        // ============================================================
        float y = pageH - margin;

        drawCenteredText(canvas, fontBold, 9f, "CÔNG TY CỔ PHẦN VẬN TẢI", BLACK, margin, pageW, y);
        y -= 14;
        drawCenteredText(canvas, fontBold, 9f, "ĐƯỜNG SẮT VIỆT NAM", BLACK, margin, pageW, y);
        y -= 16;
        drawCenteredText(canvas, fontBold, 12f, "THẺ LÊN TÀU HỎA / BOARDING PASS", BLACK, margin, pageW, y);
        y -= 10;

        // Đường kẻ ngang dưới tiêu đề
        drawHLine(canvas, margin, pageW - margin, y, 0.5f, LGRAY);
        y -= 16;

        // ============================================================
        // 2. BARCODE GIẢ (dải hình chữ nhật mô phỏng barcode)
        // ============================================================
        float bcH = 32f;
        drawBarcodeStripes(canvas, margin + 10, y - bcH, cw - 20, bcH, maVe);
        y -= (bcH + 14);

        // Đường kẻ ngang
        drawHLine(canvas, margin, pageW - margin, y, 0.5f, LGRAY);
        y -= 14;

        // ============================================================
        // 3. MÃ VÉ
        // ============================================================
        drawCenteredText(canvas, fontPlain, 8.5f,
            "Mã vé / TicketID:  " + maVe, BLACK, margin, pageW, y);
        y -= 20;

        // ============================================================
        // 4. GA ĐI  ←→  GA ĐẾN (2 cột to)
        // ============================================================
        float col1X = margin;
        float col2X = pageW / 2f;
        float colW  = (pageW / 2f) - margin;

        canvas.setFillColor(GRAY);
        canvas.beginText();
        canvas.setFontAndSize(fontPlain, 8f);
        canvas.moveText(col1X, y);
        canvas.showText("Ga đi");
        canvas.endText();

        canvas.beginText();
        canvas.setFontAndSize(fontPlain, 8f);
        canvas.moveText(col2X, y);
        canvas.showText("Ga đến");
        canvas.endText();
        y -= 14;

        // Tên ga to, đậm
        String gaKH_display = gaKhoiHanh.isEmpty() ? "N/A" : gaKhoiHanh.toUpperCase();
        String gaDen_display = gaDen.isEmpty() ? "N/A" : gaDen.toUpperCase();

        canvas.setFillColor(BLACK);
        canvas.beginText();
        canvas.setFontAndSize(fontBold, 14f);
        canvas.moveText(col1X, y);
        canvas.showText(gaKH_display);
        canvas.endText();

        canvas.beginText();
        canvas.setFontAndSize(fontBold, 14f);
        canvas.moveText(col2X, y);
        canvas.showText(gaDen_display);
        canvas.endText();
        y -= 16;

        // Đường kẻ phân cách
        drawHLine(canvas, margin, pageW - margin, y, 0.4f, LGRAY);
        y -= 14;

        // ============================================================
        // 5. BẢNG THÔNG TIN (label - value pairs)
        // ============================================================
        float labelX = margin;
        float valueX = margin + 120f;
        float lineH  = 17f;

        // Tính format tiền (dấu chấm phân ngàn VN)
        java.text.DecimalFormat dfMoney = new java.text.DecimalFormat("#,##0");
        dfMoney.setDecimalFormatSymbols(
            new java.text.DecimalFormatSymbols(new java.util.Locale("vi", "VN")));

        double tongGiam = 0;
        for (DAO_Ve.KhuyenMaiInfo km : dsKM) tongGiam += km.tienGiamThucTe;
        double giaSau = giaVeRaw - tongGiam;

        String[][] rows = {
            { "Tàu / Train:",          tenTau.isEmpty()      ? "N/A" : tenTau      },
            { "Ngày đi / Date:",        ngayDi.isEmpty()      ? "N/A" : ngayDi      },
            { "Giờ đi / Time:",         gioDi.isEmpty()       ? "N/A" : gioDi       },
            { "Toa / Coach:  " + maToa, "Chỗ / Seat:  " + viTri                    },
            { "Loại chỗ / Class:",      loaiChoClass.isEmpty() ? loaiVe : loaiChoClass },
            { "Loại vé / Ticket:",      loaiVe.isEmpty()      ? "N/A" : loaiVe      },
            { "Họ tên / Name:",         tenKH.isEmpty()       ? "xxxxxxxx" : tenKH  },
            { "",                       ""                                           },
            { "Giấy tờ / Passport:",    cccd.isEmpty()        ? "xxxxxxxx" : cccd   },
        };

        for (String[] row : rows) {
            if (row[0].isEmpty() && row[1].isEmpty()) { y -= 6; continue; }

            if (row[0].startsWith("Toa / Coach:")) {
                canvas.setFillColor(GRAY);
                canvas.beginText();
                canvas.setFontAndSize(fontPlain, 8.5f);
                canvas.moveText(labelX, y);
                canvas.showText(row[0]);
                canvas.endText();

                canvas.setFillColor(BLACK);
                canvas.beginText();
                canvas.setFontAndSize(fontBold, 8.5f);
                canvas.moveText(col2X, y);
                canvas.showText(row[1]);
                canvas.endText();
            } else {
                canvas.setFillColor(GRAY);
                canvas.beginText();
                canvas.setFontAndSize(fontPlain, 8.5f);
                canvas.moveText(labelX, y);
                canvas.showText(row[0]);
                canvas.endText();

                canvas.setFillColor(BLACK);
                canvas.beginText();
                canvas.setFontAndSize(fontBold, 8.5f);
                canvas.moveText(valueX, y);
                canvas.showText(row[1]);
                canvas.endText();
            }
            y -= lineH;
        }

        // ---- Khuyến mãi (nếu có) ----
        DeviceRgb GREEN = new DeviceRgb(0x16, 0x63, 0x34);
        DeviceRgb RED   = new DeviceRgb(0xDC, 0x26, 0x26);
        if (!dsKM.isEmpty()) {
            // Label "Khuyến mãi:"
            canvas.setFillColor(GRAY);
            canvas.beginText();
            canvas.setFontAndSize(fontPlain, 8.5f);
            canvas.moveText(labelX, y);
            canvas.showText("Khuyến mãi:");
            canvas.endText();
            // Tên KM đầu tiên
            DAO_Ve.KhuyenMaiInfo km0 = dsKM.get(0);
            String loai0 = km0.loaiKM.equals("GIAM_PHAN_TRAM")
                ? String.format("Giảm %.0f%%", km0.giaTri)
                : String.format("Giảm %s VND", dfMoney.format((long)km0.giaTri));
            canvas.setFillColor(new DeviceRgb(0x03, 0x69, 0xA1));
            canvas.beginText();
            canvas.setFontAndSize(fontBold, 8.5f);
            canvas.moveText(valueX, y);
            canvas.showText(km0.tenKM + "  (" + loai0 + ")");
            canvas.endText();
            y -= lineH;
            // Các KM tiếp theo
            for (int i = 1; i < dsKM.size(); i++) {
                DAO_Ve.KhuyenMaiInfo kmi = dsKM.get(i);
                String loaii = kmi.loaiKM.equals("GIAM_PHAN_TRAM")
                    ? String.format("Giảm %.0f%%", kmi.giaTri)
                    : String.format("Giảm %s VND", dfMoney.format((long)kmi.giaTri));
                canvas.setFillColor(new DeviceRgb(0x03, 0x69, 0xA1));
                canvas.beginText();
                canvas.setFontAndSize(fontBold, 8.5f);
                canvas.moveText(valueX, y);
                canvas.showText(kmi.tenKM + "  (" + loaii + ")");
                canvas.endText();
                y -= lineH;
            }
            // Tiết kiệm
            canvas.setFillColor(GREEN);
            canvas.beginText();
            canvas.setFontAndSize(fontPlain, 8f);
            canvas.moveText(valueX, y);
            canvas.showText("Tiết kiệm: -" + dfMoney.format((long)tongGiam) + " VND");
            canvas.endText();
            y -= lineH;
        }

        // ---- Giá / Price ----
        canvas.setFillColor(GRAY);
        canvas.beginText();
        canvas.setFontAndSize(fontPlain, 8.5f);
        canvas.moveText(labelX, y);
        canvas.showText("Giá / Price:");
        canvas.endText();

        if (tongGiam > 0) {
            // Giá gốc gạch ngang
            String gocStr = dfMoney.format((long)giaVeRaw) + " VND";
            canvas.setFillColor(GRAY);
            canvas.beginText();
            canvas.setFontAndSize(fontPlain, 8f);
            canvas.moveText(valueX, y);
            canvas.showText(gocStr);
            canvas.endText();
            // Gạch ngang thủ công
            float gocW = fontPlain.getWidth(gocStr, 8f);
            canvas.setStrokeColor(GRAY);
            canvas.setLineWidth(0.6f);
            canvas.moveTo(valueX, y + 3.5f);
            canvas.lineTo(valueX + gocW, y + 3.5f);
            canvas.stroke();
            y -= lineH;
            // Giá sau giảm (đậm, đỏ)
            canvas.setFillColor(RED);
            canvas.beginText();
            canvas.setFontAndSize(fontBold, 10f);
            canvas.moveText(valueX, y);
            canvas.showText(dfMoney.format((long)giaSau) + " VND");
            canvas.endText();
        } else {
            canvas.setFillColor(BLACK);
            canvas.beginText();
            canvas.setFontAndSize(fontBold, 8.5f);
            canvas.moveText(valueX, y);
            canvas.showText(dfMoney.format((long)giaVeRaw) + " VND");
            canvas.endText();
        }
        y -= lineH;

        // ============================================================
        // 6. ĐƯỜNG CHẤM PHÂN CÁCH (phần tear-off cuống vé)
        // ============================================================
        y -= 6;
        drawDashedHLine(canvas, margin, pageW - margin, y, LGRAY);
        y -= 14;

        // ============================================================
        // 7. CUỐNG VÉ — thông tin gọn
        // ============================================================
        drawCenteredText(canvas, fontBold, 7.5f,
            maVe + "   |   " + tenTau + "   |   " + ngayDi + "   " + gioDi
            + "   |   Toa " + maToa + "  Ghế " + viTri,
            GRAY, margin, pageW, y);
        y -= 11;

        // Ngày in
        String ngayIn = "Ngày in: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        canvas.setFillColor(LGRAY);
        canvas.beginText();
        canvas.setFontAndSize(fontPlain, 7f);
        canvas.moveText(margin, y);
        canvas.showText(ngayIn);
        canvas.endText();

        canvas.setFillColor(LGRAY);
        canvas.beginText();
        canvas.setFontAndSize(fontPlain, 7f);
        String thanks = "Cảm ơn quý khách! / Thank you!";
        float tw = fontPlain.getWidth(thanks, 7f);
        canvas.moveText(pageW - margin - tw, y);
        canvas.showText(thanks);
        canvas.endText();

        // ============================================================
        // 8. VIỀN NGOÀI toàn trang
        // ============================================================
        canvas.setStrokeColor(LGRAY);
        canvas.setLineWidth(0.8f);
        canvas.rectangle(margin - 8, margin - 8,
                         pageW - (margin - 8) * 2,
                         pageH - (margin - 8) * 2);
        canvas.stroke();

        canvas.release();
        doc.close();
    }

    // ====== Font helper — tìm font TTF hỗ trợ tiếng Việt theo OS ======
    /**
     * Tự động tìm font TTF hỗ trợ Unicode/tiếng Việt theo hệ điều hành.
     * Ưu tiên: Arial (Windows) → Helvetica Neue / SF (macOS) → DejaVu (Linux).
     * Nếu không tìm thấy bất kỳ file nào thì fallback sang Helvetica built-in
     * (sẽ không hiển thị được dấu tiếng Việt nhưng không crash).
     */
    private PdfFont loadVietnameseFont(boolean bold) throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase();

        // Danh sách đường dẫn ưu tiên theo OS
        String[][] candidates;
        if (os.contains("win")) {
            candidates = new String[][] {
                { "C:/Windows/Fonts/arial.ttf",    "C:/Windows/Fonts/arialbd.ttf"    },
                { "C:/Windows/Fonts/tahoma.ttf",   "C:/Windows/Fonts/tahomabd.ttf"   },
                { "C:/Windows/Fonts/times.ttf",    "C:/Windows/Fonts/timesbd.ttf"    },
                { "C:/Windows/Fonts/calibri.ttf",  "C:/Windows/Fonts/calibrib.ttf"   },
            };
        } else if (os.contains("mac")) {
            candidates = new String[][] {
                { "/Library/Fonts/Arial.ttf",                    "/Library/Fonts/Arial Bold.ttf"          },
                { "/System/Library/Fonts/Supplemental/Arial.ttf","/System/Library/Fonts/Supplemental/Arial Bold.ttf" },
                { "/Library/Fonts/Times New Roman.ttf",          "/Library/Fonts/Times New Roman Bold.ttf"},
            };
        } else {
            // Linux / other
            candidates = new String[][] {
                { "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",      "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"      },
                { "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf", "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf" },
                { "/usr/share/fonts/truetype/freefont/FreeSans.ttf",      "/usr/share/fonts/truetype/freefont/FreeSansBold.ttf"       },
            };
        }

        int idx = bold ? 1 : 0;
        for (String[] pair : candidates) {
            File f = new File(pair[idx]);
            // Nếu file bold không tồn tại thì thử file regular
            if (!f.exists() && idx == 1) f = new File(pair[0]);
            if (f.exists()) {
                return PdfFontFactory.createFont(
                    f.getAbsolutePath(),
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
        }

        // Fallback: Helvetica (không có dấu tiếng Việt nhưng không crash)
        return PdfFontFactory.createFont(
            bold ? com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD
                 : com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
    }

    // ====== PDF drawing helpers ======

    private void drawCenteredText(PdfCanvas c, PdfFont font, float size, String text,
                                   DeviceRgb color, float marginL, float pageW, float y) throws Exception {
        float tw = font.getWidth(text, size);
        float x  = marginL + (pageW - marginL * 2 - tw) / 2f;
        c.setFillColor(color);
        c.beginText();
        c.setFontAndSize(font, size);
        c.moveText(x, y);
        c.showText(text);
        c.endText();
    }

    private void drawHLine(PdfCanvas c, float x1, float x2, float y, float lw, DeviceRgb color) {
        c.setStrokeColor(color);
        c.setLineWidth(lw);
        c.moveTo(x1, y);
        c.lineTo(x2, y);
        c.stroke();
    }

    private void drawDashedHLine(PdfCanvas c, float x1, float x2, float y, DeviceRgb color) {
        c.setStrokeColor(color);
        c.setLineWidth(0.5f);
        c.setLineDash(3f, 3f, 0f);
        c.moveTo(x1, y);
        c.lineTo(x2, y);
        c.stroke();
        c.setLineDash(0f); // reset
    }

    /** Vẽ dải barcode giả từ maVe — xen kẽ thanh đen dày/mỏng */
    private void drawBarcodeStripes(PdfCanvas c, float x, float y, float w, float h, String seed) {
        // Sinh chuỗi bit ngẫu nhiên nhưng tái hiện được từ maVe
        long hash = seed.hashCode() & 0xFFFFFFFFL;
        java.util.Random rng = new java.util.Random(hash);
        float cx = x;
        DeviceRgb BLACK = new DeviceRgb(0, 0, 0);
        DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
        // Nền trắng
        c.setFillColor(WHITE);
        c.rectangle(x, y, w, h);
        c.fill();
        // Thanh đen
        while (cx < x + w - 1) {
            float stripeW = rng.nextBoolean() ? 1.5f : 0.8f;
            float gap     = rng.nextFloat() * 2.2f + 0.6f;
            if (cx + stripeW > x + w) break;
            c.setFillColor(BLACK);
            c.rectangle(cx, y, stripeW, h);
            c.fill();
            cx += stripeW + gap;
        }
    }

    // ================= CONFIRM HOÀN VÉ (với tính phí hoàn) =================
    private void confirmHoanVe(String maVe, JDialog chiTietDialog) {
        // Tính tiền hoàn trước khi hiện dialog
        double[] info = daoVe.tinhTienHoan(maVe);
        // info: [tienHoan, phiHoan, phanTramHoan, gioConLai, tienThanhToan]
        double tienHoan       = info[0];
        double phiHoan        = info[1];
        double phanTramHoan   = info[2];
        double gioConLai      = info[3];
        double tienThanhToan  = info[4];

        // Nếu không hoàn được (< 4 giờ)
        if (phanTramHoan == 0 && gioConLai >= 0) {
            JOptionPane.showMessageDialog(chiTietDialog,
                "<html>Không thể hoàn vé <b>" + maVe + "</b>!<br>"
              + String.format("Tàu khởi hành trong <b>%.1f giờ nữa</b> (dưới 4 giờ).<br>", gioConLai)
              + "Chính sách: không hoàn vé khi còn dưới 4 giờ khởi hành.</html>",
                "Không thể hoàn vé", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Xây dựng dialog xác nhận
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xác nhận hoàn vé", true);
        dlg.setLayout(new BorderLayout(0, 0));
        dlg.setResizable(false);

        // ---- Phần thông tin hoàn ----
        JPanel pInfo = new JPanel(new BorderLayout(0, 0));
        pInfo.setBackground(BG_CARD);

        // Top: cảnh báo màu vàng
        JPanel pWarn = new JPanel(new BorderLayout(10, 0));
        pWarn.setBackground(new Color(0xFFF3CD));
        pWarn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xFFC107), 1),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        JLabel warnMsg = new JLabel(
            "<html>Bạn có chắc muốn hoàn vé <b>" + maVe + "</b>?<br>"
          + "Vé sẽ chuyển sang <b>DAHOAN</b> và ghế sẽ được trả lại.</html>");
        warnMsg.setFont(F_CELL);
        warnMsg.setForeground(new Color(0x856404));
        pWarn.add(warnMsg, BorderLayout.CENTER);

        // Middle: chi tiết tính tiền hoàn
        JPanel pDetail = new JPanel(new GridBagLayout());
        pDetail.setBackground(BG_CARD);
        pDetail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(16, 24, 16, 24)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 5, 8);

        int dRow = 0;

        // Tiêu đề nhỏ
        gc.gridy = dRow++; gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 1;
        JLabel lblChinhSach = new JLabel("Chi tiết hoàn tiền:");
        lblChinhSach.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblChinhSach.setForeground(ACCENT);
        pDetail.add(lblChinhSach, gc);
        gc.gridwidth = 1;

        // Dòng: giờ còn lại
        String chinhSachStr;
        if      (gioConLai >= 72) chinhSachStr = String.format("Còn %.1f giờ → Hoàn %.0f%%", gioConLai, phanTramHoan);
        else if (gioConLai >= 24) chinhSachStr = String.format("Còn %.1f giờ → Hoàn %.0f%%", gioConLai, phanTramHoan);
        else                       chinhSachStr = String.format("Còn %.1f giờ → Hoàn %.0f%%", gioConLai, phanTramHoan);

        String[][] detailRows = {
            { "Thời gian còn lại:",       chinhSachStr                                          },
            { "Số tiền khách đã trả:",    String.format("%,.0f VND", tienThanhToan)             },
            { "Phí hoàn vé (giữ lại):",   String.format("%,.0f VND  (%.0f%%)", phiHoan, 100 - phanTramHoan) },
        };

        for (String[] dr : detailRows) {
            gc.gridy = dRow; gc.gridx = 0; gc.weightx = 0.5;
            JLabel k = new JLabel(dr[0]);
            k.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            k.setForeground(TEXT_MID);
            pDetail.add(k, gc);
            gc.gridx = 1; gc.weightx = 0.5;
            JLabel v = new JLabel(dr[1]);
            v.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            v.setForeground(TEXT_DARK);
            pDetail.add(v, gc);
            dRow++;
        }

        // Separator
        gc.gridy = dRow++; gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 1;
        gc.insets = new Insets(2, 0, 2, 0);
        JSeparator sepD = new JSeparator(); sepD.setForeground(BORDER);
        pDetail.add(sepD, gc);
        gc.gridwidth = 1; gc.insets = new Insets(5, 0, 5, 8);

        // Dòng TIỀN HOÀN nổi bật
        gc.gridy = dRow; gc.gridx = 0; gc.weightx = 0.5;
        JLabel lblHoanKey = new JLabel("Tiền hoàn cho khách:");
        lblHoanKey.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHoanKey.setForeground(new Color(0x065F46));
        pDetail.add(lblHoanKey, gc);
        gc.gridx = 1; gc.weightx = 0.5;
        JLabel lblHoanVal = new JLabel(String.format("%,.0f VND", tienHoan));
        lblHoanVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHoanVal.setForeground(new Color(0x16A34A));
        pDetail.add(lblHoanVal, gc);

        pInfo.add(pWarn,   BorderLayout.NORTH);
        pInfo.add(pDetail, BorderLayout.CENTER);

        // ---- Chính sách hoàn vé (note nhỏ) ----
        JLabel lblNote = new JLabel(
            "<html><font color='#6B7280' size='2'>"
          + "Chính sách: ≥72h → hoàn 90% | 24–72h → 75% | 4–24h → 50% | &lt;4h → không hoàn"
          + "</font></html>");
        lblNote.setBorder(BorderFactory.createEmptyBorder(0, 24, 10, 24));
        pInfo.add(lblNote, BorderLayout.SOUTH);

        // ---- Footer buttons ----
        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pBtn.setBackground(BG_CARD);
        pBtn.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JButton btnCancel  = makeBtn("Hủy",       BtnStyle.SECONDARY);
        JButton btnConfirm = makeBtn("Xác nhận hoàn vé", BtnStyle.WARNING);
        btnCancel.setPreferredSize(new Dimension(130, 38));
        btnConfirm.setPreferredSize(new Dimension(190, 38));

        btnCancel.addActionListener(e -> dlg.dispose());
        btnConfirm.addActionListener(e -> {
            dlg.dispose();
            boolean ok = daoVe.hoanVe(maVe);
            if (ok) {
                chiTietDialog.dispose();
                JOptionPane.showMessageDialog(TAB_TraCuuVe.this,
                    String.format("<html>Hoàn vé <b>%s</b> thành công!<br>"
                        + "Số tiền hoàn trả cho khách: <b>%,.0f VND</b><br>"
                        + "Ghế đã được trả lại.</html>", maVe, tienHoan),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadData(txtMaVe != null ? txtMaVe.getText().trim() : null);
            } else {
                JOptionPane.showMessageDialog(TAB_TraCuuVe.this,
                    "Không thể hoàn vé!\nVé không ở trạng thái 'CHUASUDUNG' hoặc có lỗi CSDL.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        pBtn.add(btnCancel);
        pBtn.add(btnConfirm);

        dlg.add(pInfo, BorderLayout.CENTER);
        dlg.add(pBtn,  BorderLayout.SOUTH);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(460, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ================= HELPERS =================
    private String trangThaiToDisplay(String raw) {
        switch (raw.toUpperCase()) {
            case "CHUASUDUNG": return "Chưa sử dụng";
            case "DASUDUNG":   return "Đã sử dụng";
            case "HETHAN":     return "Hết hạn";
            case "DAHOAN":     return "Đã hoàn";
            default:           return raw;
        }
    }

    // ================= UI BUILDERS =================
    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 12, 0));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        bar.add(createStatCard("TỔNG SỐ VÉ",      lblTongVe,     ACCENT));
        bar.add(createStatCard("VÉ CHƯA SỬ DỤNG", lblChuaSuDung, new Color(0x16A34A)));
        bar.add(createStatCard("VÉ ĐÃ SỬ DỤNG",   lblDaSuDung,   new Color(0x0369A1)));
        bar.add(createStatCard("VÉ ĐÃ HOÀN",      lblDaHoan,     new Color(0xD97706)));
        return bar;
    }

    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel lbl = new JLabel("TRA CỨU VÀ HOÀN VÉ");
        lbl.setFont(F_TITLE); lbl.setForeground(ACCENT);
        pnl.add(lbl, BorderLayout.WEST);
        return pnl;
    }

    private JPanel buildFilterCard() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        card.setBackground(BG_CARD);
        card.setBorder(new LineBorder(BORDER, 1, true));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        txtMaVe = new JTextField(18);
        txtMaVe.setFont(F_CELL);
        txtMaVe.setPreferredSize(new Dimension(200, 32));

        JButton btnTim    = makeBtn("Tìm",      BtnStyle.PRIMARY);
        JButton btnLamMoi = makeBtn("Làm mới",  BtnStyle.SECONDARY);
        btnTim.setPreferredSize(new Dimension(90, 32));
        btnLamMoi.setPreferredSize(new Dimension(90, 32));

        btnTim.addActionListener(e -> loadData(txtMaVe.getText().trim()));
        txtMaVe.addActionListener(e -> loadData(txtMaVe.getText().trim()));
        btnLamMoi.addActionListener(e -> { txtMaVe.setText(""); loadData(null); });

        JLabel lbl = new JLabel("Mã vé:");
        lbl.setFont(F_LABEL); lbl.setForeground(TEXT_MID);

        card.add(lbl); card.add(txtMaVe); card.add(btnTim); card.add(btnLamMoi);
        return card;
    }

    private JPanel buildDanhSachCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(new LineBorder(BORDER, 1, true));

        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));
        JLabel lblTitle = new JLabel("Danh sách vé");
        lblTitle.setFont(F_LABEL); lblTitle.setForeground(TEXT_DARK);
        JLabel lblHint = new JLabel("  Double-click để xem chi tiết & in vé PDF");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(TEXT_MID);
        bar.add(lblTitle, BorderLayout.WEST);
        bar.add(lblHint,  BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        styleScrollBar(scroll.getVerticalScrollBar());
        styleScrollBar(scroll.getHorizontalScrollBar());

        card.add(bar,    BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ================= COMPONENT FACTORIES =================
    private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(14, 20, 14, 20)));
        JLabel lblT = new JLabel(title);
        lblT.setForeground(COLOR_MUTED);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblValue.setForeground(accent);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        p.add(lblT,     BorderLayout.NORTH);
        p.add(lblValue, BorderLayout.CENTER);
        return p;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c;
                switch (style) {
                    case DANGER:    c = getModel().isRollover() ? BTN_RED_HVR : BTN_RED; break;
                    case WARNING:   c = isEnabled()
                                        ? (getModel().isRollover() ? BTN_ORANGE_HVR : BTN_ORANGE)
                                        : new Color(0xBDBDBD); break;
                    case SECONDARY: c = getModel().isRollover() ? new Color(0xE5ECF6) : Color.WHITE; break;
                    default:        c = getModel().isRollover() ? ACCENT_HVR : ACCENT;
                }
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (style == BtnStyle.SECONDARY) {
                    g2.setColor(BORDER);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
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
            protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDragging ? new Color(0x1A5EAB) : new Color(0x5B9BD5));
                g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 8, 8);
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
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setOpaque(true);
            l.setBackground(new Color(0x1A5EAB));
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 6));
            return l;
        }
    }
}