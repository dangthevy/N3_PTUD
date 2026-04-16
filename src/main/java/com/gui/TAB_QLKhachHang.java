package com.gui;

import com.dao.DAO_KhachHang;
import com.entities.KhachHang;
import com.entities.NhanVien;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class TAB_QLKhachHang extends JPanel {

    // ==================== COLORS & FONTS ====================
    private static final Color BG_PAGE      = new Color(0xF4F7FB);
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color ACCENT       = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR   = new Color(0x2270CC);
    private static final Color TEXT_DARK    = new Color(0x1E2B3C);
    private static final Color TEXT_MID     = new Color(0x5A6A7D);
    private static final Color BORDER       = new Color(0xE2EAF4);
    private static final Color ROW_ALT      = new Color(0xF7FAFF);
    private static final Color ROW_SEL      = new Color(0xDDEEFF);
    private static final Color BTN_GREEN    = new Color(0x1A7A4A);
    private static final Color BTN_GREEN_HVR= new Color(0x22A060);
    private static final Color BTN_RED      = new Color(0xC0392B);
    private static final Color BTN_RED_HVR  = new Color(0xE74C3C);
    private static final Color COLOR_BORDER     = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    private static final Font F_TITLE  = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_LABEL  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_DIALOG = new Font("Segoe UI", Font.PLAIN, 13);

    private static final String[] COLS = {"Mã KH", "Tên", "Email", "SĐT", "CCCD"};

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER, SUCCESS }

    // ==================== COMPONENTS ====================
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblTotal   = new JLabel("0");
    private JLabel lblCoEmail = new JLabel("0");
    private DAO_KhachHang kh_dao = new DAO_KhachHang();

    // ==================== CONSTRUCTOR ====================
    public TAB_QLKhachHang(NhanVien nv) {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildTable();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.add(buildHeader());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildStatsBar());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildFilterCard());

        add(topPanel,        BorderLayout.NORTH);
        add(buildMainCard(), BorderLayout.CENTER);

        loadData();
    }

    // ==================== DIALOG THÊM MỚI ====================
    private void openAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "THÊM KHÁCH HÀNG MỚI", true);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        // ── Các field nhập ──
        JTextField txtTen         = makeDialogField(22);
        JTextField txtSdt         = makeDialogField(22);
        JTextField txtCccd        = makeDialogField(22);
        JTextField txtEmailPrefix = makeDialogField(16);

        // ── Error labels (ẩn mặc định) ──
        JLabel errTen   = makeErrorLabel();
        JLabel errSdt   = makeErrorLabel();
        JLabel errCccd  = makeErrorLabel();
        JLabel errEmail = makeErrorLabel();

        // ── Email panel: prefix + "@gmail.com" ──
        JPanel emailRow = new JPanel(new BorderLayout(4, 0));
        emailRow.setOpaque(false);
        JLabel lblSuffix = new JLabel("@gmail.com");
        lblSuffix.setFont(F_DIALOG);
        lblSuffix.setForeground(TEXT_MID);
        emailRow.add(txtEmailPrefix, BorderLayout.CENTER);
        emailRow.add(lblSuffix,      BorderLayout.EAST);

        // ── Form panel ──
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_CARD);
        formPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridx = 0;

        // helper: thêm label tiêu đề
        int row = 0;
        // Tên
        g.gridy = row++; g.insets = new Insets(10, 0, 2, 0);
        formPanel.add(makeFieldLabel("Tên khách hàng *"), g);
        g.gridy = row++; g.insets = new Insets(0, 0, 0, 0);
        formPanel.add(txtTen, g);
        g.gridy = row++; g.insets = new Insets(1, 0, 0, 0);
        formPanel.add(errTen, g);

        // SĐT
        g.gridy = row++; g.insets = new Insets(10, 0, 2, 0);
        formPanel.add(makeFieldLabel("Số điện thoại *"), g);
        g.gridy = row++; g.insets = new Insets(0, 0, 0, 0);
        formPanel.add(txtSdt, g);
        g.gridy = row++; g.insets = new Insets(1, 0, 0, 0);
        formPanel.add(errSdt, g);

        // CCCD
        g.gridy = row++; g.insets = new Insets(10, 0, 2, 0);
        formPanel.add(makeFieldLabel("CCCD *"), g);
        g.gridy = row++; g.insets = new Insets(0, 0, 0, 0);
        formPanel.add(txtCccd, g);
        g.gridy = row++; g.insets = new Insets(1, 0, 0, 0);
        formPanel.add(errCccd, g);

        // Email
        g.gridy = row++; g.insets = new Insets(10, 0, 2, 0);
        formPanel.add(makeFieldLabel("Email *"), g);
        g.gridy = row++; g.insets = new Insets(0, 0, 0, 0);
        formPanel.add(emailRow, g);
        g.gridy = row++; g.insets = new Insets(1, 0, 0, 0);
        formPanel.add(errEmail, g);

        // ── Tab/Enter → focus tiếp theo ──
        // Thứ tự: txtTen → txtSdt → txtCccd → txtEmailPrefix → btnSave
        JButton btnSave   = makeBtn("+ Thêm mới", BtnStyle.PRIMARY);
        JButton btnCancel = makeBtn("Hủy",        BtnStyle.SECONDARY);

        JTextField[] order = {txtTen, txtSdt, txtCccd, txtEmailPrefix};
        for (int i = 0; i < order.length; i++) {
            final int next = i + 1;
            order[i].addActionListener(e -> {         // Enter
                if (next < order.length) order[next].requestFocus();
                else btnSave.doClick();
            });
            // Tab được Swing xử lý tự nhiên theo thứ tự add component,
            // nhưng ta override để chắc chắn:
            final JTextField cur  = order[i];
            final JTextField nxt  = (next < order.length) ? order[next] : null;
            cur.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {
                        e.consume();
                        if (nxt != null) nxt.requestFocus();
                        else btnSave.requestFocus();
                    }
                }
            });
        }

        // ── Xóa lỗi khi user gõ lại ──
        txtTen.getDocument().addDocumentListener(clearErr(errTen,   txtTen));
        txtSdt.getDocument().addDocumentListener(clearErr(errSdt,   txtSdt));
        txtCccd.getDocument().addDocumentListener(clearErr(errCccd, txtCccd));
        txtEmailPrefix.getDocument().addDocumentListener(clearErr(errEmail, txtEmailPrefix));

        // ── Logic validate & save ──
        Runnable doSave = () -> {
            String ten         = txtTen.getText().trim();
            String sdt         = txtSdt.getText().trim();
            String cccd        = txtCccd.getText().trim();
            String emailPrefix = txtEmailPrefix.getText().trim();
            boolean hasErr = false;

            // Reset tất cả lỗi
            clearFieldError(errTen,   txtTen);
            clearFieldError(errSdt,   txtSdt);
            clearFieldError(errCccd,  txtCccd);
            clearFieldError(errEmail, txtEmailPrefix);

            if (ten.isEmpty()) {
                setFieldError(errTen, txtTen, "Tên khách hàng không được để trống"); hasErr = true;
            }
            if (!sdt.matches("^(03|05|07|08|09)\\d{8}$")) {
                setFieldError(errSdt, txtSdt, "SĐT phải 10 số, bắt đầu 03/05/07/08/09"); hasErr = true;
            }
            if (!cccd.matches("^09\\d{10}$")) {
                setFieldError(errCccd, txtCccd, "CCCD phải đúng 12 số và bắt đầu bằng 09"); hasErr = true;
            }
            if (emailPrefix.isEmpty() || !emailPrefix.matches("^[a-zA-Z0-9]+$")) {
                setFieldError(errEmail, txtEmailPrefix, "Chỉ được chứa chữ cái và số, không dấu"); hasErr = true;
            }

            if (hasErr) {
                dialog.pack(); // co giãn lại để hiện error labels
                return;
            }

            KhachHang kh = new KhachHang(null, ten, sdt, cccd, emailPrefix + "@gmail.com");
            boolean ok = kh_dao.addKhachHang(kh);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Thêm khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();
            } else {
                setFieldError(errCccd, txtCccd, "Thêm thất bại! CCCD hoặc SĐT có thể đã tồn tại");
                dialog.pack();
            }
        };

        btnSave.addActionListener(e -> doSave.run());
        btnCancel.addActionListener(e -> dialog.dispose());

        // ── Button panel ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(new Color(0xF8FAFC));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel,  BorderLayout.SOUTH);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        // Focus vào field đầu tiên
        SwingUtilities.invokeLater(txtTen::requestFocus);
        dialog.setVisible(true);
    }

    // ── Helpers cho inline error ──
    private JLabel makeErrorLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(0xC0392B));
        l.setPreferredSize(new Dimension(100, 16));
        return l;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private void setFieldError(JLabel errLbl, JTextField field, String msg) {
        errLbl.setText("⚠ " + msg);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xC0392B), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private void clearFieldError(JLabel errLbl, JTextField field) {
        errLbl.setText(" ");
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private javax.swing.event.DocumentListener clearErr(JLabel errLbl, JTextField field) {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { clearFieldError(errLbl, field); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { clearFieldError(errLbl, field); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { clearFieldError(errLbl, field); }
        };
    }

    // ==================== DIALOG THÔNG TIN (DOUBLE CLICK) ====================
    private void openInfoDialog(int row) {
        String ma    = table.getValueAt(row, 0).toString();
        String ten   = table.getValueAt(row, 1).toString();
        String email = table.getValueAt(row, 2).toString();
        String sdt   = table.getValueAt(row, 3).toString();
        String cccd  = table.getValueAt(row, 4).toString();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "THÔNG TIN KHÁCH HÀNG", true);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        // --- Info Panel (chế độ xem) ---
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(BG_CARD);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 4, 5, 4);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;

        // Avatar / Icon vòng tròn giả
        JLabel avatar = new JLabel(String.valueOf(ten.charAt(0)).toUpperCase(), SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 28));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(true);
        avatar.setBackground(ACCENT);
        avatar.setPreferredSize(new Dimension(60, 60));
        avatar.setBorder(BorderFactory.createEmptyBorder());

        // Bọc avatar để bo tròn
        JPanel avatarWrapper = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        avatarWrapper.setOpaque(false);
        avatarWrapper.setPreferredSize(new Dimension(60, 60));
        JLabel avLbl = new JLabel(String.valueOf(ten.charAt(0)).toUpperCase(), SwingConstants.CENTER);
        avLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        avLbl.setForeground(Color.WHITE);
        avatarWrapper.add(avLbl);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 4, 16, 4);
        gbc.anchor = GridBagConstraints.CENTER;
        infoPanel.add(avatarWrapper, gbc);

        // Các trường thông tin
        String[][] infos = {
                {"Mã khách hàng", ma},
                {"Họ tên",        ten},
                {"Số điện thoại", sdt},
                {"CCCD",          cccd},
                {"Email",         email}
        };

        gbc.anchor = GridBagConstraints.WEST;
        JTextField[] editFields = new JTextField[infos.length];

        for (int i = 0; i < infos.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.gridwidth = 1;
            gbc.insets = new Insets(6, 4, 6, 16);
            gbc.weightx = 0;
            JLabel lbl = new JLabel(infos[i][0] + ":");
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(TEXT_MID);
            lbl.setPreferredSize(new Dimension(130, 20));
            infoPanel.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1; gbc.insets = new Insets(6, 4, 6, 4);
            JTextField tf = new JTextField(infos[i][1], 18);
            tf.setFont(F_DIALOG);
            tf.setForeground(TEXT_DARK);
            tf.setEditable(false);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
            tf.setBackground(new Color(0xF8FAFC));
            // Mã KH không cho sửa
            if (i == 0) tf.setBackground(new Color(0xEEF2F7));
            editFields[i] = tf;
            infoPanel.add(tf, gbc);
        }

        // Email prefix field (ẩn, dùng khi edit)
        JTextField txtEmailPrefix = new JTextField();

        // --- Button Panel (Sửa & Xóa, căn phải) ---
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(new Color(0xF8FAFC));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JPanel btnRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnRight.setOpaque(false);

        JButton btnEdit   = makeBtn("Sửa", BtnStyle.SUCCESS);
        JButton btnDelete = makeBtn("Xóa", BtnStyle.DANGER);

        // --- Sự kiện NÚT SỬA ---
        btnEdit.addActionListener(e -> {
            boolean isEditing = !editFields[1].isEditable(); // toggle

            if (!isEditing) {
                // Đang ở chế độ sửa → nhấn "Lưu"
                String newTen  = editFields[1].getText().trim();
                String newSdt  = editFields[2].getText().trim();
                String newCccd = editFields[3].getText().trim();
                String rawEmail = editFields[4].getText().trim();

                // Xử lý email: tách prefix nếu có @gmail.com
                String emailPrefix;
                if (rawEmail.contains("@")) {
                    emailPrefix = rawEmail.split("@")[0];
                } else {
                    emailPrefix = rawEmail;
                }

                // VALIDATION
                if (newTen.isEmpty()) {
                    showError(dialog, "Tên khách hàng không được để trống!"); return;
                }
                if (!newSdt.matches("^(03|05|07|08|09)\\d{8}$")) {
                    showError(dialog, "SĐT không hợp lệ!\nPhải là 10 số, bắt đầu 03/05/07/08/09."); return;
                }
                if (!newCccd.matches("^09\\d{10}$")) {
                    showError(dialog, "CCCD không hợp lệ!\nPhải đúng 12 số, bắt đầu bằng 09."); return;
                }
                if (emailPrefix.isEmpty() || !emailPrefix.matches("^[a-zA-Z0-9]+$")) {
                    showError(dialog, "Email không hợp lệ!\nChỉ chứa chữ cái và số, đuôi @gmail.com."); return;
                }

                String newEmail = emailPrefix + "@gmail.com";

                KhachHang kh = new KhachHang(ma, newTen, newSdt, newCccd, newEmail);
                boolean ok = kh_dao.updateKhachHang(kh);

                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadData();
                } else {
                    showError(dialog, "Cập nhật thất bại! Vui lòng thử lại.");
                }
            } else {
                // Chuyển sang chế độ edit
                for (int i = 1; i < editFields.length; i++) { // bỏ qua editFields[0] = mã KH
                    editFields[i].setEditable(true);
                    editFields[i].setBackground(Color.WHITE);
                    editFields[i].setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(ACCENT, 1, true),
                            BorderFactory.createEmptyBorder(4, 8, 4, 8)
                    ));
                }
                // Email: hiển thị chỉ prefix
                if (editFields[4].getText().contains("@")) {
                    editFields[4].setText(editFields[4].getText().split("@")[0]);
                }
                // Thêm hint cho email field
                editFields[4].setToolTipText("Nhập phần trước @gmail.com");

                btnEdit.setText("Lưu");
                btnDelete.setEnabled(false);
            }
        });

        // --- Sự kiện NÚT XÓA ---
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    dialog,
                    "<html>Bạn có chắc chắn muốn xóa khách hàng <b>" + ten + "</b>?<br>"
                            + "Khách hàng sẽ bị ẩn khỏi hệ thống.</html>",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = kh_dao.deleteKhachHang(ma);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Đã xóa khách hàng " + ma, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadData();
                } else {
                    showError(dialog, "Xóa thất bại! Vui lòng thử lại.");
                }
            }
        });

        btnRight.add(btnEdit);
        btnRight.add(btnDelete);
        btnPanel.add(btnRight, BorderLayout.EAST);

        dialog.add(infoPanel, BorderLayout.CENTER);
        dialog.add(btnPanel,  BorderLayout.SOUTH);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ==================== XÓA NHIỀU (CHỌ NHIỀU DÒNG) ====================
    private void performDeleteMultiple() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một khách hàng để xóa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa " + selectedRows.length + " khách hàng đã chọn?",
                "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            for (int rowIdx : selectedRows) {
                kh_dao.deleteKhachHang(table.getValueAt(rowIdx, 0).toString());
            }
            loadData();
        }
    }

    // ==================== BUILD TABLE ====================
    private JTable buildTable() {
        JTable t = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row))
                    c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        t.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        t.setFont(F_CELL);
        t.setRowHeight(38);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(BORDER);
        t.setSelectionBackground(ROW_SEL);
        t.setSelectionForeground(TEXT_DARK);
        t.setFocusable(false);
        t.setIntercellSpacing(new Dimension(0, 0));

        // Double click → dialog thông tin
        t.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = t.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        t.setRowSelectionInterval(row, row);
                        openInfoDialog(row);
                    }
                }
            }
        });

        JTableHeader header = t.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);
        return t;
    }

    // ==================== LAYOUT BUILDERS ====================
    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton btnAdd    = makeBtn("+ Thêm", BtnStyle.PRIMARY);
        JButton btnDelete = makeBtn("Xóa", BtnStyle.DANGER);

        btnAdd.addActionListener(e -> openAddDialog());
        btnDelete.addActionListener(e -> performDeleteMultiple());

        right.add(btnAdd);
        right.add(btnDelete);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        Vector<Vector<Object>> data = kh_dao.getAllKhachHang();
        int coEmail = 0;
        for (Vector<Object> row : data) {
            tableModel.addRow(row);
            if (row.size() > 2 && row.get(2) != null && !row.get(2).toString().trim().isEmpty()) coEmail++;
        }
        lblTotal.setText(String.valueOf(data.size()));
        lblCoEmail.setText(String.valueOf(coEmail));
    }

    private void search() {
        tableModel.setRowCount(0);
        Vector<Vector<Object>> data = kh_dao.searchKhachHang(txtSearch.getText().trim());
        for (Vector<Object> row : data) tableModel.addRow(row);
    }

    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        JLabel lbl = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lbl.setFont(F_TITLE);
        lbl.setForeground(ACCENT);
        pnl.add(lbl, BorderLayout.WEST);
        return pnl;
    }

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 2, 12, 0));
        bar.setOpaque(false);
        bar.add(createStatCard("TỔNG KHÁCH HÀNG", lblTotal,   ACCENT));
        bar.add(createStatCard("CÓ EMAIL",         lblCoEmail, new Color(34, 197, 94)));
        return bar;
    }

    private JPanel buildFilterCard() {
        JPanel card = buildCard(new FlowLayout(FlowLayout.LEFT, 12, 12));
        txtSearch = makeDialogField(18);
        txtSearch.setFont(F_CELL);
        JButton btnSearch  = makeBtn("Tìm",      BtnStyle.PRIMARY);
        JButton btnRefresh = makeBtn("Làm mới",  BtnStyle.SECONDARY);
        card.add(makeLabel("Tìm kiếm:"));
        card.add(txtSearch);
        card.add(btnSearch);
        card.add(btnRefresh);
        btnSearch.addActionListener(e -> search());
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadData(); });
        txtSearch.addActionListener(e -> search());
        return card;
    }

    private JPanel buildMainCard() {
        JPanel card = buildCard(new BorderLayout());
        card.add(buildActionBar(), BorderLayout.NORTH);
        card.add(buildTableBody(), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableBody() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setPreferredSize(new Dimension(0, 360));
        styleScrollBar(scroll.getVerticalScrollBar());
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(new JSeparator(), BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);
        return body;
    }

    // ==================== UTILITY METHODS ====================

    private void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(15, 20, 15, 20)
        ));
        JLabel lblT = new JLabel(title);
        lblT.setForeground(COLOR_TEXT_MUTED);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblValue.setForeground(accent);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        p.add(lblT,     BorderLayout.NORTH);
        p.add(lblValue, BorderLayout.CENTER);
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

    private JTextField makeDialogField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(F_DIALOG);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base;
                switch (style) {
                    case PRIMARY:   base = getModel().isPressed() ? ACCENT.darker()     : getModel().isRollover() ? ACCENT_HVR    : ACCENT;       break;
                    case DANGER:    base = getModel().isPressed() ? BTN_RED.darker()    : getModel().isRollover() ? BTN_RED_HVR   : BTN_RED;      break;
                    case SUCCESS:   base = getModel().isPressed() ? BTN_GREEN.darker()  : getModel().isRollover() ? BTN_GREEN_HVR : BTN_GREEN;    break;
                    default:        base = Color.WHITE; break;
                }
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (style == BtnStyle.SECONDARY) {
                    g2.setColor(BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_LABEL);
        btn.setForeground(style == BtnStyle.SECONDARY ? TEXT_DARK : Color.WHITE);
        btn.setPreferredSize(new Dimension(text.length() > 6 ? 140 : 110, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() { thumbColor = new Color(0xC0D4EE); }
        });
    }

    // ==================== INNER CLASSES ====================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setOpaque(true);
            l.setBackground(ACCENT);
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 6));
            return l;
        }
    }

    private static class ShadowBorder extends AbstractBorder {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(BORDER);
            g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
        }
    }
}