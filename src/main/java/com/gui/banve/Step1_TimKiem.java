package com.gui.banve;

import com.dao.DAO_Ga;
import com.entities.Ga;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Step1_TimKiem extends JPanel {
    private TAB_BanVe mainTab;
    private JComboBox<String> cbNoiDi, cbNoiDen;
    private JRadioButton rdoMotChieu, rdoKhuHoi;
    private UIHelper.DatePickerField dpNgayDi, dpNgayVe;

    // Map này lưu trữ "Chuỗi hiển thị" -> Đối tượng Ga
    private Map<String, Ga> mapGa = new HashMap<>();
    private final DAO_Ga daoGa = new DAO_Ga();

    public Step1_TimKiem(TAB_BanVe mainTab) {
        this.mainTab = mainTab;
        initUI();
        loadDataToFormSearch();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setOpaque(false);
        add(UIHelper.createPageTitle("TÌM KIẾM CHUYẾN TÀU", "Nhập thông tin hành trình để tìm kiếm chuyến đi phù hợp"), BorderLayout.NORTH);

        JPanel pnlWrapper = new JPanel(new GridBagLayout());
        pnlWrapper.setOpaque(false);

        JPanel pnlSearch = UIHelper.makeCard(new GridBagLayout());
        pnlSearch.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(), BorderFactory.createEmptyBorder(30, 40, 30, 40)));
        GridBagConstraints gc = UIHelper.defaultGC();

        cbNoiDi = UIHelper.makeCombo(new String[]{});
        cbNoiDen = UIHelper.makeCombo(new String[]{});

        cbNoiDi.setEditable(true);
        cbNoiDen.setEditable(true);

        rdoMotChieu = new JRadioButton("Một Chiều", true);
        rdoKhuHoi = new JRadioButton("Khứ Hồi");
        rdoMotChieu.setFont(UIHelper.F_LABEL); rdoKhuHoi.setFont(UIHelper.F_LABEL);
        rdoMotChieu.setOpaque(false); rdoKhuHoi.setOpaque(false);
        ButtonGroup bg = new ButtonGroup(); bg.add(rdoMotChieu); bg.add(rdoKhuHoi);
        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        pnlRadio.setOpaque(false); pnlRadio.add(rdoMotChieu); pnlRadio.add(rdoKhuHoi);

        dpNgayDi = new UIHelper.DatePickerField("");
        dpNgayDi.setDisablePastDates(true);
        dpNgayVe = new UIHelper.DatePickerField("");
        dpNgayVe.setDisablePastDates(true);
        dpNgayVe.setEnabledField(false);

        rdoMotChieu.addActionListener(e -> { dpNgayVe.setEnabledField(false); dpNgayVe.setDate(""); });
        rdoKhuHoi.addActionListener(e -> dpNgayVe.setEnabledField(true));

        JButton btnSearch = UIHelper.makeBtn("Tìm Kiếm", true);
        btnSearch.setPreferredSize(new Dimension(250, 48));

        int r = 0;
        UIHelper.addFormRow(pnlSearch, gc, r++, "Nơi Đi:", cbNoiDi, "Nơi Đến:", cbNoiDen);
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
        List<Ga> listGa = daoGa.getAllGa();
        String[] arrGaStr = new String[listGa.size()];
        mapGa.clear();

        for (int i = 0; i < listGa.size(); i++) {
            Ga g = listGa.get(i);
            String displayStr = g.getTenGa() + " (" + g.getTinhThanh() + ")";
            arrGaStr[i] = displayStr;
            mapGa.put(displayStr, g);
        }

        cbNoiDi.setModel(new DefaultComboBoxModel<>(arrGaStr));
        cbNoiDen.setModel(new DefaultComboBoxModel<>(arrGaStr));

        setupAutoSelectAll(cbNoiDi);
        setupAutoSelectAll(cbNoiDen);

        // Đã cập nhật bộ lọc thông minh giống y hệt TAB_Tuyen
        applySmartFilterGa(cbNoiDi, arrGaStr);
        applySmartFilterGa(cbNoiDen, arrGaStr);
    }

    private String formatSqlDate(String vnDate) {
        try {
            Date d = new SimpleDateFormat(UIHelper.DATE_FMT).parse(vnDate);
            return new SimpleDateFormat("yyyy-MM-dd").format(d);
        } catch (Exception e) { return ""; }
    }

    // Kiểm duyệt tính hợp lệ
    private void performSearch() {
        String selDi = cbNoiDi.getSelectedItem() != null ? cbNoiDi.getSelectedItem().toString() : "";
        String selDen = cbNoiDen.getSelectedItem() != null ? cbNoiDen.getSelectedItem().toString() : "";

        if (!mapGa.containsKey(selDi) || !mapGa.containsKey(selDen)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Nơi Đi và Nơi Đến hợp lệ từ danh sách gợi ý!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(selDi.equalsIgnoreCase(selDen)) {
            JOptionPane.showMessageDialog(this, "Nơi đi và Nơi đến không được trùng nhau!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
        }

        String strNgayDi = dpNgayDi.getDate();
        String strNgayVe = dpNgayVe.getDate();

        if (strNgayDi == null || strNgayDi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày đi!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
        }



        boolean isRoundTrip = rdoKhuHoi.isSelected();
        mainTab.setRoundTrip(isRoundTrip);

        Ga gaDi = mapGa.get(selDi);
        Ga gaDen = mapGa.get(selDen);

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

        boolean success = mainTab.getStep2().loadTrainData(gaDi.getMaGa(), gaDen.getMaGa(), sqlNgayDi, isRoundTrip, strNgayVe, gaDi.getTenGa(), gaDen.getTenGa());

        if (success) {
            mainTab.getSelectedSeatsData().clear();
            mainTab.nextStep();
        }
    }

    // =========================================================================
    // XỬ LÝ SỰ KIỆN ENTER VÀ FILTER THÔNG MINH (ĐỒNG BỘ TỪ TAB_TUYEN)
    // =========================================================================
    private void applySmartFilterGa(JComboBox<String> cb, String[] fullList) {
        JTextField editor = (JTextField) cb.getEditor().getEditorComponent();

        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_ENTER) {
                    String textTyped = editor.getText().trim();

                    // 1. Ưu tiên chọn item khớp hoàn toàn
                    for (int i = 0; i < cb.getItemCount(); i++) {
                        String item = cb.getItemAt(i);
                        if (item.equalsIgnoreCase(textTyped)) {
                            cb.setSelectedItem(item);
                            cb.hidePopup();
                            editor.transferFocus(); // Sang ô tiếp theo
                            return;
                        }
                    }

                    // 2. Nếu không khớp hoàn toàn -> chọn item đầu tiên đang hiển thị
                    if (cb.getItemCount() > 0) {
                        cb.setSelectedIndex(0);
                        cb.hidePopup();
                        editor.transferFocus(); // Sang ô tiếp theo
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

        // Bắt lỗi người dùng click chọn bằng chuột nhưng model chưa cập nhật
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

                if (cb.getItemCount() > 0) {
                    cb.setSelectedIndex(0);
                }
            }
        });
    }

    private void setupAutoSelectAll(JComboBox<String> cb) {
        JTextField editor = (JTextField) cb.getEditor().getEditorComponent();
        editor.addFocusListener(new java.awt.event.FocusAdapter() { public void focusGained(java.awt.event.FocusEvent e) { SwingUtilities.invokeLater(editor::selectAll); }});
        editor.addMouseListener(new java.awt.event.MouseAdapter() { public void mousePressed(java.awt.event.MouseEvent e) { SwingUtilities.invokeLater(editor::selectAll); }});
    }
}