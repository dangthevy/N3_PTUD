package com.gui;

import com.toedter.calendar.JDateChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TAB_ThongKeDoanhThu extends JPanel implements ActionListener {

    // Đã sửa lại tên biến chuẩn
    private JComboBox<String> cboThongKe;
    private JDateChooser dcTuNgay;
    private JDateChooser dcDenNgay;
    private JButton btnThongKe;
    private JButton btnXuatExcel;

    private JTable tableTKDT;
    private DefaultTableModel tableModel;
    private JPanel chartPanel;

    private DefaultCategoryDataset chartDataset;

    public TAB_ThongKeDoanhThu() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ================= KHU VỰC HEADER & FILTER (TOP) =================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ THỐNG KÊ DOANH THU", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 122, 255));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        filterPanel.setBackground(Color.WHITE);

        filterPanel.add(new JLabel("Thời gian:"));
        String[] options = {"Tùy chọn", "Tuần này", "Tháng này", "Quý này", "Năm này"};
        // Đã sửa cbKyThongKe thành cboThongKe
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

        // ================= 2. KHU VỰC SPLIT PANE (CENTER) =================
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Biểu đồ Doanh thu"));

        chartDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart(
                "",
                "Ngày",
                "Doanh thu (VNĐ)",
                chartDataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 122, 255));

        ChartPanel jfreeChartPanel = new ChartPanel(barChart);
        chartPanel.add(jfreeChartPanel, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Bảng chi tiết doanh thu"));

        String[] cols = {"STT", "Ngày", "Số vé bán ra", "Doanh thu (VNĐ)"};
        tableModel = new DefaultTableModel(cols, 0);

        tableTKDT = new JTable(tableModel);
        tableTKDT.setRowHeight(25);
        tableTKDT.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tableTKDT);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        tablePanel.setPreferredSize(new Dimension(0,330));

        add(tablePanel, BorderLayout.SOUTH);
        add(chartPanel, BorderLayout.CENTER);

        cboThongKe.addActionListener(this);
        btnThongKe.addActionListener(this);
        btnXuatExcel.addActionListener(this);
    }

    private void loadDuLieuThongKe() {
        tableModel.setRowCount(0);
        chartDataset.clear();

        tableModel.addRow(new Object[]{1, "18/03/2026", 120, "24,500,000"});
        tableModel.addRow(new Object[]{2, "19/03/2026", 155, "31,000,000"});
        tableModel.addRow(new Object[]{3, "20/03/2026", 110, "22,000,000"});
        tableModel.addRow(new Object[]{4, "21/03/2026", 210, "45,000,000"});
        tableModel.addRow(new Object[]{5, "22/03/2026", 190, "38,500,000"});

        chartDataset.addValue(24500000, "Doanh thu", "18/03");
        chartDataset.addValue(31000000, "Doanh thu", "19/03");
        chartDataset.addValue(22000000, "Doanh thu", "20/03");
        chartDataset.addValue(45000000, "Doanh thu", "21/03");
        chartDataset.addValue(38500000, "Doanh thu", "22/03");

        JOptionPane.showMessageDialog(this, "Đã cập nhật dữ liệu thống kê!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == btnThongKe) {
            loadDuLieuThongKe();
        }
        // Đã sửa cbKyThongKe thành cboThongKe
        else if (source == cboThongKe) {
            if (cboThongKe.getSelectedIndex() == 0) {
                dcTuNgay.setEnabled(true);
                dcDenNgay.setEnabled(true);
            } else {
                dcTuNgay.setEnabled(false);
                dcDenNgay.setEnabled(false);
                dcTuNgay.setDate(null);
                dcDenNgay.setDate(null);
            }
        }else if (source == btnXuatExcel) {
            JOptionPane.showMessageDialog(this, "Tính năng Xuất Excel đang được xây dựng!");
        }
    }
}