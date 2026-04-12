package com.gui.banve;

import javax.swing.*;
import java.awt.*;

public class Step4_ThanhToan extends JPanel {
    private TAB_BanVe mainTab;

    public Step4_ThanhToan(TAB_BanVe mainTab) {
        this.mainTab = mainTab;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout()); setOpaque(false);
        add(UIHelper.createPageTitle("THANH TOÁN & HOÀN TẤT", "Kiểm tra lại thông tin hóa đơn và tiến hành thanh toán"), BorderLayout.NORTH);

        JPanel pnl = new JPanel(new GridBagLayout()); pnl.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints(); gc.fill = GridBagConstraints.BOTH; gc.weighty = 1.0;

        JPanel pnlHoaDon = UIHelper.makeCard(new BorderLayout(0, 15));
        pnlHoaDon.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(), BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        JLabel lblHDTitle = new JLabel("CHI TIẾT HÓA ĐƠN"); lblHDTitle.setFont(UIHelper.F_H2); lblHDTitle.setHorizontalAlignment(SwingConstants.CENTER);
        pnlHoaDon.add(lblHDTitle, BorderLayout.NORTH);

        JPanel pnlReceipt = new JPanel(); pnlReceipt.setLayout(new BoxLayout(pnlReceipt, BoxLayout.Y_AXIS)); pnlReceipt.setOpaque(false);
        JLabel lblTau = new JLabel("Danh sách vé đã chọn"); lblTau.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblTau.setForeground(UIHelper.ACCENT);
        pnlReceipt.add(lblTau); pnlReceipt.add(Box.createVerticalStrut(10)); pnlReceipt.add(new JSeparator()); pnlReceipt.add(Box.createVerticalStrut(15));

        // Note: Dữ liệu tĩnh làm ví dụ, sau này bind theo selectedSeatsData
        addReceiptRow(pnlReceipt, "Nguyễn Minh Phúc (Ghế 9)", "810,600 đ", false, UIHelper.TEXT_DARK); pnlReceipt.add(Box.createVerticalStrut(10));
        addReceiptRow(pnlReceipt, "Trần Thị B (Ghế 13)", "810,600 đ", false, UIHelper.TEXT_DARK);
        pnlReceipt.add(Box.createVerticalStrut(15)); pnlReceipt.add(new JSeparator()); pnlReceipt.add(Box.createVerticalStrut(15));

        addReceiptRow(pnlReceipt, "Thành tiền:", "1,621,200 đ", true, UIHelper.TEXT_DARK); pnlReceipt.add(Box.createVerticalStrut(10));
        addReceiptRow(pnlReceipt, "Giảm giá (Khuyến mãi):", "- 0 đ", false, UIHelper.SUCCESS); pnlReceipt.add(Box.createVerticalStrut(10));
        addReceiptRow(pnlReceipt, "VAT (10%):", "162,120 đ", true, UIHelper.TEXT_DARK);

        pnlHoaDon.add(pnlReceipt, BorderLayout.CENTER);

        JPanel pnlBottomLeft = new JPanel(new BorderLayout(0, 20)); pnlBottomLeft.setOpaque(false);
        JPanel pnlKM = new JPanel(new BorderLayout(10, 0)); pnlKM.setOpaque(false);
        pnlKM.add(UIHelper.makeField("Nhập mã Khuyến Mãi"), BorderLayout.CENTER);
        JButton btnApDung = UIHelper.makeBtn("Áp dụng", false); btnApDung.setPreferredSize(new Dimension(100, 40)); pnlKM.add(btnApDung, BorderLayout.EAST);

        JPanel pnlTongTien = new JPanel(new BorderLayout()); pnlTongTien.setOpaque(false);
        JLabel lblTextTong = new JLabel("Tổng thanh toán:"); lblTextTong.setFont(UIHelper.F_LABEL); lblTextTong.setForeground(UIHelper.TEXT_MID);
        JLabel lblTienTong = new JLabel("1,783,320 đ"); lblTienTong.setFont(new Font("Segoe UI", Font.BOLD, 32)); lblTienTong.setForeground(UIHelper.DANGER); lblTienTong.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlTongTien.add(lblTextTong, BorderLayout.NORTH); pnlTongTien.add(lblTienTong, BorderLayout.SOUTH);
        pnlBottomLeft.add(pnlKM, BorderLayout.NORTH); pnlBottomLeft.add(pnlTongTien, BorderLayout.SOUTH); pnlHoaDon.add(pnlBottomLeft, BorderLayout.SOUTH);

        JPanel pnlThanhToan = UIHelper.makeCard(new BorderLayout(0, 25));
        pnlThanhToan.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(), BorderFactory.createEmptyBorder(25, 25, 25, 25)));
        JLabel lblPTTitle = new JLabel("HÌNH THỨC THANH TOÁN"); lblPTTitle.setFont(UIHelper.F_H2); pnlThanhToan.add(lblPTTitle, BorderLayout.NORTH);

        JPanel pnlMethods = new JPanel(new GridLayout(1, 2, 15, 0)); pnlMethods.setOpaque(false); pnlMethods.setAlignmentY(Component.TOP_ALIGNMENT);
        JToggleButton btnTienMat = createPaymentMethodCard("💵", "Tiền mặt", true);
        JToggleButton btnCK = createPaymentMethodCard("🏦", "Chuyển khoản", false);
        ButtonGroup bgPayment = new ButtonGroup(); bgPayment.add(btnTienMat); bgPayment.add(btnCK);
        pnlMethods.add(btnTienMat); pnlMethods.add(btnCK);
        JPanel pnlMethodsWrapper = new JPanel(new BorderLayout()); pnlMethodsWrapper.setOpaque(false); pnlMethodsWrapper.add(pnlMethods, BorderLayout.NORTH);
        pnlThanhToan.add(pnlMethodsWrapper, BorderLayout.CENTER);

        gc.gridx=0; gc.weightx=0.55; gc.insets = new Insets(0,0,0,10); pnl.add(pnlHoaDon, gc);
        gc.gridx=1; gc.weightx=0.45; gc.insets = new Insets(0,10,0,0); pnl.add(pnlThanhToan, gc);
        add(pnl, BorderLayout.CENTER);
    }

    private void addReceiptRow(JPanel parent, String label, String value, boolean isBold, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false);
        Font font = new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 15);
        JLabel lblL = new JLabel(label); lblL.setFont(font); lblL.setForeground(isBold ? UIHelper.TEXT_DARK : UIHelper.TEXT_MID);
        JLabel lblR = new JLabel(value); lblR.setFont(font); lblR.setForeground(valueColor); lblR.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(lblL, BorderLayout.WEST); row.add(lblR, BorderLayout.EAST); parent.add(row);
    }

    private JToggleButton createPaymentMethodCard(String icon, String text, boolean isSelected) {
        String htmlText = "<html><center><div style='font-size:24px;'>" + icon + "</div><div style='font-size:13px; margin-top:5px; font-family: Segoe UI;'>" + text + "</div></center></html>";
        JToggleButton btn = new JToggleButton(htmlText, isSelected) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 15;
                if (isSelected()) {
                    g2.setColor(new Color(238, 252, 246)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                    g2.setColor(new Color(0, 166, 118)); g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc);
                } else {
                    g2.setColor(getModel().isRollover() ? new Color(245, 245, 245) : Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                    g2.setColor(UIHelper.BORDER); g2.setStroke(new BasicStroke(1)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setPreferredSize(new Dimension(0, 90));
        btn.setForeground(isSelected ? new Color(0, 166, 118) : UIHelper.TEXT_MID);
        btn.addItemListener(e -> btn.setForeground(btn.isSelected() ? new Color(0, 166, 118) : UIHelper.TEXT_MID));
        return btn;
    }
}