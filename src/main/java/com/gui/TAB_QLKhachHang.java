package com.gui;

import com.dao.DAO_KhachHang;
import com.entities.KhachHang;
import com.entities.NhanVien;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.util.Vector;

public class TAB_QLKhachHang extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbSort;
    private JButton btnExcel; // Chỉ giữ lại nút Excel
    private DAO_KhachHang kh_dao = new DAO_KhachHang();

    private NhanVien nhanVienDangNhap;

    private final Color PRIMARY_BLUE = new Color(41, 128, 185);
    private final Color SUCCESS_GREEN = new Color(39, 174, 96);
    private final Color DANGER_RED = new Color(192, 57, 43);
    private final Color BG_LIGHT = new Color(245, 247, 250);

    public TAB_QLKhachHang(NhanVien nv) {
        this.nhanVienDangNhap = nv;

        setLayout(new BorderLayout(0, 15));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // --- 1. THANH CÔNG CỤ (HEADER) ---
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 15));
        pnlHeader.setOpaque(false);

        JLabel lblPageTitle = new JLabel("QUẢN LÝ THÔNG TIN KHÁCH HÀNG");
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblPageTitle.setForeground(PRIMARY_BLUE);
        pnlHeader.add(lblPageTitle, BorderLayout.NORTH);

        JPanel pnlSearchAction = new JPanel(new BorderLayout());
        pnlSearchAction.setOpaque(false);

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeft.setOpaque(false);

        txtSearch = new JTextField(25);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 12, 8, 12)));

        pnlLeft.add(new JLabel("Tìm kiếm (Tên/SĐT) 🔍:"));
        pnlLeft.add(txtSearch);

        cbSort = new JComboBox<>(new String[] { "Sắp xếp: Mặc định", "Tên khách hàng", "Số điện thoại" });
        cbSort.setPreferredSize(new Dimension(160, 35));
        pnlLeft.add(cbSort);

        pnlSearchAction.add(pnlLeft, BorderLayout.WEST);
        pnlHeader.add(pnlSearchAction, BorderLayout.CENTER);

        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        JPanel pnlTableContainer = new JPanel(new BorderLayout());
        pnlTableContainer.setBackground(Color.WHITE);
        pnlTableContainer.setBorder(new LineBorder(new Color(230, 233, 237), 1, true));

        String[] cols = { "Mã KH", "Họ và Tên", "Email", "Số điện thoại", "CCCD" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(240, 242, 245));

        pnlTableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        add(pnlTableContainer, BorderLayout.CENTER);

        // --- 3. NÚT BẤM (SOUTH) ---
        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlSouth.setOpaque(false);

        btnExcel = createStyledButton("Xuất Excel 📊", SUCCESS_GREEN, Color.WHITE);
        pnlSouth.add(btnExcel);
        add(pnlSouth, BorderLayout.SOUTH);

        initEvents();
        loadData();
    }

    private void initEvents() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
        });

        btnExcel.addActionListener(e -> xuatDanhSachKhachHangExcel());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showEditDialog();
            }
        });
    }

    private void xuatDanhSachKhachHangExcel() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Danh sách trống!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file Excel");
        fileChooser.setSelectedFile(new java.io.File("DanhSachKhachHang.xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".xlsx")) path += ".xlsx";

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Khách Hàng");

                String tenNV = (nhanVienDangNhap != null) ? nhanVienDangNhap.getTenNV() : "N/A";
                String thoiGian = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());

                CellStyle infoStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font infoFont = workbook.createFont();
                infoFont.setItalic(true);
                infoStyle.setFont(infoFont);

                Row rowNguoiXuat = sheet.createRow(0);
                rowNguoiXuat.createCell(0).setCellValue("Nhân viên xuất: " + tenNV);
                rowNguoiXuat.getCell(0).setCellStyle(infoStyle);

                Row rowThoiGian = sheet.createRow(1);
                rowThoiGian.createCell(0).setCellValue("Thời gian xuất: " + thoiGian);
                rowThoiGian.getCell(0).setCellStyle(infoStyle);

                sheet.createRow(2);

                CellStyle headerStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                headerStyle.setBorderBottom(BorderStyle.THIN);

                Row headerRow = sheet.createRow(3);
                for (int i = 0; i < model.getColumnCount(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(model.getColumnName(i));
                    cell.setCellStyle(headerStyle);
                }

                for (int i = 0; i < model.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 4);
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object val = model.getValueAt(i, j);
                        row.createCell(j).setCellValue(val != null ? val.toString() : "");
                    }
                }

                for (int i = 0; i < model.getColumnCount(); i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(path)) {
                    workbook.write(fileOut);
                }

                JOptionPane.showMessageDialog(this, "Xuất Excel thành công!");
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(new java.io.File(path));
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
            }
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        String ma = model.getValueAt(row, 0).toString();
        String ten = model.getValueAt(row, 1).toString();
        String email = model.getValueAt(row, 2).toString();
        String sdt = model.getValueAt(row, 3).toString();
        String cccd = model.getValueAt(row, 4).toString();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chỉnh sửa khách hàng", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 15, 2, 15);
        g.fill = GridBagConstraints.HORIZONTAL;

        String emailPrefix = email.contains("@gmail.com") ? email.split("@")[0] : email;

        JTextField tTen = createDialogTextField(ten);
        JTextField tSdt = createDialogTextField(sdt);
        JTextField tEmail = createDialogTextField(emailPrefix);
        JTextField tCccd = createDialogTextField(cccd);

        JLabel errTen = createErrorLabel();
        JLabel errSdt = createErrorLabel();
        JLabel errEmail = createErrorLabel();
        JLabel errCccd = createErrorLabel();

        tTen.addActionListener(e -> {
            tTen.setText(capitalizeWords(tTen.getText().trim()));
            errTen.setText(" ");
            tSdt.requestFocus();
        });

        tSdt.addActionListener(e -> {
            String val = tSdt.getText().trim();
            if (!val.matches("\\d{10}")) {
                errSdt.setText("SĐT phải đúng 10 chữ số!");
                tSdt.selectAll();
            } else {
                errSdt.setText(" ");
                tEmail.requestFocus();
            }
        });

        tCccd.addActionListener(e -> {
            String val = tCccd.getText().trim();
            if (!val.matches("\\d{12}")) {
                errCccd.setText("CCCD phải đúng 12 chữ số!");
                tCccd.selectAll();
            } else {
                errCccd.setText(" ");
            }
        });

        int y = 0;
        g.gridy = y++; g.gridx = 0; g.gridwidth = 2;
        dialog.add(new JLabel("Mã KH: " + ma), g);

        g.gridy = y++; g.gridx = 0; g.gridwidth = 1;
        dialog.add(new JLabel("Họ tên:"), g);
        g.gridx = 1; dialog.add(tTen, g);
        g.gridy = y++; g.gridx = 1; dialog.add(errTen, g);

        g.gridy = y++; g.gridx = 0;
        dialog.add(new JLabel("Số điện thoại:"), g);
        g.gridx = 1; dialog.add(tSdt, g);
        g.gridy = y++; g.gridx = 1; dialog.add(errSdt, g);

        g.gridy = y++; g.gridx = 0;
        dialog.add(new JLabel("Tên Email:"), g);
        g.gridx = 1; dialog.add(tEmail, g);
        g.gridy = y++; g.gridx = 1; dialog.add(errEmail, g);

        g.gridy = y++; g.gridx = 0;
        dialog.add(new JLabel("Số CCCD:"), g);
        g.gridx = 1; dialog.add(tCccd, g);
        g.gridy = y++; g.gridx = 1; dialog.add(errCccd, g);

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pBtn.setOpaque(false);
        JButton bUpdate = createStyledButton("Cập nhật", SUCCESS_GREEN, Color.WHITE);
        JButton bDelete = createStyledButton("Xóa", DANGER_RED, Color.WHITE);
        pBtn.add(bDelete); pBtn.add(bUpdate);

        g.gridy = y++; g.gridx = 0; g.gridwidth = 2;
        dialog.add(pBtn, g);

        bUpdate.addActionListener(ev -> {
            boolean isInvalid = false;
            if (tTen.getText().trim().isEmpty()) { errTen.setText("Không được để trống!"); isInvalid = true; }
            if (!tSdt.getText().trim().matches("\\d{10}")) { errSdt.setText("SĐT phải đúng 10 số!"); isInvalid = true; }
            if (!tCccd.getText().trim().matches("\\d{12}")) { errCccd.setText("CCCD phải đúng 12 số!"); isInvalid = true; }

            if (isInvalid) return;

            KhachHang kh = new KhachHang(ma, capitalizeWords(tTen.getText().trim()), tSdt.getText().trim(),
                    tCccd.getText().trim(), tEmail.getText().trim() + "@gmail.com");
            if (kh_dao.updateKhachHang(kh)) {
                JOptionPane.showMessageDialog(dialog, "Đã cập nhật!");
                dialog.dispose();
                loadData();
            }
        });

        bDelete.addActionListener(ev -> {
            if (JOptionPane.showConfirmDialog(dialog, "Xóa khách hàng này?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (kh_dao.deleteKhachHang(ma)) {
                    dialog.dispose();
                    loadData();
                }
            }
        });

        dialog.pack();
        dialog.setMinimumSize(new Dimension(450, 450));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbl.setForeground(Color.RED);
        return lbl;
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }

    private void search() {
        String key = txtSearch.getText().trim();
        if (key.isEmpty()) { loadData(); return; }
        model.setRowCount(0);
        Vector<Vector<Object>> filteredData = kh_dao.searchKhachHang(key);
        for (Vector<Object> row : filteredData) model.addRow(row);
    }

    public void loadData() {
        model.setRowCount(0);
        Vector<Vector<Object>> data = kh_dao.getAllKhachHang();
        for (Vector<Object> row : data) model.addRow(row);
    }

    private JButton createStyledButton(String t, Color bg, Color fg) {
        JButton b = new JButton(t);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTextField createDialogTextField(String text) {
        JTextField tf = new JTextField(text, 20);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }
}