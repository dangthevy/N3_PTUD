package com.gui;

import com.connectDB.ConnectDB;
import com.dao.DAO_NhanVien;
import com.entities.NhanVien;
import com.enums.ChucVu;
import com.enums.TrangThaiNhanVien;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class TAB_QLNhanVien extends JPanel {

    // ================= COLOR =================
    private static final Color BG_PAGE     = new Color(0xF4F7FB);
    private static final Color BG_CARD     = Color.WHITE;
    private static final Color ACCENT      = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR  = new Color(0x2270CC);
    private static final Color ACCENT_FOC  = new Color(0x4D9DE0);
    private static final Color TEXT_DARK   = new Color(0x1E2B3C);
    private static final Color TEXT_MID    = new Color(0x5A6A7D);
    private static final Color TEXT_LIGHT  = new Color(0xA0AEC0);
    private static final Color BORDER      = new Color(0xE2EAF4);
    private static final Color ROW_ALT     = new Color(0xF7FAFF);
    private static final Color ROW_SEL     = new Color(0xDDEEFF);
    private static final Color BTN2_BG     = new Color(0xF0F4FA);
    private static final Color BTN2_FG     = new Color(0x3A5A8C);
    private static final Color BTN_RED     = new Color(0xC0392B);
    private static final Color BTN_RED_HVR = new Color(0xE74C3C);
    private final Color COLOR_BORDER = new Color(226, 232, 240);
    private final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    // ================= FONT =================
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL = new Font("Segoe UI", Font.PLAIN, 13);

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER }
    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private static final String[] COLS = {
            "Mã","Tên", "SDT", "Email", "Chức vụ", "Ngày vào làm", "Trạng thái"
    };
    Connection conn;
    JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    DAO_NhanVien daoNhanVien;
    JDateChooser dateVaoLam;

    JLabel lblTotal = new JLabel("0");
    JLabel lblActive = new JLabel("0");
    JLabel lblOff = new JLabel("0");
    JLabel lblStopped = new JLabel("0");

    public TAB_QLNhanVien() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildTable();

        conn = ConnectDB.getConnection();
        daoNhanVien = new DAO_NhanVien(conn);

        loadDataNhanVien();

        // NORTH: header + stats + filter xếp dọc
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        topPanel.add(buildHeader());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildStatsBar());      // ← thanh stats riêng
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildFilterCard());

        add(topPanel,        BorderLayout.NORTH);
        add(buildMainCard(), BorderLayout.CENTER);
    }

    // ================= HEADER =================
    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        JLabel lbl = new JLabel("QUẢN LÝ NHÂN VIÊN");
        lbl.setFont(F_TITLE);
        lbl.setForeground(ACCENT);
        pnl.add(lbl, BorderLayout.WEST);
        return pnl;
    }

    // ================= STATS BAR (tách riêng) =================
    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 12, 0));
        bar.setOpaque(false);
        bar.add(createStatCard("TỔNG NHÂN VIÊN",  this.lblTotal,   ACCENT));
        bar.add(createStatCard("ĐANG HOẠT ĐỘNG",  this.lblActive,  new Color(34, 197, 94)));
        bar.add(createStatCard("ĐANG NGHỈ PHÉP",  this.lblOff,     new Color(245, 158, 11)));
        bar.add(createStatCard("ĐÃ NGHỈ",         this.lblStopped, new Color(239, 68, 68)));
        return bar;
    }

    // ================= FILTER =================
    private JPanel buildFilterCard() {
        JPanel card = buildCard(new FlowLayout(FlowLayout.LEFT, 12, 12));

        txtSearch = makeField("Tên / SĐT / Email...");

        // ── ComboBox Chức vụ với option "Tất cả" ──
        ChucVu[] chucVuValues = ChucVu.values();
        Object[] chucVuItems = new Object[chucVuValues.length + 1];
        chucVuItems[0] = "Tất cả";
        System.arraycopy(chucVuValues, 0, chucVuItems, 1, chucVuValues.length);
        JComboBox<Object> cbChucVu = new JComboBox<>(chucVuItems);
        cbChucVu.setPreferredSize(new Dimension(140, 34));
        cbChucVu.setFont(F_CELL);
        cbChucVu.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String text = (value instanceof ChucVu)
                    ? ((ChucVu) value).getLabel()
                    : value.toString(); // "Tất cả"
            JLabel lbl = new JLabel(text);
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
            if (isSelected) lbl.setBackground(ROW_SEL);
            return lbl;
        });

        // ── ComboBox Trạng thái với option "Tất cả" ──
        TrangThaiNhanVien[] ttValues = TrangThaiNhanVien.values();
        Object[] ttItems = new Object[ttValues.length + 1];
        ttItems[0] = "Tất cả";
        System.arraycopy(ttValues, 0, ttItems, 1, ttValues.length);
        JComboBox<Object> cbTrangThai = new JComboBox<>(ttItems);
        cbTrangThai.setPreferredSize(new Dimension(150, 34));
        cbTrangThai.setFont(F_CELL);
        cbTrangThai.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String text = (value instanceof TrangThaiNhanVien)
                    ? ((TrangThaiNhanVien) value).getLabel()
                    : value.toString(); // "Tất cả"
            JLabel lbl = new JLabel(text);
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
            if (isSelected) lbl.setBackground(ROW_SEL);
            return lbl;
        });

        JButton btnSearch = makeBtn("Tìm kiếm", BtnStyle.PRIMARY);
        JButton btnReset  = makeBtn("Làm mới",  BtnStyle.SECONDARY);

        card.add(makeLabel("Tìm:"));       card.add(txtSearch);
        card.add(makeLabel("Chức vụ:"));   card.add(cbChucVu);
        card.add(makeLabel("Trạng thái:")); card.add(cbTrangThai);
        card.add(btnSearch);
        card.add(btnReset);

        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();

            // null = "Tất cả" (không lọc)
            ChucVu chucVu = (cbChucVu.getSelectedItem() instanceof ChucVu)
                    ? (ChucVu) cbChucVu.getSelectedItem()
                    : null;
            TrangThaiNhanVien trangThai = (cbTrangThai.getSelectedItem() instanceof TrangThaiNhanVien)
                    ? (TrangThaiNhanVien) cbTrangThai.getSelectedItem()
                    : null;

            searchNhanVien(keyword, chucVu, trangThai);
        });

        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbChucVu.setSelectedIndex(0);
            cbTrangThai.setSelectedIndex(0);
            loadDataNhanVien();
        });

        return card;
    }

    // ================= MAIN =================
    private JPanel buildMainCard() {
        JPanel card = buildCard(new BorderLayout());
        card.add(buildActionBar(),  BorderLayout.NORTH);
        card.add(buildTableBody(),  BorderLayout.CENTER);
        return card;
    }

    // ================= ACTION BAR (chỉ còn label + nút Thêm) =================
    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        JLabel lbl = new JLabel("Danh sách nhân viên");
        lbl.setFont(F_LABEL);
        lbl.setForeground(TEXT_DARK);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton btnAdd = makeBtn("+ Thêm", BtnStyle.PRIMARY);
        btnAdd.addActionListener(e -> openDialog(null));
        right.add(btnAdd);

        bar.add(lbl,   BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ================= TÌM KIẾM KẾT HỢP 3 FIELD =================
    private void searchNhanVien(String keyword, ChucVu chucVu, TrangThaiNhanVien trangThai) {
        List<NhanVien> result = daoNhanVien.searchNhanVien(keyword, chucVu, trangThai);
        tableModel.setRowCount(0);
        for (NhanVien nv : result) addNhanVienToTable(nv);
    }
    // =================TABLE=================

    /** Phần thân bảng: separator + scroll + empty state */
    private JPanel buildTableBody() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setPreferredSize(new Dimension(0, 360));
        styleScrollBar(scroll.getVerticalScrollBar());
        styleScrollBar(scroll.getHorizontalScrollBar());

        JLabel lblEmpty = new JLabel(
                "Chưa có dữ liệu – nhấn \"+ Thêm mới\" hoặc tìm kiếm",
                SwingConstants.CENTER);
        lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblEmpty.setForeground(TEXT_LIGHT);
        lblEmpty.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(sep,      BorderLayout.NORTH);
        body.add(scroll,   BorderLayout.CENTER);
        body.add(lblEmpty, BorderLayout.SOUTH);
        return body;
    }

    // ================= CRUD =================
    private void openDialog(NhanVien nv) {
        boolean isEdit = (nv != null);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(isEdit ? "Cập nhật nhân viên" : "Thêm nhân viên");
        dialog.getContentPane().setBackground(BG_CARD);

        // ===== FIELDS =====
        JTextField txtName     = makeField("Tên");
        JTextField txtSDT      = makeField("SĐT");
        JTextField txtEmail    = makeField("Email");
        JTextField txtTaiKhoan = makeField("Tài khoản");

        // [SỬA] Dùng JPasswordField thay vì JTextField để che mật khẩu
        JPasswordField txtPassword = makePasswordField();

        // [THÊM MỚI] Nút toggle hiện/ẩn mật khẩu
        JButton btnTogglePwd = makeTogglePasswordBtn(txtPassword);

        // [THÊM MỚI] Wrapper gộp JPasswordField + nút toggle vào cùng 1 row
        JPanel pwdWrapper = new JPanel(new BorderLayout(0, 0));
        pwdWrapper.setOpaque(false);
        pwdWrapper.add(txtPassword, BorderLayout.CENTER);
        pwdWrapper.add(btnTogglePwd, BorderLayout.EAST);

        JComboBox<ChucVu> cbChucVu = new JComboBox<>(ChucVu.getWithoutAdmin());
        cbChucVu.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.getLabel());
            lbl.setOpaque(true);
            if (isSelected) lbl.setBackground(ROW_SEL);
            return lbl;
        });

        JComboBox<TrangThaiNhanVien> cbStatus = new JComboBox<>(TrangThaiNhanVien.values());
        cbStatus.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.getLabel());
            lbl.setOpaque(true);
            if (isSelected) lbl.setBackground(ROW_SEL);
            return lbl;
        });

        dateVaoLam = new JDateChooser();
        dateVaoLam.setDateFormatString("dd/MM/yyyy");
        dateVaoLam.setDate(new Date());

        // ===== ERROR LABELS =====
        JLabel errName     = makeErrorLabel();
        JLabel errSDT      = makeErrorLabel();
        JLabel errEmail    = makeErrorLabel();
        JLabel errTaiKhoan = makeErrorLabel();
        JLabel errPassword = makeErrorLabel();

        // ===== LOAD DATA nếu EDIT =====
        if (isEdit) {
            txtName.setText(nv.getTenNV());
            txtSDT.setText(nv.getSdt());
            txtEmail.setText(nv.getEmail());
            txtTaiKhoan.setText(nv.getTaiKhoan());
            // [SỬA] dùng setText của JPasswordField
            txtPassword.setText(nv.getMatKhau());
            cbChucVu.setSelectedItem(nv.getChucVu());
            cbStatus.setSelectedItem(nv.getTrangThai());
            dateVaoLam.setDate(nv.getNgayVaoLam());
        }

        // ===== BUTTON =====
        JButton btnSave = makeBtn(isEdit ? "Cập nhật" : "Lưu", BtnStyle.PRIMARY);
        btnSave.setEnabled(isEdit);

        // ===== VALIDATION – bind từng field, JPasswordField dùng bindPasswordValidation =====
        bindFieldValidation(txtName,     errName,     btnSave, () -> validateName(txtName,         errName), errName, errSDT, errEmail, errTaiKhoan, errPassword);
        bindFieldValidation(txtSDT,      errSDT,      btnSave, () -> validateSDT(txtSDT,           errSDT),  errName, errSDT, errEmail, errTaiKhoan, errPassword);
        bindFieldValidation(txtEmail,    errEmail,    btnSave, () -> validateEmail(txtEmail,        errEmail),errName, errSDT, errEmail, errTaiKhoan, errPassword);
        bindFieldValidation(txtTaiKhoan, errTaiKhoan, btnSave, () -> validateTaiKhoan(txtTaiKhoan, errTaiKhoan), errName, errSDT, errEmail, errTaiKhoan, errPassword);
        // [SỬA] bindPasswordValidation cho JPasswordField
        bindPasswordValidation(txtPassword, errPassword, btnSave, () -> validatePasswordField(txtPassword, errPassword), errName, errSDT, errEmail, errTaiKhoan, errPassword);

        // ===== FORM – GridBagLayout =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 6, 0, 6);

        int row = 0;
        row = addFormRow(form, gc, row, "Tên",       txtName,     errName);
        row = addFormRow(form, gc, row, "SĐT",       txtSDT,      errSDT);
        row = addFormRow(form, gc, row, "Email",     txtEmail,    errEmail);
        row = addFormRow(form, gc, row, "Tài khoản", txtTaiKhoan, errTaiKhoan);
        // [SỬA] truyền pwdWrapper (chứa cả field + toggle btn) vào form
        row = addFormRow(form, gc, row, "Mật khẩu",  pwdWrapper,  errPassword);

        row = addFormRowSimple(form, gc, row, "Chức vụ",      cbChucVu);
        row = addFormRowSimple(form, gc, row, "Trạng thái",   cbStatus);
        row = addFormRowSimple(form, gc, row, "Ngày vào làm", dateVaoLam);

        // ===== BOTTOM =====
        btnSave.addActionListener(e -> {
            boolean ok = validateName(txtName, errName)
                    & validateSDT(txtSDT, errSDT)
                    & validateEmail(txtEmail, errEmail)
                    & validateTaiKhoan(txtTaiKhoan, errTaiKhoan)
                    & validatePasswordField(txtPassword, errPassword);
            if (!ok) return;

            try {
                NhanVien newNV = getNhanVienFromForm(
                        txtName, txtSDT, txtEmail, txtTaiKhoan, txtPassword,
                        cbChucVu, cbStatus, dateVaoLam
                );

                if (isEdit) {
                    newNV.setMaNV(nv.getMaNV());
                    if (daoNhanVien.updateNhanVien(newNV)) {
                        loadDataNhanVien();
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại");
                    }
                } else {
                    if (daoNhanVien.insertNhanVien(newNV)) {
                        loadDataNhanVien();
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Thêm thất bại");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
        bottom.setBackground(BG_CARD);
        bottom.add(btnSave);

        // [THÊM MỚI] Nút xóa mềm – chỉ hiện khi edit
        if (isEdit) {
            JButton btnDelete = makeBtn("Xóa NV", BtnStyle.DANGER);
            btnDelete.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Xóa mềm nhân viên \"" + nv.getTenNV() + "\"?\n" +
                                "Nhân viên sẽ không còn xuất hiện trong hệ thống.",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (daoNhanVien.setAnNhanVien(nv.getMaNV())) {
                        dialog.dispose();
                        loadDataNhanVien();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Xóa thất bại!");
                    }
                }
            });
            bottom.add(btnDelete);
        }

        dialog.add(form,   BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(440, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER – thêm 1 hàng [label | input] + [err span 2 cột] vào form
    // ─────────────────────────────────────────────────────────────────────────
    private int addFormRow(JPanel form, GridBagConstraints gc,
                           int row, String labelText,
                           JComponent input, JLabel errLabel) {
        // Label
        gc.gridx = 0; gc.gridy = row;
        gc.weightx = 0; gc.gridwidth = 1;
        gc.insets = new Insets(8, 6, 0, 6);
        form.add(makeLabel(labelText), gc);

        // Input
        gc.gridx = 1; gc.gridy = row;
        gc.weightx = 1; gc.gridwidth = 1;
        form.add(input, gc);

        // Error – span 2 cột, ngay dưới input
        gc.gridx = 1; gc.gridy = row + 1;
        gc.weightx = 1; gc.gridwidth = 1;
        gc.insets = new Insets(1, 6, 0, 6);
        form.add(errLabel, gc);

        return row + 2;
    }

    /** Hàng không có err label (ComboBox, DateChooser) */
    private int addFormRowSimple(JPanel form, GridBagConstraints gc,
                                 int row, String labelText, JComponent input) {
        gc.gridx = 0; gc.gridy = row;
        gc.weightx = 0; gc.gridwidth = 1;
        gc.insets = new Insets(8, 6, 4, 6);
        form.add(makeLabel(labelText), gc);

        gc.gridx = 1; gc.gridy = row;
        gc.weightx = 1; gc.gridwidth = 1;
        form.add(input, gc);

        return row + 1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VALIDATION ĐỘC LẬP – mỗi hàm chỉ validate 1 field
    // ─────────────────────────────────────────────────────────────────────────

    // [SỬA] Regex tên: hỗ trợ tiếng Việt có dấu, mỗi từ viết hoa chữ đầu
    // Hợp lệ: "Nguyễn Văn An", "Nguyễn A", "Lê Thị Bích Ngọc"
    private boolean validateName(JTextField f, JLabel err) {
        String val = f.getText().trim();
        // Mỗi "từ" là 1 chuỗi ký tự Unicode Letter, viết hoa chữ đầu, có thể 1 ký tự
        // Cho phép chữ thường ở đầu nếu chỉ 1 ký tự (Nguyễn A hợp lệ)
        boolean valid = val.matches(
                "^\\p{Lu}[\\p{L}]*(?:\\s\\p{Lu}[\\p{L}]*)*$"
        );
        if (!valid) {
            err.setText("Tên phải dạng \"Nguyễn Văn An\", mỗi từ viết hoa chữ đầu");
            f.setBorder(new LineBorder(Color.RED, 1, true));
            return false;
        }
        err.setText(" "); f.setBorder(new LineBorder(BORDER, 1, true));
        return true;
    }

    private boolean validateSDT(JTextField f, JLabel err) {
        if (!f.getText().matches("0\\d{9}")) {
            err.setText("SĐT phải 10 số, bắt đầu bằng 0");
            f.setBorder(new LineBorder(Color.RED, 1, true));
            return false;
        }
        err.setText(" "); f.setBorder(new LineBorder(BORDER, 1, true));
        return true;
    }

    // [SỬA] Regex email: hỗ trợ multi-level domain như .edu.vn, .com.vn
    // Hợp lệ: abcd@gmail.com, abcd@iuh.edu.vn, abc.def@company.org
    private boolean validateEmail(JTextField f, JLabel err) {
        boolean valid = f.getText().matches(
                "^[a-zA-Z0-9][a-zA-Z0-9._%+-]*@[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+$"
        );
        if (!valid) {
            err.setText("Email không hợp lệ (vd: abc@gmail.com, abc@iuh.edu.vn)");
            f.setBorder(new LineBorder(Color.RED, 1, true));
            return false;
        }
        err.setText(" "); f.setBorder(new LineBorder(BORDER, 1, true));
        return true;
    }

    private boolean validateTaiKhoan(JTextField f, JLabel err) {
        if (f.getText().trim().length() < 4) {
            err.setText("Tài khoản phải ≥ 4 ký tự");
            f.setBorder(new LineBorder(Color.RED, 1, true));
            return false;
        }
        err.setText(" "); f.setBorder(new LineBorder(BORDER, 1, true));
        return true;
    }

    // [GIỮ] validatePassword cũ – dành cho JTextField (backward compat)
    private boolean validatePassword(JTextField f, JLabel err) {
        if (f.getText().trim().isEmpty()) {
            err.setText("Mật khẩu không được rỗng");
            f.setBorder(new LineBorder(Color.RED, 1, true));
            return false;
        }
        err.setText(" "); f.setBorder(new LineBorder(BORDER, 1, true));
        return true;
    }

    // [THÊM MỚI] validatePasswordField – dành riêng cho JPasswordField
    private boolean validatePasswordField(JPasswordField f, JLabel err) {
        String pwd = new String(f.getPassword());
        if (pwd.trim().isEmpty()) {
            err.setText("Mật khẩu không được rỗng");
            f.setBorder(new LineBorder(Color.RED, 1, true));
            return false;
        }
        err.setText(" "); f.setBorder(new LineBorder(BORDER, 1, true));
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BIND
    // ─────────────────────────────────────────────────────────────────────────
    private void bindFieldValidation(JTextField field, JLabel err,
                                     JButton btnSave, Runnable validateThis, JLabel... allErrs) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { onType(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { onType(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onType(); }
            private void onType() {
                validateThis.run();
                btnSave.setEnabled(isAllErrEmpty(allErrs));
            }
        });
    }

    // [THÊM MỚI] bind cho JPasswordField – tương tự nhưng nhận JPasswordField
    private void bindPasswordValidation(JPasswordField field, JLabel err,
                                        JButton btnSave, Runnable validateThis, JLabel... allErrs) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { onType(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { onType(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onType(); }
            private void onType() {
                validateThis.run();
                btnSave.setEnabled(isAllErrEmpty(allErrs));
            }
        });
    }

    /** Trả về true nếu tất cả errLabel đang không có nội dung lỗi */
    private boolean isAllErrEmpty(JLabel... labels) {
        for (JLabel l : labels)
            if (l.getText() != null && !l.getText().trim().isEmpty()) return false;
        return true;
    }

    private JLabel makeErrorLabel() {
        JLabel l = new JLabel(" ");
        l.setBackground(BG_CARD);
        l.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        l.setForeground(Color.RED);
        return l;
    }

    // [THÊM MỚI] Tạo JPasswordField style đồng nhất với makeField
    private JPasswordField makePasswordField() {
        JPasswordField pf = new JPasswordField(13);
        pf.setFont(F_CELL);
        pf.setBorder(new LineBorder(BORDER, 1, true));
        pf.setEchoChar('●');
        pf.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        return pf;
    }

    // [THÊM MỚI] Nút mắt để toggle hiện/ẩn password
    private JButton makeTogglePasswordBtn(JPasswordField pf) {
        JButton btn = new JButton("👁") {
            boolean showing = false;
            {
                setToolTipText("Hiện/ẩn mật khẩu");
                addActionListener(e -> {
                    showing = !showing;
                    pf.setEchoChar(showing ? (char) 0 : '●');
                    setText(showing ? "🙈" : "👁");
                });
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setPreferredSize(new Dimension(38, 0));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(BG_CARD);
        return btn;
    }

//    private void updateTableRow(NhanVien nv) {
//        for (int i = 0; i < tableModel.getRowCount(); i++) {
//            if (tableModel.getValueAt(i, 1).equals(nv.getMaNV())) {
//
//                tableModel.setValueAt(nv.getMaNV(), i, 0);
//                tableModel.setValueAt(nv.getTenNV(), i, 1);
//                tableModel.setValueAt(nv.getSdt(), i, 2);
//                tableModel.setValueAt(nv.getEmail(), i, 3);
//                tableModel.setValueAt(nv.getChucVu().getLabel(), i, 4);
//                tableModel.setValueAt(nv.getNgayVaoLam(), i, 5);
//                tableModel.setValueAt(nv.getTrangThai().getLabel(), i, 6);
//
//                break;
//            }
//        }
//    }

    // ================= BUTTON FUNCTIONS =================


    private void loadDataNhanVien() {
        tableModel.setRowCount(0);
        List<NhanVien> listNV = daoNhanVien.getAllNhanVien();
        int cntActive = 0;
        int cntOff = 0;
        int cntStopped = 0;

        for (NhanVien nv : listNV) {
            if(nv.getTrangThai().equals(TrangThaiNhanVien.HOATDONG)) {
                cntActive++;
            } else if(nv.getTrangThai().equals(TrangThaiNhanVien.NGHIPHEP)) {
                cntOff++;
            } else {
                cntStopped++;
            }
            addNhanVienToTable(nv);
        }
        lblTotal.setText(String.valueOf(cntActive + cntOff));
        lblActive.setText(String.valueOf(cntActive));
        lblOff.setText(String.valueOf(cntOff));
        lblStopped.setText(String.valueOf(cntStopped));
    }

    private void addNhanVienToTable(NhanVien nv){
        tableModel.addRow(new Object[]{
                nv.getMaNV(),
                nv.getTenNV(),
                nv.getSdt(),
                nv.getEmail(),
                nv.getChucVu().getLabel(),
                nv.getNgayVaoLam() != null ? new SimpleDateFormat(DATE_FORMAT).format(nv.getNgayVaoLam()) : "",
                nv.getTrangThai().getLabel()
        });
    }

    private NhanVien getNhanVienFromForm(
            JTextField txtName,
            JTextField txtSDT,
            JTextField txtEmail,
            JTextField txtTaiKhoan,
            JPasswordField txtPassword,   // [SỬA] JPasswordField thay JTextField
            JComboBox<ChucVu> cbChucVu,
            JComboBox<TrangThaiNhanVien> cbStatus,
            JDateChooser dateVaoLam
    ) {
        if (txtName.getText().isEmpty())
            throw new RuntimeException("Tên không được rỗng");

        NhanVien nv = new NhanVien();

        nv.setMaNV(null);
        nv.setTenNV(txtName.getText());
        nv.setSdt(txtSDT.getText());
        nv.setEmail(txtEmail.getText());
        nv.setTaiKhoan(txtTaiKhoan.getText());
        // [SỬA] Lấy password từ JPasswordField đúng cách (không dùng getText())
        nv.setMatKhau(new String(txtPassword.getPassword()));

        nv.setChucVu((ChucVu) cbChucVu.getSelectedItem());
        nv.setTrangThai((TrangThaiNhanVien) cbStatus.getSelectedItem());
        nv.setNgayVaoLam(dateVaoLam.getDate());

        return nv;
    }

    public Date getDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false); // bắt lỗi ngày sai (vd: 32/01)

            return sdf.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // ================= UI FUNCTIONS =================

    private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(new Color(255, 255, 255));
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(15, 20, 15, 20)));
        JLabel lblT = new JLabel(title);
        lblT.setForeground(COLOR_TEXT_MUTED);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblValue.setForeground(accent);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        p.add(lblT, BorderLayout.NORTH); p.add(lblValue, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCard(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(BG_CARD);
        p.setBorder(new ShadowBorder());
        return p;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(TEXT_MID);
        return l;
    }

    private JTextField makeField(String hint) {
        JTextField tf = new JTextField(13);
        tf.setFont(F_CELL);
        tf.setBorder(new LineBorder(BORDER, 1, true));
        return tf;
    }

    private JComboBox<String> makeComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(F_CELL);
        return cb;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // SỬA LẠI THÀNH:
                switch (style) {
                    case PRIMARY:
                        g2.setColor(getModel().isPressed() ? new Color(0x0F3F8C)
                                : getModel().isRollover() ? ACCENT_HVR : ACCENT);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        break;
                    case DANGER:
                        g2.setColor(getModel().isPressed() ? new Color(0x922B21)
                                : getModel().isRollover() ? BTN_RED_HVR : BTN_RED);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        break;
                    default:
                        // code cho default
                        break;
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_LABEL);
        btn.setForeground(style == BtnStyle.SECONDARY ? BTN2_FG : Color.WHITE);
        // Cách cũ cho Java 8:
        int width = 110;
        if (style == BtnStyle.PRIMARY) width = 130;
        else if (style == BtnStyle.DANGER) width = 130;

        btn.setPreferredSize(new Dimension(width, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                thumbColor = new Color(0xC0D4EE);
            }
        });
    }

    // ================= TABLE =================

    private JTable buildTable() {
        JTable t = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }

            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row))
                    c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        // ===== DOUBLE CLICK =====
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = t.getSelectedRow();
                    if (row >= 0) {
                        String maNV = tableModel.getValueAt(row, 0).toString();
                        NhanVien nv = daoNhanVien.getNhanVienByID(maNV);
                        openDialog(nv); // ✅ truyền object
                    }
                }
            }
        });

        t.setFont(F_CELL);
        t.setRowHeight(38);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(BORDER);
        t.setSelectionBackground(ROW_SEL);
        t.setSelectionForeground(TEXT_DARK);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFocusable(false);
        t.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = t.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        int[] widths = { 150, 100, 120, 120, 180, 120 };
        for (int i = 0; i < widths.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        applyRenderers(t);
        t.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiRenderer());
        return t;
    }

    /** Gán renderer padding trái đồng nhất cho tất cả cột */
    private void applyRenderers(JTable t) {
        DefaultTableCellRenderer cellR = new DefaultTableCellRenderer();
        cellR.setFont(F_CELL);
        cellR.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 6));
        for (int i = 0; i < COLS.length; i++)
            t.getColumnModel().getColumn(i).setCellRenderer(cellR);
    }

    private static class TrangThaiRenderer extends DefaultTableCellRenderer {

        private static final Color CLR_ACTIVE  = new Color(0x16A34A); // xanh lá
        private static final Color CLR_STOP    = new Color(0xDC2626); // đỏ
        private static final Color CLR_OFF     = new Color(0xD97706); // vàng cam

        private static final Color BG_ACTIVE   = new Color(0xDCFCE7);
        private static final Color BG_STOP     = new Color(0xFEE2E2);
        private static final Color BG_OFF      = new Color(0xFEF9C3);

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {

            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);

            String text = v != null ? v.toString() : "";

            // Badge: text canh giữa, bo tròn giả lập bằng padding + màu nền
            l.setHorizontalAlignment(CENTER);
            l.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

            if (!sel) {
                // Màu text + nền theo trạng thái
                if (text.equalsIgnoreCase(TrangThaiNhanVien.HOATDONG.getLabel())) {
                    l.setForeground(CLR_ACTIVE);
                    l.setBackground(BG_ACTIVE);
                } else if (text.equalsIgnoreCase(TrangThaiNhanVien.NGUNGHOATDONG.getLabel())) {
                    l.setForeground(CLR_STOP);
                    l.setBackground(BG_STOP);
                } else if (text.equalsIgnoreCase(TrangThaiNhanVien.NGHIPHEP.getLabel())) {
                    l.setForeground(CLR_OFF);
                    l.setBackground(BG_OFF);
                } else {
                    // fallback
                    l.setForeground(new Color(0x5A6A7D));
                    l.setBackground(new Color(0xF0F4FA));
                }
            }

            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            l.setOpaque(true);
            return l;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() { setHorizontalAlignment(LEFT); }

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setOpaque(true);
            l.setBackground(ACCENT);
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 6));
            return l;
        }
    }

    // ================= SHADOW =================
    private static class ShadowBorder extends AbstractBorder {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0xE2EAF4));
            g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
        }
    }
}