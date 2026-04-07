package com.gui.banve;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Step1_TimKiem extends JPanel {
    private TAB_BanVe mainTab;
    private JComboBox<String> cbGaDi, cbGaDen;
    private JRadioButton rdoMotChieu, rdoKhuHoi;
    private UIHelper.DatePickerField dpNgayDi, dpNgayVe;
    private Map<String, String> mapGa = new HashMap<>();

    public Step1_TimKiem(TAB_BanVe mainTab) {
        this.mainTab = mainTab;
        initUI();
        loadDataToFormSearch();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setOpaque(false);
        add(UIHelper.createPageTitle("TÌM KIẾM CHUYẾN TÀU", "Vui lòng nhập thông tin hành trình để tìm kiếm chuyến đi phù hợp"), BorderLayout.NORTH);

        JPanel pnlWrapper = new JPanel(new GridBagLayout());
        pnlWrapper.setOpaque(false);

        JPanel pnlSearch = UIHelper.makeCard(new GridBagLayout());
        pnlSearch.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(), BorderFactory.createEmptyBorder(30, 40, 30, 40)));
        GridBagConstraints gc = UIHelper.defaultGC();

        cbGaDi = UIHelper.makeCombo(new String[]{});
        cbGaDen = UIHelper.makeCombo(new String[]{});

        rdoMotChieu = new JRadioButton("Một Chiều", true);
        rdoKhuHoi = new JRadioButton("Khứ Hồi");
        rdoMotChieu.setFont(UIHelper.F_LABEL); rdoKhuHoi.setFont(UIHelper.F_LABEL);
        rdoMotChieu.setOpaque(false); rdoKhuHoi.setOpaque(false);
        ButtonGroup bg = new ButtonGroup(); bg.add(rdoMotChieu); bg.add(rdoKhuHoi);
        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        pnlRadio.setOpaque(false); pnlRadio.add(rdoMotChieu); pnlRadio.add(rdoKhuHoi);

        dpNgayDi = new UIHelper.DatePickerField("");
        dpNgayVe = new UIHelper.DatePickerField("");
        dpNgayVe.setEnabledField(false);

        rdoMotChieu.addActionListener(e -> { dpNgayVe.setEnabledField(false); dpNgayVe.setDate(""); });
        rdoKhuHoi.addActionListener(e -> dpNgayVe.setEnabledField(true));

        JButton btnSearch = UIHelper.makeBtn("Tìm Kiếm", true);
        btnSearch.setPreferredSize(new Dimension(250, 48));

        int r = 0;
        UIHelper.addFormRow(pnlSearch, gc, r++, "Ga Đi:", cbGaDi, "Ga Đến:", cbGaDen);
        gc.gridx=0; gc.gridy=r++; gc.gridwidth=4; pnlSearch.add(Box.createVerticalStrut(10), gc); gc.gridwidth=1;
        UIHelper.addFormRow(pnlSearch, gc, r++, "Loại vé:", pnlRadio, "", new JLabel());
        gc.gridx=0; gc.gridy=r++; gc.gridwidth=4; pnlSearch.add(Box.createVerticalStrut(10), gc); gc.gridwidth=1;
        UIHelper.addFormRow(pnlSearch, gc, r++, "Ngày Đi:", dpNgayDi, "Ngày Về:", dpNgayVe);

        gc.gridx = 0; gc.gridy = r; gc.gridwidth = 4; gc.fill = GridBagConstraints.NONE; gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(25, 0, 0, 0);
        pnlSearch.add(btnSearch, gc);

        pnlWrapper.add(pnlSearch);
        add(pnlWrapper, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> performSearch());
    }

    private void loadDataToFormSearch() {
        mapGa = mainTab.getDaoBanVe().getDanhSachGa();
        cbGaDi.removeAllItems(); cbGaDen.removeAllItems();
        for (String tenGa : mapGa.keySet()) {
            cbGaDi.addItem(tenGa);
            cbGaDen.addItem(tenGa);
        }
    }

    private String formatSqlDate(String vnDate) {
        try {
            Date d = new SimpleDateFormat(UIHelper.DATE_FMT).parse(vnDate);
            return new SimpleDateFormat("yyyy-MM-dd").format(d);
        } catch (Exception e) { return ""; }
    }

    private void performSearch() {
        if(cbGaDi.getSelectedItem() == null || cbGaDen.getSelectedItem() == null) return;
        if(cbGaDi.getSelectedItem().equals(cbGaDen.getSelectedItem())) {
            JOptionPane.showMessageDialog(this, "Ga đi và Ga đến không được trùng nhau!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
        }

        String strNgayDi = dpNgayDi.getDate();
        String strNgayVe = dpNgayVe.getDate();

        if (strNgayDi == null || strNgayDi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày đi!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
        }

        boolean isRoundTrip = rdoKhuHoi.isSelected();
        mainTab.setRoundTrip(isRoundTrip);
        String maGaDi = mapGa.get(cbGaDi.getSelectedItem().toString());
        String maGaDen = mapGa.get(cbGaDen.getSelectedItem().toString());
        String sqlNgayDi = formatSqlDate(strNgayDi);

        if (isRoundTrip) {
            if (strNgayVe == null || strNgayVe.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày về!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
            }
            String sqlNgayVe = formatSqlDate(strNgayVe);
            if (sqlNgayVe.compareTo(sqlNgayDi) < 0) {
                JOptionPane.showMessageDialog(this, "Ngày về không được trước ngày đi!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }
        }

        // Truyền lệnh load qua Step 2
        boolean success = mainTab.getStep2().loadTrainData(maGaDi, maGaDen, sqlNgayDi, isRoundTrip, strNgayVe, cbGaDi.getSelectedItem().toString(), cbGaDen.getSelectedItem().toString());

        if (success) {
            mainTab.getSelectedSeatsData().clear(); // Clear session cũ
            mainTab.nextStep();
        }
    }
}