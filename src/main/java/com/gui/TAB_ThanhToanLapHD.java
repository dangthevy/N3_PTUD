//package com.gui;
//
//import com.connectDB.ConnectDB;
//import com.toedter.calendar.JDateChooser;
//
//import javax.swing.*;
//import javax.swing.border.*;
//import javax.swing.plaf.basic.BasicScrollBarUI;
//import javax.swing.table.*;
//import java.awt.*;
//import java.io.FileOutputStream;
//import java.sql.*;
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
//
//import com.itextpdf.text.Document;
//import com.itextpdf.text.Paragraph;
//import com.itextpdf.text.pdf.PdfWriter;
//import com.itextpdf.text.FontFactory;
//
//public class TAB_ThanhToanLapHD extends JPanel {
//
//    // ================= COLOR (đồng bộ TAB_QLNhanVien) =================
//    private static final Color BG_PAGE    = new Color(0xF4F7FB);
//    private static final Color BG_CARD    = Color.WHITE;
//    private static final Color ACCENT     = new Color(0x1A5EAB);
//    private static final Color ACCENT_HVR = new Color(0x2270CC);
//    private static final Color TEXT_DARK  = new Color(0x1E2B3C);
//    private static final Color TEXT_MID   = new Color(0x5A6A7D);
//    private static final Color TEXT_LIGHT = new Color(0xA0AEC0);
//    private static final Color BORDER     = new Color(0xE2EAF4);
//    private static final Color ROW_ALT    = new Color(0xF7FAFF);
//    private static final Color ROW_SEL    = new Color(0xDDEEFF);
//    private static final Color BTN_RED    = new Color(0xC0392B);
//    private static final Color BTN_RED_HVR= new Color(0xE74C3C);
//    private static final Color BTN_GREEN  = new Color(0x16A34A);
//    private static final Color BTN_GREEN_HVR = new Color(0x22C55E);
//    private static final Color COLOR_BORDER     = new Color(226, 232, 240);
//    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
//
//    // ================= FONT (đồng bộ TAB_QLNhanVien) =================
//    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
//    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
//    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);
//
//    private enum BtnStyle { PRIMARY, SECONDARY, DANGER, SUCCESS }
//
//    // ─── Model / Table ────────────────────────────────────────────────────────
//    private DefaultTableModel modelHD, modelCT;
//    private JTable tableHD, tableCT;
//
//    // ─── Filter ───────────────────────────────────────────────────────────────
//    private JComboBox<Object> cbNhanVienLoc;
//    private JDateChooser dateTuNgay, dateToiNgay;
//    private JTextField txtTimKiemTenKH;
//
//    // ─── Chi tiết labels ──────────────────────────────────────────────────────
//    private JLabel lblMaHDVal, lblNgayLapVal, lblNhanVienVal,
//                   lblKhachHangVal, lblTongGiamVal, lblTongTienVal;
//    private JLabel lblTongHD = new JLabel("0");
//
//    private String currentMaHD = "";
//    private final DecimalFormat df  = new DecimalFormat("#,### VNĐ");
//    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//
//    public TAB_ThanhToanLapHD() {
//        setLayout(new BorderLayout(0, 16));
//        setBackground(BG_PAGE);
//        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
//
//        // Khởi tạo models
//        modelHD = new DefaultTableModel(new String[]{"Mã HD", "Ngày lập", "Khách hàng"}, 0) {
//            public boolean isCellEditable(int r, int c) { return false; }
//        };
//        modelCT = new DefaultTableModel(
//                new String[]{"Mã Vé", "Loại", "Giá Gốc", "Giảm", "Thành Tiền"}, 0) {
//            public boolean isCellEditable(int r, int c) { return false; }
//        };
//        tableHD = buildStyledTable(modelHD);
//        tableCT = buildStyledTable(modelCT);
//
//        // NORTH xếp dọc
//        JPanel topPanel = new JPanel();
//        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
//        topPanel.setOpaque(false);
//        topPanel.add(buildHeader());
//        topPanel.add(Box.createVerticalStrut(10));
//        topPanel.add(buildStatsBar());
//        topPanel.add(Box.createVerticalStrut(10));
//        topPanel.add(buildFilterCard());
//
//        add(topPanel,         BorderLayout.NORTH);
//        add(buildSplitPanel(), BorderLayout.CENTER);
//
//        loadNhanVienToCombo();
//        loadDanhSachHoaDon();
//    }
//
//    // ─── HEADER ───────────────────────────────────────────────────────────────
//    private JPanel buildHeader() {
//        JPanel pnl = new JPanel(new BorderLayout());
//        pnl.setOpaque(false);
//        JLabel lbl = new JLabel("QUẢN LÝ & TRA CỨU HÓA ĐƠN");
//        lbl.setFont(F_TITLE);
//        lbl.setForeground(ACCENT);
//        pnl.add(lbl, BorderLayout.WEST);
//        return pnl;
//    }
//
//    // ─── STATS BAR ────────────────────────────────────────────────────────────
//    private JPanel buildStatsBar() {
//        JPanel bar = new JPanel(new GridLayout(1, 1, 12, 0));
//        bar.setOpaque(false);
//        bar.add(createStatCard("TỔNG HÓA ĐƠN", lblTongHD, ACCENT));
//        return bar;
//    }
//
//    // ─── FILTER CARD ──────────────────────────────────────────────────────────
//    private JPanel buildFilterCard() {
//        JPanel card = buildCard(new FlowLayout(FlowLayout.LEFT, 12, 12));
//
//        txtTimKiemTenKH = makeField("Tên khách hàng...");
//        txtTimKiemTenKH.addKeyListener(new java.awt.event.KeyAdapter() {
//            public void keyReleased(java.awt.event.KeyEvent e) { loadDanhSachHoaDon(); }
//        });
//
//        dateTuNgay = new JDateChooser();
//        dateTuNgay.setPreferredSize(new Dimension(120, 34));
//        dateTuNgay.setFont(F_CELL);
//        dateTuNgay.addPropertyChangeListener("date", e -> loadDanhSachHoaDon());
//
//        dateToiNgay = new JDateChooser();
//        dateToiNgay.setPreferredSize(new Dimension(120, 34));
//        dateToiNgay.setFont(F_CELL);
//        dateToiNgay.addPropertyChangeListener("date", e -> loadDanhSachHoaDon());
//
//        cbNhanVienLoc = new JComboBox<>();
//        cbNhanVienLoc.setFont(F_CELL);
//        cbNhanVienLoc.setPreferredSize(new Dimension(150, 34));
//        cbNhanVienLoc.addActionListener(e -> loadDanhSachHoaDon());
//
//        JButton btnXoaLoc = makeBtn("Xóa bộ lọc", BtnStyle.SECONDARY);
//        JButton btnLamMoi  = makeBtn("Làm mới",    BtnStyle.PRIMARY);
//
//        btnXoaLoc.addActionListener(e -> xoaBoLoc());
//        btnLamMoi.addActionListener(e -> {
//            xoaBoLoc();
//            JOptionPane.showMessageDialog(this, "Đã cập nhật danh sách hóa đơn mới nhất!");
//        });
//
//        card.add(makeLabel("Khách hàng:")); card.add(txtTimKiemTenKH);
//        card.add(makeLabel("Từ:"));         card.add(dateTuNgay);
//        card.add(makeLabel("Tới:"));        card.add(dateToiNgay);
//        card.add(makeLabel("Nhân viên:"));  card.add(cbNhanVienLoc);
//        card.add(btnXoaLoc);
//        card.add(btnLamMoi);
//        return card;
//    }
//
//    // ─── SPLIT PANEL: danh sách HD | chi tiết ─────────────────────────────────
//    private JSplitPane buildSplitPanel() {
//        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
//                buildDanhSachCard(), buildChiTietCard());
//        split.setDividerLocation(400);
//        split.setDividerSize(8);
//        split.setBorder(null);
//        split.setBackground(BG_PAGE);
//        return split;
//    }
//
//    // ─── CARD DANH SÁCH HÓA ĐƠN ──────────────────────────────────────────────
//    private JPanel buildDanhSachCard() {
//        JPanel card = buildCard(new BorderLayout());
//
//        // Action bar
//        JPanel bar = new JPanel(new BorderLayout());
//        bar.setOpaque(false);
//        bar.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));
//        JLabel lbl = new JLabel("Danh sách hóa đơn");
//        lbl.setFont(F_LABEL);
//        lbl.setForeground(TEXT_DARK);
//        bar.add(lbl, BorderLayout.WEST);
//
//        // Table body
//        JSeparator sep = new JSeparator();
//        sep.setForeground(BORDER);
//
//        tableHD.getSelectionModel().addListSelectionListener(e -> {
//            if (!e.getValueIsAdjusting()) {
//                int row = tableHD.getSelectedRow();
//                if (row != -1) {
//                    currentMaHD = tableHD.getValueAt(row, 0).toString();
//                    hienThiChiTiet(currentMaHD);
//                }
//            }
//        });
//
//        JScrollPane scroll = new JScrollPane(tableHD);
//        scroll.setBorder(BorderFactory.createEmptyBorder());
//        scroll.getViewport().setBackground(BG_CARD);
//        styleScrollBar(scroll.getVerticalScrollBar());
//
//        JPanel body = new JPanel(new BorderLayout());
//        body.setOpaque(false);
//        body.add(sep,    BorderLayout.NORTH);
//        body.add(scroll, BorderLayout.CENTER);
//
//        card.add(bar,  BorderLayout.NORTH);
//        card.add(body, BorderLayout.CENTER);
//        return card;
//    }
//
//    // ─── CARD CHI TIẾT HÓA ĐƠN ───────────────────────────────────────────────
//    private JPanel buildChiTietCard() {
//        JPanel card = buildCard(new BorderLayout(0, 0));
//
//        // ── Header xanh đậm (giống table header NV) ──
//        JPanel pnlHdr = new JPanel(new GridLayout(2, 1, 0, 2));
//        pnlHdr.setBackground(ACCENT);
//        pnlHdr.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
//        JLabel lblT = new JLabel("CHI TIẾT HÓA ĐƠN");
//        lblT.setFont(new Font("Segoe UI", Font.BOLD, 14));
//        lblT.setForeground(Color.WHITE);
//        JLabel lblS = new JLabel("Hệ Thống Bán Vé Tàu Hỏa");
//        lblS.setFont(new Font("Segoe UI", Font.PLAIN, 11));
//        lblS.setForeground(new Color(0xA8C8EE));
//        pnlHdr.add(lblT);
//        pnlHdr.add(lblS);
//
//        // ── Info rows ──
//        JPanel pnlInfo = new JPanel(new GridBagLayout());
//        pnlInfo.setBackground(BG_CARD);
//        pnlInfo.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.fill   = GridBagConstraints.HORIZONTAL;
//        gbc.insets = new Insets(4, 4, 4, 8);
//
//        lblMaHDVal      = infoVal("-"); lblMaHDVal.setFont(F_LABEL);
//        lblNgayLapVal   = infoVal("-");
//        lblNhanVienVal  = infoVal("-");
//        lblKhachHangVal = infoVal("-");
//        lblTongGiamVal  = infoVal("-");
//        lblTongTienVal  = infoVal("0 VNĐ");
//        lblTongTienVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
//        lblTongTienVal.setForeground(new Color(0xDC2626));
//
//        addInfoRow(pnlInfo, "Mã hóa đơn:",   lblMaHDVal,      0, gbc);
//        addInfoRow(pnlInfo, "Ngày lập:",      lblNgayLapVal,   1, gbc);
//        addInfoRow(pnlInfo, "Nhân viên:",     lblNhanVienVal,  2, gbc);
//        addInfoRow(pnlInfo, "Khách hàng:",    lblKhachHangVal, 3, gbc);
//        addInfoRow(pnlInfo, "Tổng giảm giá:", lblTongGiamVal,  4, gbc);
//
//        // ── Table vé ──
//        JScrollPane scrollCT = new JScrollPane(tableCT);
//        scrollCT.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
//        scrollCT.getViewport().setBackground(BG_CARD);
//        styleScrollBar(scrollCT.getVerticalScrollBar());
//
//        // ── Bottom: tổng tiền + nút PDF ──
//        // QUAN TRỌNG: dùng BorderLayout tránh nút văng xuống
//        JPanel pnlBottom = new JPanel(new BorderLayout(0, 10));
//        pnlBottom.setBackground(BG_CARD);
//        pnlBottom.setBorder(BorderFactory.createEmptyBorder(12, 18, 14, 18));
//
//        // Tổng tiền - FlowLayout RIGHT
//        JPanel pnlTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
//        pnlTotal.setBackground(BG_CARD);
//        JLabel lblTotalLbl = new JLabel("THÀNH TIỀN:");
//        lblTotalLbl.setFont(F_LABEL);
//        lblTotalLbl.setForeground(TEXT_MID);
//        pnlTotal.add(lblTotalLbl);
//        pnlTotal.add(lblTongTienVal);
//
//        // Nút PDF - full width
//        JButton btnPDF = makeBtn("XUẤT FILE PDF", BtnStyle.SUCCESS);
//        btnPDF.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
//        btnPDF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
//        btnPDF.addActionListener(e -> xuatHoaDonPDF(currentMaHD));
//
//        pnlBottom.add(pnlTotal, BorderLayout.NORTH);
//        pnlBottom.add(btnPDF,   BorderLayout.SOUTH);
//
//        // ── Ghép trung tâm ──
//        JPanel pnlCenter = new JPanel(new BorderLayout());
//        pnlCenter.setBackground(BG_CARD);
//        pnlCenter.add(pnlInfo,  BorderLayout.NORTH);
//        pnlCenter.add(scrollCT, BorderLayout.CENTER);
//
//        card.add(pnlHdr,    BorderLayout.NORTH);
//        card.add(pnlCenter, BorderLayout.CENTER);
//        card.add(pnlBottom, BorderLayout.SOUTH);
//        return card;
//    }
//
//    // ─── LOGIC ────────────────────────────────────────────────────────────────
//    private void xoaBoLoc() {
//        txtTimKiemTenKH.setText("");
//        dateTuNgay.setDate(null);
//        dateToiNgay.setDate(null);
//        if (cbNhanVienLoc.getItemCount() > 0) cbNhanVienLoc.setSelectedIndex(0);
//        currentMaHD = "";
//        lblMaHDVal.setText("-"); lblNgayLapVal.setText("-");
//        lblNhanVienVal.setText("-"); lblKhachHangVal.setText("-");
//        lblTongGiamVal.setText("-"); lblTongTienVal.setText("0 VNĐ");
//        modelCT.setRowCount(0);
//        loadDanhSachHoaDon();
//    }
//
//    private void hienThiChiTiet(String maHD) {
//        modelCT.setRowCount(0);
//        double tongGiam = 0;
//        try (Connection con = ConnectDB.getConnection()) {
//            PreparedStatement ps = con.prepareStatement(
//                "SELECT h.maHD, h.ngayLap, h.tongTien, n.tenNV, k.tenKH " +
//                "FROM HoaDon h LEFT JOIN NhanVien n ON h.maNV=n.maNV " +
//                "LEFT JOIN KhachHang k ON h.maKH=k.maKH WHERE h.maHD=?");
//            ps.setString(1, maHD);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                lblMaHDVal.setText(rs.getString("maHD"));
//                lblNgayLapVal.setText(sdf.format(rs.getTimestamp("ngayLap")));
//                lblNhanVienVal.setText(rs.getString("tenNV"));
//                String tenKH = rs.getString("tenKH");
//                lblKhachHangVal.setText(tenKH != null ? tenKH : "Khách lẻ");
//                lblTongTienVal.setText(df.format(rs.getDouble("tongTien")));
//            }
//            PreparedStatement ps2 = con.prepareStatement(
//                "SELECT ct.maVe, v.maLoaiVe, ct.tienGoc, ct.tienGiam, ct.thanhTien " +
//                "FROM ChiTietHoaDon ct JOIN Ve v ON ct.maVe=v.maVe WHERE ct.maHD=?");
//            ps2.setString(1, maHD);
//            ResultSet rs2 = ps2.executeQuery();
//            while (rs2.next()) {
//                double tg = rs2.getDouble("tienGiam");
//                tongGiam += tg;
//                modelCT.addRow(new Object[]{
//                    rs2.getString("maVe"), rs2.getString("maLoaiVe"),
//                    df.format(rs2.getDouble("tienGoc")), df.format(tg),
//                    df.format(rs2.getDouble("thanhTien"))
//                });
//            }
//            lblTongGiamVal.setText(df.format(tongGiam));
//        } catch (Exception e) { e.printStackTrace(); }
//    }
//
//    private void loadDanhSachHoaDon() {
//        modelHD.setRowCount(0);
//        StringBuilder sql = new StringBuilder(
//            "SELECT h.maHD, h.ngayLap, k.tenKH FROM HoaDon h " +
//            "LEFT JOIN KhachHang k ON h.maKH=k.maKH WHERE 1=1");
//        try (Connection con = ConnectDB.getConnection()) {
//            String kw = txtTimKiemTenKH.getText().trim();
//            if (!kw.isEmpty())                        sql.append(" AND k.tenKH LIKE ?");
//            if (cbNhanVienLoc.getSelectedIndex() > 0) sql.append(" AND h.maNV=?");
//            if (dateTuNgay.getDate()  != null)        sql.append(" AND h.ngayLap >= ?");
//            if (dateToiNgay.getDate() != null)        sql.append(" AND h.ngayLap <= ?");
//            sql.append(" ORDER BY h.ngayLap DESC");
//
//            PreparedStatement ps = con.prepareStatement(sql.toString());
//            int i = 1;
//            if (!kw.isEmpty())                        ps.setString(i++, "%" + kw + "%");
//            if (cbNhanVienLoc.getSelectedIndex() > 0)
//                ps.setString(i++, cbNhanVienLoc.getSelectedItem().toString().split(" - ")[0]);
//            if (dateTuNgay.getDate()  != null) ps.setTimestamp(i++, new Timestamp(dateTuNgay.getDate().getTime()));
//            if (dateToiNgay.getDate() != null) ps.setTimestamp(i++, new Timestamp(dateToiNgay.getDate().getTime()));
//
//            ResultSet rs = ps.executeQuery();
//            int count = 0;
//            while (rs.next()) {
//                String tenKH = rs.getString("tenKH");
//                modelHD.addRow(new Object[]{
//                    rs.getString("maHD"),
//                    sdf.format(rs.getTimestamp("ngayLap")),
//                    tenKH != null ? tenKH : "Khách lẻ"
//                });
//                count++;
//            }
//            lblTongHD.setText(String.valueOf(count));
//        } catch (Exception e) { e.printStackTrace(); }
//    }
//
//    private void xuatHoaDonPDF(String maHD) {
//        if (maHD == null || maHD.isEmpty()) {
//            JOptionPane.showMessageDialog(this, "Chọn hóa đơn trước khi in!");
//            return;
//        }
//        JFileChooser fc = new JFileChooser();
//        fc.setSelectedFile(new java.io.File("HoaDon_" + maHD + ".pdf"));
//        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
//            try {
//                String path = fc.getSelectedFile().getAbsolutePath();
//                Document doc = new Document(new com.itextpdf.text.Rectangle(250, 500));
//                PdfWriter.getInstance(doc, new FileOutputStream(path));
//                doc.open();
//                doc.add(new Paragraph("DUONG SAT VIET NAM",
//                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
//                doc.add(new Paragraph("Ma HD: " + maHD));
//                doc.add(new Paragraph("Khach: " + lblKhachHangVal.getText()));
//                doc.add(new Paragraph("Tong:  " + lblTongTienVal.getText()));
//                doc.close();
//                Desktop.getDesktop().open(new java.io.File(path));
//            } catch (Exception e) {
//                JOptionPane.showMessageDialog(this, "Lỗi PDF: " + e.getMessage());
//            }
//        }
//    }
//
//    private void loadNhanVienToCombo() {
//        cbNhanVienLoc.removeAllItems();
//        cbNhanVienLoc.addItem("--- Tất cả ---");
//        try (Connection con = ConnectDB.getConnection()) {
//            ResultSet rs = con.createStatement()
//                    .executeQuery("SELECT maNV, tenNV FROM NhanVien");
//            while (rs.next())
//                cbNhanVienLoc.addItem(rs.getString(1) + " - " + rs.getString(2));
//        } catch (Exception ignored) {}
//    }
//
//    // ─── UI HELPERS ───────────────────────────────────────────────────────────
//    private JLabel infoVal(String text) {
//        JLabel l = new JLabel(text);
//        l.setFont(F_CELL);
//        l.setForeground(TEXT_DARK);
//        return l;
//    }
//
//    private void addInfoRow(JPanel pnl, String label, JLabel val, int row,
//                            GridBagConstraints gbc) {
//        gbc.gridy = row;
//        gbc.gridx = 0; gbc.weightx = 0.4;
//        JLabel lbl = new JLabel(label);
//        lbl.setFont(F_CELL);
//        lbl.setForeground(TEXT_MID);
//        pnl.add(lbl, gbc);
//        gbc.gridx = 1; gbc.weightx = 0.6;
//        pnl.add(val, gbc);
//    }
//
//    private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
//        JPanel p = new JPanel(new BorderLayout(5, 5));
//        p.setBackground(BG_CARD);
//        p.setBorder(BorderFactory.createCompoundBorder(
//                new LineBorder(COLOR_BORDER, 1, true),
//                new EmptyBorder(15, 20, 15, 20)));
//        JLabel lblT = new JLabel(title);
//        lblT.setForeground(COLOR_TEXT_MUTED);
//        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
//        lblValue.setForeground(accent);
//        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
//        p.add(lblT,     BorderLayout.NORTH);
//        p.add(lblValue, BorderLayout.CENTER);
//        return p;
//    }
//
//    private JPanel buildCard(LayoutManager layout) {
//        JPanel p = new JPanel(layout);
//        p.setBackground(BG_CARD);
//        p.setBorder(new ShadowBorder());
//        return p;
//    }
//
//    private JLabel makeLabel(String text) {
//        JLabel l = new JLabel(text);
//        l.setFont(F_LABEL);
//        l.setForeground(TEXT_MID);
//        return l;
//    }
//
//    private JTextField makeField(String hint) {
//        JTextField tf = new JTextField(13);
//        tf.setFont(F_CELL);
//        tf.setBorder(new LineBorder(BORDER, 1, true));
//        return tf;
//    }
//
//    private JTable buildStyledTable(DefaultTableModel model) {
//        JTable t = new JTable(model) {
//            public boolean isCellEditable(int r, int c) { return false; }
//            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
//                Component c = super.prepareRenderer(r, row, col);
//                if (!isRowSelected(row))
//                    c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
//                return c;
//            }
//        };
//        t.setFont(F_CELL);
//        t.setRowHeight(36);
//        t.setShowVerticalLines(false);
//        t.setShowHorizontalLines(true);
//        t.setGridColor(BORDER);
//        t.setSelectionBackground(ROW_SEL);
//        t.setSelectionForeground(TEXT_DARK);
//        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        t.setFocusable(false);
//        t.setIntercellSpacing(new Dimension(0, 0));
//
//        JTableHeader header = t.getTableHeader();
//        header.setDefaultRenderer(new HeaderRenderer());
//        header.setPreferredSize(new Dimension(0, 40));
//        header.setReorderingAllowed(false);
//
//        DefaultTableCellRenderer cellR = new DefaultTableCellRenderer();
//        cellR.setFont(F_CELL);
//        cellR.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
//        for (int i = 0; i < model.getColumnCount(); i++)
//            t.getColumnModel().getColumn(i).setCellRenderer(cellR);
//
//        return t;
//    }
//
//    private JButton makeBtn(String text, BtnStyle style) {
//        JButton btn = new JButton(text) {
//            @Override
//            protected void paintComponent(Graphics g) {
//                Graphics2D g2 = (Graphics2D) g.create();
//                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                        RenderingHints.VALUE_ANTIALIAS_ON);
//                switch (style) {
//                    case PRIMARY:
//                        g2.setColor(getModel().isPressed()  ? new Color(0x0F3F8C)
//                                  : getModel().isRollover() ? ACCENT_HVR : ACCENT);
//                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
//                        break;
//                    case SUCCESS:
//                        g2.setColor(getModel().isPressed()  ? new Color(0x166534)
//                                  : getModel().isRollover() ? BTN_GREEN_HVR : BTN_GREEN);
//                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
//                        break;
//                    case DANGER:
//                        g2.setColor(getModel().isPressed()  ? new Color(0x922B21)
//                                  : getModel().isRollover() ? BTN_RED_HVR : BTN_RED);
//                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
//                        break;
//                    default: break;
//                }
//                g2.dispose();
//                super.paintComponent(g);
//            }
//        };
//        btn.setFont(F_LABEL);
//        btn.setForeground(style == BtnStyle.SECONDARY ? new Color(0x3A5A8C) : Color.WHITE);
//        btn.setPreferredSize(new Dimension(130, 36));
//        btn.setContentAreaFilled(false);
//        btn.setBorderPainted(false);
//        btn.setFocusPainted(false);
//        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        return btn;
//    }
//
//    private void styleScrollBar(JScrollBar sb) {
//        sb.setUI(new BasicScrollBarUI() {
//            protected void configureScrollBarColors() { thumbColor = new Color(0xC0D4EE); }
//        });
//    }
//
//    // ─── RENDERERS ────────────────────────────────────────────────────────────
//    private static class HeaderRenderer extends DefaultTableCellRenderer {
//        HeaderRenderer() { setHorizontalAlignment(LEFT); }
//        public Component getTableCellRendererComponent(
//                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
//            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
//            l.setOpaque(true);
//            l.setBackground(ACCENT);
//            l.setForeground(Color.WHITE);
//            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
//            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
//            return l;
//        }
//    }
//
//    private static class ShadowBorder extends AbstractBorder {
//        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//            Graphics2D g2 = (Graphics2D) g;
//            g2.setColor(new Color(0xE2EAF4));
//            g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
//        }
//    }
//}
package com.gui;

import com.dao.Dao_HoaDon;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.FontFactory;

public class TAB_ThanhToanLapHD extends JPanel {

    // ================= COMPONENTS & DAO =================
    private Dao_HoaDon daoHD = new Dao_HoaDon();
    
    private DefaultTableModel modelHD, modelCT;
    private JTable tableHD, tableCT;
    private JComboBox<Object> cbNhanVienLoc;
    private JDateChooser dateTuNgay, dateToiNgay;
    private JTextField txtTimKiemTenKH;
    private JLabel lblMaHDVal, lblNgayLapVal, lblNhanVienVal, lblKhachHangVal, lblTongGiamVal, lblTongTienVal;
    private JLabel lblTongHD = new JLabel("0");

    private String currentMaHD = "";
    private final DecimalFormat df  = new DecimalFormat("#,### VNĐ");
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // ================= STYLE CONSTANTS =================
    private static final Color BG_PAGE    = new Color(0xF4F7FB);
    private static final Color BG_CARD    = Color.WHITE;
    private static final Color ACCENT     = new Color(0x1A5EAB);
    private static final Color TEXT_DARK  = new Color(0x1E2B3C);
    private static final Color TEXT_MID   = new Color(0x5A6A7D);
    private static final Color BORDER     = new Color(0xE2EAF4);
    private static final Color ROW_ALT    = new Color(0xF7FAFF);
    private static final Color ROW_SEL    = new Color(0xDDEEFF);
    private static final Color BTN_GREEN  = new Color(0x16A34A);
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);

    private enum BtnStyle { PRIMARY, SUCCESS }

    public TAB_ThanhToanLapHD() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        initTables();
        initUI();
        
        loadNhanVienToCombo();
        loadDanhSachHoaDon();
    }

    private void initTables() {
        modelHD = new DefaultTableModel(new String[]{"Mã HD", "Ngày lập", "Khách hàng"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        modelCT = new DefaultTableModel(new String[]{"Mã Vé", "Loại", "Giá Gốc", "Giảm", "Thành Tiền"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableHD = buildStyledTable(modelHD);
        tableCT = buildStyledTable(modelCT);

        tableHD.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableHD.getSelectedRow();
                if (row != -1) {
                    currentMaHD = tableHD.getValueAt(row, 0).toString();
                    hienThiChiTiet(currentMaHD);
                }
            }
        });
    }

    private void initUI() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.add(buildHeader());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildStatsBar());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildFilterCard());

        add(topPanel, BorderLayout.NORTH);
        add(buildSplitPanel(), BorderLayout.CENTER);
    }

    // ================= LOGIC GỌI TỪ DAO_HOADON =================

    private void loadDanhSachHoaDon() {
        modelHD.setRowCount(0);
        try {
            String tenKH = txtTimKiemTenKH.getText().trim();
            String maNV = null;
            if (cbNhanVienLoc.getSelectedIndex() > 0) {
                maNV = cbNhanVienLoc.getSelectedItem().toString().split(" - ")[0];
            }

            java.util.Date dTu = dateTuNgay.getDate();
            java.util.Date dToi = dateToiNgay.getDate();

            // RÀNG BUỘC TRƯỜNG HỢP 3: tới ngày không được bé hơn từ ngày
            if (dTu != null && dToi != null) {
                if (dTu.after(dToi)) {
                    JOptionPane.showMessageDialog(this, "Ngày kết thúc không được nhỏ hơn ngày bắt đầu!", "Lỗi chọn ngày", JOptionPane.WARNING_MESSAGE);
                    dateToiNgay.setDate(null); // Reset lại ngày tới
                    return;
                }
            }

            Timestamp tuNgay = null;
            Timestamp toiNgay = null;

            // TRƯỜNG HỢP 1: Chỉ chọn Từ Ngày -> Lọc từ ngày đó đến hiện tại
            if (dTu != null && dToi == null) {
                tuNgay = new Timestamp(dTu.getTime());
                // Set về 00:00:00 của ngày được chọn
                tuNgay.setHours(0); tuNgay.setMinutes(0); tuNgay.setSeconds(0); tuNgay.setNanos(0);
                
                toiNgay = new Timestamp(System.currentTimeMillis());
            } 
            // TRƯỜNG HỢP 2: Chỉ chọn Tới Ngày -> Lọc từ trước đến hết ngày được chọn
            else if (dTu == null && dToi != null) {
                toiNgay = new Timestamp(dToi.getTime());
                // Set về 23:59:59 để lấy hết dữ liệu trong ngày đó
                toiNgay.setHours(23); toiNgay.setMinutes(59); toiNgay.setSeconds(59); toiNgay.setNanos(999999999);
            }
            // TRƯỜNG HỢP 3: Chọn cả hai (bao gồm cả trường hợp trùng 1 ngày)
            else if (dTu != null && dToi != null) {
                tuNgay = new Timestamp(dTu.getTime());
                tuNgay.setHours(0); tuNgay.setMinutes(0); tuNgay.setSeconds(0); tuNgay.setNanos(0);

                toiNgay = new Timestamp(dToi.getTime());
                toiNgay.setHours(23); toiNgay.setMinutes(59); toiNgay.setSeconds(59); toiNgay.setNanos(999999999);
            }

            // Gọi DAO để lấy dữ liệu
            ResultSet rs = daoHD.getDanhSachHoaDon(tenKH, maNV, tuNgay, toiNgay);
            int count = 0;
            while (rs != null && rs.next()) {
                modelHD.addRow(new Object[]{
                    rs.getString("maHD"), 
                    sdf.format(rs.getTimestamp("ngayLap")), 
                    rs.getString("tenKH") != null ? rs.getString("tenKH") : "Khách lẻ"
                });
                count++;
            }
            lblTongHD.setText(String.valueOf(count));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void hienThiChiTiet(String maHD) {
        modelCT.setRowCount(0);
        double tongGiam = 0;
        try {
            // 1. Lấy thông tin chung từ Dao_HoaDon
            ResultSet rsInfo = daoHD.getThongTinHoaDon(maHD);
            if (rsInfo != null && rsInfo.next()) {
                lblMaHDVal.setText(rsInfo.getString("maHD"));
                lblNgayLapVal.setText(sdf.format(rsInfo.getTimestamp("ngayLap")));
                lblNhanVienVal.setText(rsInfo.getString("tenNV"));
                String tenKH = rsInfo.getString("tenKH");
                lblKhachHangVal.setText(tenKH != null ? tenKH : "Khách lẻ");
                lblTongTienVal.setText(df.format(rsInfo.getDouble("tongTien")));
            }

            // 2. Lấy danh sách vé chi tiết từ Dao_HoaDon
            ResultSet rsDetails = daoHD.getChiTietHoaDon(maHD);
            while (rsDetails != null && rsDetails.next()) {
                double tg = rsDetails.getDouble("tienGiam");
                tongGiam += tg;
                modelCT.addRow(new Object[]{
                    rsDetails.getString("maVe"), 
                    rsDetails.getString("maLoaiVe"),
                    df.format(rsDetails.getDouble("tienGoc")), 
                    df.format(tg), 
                    df.format(rsDetails.getDouble("thanhTien"))
                });
            }
            lblTongGiamVal.setText(df.format(tongGiam));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadNhanVienToCombo() {
        cbNhanVienLoc.removeAllItems();
        cbNhanVienLoc.addItem("--- Tất cả ---");
        try {
            // Sử dụng hàm từ Dao_HoaDon
            ResultSet rs = daoHD.getAllNhanVien();
            while (rs != null && rs.next()) {
                cbNhanVienLoc.addItem(rs.getString(1) + " - " + rs.getString(2));
            }
        } catch (SQLException ignored) {}
    }

    private void xoaBoLoc() {
        txtTimKiemTenKH.setText("");
        dateTuNgay.setDate(null);
        dateToiNgay.setDate(null);
        if (cbNhanVienLoc.getItemCount() > 0) cbNhanVienLoc.setSelectedIndex(0);
        currentMaHD = "";
        lblMaHDVal.setText("-"); lblNgayLapVal.setText("-");
        lblNhanVienVal.setText("-"); lblKhachHangVal.setText("-");
        lblTongGiamVal.setText("-"); lblTongTienVal.setText("0 VNĐ");
        modelCT.setRowCount(0);
        loadDanhSachHoaDon();
    }

    // ================= UI BUILDERS (GIỮ NGUYÊN STYLE) =================

    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        JLabel lbl = new JLabel("QUẢN LÝ & TRA CỨU HÓA ĐƠN");
        lbl.setFont(F_TITLE);
        lbl.setForeground(ACCENT);
        pnl.add(lbl, BorderLayout.WEST);
        return pnl;
    }

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 1, 12, 0));
        bar.setOpaque(false);
        bar.add(createStatCard("TỔNG HÓA ĐƠN", lblTongHD, ACCENT));
        return bar;
    }

    private JPanel buildFilterCard() {
        JPanel card = buildCard(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtTimKiemTenKH = makeField("");
        txtTimKiemTenKH.setPreferredSize(new Dimension(150, 32));
        txtTimKiemTenKH.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) { loadDanhSachHoaDon(); }
        });

        dateTuNgay = new JDateChooser();
        dateTuNgay.setPreferredSize(new Dimension(110, 32));
        dateTuNgay.addPropertyChangeListener("date", e -> loadDanhSachHoaDon());

        dateToiNgay = new JDateChooser();
        dateToiNgay.setPreferredSize(new Dimension(110, 32));
        dateToiNgay.addPropertyChangeListener("date", e -> loadDanhSachHoaDon());

        cbNhanVienLoc = new JComboBox<>();
        cbNhanVienLoc.setPreferredSize(new Dimension(130, 32));
        cbNhanVienLoc.addActionListener(e -> loadDanhSachHoaDon());

        JButton btnLamMoi = makeBtn("Làm mới", BtnStyle.PRIMARY);
        btnLamMoi.setPreferredSize(new Dimension(100, 32));
        btnLamMoi.addActionListener(e -> xoaBoLoc());

        gbc.gridy = 0;
        gbc.weightx = 0; card.add(makeLabel("Khách hàng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.2; card.add(txtTimKiemTenKH, gbc);
        gbc.gridx = 2; gbc.weightx = 0; card.add(makeLabel("Từ:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.1; card.add(dateTuNgay, gbc);
        gbc.gridx = 4; gbc.weightx = 0; card.add(makeLabel("Tới:"), gbc);
        gbc.gridx = 5; gbc.weightx = 0.1; card.add(dateToiNgay, gbc);
        gbc.gridx = 6; gbc.weightx = 0; card.add(makeLabel("NV:"), gbc);
        gbc.gridx = 7; gbc.weightx = 0.15; card.add(cbNhanVienLoc, gbc);
        gbc.gridx = 8; gbc.weightx = 0; card.add(btnLamMoi, gbc);

        return card;
    }

    private JSplitPane buildSplitPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildDanhSachCard(), buildChiTietCard());
        split.setDividerLocation(400);
        split.setDividerSize(8);
        split.setBorder(null);
        return split;
    }

    private JPanel buildDanhSachCard() {
        JPanel card = buildCard(new BorderLayout());
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));
        JLabel lbl = new JLabel("Danh sách hóa đơn");
        lbl.setFont(F_LABEL);
        bar.add(lbl, BorderLayout.WEST);

        JScrollPane scroll = new JScrollPane(tableHD);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        card.add(bar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildChiTietCard() {
        JPanel card = buildCard(new BorderLayout(0, 0));

        JPanel pnlHdr = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlHdr.setBackground(ACCENT);
        pnlHdr.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JLabel lblT = new JLabel("CHI TIẾT HÓA ĐƠN");
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblT.setForeground(Color.WHITE);
        JLabel lblS = new JLabel("Hệ Thống Bán Vé Tàu Hỏa");
        lblS.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblS.setForeground(new Color(0xA8C8EE));
        pnlHdr.add(lblT); pnlHdr.add(lblS);

        JPanel pnlInfo = new JPanel(new GridBagLayout());
        pnlInfo.setBackground(BG_CARD);
        pnlInfo.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 8);

        lblMaHDVal = infoVal("-"); lblMaHDVal.setFont(F_LABEL);
        lblNgayLapVal = infoVal("-");
        lblNhanVienVal = infoVal("-");
        lblKhachHangVal = infoVal("-");
        lblTongGiamVal = infoVal("-");
        lblTongTienVal = infoVal("0 VNĐ");
        lblTongTienVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongTienVal.setForeground(new Color(0xDC2626));

        addInfoRow(pnlInfo, "Mã hóa đơn:", lblMaHDVal, 0, gbc);
        addInfoRow(pnlInfo, "Ngày lập:", lblNgayLapVal, 1, gbc);
        addInfoRow(pnlInfo, "Nhân viên:", lblNhanVienVal, 2, gbc);
        addInfoRow(pnlInfo, "Khách hàng:", lblKhachHangVal, 3, gbc);
        addInfoRow(pnlInfo, "Tổng giảm giá:", lblTongGiamVal, 4, gbc);

        JScrollPane scrollCT = new JScrollPane(tableCT);
        scrollCT.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER));
        scrollCT.getViewport().setBackground(BG_CARD);

        JPanel pnlTotalInside = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlTotalInside.setBackground(new Color(0xF8FAFC));
        JLabel lblTotalText = new JLabel("TỔNG THÀNH TIỀN:");
        lblTotalText.setFont(F_LABEL);
        pnlTotalInside.add(lblTotalText);
        pnlTotalInside.add(lblTongTienVal);

        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 12));
        pnlAction.setBackground(BG_CARD);
        JButton btnPDF = makeBtn("XUẤT FILE PDF", BtnStyle.SUCCESS);
        btnPDF.setPreferredSize(new Dimension(150, 36));
        btnPDF.addActionListener(e -> xuatHoaDonPDF(currentMaHD));
        pnlAction.add(btnPDF);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.add(pnlInfo, BorderLayout.NORTH);
        pnlCenter.add(scrollCT, BorderLayout.CENTER);
        pnlCenter.add(pnlTotalInside, BorderLayout.SOUTH);

        card.add(pnlHdr, BorderLayout.NORTH);
        card.add(pnlCenter, BorderLayout.CENTER);
        card.add(pnlAction, BorderLayout.SOUTH);
        return card;
    }

    private void xuatHoaDonPDF(String maHD) {
        if (maHD == null || maHD.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn hóa đơn trước khi in!");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("HoaDon_" + maHD + ".pdf"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String path = fc.getSelectedFile().getAbsolutePath();
                Document doc = new Document();
                PdfWriter.getInstance(doc, new FileOutputStream(path));
                doc.open();
                doc.add(new Paragraph("DUONG SAT VIET NAM", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                doc.add(new Paragraph("Ma HD: " + maHD));
                doc.add(new Paragraph("Khach: " + lblKhachHangVal.getText()));
                doc.add(new Paragraph("Tong: " + lblTongTienVal.getText()));
                doc.close();
                Desktop.getDesktop().open(new java.io.File(path));
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi PDF: " + e.getMessage()); }
        }
    }

    private JLabel infoVal(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_CELL);
        l.setForeground(TEXT_DARK);
        return l;
    }

    private void addInfoRow(JPanel pnl, String label, JLabel val, int row, GridBagConstraints gbc) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(F_CELL);
        lbl.setForeground(TEXT_MID);
        pnl.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnl.add(val, gbc);
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(226, 232, 240), 1, true), new EmptyBorder(15, 20, 15, 20)));
        JLabel lblT = new JLabel(title);
        lblT.setForeground(new Color(100, 116, 139));
        lblValue.setForeground(accent);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        p.add(lblT, BorderLayout.NORTH);
        p.add(lblValue, BorderLayout.CENTER);
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

    private JTextField makeField(String hint) {
        JTextField tf = new JTextField();
        tf.setFont(F_CELL);
        tf.setBorder(new LineBorder(BORDER, 1, true));
        return tf;
    }

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        t.setRowHeight(36);
        t.setGridColor(BORDER);
        t.setSelectionBackground(ROW_SEL);
        t.setSelectionForeground(TEXT_DARK);
        t.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        t.getTableHeader().setPreferredSize(new Dimension(0, 40));
        return t;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = (style == BtnStyle.PRIMARY) ? ACCENT : BTN_GREEN;
                if (getModel().isRollover()) c = c.brighter();
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_LABEL);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setBackground(ACCENT);
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setHorizontalAlignment(LEFT);
            l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return l;
        }
    }
}