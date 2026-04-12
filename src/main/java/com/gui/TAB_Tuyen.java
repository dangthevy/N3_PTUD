package com.gui;

import com.dao.DAO_Ga;
import com.dao.DAO_Tuyen;
import com.entities.Ga;
import com.entities.Tuyen;
import com.formdev.flatlaf.FlatClientProperties;

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
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.KeyEvent;

public class TAB_Tuyen extends JPanel {

    // =========================================================================
    // MÀU SẮC & FONT
    // =========================================================================
    private static final Color BG_PAGE      = new Color(0xF4F7FB);
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color ACCENT       = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR   = new Color(0x2270CC);
    private static final Color ACCENT_FOC   = new Color(0x4D9DE0);
    private static final Color TEXT_DARK    = new Color(0x1E2B3C);
    private static final Color TEXT_MID     = new Color(0x5A6A7D);
    private static final Color TEXT_LIGHT   = new Color(0xA0AEC0);
    private static final Color BORDER       = new Color(0xE2EAF4);
    private static final Color ROW_ALT      = new Color(0xF7FAFF);
    private static final Color BTN2_BG      = new Color(0xF0F4FA);
    private static final Color BTN2_FG      = new Color(0x3A5A8C);
    private static final Color BTN_RED      = new Color(0xC0392B);
    private static final Color BTN_RED_HVR  = new Color(0xE74C3C);

    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER }

    // =========================================================================
    // THÀNH PHẦN GIAO DIỆN
    // =========================================================================
    private DefaultTableModel dataModel;
    private JTable table;
    private JTextField txtSearch;
    private JLabel lblTotal, lblActive, lblInactive;

    private final DAO_Tuyen dsTuyen = new DAO_Tuyen();
    private final DAO_Ga dsGa = new DAO_Ga();

    public TAB_Tuyen() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initUI();
        updateTableData();
    }

    private void initUI() {
        // ================= TOP PANEL (TITLE & DASHBOARD) =================
        JPanel pnlTop = new JPanel(new BorderLayout(0, 20));
        pnlTop.setOpaque(false);

        JLabel title = new JLabel("QUẢN LÝ TUYẾN ĐƯỜNG");
        title.setFont(F_TITLE); title.setForeground(ACCENT);
        pnlTop.add(title, BorderLayout.NORTH);

        JPanel pnlDashboard = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlDashboard.setOpaque(false);
        pnlDashboard.add(createStatCard("TỔNG SỐ TUYẾN", lblTotal = new JLabel("0"), ACCENT));
        pnlDashboard.add(createStatCard("ĐANG HOẠT ĐỘNG", lblActive = new JLabel("0"), new Color(39, 174, 96)));
        pnlDashboard.add(createStatCard("NGƯNG HOẠT ĐỘNG", lblInactive = new JLabel("0"), new Color(192, 57, 43)));
        pnlTop.add(pnlDashboard, BorderLayout.CENTER);

        // ================= CENTER PANEL (TOOL BAR & TABLE) =================
        JPanel centerPnl = makeCard(new BorderLayout(0, 15));
        centerPnl.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(), new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel pnlToolbar = new JPanel(new BorderLayout());
        pnlToolbar.setOpaque(false);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlSearch.setOpaque(false);
        txtSearch = makeField("Nhập tên hoặc mã tuyến để tìm kiếm...");
        txtSearch.setPreferredSize(new Dimension(300, 36));
        pnlSearch.add(txtSearch);

        JButton btnXoa = makeBtn("- Xóa", BtnStyle.DANGER);
        JButton btnThem = makeBtn("+ Thêm Mới", BtnStyle.PRIMARY);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnThem);

        pnlToolbar.add(pnlSearch, BorderLayout.WEST);
        pnlToolbar.add(pnlButtons, BorderLayout.EAST);

        // Đổi Header thành "Nơi Đi", "Nơi Đến"
        String[] cols = {"Mã Tuyến", "Tên Tuyến", "Thời gian", "Nơi Đi", "Nơi Đến"};
        dataModel = new DefaultTableModel(cols, 0);
        table = buildTable(dataModel);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER));
        scrollPane.getViewport().setBackground(BG_CARD);
        styleScrollBar(scrollPane.getVerticalScrollBar());

        centerPnl.add(pnlToolbar, BorderLayout.NORTH);
        centerPnl.add(scrollPane, BorderLayout.CENTER);

        // XỬ LÝ SỰ KIỆN TÌM KIẾM LIVE
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            private void thucHienTimKiem() {
                String tuKhoa = txtSearch.getText().trim();
                if(tuKhoa.equals("Nhập tên hoặc mã tuyến để tìm kiếm...")) return;

                List<Tuyen> listKetQua = dsTuyen.timKiemTuyen(tuKhoa);
                dataModel.setRowCount(0);
                for (Tuyen tuyen : listKetQua) {
                    int tongPhut = tuyen.getThoiGianChay();
                    String thoiGianHienThi = (tongPhut / 60) + "h " + (tongPhut % 60) + "m";
                    dataModel.addRow(new Object[]{
                            tuyen.getMaTuyen(), tuyen.getTenTuyen(), thoiGianHienThi,
                            tuyen.getGaDi() != null ? tuyen.getGaDi().getTenGa() : "",
                            tuyen.getGaDen() != null ? tuyen.getGaDen().getTenGa() : ""
                    });
                }
            }
        });

        btnThem.addActionListener(e -> hienThiDialogThemTuyen());
        btnXoa.addActionListener(e -> xoaTuyen());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                        hienThiDialogSuaTuyen(row);
                    }
                }
            }
        });

        add(pnlTop, BorderLayout.NORTH);
        add(centerPnl, BorderLayout.CENTER);
    }

    private void updateTableData() {
        List<Tuyen> list = dsTuyen.getAllTuyen();
        dataModel.setRowCount(0);
        for (Tuyen tuyen : list) {
            int tongPhut = tuyen.getThoiGianChay();
            String thoiGianHienThi = (tongPhut / 60) + "h " + (tongPhut % 60) + "m";

            // Hiển thị tên Ga thay vì mã Ga cho dễ nhìn
            dataModel.addRow(new Object[]{
                    tuyen.getMaTuyen(), tuyen.getTenTuyen(), thoiGianHienThi,
                    tuyen.getGaDi() != null ? tuyen.getGaDi().getTenGa() : "",
                    tuyen.getGaDen() != null ? tuyen.getGaDen().getTenGa() : ""
            });
        }

        // Cập nhật thẻ Dashboard
        lblTotal.setText(String.valueOf(list.size()));
        lblActive.setText(String.valueOf(list.size()));
        lblInactive.setText("0");
    }

    private void xoaTuyen() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn Tuyến cần xóa!"); return; }
        String maTuyen = table.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa Tuyến: " + maTuyen + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dsTuyen.deleteTuyen(maTuyen)) {
                JOptionPane.showMessageDialog(this, "Đã xóa Tuyến thành công!");
                updateTableData();
            } else {
                warn("Xóa Tuyến thất bại! Vui lòng thử lại.");
            }
        }
    }

    // =========================================================================
    // DIALOG THÊM / SỬA (VỚI TÍNH NĂNG TÌM GA THÔNG MINH)
    // =========================================================================
    private void hienThiDialogThemTuyen() {
        JDialog dialog = makeDialog("Thêm Mới Tuyến Đường");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); form.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        GridBagConstraints gc = defaultGC();

        JTextField txtMa = roField(dsTuyen.phatSinhMaTuyen());
        JTextField txtTen = roField("");

        // Khởi tạo dữ liệu Ga kèm Tỉnh Thành
        List<Ga> listGa = dsGa.getAllGa();
        String[] arrGaStr = new String[listGa.size()];
        Map<String, Ga> mapGa = new HashMap<>(); // Map để truy xuất lại Ga gốc từ chuỗi hiển thị

        for (int i = 0; i < listGa.size(); i++) {
            Ga g = listGa.get(i);
            // Chuỗi hiển thị: "Tên Ga (Tỉnh Thành)"
            String displayStr = g.getTenGa() + " (" + g.getTinhThanh() + ")";
            arrGaStr[i] = displayStr;
            mapGa.put(displayStr, g);
        }

        JComboBox<String> cbNoiDi = makeCombo(arrGaStr); cbNoiDi.setEditable(true);
        JComboBox<String> cbNoiDen = makeCombo(arrGaStr); cbNoiDen.setEditable(true);

        setupAutoSelectAll(cbNoiDi); setupAutoSelectAll(cbNoiDen);
        applySmartFilterGa(cbNoiDi, arrGaStr);
        applySmartFilterGa(cbNoiDen, arrGaStr);

        Runnable updateTenTuyen = () -> {
            String selDi = (String) cbNoiDi.getSelectedItem();
            String selDen = (String) cbNoiDen.getSelectedItem();
            if (selDi != null && selDen != null && mapGa.containsKey(selDi) && mapGa.containsKey(selDen)) {
                txtTen.setText(mapGa.get(selDi).getTenGa() + " - " + mapGa.get(selDen).getTenGa());
            }
        };

        cbNoiDi.addActionListener(e -> updateTenTuyen.run());
        cbNoiDen.addActionListener(e -> updateTenTuyen.run());

        // Thời gian
        String[] allHours = new String[100]; for (int i = 0; i <= 99; i++) allHours[i] = String.format("%02d", i);
        String[] allMinutes = new String[60]; for (int i = 0; i <= 59; i++) allMinutes[i] = String.format("%02d", i);

        JComboBox<String> cbGio = makeCombo(allHours); cbGio.setEditable(true);
        cbGio.setPreferredSize(new Dimension(75, 36));

        JComboBox<String> cbPhut = makeCombo(allMinutes); cbPhut.setEditable(true);
        cbPhut.setPreferredSize(new Dimension(75, 36));

        setupAutoSelectAll(cbGio); setupAutoSelectAll(cbPhut);
        applySmartFilterTime(cbGio, allHours); applySmartFilterTime(cbPhut, allMinutes);
        cbGio.setSelectedItem("00"); cbPhut.setSelectedItem("00");

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        timePanel.setOpaque(false);
        timePanel.add(cbGio); timePanel.add(new JLabel("Giờ"));
        timePanel.add(Box.createHorizontalStrut(10));
        timePanel.add(cbPhut); timePanel.add(new JLabel("Phút"));

        int r = 0;
        addRow(form, gc, r++, "Mã Tuyến:", txtMa);
        addRow(form, gc, r++, "Nơi Đi:", cbNoiDi); // Thay đổi nhãn
        addRow(form, gc, r++, "Nơi Đến:", cbNoiDen); // Thay đổi nhãn
        addRow(form, gc, r++, "Tên tuyến:", txtTen);
        addRow(form, gc, r++, "Thời gian:", timePanel);

        JButton btnHuy = makeBtn("Hủy", BtnStyle.SECONDARY);
        JButton btnThem = makeBtn("Thêm Tuyến", BtnStyle.PRIMARY);

        btnHuy.addActionListener(e -> dialog.dispose());
        btnThem.addActionListener(e -> {
            String selDi = cbNoiDi.getSelectedItem() != null ? cbNoiDi.getSelectedItem().toString() : "";
            String selDen = cbNoiDen.getSelectedItem() != null ? cbNoiDen.getSelectedItem().toString() : "";

            if (!mapGa.containsKey(selDi) || !mapGa.containsKey(selDen)) {
                warn("Vui lòng chọn Nơi Đi và Nơi Đến hợp lệ từ danh sách gợi ý!"); return;
            }
            if (selDi.equalsIgnoreCase(selDen)) { warn("Nơi đi và Nơi đến không được trùng nhau!"); return; }

            int thoiGian = 0;
            try {
                int gio = Integer.parseInt(cbGio.getSelectedItem().toString().trim());
                int phut = Integer.parseInt(cbPhut.getSelectedItem().toString().trim());
                thoiGian = (gio * 60) + phut;
                if (thoiGian <= 0 || thoiGian >= (100 * 60)) { warn("Thời gian không hợp lệ!"); return; }
            } catch (Exception ex) { warn("Thời gian chạy không hợp lệ!"); return; }

            Ga gaDi = mapGa.get(selDi);
            Ga gaDen = mapGa.get(selDen);

            for (int i = 0; i < dsTuyen.getAllTuyen().size(); i++) {
                Tuyen existT = dsTuyen.getAllTuyen().get(i);
                if (existT.getGaDi().getMaGa().equals(gaDi.getMaGa()) && existT.getGaDen().getMaGa().equals(gaDen.getMaGa())) {
                    warn("Tuyến đường từ " + gaDi.getTenGa() + " đến " + gaDen.getTenGa() + " đã tồn tại!"); return;
                }
            }

            Tuyen tuyenMoi = new Tuyen(txtMa.getText(), txtTen.getText(), thoiGian, gaDi, gaDen);
            if (dsTuyen.addTuyen(tuyenMoi)) {
                JOptionPane.showMessageDialog(dialog, "Thêm tuyến mới thành công!");
                updateTableData();
                dialog.dispose();
            } else {
                warn("Thêm thất bại!");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setOpaque(false); btnPanel.add(btnHuy); btnPanel.add(btnThem);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    private void hienThiDialogSuaTuyen(int row) {
        String maTuyen = table.getValueAt(row, 0).toString();
        Tuyen tuyenHienTai = dsTuyen.getTuyenByMa(maTuyen);
        if (tuyenHienTai == null) { warn("Không tìm thấy dữ liệu tuyến này!"); return; }

        JDialog dialog = makeDialog("Cập nhật Tuyến");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); form.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        GridBagConstraints gc = defaultGC();

        JTextField txtMaSua = roField(tuyenHienTai.getMaTuyen());
        JTextField txtTenSua = roField(tuyenHienTai.getTenTuyen());

        List<Ga> listGa = dsGa.getAllGa();
        String[] arrGaStr = new String[listGa.size()];
        Map<String, Ga> mapGa = new HashMap<>();

        for (int i = 0; i < listGa.size(); i++) {
            Ga g = listGa.get(i);
            String displayStr = g.getTenGa() + " (" + g.getTinhThanh() + ")";
            arrGaStr[i] = displayStr;
            mapGa.put(displayStr, g);
        }

        JComboBox<String> cbNoiDiSua = makeCombo(arrGaStr); cbNoiDiSua.setEditable(true);
        JComboBox<String> cbNoiDenSua = makeCombo(arrGaStr); cbNoiDenSua.setEditable(true);

        setupAutoSelectAll(cbNoiDiSua); setupAutoSelectAll(cbNoiDenSua);
        applySmartFilterGa(cbNoiDiSua, arrGaStr);
        applySmartFilterGa(cbNoiDenSua, arrGaStr);

        // Set dữ liệu ban đầu
        String displayGaDiOld = tuyenHienTai.getGaDi().getTenGa() + " (" + tuyenHienTai.getGaDi().getTinhThanh() + ")";
        String displayGaDenOld = tuyenHienTai.getGaDen().getTenGa() + " (" + tuyenHienTai.getGaDen().getTinhThanh() + ")";
        cbNoiDiSua.setSelectedItem(displayGaDiOld);
        cbNoiDenSua.setSelectedItem(displayGaDenOld);

        Runnable updateTenTuyenSua = () -> {
            String selDi = (String) cbNoiDiSua.getSelectedItem();
            String selDen = (String) cbNoiDenSua.getSelectedItem();
            if (selDi != null && selDen != null && mapGa.containsKey(selDi) && mapGa.containsKey(selDen)) {
                txtTenSua.setText(mapGa.get(selDi).getTenGa() + " - " + mapGa.get(selDen).getTenGa());
            }
        };
        cbNoiDiSua.addActionListener(e -> updateTenTuyenSua.run());
        cbNoiDenSua.addActionListener(e -> updateTenTuyenSua.run());

        String[] allHours = new String[100]; for (int i = 0; i <= 99; i++) allHours[i] = String.format("%02d", i);
        String[] allMinutes = new String[60]; for (int i = 0; i <= 59; i++) allMinutes[i] = String.format("%02d", i);

        JComboBox<String> cbGioSua = makeCombo(allHours); cbGioSua.setEditable(true);
        cbGioSua.setPreferredSize(new Dimension(75, 36));

        JComboBox<String> cbPhutSua = makeCombo(allMinutes); cbPhutSua.setEditable(true);
        cbPhutSua.setPreferredSize(new Dimension(75, 36));

        int tongPhut = tuyenHienTai.getThoiGianChay();
        cbGioSua.setSelectedItem(String.format("%02d", tongPhut / 60));
        cbPhutSua.setSelectedItem(String.format("%02d", tongPhut % 60));

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        timePanel.setOpaque(false);
        timePanel.add(cbGioSua); timePanel.add(new JLabel("Giờ"));
        timePanel.add(Box.createHorizontalStrut(10));
        timePanel.add(cbPhutSua); timePanel.add(new JLabel("Phút"));

        int r = 0;
        addRow(form, gc, r++, "Mã Tuyến:", txtMaSua);
        addRow(form, gc, r++, "Nơi Đi:", cbNoiDiSua);
        addRow(form, gc, r++, "Nơi Đến:", cbNoiDenSua);
        addRow(form, gc, r++, "Tên tuyến:", txtTenSua);
        addRow(form, gc, r++, "Thời gian:", timePanel);

        JButton btnHuy = makeBtn("Hủy", BtnStyle.SECONDARY);
        JButton btnSua = makeBtn("Cập nhật", BtnStyle.PRIMARY);

        btnHuy.addActionListener(e -> dialog.dispose());
        btnSua.addActionListener(e -> {
            String selDi = cbNoiDiSua.getSelectedItem() != null ? cbNoiDiSua.getSelectedItem().toString() : "";
            String selDen = cbNoiDenSua.getSelectedItem() != null ? cbNoiDenSua.getSelectedItem().toString() : "";

            if (!mapGa.containsKey(selDi) || !mapGa.containsKey(selDen)) {
                warn("Vui lòng chọn Nơi Đi và Nơi Đến hợp lệ từ danh sách gợi ý!"); return;
            }
            if (selDi.equalsIgnoreCase(selDen)) { warn("Nơi đi và Nơi đến không được trùng nhau!"); return; }

            int phutSua = (Integer.parseInt(cbGioSua.getSelectedItem().toString()) * 60)
                    + Integer.parseInt(cbPhutSua.getSelectedItem().toString());

            if (phutSua <= 0 || phutSua >= (100 * 60)) { warn("Thời gian không hợp lệ!"); return; }

            Ga gaDiMoi = mapGa.get(selDi);
            Ga gaDeNMoi = mapGa.get(selDen);

            for (int i = 0; i < dsTuyen.getAllTuyen().size(); i++) {
                Tuyen existT = dsTuyen.getAllTuyen().get(i);
                if (!existT.getMaTuyen().equals(txtMaSua.getText()) && existT.getGaDi().getMaGa().equals(gaDiMoi.getMaGa()) && existT.getGaDen().getMaGa().equals(gaDeNMoi.getMaGa())) {
                    warn("Tuyến đường từ " + gaDiMoi.getTenGa() + " đến " + gaDeNMoi.getTenGa() + " đã tồn tại!"); return;
                }
            }

            Tuyen tuyenCapNhat = new Tuyen(txtMaSua.getText(), txtTenSua.getText(), phutSua, gaDiMoi, gaDeNMoi);

            if (dsTuyen.updateTuyen(tuyenCapNhat)) {
                dialog.dispose();
                updateTableData();
                JOptionPane.showMessageDialog(this, "Cập nhật tuyến thành công!");
            } else {
                warn("Cập nhật thất bại!");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setOpaque(false); btnPanel.add(btnHuy); btnPanel.add(btnSua);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    // =========================================================================
    // HELPER UI CHUNG
    // =========================================================================
    private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
        JPanel p = makeCard(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(15, 20, 15, 20)));
        JLabel lblT = new JLabel(title);
        lblT.setForeground(TEXT_MID);
        lblT.setFont(F_LABEL);
        lblValue.setForeground(accent);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        p.add(lblT, BorderLayout.NORTH); p.add(lblValue, BorderLayout.CENTER);
        return p;
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        t.setRowHeight(38); t.setFont(F_CELL);
        t.setBackground(BG_CARD); t.setSelectionBackground(new Color(0xDDEEFF));
        t.setSelectionForeground(TEXT_DARK); t.setGridColor(BORDER);
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false); t.setFocusable(false);
        t.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(LEFT); }
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                l.setOpaque(true); l.setBackground(ACCENT); l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI",Font.BOLD,13)); l.setBorder(BorderFactory.createEmptyBorder(0,12,0,6)); return l;
            }
        });
        h.setPreferredSize(new Dimension(0, 40)); h.setReorderingAllowed(false);

        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
        for (int i = 0; i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(r);
        return t;
    }

    private JPanel makeCard(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(BG_CARD); p.setBorder(new ShadowBorder()); return p;
    }

    private JTextField makeField(String hint) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(TEXT_LIGHT);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    g2.drawString(hint, getInsets().left + 4, getHeight() / 2 + 5); g2.dispose();
                }
            }
        };
        tf.setFont(F_CELL); tf.setForeground(TEXT_DARK); tf.setBackground(new Color(0xF8FAFD));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ACCENT_FOC, 2, true), BorderFactory.createEmptyBorder(5, 9, 5, 9))); }
            public void focusLost(java.awt.event.FocusEvent e) { tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(6, 10, 6, 10))); }
        });
        return tf;
    }

    private JTextField roField(String v) {
        JTextField tf = makeField(""); tf.setText(v); tf.setEditable(false); tf.setBackground(new Color(0xEEF2F8)); return tf;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items); cb.setFont(F_CELL); cb.setBackground(new Color(0xF8FAFD)); cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        cb.setPreferredSize(new Dimension(130, 36));
        return cb;
    }

    private GridBagConstraints defaultGC() {
        GridBagConstraints gc = new GridBagConstraints(); gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST; gc.fill = GridBagConstraints.HORIZONTAL; return gc;
    }

    private void addRow(JPanel form, GridBagConstraints gc, int row, String lbl, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; JLabel l = new JLabel(lbl); l.setFont(F_LABEL); l.setForeground(TEXT_MID); form.add(l, gc);
        gc.gridx = 1; gc.weightx = 1; field.setPreferredSize(new Dimension(260, 36)); form.add(field, gc);
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                switch(style){
                    case PRIMARY -> { g2.setColor(getModel().isRollover()?ACCENT_HVR:ACCENT); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                    case DANGER  -> { g2.setColor(getModel().isRollover()?BTN_RED_HVR:BTN_RED); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                    default      -> { g2.setColor(getModel().isRollover()?new Color(0xE0ECFF):BTN2_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.setColor(BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); }
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_LABEL); b.setForeground(style == BtnStyle.SECONDARY ? BTN2_FG : Color.WHITE);
        b.setPreferredSize(new Dimension(style == BtnStyle.DANGER ? 80 : 130, 36));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    private JDialog makeDialog(String title) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog d = (owner instanceof Frame) ? new JDialog((Frame)owner, title, true) : new JDialog((Dialog)owner, title, true);
        d.setLayout(new BorderLayout()); d.getContentPane().setBackground(BG_PAGE);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); return d;
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI(){
            @Override protected void configureScrollBarColors(){thumbColor=new Color(0xC0D4EE);trackColor=BG_PAGE;}
            @Override protected JButton createDecreaseButton(int o){return zBtn();}
            @Override protected JButton createIncreaseButton(int o){return zBtn();}
            private JButton zBtn(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });
        sb.putClientProperty(FlatClientProperties.SCROLL_BAR_SHOW_BUTTONS, false);
    }

    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE); }

    // Dùng Contains tìm kiếm theo cả Tên Ga và Tên Tỉnh
    private void applySmartFilterGa(JComboBox<String> cb, String[] fullList) {
        JTextField editor = (JTextField) cb.getEditor().getEditorComponent();

        // ===== FILTER KHI GÕ =====
        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                int code = e.getKeyCode();

                // ===== ENTER: CHỌN ITEM =====
                if (code == KeyEvent.VK_ENTER) {
                    String textTyped = editor.getText().trim();

                    // Ưu tiên chọn item khớp hoàn toàn
                    for (int i = 0; i < cb.getItemCount(); i++) {
                        String item = cb.getItemAt(i);
                        if (item.equalsIgnoreCase(textTyped)) {
                            cb.setSelectedItem(item);
                            cb.hidePopup();
                            return;
                        }
                    }

                    // Nếu không khớp → chọn item đầu tiên
                    if (cb.getItemCount() > 0) {
                        cb.setSelectedIndex(0);
                        cb.hidePopup();
                    }
                    return;
                }

                // Bỏ qua phím điều hướng
                if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN ||
                        code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT) {
                    return;
                }

                String textTyped = editor.getText();
                DefaultComboBoxModel<String> filteredModel = new DefaultComboBoxModel<>();
                String lowerTyped = textTyped.toLowerCase();

                for (String item : fullList) {
                    if (item.toLowerCase().contains(lowerTyped)) {
                        filteredModel.addElement(item);
                    }
                }

                cb.setModel(filteredModel);
                editor.setText(textTyped);

                if (filteredModel.getSize() > 0) {
                    cb.showPopup();
                } else {
                    cb.hidePopup();
                }
            }
        });

        // ===== FIX CHUẨN SWING: ENTER TRONG ACTION =====
        cb.addActionListener(e -> {
            if ("comboBoxEdited".equals(e.getActionCommand())) {
                String text = editor.getText().trim();

                for (int i = 0; i < cb.getItemCount(); i++) {
                    String item = cb.getItemAt(i);
                    if (item.equalsIgnoreCase(text)) {
                        cb.setSelectedItem(item);
                        return;
                    }
                }

                // fallback nếu không match
                if (cb.getItemCount() > 0) {
                    cb.setSelectedIndex(0);
                }
            }
        });
    }

    private void applySmartFilterTime(JComboBox<String> cb, String[] fullList) {
        JTextField editor = (JTextField) cb.getEditor().getEditorComponent();
        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                int code = e.getKeyCode();
                if (code == 38 || code == 40 || code == 10 || code == 27 || code == 37 || code == 39) return;
                String textTyped = editor.getText();
                DefaultComboBoxModel<String> filteredModel = new DefaultComboBoxModel<>();
                for (String item : fullList) if (item.startsWith(textTyped)) filteredModel.addElement(item);
                cb.setModel(filteredModel); editor.setText(textTyped);
                if (filteredModel.getSize() > 0) cb.showPopup(); else cb.hidePopup();
            }
        });
    }

    private void setupAutoSelectAll(JComboBox<String> cb) {
        JTextField editor = (JTextField) cb.getEditor().getEditorComponent();
        editor.addFocusListener(new java.awt.event.FocusAdapter() { public void focusGained(java.awt.event.FocusEvent e) { SwingUtilities.invokeLater(editor::selectAll); }});
        editor.addMouseListener(new java.awt.event.MouseAdapter() { public void mousePressed(java.awt.event.MouseEvent e) { SwingUtilities.invokeLater(editor::selectAll); }});
    }

    private static class ShadowBorder extends AbstractBorder {
        private static final int S = 4;
        @Override public void paintBorder(Component c,Graphics g,int x,int y,int w,int h){
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            for(int i=S;i>0;i--){g2.setColor(new Color(100,140,200,(int)(20.0*(S-i)/S)));g2.drawRoundRect(x+i,y+i,w-2*i-1,h-2*i-1,12,12);}
            g2.setColor(new Color(0xE2EAF4));g2.drawRoundRect(x,y,w-1,h-1,12,12);
            g2.setColor(BG_CARD);g2.setClip(new RoundRectangle2D.Float(x+1,y+1,w-2,h-2,12,12));g2.fillRect(x+1,y+1,w-2,h-2);g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c){return new Insets(S,S,S,S);}
        @Override public Insets getBorderInsets(Component c,Insets ins){ins.set(S,S,S,S);return ins;}
    }
}