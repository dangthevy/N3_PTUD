package com.gui;

import com.connectDB.ConnectDB;
import com.dao.DAO_KhuyenMai;
import com.dao.DAO_KhuyenMaiDetail;
import com.entities.KhuyenMai;
import com.entities.KhuyenMaiDetail;
import com.entities.Tuyen;
import com.enums.LoaiKhuyenMai;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TAB_KhuyenMai extends JPanel {

    // ================= COLOR =================
    private static final Color BG_PAGE     = new Color(0xF4F7FB);
    private static final Color BG_CARD     = Color.WHITE;
    private static final Color ACCENT      = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR  = new Color(0x2270CC);
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
            "Mã", "Tuyến", "Loại vé", "Đối tượng", "Loại KM", "Giá trị", "Trạng thái"
    };

    private static final String[] LOAI_VE   = { "Ghế cứng", "Ghế mềm điều hoà", "Giường nằm"};
    private static final String[] DOI_TUONG = { "Tất cả", "Sinh viên", "Người lớn", "Trẻ em" };

    // ================= FIELDS =================
    Connection conn;
    JTextField txtSearch;

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

        lblKMDTitle = new JLabel();

        loadDataKhuyenMai();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        topPanel.add(buildHeader());
        topPanel.add(Box.createVerticalStrut(10)); // khoảng cách
        topPanel.add(buildFilterCard());

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
    private JPanel buildFilterCard() {
        JPanel card = buildCard(new FlowLayout(FlowLayout.LEFT, 14, 14));
        txtSearch = makeField("Tên khuyến mãi...");
        JButton btnSearch = makeBtn("Tìm kiếm", BtnStyle.PRIMARY);
        JButton btnReset  = makeBtn("Làm mới",  BtnStyle.SECONDARY);
        card.add(makeLabel("Tên:")); card.add(txtSearch);
        card.add(btnSearch); card.add(btnReset);
        btnSearch.addActionListener(e -> findKhuyenMaiByTen());
        btnReset.addActionListener(e -> { txtSearch.setText(""); loadDataKhuyenMai(); });
        return card;
    }

    // ========== MAIN CARD ==========
    private JPanel buildMainCard() {
        JPanel card = buildCard(new BorderLayout());
        card.add(buildKMActionBar(), BorderLayout.NORTH);
        card.add(buildSplitBody(),   BorderLayout.CENTER);
        return card;
    }

    // ========== ACTION BAR – KM ==========
    private JPanel buildKMActionBar() {
        JPanel bar = new JPanel(new BorderLayout()); bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));
        JLabel lbl = new JLabel("Danh sách khuyến mãi");
        lbl.setFont(F_LABEL); lbl.setForeground(TEXT_DARK);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(0, 80, 0, 0));
        JButton btnAdd = makeBtn("+ Thêm KM", BtnStyle.PRIMARY);
        JButton btnDel = makeBtn("Dừng KM",    BtnStyle.DANGER);
        btnAdd.addActionListener(e -> openDialogKM(null));
        btnDel.addActionListener(e -> deleteKhuyenMai());
        right.add(btnAdd); right.add(btnDel);
        bar.add(lbl, BorderLayout.WEST); bar.add(right, BorderLayout.CENTER);
        return bar;
    }

    // ========== SPLIT ==========
    private JSplitPane buildSplitBody() {
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildKMPanel(), buildKMDPanel());
        sp.setDividerLocation(0.55);
        sp.setResizeWeight(0.55);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false); sp.setDividerSize(6);
        return sp;
    }

    // ========== PANEL TRÁI – KM ==========
    private JPanel buildKMPanel() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 4));
        JSeparator sep = new JSeparator(); sep.setForeground(BORDER);
        JScrollPane scroll = new JScrollPane(tableKM);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setPreferredSize(new Dimension(0, 400));
        styleScrollBar(scroll.getVerticalScrollBar());
        styleScrollBar(scroll.getHorizontalScrollBar());
        p.add(sep,    BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
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
        scroll.setPreferredSize(new Dimension(0, 400));
        styleScrollBar(scroll.getVerticalScrollBar());
        styleScrollBar(scroll.getHorizontalScrollBar());
        JPanel inner = new JPanel(new BorderLayout()); inner.setOpaque(false);
        inner.add(sep,    BorderLayout.NORTH);
        inner.add(scroll, BorderLayout.CENTER);
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    // ========== ACTION BAR – KMD ==========
    private JPanel buildKMDActionBar() {
        JPanel bar = new JPanel(new BorderLayout()); bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        lblKMDTitle.setText("Chi tiết — (chưa chọn khuyến mãi)");
        lblKMDTitle.setFont(F_LABEL); lblKMDTitle.setForeground(TEXT_LIGHT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);

        btnAddDetail  = makeBtn("+ Thêm", BtnStyle.PRIMARY);
//        btnEditDetail = makeBtn("✎ Sửa",  BtnStyle.SECONDARY);
        btnDelDetail  = makeBtn("Dừng áp dụng",  BtnStyle.DANGER);
        setDetailBtnsEnabled(false);

        btnAddDetail.addActionListener(e -> openDialogKMD(null));
//        btnEditDetail.addActionListener(e -> {
//            int row = tableKMD.getSelectedRow();
//            if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng để sửa!"); return; }
//            int id = Integer.parseInt(modelKMD.getValueAt(row, 0).toString());
//            openDialogKMD(daoKMD.getKhuyenMaiDetailByID(id));
//        });
        btnDelDetail.addActionListener(e -> deleteKMDetail());

        btnPanel.add(btnAddDetail);
//        btnPanel.add(btnEditDetail);
        btnPanel.add(btnDelDetail);
        bar.add(lblKMDTitle, BorderLayout.WEST); bar.add(btnPanel, BorderLayout.EAST);
        return bar;
    }

    // ========== TABLE – KM ==========
    private JTable buildTableKM() {
        JTable t = new JTable(modelKM) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        // Click đơn → load detail
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
        // Đôi click → sửa KM
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = t.getSelectedRow();
                    if (row >= 0) openDialogKM(daoKM.getKhuyenMaiByID(
                            modelKM.getValueAt(row, 0).toString()));
                }
            }
        });
        t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN); // cột cuối fill phần còn lại
        styleTable(t);
        int[] w = { 80, 180, 120, 120, 120 };  // Mã | Tên | Ngày BĐ | Ngày KT | Trạng thái
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            t.getColumnModel().getColumn(3).setMinWidth(115); // đảm bảo "Ngày kết thúc" không bị cắt
        }
        applyRenderers(t, COLS_KM.length);
        return t;
    }

    // ========== TABLE – KMD ==========
    private JTable buildTableKMD() {
        JTable t = new JTable(modelKMD) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        // Đôi click → sửa detail
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
        t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        styleTable(t);
        int[] w = { 80, 160, 90, 90, 80, 70, 90 }; // Mã | Tuyến | Loại vé | Đối tượng | Loại KM | Giá trị | Trạng thái
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        applyRenderers(t, COLS_KMD.length);
        return t;
    }

    // ========== LOAD DATA ==========
    private void loadDataKhuyenMai() {
        modelKM.setRowCount(0); modelKMD.setRowCount(0);
        selectedKM = null; setDetailBtnsEnabled(false);
        lblKMDTitle.setText("Chi tiết — (chưa chọn khuyến mãi)");
        lblKMDTitle.setForeground(TEXT_LIGHT);
        for (KhuyenMai km : daoKM.getAllKhuyenMai()) addKhuyenMaiToTable(km);
    }

    private void loadDataKMDetail(String maKM) {
        modelKMD.setRowCount(0);
        for (KhuyenMaiDetail d : daoKMD.getKhuyenMaiDetailByMaKM(maKM)) addKMDetailToTable(d);
    }

    private void findKhuyenMaiByTen() {
        modelKM.setRowCount(0); modelKMD.setRowCount(0);
        selectedKM = null; setDetailBtnsEnabled(false);
        lblKMDTitle.setText("Chi tiết — (chưa chọn khuyến mãi)");
        lblKMDTitle.setForeground(TEXT_LIGHT);
        for (KhuyenMai km : daoKM.findKhuyenMaiByTen(txtSearch.getText().trim()))
            addKhuyenMaiToTable(km);
    }

    // ========== ADD ROWS ==========
    private void addKhuyenMaiToTable(KhuyenMai km) {
        modelKM.addRow(new Object[]{
                km.getMaKM(), km.getTenKM(),
                km.getNgayBatDau()  != null ? new SimpleDateFormat(DATE_FORMAT).format(km.getNgayBatDau()) : "",
                km.getNgayKetThuc() != null ? new SimpleDateFormat(DATE_FORMAT).format(km.getNgayKetThuc()) : "",
                km.isTrangThai() ? "Đang áp dụng" : "Dừng hoạt động",
//                km.getMoTa() != null ? km.getMoTa() : ""
        });
    }

    private void addKMDetailToTable(KhuyenMaiDetail d) {
        modelKMD.addRow(new Object[]{
                d.getMaKMDetail()
                , d.getTuyen().getMaTuyen()+ " : " + d.getTuyen().getTenTuyen()
                , d.getLoaiVe()
                , d.getDoiTuong()
                , d.getLoaiKM().getLabel()
                , formatGiaTri(d.getLoaiKM().getLabel(), d.getGiaTri())
                , d.isTrangThai() ?  "Đang áp dụng" : "Dừng áp dụng"
        });
        System.out.println("TrangThai = " + d.isTrangThai());
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
            modelKM.setValueAt(km.isTrangThai() ? "Đang áp dụng" : "Đã hết hạn", i, 4);
            break;
        }
    }

    private void updateTableRowKMD(KhuyenMaiDetail d) {
        String id = String.valueOf(d.getMaKMDetail());
        for (int i = 0; i < modelKMD.getRowCount(); i++) {
            if (!modelKMD.getValueAt(i, 0).toString().equals(id)) continue;
            modelKMD.setValueAt(d.getMaKMDetail(), i, 0);
            modelKMD.setValueAt(d.getTuyen().getMaTuyen()+ " : " + d.getTuyen().getTenTuyen(),    i, 1);
            modelKMD.setValueAt(d.getLoaiVe(),     i, 2);
            modelKMD.setValueAt(d.getDoiTuong(),   i, 3);
            modelKMD.setValueAt(d.getLoaiKM().getLabel(),     i, 4);
            modelKMD.setValueAt(formatGiaTri(d.getLoaiKM().getLabel(), d.getGiaTri()), i, 5);
            modelKMD.setValueAt(d.isTrangThai() ? "Đang áp dụng" : "Dừng áp dụng",     i, 6);

            break;
        }
    }

    // ========== DELETE ==========
    private void deleteKhuyenMai() {
        int row = tableKM.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một Khuyến mãi để dừng áp dụng!"); return; }
        String maKM = modelKM.getValueAt(row, 0).toString();
        String tenKM = modelKM.getValueAt(row, 1).toString();
        if (JOptionPane.showConfirmDialog(this,
                "Dừng áp dụng \"" + tenKM + "\"",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.YES_OPTION) return;
        if (daoKM.setActiveKhuyenMai(maKM, false)) {
            selectedKM = null; setDetailBtnsEnabled(false);
            lblKMDTitle.setText("Chi tiết — (chưa chọn khuyến mãi)");
            lblKMDTitle.setForeground(TEXT_LIGHT);
            loadDataKhuyenMai();
        } else JOptionPane.showMessageDialog(this, "Không thể dừng áp dụng!");
    }

    private void deleteKMDetail() {
        int row = tableKMD.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng để dừng hoạt động!"); return; }
        String id = modelKMD.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Dừng chi tiết KM #" + id + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.YES_OPTION) return;
        if (daoKMD.setActiveKMD(id, false))            loadDataKMDetail(selectedKM.getMaKM());
        else JOptionPane.showMessageDialog(this, "Thất bại!");
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
        JDateChooser dpBD  = new JDateChooser();
        dpBD.setDateFormatString(DATE_FORMAT);
        dpBD.setDate(new Date()); // Mặc định ngày hiện tại

        JDateChooser dpKT  = new JDateChooser();
        dpKT.setDateFormatString(DATE_FORMAT);
        dpKT.setDate(new Date());

        JCheckBox chkActive   = new JCheckBox("Đang áp dụng");
        chkActive.setBackground(BG_CARD);
        chkActive.setSelected(true);
        JTextArea txtMoTa     = new JTextArea(3, 22);
        txtMoTa.setFont(F_CELL); txtMoTa.setLineWrap(true);
        txtMoTa.setBorder(new LineBorder(BORDER, 1, true));

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
                    System.out.println(chkActive.isSelected());
                    updateTableRowKM(obj); dlg.dispose(); }
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
            JButton btnDel = makeBtn("Dừng KM", BtnStyle.DANGER);
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
        btnAll.setPreferredSize(new Dimension(70, 28));
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

        JComboBox<String> cbLoaiVe   = new JComboBox<>(LOAI_VE);
        JComboBox<String> cbDoiTuong = new JComboBox<>(DOI_TUONG);
        JComboBox<LoaiKhuyenMai> cbLoaiKM = new JComboBox<>(LoaiKhuyenMai.values());
        cbLoaiKM.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.getLabel()); lbl.setOpaque(true);
            if (isSelected) { lbl.setBackground(ROW_SEL); } return lbl;
        });
        JTextField txtGiaTri = makeField("0");
        JCheckBox chkActiveKMD   = new JCheckBox("Đang áp dụng");
        chkActiveKMD.setSelected(true);
        chkActiveKMD.setBackground(BG_CARD);
        txtGiaTri.setBackground(Color.WHITE);

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
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; rightPanel.add(makeLabel("Đối tượng"),  gc);
        gc.gridx=1;                gc.weightx=1; rightPanel.add(cbDoiTuong,              gc);
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
                try { giaTri = Double.parseDouble(txtGiaTri.getText().trim()); }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg, "Giá trị phải là số!"); return;
                }
            }
            final double giaTriFinal = giaTri;

            int ok = 0, fail = 0;
            for (Tuyen tuyen : selected) {
                KhuyenMaiDetail obj = new KhuyenMaiDetail();
                obj.setKhuyenMai(selectedKM);
                obj.setTuyen(tuyen);
                obj.setLoaiVe((String) cbLoaiVe.getSelectedItem());
                obj.setDoiTuong((String) cbDoiTuong.getSelectedItem());
                obj.setLoaiKM((LoaiKhuyenMai) cbLoaiKM.getSelectedItem());
                obj.setGiaTri(giaTriFinal);
                obj.setTrangThai(chkActiveKMD.isSelected());
                if (daoKMD.insertKhuyenMaiDetail(obj)) ok++;
                else fail++;
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
        JTextField txtTuyen = makeField(kmd.getTuyen().getMaTuyen());
        txtTuyen.setText(kmd.getTuyen().getMaTuyen() + " : " + kmd.getTuyen().getTenTuyen());
        txtTuyen.setEditable(false);
        txtTuyen.setBackground(new Color(0xF0F4FA));

        JComboBox<String> cbLoaiVe   = new JComboBox<>(LOAI_VE);
        JComboBox<String> cbDoiTuong = new JComboBox<>(DOI_TUONG);
        JComboBox<LoaiKhuyenMai> cbLoaiKM = new JComboBox<>(LoaiKhuyenMai.values());
        cbLoaiKM.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.getLabel()); lbl.setOpaque(true);
            if (isSelected) lbl.setBackground(ROW_SEL); return lbl;
        });
        JTextField txtGiaTri = makeField("0");

        cbLoaiVe.setSelectedItem(kmd.getLoaiVe());
        cbDoiTuong.setSelectedItem(kmd.getDoiTuong());
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
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; form.add(makeLabel("Loại vé"),    gc);
        gc.gridx=1;                gc.weightx=1; form.add(cbLoaiVe,                gc);
        gc.gridx=0; gc.gridy=++r; gc.weightx=0; form.add(makeLabel("Đối tượng"),  gc);
        gc.gridx=1;                gc.weightx=1; form.add(cbDoiTuong,              gc);
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
                try { giaTri = Double.parseDouble(txtGiaTri.getText().trim()); }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg, "Giá trị phải là số!"); return;
                }
            }
            KhuyenMaiDetail obj = new KhuyenMaiDetail();
            obj.setMaKMDetail(kmd.getMaKMDetail());
            obj.setKhuyenMai(selectedKM);
            obj.setTuyen(kmd.getTuyen());
            obj.setLoaiVe((String) cbLoaiVe.getSelectedItem());
            obj.setDoiTuong((String) cbDoiTuong.getSelectedItem());
            obj.setLoaiKM((LoaiKhuyenMai) cbLoaiKM.getSelectedItem());
            obj.setGiaTri(giaTri);
            obj.setTrangThai(chkActiveKMD.isSelected());

            if (daoKMD.updateKhuyenMaiDetail(obj)) { updateTableRowKMD(obj); dlg.dispose(); }
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
    private void setDetailBtnsEnabled(boolean on) {
        if (btnAddDetail  != null) btnAddDetail.setEnabled(on);
//        if (btnEditDetail != null) btnEditDetail.setEnabled(on);
        if (btnDelDetail  != null) btnDelDetail.setEnabled(on);
    }

    // ========== UI HELPERS ==========
    private JPanel buildCard(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(BG_CARD);
        p.setBorder(new ShadowBorder()); return p;
    }

    private JLabel makeLabel(String t) {
        JLabel l = new JLabel(t); l.setFont(F_LABEL); l.setForeground(TEXT_MID); return l;
    }

    private JTextField makeField(String placeHolder) {
        JTextField tf = new JTextField(14); tf.setFont(F_CELL);
        tf.setBorder(new LineBorder(BORDER, 1, true)); return tf;
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