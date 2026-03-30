package com.gui;

import com.dao.DAO_Gia;
import com.dao.DAO_Gia.GiaHeaderRow;
import com.dao.DAO_Gia.GiaDetailRow;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;

/**
 * Tab Quản lý Giá vé
 *
 * Cấu trúc DB (data2.sql):
 *  - GiaHeader : maGia, moTa, ngayApDung, ngayKetThuc
 *  - GiaDetail : maGia, maLoaiToa, maLoaiVe, maTuyen, gia
 *
 * Nghiệp vụ:
 *  - 1 GiaHeader (mã bảng giá) có nhiều GiaDetail
 *  - Chọn dòng GiaHeader → hiển thị GiaDetail tương ứng bên phải
 *  - Double-click → mở dialog cập nhật
 *  - Loại toa: LT_GMC (Ghế mềm có điều hòa), LT_GCC (Ghế cứng có điều hòa), LT_GN4 (Nằm 4 chỗ)
 *  - Loại vé:  LV01 (Người lớn), LV02 (Trẻ em - giảm 50%), LV03 (Người cao tuổi - giảm 30%)
 *  - Tuyến:    T01 (HN→SG), T02 (SG→HN), T03 (HN→ĐN)
 */
public class TAB_Gia extends JPanel {

    // =========================================================================
    // MÀU SẮC
    // =========================================================================
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
    private static final Color BTN2_BG     = new Color(0xF0F4FA);
    private static final Color BTN2_FG     = new Color(0x3A5A8C);
    private static final Color BTN_RED     = new Color(0xC0392B);
    private static final Color BTN_RED_HVR = new Color(0xE74C3C);
    private static final Color TH_BG       = new Color(0xE8F0FB);
    private static final Color CLR_ON      = new Color(0x27AE60);
    private static final Color CLR_OFF     = new Color(0x7F8C8D);

    // =========================================================================
    // FONT
    // =========================================================================
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  18);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    // =========================================================================
    // CỘT BẢNG
    // GiaHeader: 4 hiển thị + 1 ẩn (ngayKetThuc raw)
    // =========================================================================
    private static final String[] COLS_GH = {
            "Mã Bảng Giá", "Mô Tả", "Ngày Áp Dụng", "Ngày Kết Thúc", "Trạng Thái",
            "_ngayKetThucRaw"  // ẩn, index 5
    };
    private static final String[] COLS_GD = {
            "Loại Toa", "Loại Vé", "Tuyến", "Giá (VND)",
            "_maLoaiToa", "_maLoaiVe", "_maTuyen"   // ẩn index 4,5,6
    };

    // =========================================================================
    // DỮ LIỆU DANH MỤC — khớp data2.sql
    // =========================================================================
    // maLoaiToa → tenLoaiToa
    private static final String[][] DS_LOAI_TOA = {
            {"LT_GMC", "Ghế mềm có ĐH"},
            {"LT_GCC", "Ghế cứng có ĐH"},
            {"LT_GN4", "Nằm 4 chỗ có ĐH"}
    };
    // maLoaiVe → tenLoaiVe, mucGiam%
    private static final String[][] DS_LOAI_VE = {
            {"LV01", "Người lớn",        "0"},
            {"LV02", "Trẻ em",           "50"},
            {"LV03", "Người cao tuổi",   "30"}
    };
    // maTuyen → tenTuyen
    private static final String[][] DS_TUYEN = {
            {"T01", "Hà Nội → Sài Gòn"},
            {"T02", "Sài Gòn → Hà Nội"},
            {"T03", "Hà Nội → Đà Nẵng"}
    };

    private static final String[] TRANG_THAI_ARR = {"Đang áp dụng", "Ngừng áp dụng"};

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER }

    // =========================================================================
    // DAO
    // =========================================================================
    private final DAO_Gia daoGia = new DAO_Gia();

    // =========================================================================
    // MODEL & TABLE
    // =========================================================================
    private final DefaultTableModel modelGH;
    private final DefaultTableModel modelGD;
    private final JTable            tblGH;
    private final JTable            tblGD;

    // Thông tin detail bên phải
    private final JLabel lblMaGia    = infoLbl("-");
    private final JLabel lblMoTa     = infoLbl("-");
    private final JLabel lblTuNgay   = infoLbl("-");
    private final JLabel lblDenNgay  = infoLbl("-");
    private final JLabel lblTrangThai= infoLbl("-");

    // =========================================================================
    // KHỞI TẠO
    // =========================================================================
    public TAB_Gia() {
        setLayout(new BorderLayout());
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        modelGH = new DefaultTableModel(COLS_GH, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        modelGD = new DefaultTableModel(COLS_GD, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblGH = buildTableGH();
        tblGD = buildTableGD();

        // Chọn dòng GiaHeader → làm mới GiaDetail bên phải
        tblGH.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) refreshDetail();
        });

        // Double-click → sửa
        tblGH.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openUpdateHeaderDialog();
            }
        });
        tblGD.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openUpdateDetailDialog();
            }
        });

        // Layout 55% trái / 45% phải — giống TAB_LichTrinh
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        GridBagConstraints gcL = new GridBagConstraints();
        gcL.gridx = 0; gcL.gridy = 0; gcL.weightx = 0.55; gcL.weighty = 1.0;
        gcL.fill = GridBagConstraints.BOTH; gcL.insets = new Insets(0, 0, 0, 6);
        body.add(buildLeftPanel(), gcL);

        GridBagConstraints gcR = new GridBagConstraints();
        gcR.gridx = 1; gcR.gridy = 0; gcR.weightx = 0.45; gcR.weighty = 1.0;
        gcR.fill = GridBagConstraints.BOTH; gcR.insets = new Insets(0, 6, 0, 0);
        body.add(buildRightPanel(), gcR);

        add(body, BorderLayout.CENTER);

        // Load data mẫu từ data2.sql
        loadFromDB();
    }

    // =========================================================================
    // PANEL TRÁI — danh sách GiaHeader
    // =========================================================================
    private JPanel buildLeftPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(BG_PAGE);

        JLabel title = new JLabel("QUẢN LÝ BẢNG GIÁ VÉ");
        title.setFont(F_TITLE);
        title.setForeground(TEXT_DARK);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel card = makeCard(new BorderLayout());
        JScrollPane sc = new JScrollPane(tblGH);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.getViewport().setBackground(BG_CARD);
        styleScrollBar(sc.getVerticalScrollBar());
        styleScrollBar(sc.getHorizontalScrollBar());
        card.add(sc, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnBar.setOpaque(false);
        JButton btnThem = makeBtn("Thêm bảng giá", BtnStyle.PRIMARY);
        JButton btnXoa  = makeBtn("Xóa",            BtnStyle.DANGER);
        btnThem.addActionListener(e -> openAddHeaderDialog());
        btnXoa .addActionListener(e -> deleteHeader());
        btnBar.add(btnThem);
        btnBar.add(btnXoa);

        pnl.add(top,    BorderLayout.NORTH);
        pnl.add(card,   BorderLayout.CENTER);
        pnl.add(btnBar, BorderLayout.SOUTH);
        return pnl;
    }

    // =========================================================================
    // PANEL PHẢI — chi tiết giá + thông tin bảng giá
    // =========================================================================
    private JPanel buildRightPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(BG_PAGE);

        JLabel title = new JLabel("CHI TIẾT GIÁ VÉ");
        title.setFont(F_TITLE);
        title.setForeground(TEXT_DARK);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // Bảng GiaDetail
        JPanel cardDetail = makeCard(new BorderLayout());
        JScrollPane scDetail = new JScrollPane(tblGD);
        scDetail.setBorder(BorderFactory.createEmptyBorder());
        scDetail.getViewport().setBackground(BG_CARD);
        scDetail.setPreferredSize(new Dimension(0, 200));
        scDetail.setMinimumSize(new Dimension(0, 200));
        styleScrollBar(scDetail.getVerticalScrollBar());
        cardDetail.add(scDetail, BorderLayout.CENTER);

        // Nút thêm/xóa detail
        JPanel detailBtnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        detailBtnBar.setOpaque(false);
        JButton btnThemDetail = makeBtn("Thêm chi tiết", BtnStyle.PRIMARY);
        JButton btnXoaDetail  = makeBtn("Xóa",           BtnStyle.DANGER);
        btnThemDetail.addActionListener(e -> openAddDetailDialog());
        btnXoaDetail .addActionListener(e -> deleteDetail());
        detailBtnBar.add(btnThemDetail);
        detailBtnBar.add(btnXoaDetail);
        cardDetail.add(detailBtnBar, BorderLayout.SOUTH);

        // Card thông tin bảng giá đang chọn
        JPanel cardInfo = buildInfoCard();

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(cardDetail, BorderLayout.CENTER);
        center.add(cardInfo,   BorderLayout.SOUTH);

        pnl.add(top,    BorderLayout.NORTH);
        pnl.add(center, BorderLayout.CENTER);
        return pnl;
    }

    // =========================================================================
    // CARD THÔNG TIN BẢNG GIÁ ĐANG CHỌN
    // =========================================================================
    private JPanel buildInfoCard() {
        JPanel card = makeCard(new BorderLayout());
        card.setPreferredSize(new Dimension(0, 190));
        card.setMinimumSize(new Dimension(0, 190));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        body.add(sectionLbl("Thông tin Bảng Giá Đang Chọn"));
        body.add(Box.createVerticalStrut(8));
        body.add(makeTblHeader(new String[]{"Mã Bảng Giá", "Mô Tả"}));
        body.add(makeTblRow(new JLabel[]{lblMaGia, lblMoTa}));
        body.add(Box.createVerticalStrut(10));
        body.add(makeTblHeader(new String[]{"Ngày Áp Dụng", "Ngày Kết Thúc", "Trạng Thái"}));
        body.add(makeTblRow(new JLabel[]{lblTuNgay, lblDenNgay, lblTrangThai}));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // XÂY DỰNG BẢNG
    // =========================================================================
    private JTable buildTableGH() {
        JTable t = new JTable(modelGH) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        styleTable(t);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Ẩn cột raw index 5
        t.getColumnModel().getColumn(5).setMinWidth(0);
        t.getColumnModel().getColumn(5).setMaxWidth(0);
        t.getColumnModel().getColumn(5).setWidth(0);

        // Renderer trạng thái cột 4
        t.getColumnModel().getColumn(4).setCellRenderer(new TrangThaiRenderer());

        int[] w = {110, 200, 110, 110, 130};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        applyPaddingRenderer(t, 4); // không đè renderer cột 4
        return t;
    }

    private JTable buildTableGD() {
        JTable t = new JTable(modelGD) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        styleTable(t);
        // Ẩn 3 cột mã raw (index 4,5,6)
        for (int i = 4; i <= 6; i++) {
            t.getColumnModel().getColumn(i).setMinWidth(0);
            t.getColumnModel().getColumn(i).setMaxWidth(0);
            t.getColumnModel().getColumn(i).setWidth(0);
        }
        int[] w = {150, 130, 140, 130};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        applyPaddingRenderer(t, 4);
        return t;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(36); t.setFont(F_CELL);
        t.setBackground(BG_CARD);
        t.setSelectionBackground(new Color(0xDDEEFF));
        t.setSelectionForeground(TEXT_DARK);
        t.setGridColor(BORDER);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setFocusable(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new HeaderRenderer());
        h.setPreferredSize(new Dimension(0, 40));
        h.setReorderingAllowed(false);
    }

    private void applyPaddingRenderer(JTable t, int upTo) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setFont(F_CELL);
        r.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
        for (int i = 0; i < upTo; i++) {
            // Không ghi đè renderer cột trạng thái của tblGH
            if (t == tblGH && i == 4) continue;
            t.getColumnModel().getColumn(i).setCellRenderer(r);
        }
    }

    // =========================================================================
    // LÀM MỚI CHI TIẾT
    // =========================================================================
    private void refreshDetail() {
        modelGD.setRowCount(0);
        int row = tblGH.getSelectedRow();
        if (row < 0) { resetInfo(); return; }

        String maGia    = modelGH.getValueAt(row, 0).toString();
        String moTa     = modelGH.getValueAt(row, 1).toString();
        String tuNgay   = modelGH.getValueAt(row, 2).toString();
        String denNgay  = modelGH.getValueAt(row, 3).toString();
        String trangThai= modelGH.getValueAt(row, 4).toString();

        lblMaGia    .setText(maGia);
        lblMoTa     .setText(moTa);
        lblTuNgay   .setText(tuNgay);
        lblDenNgay  .setText(denNgay);
        lblTrangThai.setText(trangThai);
        lblTrangThai.setForeground(trangThai.equals("Đang áp dụng") ? CLR_ON : CLR_OFF);

        // Load GiaDetail tương ứng maGia từ DB (hoặc cache trong memory)
        loadDetailForMaGia(maGia);
    }

    private void resetInfo() {
        lblMaGia.setText("-"); lblMoTa.setText("-");
        lblTuNgay.setText("-"); lblDenNgay.setText("-"); lblTrangThai.setText("-");
        lblTrangThai.setForeground(TEXT_MID);
    }

    // =========================================================================
    // LOAD DỮ LIỆU — data2.sql: GiaHeader + GiaDetail
    // =========================================================================
    private void loadFromDB() {
        modelGH.setRowCount(0);
        try {
            List<GiaHeaderRow> list = daoGia.getAllHeader();
            java.time.LocalDate today = java.time.LocalDate.now();
            for (GiaHeaderRow h : list) {
                // Tính trạng thái dựa trên ngày hiện tại
                String trangThai = "Ngừng áp dụng";
                try {
                    java.time.LocalDate den = java.time.LocalDate.parse(h.ngayKetThuc);
                    java.time.LocalDate tu  = java.time.LocalDate.parse(h.ngayApDung);
                    if (!today.isBefore(tu) && !today.isAfter(den)) trangThai = "Đang áp dụng";
                } catch (Exception ignored) {}

                modelGH.addRow(new Object[]{
                        h.maGia,
                        h.moTa,
                        formatNgay(h.ngayApDung),
                        formatNgay(h.ngayKetThuc),
                        trangThai,
                        h.ngayKetThuc   // raw ẩn
                });
            }
            if (modelGH.getRowCount() > 0) {
                tblGH.setRowSelectionInterval(0, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            warn("Không thể tải dữ liệu: " + e.getMessage());
        }
    }

    private void loadDetailForMaGia(String maGia) {
        modelGD.setRowCount(0);
        try {
            List<GiaDetailRow> list = daoGia.getDetailByMaGia(maGia);
            for (GiaDetailRow d : list) {
                modelGD.addRow(new Object[]{
                        tenLoaiToa(d.maLoaiToa),
                        tenLoaiVe(d.maLoaiVe),
                        tenTuyen(d.maTuyen),
                        formatGia(d.gia),
                        d.maLoaiToa,   // ẩn index 4
                        d.maLoaiVe,    // ẩn index 5
                        d.maTuyen      // ẩn index 6
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // DIALOG THÊM GIA HEADER
    // =========================================================================
    private void openAddHeaderDialog() {
        JDialog dlg = makeDialog("Thêm Bảng Giá Mới");

        JTextField txtMaGia   = makeField("VD: G2027");
        JTextField txtMoTa    = makeField("VD: Bảng giá năm 2027");
        JTextField txtTuNgay  = makeField("VD: 01/01/2027");
        JTextField txtDenNgay = makeField("VD: 31/12/2027");
        JComboBox<String> cbTT = makeCombo(TRANG_THAI_ARR);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();
        int r = 0;
        addRow(form, gc, r++, "Mã bảng giá:", txtMaGia);
        addRow(form, gc, r++, "Mô tả:",        txtMoTa);
        addRow(form, gc, r++, "Ngày áp dụng:", txtTuNgay);
        addRow(form, gc, r++, "Ngày kết thúc:",txtDenNgay);
        addRow(form, gc, r,   "Trạng thái:",   cbTT);

        JButton btnLuu = makeBtn("Lưu", BtnStyle.PRIMARY);
        btnLuu.addActionListener(e -> {
            String ma   = txtMaGia.getText().trim();
            String moTa = txtMoTa.getText().trim();
            String tu   = txtTuNgay.getText().trim();   // dd/MM/yyyy
            String den  = txtDenNgay.getText().trim();  // dd/MM/yyyy
            if (ma.isEmpty() || moTa.isEmpty() || tu.isEmpty() || den.isEmpty()) {
                warn("Vui lòng điền đầy đủ thông tin."); return;
            }
            String tuRaw  = toDbDate(tu);
            String denRaw = toDbDate(den);
            boolean ok = daoGia.insertHeader(ma, moTa, tuRaw, denRaw);
            if (ok) {
                String trangThai = tinhTrangThai(tuRaw, denRaw);
                modelGH.addRow(new Object[]{ma, moTa, tu, den, trangThai, denRaw});
                dlg.dispose();
            } else {
                warn("Lỗi khi lưu vào database! Kiểm tra lại Mã bảng giá.");
            }
        });
        showDlg(dlg, form, btnLuu);
    }

    // =========================================================================
    // DIALOG CẬP NHẬT GIA HEADER
    // =========================================================================
    private void openUpdateHeaderDialog() {
        int row = tblGH.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một bảng giá."); return; }

        String maGia    = modelGH.getValueAt(row, 0).toString();
        String moTa     = modelGH.getValueAt(row, 1).toString();
        String tuNgay   = modelGH.getValueAt(row, 2).toString();
        String denNgay  = modelGH.getValueAt(row, 3).toString();
        String trangThai= modelGH.getValueAt(row, 4).toString();

        JDialog dlg = makeDialog("Cập Nhật Bảng Giá");

        JTextField txtMaGia   = roField(maGia);
        JTextField txtMoTa    = makeFieldVal(moTa);
        JTextField txtTuNgay  = makeFieldVal(tuNgay);
        JTextField txtDenNgay = makeFieldVal(denNgay);
        JComboBox<String> cbTT = makeCombo(TRANG_THAI_ARR);
        cbTT.setSelectedItem(trangThai);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();
        int r = 0;
        addRow(form, gc, r++, "Mã bảng giá:", txtMaGia);
        addRow(form, gc, r++, "Mô tả:",        txtMoTa);
        addRow(form, gc, r++, "Ngày áp dụng:", txtTuNgay);
        addRow(form, gc, r++, "Ngày kết thúc:",txtDenNgay);
        addRow(form, gc, r,   "Trạng thái:",   cbTT);

        final int selRow = row;
        JButton btnLuu = makeBtn("Cập nhật", BtnStyle.PRIMARY);
        btnLuu.addActionListener(e -> {
            String moTaNew = txtMoTa.getText().trim();
            String tuNew   = txtTuNgay.getText().trim();   // dd/MM/yyyy
            String denNew  = txtDenNgay.getText().trim();  // dd/MM/yyyy
            if (moTaNew.isEmpty() || tuNew.isEmpty() || denNew.isEmpty()) {
                warn("Vui lòng điền đầy đủ thông tin."); return;
            }
            String tuRaw  = toDbDate(tuNew);
            String denRaw = toDbDate(denNew);
            boolean ok = daoGia.updateHeader(maGia, moTaNew, tuRaw, denRaw);
            if (ok) {
                modelGH.setValueAt(moTaNew,              selRow, 1);
                modelGH.setValueAt(tuNew,                selRow, 2);
                modelGH.setValueAt(denNew,               selRow, 3);
                modelGH.setValueAt(tinhTrangThai(tuRaw, denRaw), selRow, 4);
                modelGH.setValueAt(denRaw,               selRow, 5);
                refreshDetail();
                dlg.dispose();
            } else {
                warn("Lỗi khi cập nhật database!");
            }
        });
        showDlg(dlg, form, btnLuu);
    }

    // =========================================================================
    // XÓA GIA HEADER
    // =========================================================================
    private void deleteHeader() {
        int row = tblGH.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một bảng giá để xóa."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Xóa bảng giá này sẽ xóa luôn các chi tiết giá liên quan.\nBạn có chắc chắn?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            String maGia = modelGH.getValueAt(row, 0).toString();
            boolean deleted = daoGia.deleteHeader(maGia);
            if (deleted) {
                modelGH.removeRow(row);
                modelGD.setRowCount(0);
                resetInfo();
            } else {
                warn("Lỗi khi xóa! Kiểm tra lại ràng buộc khóa ngoại.");
            }
        }
    }

    // =========================================================================
    // DIALOG THÊM GIA DETAIL
    // =========================================================================
    private void openAddDetailDialog() {
        int row = tblGH.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một bảng giá trước."); return; }
        String maGia = modelGH.getValueAt(row, 0).toString();

        JDialog dlg = makeDialog("Thêm Chi Tiết Giá — " + maGia);

        // ComboBox Loại Toa
        String[] tenToaArr = new String[DS_LOAI_TOA.length];
        for (int i = 0; i < DS_LOAI_TOA.length; i++) tenToaArr[i] = DS_LOAI_TOA[i][1];
        JComboBox<String> cbToa = makeCombo(tenToaArr);

        // ComboBox Loại Vé
        String[] tenVeArr = new String[DS_LOAI_VE.length];
        for (int i = 0; i < DS_LOAI_VE.length; i++)
            tenVeArr[i] = DS_LOAI_VE[i][1] + " (giảm " + DS_LOAI_VE[i][2] + "%)";
        JComboBox<String> cbVe = makeCombo(tenVeArr);

        // ComboBox Tuyến
        String[] tenTuyenArr = new String[DS_TUYEN.length];
        for (int i = 0; i < DS_TUYEN.length; i++) tenTuyenArr[i] = DS_TUYEN[i][1];
        JComboBox<String> cbTuyen = makeCombo(tenTuyenArr);

        JTextField txtGia = makeField("VD: 1150000");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();
        int r = 0;
        addRow(form, gc, r++, "Loại toa:",  cbToa);
        addRow(form, gc, r++, "Loại vé:",   cbVe);
        addRow(form, gc, r++, "Tuyến:",     cbTuyen);
        addRow(form, gc, r,   "Giá (VND):", txtGia);

        JButton btnLuu = makeBtn("Lưu", BtnStyle.PRIMARY);
        btnLuu.addActionListener(e -> {
            String giaStr = txtGia.getText().trim();
            if (giaStr.isEmpty()) { warn("Vui lòng nhập giá."); return; }
            long giaVal;
            try { giaVal = Long.parseLong(giaStr.replaceAll("[^0-9]", "")); }
            catch (NumberFormatException ex) { warn("Giá không hợp lệ."); return; }

            int idxToa   = cbToa.getSelectedIndex();
            int idxVe    = cbVe.getSelectedIndex();
            int idxTuyen = cbTuyen.getSelectedIndex();
            String maToa   = DS_LOAI_TOA[idxToa][0];
            String maVe    = DS_LOAI_VE[idxVe][0];
            String maTuyenSel = DS_TUYEN[idxTuyen][0];

            boolean ok = daoGia.insertDetail(maGia, maToa, maVe, maTuyenSel, giaVal);
            if (ok) {
                modelGD.addRow(new Object[]{
                        tenLoaiToa(maToa), tenLoaiVe(maVe), tenTuyen(maTuyenSel),
                        formatGia(giaVal),
                        maToa, maVe, maTuyenSel   // ẩn
                });
                dlg.dispose();
            } else {
                warn("Lỗi khi lưu! Tổ hợp (Toa / Vé / Tuyến) này có thể đã tồn tại.");
            }
        });
        showDlg(dlg, form, btnLuu);
    }

    // =========================================================================
    // DIALOG CẬP NHẬT GIA DETAIL
    // =========================================================================
    private void openUpdateDetailDialog() {
        int row = tblGD.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một dòng chi tiết giá."); return; }

        String tenToa   = modelGD.getValueAt(row, 0).toString();
        String tenVe    = modelGD.getValueAt(row, 1).toString();
        String tenTuyen = modelGD.getValueAt(row, 2).toString();
        String giaHienTai = modelGD.getValueAt(row, 3).toString()
                .replace(".", "").replace(" VND", "");

        JDialog dlg = makeDialog("Cập Nhật Chi Tiết Giá");

        JTextField txtToa   = roField(tenToa);
        JTextField txtVe    = roField(tenVe);
        JTextField txtTuyen = roField(tenTuyen);
        JTextField txtGia   = makeFieldVal(giaHienTai);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();
        int r = 0;
        addRow(form, gc, r++, "Loại toa:",  txtToa);
        addRow(form, gc, r++, "Loại vé:",   txtVe);
        addRow(form, gc, r++, "Tuyến:",     txtTuyen);
        addRow(form, gc, r,   "Giá (VND):", txtGia);

        // Đọc mã raw từ cột ẩn
        String maToa   = modelGD.getValueAt(row, 4).toString();
        String maVe    = modelGD.getValueAt(row, 5).toString();
        String maTuyenSel = modelGD.getValueAt(row, 6).toString();
        // maGia lấy từ header đang chọn
        String maGiaHdr = modelGH.getValueAt(tblGH.getSelectedRow(), 0).toString();

        final int selRow = row;
        JButton btnLuu = makeBtn("Cập nhật", BtnStyle.PRIMARY);
        btnLuu.addActionListener(e -> {
            String giaStr = txtGia.getText().trim();
            if (giaStr.isEmpty()) { warn("Vui lòng nhập giá."); return; }
            long giaVal;
            try { giaVal = Long.parseLong(giaStr.replaceAll("[^0-9]", "")); }
            catch (NumberFormatException ex) { warn("Giá không hợp lệ."); return; }
            boolean ok = daoGia.updateDetail(maGiaHdr, maToa, maVe, maTuyenSel, giaVal);
            if (ok) {
                modelGD.setValueAt(formatGia(giaVal), selRow, 3);
                dlg.dispose();
            } else {
                warn("Lỗi khi cập nhật database!");
            }
        });
        showDlg(dlg, form, btnLuu);
    }

    // =========================================================================
    // XÓA GIA DETAIL
    // =========================================================================
    private void deleteDetail() {
        int row = tblGD.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một dòng chi tiết giá để xóa."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa dòng chi tiết giá này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            String maToa      = modelGD.getValueAt(row, 4).toString();
            String maVe       = modelGD.getValueAt(row, 5).toString();
            String maTuyenSel = modelGD.getValueAt(row, 6).toString();
            String maGiaHdr   = modelGH.getValueAt(tblGH.getSelectedRow(), 0).toString();
            boolean deleted = daoGia.deleteDetail(maGiaHdr, maToa, maVe, maTuyenSel);
            if (deleted) {
                modelGD.removeRow(row);
            } else {
                warn("Lỗi khi xóa chi tiết giá!");
            }
        }
    }

    // =========================================================================
    // HELPER UI — giống TAB_LichTrinh
    // =========================================================================
    private JPanel makeCard(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG_CARD);
        p.setBorder(new ShadowBorder());
        return p;
    }

    private JPanel makeTblHeader(String[] cols) {
        JPanel p = new JPanel(new GridLayout(1, cols.length));
        p.setBackground(TH_BG);
        p.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, BORDER));
        for (String c : cols) {
            JLabel l = new JLabel(c);
            l.setFont(F_LABEL); l.setForeground(TEXT_DARK);
            l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 4));
            p.add(l);
        }
        return p;
    }

    private JPanel makeTblRow(JLabel[] labels) {
        JPanel p = new JPanel(new GridLayout(1, labels.length));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, BORDER));
        for (JLabel l : labels) {
            l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 4));
            p.add(l);
        }
        return p;
    }

    private static JLabel infoLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(F_CELL); l.setForeground(TEXT_DARK); return l;
    }

    private JLabel sectionLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(F_LABEL); l.setForeground(TEXT_DARK); return l;
    }

    private JTextField roField(String v) {
        JTextField tf = makeFieldVal(v);
        tf.setEditable(false);
        tf.setBackground(new Color(0xEEF2F8));
        return tf;
    }

    private JTextField makeField(String hint) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_LIGHT);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    Insets ins = getInsets();
                    g2.drawString(hint, ins.left + 4, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        styleField(tf); return tf;
    }

    private JTextField makeFieldVal(Object v) {
        JTextField tf = new JTextField(v != null ? v.toString() : "");
        styleField(tf); return tf;
    }

    private void styleField(JTextField tf) {
        tf.setFont(F_CELL); tf.setForeground(TEXT_DARK);
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
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(F_CELL); cb.setBackground(new Color(0xF8FAFD)); cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        return cb;
    }

    private GridBagConstraints defaultGC() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 6, 5, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;
        return gc;
    }

    private void addRow(JPanel form, GridBagConstraints gc, int row, String lbl, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        JLabel l = new JLabel(lbl); l.setFont(F_LABEL); l.setForeground(TEXT_MID);
        form.add(l, gc);
        gc.gridx = 1; gc.weightx = 1;
        field.setPreferredSize(new Dimension(260, 36));
        form.add(field, gc);
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                switch (style) {
                    case PRIMARY   -> { g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); }
                    case DANGER    -> { g2.setColor(getModel().isRollover() ? BTN_RED_HVR : BTN_RED);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); }
                    default        -> { g2.setColor(getModel().isRollover() ? new Color(0xE0ECFF) : BTN2_BG);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        g2.setColor(BORDER);
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8); }
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_LABEL);
        b.setForeground(style == BtnStyle.SECONDARY ? BTN2_FG : Color.WHITE);
        b.setPreferredSize(new Dimension(style == BtnStyle.DANGER ? 80 : 150, 36));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JDialog makeDialog(String title) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog d = (owner instanceof Frame)
                ? new JDialog((Frame) owner, title, true)
                : new JDialog((Dialog) owner, title, true);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(BG_PAGE);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        return d;
    }

    private void showDlg(JDialog dlg, JPanel form, JButton ok) {
        JButton huy = makeBtn("Hủy", BtnStyle.SECONDARY);
        huy.addActionListener(e -> dlg.dispose());
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        bar.setOpaque(false); bar.add(huy); bar.add(ok);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(bar,  BorderLayout.SOUTH);
        dlg.setResizable(false);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(460, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0xC0D4EE); trackColor = BG_PAGE;
            }
            @Override protected JButton createDecreaseButton(int o) { return zBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zBtn(); }
            private JButton zBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
            }
        });
    }

    // =========================================================================
    // HELPER — tra tên từ mã (dùng cho loadDetailForMaGia)
    // =========================================================================
    private String tenLoaiToa(String ma) {
        for (String[] r : DS_LOAI_TOA) if (r[0].equals(ma)) return r[1];
        return ma;
    }
    private String tenLoaiVe(String ma) {
        for (String[] r : DS_LOAI_VE) if (r[0].equals(ma)) return r[1];
        return ma;
    }
    private String tenTuyen(String ma) {
        for (String[] r : DS_TUYEN) if (r[0].equals(ma)) return r[1];
        return ma;
    }

    // =========================================================================
    // HELPER — tính trạng thái từ ngày raw yyyy-MM-dd
    // =========================================================================
    private String tinhTrangThai(String tuRaw, String denRaw) {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate tu  = java.time.LocalDate.parse(tuRaw);
            java.time.LocalDate den = java.time.LocalDate.parse(denRaw);
            if (!today.isBefore(tu) && !today.isAfter(den)) return "Đang áp dụng";
        } catch (Exception ignored) {}
        return "Ngừng áp dụng";
    }

    // =========================================================================
    // HELPER — chuyển đổi định dạng ngày
    // =========================================================================
    /** dd/MM/yyyy → yyyy-MM-dd để lưu DB */
    private String toDbDate(String display) {
        try {
            String[] p = display.split("/");
            if (p.length == 3) return p[2] + "-" + p[1] + "-" + p[0];
        } catch (Exception ignored) {}
        return display; // fallback: trả nguyên nếu đã đúng định dạng
    }

    /** yyyy-MM-dd → dd/MM/yyyy để hiển thị */
    private String formatNgay(String raw) {
        try {
            String[] p = raw.split("-");
            if (p.length == 3) return p[2] + "/" + p[1] + "/" + p[0];
        } catch (Exception ignored) {}
        return raw;
    }

    private String formatGia(long gia) {
        return String.format("%,d", gia).replace(',', '.') + " VND";
    }

    // =========================================================================
    // RENDERER TRẠNG THÁI
    // =========================================================================
    private static class TrangThaiRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String val = v != null ? v.toString() : "";
            if (val.equals("Đang áp dụng")) l.setForeground(new Color(0x27AE60));
            else                             l.setForeground(new Color(0x7F8C8D));
            if (!sel) l.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
            return l;
        }
    }

    // =========================================================================
    // RENDERER HEADER
    // =========================================================================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() { setHorizontalAlignment(LEFT); }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setOpaque(true); l.setBackground(ACCENT); l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
            return l;
        }
    }

    // =========================================================================
    // SHADOW BORDER
    // =========================================================================
    private static class ShadowBorder extends AbstractBorder {
        private static final int S = 4;
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = S; i > 0; i--) {
                g2.setColor(new Color(100, 140, 200, (int)(20.0 * (S - i) / S)));
                g2.drawRoundRect(x + i, y + i, w - 2*i - 1, h - 2*i - 1, 12, 12);
            }
            g2.setColor(new Color(0xE2EAF4)); g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
            g2.setColor(BG_CARD);
            g2.setClip(new RoundRectangle2D.Float(x+1, y+1, w-2, h-2, 12, 12));
            g2.fillRect(x+1, y+1, w-2, h-2);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(S, S, S, S); }
        @Override public Insets getBorderInsets(Component c, Insets ins) { ins.set(S, S, S, S); return ins; }
    }
}