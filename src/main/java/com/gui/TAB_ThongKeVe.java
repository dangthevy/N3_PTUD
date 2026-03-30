package com.gui;

import com.toedter.calendar.JDateChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TAB_ThongKeVe extends JPanel implements ActionListener {

    // Đã thêm final để IntelliJ hết báo gạch vàng
    private final JComboBox<String> cboTieuChi;
    private final JComboBox<String> cboThongKe;
    private final JDateChooser dcTuNgay;
    private final JDateChooser dcDenNgay;
    private final JButton btnThongKe;
    private final JButton btnXuatExcel;

    private final JTable tableTKVe;
    private final DefaultTableModel tableModel;
    private final JPanel chartPanel;

    // Đổi sang Dataset dành riêng cho Biểu đồ tròn (Pie Chart)
    private final DefaultPieDataset pieDataset;

    public TAB_ThongKeVe() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ================= 1. KHU VỰC HEADER & FILTER (TOP) =================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ THỐNG KÊ VÉ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 122, 255));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        filterPanel.setBackground(Color.WHITE);

        filterPanel.add(new JLabel("Thống kê theo:"));
        String[] tieuChi = {"Trạng thái vé", "Loại vé", "Tuyến đi"};
        cboTieuChi = new JComboBox<>(tieuChi);
        filterPanel.add(cboTieuChi);

        filterPanel.add(new JLabel("Thời gian:"));
        String[] options = {"Tùy chọn", "Tuần này", "Tháng này", "Quý này", "Năm này"};
        cboThongKe = new JComboBox<>(options);
        filterPanel.add(cboThongKe);

        filterPanel.add(new JLabel("Từ ngày:"));
        dcTuNgay = new JDateChooser();
        dcTuNgay.setDateFormatString("dd/MM/yyyy");
        dcTuNgay.setPreferredSize(new Dimension(130, 25));
        filterPanel.add(dcTuNgay);

        filterPanel.add(new JLabel("Đến ngày:"));
        dcDenNgay = new JDateChooser();
        dcDenNgay.setDateFormatString("dd/MM/yyyy");
        dcDenNgay.setPreferredSize(new Dimension(130, 25));
        filterPanel.add(dcDenNgay);

        btnThongKe = new JButton("Xem thống kê");
        btnThongKe.setBackground(new Color(0, 122, 255));
        btnThongKe.setForeground(Color.WHITE);
        btnThongKe.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        filterPanel.add(btnThongKe);

        btnXuatExcel = new JButton("Xuất file Excel");
        btnXuatExcel.setBackground(new Color(40, 167, 69));
        btnXuatExcel.setForeground(Color.WHITE);
        btnXuatExcel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        filterPanel.add(btnXuatExcel);

        topPanel.add(filterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ================= 2. KHU VỰC TÓM TẮT (KPI CARDS) =================
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        kpiPanel.setBackground(Color.WHITE);
        kpiPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 20, 50));

        kpiPanel.add(createKpiCard("Tổng Vé Bán Ra", "1,250", new Color(0, 122, 255)));
        kpiPanel.add(createKpiCard("Vé Đã Sử Dụng", "980", new Color(40, 167, 69)));
        kpiPanel.add(createKpiCard("Vé Hủy/Hết Hạn", "270", new Color(220, 53, 69)));

        topPanel.add(kpiPanel, BorderLayout.SOUTH);

        // ================= 3. KHU VỰC SPLIT PANE (CENTER: TRÁI - PHẢI) =================
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Biểu đồ Tỷ lệ (%)"));

        // KHỞI TẠO BIỂU ĐỒ TRÒN JFREECHART Ở ĐÂY
        pieDataset = new DefaultPieDataset();
        JFreeChart pieChart = ChartFactory.createPieChart(
                "", // Tiêu đề biểu đồ (Để trống vì đã có viền TitledBorder)
                pieDataset,
                true, // Có hiển thị chú thích (Legend)
                true,
                false);

        // Cấu hình nền trắng cho biểu đồ đẹp hơn
        pieChart.setBackgroundPaint(Color.WHITE);
        pieChart.getPlot().setBackgroundPaint(Color.WHITE);

        ChartPanel jfreeChartPanel = new ChartPanel(pieChart);
        chartPanel.add(jfreeChartPanel, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Bảng Số Liệu Chi Tiết"));

        String[] cols = {"STT", "Tiêu chí", "Số lượng vé", "Tỷ lệ (%)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableTKVe = new JTable(tableModel);
        tableTKVe.setRowHeight(25);
        tableTKVe.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tableTKVe);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        chartPanel.setPreferredSize(new Dimension(450,0));

        add(chartPanel, BorderLayout.WEST);
        add(tablePanel, BorderLayout.CENTER);

        // Đăng ký sự kiện
        cboThongKe.addActionListener(this);
        btnThongKe.addActionListener(this);
        btnXuatExcel.addActionListener(this);
    }

    private JPanel createKpiCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(color, 2, true));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setForeground(Color.DARK_GRAY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 28));
        lblValue.setForeground(color);
        lblValue.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    // Đã viết lại hàm Load Dữ Liệu chuẩn cho Biểu Đồ Tròn
    private void loadDuLieuThongKe() {
        tableModel.setRowCount(0);
        pieDataset.clear();

        // 1. Đổ dữ liệu giả lập vào Bảng (Phù hợp với 4 cột: STT, Tiêu chí, Số lượng, Tỷ lệ)
        tableModel.addRow(new Object[]{1, "Đã sử dụng", 980, "78.4%"});
        tableModel.addRow(new Object[]{2, "Chưa sử dụng", 200, "16.0%"});
        tableModel.addRow(new Object[]{3, "Hết hạn/Hủy", 70, "5.6%"});

        // 2. Đổ dữ liệu vào Biểu đồ tròn
        // Cú pháp: setValue("Tên_nhãn", Giá_trị_kiểu_số)
        pieDataset.setValue("Đã sử dụng", 980);
        pieDataset.setValue("Chưa sử dụng", 200);
        pieDataset.setValue("Hết hạn/Hủy", 70);

        JOptionPane.showMessageDialog(this, "Đã cập nhật dữ liệu biểu đồ!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == cboThongKe) {
            if (cboThongKe.getSelectedIndex() == 0) {
                dcTuNgay.setEnabled(true);
                dcDenNgay.setEnabled(true);
            } else {
                dcTuNgay.setEnabled(false);
                dcDenNgay.setEnabled(false);
                dcTuNgay.setDate(null);
                dcDenNgay.setDate(null);
            }
        } else if (source == btnThongKe) {
            // Gọi hàm load dữ liệu ở đây
            loadDuLieuThongKe();
        } else if (source == btnXuatExcel) {
            JOptionPane.showMessageDialog(this, "Tính năng Xuất Excel đang được xây dựng!");
        }
    }
}