package com.gui.banve;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Step2_ChonChoNgoi extends JPanel {
    private TAB_BanVe mainTab;
    private JPanel pnlDirectionToggle;
    private CardLayout routeCardLayout;
    private JPanel pnlRouteCards;
    private JToggleButton btnChieuDi, btnChieuVe;

    private RoutePanel pnlOutbound;
    private RoutePanel pnlReturn;

    public Step2_ChonChoNgoi(TAB_BanVe mainTab) {
        this.mainTab = mainTab;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setOpaque(false);
        add(UIHelper.createPageTitle("CHỌN CHUYẾN & GHẾ", "Lựa chọn chuyến tàu và vị trí ghế ngồi phù hợp với bạn"), BorderLayout.NORTH);

        JPanel pnl = new JPanel(new BorderLayout(0, 15));
        pnl.setOpaque(false);

        pnlDirectionToggle = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pnlDirectionToggle.setOpaque(false);

        btnChieuDi = UIHelper.createSelectionTab("CHIỀU ĐI", true);
        btnChieuVe = UIHelper.createSelectionTab("CHIỀU VỀ", false);
        btnChieuDi.setPreferredSize(new Dimension(200, 40));
        btnChieuVe.setPreferredSize(new Dimension(200, 40));

        ButtonGroup bgDir = new ButtonGroup();
        bgDir.add(btnChieuDi); bgDir.add(btnChieuVe);

        pnlDirectionToggle.add(btnChieuDi); pnlDirectionToggle.add(btnChieuVe);

        routeCardLayout = new CardLayout();
        pnlRouteCards = new JPanel(routeCardLayout);
        pnlRouteCards.setOpaque(false);

        pnlOutbound = new RoutePanel();
        pnlReturn = new RoutePanel();

        pnlRouteCards.add(pnlOutbound, "OUTBOUND");
        pnlRouteCards.add(pnlReturn, "RETURN");

        btnChieuDi.addActionListener(e -> routeCardLayout.show(pnlRouteCards, "OUTBOUND"));
        btnChieuVe.addActionListener(e -> routeCardLayout.show(pnlRouteCards, "RETURN"));

        pnl.add(pnlDirectionToggle, BorderLayout.NORTH);
        pnl.add(pnlRouteCards, BorderLayout.CENTER);

        add(pnl, BorderLayout.CENTER);
    }

    // Hàm được gọi từ Step 1
    public boolean loadTrainData(String maGaDi, String maGaDen, String sqlNgayDi, boolean isRoundTrip, String strNgayVe, String tenGaDi, String tenGaDen) {
        pnlOutbound.clearData();
        pnlReturn.clearData();

        pnlOutbound.lblTitle.setText("CHIỀU ĐI: " + tenGaDi + " → " + tenGaDen);
        boolean hasOutbound = pnlOutbound.fetchDataTrains(maGaDi, maGaDen, sqlNgayDi);

        if (!hasOutbound) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy chuyến tàu nào cho Chiều Đi vào ngày này!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        if (isRoundTrip) {
            String sqlNgayVe = "";
            try {
                Date d = new SimpleDateFormat(UIHelper.DATE_FMT).parse(strNgayVe);
                sqlNgayVe = new SimpleDateFormat("yyyy-MM-dd").format(d);
            } catch (Exception e) {}

            pnlReturn.lblTitle.setText("CHIỀU VỀ: " + tenGaDen + " → " + tenGaDi);
            boolean hasReturn = pnlReturn.fetchDataTrains(maGaDen, maGaDi, sqlNgayVe);

            if (!hasReturn) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy chuyến tàu nào cho Chiều Về vào ngày này!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        }

        pnlDirectionToggle.setVisible(isRoundTrip);
        btnChieuDi.setSelected(true);
        routeCardLayout.show(pnlRouteCards, "OUTBOUND");
        return true;
    }

    // INNER CLASS xử lý ghế
    private class RoutePanel extends JPanel {
        JLabel lblTitle;
        JPanel pnlTauList, pnlToaList, pnlGhe;
        ButtonGroup bgTau, bgToa;
        String currentMaLT = "", currentTenTau = "", currentTenToa = "";

        public RoutePanel() {
            setLayout(new BorderLayout(0, 15)); setOpaque(false);

            JPanel pnlHeader = new JPanel(new BorderLayout()); pnlHeader.setOpaque(false); pnlHeader.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            lblTitle = new JLabel(); lblTitle.setFont(UIHelper.F_H1); lblTitle.setForeground(UIHelper.ACCENT); pnlHeader.add(lblTitle, BorderLayout.WEST);

            JPanel pnlSelectionWrapper = UIHelper.makeCard(new BorderLayout(0, 15));
            pnlSelectionWrapper.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(), BorderFactory.createEmptyBorder(15, 15, 15, 15)));

            JPanel pnlTopControls = new JPanel(); pnlTopControls.setLayout(new BoxLayout(pnlTopControls, BoxLayout.Y_AXIS)); pnlTopControls.setOpaque(false);

            JPanel pnlTauWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pnlTauWrapper.setOpaque(false);
            pnlTauWrapper.add(new JLabel("<html><b style='color:#5A6A7D'>Chọn Tàu: &nbsp;</b></html>"));
            pnlTauList = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); pnlTauList.setOpaque(false);
            pnlTauWrapper.add(pnlTauList);

            JPanel pnlToaWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pnlToaWrapper.setOpaque(false); pnlToaWrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
            pnlToaWrapper.add(new JLabel("<html><b style='color:#5A6A7D'>Chọn Toa: &nbsp;</b></html>"));
            pnlToaList = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); pnlToaList.setOpaque(false);
            pnlToaWrapper.add(pnlToaList);

            pnlTopControls.add(pnlTauWrapper); pnlTopControls.add(pnlToaWrapper);

            JPanel pnlGheWrapper = new JPanel(new BorderLayout()); pnlGheWrapper.setOpaque(false);
            pnlGheWrapper.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIHelper.BORDER), "Sơ đồ ghế đang trống", 0, 0, UIHelper.F_LABEL, UIHelper.ACCENT));

            pnlGhe = new JPanel(new GridLayout(0, 14, 8, 8)); pnlGhe.setOpaque(false); pnlGhe.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            JScrollPane scrollGhe = new JScrollPane(pnlGhe); scrollGhe.setBorder(null); scrollGhe.setOpaque(false); scrollGhe.getViewport().setOpaque(false);
            pnlGheWrapper.add(scrollGhe, BorderLayout.CENTER);

            pnlSelectionWrapper.add(pnlTopControls, BorderLayout.NORTH); pnlSelectionWrapper.add(pnlGheWrapper, BorderLayout.CENTER);
            add(pnlHeader, BorderLayout.NORTH); add(pnlSelectionWrapper, BorderLayout.CENTER);
        }

        public void clearData() {
            pnlTauList.removeAll(); pnlToaList.removeAll(); pnlGhe.removeAll();
            revalidate(); repaint();
        }

        public boolean fetchDataTrains(String maGaDi, String maGaDen, String ngay) {
            clearData();
            List<Map<String, Object>> dsChuyen = mainTab.getDaoBanVe().timChuyenTau(maGaDi, maGaDen, ngay);
            if (dsChuyen.isEmpty()) return false;

            bgTau = new ButtonGroup(); boolean isFirst = true;
            for (Map<String, Object> chuyen : dsChuyen) {
                String maTau = chuyen.get("maTau").toString();
                String tenTau = chuyen.get("tenTau").toString();
                String gioLT = chuyen.get("gioKhoiHanh").toString().substring(0, 5);
                String maLT = chuyen.get("maLT").toString();

                JToggleButton btnTau = UIHelper.createSelectionTab(tenTau + " (" + gioLT + ")", isFirst);
                bgTau.add(btnTau); pnlTauList.add(btnTau);

                btnTau.addActionListener(e -> { currentMaLT = maLT; currentTenTau = tenTau; fetchDataToa(maTau); });

                if (isFirst) { isFirst = false; currentMaLT = maLT; currentTenTau = tenTau; fetchDataToa(maTau); }
            }
            revalidate(); repaint(); return true;
        }

        private void fetchDataToa(String maTau) {
            pnlToaList.removeAll(); pnlGhe.removeAll();
            List<Map<String, Object>> dsToa = mainTab.getDaoBanVe().getDanhSachToa(maTau);
            if (dsToa.isEmpty()) { revalidate(); repaint(); return; }

            bgToa = new ButtonGroup(); boolean isFirst = true;
            for (Map<String, Object> toa : dsToa) {
                String maToa = toa.get("maToa").toString();
                String tenToa = toa.get("tenToa").toString();

                JToggleButton btnToa = UIHelper.createSelectionTab(tenToa + ": " + toa.get("tenLoaiToa").toString(), isFirst);
                btnToa.setPreferredSize(new Dimension(160, 35)); bgToa.add(btnToa); pnlToaList.add(btnToa);

                btnToa.addActionListener(e -> { currentTenToa = tenToa; fetchDataGhe(maToa); });
                if (isFirst) { isFirst = false; currentTenToa = tenToa; fetchDataGhe(maToa); }
            }
            revalidate(); repaint();
        }

        private void fetchDataGhe(String maToa) {
            pnlGhe.removeAll();
            List<Map<String, Object>> dsGhe = mainTab.getDaoBanVe().getDanhSachGhe(maToa);

            for (Map<String, Object> ghe : dsGhe) {
                String maCho = ghe.get("maCho").toString();
                String tenCho = ghe.get("tenCho").toString();
                String trangThai = ghe.get("trangThai").toString();

                JToggleButton btnGhe = new JToggleButton(tenCho);
                btnGhe.setFont(UIHelper.F_CELL); btnGhe.setFocusPainted(false);

                boolean isAlreadySelectedInSession = mainTab.getSelectedSeatsData().stream().anyMatch(s -> s.get("maCho").equals(maCho) && s.get("maLT").equals(currentMaLT));

                if (trangThai.equals("DADAT") || trangThai.equals("GIUCHO") || trangThai.equals("BAOTRI")) {
                    btnGhe.setBackground(UIHelper.DANGER); btnGhe.setForeground(Color.WHITE); btnGhe.setEnabled(false);
                } else {
                    if (isAlreadySelectedInSession) {
                        btnGhe.setBackground(UIHelper.SUCCESS); btnGhe.setForeground(Color.WHITE); btnGhe.setSelected(true);
                    } else {
                        btnGhe.setBackground(UIHelper.BORDER); btnGhe.setForeground(UIHelper.TEXT_DARK);
                    }
                    btnGhe.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    btnGhe.addItemListener(e -> {
                        if (btnGhe.isSelected()) {
                            btnGhe.setBackground(UIHelper.SUCCESS); btnGhe.setForeground(Color.WHITE);
                            Map<String, String> seatData = new HashMap<>();
                            seatData.put("maLT", currentMaLT); seatData.put("maCho", maCho); seatData.put("tenCho", tenCho);
                            seatData.put("tenToa", currentTenToa); seatData.put("tenTau", currentTenTau);
                            mainTab.getSelectedSeatsData().add(seatData);
                        } else {
                            btnGhe.setBackground(UIHelper.BORDER); btnGhe.setForeground(UIHelper.TEXT_DARK);
                            mainTab.getSelectedSeatsData().removeIf(s -> s.get("maCho").equals(maCho) && s.get("maLT").equals(currentMaLT));
                        }
                    });
                }
                pnlGhe.add(btnGhe);
            }
            revalidate(); repaint();
        }
    }
}