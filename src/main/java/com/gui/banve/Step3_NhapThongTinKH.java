package com.gui.banve;

import com.dao.DAO_KhachHang;
import com.entities.KhachHang;
import com.formdev.flatlaf.FlatClientProperties;
import com.gui.UITheme;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Step3_NhapThongTinKH - Nhập thông tin hành khách khi bán vé
 * UI đồng bộ UITheme: card trắng bo góc, field label-trên, nút PRIMARY/SUCCESS/DANGER
 */
public class Step3_NhapThongTinKH extends JPanel {

    private final TAB_BanVe mainTab;
    private final DAO_KhachHang kh_dao = new DAO_KhachHang();

    private JLabel lblTimer;

    // BOOKER
    private JTextField txtBkSdt, txtBkHoTen, txtBkCccd, txtBkEmail;
    private JLabel lblBkStatus, lblCccdError;
    private JButton btnConfirmBooker, btnEditBooker;
    private KhachHang confirmedBooker = null;

    // PASSENGER
    private JLabel lblCurrentTicket, lblPaxStatus;
    private JTextField txtPaxSdt, txtPaxHoTen, txtPaxCccd, txtPaxEmail;
    private JComboBox<String> cbLoaiVe;
    private JButton btnConfirmPax, btnCopyBooker;
    private JPanel pnlPaxSection;
    private JLabel lblPaxSdtError, lblPaxCccdError;

    // Debounce
    private final Timer debounceBk  = new Timer(400, e -> searchBookerFromDB());
    private final Timer debouncePax = new Timer(400, e -> searchPaxFromDB());
    { debounceBk.setRepeats(false); debouncePax.setRepeats(false); }

    // RIGHT PANEL
    private JPanel pnlTicketList;
    private final List<PassengerCard> passengerCards = new ArrayList<>();
    private int currentIdx = 0;

    public Step3_NhapThongTinKH(TAB_BanVe mainTab) {
        this.mainTab = mainTab;
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_PAGE);
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(UITheme.PRIMARY);
        pnlHeader.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("THÔNG TIN HÀNH KHÁCH");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        lblTimer = new JLabel("Thời gian giữ chỗ: 08:00", JLabel.RIGHT);
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTimer.setForeground(new Color(255, 220, 100));

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(lblTimer, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // Body: 2 cột bằng nhau
        JPanel pnlBody = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlBody.setBackground(UITheme.BG_PAGE);
        pnlBody.setBorder(new EmptyBorder(14, 14, 14, 14));

        pnlBody.add(buildLeftPanel());
        pnlBody.add(buildRightPanel());
        add(pnlBody, BorderLayout.CENTER);
    }

    // ============================================================
    //  LEFT PANEL
    // ============================================================
    private JPanel buildLeftPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.BG_PAGE);

        content.add(buildBookerCard());
        content.add(Box.createVerticalStrut(14));
        pnlPaxSection = buildPaxCard();
        pnlPaxSection.setVisible(false);
        content.add(pnlPaxSection);

        JScrollPane sp = new JScrollPane(content);
        sp.setBackground(UITheme.BG_PAGE);
        sp.getViewport().setBackground(UITheme.BG_PAGE);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BG_PAGE);
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    // --- Card người đặt vé ---
    private JPanel buildBookerCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));

        // Title row
        JPanel pnlTop = new JPanel(new BorderLayout(0, 3));
        pnlTop.setOpaque(false);
        JLabel lblTitle = UITheme.makeSectionLabel("THÔNG TIN NGƯỜI ĐẶT VÉ");
        lblTitle.setForeground(UITheme.DANGER);
        lblBkStatus = new JLabel("Nhập SĐT để tìm khách hàng...");
        lblBkStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblBkStatus.setForeground(UITheme.TEXT_MID);
        pnlTop.add(lblTitle,   BorderLayout.NORTH);
        pnlTop.add(lblBkStatus, BorderLayout.SOUTH);
        card.add(pnlTop, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        txtBkSdt   = makePlainField("Nhập 10 số");   applyNumberOnly(txtBkSdt);
        txtBkHoTen = makePlainField("Họ và tên");    applyTitleCase(txtBkHoTen); applyTextOnly(txtBkHoTen);
        txtBkCccd  = makePlainField("Nhập 12 số");   applyNumberOnly(txtBkCccd);
        txtBkEmail = makePlainField("Tên tài khoản"); applyEmailOnly(txtBkEmail);

        lblCccdError = makeErrorLabel();

        addFormRow(form, "Số điện thoại", txtBkSdt);
        addFormRow(form, "Họ và Tên",     txtBkHoTen);
        addFormRow(form, "Số CCCD",       txtBkCccd);
        form.add(lblCccdError);
        addFormRowSuffix(form, "Email", txtBkEmail, "@gmail.com");
        card.add(form, BorderLayout.CENTER);

        // Buttons
        btnConfirmBooker = UITheme.makePrimaryBtn("XÁC NHẬN NGƯỜI ĐẶT");
        btnConfirmBooker.addActionListener(e -> confirmBooker());

        btnEditBooker = UITheme.makeBtn("SỬA THÔNG TIN", UITheme.TEXT_MID);
        btnEditBooker.setVisible(false);
        btnEditBooker.addActionListener(e -> unlockBooker());

        JPanel pnlBtn = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.setBorder(new EmptyBorder(12, 0, 0, 0));
        pnlBtn.add(btnConfirmBooker);
        pnlBtn.add(btnEditBooker);
        card.add(pnlBtn, BorderLayout.SOUTH);

        // Events
        txtBkSdt.getDocument().addDocumentListener(docListener(debounceBk));
        chainEnter(txtBkSdt, txtBkHoTen); chainEnter(txtBkHoTen, txtBkCccd);
        chainEnter(txtBkCccd, txtBkEmail); chainEnter(txtBkEmail, btnConfirmBooker);
        return card;
    }

    // --- Card hành khách ---
    private JPanel buildPaxCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));

        JPanel pnlTop = new JPanel(new BorderLayout(0, 3));
        pnlTop.setOpaque(false);
        lblCurrentTicket = UITheme.makeSectionLabel("HÀNH KHÁCH 1");
        lblCurrentTicket.setForeground(UITheme.PRIMARY);
        lblPaxStatus = new JLabel("Nhập SĐT hoặc điền tay");
        lblPaxStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblPaxStatus.setForeground(UITheme.TEXT_MID);
        pnlTop.add(lblCurrentTicket, BorderLayout.NORTH);
        pnlTop.add(lblPaxStatus,     BorderLayout.SOUTH);
        card.add(pnlTop, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        txtPaxSdt   = makePlainField("SĐT");          applyNumberOnly(txtPaxSdt);
        txtPaxHoTen = makePlainField("Họ và tên");    applyTitleCase(txtPaxHoTen); applyTextOnly(txtPaxHoTen);
        txtPaxCccd  = makePlainField("CCCD (12 số)"); applyNumberOnly(txtPaxCccd);
        txtPaxEmail = makePlainField("Email");         applyEmailOnly(txtPaxEmail);
        cbLoaiVe    = new JComboBox<>(new String[]{"Người lớn", "Trẻ em", "Sinh viên", "Người cao tuổi"});
        cbLoaiVe.setFont(UITheme.FONT_LABEL);

        lblPaxSdtError  = makeErrorLabel();
        lblPaxCccdError = makeErrorLabel();

        addFormRow(form, "Số điện thoại", txtPaxSdt);
        form.add(lblPaxSdtError);
        addFormRow(form, "Họ và Tên",     txtPaxHoTen);
        addFormRow(form, "Số CCCD",       txtPaxCccd);
        form.add(lblPaxCccdError);
        addFormRow(form, "Loại vé",       cbLoaiVe);
        addFormRowSuffix(form, "Email",   txtPaxEmail, "@gmail.com");
        card.add(form, BorderLayout.CENTER);

        btnCopyBooker = UITheme.makeBtn("Lấy thông tin người đặt", UITheme.TEXT_MID);
        btnConfirmPax = UITheme.makePrimaryBtn("XÁC NHẬN VÉ NÀY");
        btnCopyBooker.addActionListener(e -> copyBookerToPax());
        btnConfirmPax.addActionListener(e -> confirmPassenger());

        JPanel pnlBtn = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlBtn.setOpaque(false);
        pnlBtn.setBorder(new EmptyBorder(12, 0, 0, 0));
        pnlBtn.add(btnCopyBooker);
        pnlBtn.add(btnConfirmPax);
        card.add(pnlBtn, BorderLayout.SOUTH);

        txtPaxSdt.getDocument().addDocumentListener(docListener(debouncePax));
        return card;
    }

    // ============================================================
    //  RIGHT PANEL: Danh sách vé
    // ============================================================
    private JPanel buildRightPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(UITheme.BG_PAGE);

        JLabel lbl = UITheme.makeSectionLabel("DANH SÁCH VÉ ĐANG ĐẶT");
        lbl.setBorder(new EmptyBorder(4, 0, 8, 0));
        wrapper.add(lbl, BorderLayout.NORTH);

        pnlTicketList = new JPanel();
        pnlTicketList.setLayout(new BoxLayout(pnlTicketList, BoxLayout.Y_AXIS));
        pnlTicketList.setBackground(UITheme.BG_PAGE);

        JScrollPane sc = new JScrollPane(pnlTicketList);
        sc.setBorder(null);
        sc.setBackground(UITheme.BG_PAGE);
        sc.getViewport().setBackground(UITheme.BG_PAGE);
        sc.getVerticalScrollBar().setUnitIncrement(12);
        wrapper.add(sc, BorderLayout.CENTER);
        return wrapper;
    }

    // ============================================================
    //  FORM HELPERS
    // ============================================================
    private void addFormRow(JPanel parent, String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_STAT_LABEL);
        lbl.setForeground(UITheme.TEXT_MID);
        row.add(lbl,   BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        parent.add(row);
    }

    private void addFormRowSuffix(JPanel parent, String label, JTextField field, String suffix) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_STAT_LABEL);
        lbl.setForeground(UITheme.TEXT_MID);
        row.add(lbl, BorderLayout.NORTH);
        JPanel fg = new JPanel(new BorderLayout(5, 0));
        fg.setOpaque(false);
        fg.add(field, BorderLayout.CENTER);
        JLabel sfx = new JLabel(suffix);
        sfx.setFont(UITheme.FONT_LABEL);
        sfx.setForeground(UITheme.TEXT_MID);
        fg.add(sfx, BorderLayout.EAST);
        row.add(fg, BorderLayout.CENTER);
        parent.add(row);
    }

    private JTextField makePlainField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.FONT_LABEL);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(5, 8, 5, 8)));
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        return tf;
    }

    private JLabel makeErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbl.setForeground(UITheme.DANGER);
        return lbl;
    }

    // ============================================================
    //  BUSINESS LOGIC (giữ nguyên, chỉ đổi màu status)
    // ============================================================
    private void confirmBooker() {
        String hoTen = txtBkHoTen.getText().trim();
        String sdt   = txtBkSdt.getText().trim();
        String cccd  = txtBkCccd.getText().trim();
        String emailName = txtBkEmail.getText().trim();
        String fullEmail = emailName.isEmpty() ? "" : emailName + "@gmail.com";

        if (hoTen.isEmpty() || sdt.isEmpty() || cccd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Họ tên, SĐT và CCCD!");
            return;
        }
        if (!sdt.matches("^(03|05|07|08|09)\\d{8}$")) {
            JOptionPane.showMessageDialog(this, "SĐT phải 10 số và bắt đầu bằng 03, 05, 07, 08, 09!");
            txtBkSdt.requestFocus(); return;
        }
        if (!cccd.matches("^09\\d{10}$")) {
            JOptionPane.showMessageDialog(this, "CCCD phải 12 số và bắt đầu bằng 09!");
            txtBkCccd.requestFocus(); return;
        }

        if (confirmedBooker != null && !confirmedBooker.getMaKH().isEmpty()) {
            confirmedBooker.setHoTen(hoTen); confirmedBooker.setSdt(sdt);
            confirmedBooker.setCccd(cccd);   confirmedBooker.setEmail(fullEmail);
            kh_dao.updateKhachHang(confirmedBooker);
        } else {
            confirmedBooker = new KhachHang("", hoTen, sdt, cccd, fullEmail);
            kh_dao.addKhachHang(confirmedBooker);
        }

        setBookerFieldsEditable(false);
        btnConfirmBooker.setEnabled(false);
        btnEditBooker.setVisible(true);
        lblBkStatus.setText("✓ Đã xác nhận: " + hoTen);
        lblBkStatus.setForeground(UITheme.SUCCESS);
        pnlPaxSection.setVisible(true);
        if (!passengerCards.isEmpty()) selectCard(0);
        revalidate(); repaint();
    }

    private void unlockBooker() {
        setBookerFieldsEditable(true);
        btnConfirmBooker.setEnabled(true);
        btnEditBooker.setVisible(false);
        pnlPaxSection.setVisible(false);
        txtBkSdt.requestFocusInWindow();
    }

    private void searchBookerFromDB() {
        String sdt = txtBkSdt.getText().trim();
        if (sdt.length() != 10) return;
        List<KhachHang> list = kh_dao.searchBySdt(sdt);
        if (list != null && !list.isEmpty()) {
            KhachHang kh = list.get(0);
            confirmedBooker = kh;
            txtBkHoTen.setText(kh.getHoTen());
            txtBkCccd.setText(kh.getCccd());
            txtBkEmail.setText((kh.getEmail() != null ? kh.getEmail() : "").replace("@gmail.com", ""));
            lblBkStatus.setText("Khách hàng cũ: " + kh.getHoTen());
            lblBkStatus.setForeground(UITheme.SUCCESS);
            btnEditBooker.setVisible(true);
        } else {
            lblBkStatus.setText("Khách hàng mới");
            lblBkStatus.setForeground(UITheme.PRIMARY);
            btnEditBooker.setVisible(false);
        }
    }

    private void searchPaxFromDB() {
        String sdt = txtPaxSdt.getText().trim();
        if (sdt.length() != 10) return;
        List<KhachHang> list = kh_dao.searchBySdt(sdt);
        if (list != null && !list.isEmpty()) {
            KhachHang kh = list.get(0);
            txtPaxHoTen.setText(kh.getHoTen());
            txtPaxCccd.setText(kh.getCccd());
            txtPaxEmail.setText((kh.getEmail() != null ? kh.getEmail() : "").replace("@gmail.com", ""));
        }
    }

    private void confirmPassenger() {
        if (currentIdx >= passengerCards.size()) return;
        String hoTen = txtPaxHoTen.getText().trim();
        String sdt   = txtPaxSdt.getText().trim();
        String cccd  = txtPaxCccd.getText().trim();
        String email = txtPaxEmail.getText().trim();
        if (!email.isEmpty()) email += "@gmail.com";

        lblPaxSdtError.setText(" "); lblPaxCccdError.setText(" ");
        boolean hasError = false;
        if (hoTen.isEmpty()) hasError = true;
        if (sdt.isEmpty())   { lblPaxSdtError.setText("Không được để trống SĐT"); hasError = true; }
        if (cccd.isEmpty())  { lblPaxCccdError.setText("Không được để trống CCCD"); hasError = true; }
        if (!sdt.isEmpty() && !sdt.matches("^(03|05|07|08|09)\\d{8}$"))
            { lblPaxSdtError.setText("SĐT phải 10 số (03,05,07,08,09)"); hasError = true; }
        if (!cccd.isEmpty() && !cccd.matches("^09\\d{10}$"))
            { lblPaxCccdError.setText("CCCD phải 12 số, bắt đầu 09"); hasError = true; }
        if (hasError) return;

        String maKH = savePassengerToDB(hoTen, sdt, cccd, email);
        mainTab.getSelectedSeatsData().get(currentIdx).put("maKH", maKH);
        passengerCards.get(currentIdx).setData(hoTen, sdt, cccd, cbLoaiVe.getSelectedItem().toString());

        if (currentIdx < passengerCards.size() - 1) {
            selectCard(currentIdx + 1);
        } else {
            lblCurrentTicket.setText("ĐÃ HOÀN TẤT NHẬP LIỆU");
            lblCurrentTicket.setForeground(UITheme.SUCCESS);
            btnConfirmPax.setEnabled(false);
        }
        mainTab.setNextButtonEnabled(isAllPassengersFilled());
    }

    private String savePassengerToDB(String hoTen, String sdt, String cccd, String email) {
        if (!sdt.isEmpty() && sdt.length() == 10) {
            List<KhachHang> existing = kh_dao.searchBySdt(sdt);
            if (existing != null && !existing.isEmpty()) {
                KhachHang kh = existing.get(0);
                kh.setHoTen(hoTen); kh.setCccd(cccd); kh.setEmail(email);
                kh_dao.updateKhachHang(kh);
                return kh.getMaKH();
            }
        }
        KhachHang kh = new KhachHang("", hoTen, sdt, cccd, email);
        kh_dao.addKhachHang(kh);
        return kh.getMaKH();
    }

    private void selectCard(int idx) {
        currentIdx = idx;
        lblCurrentTicket.setText("HÀNH KHÁCH " + (idx + 1));
        lblCurrentTicket.setForeground(UITheme.PRIMARY);
        for (int i = 0; i < passengerCards.size(); i++)
            passengerCards.get(i).setSelected(i == idx);
        clearPaxFields();
        btnCopyBooker.setVisible(idx == 0);
    }

    private void setBookerFieldsEditable(boolean e) {
        txtBkSdt.setEditable(e); txtBkHoTen.setEditable(e);
        txtBkCccd.setEditable(e); txtBkEmail.setEditable(e);
    }

    private void clearBookerFields() {
        txtBkSdt.setText(""); txtBkHoTen.setText("");
        txtBkCccd.setText(""); txtBkEmail.setText("");
        btnEditBooker.setVisible(false);
        btnConfirmBooker.setEnabled(true);
        setBookerFieldsEditable(true);
    }

    private void clearPaxFields() {
        txtPaxSdt.setText(""); txtPaxHoTen.setText("");
        txtPaxCccd.setText(""); txtPaxEmail.setText("");
        lblPaxSdtError.setText(" "); lblPaxCccdError.setText(" ");
        cbLoaiVe.setSelectedIndex(0); btnConfirmPax.setEnabled(true);
    }

    private void copyBookerToPax() {
        if (confirmedBooker == null) return;
        txtPaxSdt.setText(confirmedBooker.getSdt());
        txtPaxHoTen.setText(confirmedBooker.getHoTen());
        txtPaxCccd.setText(confirmedBooker.getCccd());
        String mail = confirmedBooker.getEmail() != null ? confirmedBooker.getEmail() : "";
        txtPaxEmail.setText(mail.replace("@gmail.com", ""));
    }

    public void updateTimerDisplay(String text) {
        if (lblTimer != null) lblTimer.setText(text);
    }

    public void updatePassengerForms() {
        confirmedBooker = null; clearBookerFields();
        pnlPaxSection.setVisible(false);
        mainTab.setNextButtonEnabled(false);
        pnlTicketList.removeAll(); passengerCards.clear();
        List<Map<String, String>> seats = mainTab.getSelectedSeatsData();
        for (int i = 0; i < seats.size(); i++) {
            Map<String, String> sd = seats.get(i);
            String info = sd.get("tenTau") + " | " + sd.get("tenToa") + " - Ghế " + sd.get("tenCho");
            PassengerCard pc = new PassengerCard(i + 1, info);
            final int idx = i;
            pc.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (pnlPaxSection.isVisible()) selectCard(idx);
                }
            });
            passengerCards.add(pc);
            pnlTicketList.add(pc);
            pnlTicketList.add(Box.createVerticalStrut(10));
        }
        pnlTicketList.revalidate(); pnlTicketList.repaint();
    }

    public boolean isAllPassengersFilled() {
        for (PassengerCard pc : passengerCards) if (!pc.filled) return false;
        return true;
    }

    // ============================================================
    //  DocumentFilter helpers
    // ============================================================
    private void applyNumberOnly(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void replace(FilterBypass fb, int off, int len, String text, AttributeSet a) throws BadLocationException {
                if (text != null && text.matches("\\d*")) super.replace(fb, off, len, text, a);
            }
        });
    }

    private void applyTextOnly(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void replace(FilterBypass fb, int off, int len, String text, AttributeSet a) throws BadLocationException {
                if (text != null && text.matches("[a-zA-ZÀ-ỹ\\s]*")) super.replace(fb, off, len, text, a);
            }
        });
    }

    private void applyEmailOnly(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void replace(FilterBypass fb, int off, int len, String text, AttributeSet a) throws BadLocationException {
                if (text != null && text.matches("[a-zA-Z0-9]*")) super.replace(fb, off, len, text, a);
            }
        });
    }

    private void applyTitleCase(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void replace(FilterBypass fb, int offset, int len, String text, AttributeSet a) throws BadLocationException {
                if (text != null && !text.isEmpty()) {
                    StringBuilder sb = new StringBuilder(text);
                    try {
                        if (offset == 0 || fb.getDocument().getText(offset - 1, 1).equals(" "))
                            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
                    } catch (Exception ignored) {}
                    super.replace(fb, offset, len, sb.toString(), a);
                } else super.replace(fb, offset, len, text, a);
            }
        });
    }

    private static void chainEnter(JComponent src, JComponent dst) {
        src.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) dst.requestFocusInWindow();
            }
        });
    }

    private static DocumentListener docListener(Timer debounce) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { debounce.restart(); }
            public void removeUpdate(DocumentEvent e) { debounce.restart(); }
            public void changedUpdate(DocumentEvent e) { debounce.restart(); }
        };
    }

    // ============================================================
    //  PassengerCard (đồng bộ UITheme)
    // ============================================================
    private class PassengerCard extends JPanel {
        private final JLabel lblName, lblSeat, lblInfo;
        private final JPanel indicator;
        boolean filled = false;

        PassengerCard(int index, String seatInfo) {
            setLayout(new BorderLayout(10, 0));
            setBackground(UITheme.BG_CARD);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            indicator = new JPanel();
            indicator.setPreferredSize(new Dimension(5, 0));
            indicator.setBackground(UITheme.BORDER_COLOR);
            add(indicator, BorderLayout.WEST);

            JPanel mid = new JPanel();
            mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
            mid.setOpaque(false);
            mid.setBorder(new EmptyBorder(10, 0, 10, 10));

            lblName = new JLabel("Hành khách " + index + " (Trống)");
            lblName.setFont(UITheme.FONT_BOLD);
            lblName.setForeground(UITheme.TEXT_DARK);

            lblSeat = new JLabel(seatInfo);
            lblSeat.setFont(UITheme.FONT_STAT_LABEL);
            lblSeat.setForeground(UITheme.TEXT_MID);

            lblInfo = new JLabel("Chưa có thông tin");
            lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblInfo.setForeground(Color.GRAY);

            mid.add(lblName);
            mid.add(Box.createVerticalStrut(2));
            mid.add(lblSeat);
            mid.add(Box.createVerticalStrut(2));
            mid.add(lblInfo);
            add(mid, BorderLayout.CENTER);
            applyCardBorder(false, false);
        }

        void setSelected(boolean sel) {
            indicator.setBackground(sel ? UITheme.PRIMARY : (filled ? UITheme.SUCCESS : UITheme.BORDER_COLOR));
            setBackground(sel ? UITheme.PRIMARY_LIGHT : UITheme.BG_CARD);
            applyCardBorder(sel, filled);
        }

        void setData(String name, String sdt, String cccd, String type) {
            filled = true;
            lblName.setText(name.toUpperCase());
            lblName.setForeground(UITheme.PRIMARY);
            lblInfo.setText("Loại: " + type + " | SĐT: " + sdt + (cccd.isEmpty() ? "" : " | CCCD: " + cccd));
            lblInfo.setFont(UITheme.FONT_STAT_LABEL);
            lblInfo.setForeground(UITheme.TEXT_MID);
            indicator.setBackground(UITheme.SUCCESS);
        }

        private void applyCardBorder(boolean sel, boolean ok) {
            Color line = sel ? UITheme.PRIMARY : (ok ? UITheme.SUCCESS : UITheme.BORDER_COLOR);
            setBorder(BorderFactory.createLineBorder(line, sel ? 2 : 1, true));
        }
    }
}