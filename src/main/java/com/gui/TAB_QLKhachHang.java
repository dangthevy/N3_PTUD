package com.gui;

import com.dao.DAO_KhachHang;
import com.entities.KhachHang;
import com.entities.NhanVien;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

// Apache POI cho Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.*;

public class TAB_QLKhachHang extends JPanel {

    // ==================== COLORS & FONTS ====================
    private static final Color BG_PAGE       = new Color(0xF4F7FB);
    private static final Color BG_CARD       = Color.WHITE;
    private static final Color ACCENT        = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR    = new Color(0x2270CC);
    private static final Color TEXT_DARK     = new Color(0x1E2B3C);
    private static final Color TEXT_MID      = new Color(0x5A6A7D);
    private static final Color BORDER        = new Color(0xE2EAF4);
    private static final Color ROW_ALT       = new Color(0xF7FAFF);
    private static final Color ROW_SEL       = new Color(0xDDEEFF);
    private static final Color BTN_GREEN     = new Color(0x1A7A4A);
    private static final Color BTN_GREEN_HVR = new Color(0x22A060);
    private static final Color BTN_RED       = new Color(0xC0392B);
    private static final Color BTN_RED_HVR   = new Color(0xE74C3C);
    private static final Color BTN_EXCEL     = new Color(0x217346);   // màu Excel xanh lá
    private static final Color BTN_EXCEL_HVR = new Color(0x2E9E5E);
    private static final Color COLOR_BORDER  = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    private static final java.awt.Font F_TITLE  = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22);
    private static final java.awt.Font F_LABEL  = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13);
    private static final java.awt.Font F_CELL   = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13);
    private static final java.awt.Font F_DIALOG = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13);

    private static final String[] COLS = {"Mã KH", "Tên", "Email", "SĐT", "CCCD"};

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER, SUCCESS, EXCEL }

    // ==================== COMPONENTS ====================
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblTotal   = new JLabel("0");
    private JLabel lblCoEmail = new JLabel("0");
    private DAO_KhachHang kh_dao = new DAO_KhachHang();

    // Giữ tham chiếu nhân viên để ghi vào Excel
    private NhanVien currentNV;

    // ==================== CONSTRUCTOR ====================
    public TAB_QLKhachHang(NhanVien nv) {
        this.currentNV = nv;
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildTable();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        topPanel.add(buildStatsBar());
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(buildHeader());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildFilterCard());

        add(topPanel, BorderLayout.NORTH);
        add(buildMainCard(), BorderLayout.CENTER);

        loadData();
    }

    // ==================== DIALOG THÊM MỚI ====================
    private void openAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "THÊM KHÁCH HÀNG MỚI", true);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JTextField txtTen         = makeDialogField(22);
        JTextField txtSdt         = makeDialogField(22);
        JTextField txtCccd        = makeDialogField(22);
        JTextField txtEmailPrefix = makeDialogField(16);

        txtTen.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c) && c != ' ' && c != KeyEvent.VK_BACK_SPACE) e.consume();
            }
        });
        txtSdt.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) { e.consume(); return; }
                if (txtSdt.getText().length() >= 10 && txtSdt.getSelectedText() == null) e.consume();
            }
        });
        txtCccd.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) { e.consume(); return; }
                if (txtCccd.getText().length() >= 12 && txtCccd.getSelectedText() == null) e.consume();
            }
        });

        JLabel errTen   = makeErrorLabel();
        JLabel errSdt   = makeErrorLabel();
        JLabel errCccd  = makeErrorLabel();
        JLabel errEmail = makeErrorLabel();

        txtTen.getDocument().addDocumentListener(clearErr(errTen, txtTen));
        txtSdt.getDocument().addDocumentListener(clearErr(errSdt, txtSdt));
        txtCccd.getDocument().addDocumentListener(clearErr(errCccd, txtCccd));
        txtEmailPrefix.getDocument().addDocumentListener(clearErr(errEmail, txtEmailPrefix));

        JPanel emailRow = new JPanel(new BorderLayout(4, 0));
        emailRow.setOpaque(false);
        emailRow.add(txtEmailPrefix, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_CARD);
        formPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; g.gridx = 0;

        int r = 0;
        g.gridy = r++; formPanel.add(makeFieldLabel("Tên khách hàng *"), g);
        g.gridy = r++; formPanel.add(txtTen, g);
        g.gridy = r++; formPanel.add(errTen, g);
        g.gridy = r++; formPanel.add(makeFieldLabel("Số điện thoại *"), g);
        g.gridy = r++; formPanel.add(txtSdt, g);
        g.gridy = r++; formPanel.add(errSdt, g);
        g.gridy = r++; formPanel.add(makeFieldLabel("CCCD *"), g);
        g.gridy = r++; formPanel.add(txtCccd, g);
        g.gridy = r++; formPanel.add(errCccd, g);
        g.gridy = r++; formPanel.add(makeFieldLabel("Email"), g);
        g.gridy = r++; formPanel.add(emailRow, g);
        g.gridy = r++;  formPanel.add(errEmail, g);

        JButton btnSave   = makeBtn("+ Thêm mới", BtnStyle.PRIMARY);
        JButton btnCancel = makeBtn("Hủy", BtnStyle.SECONDARY);

        txtTen.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) { txtTen.setText(capitalizeName(txtTen.getText())); }
        });
        txtTen.addActionListener(e -> { txtTen.setText(capitalizeName(txtTen.getText())); txtSdt.requestFocus(); });
        txtSdt.addActionListener(e -> txtCccd.requestFocus());
        txtCccd.addActionListener(e -> txtEmailPrefix.requestFocus());
        txtEmailPrefix.addActionListener(e -> btnSave.doClick());

        Runnable doSave = () -> {
            boolean hasError = false;
            String ten  = capitalizeName(txtTen.getText().trim());
            String sdt  = txtSdt.getText().trim();
            String cccd = txtCccd.getText().trim();
            String emailPrefix = txtEmailPrefix.getText().trim();

            if (ten.isEmpty()) {
                setFieldError(errTen, txtTen, "Tên không được để trống");
                txtTen.requestFocus(); hasError = true;
            }
            if (!hasError && !sdt.matches("^\\d{10}$")) {
                setFieldError(errSdt, txtSdt, "SĐT phải là 10 chữ số");
                txtSdt.requestFocus(); hasError = true;
            }
            if (!hasError && !cccd.matches("^\\d{12}$")) {
                setFieldError(errCccd, txtCccd, "CCCD phải là 12 chữ số");
                txtCccd.requestFocus(); hasError = true;
            }
            if (!hasError && !emailPrefix.isEmpty() && !emailPrefix.matches("^[a-zA-Z0-9._@+\\-]+$")) {
                setFieldError(errEmail, txtEmailPrefix, "Email không hợp lệ");
                txtEmailPrefix.requestFocus(); hasError = true;
            }
            if (hasError) return;

            String email = emailPrefix;
            if (!email.isEmpty() && kh_dao.isDuplicate(sdt, cccd, email, null)) {
                JOptionPane.showMessageDialog(dialog, "SĐT, CCCD hoặc Email đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (email.isEmpty() && kh_dao.isDuplicate(sdt, cccd, null, null)) {
                JOptionPane.showMessageDialog(dialog, "SĐT hoặc CCCD đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (kh_dao.addKhachHang(new KhachHang(null, ten, sdt, cccd, email))) {
                JOptionPane.showMessageDialog(dialog, "Thêm thành công!");
                dialog.dispose();
                loadData();
            }
        };

        btnSave.addActionListener(e -> doSave.run());
        btnCancel.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(new Color(0xF8FAFC));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setMinimumSize(dialog.getSize());
        dialog.setMaximumSize(dialog.getSize());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ==================== DIALOG THÔNG TIN (DOUBLE CLICK) ====================
    private void openInfoDialog(int row) {
        String ma        = table.getValueAt(row, 0).toString();
        String ten       = table.getValueAt(row, 1).toString();
        String emailFull = table.getValueAt(row, 2).toString();
        String sdt       = table.getValueAt(row, 3).toString();
        String cccd      = table.getValueAt(row, 4).toString();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "CHI TIẾT KHÁCH HÀNG", true);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JTextField txtTen   = makeDialogField(20); txtTen.setText(ten);
        JTextField txtSdt   = makeDialogField(20); txtSdt.setText(sdt);
        JTextField txtCccd  = makeDialogField(20); txtCccd.setText(cccd);
        JTextField txtEmail = makeDialogField(20);
        txtEmail.setText(emailFull);

        JLabel errTen  = makeErrorLabel();
        JLabel errSdt  = makeErrorLabel();
        JLabel errCccd = makeErrorLabel();
        JLabel errEmail= makeErrorLabel();

        JTextField[] fields = {txtTen, txtSdt, txtCccd, txtEmail};
        for (JTextField f : fields) { f.setEditable(false); f.setBackground(new Color(0xF8FAFC)); }

        txtTen.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar(); if (!Character.isLetter(c) && c != ' ') e.consume();
            }
        });
        txtSdt.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) { e.consume(); return; }
                if (txtSdt.getText().length() >= 10 && txtSdt.getSelectedText() == null) e.consume();
            }
        });
        txtCccd.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) { e.consume(); return; }
                if (txtCccd.getText().length() >= 12 && txtCccd.getSelectedText() == null) e.consume();
            }
        });

        txtTen.getDocument().addDocumentListener(clearErr(errTen, txtTen));
        txtSdt.getDocument().addDocumentListener(clearErr(errSdt, txtSdt));
        txtCccd.getDocument().addDocumentListener(clearErr(errCccd, txtCccd));
        txtEmail.getDocument().addDocumentListener(clearErr(errEmail, txtEmail));

        JPanel maKhPanel = new JPanel(new BorderLayout(6, 0));
        maKhPanel.setOpaque(false);
        JTextField txtMaKH = makeDialogField(20); txtMaKH.setText(ma); txtMaKH.setEditable(false);
        txtMaKH.setBackground(new Color(0xEEF4FF));
        txtMaKH.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        maKhPanel.add(txtMaKH, BorderLayout.CENTER);

        JPanel emailRow = new JPanel(new BorderLayout(4, 0));
        emailRow.setOpaque(false);
        JTextField txtEmailRef = txtEmail;
        emailRow.add(txtEmail, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_CARD);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; g.gridx = 0;

        int r = 0;
        g.gridy = r++; formPanel.add(makeFieldLabel("Mã khách hàng"), g);
        g.gridy = r++; formPanel.add(maKhPanel, g);
        g.gridy = r++; formPanel.add(makeFieldLabel("Họ và tên *"), g);
        g.gridy = r++; formPanel.add(txtTen, g);
        g.gridy = r++; formPanel.add(errTen, g);
        g.gridy = r++; formPanel.add(makeFieldLabel("Số điện thoại *"), g);
        g.gridy = r++; formPanel.add(txtSdt, g);
        g.gridy = r++; formPanel.add(errSdt, g);
        g.gridy = r++; formPanel.add(makeFieldLabel("CCCD *"), g);
        g.gridy = r++; formPanel.add(txtCccd, g);
        g.gridy = r++; formPanel.add(errCccd, g);
        g.gridy = r++; formPanel.add(makeFieldLabel("Email"), g);
        g.gridy = r++; formPanel.add(emailRow, g);
        g.gridy = r++;  formPanel.add(errEmail, g);

        JButton btnEdit   = makeBtn("Sửa", BtnStyle.SUCCESS);
        JButton btnDelete = makeBtn("Xóa", BtnStyle.DANGER);

        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                "Xác nhận xóa khách hàng " + ten + "?", "Cảnh báo",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                kh_dao.deleteKhachHang(ma);
                JOptionPane.showMessageDialog(dialog, "Đã xóa thành công!");
                dialog.dispose();
                loadData();
            }
        });

        final boolean[] editing = {false};
        btnEdit.addActionListener(e -> {
            if (!editing[0]) {
                for (JTextField f : fields) { f.setEditable(true); f.setBackground(Color.WHITE); }
                btnEdit.setText("💾 Lưu");
                editing[0] = true;
                txtTen.requestFocus();
            } else {
                txtTen.setText(capitalizeName(txtTen.getText()));
                String newTen  = txtTen.getText().trim();
                String newSdt  = txtSdt.getText().trim();
                String newCccd = txtCccd.getText().trim();
                String emailPrefix = txtEmail.getText().trim();
                boolean hasErr = false;
                if (newTen.isEmpty()) { setFieldError(errTen, txtTen, "Tên không được để trống"); txtTen.requestFocus(); hasErr = true; }
                if (!hasErr && !newSdt.matches("^\\d{10}$")) { setFieldError(errSdt, txtSdt, "SĐT phải là 10 chữ số"); txtSdt.requestFocus(); hasErr = true; }
                if (!hasErr && !newCccd.matches("^\\d{12}$")) { setFieldError(errCccd, txtCccd, "CCCD phải là 12 chữ số"); txtCccd.requestFocus(); hasErr = true; }
                if (!hasErr && !emailPrefix.isEmpty() && !emailPrefix.matches("^[a-zA-Z0-9._@+\\-]+$")) { setFieldError(errEmail, txtEmail, "Email không hợp lệ"); txtEmail.requestFocus(); hasErr = true; }
                if (hasErr) return;

                String newEmail = emailPrefix;
                if (kh_dao.isDuplicate(newSdt, newCccd, newEmail.isEmpty() ? null : newEmail, ma)) {
                    JOptionPane.showMessageDialog(dialog, "SĐT/CCCD/Email trùng với khách khác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (kh_dao.updateKhachHang(new KhachHang(ma, newTen, newSdt, newCccd, newEmail))) {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!");
                    dialog.dispose();
                    loadData();
                }
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btnPanel.setBackground(new Color(0xF8FAFC));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        btnPanel.add(btnDelete);
        btnPanel.add(btnEdit);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        Dimension fixed = dialog.getSize();
        dialog.setMinimumSize(fixed);
        dialog.setMaximumSize(fixed);
        dialog.setPreferredSize(fixed);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ==================== XÓA NHIỀU ====================
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

    // ==================== IN EXCEL ====================
    private void inDanhSachExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu danh sách khách hàng Excel");
        fc.setSelectedFile(new java.io.File("DanhSach_KhachHang_" +
            new SimpleDateFormat("ddMMyyyy").format(new Date()) + ".xlsx"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".xlsx")) path += ".xlsx";

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Danh sách khách hàng");

            // ---- Styles ----
            // Tiêu đề chính
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(new XSSFColor(new byte[]{0x1A, 0x5E, (byte)0xAB}, null));
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Header cột
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{0x2E, 0x75, (byte)0xB6}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Data style thường
            CellStyle dataStyle = wb.createCellStyle();
            Font dataFont = wb.createFont();
            dataFont.setFontHeightInPoints((short) 10);
            dataStyle.setFont(dataFont);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Data style zebra
            CellStyle dataStyleAlt = wb.createCellStyle();
            dataStyleAlt.cloneStyleFrom(dataStyle);
            dataStyleAlt.setFillForegroundColor(new XSSFColor(new byte[]{(byte)0xF0, (byte)0xF5, (byte)0xFF}, null));
            dataStyleAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Info style (người in, ngày in)
            CellStyle infoStyle = wb.createCellStyle();
            Font infoFont = wb.createFont();
            infoFont.setItalic(true);
            infoFont.setFontHeightInPoints((short) 9);
            infoStyle.setFont(infoFont);

            // ---- Row 0: Tiêu đề ----
            Row rowTitle = sheet.createRow(0);
            rowTitle.setHeightInPoints(28);
            Cell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("DANH SÁCH KHÁCH HÀNG - HỆ THỐNG BAN VÉ TÀU HỎA");
            cellTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

            // ---- Row 1: Người in & ngày in ----
			String tenNV = (currentNV != null && currentNV.getHoTen() != null) ? (String) currentNV.getHoTen() : "N/A";
            String ngayIn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            Row rowInfo = sheet.createRow(1);
            Cell cellInfo = rowInfo.createCell(0);
            cellInfo.setCellValue("Người in: " + tenNV + "     |     Ngày in: " + ngayIn);
            cellInfo.setCellStyle(infoStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 5));

            // ---- Row 2: Trống ----
            sheet.createRow(2);

            // ---- Row 3: Header cột ----
            Row rowHeader = sheet.createRow(3);
            rowHeader.setHeightInPoints(22);
            String[] headers = {"STT", "Mã KH", "Tên khách hàng", "Email", "Số điện thoại", "CCCD"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = rowHeader.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // ---- Data rows ----
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Row dataRow = sheet.createRow(4 + i);
                dataRow.setHeightInPoints(18);
                CellStyle style = (i % 2 == 0) ? dataStyle : dataStyleAlt;

                Cell cStt = dataRow.createCell(0); cStt.setCellValue(i + 1); cStt.setCellStyle(style);
                for (int j = 0; j < COLS.length; j++) {
                    Cell c = dataRow.createCell(j + 1);
                    Object val = tableModel.getValueAt(i, j);
                    c.setCellValue(val != null ? val.toString() : "");
                    c.setCellStyle(style);
                }
            }

            // ---- Auto-size columns ----
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Thêm padding
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
            }

            // ---- Ghi file ----
            try (FileOutputStream fos = new FileOutputStream(path)) {
                wb.write(fos);
            }

            int opt = JOptionPane.showConfirmDialog(this,
                "Xuất Excel thành công!\nBạn có muốn mở file không?",
                "Thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(new java.io.File(path));
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi xuất Excel: " + ex.getMessage() +
                "\n(Kiểm tra thư viện Apache POI có trong classpath không?)",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // ==================== BUILD TABLE ====================
    private JTable buildTable() {
        JTable t = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
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

        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = t.rowAtPoint(e.getPoint());
                    if (row >= 0) { t.setRowSelectionInterval(row, row); openInfoDialog(row); }
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
        JButton btnExcel  = makeBtn("In Excel", BtnStyle.EXCEL);

        btnAdd.addActionListener(e -> openAddDialog());
        btnDelete.addActionListener(e -> performDeleteMultiple());
        btnExcel.addActionListener(e -> inDanhSachExcel());

        right.add(btnAdd);
        right.add(btnDelete);
        right.add(btnExcel);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        Vector<Vector<Object>> data = kh_dao.getAllKhachHang();
        int coEmail = 0;
        for (Vector<Object> row : data) {
            tableModel.addRow(row);
            if (row.size() > 2 && row.get(2) != null && !row.get(2).toString().trim().isEmpty())
                coEmail++;
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
        bar.add(createStatCard("TỔNG KHÁCH HÀNG", lblTotal, ACCENT));
        bar.add(createStatCard("CÓ EMAIL", lblCoEmail, new Color(34, 197, 94)));
        return bar;
    }

    private JPanel buildFilterCard() {
        JPanel card = buildCard(new FlowLayout(FlowLayout.LEFT, 12, 12));
        txtSearch = makeDialogField(18);
        txtSearch.setFont(F_CELL);
        JButton btnSearch  = makeBtn("Tìm", BtnStyle.PRIMARY);
        JButton btnRefresh = makeBtn("Làm mới", BtnStyle.SECONDARY);
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
        styleScrollBar(scroll.getVerticalScrollBar());
        styleScrollBar(scroll.getHorizontalScrollBar());
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(new JSeparator(), BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);
        return body;
    }

    // ==================== UTILITY ====================
    private String capitalizeName(String name) {
        if (name == null || name.trim().isEmpty()) return name;
        String[] words = name.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty())
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private JLabel makeErrorLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        l.setForeground(new Color(0xC0392B));
        l.setPreferredSize(new Dimension(100, 16));
        return l;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private void setFieldError(JLabel errLbl, JTextField field, String msg) {
        errLbl.setText("⚠ " + msg);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xC0392B), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    private void clearFieldError(JLabel errLbl, JTextField field) {
        errLbl.setText(" ");
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    private DocumentListener clearErr(JLabel errLbl, JTextField field) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { clearFieldError(errLbl, field); }
            public void removeUpdate(DocumentEvent e)  { clearFieldError(errLbl, field); }
            public void changedUpdate(DocumentEvent e) { clearFieldError(errLbl, field); }
        };
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(15, 20, 15, 20)));
        JLabel lblT = new JLabel(title);
        lblT.setForeground(COLOR_TEXT_MUTED);
        lblT.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblValue.setForeground(accent);
        lblValue.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        p.add(lblT, BorderLayout.NORTH);
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
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base;
                switch (style) {
                    case PRIMARY:   base = getModel().isPressed() ? ACCENT.darker()       : getModel().isRollover() ? ACCENT_HVR    : ACCENT; break;
                    case DANGER:    base = getModel().isPressed() ? BTN_RED.darker()      : getModel().isRollover() ? BTN_RED_HVR   : BTN_RED; break;
                    case SUCCESS:   base = getModel().isPressed() ? BTN_GREEN.darker()    : getModel().isRollover() ? BTN_GREEN_HVR : BTN_GREEN; break;
                    case EXCEL:     base = getModel().isPressed() ? BTN_EXCEL.darker()    : getModel().isRollover() ? BTN_EXCEL_HVR : BTN_EXCEL; break;
                    default:        base = Color.WHITE; break;
                }
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (style == BtnStyle.SECONDARY) {
                    g2.setColor(BORDER);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_LABEL);
        btn.setForeground(style == BtnStyle.SECONDARY ? TEXT_DARK : Color.WHITE);
        btn.setPreferredSize(new Dimension(text.length() > 8 ? 160 : 110, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() { thumbColor = new Color(0x5B9BD5); trackColor = new Color(0xF0F5FF); }
            protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDragging ? new Color(0x1A5EAB) : new Color(0x5B9BD5));
                g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 8, 8);
                g2.dispose();
            }
            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                g.setColor(new Color(0xF0F5FF));
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        });
        sb.setPreferredSize(new Dimension(10, 10));
    }

    // ==================== INNER CLASSES ====================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setOpaque(true);
            l.setBackground(ACCENT);
            l.setForeground(Color.WHITE);
            l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 6));
            return l;
        }
    }

    private static class ShadowBorder extends AbstractBorder {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(BORDER);
            g2.drawRoundRect(x, y, w-1, h-1, 12, 12);
        }
    }
}