package com.gui.banve;


import com.dao.DAO_BanVe;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TAB_BanVe extends JPanel {

    private CardLayout cardLayout;
    private JPanel pnlCards;
    private StepProgressPanel stepProgress;
    private JButton btnBack, btnNext;

    private int currentStep = 0;
    private final String[] STEP_NAMES = {"Tìm kiếm", "Chọn chuyến & ghế", "Thông tin KH", "Thanh toán", "Hoàn tất"};

    private Timer holdTimer;
    private int timeLeft = 500;

    // === DỮ LIỆU DÙNG CHUNG ===
    private DAO_BanVe daoBanVe = new DAO_BanVe();
    private List<Map<String, String>> selectedSeatsData = new ArrayList<>();
    private boolean isRoundTrip = false;

    // === CÁC PANEL CON ===
    private Step1_TimKiem step1;
    private Step2_ChonChoNgoi step2;
    private Step3_NhapThongTinKH step3;
    private Step4_ThanhToan step4;
    private Step5_SuccessPanel step5;

    public TAB_BanVe() {
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        stepProgress = new StepProgressPanel(STEP_NAMES);
        add(stepProgress, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        pnlCards = new JPanel(cardLayout);
        pnlCards.setOpaque(false);
        pnlCards.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        // Khởi tạo các Step và truyền this vào
        step1 = new Step1_TimKiem(this);
        step2 = new Step2_ChonChoNgoi(this);
        step3 = new Step3_NhapThongTinKH(this);
        step4 = new Step4_ThanhToan(this);
        step5 = new Step5_SuccessPanel(this);

        pnlCards.add(step1, "STEP_0");
        pnlCards.add(step2, "STEP_1");
        pnlCards.add(step3, "STEP_2");
        pnlCards.add(step4, "STEP_3");
        pnlCards.add(step5, "STEP_4");

        add(pnlCards, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlFooter.setOpaque(false);
        btnBack = UIHelper.makeBtn("Quay lại", false);
        btnNext = UIHelper.makeBtn("Tiếp tục", true);

        btnBack.setVisible(false);
        btnNext.setVisible(false);

        pnlFooter.add(btnBack);
        pnlFooter.add(btnNext);
        add(pnlFooter, BorderLayout.SOUTH);

        btnNext.addActionListener(e -> nextStep());
        btnBack.addActionListener(e -> prevStep());

        setupTimer();
    }

    // === GETTERS & SETTERS CHO CÁC STEP SỬ DỤNG ===
    public DAO_BanVe getDaoBanVe() { return daoBanVe; }
    public List<Map<String, String>> getSelectedSeatsData() { return selectedSeatsData; }
    public boolean isRoundTrip() { return isRoundTrip; }
    public void setRoundTrip(boolean roundTrip) { isRoundTrip = roundTrip; }
    public Step2_ChonChoNgoi getStep2() { return step2; }

    public void resetProcess() {
        currentStep = 0;
        selectedSeatsData.clear();
        switchCard();
    }

    public void nextStep() {
        // Validate trước khi qua trang
        if (currentStep == 1) {
            if (selectedSeatsData.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 ghế để tiếp tục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            step3.updatePassengerForms();
        }

        if (currentStep < 4) {
            currentStep++;
            switchCard();
            if(currentStep == 2) {
                timeLeft = 500;
                holdTimer.restart();
            }
        }
    }

    private void prevStep() {
        if (currentStep > 0) {
            currentStep--;
            switchCard();
            if(currentStep != 2) holdTimer.stop();
        }
    }

    private void switchCard() {
        cardLayout.show(pnlCards, "STEP_" + currentStep);
        stepProgress.updateStep(currentStep);

        btnBack.setVisible(currentStep > 0 && currentStep < 4);

        if (currentStep == 0) btnNext.setVisible(false);
        else if (currentStep == 1) { btnNext.setVisible(true); btnNext.setText("Tiếp tục: Nhập thông tin"); }
        else if (currentStep == 2) { btnNext.setVisible(true); btnNext.setText("Chuyển đến Thanh toán"); }
        else if (currentStep == 3) { btnNext.setVisible(true); btnNext.setText("Xác nhận Thanh Toán"); }
        else btnNext.setVisible(false);
    }

    private void setupTimer() {
        holdTimer = new Timer(1000, e -> {
            if (timeLeft > 0) {
                timeLeft--;
                int min = timeLeft / 60;
                int sec = timeLeft % 60;
                step3.updateTimerDisplay(String.format("Thời gian giữ chỗ: %02d:%02d", min, sec));
            } else {
                holdTimer.stop();
                JOptionPane.showMessageDialog(this, "Đã hết thời gian giữ chỗ. Vui lòng thao tác lại từ đầu!", "Hết giờ", JOptionPane.WARNING_MESSAGE);
                resetProcess();
            }
        });
    }

    // Component Progress Bar nội bộ
    private class StepProgressPanel extends JPanel {
        private String[] steps;
        private int current = 0;

        public StepProgressPanel(String[] steps) {
            this.steps = steps;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 80));
        }

        public void updateStep(int step) {
            this.current = step;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int stepCount = steps.length;
            int paddingX = 100;
            int spacing = (width - paddingX * 2) / (stepCount - 1);
            int circleRadius = 30;
            int cy = height / 2 - 10;

            g2.setColor(UIHelper.BORDER);
            g2.setStroke(new BasicStroke(4));
            g2.drawLine(paddingX, cy, width - paddingX, cy);

            g2.setColor(UIHelper.ACCENT);
            if (current > 0) g2.drawLine(paddingX, cy, paddingX + (spacing * current), cy);

            for (int i = 0; i < stepCount; i++) {
                int cx = paddingX + (i * spacing);

                if (i <= current) g2.setColor(UIHelper.ACCENT);
                else g2.setColor(UIHelper.BORDER);

                g2.fillOval(cx - circleRadius/2, cy - circleRadius/2, circleRadius, circleRadius);

                g2.setColor(i <= current ? Color.WHITE : UIHelper.TEXT_MID);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String num = String.valueOf(i + 1);
                g2.drawString(num, cx - fm.stringWidth(num)/2, cy + fm.getAscent()/2 - 1);

                g2.setColor(i <= current ? UIHelper.ACCENT : UIHelper.TEXT_MID);
                g2.setFont(new Font("Segoe UI", i == current ? Font.BOLD : Font.PLAIN, 13));
                fm = g2.getFontMetrics();
                g2.drawString(steps[i], cx - fm.stringWidth(steps[i])/2, cy + circleRadius + 10);
            }
            g2.dispose();
        }
    }
}