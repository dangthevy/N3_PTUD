package com.gui.banve;

import com.dao.DAO_KhachHang;
import com.entities.KhachHang;
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
 */
public class Step3_NhapThongTinKH extends JPanel {

    private final TAB_BanVe mainTab;
    private final DAO_KhachHang kh_dao = new DAO_KhachHang();

    private JLabel lblTimer;

    // BOOKER
    private JTextField txtBkSdt, txtBkHoTen, txtBkCccd, txtBkEmail;
    private JLabel lblBkStatus;
    private JLabel lblBkSdtError, lblBkHoTenError, lblBkCccdError, lblBkEmailError;
    private JButton btnConfirmBooker;
    private KhachHang confirmedBooker = null;

    // PASSENGER
    private JLabel lblCurrentTicket, lblPaxStatus;
    private JTextField txtPaxSdt, txtPaxHoTen, txtPaxCccd, txtPaxEmail;
    private JComboBox<String> cbLoaiVe;
    private JButton btnConfirmPax, btnCopyBooker;
    private JPanel pnlPaxSection;
    private JLabel lblPaxSdtError, lblPaxHoTenError, lblPaxCccdError, lblPaxEmailError;

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

    // LEFT PANNEL
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

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setToolTipText("Nhấp đúp chuột để sửa thông tin");
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !txtBkSdt.isEditable()) {
                    unlockBooker();
                }
            }
        });

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
        txtBkHoTen = makePlainField("Họ và tên");    applyTextOnly(txtBkHoTen);
        txtBkCccd  = makePlainField("Nhập 12 số");   applyNumberOnly(txtBkCccd);
        txtBkEmail = makePlainField("Ví dụ: abc@gmail.com"); applyEmailOnly(txtBkEmail);

        lblBkSdtError   = makeErrorLabel();
        lblBkHoTenError = makeErrorLabel();
        lblBkCccdError  = makeErrorLabel();
        lblBkEmailError = makeErrorLabel();

        addFormRow(form, "Số điện thoại", txtBkSdt); form.add(lblBkSdtError);
        addFormRow(form, "Họ và Tên",     txtBkHoTen); form.add(lblBkHoTenError);
        addFormRow(form, "Số CCCD",       txtBkCccd); form.add(lblBkCccdError);
        addFormRow(form, "Email",         txtBkEmail); form.add(lblBkEmailError);

        card.add(form, BorderLayout.CENTER);

        // Buttons
        btnConfirmBooker = UITheme.makePrimaryBtn("XÁC NHẬN NGƯỜI ĐẶT");
        btnConfirmBooker.addActionListener(e -> confirmBooker());

        JPanel pnlBtn = new JPanel(new GridLayout(1, 1, 10, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.setBorder(new EmptyBorder(12, 0, 0, 0));
        pnlBtn.add(btnConfirmBooker);
        card.add(pnlBtn, BorderLayout.SOUTH);

        // Sự kiện Enter từng ô bên người đặt
        txtBkSdt.addActionListener(e -> {
            debounceBk.stop();
            if (validateBkSdt()) txtBkHoTen.requestFocus();
        });
        txtBkHoTen.addActionListener(e -> { if (validateBkHoTen()) txtBkCccd.requestFocus(); });
        txtBkCccd.addActionListener(e -> { if (validateBkCccd()) txtBkEmail.requestFocus(); });
        txtBkEmail.addActionListener(e -> { if (validateBkEmail()) btnConfirmBooker.doClick(); });

        txtBkSdt.getDocument().addDocumentListener(docListener(debounceBk));
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
        txtPaxHoTen = makePlainField("Họ và tên");    applyTextOnly(txtPaxHoTen);
        txtPaxCccd  = makePlainField("CCCD (12 số)"); applyNumberOnly(txtPaxCccd);
        txtPaxEmail = makePlainField("Ví dụ: abc@gmail.com"); applyEmailOnly(txtPaxEmail);

        cbLoaiVe    = new JComboBox<>(new String[]{"Người lớn", "Trẻ em", "Sinh viên", "Người cao tuổi"});
        cbLoaiVe.setFont(UITheme.FONT_LABEL);

        lblPaxSdtError   = makeErrorLabel();
        lblPaxHoTenError = makeErrorLabel();
        lblPaxCccdError  = makeErrorLabel();
        lblPaxEmailError = makeErrorLabel();

        addFormRow(form, "Số điện thoại", txtPaxSdt);   form.add(lblPaxSdtError);
        addFormRow(form, "Họ và Tên",     txtPaxHoTen); form.add(lblPaxHoTenError);
        addFormRow(form, "Số CCCD",       txtPaxCccd);  form.add(lblPaxCccdError);
        addFormRow(form, "Loại vé",       cbLoaiVe);    form.add(Box.createVerticalStrut(5));
        addFormRow(form, "Email",         txtPaxEmail); form.add(lblPaxEmailError);

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

        // ==============================================================
        // SỰ KIỆN ENTER TỪNG Ô BÊN HÀNH KHÁCH
        // ==============================================================
        txtPaxSdt.addActionListener(e -> {
            debounceBk.stop();
            if (validatePaxSdt()) txtPaxHoTen.requestFocus();
        });
        txtPaxHoTen.addActionListener(e -> { if (validatePaxHoTen()) txtPaxCccd.requestFocus(); });
        txtPaxCccd.addActionListener(e -> { if (validatePaxCccd()) cbLoaiVe.requestFocus(); });
        txtPaxEmail.addActionListener(e -> { if (validatePaxEmail()) btnConfirmPax.doClick(); });

        txtPaxSdt.getDocument().addDocumentListener(docListener(debouncePax));
        return card;
    }

    // Right panel: DS bán vé
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

    //  FORM HELPERS
    private void addFormRow(JPanel parent, String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 2, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_STAT_LABEL);
        lbl.setForeground(UITheme.TEXT_MID);
        row.add(lbl,   BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
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
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        return lbl;
    }

    private void clearBookerErrors() {
        lblBkSdtError.setText(" ");
        lblBkHoTenError.setText(" ");
        lblBkCccdError.setText(" ");
        lblBkEmailError.setText(" ");
    }

    private void clearPaxErrors() {
        lblPaxSdtError.setText(" ");
        lblPaxHoTenError.setText(" ");
        lblPaxCccdError.setText(" ");
        lblPaxEmailError.setText(" ");
    }

    // CỤM 4 HÀM BỊ THIẾU TỪ LẦN TRƯỚC ĐƯỢC CHÈN LẠI VÀO ĐÂY
    private void setBookerFieldsEditable(boolean e) {
        txtBkSdt.setEditable(e);
        txtBkHoTen.setEditable(e);
        txtBkCccd.setEditable(e);
        txtBkEmail.setEditable(e);
    }

    private void clearBookerFields() {
        txtBkSdt.setText("");
        txtBkHoTen.setText("");
        txtBkCccd.setText("");
        txtBkEmail.setText("");
        btnConfirmBooker.setVisible(true);
        setBookerFieldsEditable(true);
        clearBookerErrors();
    }

    private void setPaxFieldsEditable(boolean e) {
        txtPaxSdt.setEditable(e);
        txtPaxHoTen.setEditable(e);
        txtPaxCccd.setEditable(e);
        txtPaxEmail.setEditable(e);
        cbLoaiVe.setEnabled(e);
    }

    private void clearPaxFields() {
        txtPaxSdt.setText("");
        txtPaxHoTen.setText("");
        txtPaxCccd.setText("");
        txtPaxEmail.setText("");
        cbLoaiVe.setSelectedIndex(0);
        clearPaxErrors();
    }

    private void copyBookerToPax() {
        if (confirmedBooker == null) return;
        txtPaxSdt.setText(confirmedBooker.getSdt());
        txtPaxHoTen.setText(confirmedBooker.getHoTen());
        txtPaxCccd.setText(confirmedBooker.getCccd());
        txtPaxEmail.setText(confirmedBooker.getEmail() != null ? confirmedBooker.getEmail() : "");
        clearPaxErrors();
    }

    // ============================================================
    //  HÀM CHUẨN HÓA (VIẾT HOA CHỮ CÁI ĐẦU)
    // ============================================================
    private String toTitleCase(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        String[] words = text.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) {
                    sb.append(w.substring(1).toLowerCase());
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    // ============================================================
    //  HÀM VALIDATE (KIỂM TRA LỖI) CHO TỪNG Ô NGƯỜI ĐẶT VÉ
    // ============================================================
    private boolean validateBkSdt() {
        String sdt = txtBkSdt.getText().trim();
        if (sdt.isEmpty()) { lblBkSdtError.setText("Vui lòng nhập Số điện thoại"); return false; }
        if (!sdt.matches("^\\d{10}$")) { lblBkSdtError.setText("Số điện thoại phải bao gồm đúng 10 chữ số"); return false; }
        lblBkSdtError.setText(" "); return true;
    }

    private boolean validateBkHoTen() {
        String name = txtBkHoTen.getText().trim();
        if (name.isEmpty()) { lblBkHoTenError.setText("Vui lòng nhập Họ và Tên"); return false; }

        name = toTitleCase(name);
        txtBkHoTen.setText(name);

        lblBkHoTenError.setText(" "); return true;
    }

    private boolean validateBkCccd() {
        String cccd = txtBkCccd.getText().trim();
        if (cccd.isEmpty()) { lblBkCccdError.setText("Vui lòng nhập Số CCCD"); return false; }
        if (!cccd.matches("^\\d{12}$")) { lblBkCccdError.setText("Số CCCD phải bao gồm đúng 12 chữ số"); return false; }
        lblBkCccdError.setText(" "); return true;
    }

    private boolean validateBkEmail() {
        String email = txtBkEmail.getText().trim();
        if (email.isEmpty()) { lblBkEmailError.setText("Vui lòng nhập Email"); return false; }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            lblBkEmailError.setText("Email không hợp lệ! (Ví dụ: abc@gmail.com)"); return false;
        }
        lblBkEmailError.setText(" "); return true;
    }

    //  HÀM VALIDATE (KIỂM TRA LỖI) CHO TỪNG Ô HÀNH KHÁCH
    private boolean validatePaxSdt() {
        String sdt = txtPaxSdt.getText().trim();
        if (sdt.isEmpty()) { lblPaxSdtError.setText("Vui lòng nhập Số điện thoại"); return false; }
        if (!sdt.matches("^\\d{10}$")) { lblPaxSdtError.setText("Số điện thoại phải bao gồm đúng 10 chữ số"); return false; }
        lblPaxSdtError.setText(" "); return true;
    }

    private boolean validatePaxHoTen() {
        String name = txtPaxHoTen.getText().trim();
        if (name.isEmpty()) { lblPaxHoTenError.setText("Vui lòng nhập Họ và Tên"); return false; }

        name = toTitleCase(name);
        txtPaxHoTen.setText(name);

        lblPaxHoTenError.setText(" "); return true;
    }

    private boolean validatePaxCccd() {
        String cccd = txtPaxCccd.getText().trim();
        if (cccd.isEmpty()) { lblPaxCccdError.setText("Vui lòng nhập Số CCCD"); return false; }
        if (!cccd.matches("^\\d{12}$")) { lblPaxCccdError.setText("Số CCCD phải bao gồm đúng 12 chữ số"); return false; }
        lblPaxCccdError.setText(" "); return true;
    }

    private boolean validatePaxEmail() {
        String email = txtPaxEmail.getText().trim();
        if (email.isEmpty()) { lblPaxEmailError.setText("Vui lòng nhập Email"); return false; }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            lblPaxEmailError.setText("Email hành khách không hợp lệ! (Ví dụ: abc@gmail.com)"); return false;
        }
        lblPaxEmailError.setText(" "); return true;
    }


    // ============================================================
    //  BUSINESS LOGIC CHÍNH
    // ============================================================
    private void confirmBooker() {
        boolean ok = true;
        if (!validateBkSdt()) ok = false;
        if (!validateBkHoTen()) ok = false;
        if (!validateBkCccd()) ok = false;
        if (!validateBkEmail()) ok = false;
        if (!ok) return;

        String hoTen = txtBkHoTen.getText().trim();
        String sdt   = txtBkSdt.getText().trim();
        String cccd  = txtBkCccd.getText().trim();
        String email = txtBkEmail.getText().trim();

        if (confirmedBooker != null && !confirmedBooker.getMaKH().isEmpty()) {
            confirmedBooker.setHoTen(hoTen); confirmedBooker.setSdt(sdt);
            confirmedBooker.setCccd(cccd);   confirmedBooker.setEmail(email);
            kh_dao.updateKhachHang(confirmedBooker);
        } else {
            confirmedBooker = new KhachHang("", hoTen, sdt, cccd, email);
            kh_dao.addKhachHang(confirmedBooker);
        }

        // Lưu người đặt vé dùng chung cho Step4
        mainTab.setConfirmedBooker(confirmedBooker);

        setBookerFieldsEditable(false);
        btnConfirmBooker.setVisible(false);
        lblBkStatus.setText("✓ Đã xác nhận: " + hoTen + " (Nhấp đúp để sửa)");
        lblBkStatus.setForeground(UITheme.SUCCESS);
        pnlPaxSection.setVisible(true);
        if (!passengerCards.isEmpty()) selectCard(0);
        revalidate(); repaint();
    }

    private void unlockBooker() {
        setBookerFieldsEditable(true);
        btnConfirmBooker.setVisible(true);
        lblBkStatus.setText("Nhập SĐT để tìm khách hàng...");
        lblBkStatus.setForeground(UITheme.TEXT_MID);
        pnlPaxSection.setVisible(false);
        mainTab.setConfirmedBooker(null);
        txtBkSdt.requestFocusInWindow();
    }

    private void searchBookerFromDB() {
        String sdt = txtBkSdt.getText().trim();

        if (sdt.length() < 10) {
            confirmedBooker = null;
            txtBkHoTen.setText("");
            txtBkCccd.setText("");
            txtBkEmail.setText("");
            lblBkStatus.setText("Nhập SĐT để tìm khách hàng...");
            lblBkStatus.setForeground(UITheme.TEXT_MID);
            clearBookerErrors();
            return;
        }

        if (sdt.length() == 10) {
            List<KhachHang> list = kh_dao.searchBySdt(sdt);
            if (list != null && !list.isEmpty()) {
                KhachHang kh = list.get(0);
                confirmedBooker = kh;
                txtBkHoTen.setText(kh.getHoTen());
                txtBkCccd.setText(kh.getCccd());
                txtBkEmail.setText(kh.getEmail() != null ? kh.getEmail() : "");
                lblBkStatus.setText("Khách hàng cũ: " + kh.getHoTen());
                lblBkStatus.setForeground(UITheme.SUCCESS);
                clearBookerErrors();
            } else {
                lblBkStatus.setText("Khách hàng mới");
                lblBkStatus.setForeground(UITheme.PRIMARY);
                confirmedBooker = null;
                txtBkHoTen.setText("");
                txtBkCccd.setText("");
                txtBkEmail.setText("");
            }
        }
    }

    private void searchPaxFromDB() {
        String sdt = txtPaxSdt.getText().trim();

        if (sdt.length() < 10) {
            txtPaxHoTen.setText("");
            txtPaxCccd.setText("");
            txtPaxEmail.setText("");
            lblPaxStatus.setText("Nhập SĐT hoặc điền tay");
            clearPaxErrors();
            return;
        }

        if (sdt.length() == 10) {
            List<KhachHang> list = kh_dao.searchBySdt(sdt);
            if (list != null && !list.isEmpty()) {
                KhachHang kh = list.get(0);
                txtPaxHoTen.setText(kh.getHoTen());
                txtPaxCccd.setText(kh.getCccd());
                txtPaxEmail.setText(kh.getEmail() != null ? kh.getEmail() : "");
                lblPaxStatus.setText("Tìm thấy: " + kh.getHoTen());
                clearPaxErrors();
            } else {
                lblPaxStatus.setText("Hành khách mới");
                txtPaxHoTen.setText("");
                txtPaxCccd.setText("");
                txtPaxEmail.setText("");
            }
        }
    }

    private void confirmPassenger() {
        if (currentIdx >= passengerCards.size()) return;

        boolean ok = true;
        if (!validatePaxSdt()) ok = false;
        if (!validatePaxHoTen()) ok = false;
        if (!validatePaxCccd()) ok = false;
        if (!validatePaxEmail()) ok = false;
        if (!ok) return;

        String hoTen = txtPaxHoTen.getText().trim();
        String sdt   = txtPaxSdt.getText().trim();
        String cccd  = txtPaxCccd.getText().trim();
        String email = txtPaxEmail.getText().trim();

        // Lưu DB
        String maKH = savePassengerToDB(hoTen, sdt, cccd, email);

        // CẬP NHẬT TẤT CẢ DỮ LIỆU VÀO GIỎ HÀNG (MAP)
        Map<String, String> seatMap = mainTab.getSelectedSeatsData().get(currentIdx);
        seatMap.put("maKH", maKH);
        seatMap.put("hoTen", hoTen);
        seatMap.put("sdt", sdt);
        seatMap.put("cccd", cccd);
        seatMap.put("email", email);

        String tenLoaiVe = cbLoaiVe.getSelectedItem().toString();
        String maLoaiVe = tenLoaiVe.equals("Người lớn") ? "LV01" : (tenLoaiVe.equals("Trẻ em") ? "LV02" : "LV03");
        seatMap.put("loaiVe", tenLoaiVe);
        seatMap.put("maLoaiVe", maLoaiVe);

        // Cập nhật thẻ hiển thị (kèm giá vé từ Step2)
        passengerCards.get(currentIdx).setData(hoTen, sdt, cccd, tenLoaiVe, seatMap.get("giaVe"));

        // Khóa form hiện tại lại
        setPaxFieldsEditable(false);
        btnConfirmPax.setVisible(false);

        // Chuyển sang thẻ tiếp theo nếu chưa điền xong
        if (currentIdx < passengerCards.size() - 1) {
            selectCard(currentIdx + 1);
        } else {
            lblCurrentTicket.setText("ĐÃ HOÀN TẤT NHẬP LIỆU");
            lblCurrentTicket.setForeground(UITheme.SUCCESS);
        }
        mainTab.setNextButtonEnabled(isAllPassengersFilled());
    }

    private String formatGiaVeForCard(String rawGiaVe) {
        if (rawGiaVe == null || rawGiaVe.trim().isEmpty()) {
            return "Chưa cập nhật";
        }
        try {
            long gia = Math.round(Double.parseDouble(rawGiaVe.trim()));
            return String.format("%,d đ", gia);
        } catch (Exception e) {
            return rawGiaVe;
        }
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
        clearPaxErrors();

        for (int i = 0; i < passengerCards.size(); i++) {
            passengerCards.get(i).setSelected(i == idx);
        }

        Map<String, String> seatMap = mainTab.getSelectedSeatsData().get(idx);
        if (seatMap.containsKey("hoTen")) {
            txtPaxHoTen.setText(seatMap.get("hoTen"));
            txtPaxSdt.setText(seatMap.get("sdt"));
            txtPaxCccd.setText(seatMap.get("cccd"));
            txtPaxEmail.setText(seatMap.get("email"));
            cbLoaiVe.setSelectedItem(seatMap.get("loaiVe"));

            setPaxFieldsEditable(false);
            btnConfirmPax.setVisible(false);
            btnCopyBooker.setVisible(false);
            lblPaxStatus.setText("Thông tin đã được lưu (Nhấp đúp vào thẻ để sửa)");
        } else {
            clearPaxFields();
            setPaxFieldsEditable(true);
            btnConfirmPax.setText("XÁC NHẬN VÉ NÀY");
            btnConfirmPax.setVisible(true);
            btnCopyBooker.setVisible(idx == 0);
            lblPaxStatus.setText("Nhập SĐT hoặc điền tay");
        }
    }

    public void updateTimerDisplay(String text) {
        if (lblTimer != null) lblTimer.setText(text);
    }

    public void updatePassengerForms() {
        confirmedBooker = null; clearBookerFields();
        mainTab.setConfirmedBooker(null);
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
                    if (pnlPaxSection.isVisible()) {
                        selectCard(idx);

                        if (e.getClickCount() == 2) {
                            setPaxFieldsEditable(true);
                            btnConfirmPax.setText("CẬP NHẬT VÉ NÀY");
                            btnConfirmPax.setVisible(true);
                            lblPaxStatus.setText("Đang chỉnh sửa thông tin hành khách");
                            if (idx == 0) btnCopyBooker.setVisible(true);
                        }
                    }
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
                // CHO PHÉP NHẬP CẢ CHỮ, SỐ, @, DẤU CHẤM VÀ DẤU GẠCH
                if (text != null && text.matches("[a-zA-Z0-9@._-]*")) super.replace(fb, off, len, text, a);
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

    //  PassengerCard (đồng bộ UITheme)
    private class PassengerCard extends JPanel {
        private final JLabel lblName, lblSeat, lblInfo;
        private final JPanel indicator;
        boolean filled = false;

        PassengerCard(int index, String seatInfo) {
            setLayout(new BorderLayout(10, 0));
            setBackground(UITheme.BG_CARD);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Nhấp 1 lần để xem, Nhấp đúp để sửa");

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

        void setData(String name, String sdt, String cccd, String type, String rawGiaVe) {
            filled = true;
            lblName.setText(name.toUpperCase());
            lblName.setForeground(UITheme.PRIMARY);
            String giaVeHienThi = formatGiaVeForCard(rawGiaVe);
            lblInfo.setText("Loại: " + type + " | Giá vé: " + giaVeHienThi + " | SĐT: " + sdt
                    + (cccd.isEmpty() ? "" : " | CCCD: " + cccd));
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

