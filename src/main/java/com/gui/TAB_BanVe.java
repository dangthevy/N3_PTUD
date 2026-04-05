package com.gui;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class TAB_BanVe extends JPanel {

    // =========================================================================
    // MÀU SẮC & FONT (Chuẩn hệ thống)
    // =========================================================================
    private static final Color BG_PAGE      = new Color(0xF4F7FB);
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color ACCENT       = new Color(0x2D7AF1);
    private static final Color ACCENT_HVR   = new Color(0x1F62C8);
    private static final Color TEXT_DARK    = new Color(0x1E2B3C);
    private static final Color TEXT_MID     = new Color(0x5A6A7D);
    private static final Color TEXT_LIGHT   = new Color(0xA0AEC0);
    private static final Color BORDER       = new Color(0xE2EAF4);
    private static final Color SUCCESS      = new Color(0x28A745);
    private static final Color DANGER       = new Color(0xDC3545);

    private static final Font F_H1 = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_H2 = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font F_CELL = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final String DATE_FMT = "dd/MM/yyyy";

    // =========================================================================
    // THÀNH PHẦN GIAO DIỆN CHÍNH
    // =========================================================================
    private CardLayout cardLayout;
    private JPanel pnlCards;
    private StepProgressPanel stepProgress;
    private JButton btnBack, btnNext;

    private int currentStep = 0;
    private final String[] STEP_NAMES = {"Tìm kiếm", "Chọn chuyến & ghế", "Thông tin KH", "Thanh toán", "Hoàn tất"};

    private Timer holdTimer;
    private int timeLeft = 500;
    private JLabel lblTimer;

    private JComboBox<String> cbGaDi, cbGaDen;
    private JRadioButton rdoMotChieu, rdoKhuHoi;
    private DatePickerField dpNgayDi, dpNgayVe;

    private boolean isRoundTrip = false;
    private JPanel pnlDirectionToggle;
    private CardLayout routeCardLayout;
    private JPanel pnlRouteCards;
    private JToggleButton btnChieuDi, btnChieuVe;
    private JLabel lblOutboundTitle, lblReturnTitle;

    private List<String> selectedSeats = new ArrayList<>();
    private JPanel pnlDSKhach;

    public TAB_BanVe() {
        setLayout(new BorderLayout());
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        stepProgress = new StepProgressPanel(STEP_NAMES);
        add(stepProgress, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        pnlCards = new JPanel(cardLayout);
        pnlCards.setOpaque(false);
        pnlCards.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        pnlCards.add(buildStep1_Search(), "STEP_0");
        pnlCards.add(buildStep2_SelectSeat(), "STEP_1");
        pnlCards.add(buildStep3_PassengerInfo(), "STEP_2");
        pnlCards.add(buildStep4_Payment(), "STEP_3");
        pnlCards.add(buildStep5_Success(), "STEP_4");

        add(pnlCards, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlFooter.setOpaque(false);

        btnBack = makeBtn("Quay lại", false);
        btnNext = makeBtn("Tiếp tục", true);

        btnBack.setVisible(false);
        btnNext.setVisible(false);

        pnlFooter.add(btnBack);
        pnlFooter.add(btnNext);
        add(pnlFooter, BorderLayout.SOUTH);

        btnNext.addActionListener(e -> nextStep());
        btnBack.addActionListener(e -> prevStep());

        setupTimer();
    }

    // =========================================================================
    // BƯỚC 1: TÌM KIẾM
    // =========================================================================
    private JPanel buildStep1_Search() {
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setOpaque(false);
        // THÊM TIÊU ĐỀ TRANG CHO BƯỚC 1
        pnlMain.add(createPageTitle("TÌM KIẾM CHUYẾN TÀU", "Vui lòng nhập thông tin hành trình để tìm kiếm chuyến đi phù hợp"), BorderLayout.NORTH);

        JPanel pnlWrapper = new JPanel(new GridBagLayout());
        pnlWrapper.setOpaque(false);

        JPanel pnlSearch = makeCard(new GridBagLayout());
        pnlSearch.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), BorderFactory.createEmptyBorder(30, 40, 30, 40)));
        GridBagConstraints gc = defaultGC();

        cbGaDi = makeCombo(new String[]{"Hà Nội", "Đà Nẵng", "Sài Gòn"});
        cbGaDen = makeCombo(new String[]{"Sài Gòn", "Nha Trang", "Huế"});

        rdoMotChieu = new JRadioButton("Một Chiều", true);
        rdoKhuHoi = new JRadioButton("Khứ Hồi");
        rdoMotChieu.setFont(F_LABEL); rdoKhuHoi.setFont(F_LABEL);
        rdoMotChieu.setOpaque(false); rdoKhuHoi.setOpaque(false);
        ButtonGroup bg = new ButtonGroup(); bg.add(rdoMotChieu); bg.add(rdoKhuHoi);
        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        pnlRadio.setOpaque(false); pnlRadio.add(rdoMotChieu); pnlRadio.add(rdoKhuHoi);

        dpNgayDi = new DatePickerField("");
        dpNgayVe = new DatePickerField("");
        dpNgayVe.setEnabledField(false);

        rdoMotChieu.addActionListener(e -> {
            dpNgayVe.setEnabledField(false);
            dpNgayVe.setDate("");
        });
        rdoKhuHoi.addActionListener(e -> dpNgayVe.setEnabledField(true));

        JButton btnSearch = makeBtn("Tìm Kiếm", true);
        btnSearch.setPreferredSize(new Dimension(250, 48));

        int r = 0;
        addFormRow(pnlSearch, gc, r++, "Ga Đi:", cbGaDi, "Ga Đến:", cbGaDen);

        gc.gridx=0; gc.gridy=r++; gc.gridwidth=4;
        pnlSearch.add(Box.createVerticalStrut(10), gc);
        gc.gridwidth=1;

        addFormRow(pnlSearch, gc, r++, "Loại vé:", pnlRadio, "", new JLabel());

        gc.gridx=0; gc.gridy=r++; gc.gridwidth=4;
        pnlSearch.add(Box.createVerticalStrut(10), gc);
        gc.gridwidth=1;

        addFormRow(pnlSearch, gc, r++, "Ngày Đi:", dpNgayDi, "Ngày Về:", dpNgayVe);

        gc.gridx = 0; gc.gridy = r; gc.gridwidth = 4; gc.fill = GridBagConstraints.NONE; gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(25, 0, 0, 0);
        pnlSearch.add(btnSearch, gc);

        pnlWrapper.add(pnlSearch);
        pnlMain.add(pnlWrapper, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> {
            if(cbGaDi.getSelectedItem().equals(cbGaDen.getSelectedItem())) {
                JOptionPane.showMessageDialog(this, "Ga đi và Ga đến không được trùng nhau!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String strNgayDi = dpNgayDi.getDate();
            String strNgayVe = dpNgayVe.getDate();

            if (strNgayDi == null || strNgayDi.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày đi!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.LocalDate ngayDi = java.time.LocalDate.parse(strNgayDi, formatter);

            if (rdoKhuHoi.isSelected()) {
                if (strNgayVe == null || strNgayVe.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày về!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                java.time.LocalDate ngayVe = java.time.LocalDate.parse(strNgayVe, formatter);
                if (ngayVe.isBefore(ngayDi)) {
                    JOptionPane.showMessageDialog(this, "Ngày về không được trước ngày đi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            isRoundTrip = rdoKhuHoi.isSelected();

            // Mũi tên → chuẩn không bị lỗi font
            lblOutboundTitle.setText("CHIỀU ĐI: " + cbGaDi.getSelectedItem() + " → " + cbGaDen.getSelectedItem());
            lblReturnTitle.setText("CHIỀU VỀ: " + cbGaDen.getSelectedItem() + " → " + cbGaDi.getSelectedItem());

            pnlDirectionToggle.setVisible(isRoundTrip);
            btnChieuDi.setSelected(true);
            routeCardLayout.show(pnlRouteCards, "OUTBOUND");

            selectedSeats.clear();
            nextStep();
        });

        return pnlMain;
    }

    // =========================================================================
    // BƯỚC 2: CHỌN CHUYẾN & CHỌN GHẾ
    // =========================================================================
    private JPanel buildStep2_SelectSeat() {
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setOpaque(false);
        // THÊM TIÊU ĐỀ TRANG CHO BƯỚC 2
        pnlMain.add(createPageTitle("CHỌN CHUYẾN & GHẾ", "Lựa chọn chuyến tàu và vị trí ghế ngồi phù hợp với bạn"), BorderLayout.NORTH);

        JPanel pnl = new JPanel(new BorderLayout(0, 15));
        pnl.setOpaque(false);

        pnlDirectionToggle = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pnlDirectionToggle.setOpaque(false);

        btnChieuDi = createSelectionTab("CHIỀU ĐI", true);
        btnChieuVe = createSelectionTab("CHIỀU VỀ", false);
        btnChieuDi.setPreferredSize(new Dimension(200, 40));
        btnChieuVe.setPreferredSize(new Dimension(200, 40));

        ButtonGroup bgDir = new ButtonGroup();
        bgDir.add(btnChieuDi); bgDir.add(btnChieuVe);

        pnlDirectionToggle.add(btnChieuDi);
        pnlDirectionToggle.add(btnChieuVe);

        routeCardLayout = new CardLayout();
        pnlRouteCards = new JPanel(routeCardLayout);
        pnlRouteCards.setOpaque(false);

        lblOutboundTitle = new JLabel();
        lblOutboundTitle.setFont(F_H1);
        lblOutboundTitle.setForeground(ACCENT);

        lblReturnTitle = new JLabel();
        lblReturnTitle.setFont(F_H1);
        lblReturnTitle.setForeground(ACCENT);

        JPanel pnlOutbound = createRouteSelectionPanel(lblOutboundTitle);
        JPanel pnlReturn = createRouteSelectionPanel(lblReturnTitle);

        pnlRouteCards.add(pnlOutbound, "OUTBOUND");
        pnlRouteCards.add(pnlReturn, "RETURN");

        btnChieuDi.addActionListener(e -> routeCardLayout.show(pnlRouteCards, "OUTBOUND"));
        btnChieuVe.addActionListener(e -> routeCardLayout.show(pnlRouteCards, "RETURN"));

        pnl.add(pnlDirectionToggle, BorderLayout.NORTH);
        pnl.add(pnlRouteCards, BorderLayout.CENTER);

        pnlMain.add(pnl, BorderLayout.CENTER);
        return pnlMain;
    }

    private JPanel createRouteSelectionPanel(JLabel lblTitle) {
        JPanel pnl = new JPanel(new BorderLayout(0, 15));
        pnl.setOpaque(false);

        // Bỏ Background trắng của Tuyến đi, để nền trong suốt cho đẹp
        JPanel pnlTuyen = new JPanel(new BorderLayout());
        pnlTuyen.setOpaque(false);
        pnlTuyen.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        pnlTuyen.add(lblTitle, BorderLayout.WEST);

        JPanel pnlSelectionWrapper = makeCard(new BorderLayout(0, 15));
        pnlSelectionWrapper.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JPanel pnlTopControls = new JPanel();
        pnlTopControls.setLayout(new BoxLayout(pnlTopControls, BoxLayout.Y_AXIS));
        pnlTopControls.setOpaque(false);

        JPanel pnlTauList = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTauList.setOpaque(false);
        pnlTauList.add(new JLabel("<html><b style='color:#5A6A7D'>Chọn Tàu: &nbsp;</b></html>"));

        String[] taus = {"SE1 (19:00)", "SE3 (21:00)", "SE5 (06:00)", "SE7 (09:00)"};
        ButtonGroup bgTau = new ButtonGroup();
        for(int i=0; i<taus.length; i++) {
            JToggleButton btnTau = createSelectionTab(taus[i], i==0);
            bgTau.add(btnTau);
            pnlTauList.add(btnTau);
        }

        JPanel pnlToaList = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlToaList.setOpaque(false);
        pnlToaList.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        pnlToaList.add(new JLabel("<html><b style='color:#5A6A7D'>Chọn Toa: &nbsp;</b></html>"));

        String[] toas = {"Toa 1: VIP", "Toa 2: Nằm mềm", "Toa 3: Ngồi mềm", "Toa 4: Ngồi cứng"};
        ButtonGroup bgToa = new ButtonGroup();
        for(int i=0; i<toas.length; i++){
            JToggleButton btnToa = createSelectionTab(toas[i], i==0);
            btnToa.setPreferredSize(new Dimension(160, 35));
            bgToa.add(btnToa);
            pnlToaList.add(btnToa);
        }

        pnlTopControls.add(pnlTauList);
        pnlTopControls.add(pnlToaList);

        JPanel pnlGheWrapper = new JPanel(new BorderLayout());
        pnlGheWrapper.setOpaque(false);
        pnlGheWrapper.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), "Sơ đồ ghế đang trống", 0, 0, F_LABEL, ACCENT));

        JPanel pnlGhe = new JPanel(new GridLayout(4, 14, 8, 8));
        pnlGhe.setOpaque(false);
        pnlGhe.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for(int i=1; i<=56; i++){
            JToggleButton btnGhe = new JToggleButton(i+"");
            btnGhe.setFont(F_CELL);
            btnGhe.setFocusPainted(false);

            if(i%7 == 0) {
                btnGhe.setBackground(DANGER);
                btnGhe.setForeground(Color.WHITE);
                btnGhe.setEnabled(false);
            } else {
                btnGhe.setBackground(BORDER);
                btnGhe.setForeground(TEXT_DARK);
                btnGhe.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                btnGhe.addItemListener(e -> {
                    String seatName = "Ghế " + btnGhe.getText();
                    if (btnGhe.isSelected()) {
                        btnGhe.setBackground(SUCCESS);
                        btnGhe.setForeground(Color.WHITE);
                        selectedSeats.add(seatName);
                    } else {
                        btnGhe.setBackground(BORDER);
                        btnGhe.setForeground(TEXT_DARK);
                        selectedSeats.remove(seatName);
                    }
                });
            }
            pnlGhe.add(btnGhe);
        }

        JScrollPane scrollGhe = new JScrollPane(pnlGhe);
        scrollGhe.setBorder(null);
        scrollGhe.setOpaque(false); scrollGhe.getViewport().setOpaque(false);

        pnlGheWrapper.add(scrollGhe, BorderLayout.CENTER);

        pnlSelectionWrapper.add(pnlTopControls, BorderLayout.NORTH);
        pnlSelectionWrapper.add(pnlGheWrapper, BorderLayout.CENTER);

        pnl.add(pnlTuyen, BorderLayout.NORTH);
        pnl.add(pnlSelectionWrapper, BorderLayout.CENTER);
        return pnl;
    }


    // =========================================================================
    // BƯỚC 3: THÔNG TIN KHÁCH HÀNG & ĐẾM NGƯỢC
    // =========================================================================
    private JPanel buildStep3_PassengerInfo() {
        JPanel pnlMain = new JPanel(new BorderLayout(0, 10));
        pnlMain.setOpaque(false);

        // THÊM TIÊU ĐỀ TRANG CHO BƯỚC 3 (Đồng thời lồng bộ đếm ngược thời gian vào góc phải)
        JPanel pnlTitle = createPageTitle("THÔNG TIN HÀNH KHÁCH", "Vui lòng điền đầy đủ và chính xác thông tin (Dữ liệu sẽ được in lên vé)");

        lblTimer = new JLabel("Thời gian giữ chỗ: 08:20", SwingConstants.RIGHT);
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTimer.setForeground(DANGER);
        pnlTitle.add(lblTimer, BorderLayout.EAST);

        pnlMain.add(pnlTitle, BorderLayout.NORTH);

        pnlDSKhach = new JPanel();
        pnlDSKhach.setLayout(new BoxLayout(pnlDSKhach, BoxLayout.Y_AXIS));
        pnlDSKhach.setOpaque(false);

        JScrollPane sc = new JScrollPane(pnlDSKhach);
        sc.setBorder(null); sc.getViewport().setOpaque(false); sc.setOpaque(false);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        pnlMain.add(sc, BorderLayout.CENTER);

        return pnlMain;
    }

    private void updatePassengerForms() {
        pnlDSKhach.removeAll();

        pnlDSKhach.add(createBookerForm());
        pnlDSKhach.add(Box.createVerticalStrut(25));

        JLabel lblTicketTitle = new JLabel("THÔNG TIN CHI TIẾT TỪNG VÉ");
        lblTicketTitle.setFont(F_H2);
        lblTicketTitle.setForeground(ACCENT);
        lblTicketTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDSKhach.add(lblTicketTitle);
        pnlDSKhach.add(Box.createVerticalStrut(10));

        for (int i = 0; i < selectedSeats.size(); i++) {
            String fullSeatInfo = "Tàu SE1, Toa 1, " + selectedSeats.get(i);
            pnlDSKhach.add(createPassengerForm(i + 1, fullSeatInfo));
            pnlDSKhach.add(Box.createVerticalStrut(15));
        }

        pnlDSKhach.revalidate();
        pnlDSKhach.repaint();
    }

    private JPanel createBookerForm() {
        JPanel card = makeCard(new BorderLayout(0, 15));
        card.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), BorderFactory.createEmptyBorder(15, 20, 20, 20)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("THÔNG TIN NGƯỜI ĐẶT VÉ");
        lblTitle.setFont(F_H2);
        lblTitle.setForeground(new Color(0xDC3545));
        card.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = defaultGC();

        addFormRow(form, gc, 0, "Họ và Tên:", makeField("Nhập họ tên người mua..."), "Số CCCD:", makeField("Nhập CCCD/Hộ chiếu..."));
        addFormRow(form, gc, 1, "Số điện thoại:", makeField("Nhập SĐT..."), "Email:", makeField("Nhập Email nhận vé (tùy chọn)"));

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createPassengerForm(int ticketIndex, String seatInfo) {
        JPanel card = makeCard(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTitle.setOpaque(false);

        JLabel lblIndex = new JLabel("Hành khách " + ticketIndex + "  |  ");
        lblIndex.setFont(F_H2); lblIndex.setForeground(TEXT_DARK);

        JLabel lblSeat = new JLabel(seatInfo);
        lblSeat.setFont(F_H2); lblSeat.setForeground(ACCENT);

        pnlTitle.add(lblIndex);
        pnlTitle.add(lblSeat);

        card.add(pnlTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = defaultGC();

        addFormRow(form, gc, 0, "Họ Tên hành khách:", makeField(""), "Năm Sinh:", makeField(""));
        addFormRow(form, gc, 1, "Số CCCD:", makeField("Trẻ em không cần nhập"), "Loại Vé:", makeCombo(new String[]{"Người lớn", "Trẻ em", "Học sinh/Sinh viên", "Người cao tuổi"}));

        card.add(form, BorderLayout.CENTER);
        return card;
    }


    // =========================================================================
    // BƯỚC 4: THANH TOÁN
    // =========================================================================
    private JPanel buildStep4_Payment() {
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setOpaque(false);
        // THÊM TIÊU ĐỀ TRANG CHO BƯỚC 4
        pnlMain.add(createPageTitle("THANH TOÁN & HOÀN TẤT", "Kiểm tra lại thông tin hóa đơn và tiến hành thanh toán"), BorderLayout.NORTH);

        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH; gc.weighty = 1.0;

        // 1. KHUNG BÊN TRÁI: CHI TIẾT HÓA ĐƠN
        JPanel pnlHoaDon = makeCard(new BorderLayout(0, 15));
        pnlHoaDon.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        JLabel lblHDTitle = new JLabel("CHI TIẾT HÓA ĐƠN");
        lblHDTitle.setFont(F_H2);
        lblHDTitle.setHorizontalAlignment(SwingConstants.CENTER);
        pnlHoaDon.add(lblHDTitle, BorderLayout.NORTH);

        JPanel pnlReceipt = new JPanel();
        pnlReceipt.setLayout(new BoxLayout(pnlReceipt, BoxLayout.Y_AXIS));
        pnlReceipt.setOpaque(false);

        JLabel lblTau = new JLabel("Tàu SE1 (Hà Nội - Sài Gòn)");
        lblTau.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTau.setForeground(ACCENT);
        pnlReceipt.add(lblTau);
        pnlReceipt.add(Box.createVerticalStrut(10));
        pnlReceipt.add(new JSeparator());
        pnlReceipt.add(Box.createVerticalStrut(15));

        addReceiptRow(pnlReceipt, "Nguyễn Minh Phúc (Ghế 9)", "810,600 đ", false, TEXT_DARK);
        pnlReceipt.add(Box.createVerticalStrut(10));
        addReceiptRow(pnlReceipt, "Trần Thị B (Ghế 13)", "810,600 đ", false, TEXT_DARK);

        pnlReceipt.add(Box.createVerticalStrut(15));
        pnlReceipt.add(new JSeparator());
        pnlReceipt.add(Box.createVerticalStrut(15));

        addReceiptRow(pnlReceipt, "Thành tiền:", "1,621,200 đ", true, TEXT_DARK);
        pnlReceipt.add(Box.createVerticalStrut(10));
        addReceiptRow(pnlReceipt, "Giảm giá (Khuyến mãi):", "- 0 đ", false, SUCCESS);
        pnlReceipt.add(Box.createVerticalStrut(10));
        addReceiptRow(pnlReceipt, "VAT (10%):", "162,120 đ", true, TEXT_DARK);

        pnlHoaDon.add(pnlReceipt, BorderLayout.CENTER);

        JPanel pnlBottomLeft = new JPanel(new BorderLayout(0, 20));
        pnlBottomLeft.setOpaque(false);

        JPanel pnlKM = new JPanel(new BorderLayout(10, 0));
        pnlKM.setOpaque(false);
        JTextField txtKM = makeField("Nhập mã Khuyến Mãi");
        JButton btnApDung = makeBtn("Áp dụng", false);
        btnApDung.setPreferredSize(new Dimension(100, 40));
        pnlKM.add(txtKM, BorderLayout.CENTER);
        pnlKM.add(btnApDung, BorderLayout.EAST);

        JPanel pnlTongTien = new JPanel(new BorderLayout());
        pnlTongTien.setOpaque(false);
        JLabel lblTextTong = new JLabel("Tổng thanh toán:");
        lblTextTong.setFont(F_LABEL);
        lblTextTong.setForeground(TEXT_MID);

        JLabel lblTienTong = new JLabel("1,783,320 đ");
        lblTienTong.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTienTong.setForeground(DANGER);
        lblTienTong.setHorizontalAlignment(SwingConstants.RIGHT);

        pnlTongTien.add(lblTextTong, BorderLayout.NORTH);
        pnlTongTien.add(lblTienTong, BorderLayout.SOUTH);

        pnlBottomLeft.add(pnlKM, BorderLayout.NORTH);
        pnlBottomLeft.add(pnlTongTien, BorderLayout.SOUTH);

        pnlHoaDon.add(pnlBottomLeft, BorderLayout.SOUTH);

        // 2. KHUNG BÊN PHẢI: PHƯƠNG THỨC THANH TOÁN
        JPanel pnlThanhToan = makeCard(new BorderLayout(0, 25));
        pnlThanhToan.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        JLabel lblPTTitle = new JLabel("HÌNH THỨC THANH TOÁN");
        lblPTTitle.setFont(F_H2);
        pnlThanhToan.add(lblPTTitle, BorderLayout.NORTH);

        JPanel pnlMethods = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlMethods.setOpaque(false);
        pnlMethods.setAlignmentY(Component.TOP_ALIGNMENT);

        JToggleButton btnTienMat = createPaymentMethodCard("💵", "Tiền mặt", true);
        JToggleButton btnCK = createPaymentMethodCard("🏦", "Chuyển khoản", false);

        ButtonGroup bgPayment = new ButtonGroup();
        bgPayment.add(btnTienMat);
        bgPayment.add(btnCK);

        pnlMethods.add(btnTienMat);
        pnlMethods.add(btnCK);

        JPanel pnlMethodsWrapper = new JPanel(new BorderLayout());
        pnlMethodsWrapper.setOpaque(false);
        pnlMethodsWrapper.add(pnlMethods, BorderLayout.NORTH);

        pnlThanhToan.add(pnlMethodsWrapper, BorderLayout.CENTER);

        gc.gridx=0; gc.weightx=0.55; gc.insets = new Insets(0,0,0,10); pnl.add(pnlHoaDon, gc);
        gc.gridx=1; gc.weightx=0.45; gc.insets = new Insets(0,10,0,0); pnl.add(pnlThanhToan, gc);

        pnlMain.add(pnl, BorderLayout.CENTER);
        return pnlMain;
    }

    // =========================================================================
    // BƯỚC 5: THÀNH CÔNG
    // =========================================================================
    private JPanel buildStep5_Success() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);

        JPanel card = makeCard(new BorderLayout(0, 20));
        card.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), BorderFactory.createEmptyBorder(40, 60, 40, 60)));

        JLabel lblIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight());
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g2.setColor(new Color(40, 167, 69, 30));
                g2.fillOval(cx - size/2, cy - size/2, size, size);

                g2.setColor(SUCCESS);
                g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] xPoints = {cx - 15, cx - 3, cx + 18};
                int[] yPoints = {cy + 2, cy + 14, cy - 14};
                g2.drawPolyline(xPoints, yPoints, 3);

                g2.dispose();
            }
        };
        lblIcon.setPreferredSize(new Dimension(80, 80));

        JLabel lblMsg = new JLabel("<html><center><b style='font-size:26px'>Đặt vé thành công!</b><br/><br/><span style='color:gray; font-size:15px'>Vé đã được lưu vào hệ thống và có thể in ngay.</span></center></html>", SwingConstants.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pnlBtn.setOpaque(false);
        JButton btnIn = makeBtn("In Hóa Đơn", true);
        JButton btnMoi = makeBtn("Đặt vé mới", false);

        btnMoi.addActionListener(e -> {
            currentStep = 0;
            selectedSeats.clear();
            switchCard();
        });

        pnlBtn.add(btnMoi); pnlBtn.add(btnIn);

        JPanel pnlIconWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlIconWrapper.setOpaque(false);
        pnlIconWrapper.add(lblIcon);

        card.add(pnlIconWrapper, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(pnlBtn, BorderLayout.SOUTH);

        pnl.add(card);
        return pnl;
    }

    // =========================================================================
    // LOGIC ĐIỀU HƯỚNG WIZARD
    // =========================================================================
    private void nextStep() {
        if (currentStep == 1) {
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 ghế để tiếp tục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            updatePassengerForms();
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

        if (currentStep == 0) {
            btnNext.setVisible(false);
        } else if (currentStep == 1) {
            btnNext.setVisible(true);
            btnNext.setText("Tiếp tục: Nhập thông tin");
        } else if (currentStep == 2) {
            btnNext.setVisible(true);
            btnNext.setText("Chuyển đến Thanh toán");
        } else if (currentStep == 3) {
            btnNext.setVisible(true);
            btnNext.setText("Xác nhận Thanh Toán");
        } else {
            btnNext.setVisible(false);
        }
    }

    private void setupTimer() {
        holdTimer = new Timer(1000, e -> {
            if (timeLeft > 0) {
                timeLeft--;
                int min = timeLeft / 60;
                int sec = timeLeft % 60;
                lblTimer.setText(String.format("Thời gian giữ chỗ: %02d:%02d", min, sec));
            } else {
                holdTimer.stop();
                JOptionPane.showMessageDialog(this, "Đã hết thời gian giữ chỗ. Vui lòng thao tác lại từ đầu!", "Hết giờ", JOptionPane.WARNING_MESSAGE);
                currentStep = 0;
                selectedSeats.clear();
                switchCard();
            }
        });
    }

    // =========================================================================
    // HELPER COMPONENT CHUNG
    // =========================================================================

    // HÀM TẠO TIÊU ĐỀ TRANG MỚI (Đồng bộ với TAB_Ga_Tuyen)
    private JPanel createPageTitle(String title, String subTitle) {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 5));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(F_H1);
        lblTitle.setForeground(ACCENT);
        pnl.add(lblTitle, BorderLayout.NORTH);

        if (subTitle != null && !subTitle.isEmpty()) {
            JLabel lblSub = new JLabel(subTitle);
            lblSub.setFont(F_CELL);
            lblSub.setForeground(TEXT_MID);
            pnl.add(lblSub, BorderLayout.CENTER);
        }

        return pnl;
    }

    private void addReceiptRow(JPanel parent, String label, String value, boolean isBold, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        Font font = new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 15);

        JLabel lblL = new JLabel(label);
        lblL.setFont(font);
        lblL.setForeground(isBold ? TEXT_DARK : TEXT_MID);

        JLabel lblR = new JLabel(value);
        lblR.setFont(font);
        lblR.setForeground(valueColor);
        lblR.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lblL, BorderLayout.WEST);
        row.add(lblR, BorderLayout.EAST);
        parent.add(row);
    }

    private JToggleButton createPaymentMethodCard(String icon, String text, boolean isSelected) {
        String htmlText = "<html><center><div style='font-size:24px;'>" + icon + "</div><div style='font-size:13px; margin-top:5px; font-family: Segoe UI;'>" + text + "</div></center></html>";

        JToggleButton btn = new JToggleButton(htmlText, isSelected) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 15;

                if (isSelected()) {
                    g2.setColor(new Color(238, 252, 246));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                    g2.setColor(new Color(0, 166, 118));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc);
                } else {
                    g2.setColor(getModel().isRollover() ? new Color(245, 245, 245) : Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                    g2.setColor(BORDER);
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 90));

        btn.setForeground(isSelected ? new Color(0, 166, 118) : TEXT_MID);
        btn.addItemListener(e -> btn.setForeground(btn.isSelected() ? new Color(0, 166, 118) : TEXT_MID));
        return btn;
    }

    private JPanel makeCard(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(BG_CARD); p.setBorder(new ShadowBorder()); return p;
    }

    private JTextField makeField(String ph) {
        JTextField tf = new JTextField();
        tf.setFont(F_CELL); tf.setForeground(TEXT_DARK); tf.setBackground(new Color(0xF8FAFD));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, ph);
        tf.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        return tf;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(F_CELL); cb.setBackground(new Color(0xF8FAFD)); cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return cb;
    }

    private JButton makeBtn(String text, boolean isPrimary) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isPrimary) g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT);
                else g2.setColor(getModel().isRollover() ? BORDER : BG_CARD);

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                if (!isPrimary) {
                    g2.setColor(BORDER);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_LABEL);
        b.setForeground(isPrimary ? Color.WHITE : TEXT_DARK);
        b.setPreferredSize(new Dimension(200, 42));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    private JToggleButton createSelectionTab(String text, boolean isSelected) {
        JToggleButton btn = new JToggleButton(text, isSelected) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(ACCENT);
                } else {
                    g2.setColor(getModel().isRollover() ? BORDER : BG_CARD);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (!isSelected()) {
                    g2.setColor(BORDER);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_LABEL);
        btn.setForeground(isSelected ? Color.WHITE : TEXT_DARK);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 35));
        btn.addItemListener(e -> btn.setForeground(btn.isSelected() ? Color.WHITE : TEXT_DARK));
        return btn;
    }

    private GridBagConstraints defaultGC() {
        GridBagConstraints gc = new GridBagConstraints(); gc.insets = new Insets(8, 10, 8, 10);
        gc.anchor = GridBagConstraints.WEST; gc.fill = GridBagConstraints.HORIZONTAL; return gc;
    }

    private void addFormRow(JPanel form, GridBagConstraints gc, int row, String l1, JComponent c1, String l2, JComponent c2) {
        gc.gridy = row;
        gc.gridx = 0; gc.weightx = 0;
        JLabel lbl1 = new JLabel(l1); lbl1.setFont(F_LABEL); lbl1.setForeground(TEXT_MID); form.add(lbl1, gc);
        gc.gridx = 1; gc.weightx = 1; c1.setPreferredSize(new Dimension(250, 40)); form.add(c1, gc);

        if(!l2.isEmpty()) {
            gc.gridx = 2; gc.weightx = 0;
            JLabel lbl2 = new JLabel(l2); lbl2.setFont(F_LABEL); lbl2.setForeground(TEXT_MID); form.add(lbl2, gc);
            gc.gridx = 3; gc.weightx = 1; c2.setPreferredSize(new Dimension(250, 40)); form.add(c2, gc);
        }
    }

    // =========================================================================
    // LỚP VẼ THANH TIẾN TRÌNH
    // =========================================================================
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

            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(4));
            g2.drawLine(paddingX, cy, width - paddingX, cy);

            g2.setColor(ACCENT);
            if (current > 0) {
                g2.drawLine(paddingX, cy, paddingX + (spacing * current), cy);
            }

            for (int i = 0; i < stepCount; i++) {
                int cx = paddingX + (i * spacing);

                if (i <= current) g2.setColor(ACCENT);
                else g2.setColor(BORDER);

                g2.fillOval(cx - circleRadius/2, cy - circleRadius/2, circleRadius, circleRadius);

                g2.setColor(i <= current ? Color.WHITE : TEXT_MID);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String num = String.valueOf(i + 1);
                g2.drawString(num, cx - fm.stringWidth(num)/2, cy + fm.getAscent()/2 - 1);

                g2.setColor(i <= current ? ACCENT : TEXT_MID);
                g2.setFont(new Font("Segoe UI", i == current ? Font.BOLD : Font.PLAIN, 13));
                fm = g2.getFontMetrics();
                g2.drawString(steps[i], cx - fm.stringWidth(steps[i])/2, cy + circleRadius + 10);
            }
            g2.dispose();
        }
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

    // =========================================================================
    // DATE PICKER CUSTOM
    // =========================================================================
    private class DatePickerField extends JPanel {
        private final JTextField   txt;
        private final Calendar     cal;
        private JPanel             pnlGrid;
        private JComboBox<String>  cbThang;
        private JComboBox<Integer> cbNam;
        private JWindow            popup;
        private boolean            isEnabled = true;

        private static final String[] TEN_THANG={"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6","Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};
        private static final String[] TEN_THU={"T2","T3","T4","T5","T6","T7","CN"};

        DatePickerField(String init){
            setLayout(new BorderLayout()); setOpaque(false);
            cal=Calendar.getInstance();
            if(init!=null&&!init.isEmpty()){try{cal.setTime(new SimpleDateFormat(DATE_FMT).parse(init));}catch(Exception ignored){}}
            String disp=init!=null&&!init.isEmpty()?init:new SimpleDateFormat(DATE_FMT).format(cal.getTime());

            txt=new JTextField(disp); txt.setFont(F_CELL); txt.setForeground(TEXT_DARK);
            txt.setBackground(new Color(0xF8FAFD)); txt.setEditable(false);
            txt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1,true),BorderFactory.createEmptyBorder(6,10,6,36)));

            JLabel ico=new JLabel(){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isEnabled ? TEXT_MID : TEXT_LIGHT);
                    int cx=getWidth()/2,cy=getHeight()/2;g2.drawRoundRect(cx-8,cy-7,16,14,3,3);g2.drawLine(cx-8,cy-4,cx+8,cy-4);g2.drawLine(cx-4,cy-10,cx-4,cy-5);g2.drawLine(cx+4,cy-10,cx+4,cy-5);
                    g2.fillOval(cx-6,cy-1,3,3);g2.fillOval(cx-1,cy-1,3,3);g2.fillOval(cx+4,cy-1,3,3);g2.fillOval(cx-6,cy+3,3,3);g2.fillOval(cx-1,cy+3,3,3);g2.dispose();
                }
            };
            ico.setPreferredSize(new Dimension(32,36)); ico.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JPanel wrap=new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.add(txt,BorderLayout.CENTER); wrap.add(ico,BorderLayout.EAST); add(wrap,BorderLayout.CENTER);

            MouseAdapter ma=new MouseAdapter(){
                @Override public void mouseClicked(MouseEvent e){ if(isEnabled) toggle(); }
            };
            txt.addMouseListener(ma); ico.addMouseListener(ma);
        }

        public void setEnabledField(boolean enabled) {
            this.isEnabled = enabled;
            txt.setBackground(enabled ? new Color(0xF8FAFD) : new Color(0xEEF2F8));
            txt.setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            repaint();
        }

        public void setDate(String date) { txt.setText(date); }
        public String getDate() { return txt.getText(); }

        private void toggle(){if(popup!=null&&popup.isVisible()){popup.dispose();popup=null;return;}showPop();}
        private void showPop(){
            popup=new JWindow(SwingUtilities.getWindowAncestor(this));popup.setLayout(new BorderLayout());
            JPanel p=new JPanel(new BorderLayout(0,6));p.setBackground(BG_CARD);
            p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1),BorderFactory.createEmptyBorder(12,12,12,12)));
            p.add(navBar(),BorderLayout.NORTH);pnlGrid=new JPanel(new GridLayout(0,7,2,2));pnlGrid.setBackground(BG_CARD);p.add(pnlGrid,BorderLayout.CENTER);
            fillGrid();popup.add(p);popup.pack();popup.setSize(Math.max(280,popup.getWidth()),popup.getHeight());
            Point loc=txt.getLocationOnScreen();popup.setLocation(loc.x,loc.y+txt.getHeight()+2);popup.setVisible(true);
            popup.addWindowFocusListener(new java.awt.event.WindowFocusListener(){
                @Override public void windowGainedFocus(java.awt.event.WindowEvent e){}
                @Override public void windowLostFocus(java.awt.event.WindowEvent e){if(popup!=null){popup.dispose();popup=null;}}
            });
        }
        private JPanel navBar(){
            JPanel nav=new JPanel(new BorderLayout(4,0));nav.setBackground(BG_CARD);
            JButton prev=navBtn("<");JButton next=navBtn(">");
            prev.addActionListener(e->{cal.add(Calendar.MONTH,-1);cbThang.setSelectedIndex(cal.get(Calendar.MONTH));cbNam.setSelectedItem(cal.get(Calendar.YEAR));fillGrid();});
            next.addActionListener(e->{cal.add(Calendar.MONTH, 1);cbThang.setSelectedIndex(cal.get(Calendar.MONTH));cbNam.setSelectedItem(cal.get(Calendar.YEAR));fillGrid();});
            cbThang=new JComboBox<>(TEN_THANG);cbThang.setFont(F_SMALL);cbThang.setSelectedIndex(cal.get(Calendar.MONTH));cbThang.setPreferredSize(new Dimension(82,26));
            cbThang.addActionListener(e->{cal.set(Calendar.MONTH,cbThang.getSelectedIndex());fillGrid();});
            int y=Calendar.getInstance().get(Calendar.YEAR);Integer[] yrs=new Integer[16];for(int i=0;i<16;i++)yrs[i]=y-5+i;
            cbNam=new JComboBox<>(yrs);cbNam.setFont(F_SMALL);cbNam.setSelectedItem(cal.get(Calendar.YEAR));cbNam.setPreferredSize(new Dimension(60,26));
            cbNam.addActionListener(e->{if(cbNam.getSelectedItem()!=null){cal.set(Calendar.YEAR,(Integer)cbNam.getSelectedItem());fillGrid();}});
            JPanel ctr=new JPanel(new FlowLayout(FlowLayout.CENTER,4,0));ctr.setBackground(BG_CARD);ctr.add(cbThang);ctr.add(cbNam);
            nav.add(prev,BorderLayout.WEST);nav.add(ctr,BorderLayout.CENTER);nav.add(next,BorderLayout.EAST);return nav;
        }
        private void fillGrid(){
            pnlGrid.removeAll();
            for(String th:TEN_THU){JLabel l=new JLabel(th,SwingConstants.CENTER);l.setFont(new Font("Segoe UI",Font.BOLD,11));l.setPreferredSize(new Dimension(32,24));l.setForeground(TEXT_MID);pnlGrid.add(l);}
            Calendar tmp=(Calendar)cal.clone();tmp.set(Calendar.DAY_OF_MONTH,1);int first=(tmp.get(Calendar.DAY_OF_WEEK)+5)%7;
            Calendar today=Calendar.getInstance();int todayD=today.get(Calendar.DAY_OF_MONTH);
            boolean sm=today.get(Calendar.MONTH)==cal.get(Calendar.MONTH)&&today.get(Calendar.YEAR)==cal.get(Calendar.YEAR);
            int chosen=-1;
            try{Calendar c=Calendar.getInstance();c.setTime(new SimpleDateFormat(DATE_FMT).parse(txt.getText()));if(c.get(Calendar.MONTH)==cal.get(Calendar.MONTH)&&c.get(Calendar.YEAR)==cal.get(Calendar.YEAR))chosen=c.get(Calendar.DAY_OF_MONTH);}catch(Exception ignored){}
            for(int i=0;i<first;i++)pnlGrid.add(new JLabel());
            int days=cal.getActualMaximum(Calendar.DAY_OF_MONTH);final int fc=chosen;
            for(int d=1;d<=days;d++){
                final int nd=d;boolean isT=sm&&d==todayD;boolean isSel=d==fc;
                JButton b=new JButton(String.valueOf(d)){
                    @Override protected void paintComponent(Graphics g){
                        Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                        if(isSel){g2.setColor(ACCENT);g2.fillOval(1,1,getWidth()-2,getHeight()-2);}
                        else if(getModel().isRollover()){g2.setColor(new Color(0xDDEEFF));g2.fillOval(1,1,getWidth()-2,getHeight()-2);}
                        else if(isT){g2.setColor(new Color(0xE8F1FB));g2.fillOval(1,1,getWidth()-2,getHeight()-2);}
                        g2.dispose();super.paintComponent(g);
                    }
                };
                b.setFont(new Font("Segoe UI",isT?Font.BOLD:Font.PLAIN,11));b.setForeground(isSel?Color.WHITE:isT?ACCENT:TEXT_DARK);
                b.setPreferredSize(new Dimension(32,32));b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setMargin(new Insets(0,0,0,0));
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.addActionListener(e->{cal.set(Calendar.DAY_OF_MONTH,nd);txt.setText(new SimpleDateFormat(DATE_FMT).format(cal.getTime()));if(popup!=null){popup.dispose();popup=null;}});
                pnlGrid.add(b);
            }
            pnlGrid.revalidate();pnlGrid.repaint();
        }
        private JButton navBtn(String t){
            JButton b=new JButton(t);b.setFont(new Font("Segoe UI",Font.BOLD,14));b.setForeground(ACCENT);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setMargin(new Insets(0,0,0,0));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setPreferredSize(new Dimension(32,32));return b;
        }
    }
}