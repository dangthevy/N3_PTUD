package com.gui.banve;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.util.Map;

public class Step3_NhapThongTinKH extends JPanel {
    private TAB_BanVe mainTab;
    private JPanel pnlDSKhach;
    private JLabel lblTimer;

    public Step3_NhapThongTinKH(TAB_BanVe mainTab) {
        this.mainTab = mainTab;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 10)); setOpaque(false);

        JPanel pnlTitle = UIHelper.createPageTitle("THÔNG TIN HÀNH KHÁCH", "Vui lòng điền đầy đủ và chính xác thông tin (Dữ liệu sẽ được in lên vé)");
        lblTimer = new JLabel("Thời gian giữ chỗ: 08:20", SwingConstants.RIGHT);
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTimer.setForeground(UIHelper.DANGER);
        pnlTitle.add(lblTimer, BorderLayout.EAST);

        add(pnlTitle, BorderLayout.NORTH);

        pnlDSKhach = new JPanel();
        pnlDSKhach.setLayout(new BoxLayout(pnlDSKhach, BoxLayout.Y_AXIS));
        pnlDSKhach.setOpaque(false);

        JScrollPane sc = new JScrollPane(pnlDSKhach);
        sc.setBorder(null); sc.getViewport().setOpaque(false); sc.setOpaque(false);
        sc.getVerticalScrollBar().setUnitIncrement(16);

        sc.getVerticalScrollBar().setUI(new BasicScrollBarUI(){
            @Override protected void configureScrollBarColors(){thumbColor=new Color(0xC0D4EE);trackColor=UIHelper.BG_PAGE;}
            @Override protected JButton createDecreaseButton(int o){return zBtn();}
            @Override protected JButton createIncreaseButton(int o){return zBtn();}
            private JButton zBtn(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });
        sc.getVerticalScrollBar().putClientProperty(FlatClientProperties.SCROLL_BAR_SHOW_BUTTONS, false);

        add(sc, BorderLayout.CENTER);
    }

    public void updateTimerDisplay(String text) {
        lblTimer.setText(text);
    }

    public void updatePassengerForms() {
        pnlDSKhach.removeAll();
        pnlDSKhach.add(createBookerForm());
        pnlDSKhach.add(Box.createVerticalStrut(25));

        JLabel lblTicketTitle = new JLabel("THÔNG TIN CHI TIẾT TỪNG VÉ");
        lblTicketTitle.setFont(UIHelper.F_H2); lblTicketTitle.setForeground(UIHelper.ACCENT);
        lblTicketTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDSKhach.add(lblTicketTitle); pnlDSKhach.add(Box.createVerticalStrut(10));

        java.util.List<Map<String, String>> seats = mainTab.getSelectedSeatsData();
        for (int i = 0; i < seats.size(); i++) {
            Map<String, String> sd = seats.get(i);
            String fullSeatInfo = sd.get("tenTau") + ", " + sd.get("tenToa") + ", Ghế " + sd.get("tenCho");
            pnlDSKhach.add(createPassengerForm(i + 1, fullSeatInfo));
            pnlDSKhach.add(Box.createVerticalStrut(15));
        }

        pnlDSKhach.revalidate(); pnlDSKhach.repaint();
    }

    private JPanel createBookerForm() {
        JPanel card = UIHelper.makeCard(new BorderLayout(0, 15));
        card.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(), BorderFactory.createEmptyBorder(15, 20, 20, 20)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("THÔNG TIN NGƯỜI ĐẶT VÉ");
        lblTitle.setFont(UIHelper.F_H2); lblTitle.setForeground(new Color(0xDC3545));
        card.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        GridBagConstraints gc = UIHelper.defaultGC();

        UIHelper.addFormRow(form, gc, 0, "Họ và Tên:", UIHelper.makeField("Nhập họ tên..."), "Số CCCD:", UIHelper.makeField("Nhập CCCD..."));
        UIHelper.addFormRow(form, gc, 1, "Số điện thoại:", UIHelper.makeField("Nhập SĐT..."), "Email:", UIHelper.makeField("Nhập Email..."));

        card.add(form, BorderLayout.CENTER); return card;
    }

    private JPanel createPassengerForm(int ticketIndex, String seatInfo) {
        JPanel card = UIHelper.makeCard(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(), BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); pnlTitle.setOpaque(false);
        JLabel lblIndex = new JLabel("Hành khách " + ticketIndex + "  |  "); lblIndex.setFont(UIHelper.F_H2); lblIndex.setForeground(UIHelper.TEXT_DARK);
        JLabel lblSeat = new JLabel(seatInfo); lblSeat.setFont(UIHelper.F_H2); lblSeat.setForeground(UIHelper.ACCENT);
        pnlTitle.add(lblIndex); pnlTitle.add(lblSeat);
        card.add(pnlTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints gc = UIHelper.defaultGC();
        UIHelper.addFormRow(form, gc, 0, "Họ Tên hành khách:", UIHelper.makeField(""), "Năm Sinh:", UIHelper.makeField(""));
        UIHelper.addFormRow(form, gc, 1, "Số CCCD:", UIHelper.makeField("Trẻ em không cần nhập"), "Loại Vé:", UIHelper.makeCombo(new String[]{"Người lớn", "Trẻ em", "Học sinh/Sinh viên", "Người cao tuổi"}));

        card.add(form, BorderLayout.CENTER); return card;
    }
}