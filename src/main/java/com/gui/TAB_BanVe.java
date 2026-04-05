package com.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class TAB_BanVe extends JPanel {
    private Color primaryColor = new Color(0, 122, 255);
    private JTextField txtDi, txtDen;
    private JTable tblChuyenTau;
    private DefaultTableModel modelChuyenTau;
    private JLabel lblStatus;

    public TAB_BanVe() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // 1. Header: Thanh công cụ tìm kiếm nhanh
        initHeader();

        // 2. Center: Danh sách chuyến tàu
        initContent();
    }

    private void initHeader() {
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        txtDi = new JTextField(15);
        txtDen = new JTextField(15);
        JButton btnLoc = new JButton("Lọc lại");
        btnLoc.setBackground(primaryColor);
        btnLoc.setForeground(Color.WHITE);

        lblStatus = new JLabel("Vui lòng chọn chuyến tàu");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        pnlHeader.add(new JLabel("Từ:"));
        pnlHeader.add(txtDi);
        pnlHeader.add(new JLabel("Đến:"));
        pnlHeader.add(txtDen);
        pnlHeader.add(btnLoc);
        pnlHeader.add(Box.createHorizontalStrut(50));
        pnlHeader.add(lblStatus);

        add(pnlHeader, BorderLayout.NORTH);
    }

    private void initContent() {
        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnlContent.setOpaque(false);

        // Khởi tạo bảng danh sách chuyến tàu
        String[] columns = {"Mã Chuyến", "Tàu", "Giờ đi", "Giờ đến", "Quãng đường", "Trạng thái"};
        modelChuyenTau = new DefaultTableModel(columns, 0);
        tblChuyenTau = new JTable(modelChuyenTau);
        tblChuyenTau.setRowHeight(35);
        tblChuyenTau.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JScrollPane scrollPane = new JScrollPane(tblChuyenTau);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        pnlContent.add(new JLabel("KẾT QUẢ TÌM KIẾM CHUYẾN TÀU", JLabel.LEFT), BorderLayout.NORTH);
        pnlContent.add(scrollPane, BorderLayout.CENTER);

        add(pnlContent, BorderLayout.CENTER);
    }

    /**
     * Hàm này được gọi từ TAB_Dashboard thông qua GUI_General
     */
    public void filterTrains(String di, String den, Date ngay) {
        // 1. Cập nhật giao diện thanh tìm kiếm nhanh
        txtDi.setText(di);
        txtDen.setText(den);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        lblStatus.setText("Kết quả tìm kiếm cho ngày: " + sdf.format(ngay));
        lblStatus.setForeground(primaryColor);

        // 2. Xóa dữ liệu cũ trên bảng
        modelChuyenTau.setRowCount(0);

        // 3. Giả lập hoặc gọi DAO để lấy dữ liệu
        // Ở đây tôi thêm ví dụ, bạn sẽ thay bằng gọi DAO_ChuyenTau
        modelChuyenTau.addRow(new Object[]{"CT001", "SE1", "08:00", "14:00", "450km", "Còn vé"});
        modelChuyenTau.addRow(new Object[]{"CT005", "SE3", "10:30", "16:45", "450km", "Còn vé"});
        
        // Cập nhật lại UI
        revalidate();
        repaint();
    }
}