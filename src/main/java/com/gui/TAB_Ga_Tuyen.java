package com.gui;

import com.dao.DAO_Ga;
import com.dao.DAO_Tuyen;
import com.entities.Ga;
import com.entities.Tuyen;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class TAB_Ga_Tuyen extends JPanel {

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
    private static final Color BORDER       = new Color(0xE2EAF4);
    private static final Color ROW_ALT      = new Color(0xF7FAFF);
    private static final Color BTN2_BG      = new Color(0xF0F4FA);
    private static final Color BTN2_FG      = new Color(0x3A5A8C);
    private static final Color BTN_RED      = new Color(0xC0392B);
    private static final Color BTN_RED_HVR  = new Color(0xE74C3C);

    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  18);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER }

    // =========================================================================
    // THÀNH PHẦN GIAO DIỆN
    // =========================================================================
    private DefaultTableModel dataModel_Ga;
    private JTable tableGa;
    private JTextField txtSearchGa;

    private DefaultTableModel dataModel_Tuyen;
    private JTable tableTuyen;
    private JTextField txtSearchTuyen; // Thanh tìm kiếm Tuyến mới

    private final DAO_Ga dsGa = new DAO_Ga();
    private final DAO_Tuyen dsTuyen  = new DAO_Tuyen();

    public TAB_Ga_Tuyen() {
        setLayout(new BorderLayout());
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel body = new JPanel(new GridBagLayout()) {
            @Override public Dimension getPreferredSize() { return super.getPreferredSize(); }
            @Override public Dimension getMinimumSize() { return new Dimension(0,0); }
        };
        body.setOpaque(false);

        GridBagConstraints gcLeft = new GridBagConstraints();
        gcLeft.gridx = 0; gcLeft.gridy = 0;
        gcLeft.weightx = 0.55;
        gcLeft.weighty = 1.0;
        gcLeft.fill = GridBagConstraints.BOTH;
        gcLeft.insets = new Insets(0, 0, 0, 8);
        body.add(createGaPanel(), gcLeft);

        GridBagConstraints gcRight = new GridBagConstraints();
        gcRight.gridx = 1; gcRight.gridy = 0;
        gcRight.weightx = 0.45;
        gcRight.weighty = 1.0;
        gcRight.fill = GridBagConstraints.BOTH;
        gcRight.insets = new Insets(0, 8, 0, 0);
        body.add(createTuyenPanel(), gcRight);

        add(body, BorderLayout.CENTER);

        updateTableData_Ga();
        updateTableData_Tuyen();
    }

    // =========================================================================
    // PANEL TRÁI: QUẢN LÝ GA
    // =========================================================================
    private JPanel createGaPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(BG_PAGE);

        JLabel title = new JLabel("QUẢN LÝ GA");
        title.setFont(F_TITLE); title.setForeground(TEXT_DARK);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false); top.add(title); top.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel searchCard = makeCard(new BorderLayout(9, 0));
        searchCard.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        txtSearchGa = makeField("");
        txtSearchGa.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên hoặc mã ga để tìm kiếm...");
        txtSearchGa.putClientProperty(FlatClientProperties.STYLE, "arc: 15");

        JButton btnXoaGa = makeBtn("Xóa", BtnStyle.DANGER);
        JButton btnThemGa = makeBtn("Thêm", BtnStyle.PRIMARY);

        btnXoaGa.setPreferredSize(new Dimension(80, 36));
        btnThemGa.setPreferredSize(new Dimension(80, 36));

        JPanel btnGroupGa = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnGroupGa.setOpaque(false);
        btnGroupGa.add(btnThemGa);
        btnGroupGa.add(btnXoaGa);

        searchCard.add(txtSearchGa, BorderLayout.CENTER);
        searchCard.add(btnGroupGa, BorderLayout.EAST);

        JPanel tableCard = makeCard(new BorderLayout());
        String[] cols_Ga = {"Mã Ga", "Tên Ga", "Địa chỉ"};
        dataModel_Ga = new DefaultTableModel(cols_Ga, 0);
        tableGa = buildTable(dataModel_Ga);

        tableGa.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableGa.getColumnModel().getColumn(1).setPreferredWidth(150);
        tableGa.getColumnModel().getColumn(2).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(tableGa);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CARD);
        styleScrollBar(scrollPane.getVerticalScrollBar());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // LIVE SEARCH GA
        txtSearchGa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            private void thucHienTimKiem() {
                String tuKhoa = txtSearchGa.getText().trim();
                List<Ga> listKetQua = dsGa.timKiemGa(tuKhoa);
                dataModel_Ga.setRowCount(0);
                for (Ga ga : listKetQua) {
                    dataModel_Ga.addRow(new Object[]{ga.getMaGa(), ga.getTenGa(), ga.getDiaChi()});
                }
            }
        });

        btnXoaGa.addActionListener(e -> {
            int row = tableGa.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn Ga cần xóa!"); return; }
            String maGa = tableGa.getValueAt(row, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa Ga: " + maGa + " khỏi danh sách hoạt động?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (dsGa.deleteGa(maGa)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa Ga thành công!");
                    updateTableData_Ga();
                } else {
                    warn("Xóa Ga thất bại! Vui lòng thử lại.");
                }
            }
        });

        btnThemGa.addActionListener(e -> hienThiDialogThemGa());

        tableGa.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = tableGa.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    tableGa.setRowSelectionInterval(row, row);
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                        hienThiDialogGa(row);
                    }
                }
            }
        });

        pnl.add(top, BorderLayout.NORTH);
        JPanel centerPnl = new JPanel(new BorderLayout(0, 12));
        centerPnl.setOpaque(false);
        centerPnl.add(searchCard, BorderLayout.NORTH);
        centerPnl.add(tableCard, BorderLayout.CENTER);
        pnl.add(centerPnl, BorderLayout.CENTER);

        return pnl;
    }

    // =========================================================================
    // PANEL PHẢI: QUẢN LÝ TUYẾN (ĐÃ ĐƯỢC THIẾT KẾ LẠI)
    // =========================================================================
    private JPanel createTuyenPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(BG_PAGE);

        JLabel title = new JLabel("QUẢN LÝ TUYẾN ĐƯỜNG");
        title.setFont(F_TITLE); title.setForeground(TEXT_DARK);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false); top.add(title); top.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // Khung tìm kiếm Tuyến tương tự bên Ga
        JPanel searchCard = makeCard(new BorderLayout(9, 0));
        searchCard.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        txtSearchTuyen = makeField("");
        txtSearchTuyen.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên hoặc mã tuyến để tìm kiếm...");
        txtSearchTuyen.putClientProperty(FlatClientProperties.STYLE, "arc: 15");

        JButton btnXoaTuyen = makeBtn("Xóa", BtnStyle.DANGER);
        JButton btnThemTuyen = makeBtn("Thêm", BtnStyle.PRIMARY);

        btnXoaTuyen.setPreferredSize(new Dimension(80, 36));
        btnThemTuyen.setPreferredSize(new Dimension(80, 36));

        JPanel btnGroupTuyen = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnGroupTuyen.setOpaque(false);
        btnGroupTuyen.add(btnThemTuyen);
        btnGroupTuyen.add(btnXoaTuyen);

        searchCard.add(txtSearchTuyen, BorderLayout.CENTER);
        searchCard.add(btnGroupTuyen, BorderLayout.EAST);

        // Bảng dữ liệu Tuyến
        JPanel tableCard = makeCard(new BorderLayout());
        String[] cols = {"Mã Tuyến", "Tên Tuyến", "Thời gian", "Ga Đi", "Ga Đến"};
        dataModel_Tuyen = new DefaultTableModel(cols, 0);
        tableTuyen = buildTable(dataModel_Tuyen);

        tableTuyen.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableTuyen.getColumnModel().getColumn(1).setPreferredWidth(180);
        tableTuyen.getColumnModel().getColumn(2).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(tableTuyen);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CARD);
        styleScrollBar(scrollPane.getVerticalScrollBar());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // SỰ KIỆN: LIVE SEARCH TUYẾN
        txtSearchTuyen.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            private void thucHienTimKiem() {
                String tuKhoa = txtSearchTuyen.getText().trim();
                List<Tuyen> listKetQua = dsTuyen.timKiemTuyen(tuKhoa);
                dataModel_Tuyen.setRowCount(0);
                for (Tuyen tuyen : listKetQua) {
                    int tongPhut = tuyen.getThoiGianChay();
                    String thoiGianHienThi = (tongPhut / 60) + "h " + (tongPhut % 60) + "m";
                    dataModel_Tuyen.addRow(new Object[]{
                            tuyen.getMaTuyen(), tuyen.getTenTuyen(), thoiGianHienThi,
                            tuyen.getGaDi() != null ? tuyen.getGaDi().getMaGa() : "",
                            tuyen.getGaDen() != null ? tuyen.getGaDen().getMaGa() : ""
                    });
                }
            }
        });

        // Nút Thêm
        btnThemTuyen.addActionListener(e -> hienThiDialogThemTuyen());

        // Nút Xóa
        btnXoaTuyen.addActionListener(e -> {
            int row = tableTuyen.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn Tuyến cần xóa!"); return; }
            String maTuyen = tableTuyen.getValueAt(row, 0).toString();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa Tuyến: " + maTuyen + " khỏi danh sách hoạt động?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (dsTuyen.deleteTuyen(maTuyen)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa Tuyến thành công!");
                    updateTableData_Tuyen();
                } else {
                    warn("Xóa Tuyến thất bại! Vui lòng thử lại.");
                }
            }
        });

        // Double Click Bảng
        tableTuyen.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = tableTuyen.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    tableTuyen.setRowSelectionInterval(row, row);
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                        hienThiDialogSuaTuyen(row);
                    }
                }
            }
        });

        pnl.add(top, BorderLayout.NORTH);
        JPanel centerPnl = new JPanel(new BorderLayout(0, 12));
        centerPnl.setOpaque(false);
        centerPnl.add(searchCard, BorderLayout.NORTH);
        centerPnl.add(tableCard, BorderLayout.CENTER);
        pnl.add(centerPnl, BorderLayout.CENTER);
        return pnl;
    }

    // =========================================================================
    // HÀM XỬ LÝ DỮ LIỆU CƠ BẢN
    // =========================================================================
    private void updateTableData_Ga() {
        List<Ga> list = dsGa.getAllGa();
        dataModel_Ga.setRowCount(0);
        for (Ga ga : list) {
            dataModel_Ga.addRow(new Object[]{ga.getMaGa(), ga.getTenGa(), ga.getDiaChi()});
        }
    }

    private void updateTableData_Tuyen() {
        List<Tuyen> list = dsTuyen.getAllTuyen();
        dataModel_Tuyen.setRowCount(0);
        for (Tuyen tuyen : list) {
            int tongPhut = tuyen.getThoiGianChay();
            String thoiGianHienThi = (tongPhut / 60) + "h " + (tongPhut % 60) + "m";
            dataModel_Tuyen.addRow(new Object[]{
                    tuyen.getMaTuyen(), tuyen.getTenTuyen(), thoiGianHienThi,
                    tuyen.getGaDi() != null ? tuyen.getGaDi().getMaGa() : "",
                    tuyen.getGaDen() != null ? tuyen.getGaDen().getMaGa() : ""
            });
        }
    }

    // =========================================================================
    // DIALOG THÊM / SỬA
    // =========================================================================
    private void hienThiDialogThemGa() {
        JDialog dialog = makeDialog("Thêm Mới Ga");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); form.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        GridBagConstraints gc = defaultGC();

        JTextField txtMa = roField(dsGa.phatSinhMaGa());
        JTextField txtTen = makeField("");
        JTextField txtDiaChi = makeField("");

        int r = 0;
        addRow(form, gc, r++, "Mã Ga:", txtMa);
        addRow(form, gc, r++, "Tên Ga:", txtTen);
        addRow(form, gc, r++, "Địa chỉ:", txtDiaChi);

        JButton btnHuy = makeBtn("Hủy", BtnStyle.SECONDARY);
        JButton btnThem = makeBtn("Thêm", BtnStyle.PRIMARY);

        btnHuy.addActionListener(e -> dialog.dispose());
        btnThem.addActionListener(e -> {
            String tenMoi = txtTen.getText().trim();
            if (tenMoi.isEmpty()) { warn("Tên ga không được để trống!"); return; }

            for (Ga existingGa : dsGa.getAllGa()) {
                if (existingGa.getTenGa().equalsIgnoreCase(tenMoi)) {
                    warn("Trùng ga! Tên ga này đã tồn tại trong CSDL.");
                    return;
                }
            }

            Ga gaMoi = new Ga(txtMa.getText(), tenMoi, txtDiaChi.getText().trim());
            if (dsGa.addGa(gaMoi)) {
                JOptionPane.showMessageDialog(dialog, "Thêm Ga thành công!");
                updateTableData_Ga();
                dialog.dispose();
            } else {
                warn("Thêm Ga thất bại!");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false); btnPanel.add(btnHuy); btnPanel.add(btnThem);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    private void hienThiDialogGa(int row) {
        String maGa = tableGa.getValueAt(row, 0).toString();
        Ga gaHienTai = dsGa.getGaByMa(maGa);
        if (gaHienTai == null) { warn("Không tìm thấy dữ liệu ga này trong CSDL!"); return; }

        JDialog dialog = makeDialog("Cập nhật Thông tin Ga");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); form.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        GridBagConstraints gc = defaultGC();

        JTextField txtMa = roField(gaHienTai.getMaGa());
        JTextField txtTen = makeField(""); txtTen.setText(gaHienTai.getTenGa());
        JTextField txtDiaChi = makeField(""); txtDiaChi.setText(gaHienTai.getDiaChi());

        int r = 0;
        addRow(form, gc, r++, "Mã Ga:", txtMa);
        addRow(form, gc, r++, "Tên Ga:", txtTen);
        addRow(form, gc, r++, "Địa chỉ:", txtDiaChi);

        JButton btnHuy = makeBtn("Hủy", BtnStyle.SECONDARY);
        JButton btnSua = makeBtn("Cập nhật", BtnStyle.PRIMARY);

        btnHuy.addActionListener(e -> dialog.dispose());
        btnSua.addActionListener(e -> {
            String tenSua = txtTen.getText().trim();
            if (tenSua.isEmpty()) { warn("Tên ga không được để trống!"); return; }

            for (Ga existingGa : dsGa.getAllGa()) {
                if (!existingGa.getMaGa().equals(txtMa.getText()) && existingGa.getTenGa().equalsIgnoreCase(tenSua)) {
                    warn("Trùng ga! Tên ga này đã bị trùng lặp với một Ga khác.");
                    return;
                }
            }

            Ga gaCapNhat = new Ga(txtMa.getText(), tenSua, txtDiaChi.getText().trim());
            if (dsGa.updateGa(gaCapNhat)) {
                JOptionPane.showMessageDialog(dialog, "Cập nhật Ga thành công!");
                updateTableData_Ga();
                dialog.dispose();
            } else {
                warn("Cập nhật Ga thất bại!");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false); btnPanel.add(btnHuy); btnPanel.add(btnSua);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    // Giao diện POPUP THÊM TUYẾN MỚI
    private void hienThiDialogThemTuyen() {
        JDialog dialog = makeDialog("Thêm Mới Tuyến Đường");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); form.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        GridBagConstraints gc = defaultGC();

        JTextField txtMa = roField(dsTuyen.phatSinhMaTuyen());
        JTextField txtTen = roField("");

        JComboBox<String> cbGaDi = makeCombo(new String[]{});
        JComboBox<String> cbGaDen = makeCombo(new String[]{});
        for (Ga ga : dsGa.getAllGa()) {
            cbGaDi.addItem(ga.getMaGa());
            cbGaDen.addItem(ga.getMaGa());
        }

        Runnable updateTenTuyen = () -> {
            if(cbGaDi.getSelectedItem() != null && cbGaDen.getSelectedItem() != null) {
                Ga gaDi = dsGa.getGaByMa(cbGaDi.getSelectedItem().toString());
                Ga gaDen = dsGa.getGaByMa(cbGaDen.getSelectedItem().toString());
                txtTen.setText(gaDi.getTenGa() + " - " + gaDen.getTenGa());
            }
        };
        cbGaDi.addActionListener(e -> updateTenTuyen.run());
        cbGaDen.addActionListener(e -> updateTenTuyen.run());
        if (cbGaDi.getItemCount() > 0) updateTenTuyen.run();

        String[] allHours = new String[100]; for (int i = 0; i <= 99; i++) allHours[i] = String.format("%02d", i);
        String[] allMinutes = new String[60]; for (int i = 0; i <= 59; i++) allMinutes[i] = String.format("%02d", i);

        JComboBox<String> cbGio = makeCombo(allHours); cbGio.setEditable(true);
        JComboBox<String> cbPhut = makeCombo(allMinutes); cbPhut.setEditable(true);
        setupAutoSelectAll(cbGio); setupAutoSelectAll(cbPhut);
        applySmartFilter(cbGio, allHours); applySmartFilter(cbPhut, allMinutes);
        cbGio.setSelectedItem("00"); cbPhut.setSelectedItem("00");

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timePanel.setOpaque(false);
        timePanel.add(cbGio); timePanel.add(new JLabel("Giờ"));
        timePanel.add(Box.createHorizontalStrut(20));
        timePanel.add(cbPhut); timePanel.add(new JLabel("Phút"));

        int r = 0;
        addRow(form, gc, r++, "Mã Tuyến:", txtMa);
        addRow(form, gc, r++, "Ga Đi:", cbGaDi);
        addRow(form, gc, r++, "Ga Đến:", cbGaDen);
        addRow(form, gc, r++, "Tên tuyến:", txtTen);
        addRow(form, gc, r++, "Thời gian:", timePanel);

        JButton btnHuy = makeBtn("Hủy", BtnStyle.SECONDARY);
        JButton btnThem = makeBtn("Thêm Tuyến", BtnStyle.PRIMARY);

        btnHuy.addActionListener(e -> dialog.dispose());
        btnThem.addActionListener(e -> {
            String maDi = cbGaDi.getSelectedItem() != null ? cbGaDi.getSelectedItem().toString() : "";
            String maDen = cbGaDen.getSelectedItem() != null ? cbGaDen.getSelectedItem().toString() : "";

            if (maDi.isEmpty() || maDen.isEmpty()) { warn("Vui lòng chọn Ga Đi và Ga Đến!"); return; }
            if (maDi.equalsIgnoreCase(maDen)) { warn("Ga đi và Ga đến không được trùng nhau!"); return; }

            int thoiGian = 0;
            try {
                int gio = Integer.parseInt(cbGio.getSelectedItem().toString().trim());
                int phut = Integer.parseInt(cbPhut.getSelectedItem().toString().trim());
                thoiGian = (gio * 60) + phut;
                if (thoiGian <= 0 || thoiGian >= (100 * 60)) {
                    warn("Thời gian không hợp lệ! (Phải từ 1 phút đến 99h 59m)"); return;
                }
            } catch (Exception ex) { warn("Thời gian chạy không hợp lệ!"); return; }

            for (int i = 0; i < dataModel_Tuyen.getRowCount(); i++) {
                String existGaDi = dataModel_Tuyen.getValueAt(i, 3).toString();
                String existGaDen = dataModel_Tuyen.getValueAt(i, 4).toString();
                if (existGaDi.equalsIgnoreCase(maDi) && existGaDen.equalsIgnoreCase(maDen)) {
                    warn("Tuyến đường từ " + maDi + " đến " + maDen + " đã tồn tại!");
                    return;
                }
            }

            Ga gaDi = dsGa.getGaByMa(maDi);
            Ga gaDen = dsGa.getGaByMa(maDen);
            Tuyen tuyenMoi = new Tuyen(txtMa.getText(), txtTen.getText(), thoiGian, gaDi, gaDen);

            if (dsTuyen.addTuyen(tuyenMoi)) {
                JOptionPane.showMessageDialog(dialog, "Thêm tuyến mới thành công!");
                updateTableData_Tuyen();
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
        String maTuyen = tableTuyen.getValueAt(row, 0).toString();
        Tuyen tuyenHienTai = dsTuyen.getTuyenByMa(maTuyen);
        if (tuyenHienTai == null) { warn("Không tìm thấy dữ liệu tuyến này trong CSDL!"); return; }

        JDialog dialog = makeDialog("Cập nhật Tuyến");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); form.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        GridBagConstraints gc = defaultGC();

        JTextField txtMaSua = roField(tuyenHienTai.getMaTuyen());
        JTextField txtTenSua = roField(tuyenHienTai.getTenTuyen());

        JComboBox<String> cbGaDiSua = makeCombo(new String[]{});
        JComboBox<String> cbGaDenSua = makeCombo(new String[]{});
        for (Ga ga : dsGa.getAllGa()) {
            cbGaDiSua.addItem(ga.getMaGa());
            cbGaDenSua.addItem(ga.getMaGa());
        }
        cbGaDiSua.setSelectedItem(tuyenHienTai.getGaDi().getMaGa());
        cbGaDenSua.setSelectedItem(tuyenHienTai.getGaDen().getMaGa());

        Runnable updateTenTuyenSua = () -> {
            if(cbGaDiSua.getSelectedItem() != null && cbGaDenSua.getSelectedItem() != null) {
                Ga gaDi = dsGa.getGaByMa(cbGaDiSua.getSelectedItem().toString());
                Ga gaDen = dsGa.getGaByMa(cbGaDenSua.getSelectedItem().toString());
                txtTenSua.setText(gaDi.getTenGa() + " - " + gaDen.getTenGa());
            }
        };
        cbGaDiSua.addActionListener(e -> updateTenTuyenSua.run());
        cbGaDenSua.addActionListener(e -> updateTenTuyenSua.run());

        JComboBox<String> cbGioSua = makeCombo(new String[0]);
        JComboBox<String> cbPhutSua = makeCombo(new String[0]);
        for (int i = 0; i <= 99; i++) cbGioSua.addItem(String.format("%02d", i));
        for (int i = 0; i <= 59; i++) cbPhutSua.addItem(String.format("%02d", i));

        int tongPhut = tuyenHienTai.getThoiGianChay();
        cbGioSua.setSelectedItem(String.format("%02d", tongPhut / 60));
        cbPhutSua.setSelectedItem(String.format("%02d", tongPhut % 60));

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timePanel.setOpaque(false);
        timePanel.add(cbGioSua); timePanel.add(new JLabel("Giờ"));
        timePanel.add(Box.createHorizontalStrut(20));
        timePanel.add(cbPhutSua); timePanel.add(new JLabel("Phút"));

        int r = 0;
        addRow(form, gc, r++, "Mã Tuyến:", txtMaSua);
        addRow(form, gc, r++, "Ga Đi:", cbGaDiSua);
        addRow(form, gc, r++, "Ga Đến:", cbGaDenSua);
        addRow(form, gc, r++, "Tên tuyến:", txtTenSua);
        addRow(form, gc, r++, "Thời gian:", timePanel);

        JButton btnHuy = makeBtn("Hủy", BtnStyle.SECONDARY);
        JButton btnSua = makeBtn("Cập nhật", BtnStyle.PRIMARY);

        btnHuy.addActionListener(e -> dialog.dispose());
        btnSua.addActionListener(e -> {
            String maDiSua = cbGaDiSua.getSelectedItem().toString();
            String maDenSua = cbGaDenSua.getSelectedItem().toString();

            if (maDiSua.equalsIgnoreCase(maDenSua)) {
                warn("Ga đi và Ga đến không được trùng nhau!"); return;
            }

            int phutSua = (Integer.parseInt(cbGioSua.getSelectedItem().toString()) * 60)
                    + Integer.parseInt(cbPhutSua.getSelectedItem().toString());

            if (phutSua <= 0 || phutSua >= (100 * 60)) {
                warn("Thời gian không hợp lệ! (Phải từ 1 phút đến 99h 59m)"); return;
            }

            for (int i = 0; i < dataModel_Tuyen.getRowCount(); i++) {
                if (i == row) continue;
                String existGaDi = dataModel_Tuyen.getValueAt(i, 3).toString();
                String existGaDen = dataModel_Tuyen.getValueAt(i, 4).toString();

                if (existGaDi.equalsIgnoreCase(maDiSua) && existGaDen.equalsIgnoreCase(maDenSua)) {
                    warn("Tuyến đường từ " + maDiSua + " đến " + maDenSua + " đã tồn tại!");
                    return;
                }
            }

            Ga gaDiMoi = dsGa.getGaByMa(maDiSua);
            Ga gaDeNMoi = dsGa.getGaByMa(maDenSua);

            Tuyen tuyenCapNhat = new Tuyen(txtMaSua.getText(), txtTenSua.getText(), phutSua, gaDiMoi, gaDeNMoi);

            if (dsTuyen.updateTuyen(tuyenCapNhat)) {
                dialog.dispose();
                updateTableData_Tuyen();
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
    // HELPER UI
    // =========================================================================
    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        t.setRowHeight(36); t.setFont(F_CELL);
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
        JTextField tf = new JTextField();
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
        cb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(2, 4, 2, 4))); return cb;
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

    private void applySmartFilter(JComboBox<String> cb, String[] fullList) {
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