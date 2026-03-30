package com.gui;

import com.dao.DAO_KhachHang;
import com.entities.KhachHang;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class TAB_QLKhachHang extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JComboBox<String> cbSort;
    private JButton btnPrint;
    private DAO_KhachHang kh_dao = new DAO_KhachHang();

    private final Color PRIMARY_BLUE = new Color(41, 128, 185);
    private final Color SUCCESS_GREEN = new Color(39, 174, 96);
    private final Color DANGER_RED = new Color(192, 57, 43);
    private final Color BG_LIGHT = new Color(245, 247, 250);

    public TAB_QLKhachHang() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // --- 1. THANH CÔNG CỤ (HEADER) BAO GỒM TIÊU ĐỀ ---
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 15));
        pnlHeader.setOpaque(false);

        // TIÊU ĐỀ TRANG
        JLabel lblPageTitle = new JLabel("QUẢN LÝ THÔNG TIN KHÁCH HÀNG");
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblPageTitle.setForeground(PRIMARY_BLUE);
        pnlHeader.add(lblPageTitle, BorderLayout.NORTH);

        // THANH TÌM KIẾM
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
        model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(240, 242, 245));
        
        pnlTableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        add(pnlTableContainer, BorderLayout.CENTER);

        // --- 3. NÚT BẤM (SOUTH) ---
        btnPrint = createStyledButton("In danh sách Khách hàng", PRIMARY_BLUE, Color.WHITE);
        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSouth.setOpaque(false);
        pnlSouth.add(btnPrint);
        add(pnlSouth, BorderLayout.SOUTH);

        initEvents();
        loadData();
    }

    private void initEvents() {
        // 1. Sự kiện tìm kiếm (Gõ đến đâu tìm đến đó)
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
        });

        // 2. Sự kiện nút In danh sách (Phải đặt ĐỘC LẬP bên ngoài DocumentListener)
        btnPrint.addActionListener(e -> xuatDanhSachKhachHangPDF());

        // 3. Sự kiện click vào bảng để sửa
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showEditDialog();
            }
        });
        
        // 4. Sự kiện ComboBox sắp xếp (Nếu bạn muốn kích hoạt chức năng sắp xếp)
        cbSort.addActionListener(e -> {
            // Có thể thêm logic sắp xếp tại đây nếu cần
        });
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
        g.insets = new Insets(10, 15, 10, 15);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField tTen = createDialogTextField(ten);
        JTextField tEmail = createDialogTextField(email);
        JTextField tSdt = createDialogTextField(sdt);
        JTextField tCccd = createDialogTextField(cccd);

        g.gridx = 0; g.gridy = 0; dialog.add(new JLabel("Mã KH: " + ma), g);
        g.gridx = 0; g.gridy = 1; dialog.add(new JLabel("Họ tên:"), g);
        g.gridx = 1; dialog.add(tTen, g);
        g.gridx = 0; g.gridy = 2; dialog.add(new JLabel("Số điện thoại:"), g);
        g.gridx = 1; dialog.add(tSdt, g);
        g.gridx = 0; g.gridy = 3; dialog.add(new JLabel("Email:"), g);
        g.gridx = 1; dialog.add(tEmail, g);
        g.gridx = 0; g.gridy = 4; dialog.add(new JLabel("Số CCCD:"), g);
        g.gridx = 1; dialog.add(tCccd, g);

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton bUpdate = createStyledButton("Cập nhật", SUCCESS_GREEN, Color.WHITE);
        JButton bDelete = createStyledButton("Xóa", DANGER_RED, Color.WHITE);
        pBtn.add(bDelete); pBtn.add(bUpdate);

        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        dialog.add(pBtn, g);

        bUpdate.addActionListener(ev -> {
            KhachHang kh = new KhachHang(ma, tTen.getText().trim(), tSdt.getText().trim(), tCccd.getText().trim(), tEmail.getText().trim());
            if (kh_dao.updateKhachHang(kh)) {
                JOptionPane.showMessageDialog(dialog, "Đã cập nhật!");
                dialog.dispose(); loadData();
            }
        });

        bDelete.addActionListener(ev -> {
            if (JOptionPane.showConfirmDialog(dialog, "Xóa khách hàng này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (kh_dao.deleteKhachHang(ma)) {
                    dialog.dispose(); loadData();
                }
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTextField createDialogTextField(String text) {
        JTextField tf = new JTextField(text, 20);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(220, 220, 220), 1), new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }
    private void xuatDanhSachKhachHangPDF() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Danh sách trống, không có gì để in!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu danh sách khách hàng PDF");
        fileChooser.setSelectedFile(new java.io.File("DanhSachKhachHang.pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            
            try {
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(path));
                document.open();

                // Tiêu đề file PDF
                com.itextpdf.text.Font titleFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 18);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("DANH SACH KHACH HANG", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
                document.add(new com.itextpdf.text.Paragraph("Ngay in: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date())));
                document.add(new com.itextpdf.text.Paragraph(" ")); // Khoảng trống

                // Tạo bảng PDF (5 cột tương ứng với JTable)
                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(5);
                pdfTable.setWidthPercentage(100);
                float[] columnWidths = {15f, 30f, 30f, 20f, 20f};
                pdfTable.setWidths(columnWidths);

                // Header của bảng
                String[] headers = {"Ma KH", "Ho Ten", "Email", "SDT", "CCCD"};
                for (String header : headers) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header));
                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                    pdfTable.addCell(cell);
                }

                // Đổ dữ liệu từ JTable vào PDF
                for (int i = 0; i < model.getRowCount(); i++) {
                    pdfTable.addCell(model.getValueAt(i, 0).toString());
                    pdfTable.addCell(model.getValueAt(i, 1).toString());
                    pdfTable.addCell(model.getValueAt(i, 2).toString());
                    pdfTable.addCell(model.getValueAt(i, 3).toString());
                    pdfTable.addCell(model.getValueAt(i, 4).toString());
                }

                document.add(pdfTable);
                document.close();

                JOptionPane.showMessageDialog(this, "Xuất danh sách thành công!");
                
                // Tự động mở file
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(new java.io.File(path));
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi in PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}