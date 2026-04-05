package com.gui;

import com.connectDB.ConnectDB;
import com.dao.DAO_NhanVien;
import com.entities.NhanVien;
import com.enums.ChucVu;
import com.enums.TrangThaiNhanVien;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    public TAB_QLNhanVien() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

     // Khởi tạo model và bảng trước
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildTable();

        conn = ConnectDB.getConnection();
        daoNhanVien = new DAO_NhanVien(conn);

        loadDataNhanVien();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        topPanel.add(buildHeader());
        topPanel.add(Box.createVerticalStrut(10)); // khoảng cách
        topPanel.add(buildFilterCard());

        add(topPanel, BorderLayout.NORTH);
        add(buildMainCard(), BorderLayout.CENTER); // ⬅️ đổi thành CENTER
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

    // ================= FILTER =================
    private JPanel buildFilterCard() {
        JPanel card = buildCard(new FlowLayout(FlowLayout.LEFT, 14, 14));

        txtSearch = makeField("Tên nhân viên...");
        JComboBox<ChucVu> cbChucVu = new JComboBox<>(ChucVu.values());
        cbChucVu.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.getLabel());
            return label;
        });

        JComboBox<TrangThaiNhanVien> cbTrangThai = new JComboBox<>(TrangThaiNhanVien.values());
        cbTrangThai.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.getLabel());
            return label;
        });

        JButton btnSearch = makeBtn("Tìm kiếm", BtnStyle.PRIMARY);
        JButton btnReset = makeBtn("Làm mới", BtnStyle.SECONDARY);

        card.add(makeLabel("Tên:"));
        card.add(txtSearch);
//        card.add(makeLabel("Vai trò:"));
//        card.add(cbChucVu);
//        card.add(makeLabel("Trạng thái:"));
//        card.add(cbTrangThai);
        card.add(btnSearch);
        card.add(btnReset);

        btnSearch.addActionListener(e -> findNhanVienByTenNV());
        btnReset.addActionListener(e -> loadDataNhanVien());

        return card;
    }

    // ================= MAIN =================
    private JPanel buildMainCard() {
        JPanel card = buildCard(new BorderLayout());
        card.add(buildActionBar(), BorderLayout.NORTH);
        card.add(buildTableBody(), BorderLayout.CENTER);
        return card;
    }

    // ================= ACTION =================
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
//        btnEdit.addActionListener(e -> editRow());
//        btnDelete.addActionListener(e -> deleteRow());

        right.add(btnAdd);

        bar.add(lbl, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }
    // ================= =================
    
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

        JPanel form = new JPanel(new GridLayout(9, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtName  = makeField("Tên");
        JTextField txtSDT   = makeField("SĐT");
        JTextField txtEmail = makeField("Email");
        JTextField txtTaiKhoan = makeField("Tài Khoản");
        JTextField txtPassword = makeField("Mật khẩu");

        JComboBox<ChucVu> cbChucVu = new JComboBox<>(ChucVu.values());
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
        dateVaoLam.setDate(new Date()); // Mặc định ngày hiện tại

        // ===== LOAD DATA nếu EDIT =====
        if (isEdit) {
            txtName.setText(nv.getTenNV());
            txtSDT.setText(nv.getSdt());
            txtEmail.setText(nv.getEmail());
            txtTaiKhoan.setText(nv.getTaiKhoan());
            txtPassword.setText(nv.getMatKhau());
            cbChucVu.setSelectedItem(nv.getChucVu());
            cbStatus.setSelectedItem(nv.getTrangThai());
            dateVaoLam.setDate(nv.getNgayVaoLam());
        }

        // ===== FORM =====
        form.add(new JLabel("Tên"));          form.add(txtName);
        form.add(new JLabel("SĐT"));          form.add(txtSDT);
        form.add(new JLabel("Email"));        form.add(txtEmail);
        form.add(new JLabel("Tài khoản"));        form.add(txtTaiKhoan);
        form.add(new JLabel("Mật khẩu"));        form.add(txtPassword);
        form.add(new JLabel("Chức vụ"));      form.add(cbChucVu);
        form.add(new JLabel("Trạng thái"));   form.add(cbStatus);
        form.add(new JLabel("Ngày vào làm")); form.add(dateVaoLam);

        JButton btnSave = makeBtn(isEdit ? "Cập nhật" : "Lưu", BtnStyle.PRIMARY);

        btnSave.addActionListener(e -> {
            try {
                NhanVien newNV = getNhanVienFromForm(
                        txtName, txtSDT, txtEmail, txtTaiKhoan, txtPassword,
                        cbChucVu, cbStatus, dateVaoLam
                );

                if (isEdit) {
                    newNV.setMaNV(nv.getMaNV()); // giữ nguyên ID

                    boolean ok = daoNhanVien.updateNhanVien(newNV);

                    if (ok) {
                        loadDataNhanVien();
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại");
                    }

                } else {
                    boolean ok = daoNhanVien.insertNhanVien(newNV);

                    if (ok) {
                        addNhanVienToTable(newNV);
                        dialog.dispose();
                    }
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        JPanel bottom = new JPanel();
        bottom.add(btnSave);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
            for (NhanVien nv : listNV) {
                addNhanVienToTable(nv);
            }

    }



    private void findNhanVienByTenNV(){
        String tenNV = txtSearch.getText();
        List<NhanVien> search = daoNhanVien.findNhanVienByTenNV(tenNV);
        tableModel.setRowCount(0);
        for (NhanVien nv : search) {
            addNhanVienToTable(nv);
        }
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
            JTextField txtPassword,
            JComboBox<ChucVu> cbChucVu,
            JComboBox<TrangThaiNhanVien> cbStatus,
            JDateChooser dateVaoLam
    ) {
        if (txtName.getText().isEmpty())
            throw new RuntimeException("Tên không được rỗng");

        NhanVien nv = new NhanVien();

        nv.setMaNV(null); // tự sinh mã
        nv.setTenNV(txtName.getText());
        nv.setSdt(txtSDT.getText());
        nv.setEmail(txtEmail.getText());
        nv.setTaiKhoan(txtTaiKhoan.getText());
        nv.setMatKhau(txtPassword.getText()); // default

        nv.setChucVu((ChucVu) cbChucVu.getSelectedItem());
        nv.setTrangThai((TrangThaiNhanVien) cbStatus.getSelectedItem());

        nv.setNgayVaoLam(dateVaoLam.getDate()); // lấy Date

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
        else if (style == BtnStyle.DANGER) width = 80;

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
                        NhanVien nv = daoNhanVien.getNhanVienById(maNV);
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