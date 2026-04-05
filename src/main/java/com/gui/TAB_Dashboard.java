package com.gui;

import com.connectDB.ConnectDB;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TAB_Dashboard extends JPanel {

    private GUI_General parentController;
    private String role;

    // ================= COLOR TONE & UI =================
    private static final Color BG_PAGE     = new Color(0xF8FAFC);
    private static final Color BG_CARD     = Color.WHITE;
    private static final Color TEXT_DARK   = new Color(0x0F172A);
    private static final Color TEXT_MID    = new Color(0x64748B);
    private static final Color BORDER      = new Color(0xE2E8F0);

    // Bảng màu Pastel
    private static final Color C_BLUE_BG   = new Color(239, 246, 255);
    private static final Color C_BLUE_FG   = new Color(59, 130, 246);
    private static final Color C_GREEN_BG  = new Color(240, 253, 244);
    private static final Color C_GREEN_FG  = new Color(34, 197, 94);
    private static final Color C_PURPLE_BG = new Color(250, 245, 255);
    private static final Color C_PURPLE_FG = new Color(168, 85, 247);
    private static final Color C_ORANGE_BG = new Color(255, 247, 237);
    private static final Color C_ORANGE_FG = new Color(249, 115, 22);

    private DecimalFormat df = new DecimalFormat("#,### đ");
    private DecimalFormat numDf = new DecimalFormat("#,###");

    public TAB_Dashboard(GUI_General parent, String role) {
        this.parentController = parent;
        this.role = role != null ? role.trim() : "NhanVien";

        setLayout(new BorderLayout(0, 20));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        initHeader();

        if (this.role.equalsIgnoreCase("QuanLy") || this.role.equalsIgnoreCase("Quản lý") || this.role.equalsIgnoreCase("Admin") || this.role.equalsIgnoreCase("QUANLY")) {
            initManagerView();
        } else {
            initStaffView();
        }
    }

    private void initHeader() {
        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Dashboard (" + role + ")");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(TEXT_DARK);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy");
        JLabel lblSub = new JLabel("Tổng quan hoạt động hệ thống – " + sdf.format(new Date()));
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSub.setForeground(TEXT_MID);

        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSub);
        add(pnlHeader, BorderLayout.NORTH);
    }

    // =====================================================================
    // MÀN HÌNH QUẢN LÝ
    // =====================================================================
    private void initManagerView() {
        JPanel pnlMain = new JPanel(new BorderLayout(0, 20));
        pnlMain.setOpaque(false);

        // 1. THẺ KPI
        JPanel pnlStats = new JPanel(new GridLayout(1, 4, 20, 0));
        pnlStats.setOpaque(false);

        long tongDoanhThu = 0;
        int tongVe = 0, tauHoatDong = 0;
        double tiLeLapDay = 0;

        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            // FIX: Đổi thanhTien thành tongTien theo DB mới
            ResultSet rs1 = st.executeQuery("SELECT ISNULL(SUM(tongTien), 0) FROM HoaDon WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)");
            if(rs1.next()) tongDoanhThu = rs1.getLong(1);

            // FIX: JOIN với ChiTietHoaDon để đếm chính xác số lượng vé bán ra thay vì số hóa đơn
            ResultSet rs2 = st.executeQuery("SELECT COUNT(cthd.maVe) FROM ChiTietHoaDon cthd JOIN HoaDon hd ON cthd.maHD = hd.maHD WHERE CAST(hd.ngayLap AS DATE) = CAST(GETDATE() AS DATE)");
            if(rs2.next()) tongVe = rs2.getInt(1);

            ResultSet rs3 = st.executeQuery("SELECT COUNT(*) FROM Tau WHERE trangThai = 'HOATDONG'");
            if(rs3.next()) tauHoatDong = rs3.getInt(1);

            ResultSet rs4 = st.executeQuery("SELECT " +
                    "ISNULL(CAST((SELECT COUNT(*) FROM ChoNgoi WHERE trangThai='DADAT') AS FLOAT) / " +
                    "NULLIF((SELECT COUNT(*) FROM ChoNgoi), 0) * 100, 0)");
            if(rs4.next()) tiLeLapDay = rs4.getDouble(1);
        } catch(Exception e) { e.printStackTrace(); }

        pnlStats.add(createModernStatCard("Doanh thu hôm nay", df.format(tongDoanhThu), C_BLUE_BG, C_BLUE_FG, "VND", "+12.3%", true));
        pnlStats.add(createModernStatCard("Vé đã bán hôm nay", numDf.format(tongVe), C_GREEN_BG, C_GREEN_FG, "VE", "+8.7%", true));
        pnlStats.add(createModernStatCard("Chuyến tàu đang chạy", numDf.format(tauHoatDong), C_PURPLE_BG, C_PURPLE_FG, "TAU", "0%", true));
        pnlStats.add(createModernStatCard("Tỉ lệ lấp đầy TB", String.format("%.1f%%", tiLeLapDay), C_ORANGE_BG, C_ORANGE_FG, "KH", "-2.1%", false));

        // 2. BIỂU ĐỒ
        JPanel pnlCharts = new JPanel(new GridLayout(2, 1, 20, 20));
        pnlCharts.setOpaque(false);

        // Hàng 1
        JPanel pnlTopCharts = new JPanel(new BorderLayout(20, 0));
        pnlTopCharts.setOpaque(false);

        JPanel pnlLineChart = createChartCard(createRevenue7DaysChart());
        pnlLineChart.setPreferredSize(new Dimension(0, 300));

        JPanel pnlPieChart = createChartCard(createSeatTypePieChart());
        pnlPieChart.setPreferredSize(new Dimension(400, 300));

        pnlTopCharts.add(pnlLineChart, BorderLayout.CENTER);
        pnlTopCharts.add(pnlPieChart, BorderLayout.EAST);

        // Hàng 2
        JPanel pnlBottomChart = new JPanel(new BorderLayout());
        pnlBottomChart.setOpaque(false);
        pnlBottomChart.add(createChartCard(createRevenueByRouteChart()), BorderLayout.CENTER);

        pnlCharts.add(pnlTopCharts);
        pnlCharts.add(pnlBottomChart);

        pnlMain.add(pnlStats, BorderLayout.NORTH);
        pnlMain.add(pnlCharts, BorderLayout.CENTER);
        add(pnlMain, BorderLayout.CENTER);
    }

    // =====================================================================
    // MÀN HÌNH NHÂN VIÊN
    // =====================================================================
    private void initStaffView() {
        JPanel pnlMain = new JPanel(new BorderLayout(0, 20));
        pnlMain.setOpaque(false);

        JPanel pnlStats = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlStats.setOpaque(false);

        int veHomNay = 0, tauHoatDong = 0, khachHang = 0;
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs1 = st.executeQuery("SELECT COUNT(cthd.maVe) FROM ChiTietHoaDon cthd JOIN HoaDon hd ON cthd.maHD = hd.maHD WHERE CAST(hd.ngayLap AS DATE) = CAST(GETDATE() AS DATE)");
            if(rs1.next()) veHomNay = rs1.getInt(1);

            ResultSet rs2 = st.executeQuery("SELECT COUNT(*) FROM Tau WHERE trangThai = 'HOATDONG'");
            if(rs2.next()) tauHoatDong = rs2.getInt(1);

            ResultSet rs3 = st.executeQuery("SELECT COUNT(*) FROM KhachHang");
            if(rs3.next()) khachHang = rs3.getInt(1);
        } catch(Exception e) {}

        pnlStats.add(createModernStatCard("Vé bán hôm nay", numDf.format(veHomNay), C_GREEN_BG, C_GREEN_FG, "VE", "+5.2%", true));
        pnlStats.add(createModernStatCard("Chuyến tàu đang chạy", numDf.format(tauHoatDong), C_PURPLE_BG, C_PURPLE_FG, "TAU", " 0% ", true));
        pnlStats.add(createModernStatCard("Tổng lượng Khách hàng", numDf.format(khachHang), C_ORANGE_BG, C_ORANGE_FG, "KH", "+1.2%", true));

        JPanel pnlContent = new JPanel(new BorderLayout(20, 0));
        pnlContent.setOpaque(false);

        JPanel pnlPie = createChartCard(createSeatTypePieChart());
        pnlPie.setPreferredSize(new Dimension(400, 0));

        JPanel pnlSchedule = new JPanel(new BorderLayout());
        pnlSchedule.setBackground(BG_CARD);
        pnlSchedule.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(20, 20, 20, 20)));

        JLabel lblTbl = new JLabel("Lịch trình tàu khởi hành hôm nay");
        lblTbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTbl.setBorder(new EmptyBorder(0, 0, 15, 0));

        String[] cols = {"Giờ đi", "Tên Chuyến", "Tuyến"};
        DefaultTableModel mod = new DefaultTableModel(cols, 0);
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT gioKhoiHanh, tenChuyen, tenTuyen FROM LichTrinh lt JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen JOIN Tuyen t ON ct.maTuyen = t.maTuyen WHERE ngayKhoiHanh = CAST(GETDATE() AS DATE) ORDER BY gioKhoiHanh ASC");
            while(rs.next()) {
                mod.addRow(new Object[]{rs.getString(1).substring(0,5), rs.getString(2), rs.getString(3)});
            }
            if (mod.getRowCount() == 0) mod.addRow(new Object[]{"-", "Không có chuyến nào hôm nay", "-"});
        } catch(Exception e) {}

        JTable table = new JTable(mod);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(C_BLUE_BG);
        table.getTableHeader().setForeground(TEXT_DARK);

        pnlSchedule.add(lblTbl, BorderLayout.NORTH);
        pnlSchedule.add(new JScrollPane(table), BorderLayout.CENTER);

        pnlContent.add(pnlPie, BorderLayout.WEST);
        pnlContent.add(pnlSchedule, BorderLayout.CENTER);

        pnlMain.add(pnlStats, BorderLayout.NORTH);
        pnlMain.add(pnlContent, BorderLayout.CENTER);
        add(pnlMain, BorderLayout.CENTER);
    }

    // ================= BIỂU ĐỒ ĐƯỜNG (LINE CHART) =================
    private JFreeChart createRevenue7DaysChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            // FIX: Đổi thanhTien thành tongTien
            String sql = "SELECT TOP 7 FORMAT(ngayLap, 'dd/MM') as Ngay, SUM(tongTien) as DoanhThu FROM HoaDon WHERE ngayLap >= CAST(DATEADD(day, -6, GETDATE()) AS DATE) GROUP BY FORMAT(ngayLap, 'dd/MM'), CAST(ngayLap AS DATE) ORDER BY CAST(ngayLap AS DATE) ASC";
            ResultSet rs = st.executeQuery(sql);
            boolean hasData = false;
            while(rs.next()) {
                dataset.addValue(rs.getDouble("DoanhThu"), "Doanh thu", rs.getString("Ngay"));
                hasData = true;
            }
            if(!hasData) throw new Exception();
        } catch(Exception e) {
            dataset.addValue(42000000, "Doanh thu", "21/03"); dataset.addValue(38000000, "Doanh thu", "22/03");
            dataset.addValue(56000000, "Doanh thu", "23/03"); dataset.addValue(48000000, "Doanh thu", "24/03");
            dataset.addValue(68000000, "Doanh thu", "25/03"); dataset.addValue(58000000, "Doanh thu", "26/03");
            dataset.addValue(74000000, "Doanh thu", "27/03");
        }

        JFreeChart chart = ChartFactory.createLineChart("Doanh thu 7 ngày qua", "", "", dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        chart.getTitle().setHorizontalAlignment(org.jfree.chart.ui.HorizontalAlignment.LEFT);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(BORDER);

        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, C_BLUE_FG);
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);
        return chart;
    }

    // ================= BIỂU ĐỒ TRÒN (PIE CHART) CÓ CHÚ THÍCH =================
    private JFreeChart createSeatTypePieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            String sql = "SELECT lt.tenLoaiToa, COUNT(v.maVe) as SL " +
                    "FROM Ve v JOIN ChoNgoi cn ON v.maCho = cn.maCho " +
                    "JOIN Toa t ON cn.maToa = t.maToa JOIN LoaiToa lt ON t.maLoaiToa = lt.maLoaiToa " +
                    "GROUP BY lt.tenLoaiToa";
            ResultSet rs = st.executeQuery(sql);
            boolean hasData = false;
            while(rs.next()) {
                dataset.setValue(rs.getString("tenLoaiToa"), rs.getInt("SL"));
                hasData = true;
            }
            if(!hasData) throw new Exception();
        } catch(Exception e) {
            dataset.setValue("Ghế cứng có ĐH", 35); dataset.setValue("Ghế mềm có ĐH", 28);
            dataset.setValue("Giường nằm K4", 18); dataset.setValue("Giường nằm K6", 14);
        }

        JFreeChart chart = ChartFactory.createPieChart("Loại ghế bán chạy", dataset, true, true, false);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        chart.getTitle().setHorizontalAlignment(org.jfree.chart.ui.HorizontalAlignment.LEFT);

        if (chart.getLegend() != null) {
            chart.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE);
            chart.getLegend().setBackgroundPaint(Color.WHITE);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelGenerator(null);

        plot.setSectionPaint("Ghế cứng có ĐH", C_BLUE_FG);
        plot.setSectionPaint("Ghế mềm có ĐH", C_PURPLE_FG);
        plot.setSectionPaint("Giường nằm", C_ORANGE_FG);
        return chart;
    }

    // ================= BIỂU ĐỒ CỘT (BAR CHART) =================
    private JFreeChart createRevenueByRouteChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            // FIX: Đổi cthd.donGia thành cthd.thanhTien theo DB mới
            String sql = "SELECT TOP 5 t.tenTuyen, SUM(cthd.thanhTien) as DoanhThu FROM ChiTietHoaDon cthd JOIN Ve v ON cthd.maVe = v.maVe JOIN LichTrinh lt ON v.maLT = lt.maLT JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen JOIN Tuyen t ON ct.maTuyen = t.maTuyen GROUP BY t.tenTuyen ORDER BY DoanhThu DESC";
            ResultSet rs = st.executeQuery(sql);
            boolean hasData = false;
            while(rs.next()) {
                String tuyen = rs.getString("tenTuyen").replace("Sài Gòn", "SG").replace("Hà Nội", "HN").replace("Đà Nẵng", "ĐN");
                dataset.addValue(rs.getDouble("DoanhThu"), "Doanh thu", tuyen);
                hasData = true;
            }
            if(!hasData) throw new Exception();
        } catch(Exception e) {
            dataset.addValue(950000000, "Doanh thu", "HN-SG"); dataset.addValue(450000000, "Doanh thu", "HN-ĐN");
            dataset.addValue(300000000, "Doanh thu", "HN-HUE"); dataset.addValue(280000000, "Doanh thu", "ĐN-SG");
        }

        JFreeChart chart = ChartFactory.createBarChart("Doanh thu theo tuyến", "", "", dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        chart.getTitle().setHorizontalAlignment(org.jfree.chart.ui.HorizontalAlignment.LEFT);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(BORDER);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, C_BLUE_FG);
        renderer.setMaximumBarWidth(0.12);
        renderer.setDrawBarOutline(false);
        return chart;
    }

    // ================= GIAO DIỆN HELPERS =================

    private JPanel createModernStatCard(String title, String value, Color bgColor, Color iconColor, String iconTxt, String trend, boolean isUp) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(18, 18, 18, 18)));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblIcon = new JLabel(iconTxt, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIcon.setForeground(Color.WHITE);
        lblIcon.setOpaque(true);
        lblIcon.setBackground(iconColor);
        lblIcon.setPreferredSize(new Dimension(36, 36));

        JLabel lblTrend = new JLabel(trend);
        lblTrend.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTrend.setForeground(isUp ? C_GREEN_FG : new Color(231, 76, 60));
        lblTrend.setOpaque(true);
        lblTrend.setBackground(isUp ? C_GREEN_BG : new Color(253, 237, 237));
        lblTrend.setBorder(new EmptyBorder(4, 8, 4, 8));

        pnlTop.add(lblIcon, BorderLayout.WEST);
        pnlTop.add(lblTrend, BorderLayout.EAST);

        JLabel lblV = new JLabel(value);
        lblV.setForeground(TEXT_DARK);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lblT = new JLabel(title);
        lblT.setForeground(TEXT_MID);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel pnlText = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlText.setOpaque(false);
        pnlText.add(lblV);
        pnlText.add(lblT);

        p.add(pnlTop, BorderLayout.NORTH);
        p.add(pnlText, BorderLayout.CENTER);

        return p;
    }

    private JPanel createChartCard(JFreeChart chart) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(15, 15, 15, 15)));
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(Color.WHITE);
        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    private static class ShadowBorder extends AbstractBorder {
        private static final int S = 4;
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = S; i > 0; i--) {
                g2.setColor(new Color(150, 160, 180, (int) (10.0 * (S - i) / S)));
                g2.drawRoundRect(x + i, y + i, w - 2 * i - 1, h - 2 * i - 1, 15, 15);
            }
            g2.setColor(BORDER); g2.drawRoundRect(x, y, w - 1, h - 1, 15, 15);
            g2.setColor(BG_CARD); g2.setClip(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, 15, 15)); g2.fillRect(x + 1, y + 1, w - 2, h - 2); g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(S, S, S, S); }
    }
}