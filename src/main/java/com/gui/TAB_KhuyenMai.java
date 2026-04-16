package com.gui;

import com.connectDB.ConnectDB;
import com.dao.DAO_KhuyenMai;
import com.dao.DAO_KhuyenMaiDetail;
import com.entities.*;
import com.enums.LoaiKhuyenMai;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TAB_KhuyenMai extends JPanel {

    // ================= COLOR =================
    private static final Color BG_PAGE     = new Color(0xF4F7FB);
    private static final Color BG_CARD     = Color.WHITE;
    private static final Color ACCENT      = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR  = new Color(0x2270CC);
    private static final Color ACCENT_FOC  = new Color(0x4D9DE0);
    private static final Color TEXT_DARK   = new Color(0x1E2B3C);
    private static final Color TEXT_MID    = new Color(0x5A6A7D);
    private static final Color TEXT_LIGHT  = new Color(0xA0AEC0);
    private static final Color BORDER      = new Color(0xE2EAF4);
    private static final Color ROW_ALT     = new Color(0xF7FAFF);
    private static final Color ROW_SEL     = new Color(0xDDEEFF);
    private static final Color BTN2_FG     = new Color(0x3A5A8C);
    private static final Color BTN_RED     = new Color(0xC0392B);
    private static final Color BTN_RED_HVR = new Color(0xE74C3C);
    private static final Color BG_RIGHT    = new Color(0xF7FAFF);

    // [THÊM MỚI] Màu dùng cho stat card (đồng bộ với TAB_QLNhanVien)
    private static final Color COLOR_BORDER     = new Color(0xE2EAF4);
    private static final Color COLOR_TEXT_MUTED = new Color(0x5A6A7D);

    // ================= FONT =================
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER }

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    // ===== Cột KhuyenMai – KHÔNG có loaiKM, giaTri =====
    private static final String[] COLS_KM = {
            "Mã KM", "Tên KM", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"
    };

    // ===== Cột KhuyenMaiDetail – CÓ loaiKM, giaTri =====
    private static final String[] COLS_KMD = {
            "Mã", "Tuyến", "Loại ghế", "Loại vé", "Loại KM", "Giá trị", "Trạng thái"
    };

    // ================= FIELDS =================
    Connection conn;
    JTextField txtSearch;

    // [THÊM MỚI] Filter ngày
    JDateChooser dateFilterBD;
    JDateChooser dateFilterKT;

    // [THÊM MỚI] Stat labels – đếm số KM theo trạng thái
    JLabel lblStatTotal   = new JLabel("0");
    JLabel lblStatActive  = new JLabel("0");
    JLabel lblStatStopped = new JLabel("0");
    JLabel lblStatUsed    = new JLabel("0");

    private JTable tableKM;
    private DefaultTableModel modelKM;
    private JTable tableKMD;
    private DefaultTableModel modelKMD;

    private JLabel  lblKMDTitle;
    private JButton btnAddDetail;
    //    private JButton btnEditDetail;
    private JButton btnDelDetail;

    DAO_KhuyenMai daoKM;
    DAO_KhuyenMaiDetail daoKMD;
    private KhuyenMai selectedKM = null;
    // [THÊM MỚI] Cache danh sách LoaiVe load 1 lần từ DAO
    private List<LoaiVe> dsLoaiVe = new java.util.ArrayList<>();
    private List<LoaiToa> dsLoaiToa = new java.util.ArrayList<>();

    // ================= CONSTRUCTOR =================
    public TAB_KhuyenMai() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        modelKM = new DefaultTableModel(COLS_KM, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        modelKMD = new DefaultTableModel(COLS_KMD, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tableKM  = buildTableKM();
        tableKMD = buildTableKMD();

        conn   = ConnectDB.getConnection();
        daoKM  = new DAO_KhuyenMai(conn);
        daoKMD = new DAO_KhuyenMaiDetail(conn);

        // [THÊM MỚI] Load danh sách LoaiVe 1 lần khi khởi tạo
        dsLoaiVe = daoKM.getAllLoaiVe();
        dsLoaiToa = daoKM.getAllLoaiToa();

        lblKMDTitle = new JLabel();

        loadDataKhuyenMai();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        topPanel.add(buildHeader());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildStatsBar());      // [THÊM MỚI] thanh stats riêng bên dưới header
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildFilterCard());    // filter đã được mở rộng thêm ngày

        add(topPanel, BorderLayout.NORTH);
        add(buildMainCard(), BorderLayout.CENTER); // ⬅️ đổi thành CENTER
    }

    // ========== HEADER ==========
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel l = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        l.setFont(F_TITLE); l.setForeground(ACCENT);
        p.add(l, BorderLayout.WEST); return p;
    }

    // ========== FILTER ==========
    // [SỬA] Thêm filter ngày bắt đầu và ngày kết thúc
    private JPanel buildFilterCard() {
        JPanel card = buildCard(new FlowLayout(FlowLayout.LEFT, 12, 12));

        txtSearch = makeField("Tên khuyến mãi...");
        txtSearch.setPreferredSize(new Dimension(400, 36));

        // [THÊM MỚI] JDateChooser filter ngày bắt đầu
        dateFilterBD = new JDateChooser();
        dateFilterBD.setDateFormatString(DATE_FORMAT);
        dateFilterBD.setPreferredSize(new Dimension(130, 34));
        dateFilterBD.setToolTipText("Lọc từ ngày bắt đầu");

        // [THÊM MỚI] JDateChooser filter ngày kết thúc
        dateFilterKT = new JDateChooser();
        dateFilterKT.setDateFormatString(DATE_FORMAT);
        dateFilterKT.setPreferredSize(new Dimension(130, 34));
        dateFilterKT.setToolTipText("Lọc đến ngày kết thúc");

        JButton btnSearch = makeBtn("Tìm kiếm", BtnStyle.PRIMARY);
        JButton btnReset  = makeBtn("Làm mới",  BtnStyle.SECONDARY);

        card.add(makeLabel("Tên:"));         card.add(txtSearch);
        card.add(makeLabel("Từ ngày:"));     card.add(dateFilterBD);   // [THÊM MỚI]
        card.add(makeLabel("Đến ngày:"));    card.add(dateFilterKT);   // [THÊM MỚI]
        card.add(btnSearch);
        card.add(btnReset);

        // [SỬA] tìm kiếm kết hợp cả tên + ngày
        btnSearch.addActionListener(e -> findKhuyenMai());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            dateFilterBD.setDate(null);   // [THÊM MỚI] reset ngày
            dateFilterKT.setDate(null);   // [THÊM MỚI]
            loadDataKhuyenMai();
        });
        return card;
    }

    // [THÊM MỚI] Thanh stats – đếm KM đang áp dụng / đã dừng / đã dùng
    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 12, 0));
        bar.setOpaque(false);
        bar.add(createStatCard("TỔNG KHUYẾN MÃI",  lblStatTotal,   ACCENT));
        bar.add(createStatCard("ĐANG ÁP DỤNG",     lblStatActive,  new Color(34, 197, 94)));
        bar.add(createStatCard("ĐÃ DỪNG",           lblStatStopped, new Color(239, 68, 68)));
        bar.add(createStatCard("ĐÃ ĐƯỢC DÙNG",      lblStatUsed,    new Color(245, 158, 11)));
        return bar;
    }

    // [THÊM MỚI] Tạo 1 stat card (đồng bộ với TAB_QLNhanVien)
    private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(15, 20, 15, 20)));
        JLabel lblT = new JLabel(title);
        lblT.setForeground(COLOR_TEXT_MUTED);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblValue.setForeground(accent);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        p.add(lblT,     BorderLayout.NORTH);
        p.add(lblValue, BorderLayout.CENTER);
        return p;
    }

    // ========== MAIN CARD ==========
    // [SỬA] bỏ buildKMActionBar ở đây – nút Thêm/Xóa KM đã nằm trong buildKMPanel
    private JPanel buildMainCard() {
        JPanel card = buildCard(new BorderLayout());
        card.add(buildSplitBody(), BorderLayout.CENTER);
        return card;
    }

    // ========== SPLIT ==========
    // [SỬA] setDividerLocation(0.5) để split về giữa màn hình
    private JSplitPane buildSplitBody() {
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildKMPanel(), buildKMDPanel());
        sp.setDividerLocation(0.5);   // [SỬA] 0.55 → 0.5 để hai panel bằng nhau
        sp.setResizeWeight(0.5);      // [SỬA] giữ tỉ lệ khi resize cửa sổ
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false); sp.setDividerSize(6);
        return sp;
    }

    // ========== PANEL TRÁI – KM ==========
    // [SỬA] Gộp action bar (label + nút Thêm KM / Xóa KM) vào chính panel này
    // → các nút không còn bị lệch khi kéo split
    private JPanel buildKMPanel() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 4));

        // ── Action bar nằm trong panel trái ──────────────────────────────────
        JPanel actionBar = new JPanel(new BorderLayout()); actionBar.setOpaque(false);
        actionBar.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        JLabel lbl = new JLabel("Danh sách khuyến mãi");
        lbl.setFont(F_LABEL); lbl.setForeground(TEXT_DARK);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton btnAdd = makeBtn("+ Thêm KM", BtnStyle.PRIMARY);
        // [SỬA] Enable xóa mềm – set disable=1 trong DB, không xóa thật
//        JButton btnDelKM = makeBtn("Xóa KM", BtnStyle.DANGER);
        btnAdd.addActionListener(e -> openDialogKM(null));
//        btnDelKM.addActionListener(e -> deleteKhuyenMai());
        btnRow.add(btnAdd);
//        btnRow.add(btnDelKM); // [SỬA] đã bật lại

        actionBar.add(lbl,    BorderLayout.WEST);
        actionBar.add(btnRow, BorderLayout.EAST);
        // ─────────────────────────────────────────────────────────────────────

        JSeparator sep = new JSeparator(); sep.setForeground(BORDER);

        JScrollPane scroll = new JScrollPane(tableKM);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_CARD);
        // [SỬA] Bỏ setPreferredSize cứng → scroll tự lấp đầy không gian còn lại
        styleScrollBar(scroll.getVerticalScrollBar());
        styleScrollBar(scroll.getHorizontalScrollBar());

        p.add(actionBar, BorderLayout.NORTH);
        JPanel inner = new JPanel(new BorderLayout()); inner.setOpaque(false);
        inner.add(sep,    BorderLayout.NORTH);
        inner.add(scroll, BorderLayout.CENTER);
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    // ========== PANEL PHẢI – KMD ==========
    private JPanel buildKMDPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_RIGHT);
        p.setBorder(BorderFactory.createEmptyBorder(0, 4, 18, 18));
        p.add(buildKMDActionBar(), BorderLayout.NORTH);
        JSeparator sep = new JSeparator(); sep.setForeground(BORDER);
        JScrollPane scroll = new JScrollPane(tableKMD);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_CARD);
        // [SỬA] Bỏ setPreferredSize cứng → scroll tự lấp đầy không gian còn lại
        styleScrollBar(scroll.getVerticalScrollBar());
        styleScrollBar(scroll.getHorizontalScrollBar());
        JPanel inner = new JPanel(new BorderLayout()); inner.setOpaque(false);
        inner.add(sep,    BorderLayout.NORTH);
        inner.add(scroll, BorderLayout.CENTER);
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    // ========== ACTION BAR – KMD ==========
    // [SỬA] Comment nút "Dừng áp dụng" – chưa hiển thị trên giao diện
    private JPanel buildKMDActionBar() {
        JPanel bar = new JPanel(new BorderLayout()); bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        bar.setBackground(BG_CARD);

        lblKMDTitle.setText("Chi tiết — (chưa chọn khuyến mãi)");
        lblKMDTitle.setFont(F_LABEL); lblKMDTitle.setForeground(TEXT_LIGHT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);

        btnAddDetail = makeBtn("+ Thêm", BtnStyle.PRIMARY);
        // [SỬA] Enable xóa mềm KMD
//        btnDelDetail = makeBtn("✕ Xóa", BtnStyle.DANGER);
        setDetailBtnsEnabled(false);

        btnAddDetail.addActionListener(e -> openDialogKMD(null));
//        btnDelDetail.addActionListener(e -> deleteKMDetail()); // [SỬA] đã bật lại

        btnPanel.add(btnAddDetail);
//        btnPanel.add(btnDelDetail); // [SỬA] đã bật lại

        bar.add(lblKMDTitle, BorderLayout.WEST);
        bar.add(btnPanel,    BorderLayout.EAST);
        return bar;
    }

    // ========== TABLE – KM ==========
    // [SỬA] AUTO_RESIZE_OFF + tooltip cell để cột không bị cắt chữ
    private JTable buildTableKM() {
        JTable t = new JTable(modelKM) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
            // [THÊM MỚI] tooltip khi text bị cắt
            @Override public String getToolTipText(java.awt.event.MouseEvent e) {
                int col = columnAtPoint(e.getPoint());
                int row = rowAtPoint(e.getPoint());
                if (row < 0 || col < 0) return null;
                Object val = getValueAt(row, col);
                return val != null ? val.toString() : null;
            }
        };
        t.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = t.getSelectedRow();
                if (row >= 0) {
                    String maKM  = modelKM.getValueAt(row, 0).toString();
                    String tenKM = modelKM.getValueAt(row, 1).toString();
                    selectedKM   = daoKM.getKhuyenMaiByID(maKM);
                    loadDataKMDetail(maKM);
                    lblKMDTitle.setText("Chi tiết — " + tenKM);
                    lblKMDTitle.setForeground(ACCENT);
                    setDetailBtnsEnabled(true);
                }
            }
        });
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = t.getSelectedRow();
                    if (row >= 0) openDialogKM(daoKM.getKhuyenMaiByID(
                            modelKM.getValueAt(row, 0).toString()));
                }
            }
        });
        // [SỬA] AUTO_RESIZE_OFF thay vì AUTO_RESIZE_LAST_COLUMN
        // → mỗi cột giữ đúng width đã set, không bị co giãn làm mất chữ
//        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        styleTable(t);
        int[] w = { 80, 180, 110, 110, 120 };
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        applyRenderers(t, COLS_KM.length);
        t.getColumnModel().getColumn(4).setCellRenderer(new TrangThaiRenderer());
        return t;
    }


    private static class TrangThaiRenderer extends DefaultTableCellRenderer {

        private static final Color CLR_ACTIVE  = new Color(0x16A34A); // xanh lá
        private static final Color CLR_STOP    = new Color(0xDC2626); // đỏ
        private static final Color CLR_OFF     = new Color(0xD97706); // vàng cam

        private static final Color BG_ACTIVE   = new Color(0xDCFCE7);
        private static final Color BG_STOP     = new Color(0xFEE2E2);
        private static final Color BG_OFF      = new Color(0xFEF9C3);

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {

            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);

            String text = v != null ? v.toString() : "";

            // Badge: text canh giữa, bo tròn giả lập bằng padding + màu nền
            l.setHorizontalAlignment(CENTER);
            l.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

            if (!sel) {
                // Màu text + nền theo trạng thái
                if (text.equalsIgnoreCase("Đang áp dụng")) {
                    l.setForeground(CLR_ACTIVE);
                    l.setBackground(BG_ACTIVE);
                } else if (text.equalsIgnoreCase("Dừng áp dụng")) {
                    l.setForeground(CLR_STOP);
                    l.setBackground(BG_STOP);
                } else {
                    // fallback
                    l.setForeground(new Color(0x5A6A7D));
                    l.setBackground(new Color(0xF0F4FA));
                }
            }

            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            l.setOpaque(true);
            return l;
        }
    }


    // ========== TABLE – KMD ==========
    // [SỬA] AUTO_RESIZE_OFF + tooltip cell để cột không bị cắt chữ
    private JTable buildTableKMD() {
        JTable t = new JTable(modelKMD) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
            // [THÊM MỚI] tooltip khi text bị cắt
            @Override public String getToolTipText(java.awt.event.MouseEvent e) {
                int col = columnAtPoint(e.getPoint());
                int row = rowAtPoint(e.getPoint());
                if (row < 0 || col < 0) return null;
                Object val = getValueAt(row, col);
                return val != null ? val.toString() : null;
            }
        };
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && selectedKM != null) {
                    int row = t.getSelectedRow();
                    if (row >= 0) {
                        String id = modelKMD.getValueAt(row, 0).toString();
                        openDialogKMD(daoKMD.getKhuyenMaiDetailByID(id));
                    }
                }
            }
        });
        // [SỬA] AUTO_RESIZE_OFF để cột không bị co làm mất chữ
//        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        styleTable(t);
        int[] w = { 70, 170, 90, 90, 80, 70, 100 };
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        applyRenderers(t, COLS_KMD.length);
        t.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiRenderer());
        return t;
    }

    // ========== LOAD DATA ==========
    // [SỬA] Thêm đếm stats khi load dữ liệu
    private void loadDataKhuyenMai() {
        modelKM.setRowCount(0); modelKMD.setRowCount(0);
        selectedKM = null; setDetailBtnsEnabled(false);
        lblKMDTitle.setText("Chi tiết — (chưa chọn khuyến mãi)");
        lblKMDTitle.setForeground(TEXT_LIGHT);

        List<KhuyenMai> list = daoKM.getAllKhuyenMai();

        // [THÊM MỚI] Đếm stats
        int total = list.size(), active = 0, stopped = 0;
        for (KhuyenMai km : list) {
            if (km.isTrangThai()) active++; else stopped++;
            addKhuyenMaiToTable(km);
        }
        // [THÊM MỚI] Số KM đã được dùng – lấy từ DAO (đếm KMDetail có lượt dùng > 0)
        int used = daoKM.countKhuyenMaiDaDung();

        // [THÊM MỚI] Cập nhật stat labels
        lblStatTotal.setText(String.valueOf(total));
        lblStatActive.setText(String.valueOf(active));
        lblStatStopped.setText(String.valueOf(stopped));
        lblStatUsed.setText(String.valueOf(used));
    }

    private void loadDataKMDetail(String maKM) {
        modelKMD.setRowCount(0);
        for (KhuyenMaiDetail d : daoKMD.getKhuyenMaiDetailByMaKM(maKM)) addKMDetailToTable(d);
    }

    // [SỬA] Đổi tên + mở rộng: tìm theo tên VÀ/HOẶC ngày bắt đầu, ngày kết thúc
    private void findKhuyenMai() {
        String ten   = txtSearch.getText().trim();
        Date ngayBD = dateFilterBD.getDate();  // null nếu chưa chọn
        Date ngayKT = dateFilterKT.getDate();  // null nếu chưa chọn

        modelKM.setRowCount(0); modelKMD.setRowCount(0);
        selectedKM = null; setDetailBtnsEnabled(false);
        lblKMDTitle.setText("Chi tiết — (chưa chọn khuyến mãi)");
        lblKMDTitle.setForeground(TEXT_LIGHT);

        // [THÊM MỚI] Gọi DAO với 3 tham số, null = bỏ qua điều kiện đó
        List<KhuyenMai> result = daoKM.searchKhuyenMai(ten, ngayBD, ngayKT);

        // Cập nhật stats theo kết quả tìm kiếm
//        int total = result.size(), active = 0, stopped = 0;
        for (KhuyenMai km : result) {
            if (km.isTrangThai()) //active++; else stopped++;
                addKhuyenMaiToTable(km);
        }
//        lblStatTotal.setText(String.valueOf(total));
//        lblStatActive.setText(String.valueOf(active));
//        lblStatStopped.setText(String.valueOf(stopped));
    }

    private void findKhuyenMaiByTen() { findKhuyenMai(); } // [THÊM] backward compat

    // ========== ADD ROWS ==========
    private void addKhuyenMaiToTable(KhuyenMai km) {
        modelKM.addRow(new Object[]{
                km.getMaKM(), km.getTenKM(),
                km.getNgayBatDau()  != null ? new SimpleDateFormat(DATE_FORMAT).format(km.getNgayBatDau()) : "",
                km.getNgayKetThuc() != null ? new SimpleDateFormat(DATE_FORMAT).format(km.getNgayKetThuc()) : "",
                km.isTrangThai() ? "Đang áp dụng" : "Dừng áp dụng",
//                km.getMoTa() != null ? km.getMoTa() : ""
        });
    }

    private void addKMDetailToTable(KhuyenMaiDetail d) {
        modelKMD.addRow(new Object[]{
                d.getMaKMDetail()
                , d.getTuyen() != null ? d.getTuyen().getMaTuyen() + " : " + d.getTuyen().getTenTuyen() : "Tất cả"
                , d.getLoaiToa() != null ? d.getLoaiToa().getTenLoaiToa() : "Tất cả"
                , d.getLoaiVe() != null ? d.getLoaiVe().getTenLoai() : "Tất cả"
                , d.getLoaiKM().getLabel()
                , formatGiaTri(d.getLoaiKM().getLabel(), d.getGiaTri())
                , d.isTrangThai() ? "Đang áp dụng" : "Dừng áp dụng"
        });
    }

    private String formatGiaTri(String loai, double v) {
        if ("Giảm %".equals(loai))  return String.format("%.0f%%", v);
        if ("Miễn phí".equals(loai)) return "—";
        return String.format("%,.0f đ", v);
    }

    // ========== UPDATE ROWS ==========
    private void updateTableRowKM(KhuyenMai km) {
        for (int i = 0; i < modelKM.getRowCount(); i++) {
            if (!modelKM.getValueAt(i, 0).equals(km.getMaKM())) continue;
            modelKM.setValueAt(km.getMaKM(),   i, 0);
            modelKM.setValueAt(km.getTenKM(),  i, 1);
            modelKM.setValueAt(km.getNgayBatDau()  != null ? new SimpleDateFormat(DATE_FORMAT).format(km.getNgayBatDau())  : "", i, 2);
            modelKM.setValueAt(km.getNgayKetThuc() != null ? new SimpleDateFormat(DATE_FORMAT).format(km.getNgayKetThuc()) : "", i, 3);
            modelKM.setValueAt(km.isTrangThai() ? "Đang áp dụng" : "Dừng áp dụng", i, 4);
            break;
        }
    }

    private void updateTableRowKMD(KhuyenMaiDetail d) {
        String id = String.valueOf(d.getMaKMDetail());
        // [SỬA] loaiVe nay là entity LoaiVe
        String tenLoaiVe = (d.getLoaiVe() != null) ? d.getLoaiVe().getTenLoai() : "";
        for (int i = 0; i < modelKMD.getRowCount(); i++) {
            if (!modelKMD.getValueAt(i, 0).toString().equals(id)) continue;
            modelKMD.setValueAt(d.getMaKMDetail(), i, 0);
            modelKMD.setValueAt(d.getTuyen().getMaTuyen() + " : " + d.getTuyen().getTenTuyen(), i, 1);
            modelKMD.setValueAt(d.getLoaiToa().getTenLoaiToa(),                    i, 2);
            modelKMD.setValueAt(tenLoaiVe,                          i, 3);
            modelKMD.setValueAt(d.getLoaiKM().getLabel(),           i, 4);
            modelKMD.setValueAt(formatGiaTri(d.getLoaiKM().getLabel(), d.getGiaTri()), i, 5);
            modelKMD.setValueAt(d.isTrangThai() ? "Đang áp dụng" : "Dừng áp dụng",   i, 6);
            break;
        }
    }

    // ========== DELETE ==========
    private void deleteKhuyenMai() {
        int row = tableKM.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một Khuyến mãi để xoá!"); return; }
        String maKM = modelKM.getValueAt(row, 0).toString();
        String tenKM = modelKM.getValueAt(row, 1).toString();
        if (JOptionPane.showConfirmDialog(this,
                "Xoá khuyến mãi \"" + tenKM + "\"?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.YES_OPTION) return;
        if (daoKM.setAnKhuyenMai(maKM)) {
            selectedKM = null; setDetailBtnsEnabled(false);
            lblKMDTitle.setText("Chi tiết — (chưa chọn khuyến mãi)");
            lblKMDTitle.setForeground(TEXT_LIGHT);
            loadDataKhuyenMai();
        } else JOptionPane.showMessageDialog(this, "Không thể xoá khuyến mãi này!");
    }

    private void deleteKMDetail() {
        int row = tableKMD.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng để xoá!"); return; }
        String id = modelKMD.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Xoá chi tiết khuyến mãi #" + id + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.YES_OPTION) return;
        if (daoKMD.setAnKMD(id))            loadDataKMDetail(selectedKM.getMaKM());
        else JOptionPane.showMessageDialog(this, "Không thể xoá chi tiết khuyến mãi này!");
    }

    // ========== DIALOG – KhuyenMai ==========
    private void openDialogKM(KhuyenMai km) {
        boolean isEdit = km != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dlg.setTitle(isEdit ? "Cập nhật khuyến mãi" : "Thêm khuyến mãi");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        form.setBackground(BG_CARD);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 6, 7, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField  txtMa     = makeField("Tự động sinh"); txtMa.setEditable(false);
        JTextField  txtTen    = makeField("Tên khuyến mãi");

        JDateChooser dpBD = makeStyledDateChooser();
        dpBD.setDate(new Date());

        JDateChooser dpKT = makeStyledDateChooser();
        dpKT.setDate(new Date());

        JCheckBox chkActive   = new JCheckBox("Đang áp dụng");
        chkActive.setBackground(BG_CARD);
        chkActive.setSelected(true);
        JTextArea txtMoTa = makeStyledTextArea(3, 22);

        if (isEdit) {
            txtMa.setText(km.getMaKM()); txtMa.setEditable(false);
            txtMa.setBackground(new Color(0xF0F4FA));
            txtTen.setText(km.getTenKM());
            chkActive.setSelected(km.isTrangThai());
            if (km.getMoTa() != null) txtMoTa.setText(km.getMoTa());
            dpBD.setDate(km.getNgayBatDau());
            dpKT.setDate(km.getNgayKetThuc());
            // Row 0 – Mã
            gc.gridx=0; gc.gridy=0; gc.weightx=0; form.add(makeLabel("Mã KM"),        gc);
            gc.gridx=1;              gc.weightx=1; form.add(txtMa,                      gc);
        }


        // Row 1 – Tên
        gc.gridx=0; gc.gridy=1; gc.weightx=0; form.add(makeLabel("Tên KM"),        gc);
        gc.gridx=1;              gc.weightx=1; form.add(txtTen,                     gc);
        // Row 2 – Ngày bắt đầu
        gc.gridx=0; gc.gridy=2; gc.weightx=0; form.add(makeLabel("Ngày bắt đầu"), gc);
        gc.gridx=1;              gc.weightx=1; form.add(dpBD,                       gc);
        // Row 3 – Ngày kết thúc
        gc.gridx=0; gc.gridy=3; gc.weightx=0; form.add(makeLabel("Ngày kết thúc"),gc);
        gc.gridx=1;              gc.weightx=1; form.add(dpKT,                       gc);
        // Row 4 – Trạng thái
        gc.gridx=0; gc.gridy=4; gc.weightx=0; form.add(makeLabel("Trạng thái"),    gc);
        gc.gridx=1;              gc.weightx=1; form.add(chkActive,                  gc);
        // Row 5 – Mô tả
        gc.gridx=0; gc.gridy=5; gc.weightx=0; gc.anchor=GridBagConstraints.NORTHWEST;
        form.add(makeLabel("Mô tả"), gc);
        gc.gridx=1; gc.weightx=1; gc.fill=GridBagConstraints.BOTH;
        form.add(new JScrollPane(txtMoTa), gc);

        JButton btnSave = makeBtn(isEdit ? "Cập nhật" : "Lưu", BtnStyle.PRIMARY);
        btnSave.addActionListener(e -> {
            if (txtTen.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Tên KM không được rỗng!"); return;
            }
            KhuyenMai obj = new KhuyenMai();
            obj.setMaKM(isEdit ? km.getMaKM() : null);
            obj.setTenKM(txtTen.getText().trim());
            obj.setNgayBatDau(dpBD.getDate());
            obj.setNgayKetThuc(dpKT.getDate());
            obj.setTrangThai(chkActive.isSelected());
            obj.setMoTa(txtMoTa.getText().trim());

            if (isEdit) {
                if (daoKM.updateKhuyenMai(obj)) {
                    // updateTableRowKM(obj);
                    loadDataKhuyenMai(); dlg.dispose(); }
                else JOptionPane.showMessageDialog(dlg, "Cập nhật thất bại!");
            } else {
                if (daoKM.insertKhuyenMai(obj)) { loadDataKhuyenMai(); dlg.dispose(); }
                else JOptionPane.showMessageDialog(dlg, "Thêm thất bại!");
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
        bottom.setBackground(BG_CARD);
        bottom.add(btnSave);
        if (isEdit) {
            JButton btnDel = makeBtn("Xoá KM", BtnStyle.DANGER);
            btnDel.addActionListener(e -> { dlg.dispose(); deleteKhuyenMai(); });
            bottom.add(btnDel);
        }

        dlg.getContentPane().setBackground(BG_CARD);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(430, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ========== DIALOG – KhuyenMaiDetail ==========
    private void openDialogKMD(KhuyenMaiDetail kmd) {
        if (selectedKM == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một Khuyến mãi trước!"); return;
        }
        boolean isEdit = kmd != null;

        // ── Nếu là EDIT: dialog đơn giản (1 tuyến cố định) ──────────────────
        if (isEdit) {
            openDialogKMDEdit(kmd);
            return;
        }

        // ── Nếu là THÊM MỚI: dialog chọn nhiều tuyến ────────────────────────
        openDialogKMDAdd();
    }

    /** Dialog THÊM – chọn nhiều tuyến + cấu hình KM, lưu 1 KMDetail/tuyến */
    private void openDialogKMDAdd() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dlg.setTitle("Thêm chi tiết — " + selectedKM.getTenKM());
        dlg.getContentPane().setBackground(BG_CARD);
        dlg.setLayout(new BorderLayout());

        // ── Lấy danh sách tuyến từ DAO ───────────────────────────────────────
        List<Tuyen> dsTuyen = daoKMD.getAllTuyen();

        // ══════════════════════════════════════════════════════════════════════
        // PANEL TRÁI – danh sách tuyến có thể chọn nhiều
        // ══════════════════════════════════════════════════════════════════════
        JPanel leftPanel = new JPanel(new BorderLayout(0, 6));
        leftPanel.setBackground(BG_CARD);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 10));

        // Tiêu đề + nút Tất cả
        JPanel leftTop = new JPanel(new BorderLayout(8, 0));
        leftTop.setOpaque(false);
        JLabel lblTuyen = makeLabel("Chọn tuyến");
        JButton btnAll = new JButton("Tất cả") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0xE8F1FB) : new Color(0xF0F6FF));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAll.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAll.setForeground(ACCENT);
        btnAll.setContentAreaFilled(false); btnAll.setBorderPainted(false);
        btnAll.setFocusPainted(false);
        btnAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAll.setPreferredSize(new Dimension(140, 28));
        leftTop.add(lblTuyen, BorderLayout.WEST);
        leftTop.add(btnAll,   BorderLayout.EAST);

        // Search tuyến
        JTextField txtSearchTuyen = makeField("Tìm tuyến...");
        txtSearchTuyen.setPreferredSize(new Dimension(0, 32));

        // List tuyến với checkbox
        DefaultListModel<Tuyen> listModel = new DefaultListModel<>();
        dsTuyen.forEach(listModel::addElement);

        JList<Tuyen> listTuyen = new JList<>(listModel);
//        listTuyen.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listTuyen.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                // Toggle thay vì replace
                if (isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                } else {
                    super.addSelectionInterval(index0, index1);
                }
            }
        });
        listTuyen.setFont(F_CELL);
        listTuyen.setBackground(BG_CARD);
        listTuyen.setFixedCellHeight(36);
        listTuyen.setCellRenderer(new TuyenCheckboxRenderer());

        // Nút Tất cả toggle
        btnAll.addActionListener(e -> {
            if (listTuyen.getSelectedIndices().length == listModel.getSize())
                listTuyen.clearSelection();
            else
                listTuyen.setSelectionInterval(0, listModel.getSize() - 1);
        });

        // Search filter
        txtSearchTuyen.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String q = txtSearchTuyen.getText().trim().toLowerCase();
                listModel.clear();
                dsTuyen.stream()
                        .filter(t -> t.getMaTuyen().toLowerCase().contains(q)
                                || t.getTenTuyen().toLowerCase().contains(q))
                        .forEach(listModel::addElement);
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        // Badge đếm số đã chọn
        JLabel lblCount = new JLabel("Chưa chọn tuyến nào");
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCount.setForeground(TEXT_LIGHT);
        listTuyen.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int n = listTuyen.getSelectedIndices().length;
                lblCount.setText(n == 0 ? "Chưa chọn tuyến nào"
                        : "Đã chọn " + n + " tuyến");
                lblCount.setForeground(n == 0 ? TEXT_LIGHT : ACCENT);
            }
        });

        JScrollPane scrollTuyen = new JScrollPane(listTuyen);
        scrollTuyen.setBorder(new LineBorder(BORDER, 1, true));
        scrollTuyen.setPreferredSize(new Dimension(240, 300));
        styleScrollBar(scrollTuyen.getVerticalScrollBar());

        leftPanel.add(leftTop,         BorderLayout.NORTH);
        JPanel searchWrap = new JPanel(new BorderLayout(0, 6));
        searchWrap.setOpaque(false);
        searchWrap.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        searchWrap.add(txtSearchTuyen, BorderLayout.CENTER);
        searchWrap.add(lblCount,       BorderLayout.SOUTH);
        leftPanel.add(searchWrap,      BorderLayout.CENTER);  // bố cục lại
        // Thực ra dùng BoxLayout cho gọn hơn:
        JPanel leftContent = new JPanel();
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));
        leftContent.setOpaque(false);
        leftContent.add(leftTop);
        leftContent.add(Box.createVerticalStrut(8));
        leftContent.add(txtSearchTuyen);
        leftContent.add(Box.createVerticalStrut(4));
        leftContent.add(lblCount);
        leftContent.add(Box.createVerticalStrut(6));
        leftContent.add(scrollTuyen);

        JPanel leftWrap = new JPanel(new BorderLayout());
        leftWrap.setBackground(BG_CARD);
        leftWrap.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 10));
        leftWrap.add(leftContent, BorderLayout.CENTER);

        // ══════════════════════════════════════════════════════════════════════
        // PANEL PHẢI – cấu hình loại KM, giá trị, loại vé, đối tượng
        // ══════════════════════════════════════════════════════════════════════
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(0xF7FAFF));
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(20, 16, 10, 20)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 6, 7, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JLabel lblKM = new JLabel(selectedKM.getTenKM());
        lblKM.setFont(F_LABEL); lblKM.setForeground(ACCENT);

        List<LoaiVe> dsLVCombo = new java.util.ArrayList<>();
        dsLVCombo.add(TAT_CA_LOAI_VE);
        dsLVCombo.addAll(dsLoaiVe);

        JComboBox<LoaiVe> cbLoaiVe = makeStyledComboBox(dsLVCombo.toArray(new LoaiVe[0]));
        cbLoaiVe.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value != null ? value.getTenLoai() : "");
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            if (isSelected) lbl.setBackground(ROW_SEL);
            else lbl.setBackground(index % 2 == 0 ? Color.WHITE : ROW_ALT);
            if (value == TAT_CA_LOAI_VE) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setForeground(ACCENT);
            } else {
                lbl.setFont(F_CELL); lbl.setForeground(TEXT_DARK);
            }
            return lbl;
        });

        List<LoaiToa> dsLTCombo = new java.util.ArrayList<>();
        dsLTCombo.add(TAT_CA_LOAI_TOA);
        dsLTCombo.addAll(dsLoaiToa);

        JComboBox<LoaiToa> cbLoaiToa = makeStyledComboBox(dsLTCombo.toArray(new LoaiToa[0]));
        cbLoaiToa.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value != null ? value.getTenLoaiToa() : "");
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            if (isSelected) lbl.setBackground(ROW_SEL);
            else lbl.setBackground(index % 2 == 0 ? Color.WHITE : ROW_ALT);
            if (value == TAT_CA_LOAI_TOA) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setForeground(ACCENT);
            } else {
                lbl.setFont(F_CELL); lbl.setForeground(TEXT_DARK);
            }
            return lbl;
        });

        JComboBox<LoaiKhuyenMai> cbLoaiKM = makeStyledComboBox(Arrays.stream(LoaiKhuyenMai.values())
                .filter(v -> v != LoaiKhuyenMai.MIEN_PHI)
                .toArray(LoaiKhuyenMai[]::new)
        );
        cbLoaiKM.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.getLabel());
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            if (isSelected) lbl.setBackground(ROW_SEL);
            else lbl.setBackground(index % 2 == 0 ? Color.WHITE : ROW_ALT);
            return lbl;
        });

        JTextField txtGiaTri = makeField("0");
        JCheckBox chkActiveKMD = new JCheckBox("Đang áp dụng");
        chkActiveKMD.setSelected(true);
        chkActiveKMD.setBackground(BG_CARD);
        chkActiveKMD.setFont(F_CELL);
        chkActiveKMD.setForeground(TEXT_DARK);

        Runnable updateGiaTri = () -> {
            boolean free = LoaiKhuyenMai.MIEN_PHI.equals(cbLoaiKM.getSelectedItem());
            txtGiaTri.setEnabled(!free);
            txtGiaTri.setBackground(free ? new Color(0xF0F4FA) : Color.WHITE);
            if (free) txtGiaTri.setText("0");
        };
        cbLoaiKM.addActionListener(e -> updateGiaTri.run());
        updateGiaTri.run();

        int r = 0;
        gc.gridx=0; gc.gridy=r;   gc.weightx=0; rightPanel.add(makeLabel("Khuyến mãi"), gc);
        gc.gridx=1;                gc.weightx=1; rightPanel.add(lblKM,                   gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; rightPanel.add(makeLabel("Loại vé"),    gc);
        gc.gridx=1;                gc.weightx=1; rightPanel.add(cbLoaiVe,                gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; rightPanel.add(makeLabel("Loại ghế"),  gc);
        gc.gridx=1;                gc.weightx=1; rightPanel.add(cbLoaiToa,              gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; rightPanel.add(makeLabel("Loại KM"),    gc);
        gc.gridx=1;                gc.weightx=1; rightPanel.add(cbLoaiKM,                gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; rightPanel.add(makeLabel("Giá trị"),    gc);
        gc.gridx=1;                gc.weightx=1; rightPanel.add(txtGiaTri,               gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; rightPanel.add(makeLabel("Trạng thái"),    gc);
        gc.gridx=1;                gc.weightx=1; rightPanel.add(chkActiveKMD,               gc);

        // Gợi ý: hiển thị số tuyến sẽ được tạo
        JLabel lblPreview = new JLabel(" ");
        lblPreview.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblPreview.setForeground(TEXT_MID);
        gc.gridx=0; gc.gridy=++r; gc.gridwidth=2;
        rightPanel.add(lblPreview, gc);
        gc.gridwidth=1;

        listTuyen.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int n = listTuyen.getSelectedIndices().length;
                lblPreview.setText(n == 0 ? " " : "→ Sẽ tạo " + n + " chi tiết khuyến mãi");
            }
        });

        // ══════════════════════════════════════════════════════════════════════
        // BOTTOM – nút Lưu / Hủy
        // ══════════════════════════════════════════════════════════════════════
        JButton btnSave   = makeBtn("Lưu", BtnStyle.PRIMARY);
        JButton btnCancel = makeBtn("Hủy", BtnStyle.SECONDARY);

        btnSave.addActionListener(e -> {
            List<Tuyen> selected = listTuyen.getSelectedValuesList();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng chọn ít nhất một tuyến!"); return;
            }
            double giaTri = 0;
            if (!LoaiKhuyenMai.MIEN_PHI.equals(cbLoaiKM.getSelectedItem())) {
                String txt = txtGiaTri.getText().trim();
                if (txt.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Vui lòng nhập giá trị!");
                    return;
                }

                try {
                    giaTri = Double.parseDouble(txt);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg, "Giá trị phải là số!");
                    return;
                }

                if (LoaiKhuyenMai.GIAM_PHAN_TRAM.equals(cbLoaiKM.getSelectedItem()) && (giaTri > 100.0 || giaTri < 0.0)) {
                    JOptionPane.showMessageDialog(dlg, "Giá trị phải từ 0 đến 100!");
                    return;
                }
            }
            final double giaTriFinal = giaTri;

            int ok = 0, fail = 0;

            LoaiVe lv = (LoaiVe) cbLoaiVe.getSelectedItem();
            if (lv != null && "ALL".equals(lv.getMaLoai())) {
                lv = null;
            }

            LoaiToa lt = (LoaiToa) cbLoaiToa.getSelectedItem();
            if (lt != null && "ALL".equals(lt.getMaLoaiToa())) {
                lt = null;
            }

            if(selected.size()==dsTuyen.size()){
                KhuyenMaiDetail obj = new KhuyenMaiDetail();
                obj.setKhuyenMai(selectedKM);
                obj.setTuyen(null);
                // [SỬA] set LoaiVe object thay vì String
                obj.setLoaiVe(lv);
                obj.setLoaiToa(lt);
                obj.setLoaiKM((LoaiKhuyenMai) cbLoaiKM.getSelectedItem());
                obj.setGiaTri(giaTriFinal);
                obj.setTrangThai(chkActiveKMD.isSelected());
                if (daoKMD.insertKhuyenMaiDetail(obj)) ok++;
                else fail++;
            } else {
                for (Tuyen tuyen : selected) {
                    KhuyenMaiDetail obj = new KhuyenMaiDetail();
                    obj.setKhuyenMai(selectedKM);
                    obj.setTuyen(tuyen);
                    // [SỬA] set LoaiVe object thay vì String
                    obj.setLoaiVe(lv);
                    obj.setLoaiToa(lt);
                    obj.setLoaiKM((LoaiKhuyenMai) cbLoaiKM.getSelectedItem());
                    obj.setGiaTri(giaTriFinal);
                    obj.setTrangThai(chkActiveKMD.isSelected());
                    if (daoKMD.insertKhuyenMaiDetail(obj)) ok++;
                    else fail++;
                }
            }
            // Reload bảng detail
            loadDataKMDetail(selectedKM.getMaKM());
            dlg.dispose();

            if (fail > 0)
                JOptionPane.showMessageDialog(this,
                        "Đã lưu " + ok + " tuyến, thất bại " + fail + " tuyến.");
        });

        btnCancel.addActionListener(e -> dlg.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
        bottom.setBackground(BG_CARD);
        bottom.add(btnSave);
        bottom.add(btnCancel);

        // ── Ghép layout ──────────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftWrap, rightPanel);
        split.setDividerSize(1);
        split.setDividerLocation(280);
        split.setBorder(BorderFactory.createEmptyBorder());

        dlg.add(split,  BorderLayout.CENTER);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setSize(680, dlg.getHeight() + 20);
        dlg.setMinimumSize(new Dimension(600, 450));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /** Dialog SỬA – giống cũ, chỉ 1 tuyến (maTuyen read-only) */
    private void openDialogKMDEdit(KhuyenMaiDetail kmd) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dlg.setTitle("Sửa chi tiết — " + selectedKM.getTenKM());
        dlg.getContentPane().setBackground(BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        form.setBackground(BG_CARD);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 6, 7, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JLabel lblKM = new JLabel(selectedKM.getTenKM());
        lblKM.setFont(F_LABEL); lblKM.setForeground(ACCENT);

        // Tuyến hiển thị read-only (không cho đổi khi sửa)
        String tuyen = kmd.getTuyen() != null ? kmd.getTuyen().getMaTuyen() + " : " + kmd.getTuyen().getTenTuyen() : "Tất cả";
        JTextField txtTuyen = makeField(tuyen);
        txtTuyen.setText(tuyen);
        txtTuyen.setEditable(false);
        txtTuyen.setBackground(new Color(0xF0F4FA));

        List<LoaiVe> dsLVCombo = new java.util.ArrayList<>();
        dsLVCombo.add(TAT_CA_LOAI_VE);
        dsLVCombo.addAll(dsLoaiVe);

        JComboBox<LoaiVe> cbLoaiVe = makeStyledComboBox(dsLVCombo.toArray(new LoaiVe[0]));
        cbLoaiVe.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value != null ? value.getTenLoai() : "");
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            if (isSelected) lbl.setBackground(ROW_SEL);
            else lbl.setBackground(index % 2 == 0 ? Color.WHITE : ROW_ALT);
            return lbl;
        });

        List<LoaiToa> dsLTCombo = new java.util.ArrayList<>();
        dsLTCombo.add(TAT_CA_LOAI_TOA);
        dsLTCombo.addAll(dsLoaiToa);

        JComboBox<LoaiToa> cbLoaiToa = makeStyledComboBox(dsLTCombo.toArray(new LoaiToa[0]));
        cbLoaiToa.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value != null ? value.getTenLoaiToa() : "");
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            if (isSelected) lbl.setBackground(ROW_SEL);
            else lbl.setBackground(index % 2 == 0 ? Color.WHITE : ROW_ALT);
            return lbl;
        });
        JComboBox<LoaiKhuyenMai> cbLoaiKM = makeStyledComboBox(Arrays.stream(LoaiKhuyenMai.values())
                .filter(v -> v != LoaiKhuyenMai.MIEN_PHI)
                .toArray(LoaiKhuyenMai[]::new)
        );
        cbLoaiKM.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.getLabel());
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            if (isSelected) lbl.setBackground(ROW_SEL);
            else lbl.setBackground(index % 2 == 0 ? Color.WHITE : ROW_ALT);
            return lbl;
        });
        JTextField txtGiaTri = makeField("0");

        // [SỬA] setSelectedItem bằng LoaiVe object (so sánh theo maLoai)
        if (kmd.getLoaiVe() != null) {
            for (int i = 0; i < cbLoaiVe.getItemCount(); i++) {
                if (cbLoaiVe.getItemAt(i).getMaLoai().equals(kmd.getLoaiVe().getMaLoai())) {
                    cbLoaiVe.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (kmd.getLoaiToa() != null) {
            for (int i = 0; i < cbLoaiToa.getItemCount(); i++) {
                if (cbLoaiToa.getItemAt(i).getMaLoaiToa().equals(kmd.getLoaiToa().getMaLoaiToa())) {
                    cbLoaiToa.setSelectedIndex(i);
                    break;
                }
            }
        }

        cbLoaiKM.setSelectedItem(kmd.getLoaiKM());
        txtGiaTri.setText(String.valueOf(kmd.getGiaTri()));

        JCheckBox chkActiveKMD   = new JCheckBox("Đang áp dụng");
        chkActiveKMD.setSelected(kmd.isTrangThai());
        chkActiveKMD.setBackground(BG_CARD);

        Runnable updateGiaTri = () -> {
            boolean free = LoaiKhuyenMai.MIEN_PHI.equals(cbLoaiKM.getSelectedItem());
            txtGiaTri.setEnabled(!free);
            txtGiaTri.setBackground(free ? new Color(0xF0F4FA) : Color.WHITE);
            if (free) txtGiaTri.setText("0");
        };
        cbLoaiKM.addActionListener(e -> updateGiaTri.run());
        updateGiaTri.run();

        int r = 0;
        gc.gridx=0; gc.gridy=r;   gc.weightx=0; form.add(makeLabel("Khuyến mãi"), gc);
        gc.gridx=1;                gc.weightx=1; form.add(lblKM,                   gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; form.add(makeLabel("Mã tuyến"),   gc);
        gc.gridx=1;                gc.weightx=1; form.add(txtTuyen,                gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; form.add(makeLabel("Loại Toa"),  gc);
        gc.gridx=1;                gc.weightx=1; form.add(cbLoaiToa,              gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; form.add(makeLabel("Loại vé"),    gc);
        gc.gridx=1;                gc.weightx=1; form.add(cbLoaiVe,                gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; form.add(makeLabel("Loại KM"),    gc);
        gc.gridx=1;                gc.weightx=1; form.add(cbLoaiKM,                gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; form.add(makeLabel("Giá trị"),    gc);
        gc.gridx=1;                gc.weightx=1; form.add(txtGiaTri,               gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; form.add(makeLabel("Trạng thái"),    gc);
        gc.gridx=1;                gc.weightx=1; form.add(chkActiveKMD,               gc);

        JButton btnSave = makeBtn("Cập nhật", BtnStyle.PRIMARY);
        btnSave.addActionListener(e -> {
            double giaTri = 0;
            if (!LoaiKhuyenMai.MIEN_PHI.equals(cbLoaiKM.getSelectedItem())) {
                String txt = txtGiaTri.getText().trim();
                if (txt.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Vui lòng nhập giá trị!");
                    return;
                }

                try {
                    giaTri = Double.parseDouble(txt);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg, "Giá trị phải là số!");
                    return;
                }

                if (LoaiKhuyenMai.GIAM_PHAN_TRAM.equals(cbLoaiKM.getSelectedItem()) && (giaTri > 100.0 || giaTri < 0.0)) {
                    JOptionPane.showMessageDialog(dlg, "Giá trị phải từ 0 đến 100!");
                    return;
                }
            }

            LoaiVe lv = (LoaiVe) cbLoaiVe.getSelectedItem();
            if (lv != null && "ALL".equals(lv.getMaLoai())) {
                lv = null;
            }

            LoaiToa lt = (LoaiToa) cbLoaiToa.getSelectedItem();
            if (lt != null && "ALL".equals(lt.getMaLoaiToa())) {
                lt = null;
            }

            KhuyenMaiDetail obj = new KhuyenMaiDetail();
            obj.setMaKMDetail(kmd.getMaKMDetail());
            obj.setKhuyenMai(selectedKM);
            obj.setTuyen(kmd.getTuyen());
            // [SỬA] set LoaiVe object thay vì String
            obj.setLoaiVe(lv);
            obj.setLoaiToa(lt);
            obj.setLoaiKM((LoaiKhuyenMai) cbLoaiKM.getSelectedItem());
            obj.setGiaTri(giaTri);
            obj.setTrangThai(chkActiveKMD.isSelected());

            if (daoKMD.updateKhuyenMaiDetail(obj)) { loadDataKMDetail(selectedKM.getMaKM()); dlg.dispose(); }
            else JOptionPane.showMessageDialog(dlg, "Cập nhật thất bại!");
        });

        JButton btnDel = makeBtn("Dừng KM", BtnStyle.DANGER);
        btnDel.addActionListener(e -> { dlg.dispose(); deleteKMDetail(); });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
        bottom.setBackground(BG_CARD);
        bottom.add(btnSave); bottom.add(btnDel);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(400, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /** Renderer checkbox cho JList tuyến */
    private class TuyenCheckboxRenderer implements javax.swing.ListCellRenderer<Tuyen> {
        private final JPanel  pnl = new JPanel(new BorderLayout(10, 0));
        private final JCheckBox chk = new JCheckBox();
        private final JLabel  lblMa  = new JLabel();
        private final JLabel  lblTen = new JLabel();

        TuyenCheckboxRenderer() {
            pnl.setOpaque(true);
            pnl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            chk.setOpaque(false);
            lblMa.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblMa.setForeground(ACCENT);
            lblTen.setFont(F_CELL);
            lblTen.setForeground(TEXT_DARK);
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 1));
            textPanel.setOpaque(false);
            textPanel.add(lblMa);
            textPanel.add(lblTen);
            pnl.add(chk,       BorderLayout.WEST);
            pnl.add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Tuyen> list,
                Tuyen value, int index,
                boolean isSelected, boolean cellHasFocus) {
            chk.setSelected(isSelected);
            lblMa.setText(value.getMaTuyen());
            lblTen.setText(value.getTenTuyen());
            pnl.setBackground(isSelected ? ROW_SEL : index % 2 == 0 ? BG_CARD : ROW_ALT);
            chk.setBackground(pnl.getBackground());
            return pnl;
        }
    }

    // ========== UTILS ==========
    // [THÊM] "Tất cả" → null (lưu vào DB), null → "Tất cả" (hiển thị lại UI)
    private static String tatCaToNull(String val) {
        return (val == null || "Tất cả".equals(val)) ? null : val;
    }
    private static String nullToTatCa(String val) {
        return (val == null) ? "Tất cả" : val;
    }

    /** null-object sentinel dùng cho "Tất cả" trong combobox entity */
    private static final LoaiVe  TAT_CA_LOAI_VE  = new LoaiVe()  {{ setMaLoai("ALL");     setTenLoai("Tất cả"); }};
    private static final LoaiToa TAT_CA_LOAI_TOA = new LoaiToa() {{ setMaLoaiToa("ALL");   setTenLoaiToa("Tất cả"); }};

    /** Sentinel "Tất cả" → null để lưu DB; giữ nguyên nếu là giá trị thật */
    private static LoaiVe  dbLoaiVe (LoaiVe  v) { return (v == null || v == TAT_CA_LOAI_VE)  ? null : v; }
    private static LoaiToa dbLoaiToa(LoaiToa v) { return (v == null || v == TAT_CA_LOAI_TOA) ? null : v; }

    // [SỬA] Enable lại btnDelDetail vì xóa mềm đã được bật
    private void setDetailBtnsEnabled(boolean on) {
        if (btnAddDetail != null) btnAddDetail.setEnabled(on);
        if (btnDelDetail != null) btnDelDetail.setEnabled(on); // [SỬA] đã bật lại
    }

    // ========== UI HELPERS ==========
    private JPanel buildCard(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(BG_CARD);
        p.setBorder(new ShadowBorder()); return p;
    }

    private JLabel makeLabel(String t) {
        JLabel l = new JLabel(t); l.setFont(F_LABEL); l.setForeground(TEXT_MID); return l;
    }

    // [SỬA] makeField – nâng cấp: nền F8FAFD, placeholder, focus border ACCENT_FOC
    private JTextField makeField(String placeHolder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_LIGHT);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    g2.drawString(placeHolder, getInsets().left + 4, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        tf.setFont(F_CELL);
        tf.setForeground(TEXT_DARK);
        tf.setBackground(new Color(0xF8FAFD));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACCENT_FOC, 2, true),
                        BorderFactory.createEmptyBorder(5, 9, 5, 9)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
        return tf;
    }

    // [THÊM MỚI] makeStyledComboBox – nền F8FAFD, border BORDER→ACCENT_FOC, arrow tùy chỉnh
    private <T> JComboBox<T> makeStyledComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(F_CELL);
        cb.setBackground(new Color(0xF8FAFD));
        cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(0, 4, 0, 4)));
        cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(0xF8FAFD));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(TEXT_MID);
                        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                        FontMetrics fm = g2.getFontMetrics();
                        String txt = "▾";
                        g2.drawString(txt,
                                (getWidth()  - fm.stringWidth(txt)) / 2,
                                (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                        g2.dispose();
                    }
                };
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                b.setPreferredSize(new Dimension(24, 0));
                return b;
            }
        });
        cb.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                cb.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACCENT_FOC, 2, true),
                        BorderFactory.createEmptyBorder(0, 3, 0, 3)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                cb.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        BorderFactory.createEmptyBorder(0, 4, 0, 4)));
            }
        });
        return cb;
    }

    // [THÊM MỚI] makeStyledDateChooser – nền F8FAFD, border BORDER→ACCENT_FOC
    private JDateChooser makeStyledDateChooser() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString(DATE_FORMAT);
        dc.setFont(F_CELL);
        dc.setBackground(new Color(0xF8FAFD));
        JTextField editor = (JTextField) dc.getDateEditor().getUiComponent();
        editor.setFont(F_CELL);
        editor.setForeground(TEXT_DARK);
        editor.setBackground(new Color(0xF8FAFD));
        editor.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 4));
        for (Component c : dc.getComponents()) {
            if (c instanceof JButton) {
                JButton calBtn = (JButton) c;
                calBtn.setPreferredSize(new Dimension(30, 0));
                calBtn.setBackground(new Color(0xF8FAFD));
                calBtn.setForeground(TEXT_MID);
                calBtn.setBorderPainted(false);
                calBtn.setFocusPainted(false);
                calBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                calBtn.setIcon(null);
                calBtn.setText("▦");
                calBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        }
        dc.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        editor.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                dc.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACCENT_FOC, 2, true),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                dc.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)));
            }
        });
        return dc;
    }

    // [THÊM MỚI] makeStyledTextArea – nền F8FAFD, border BORDER→ACCENT_FOC, wrap
    private JTextArea makeStyledTextArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(F_CELL);
        ta.setForeground(TEXT_DARK);
        ta.setBackground(new Color(0xF8FAFD));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        ta.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                ta.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACCENT_FOC, 2, true),
                        BorderFactory.createEmptyBorder(5, 9, 5, 9)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                ta.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
        return ta;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // SỬA LẠI THÀNH:
                switch (style) {
                    case PRIMARY:
                        g2.setColor(getModel().isPressed() ? new Color(0x0F3F8C)
                                : getModel().isRollover() ? ACCENT_HVR : ACCENT);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        break;
                    case DANGER:
                        g2.setColor(getModel().isPressed() ? new Color(0x922B21)
                                : getModel().isRollover() ? BTN_RED_HVR : BTN_RED);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        break;
                    default:
                        // code cho default
                        break;
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_LABEL);
        btn.setForeground(style == BtnStyle.SECONDARY ? BTN2_FG : Color.WHITE);
        // Cách cũ cho Java 8:
        int width = 110;
        if (style == BtnStyle.PRIMARY) width = 130;
        else if (style == BtnStyle.DANGER) width = 130;

        btn.setPreferredSize(new Dimension(width, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable t) {
        t.setFont(F_CELL); t.setRowHeight(38);
        t.setShowVerticalLines(false); t.setShowHorizontalLines(true);
        t.setGridColor(BORDER); t.setSelectionBackground(ROW_SEL);
        t.setSelectionForeground(TEXT_DARK);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFocusable(false); t.setIntercellSpacing(new Dimension(0, 0));
        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new HeaderRenderer());
        h.setPreferredSize(new Dimension(0, 42));
        h.setReorderingAllowed(false);
    }

    private void applyRenderers(JTable t, int n) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setFont(F_CELL); r.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        for (int i = 0; i < n; i++) t.getColumnModel().getColumn(i).setCellRenderer(r);
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() { thumbColor = new Color(0xC0D4EE); }
        });
    }

    // ========== HEADER RENDERER ==========
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() { setHorizontalAlignment(LEFT); }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setOpaque(true); l.setBackground(ACCENT); l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 6));
            return l;
        }
    }

    // ========== SHADOW BORDER ==========
    private static class ShadowBorder extends AbstractBorder {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0xE2EAF4));
            g2.drawRoundRect(x, y, w-1, h-1, 12, 12);
        }
    }

}