package com.gui.banve;

import javax.swing.*;
import java.awt.*;

public class Step5_SuccessPanel extends JPanel {
    private TAB_BanVe mainTab;

    public Step5_SuccessPanel(TAB_BanVe mainTab) {
        this.mainTab = mainTab;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel card = UIHelper.makeCard(new BorderLayout(0, 20));
        card.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(), BorderFactory.createEmptyBorder(40, 60, 40, 60)));

        JLabel lblIcon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight()); int cx = getWidth() / 2; int cy = getHeight() / 2;

                g2.setColor(new Color(40, 167, 69, 30)); g2.fillOval(cx - size/2, cy - size/2, size, size);
                g2.setColor(UIHelper.SUCCESS); g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] xPoints = {cx - 15, cx - 3, cx + 18}; int[] yPoints = {cy + 2, cy + 14, cy - 14};
                g2.drawPolyline(xPoints, yPoints, 3);
                g2.dispose();
            }
        };
        lblIcon.setPreferredSize(new Dimension(80, 80));

        JLabel lblMsg = new JLabel("<html><center><b style='font-size:26px'>Đặt vé thành công!</b><br/><br/><span style='color:gray; font-size:15px'>Vé đã được lưu vào hệ thống và có thể in ngay.</span></center></html>", SwingConstants.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); pnlBtn.setOpaque(false);
        JButton btnIn = UIHelper.makeBtn("In Hóa Đơn", true);
        JButton btnMoi = UIHelper.makeBtn("Đặt vé mới", false);

        btnMoi.addActionListener(e -> mainTab.resetProcess());

        pnlBtn.add(btnMoi); pnlBtn.add(btnIn);

        JPanel pnlIconWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER)); pnlIconWrapper.setOpaque(false);
        pnlIconWrapper.add(lblIcon);

        card.add(pnlIconWrapper, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(pnlBtn, BorderLayout.SOUTH);

        add(card);
    }
}