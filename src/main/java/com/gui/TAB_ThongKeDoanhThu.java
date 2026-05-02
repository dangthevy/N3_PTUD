package com.gui;

import com.dao.DAO_ThongKeDoanhThu;
import com.formdev.flatlaf.FlatClientProperties;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class TAB_ThongKeDoanhThu extends JPanel {
    // Khai báo màu sắc và font
    private static final Color BG_PAGE      = new Color(0xF4F7FB);
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color ACCENT       = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR   = new Color(0x2270CC);
    private static final Color ACCENT_FOC   = new Color(0x4D9DE0);
    private static final Color TEXT_DARK    = new Color(0x1E2B3C);
    private static final Color TEXT_MID     = new Color(0x5A6A7D);
    private static final Color TEXT_LIGHT   = new Color(0xA0AEC0);
    private static final Color BORDER       = new Color(0xE2EAF4);
    private static final Color ROW_ALT      = new Color(0xF7FAFF);
    private static final Color TH_BG        = new Color(0xE8F0FB);

    // Màu thành công cho nút Xuất Excel
    private static final Color BTN_SUCCESS      = new Color(40, 167, 69);
    private static final Color BTN_SUCCESS_HVR  = new Color(33, 136, 56);

    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  18);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final String DATE_FMT = "dd/MM/yyyy";

    private enum BtnStyle { PRIMARY, SUCCESS }

    private JComboBox<String> cboThongKe;
    private DatePickerField dcNgayBD;
    private DatePickerField dcNgayKT;
    private JButton btnThongKe;
    private JButton btnXuatExcel;

    private JTable tableTKDT;
    private DefaultTableModel tableModel;
    private DefaultCategoryDataset chartDataset;
    private JLabel lblTongHoaDon;
    private JLabel lblTongDoanhThu;

    public TAB_ThongKeDoanhThu() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // --- Header và filter (top) ---
        JPanel topWrapper = new JPanel(new BorderLayout(0, 12));
        topWrapper.setOpaque(false);

        JLabel lblTitle = new JLabel("THỐNG KÊ DOANH THU");
        lblTitle.setFont(F_TITLE);
        lblTitle.setForeground(ACCENT);
        topWrapper.add(lblTitle, BorderLayout.CENTER);

        JPanel kpiPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        lblTongHoaDon = new JLabel("0");
        lblTongDoanhThu = new JLabel("0 đ");

        kpiPanel.add(createKpiCard("TỔNG HÓA ĐƠN", lblTongHoaDon, ACCENT));
        kpiPanel.add(createKpiCard("TỔNG DOANH THU", lblTongDoanhThu, new Color(40, 167, 69)));

        topWrapper.add(kpiPanel, BorderLayout.NORTH);

        JPanel filterCard = makeCard(new FlowLayout(FlowLayout.LEFT, 15, 12));

        filterCard.add(makeLabel("Thời gian:"));
        String[] options = {"Tùy chọn", "Tuần này", "Tháng này", "Quý này", "Năm này"};
        cboThongKe = makeCombo(options);
        cboThongKe.setPreferredSize(new Dimension(110, 36));
        filterCard.add(cboThongKe);

        filterCard.add(makeLabel("Ngày bắt đầu:"));
        dcNgayBD = new DatePickerField("");
        dcNgayBD.setPreferredSize(new Dimension(148, 36));
        filterCard.add(dcNgayBD);

        filterCard.add(makeLabel("Ngày kết thúc:"));
        dcNgayKT = new DatePickerField("");
        dcNgayKT.setPreferredSize(new Dimension(148, 36));
        filterCard.add(dcNgayKT);

        btnThongKe = makeBtn("Thống kê", BtnStyle.PRIMARY);
        btnThongKe.setPreferredSize(new Dimension(105, 36));
        btnXuatExcel = makeBtn("Xuất Excel", BtnStyle.SUCCESS);
        btnXuatExcel.setPreferredSize(new Dimension(105, 36));

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionGroup.setOpaque(false);
        actionGroup.add(btnThongKe);
        actionGroup.add(btnXuatExcel);

        filterCard.add(Box.createHorizontalStrut(20)); // Tạo khoảng cách
        filterCard.add(actionGroup);

        topWrapper.add(filterCard, BorderLayout.SOUTH);
        add(topWrapper, BorderLayout.NORTH);

        // ================= BIỂU ĐỒ & BẢNG (CENTER) =================
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 16));
        centerWrapper.setOpaque(false);

        // --- A. BIỂU ĐỒ ---
        JPanel chartCard = makeCard(new BorderLayout());
        chartCard.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        chartDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart(
                "Biểu Đồ Doanh Thu", "Ngày", "Doanh thu (VNĐ)",
                chartDataset, PlotOrientation.VERTICAL,
                false, true, false);

        // Làm đẹp JFreeChart theo phong cách Flat
        barChart.setBackgroundPaint(BG_CARD);
        barChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        barChart.getTitle().setPaint(TEXT_DARK);

        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(BG_CARD);
        plot.setRangeGridlinePaint(BORDER);
        plot.setOutlineVisible(false); // Bỏ viền đen bao quanh plot
        plot.getDomainAxis().setTickLabelFont(F_CELL);
        plot.getRangeAxis().setTickLabelFont(F_CELL);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, ACCENT);
        renderer.setBarPainter(new StandardBarPainter()); // Bỏ hiệu ứng 3D gradient lỗi thời
        renderer.setShadowVisible(false); // Bỏ bóng mặc định của cột
        renderer.setItemMargin(0.2); // Khoảng cách giữa các cột

        ChartPanel jfreeChartPanel = new ChartPanel(barChart);
        jfreeChartPanel.setOpaque(false);
        chartCard.add(jfreeChartPanel, BorderLayout.CENTER);

        // --- B. BẢNG CHI TIẾT ---
        JPanel tableCard = makeCard(new BorderLayout());
        // ===== CẬP NHẬT: Đổi tên cột từ "Số vé bán ra" thành "Số hóa đơn" =====
        String[] cols = {"STT", "Ngày", "Số hóa đơn", "Doanh thu (VNĐ)"};
        tableModel = new DefaultTableModel(cols, 0);
        tableTKDT = buildTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableTKDT);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CARD);
        styleScrollBar(scrollPane.getVerticalScrollBar());

        tableCard.add(scrollPane, BorderLayout.CENTER);
        tableCard.setPreferredSize(new Dimension(0, 250)); // Giới hạn chiều cao của bảng

        centerWrapper.add(chartCard, BorderLayout.CENTER);
        centerWrapper.add(tableCard, BorderLayout.SOUTH);

        add(centerWrapper, BorderLayout.CENTER);

        // ================= GÁN SỰ KIỆN =================
        btnThongKe.addActionListener(e -> loadDuLieuThongKe());

        btnXuatExcel.addActionListener(e -> xuatFileExcel());

        cboThongKe.addActionListener(e -> {
            boolean isCustom = cboThongKe.getSelectedIndex() == 0; // index 0 là "Tùy chọn"

            // 1. Bật/Tắt ô nhập ngày dựa trên việc có chọn "Tùy chọn" hay không
            dcNgayBD.setEnabledField(isCustom);
            dcNgayKT.setEnabledField(isCustom);

            // 2. Luôn xóa trắng ngày hiển thị mỗi khi thay đổi lựa chọn thời gian
            dcNgayBD.setDate("");
            dcNgayKT.setDate("");

            // 3. Nếu chọn "Tùy chọn", reset luôn Bảng và Biểu đồ về giao diện ban đầu
            if (isCustom) {
                tableModel.setRowCount(0); // Xóa dữ liệu bảng
                chartDataset.clear();      // Xóa dữ liệu biểu đồ
                lblTongHoaDon.setText("0");
                lblTongDoanhThu.setText("0 đ");
            }
        });
    }

    private String formatCurrencyVnd(double amount) {
        return new DecimalFormat("#,###").format(amount) + " đ";
    }

    private JPanel createKpiCard(String title, JLabel lblValue, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(TEXT_MID);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));

        lblValue.setForeground(color);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    // HÀM XỬ LÝ LẤY DỮ LIỆU TỪ DATABASE
    private void loadDuLieuThongKe() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT);
        Date fromDate = null;
        Date toDate = null;

        // Lấy ngày hiện tại (Bỏ đi giờ/phút/giây để so sánh chính xác)
        Calendar calToday = Calendar.getInstance();
        calToday.set(Calendar.HOUR_OF_DAY, 0); calToday.set(Calendar.MINUTE, 0);
        calToday.set(Calendar.SECOND, 0); calToday.set(Calendar.MILLISECOND, 0);
        Date today = calToday.getTime();

        int type = cboThongKe.getSelectedIndex();
        Calendar cal = Calendar.getInstance();

        try {
            if (type == 0) { // Tùy chọn
                String sFrom = dcNgayBD.getDate();
                String sTo = dcNgayKT.getDate();
                if (!sFrom.isEmpty()) fromDate = sdf.parse(sFrom);
                if (!sTo.isEmpty()) toDate = sdf.parse(sTo);
            } else if (type == 1) { // Tuần này
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                fromDate = cal.getTime();
                toDate = today;
            } else if (type == 2) { // Tháng này
                cal.set(Calendar.DAY_OF_MONTH, 1);
                fromDate = cal.getTime();
                toDate = today;
            } else if (type == 3) { // Quý này
                int quarter = cal.get(Calendar.MONTH) / 3;
                cal.set(Calendar.MONTH, quarter * 3);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                fromDate = cal.getTime();
                toDate = today;
            } else if (type == 4) { // Năm này
                cal.set(Calendar.DAY_OF_YEAR, 1);
                fromDate = cal.getTime();
                toDate = today;
            }

            // KIỂM TRA RÀNG BUỘC NGÀY THÁNG
            if (fromDate != null && fromDate.after(today)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được lớn hơn ngày kết thúc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (toDate != null && toDate.after(today)) {
                JOptionPane.showMessageDialog(this, "Ngày kết thúc không được lớn hơn ngày hiện tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (fromDate != null && toDate != null && toDate.before(fromDate)) {
                JOptionPane.showMessageDialog(this, "Thời gian không hợp lệ: Ngày kết thúc phải lớn hơn hoặc bằng ngày hiện tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày tháng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // KẾT NỐI DAO VÀ ĐỔ DỮ LIỆU
        DAO_ThongKeDoanhThu dao = new DAO_ThongKeDoanhThu();
        List<Object[]> listData = dao.getDsTheoDoanhThu(fromDate, toDate);

        tableModel.setRowCount(0);
        chartDataset.clear();
        lblTongHoaDon.setText("0");
        lblTongDoanhThu.setText("0 đ");

        if (listData == null || listData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu doanh thu trong khoảng thời gian này!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DecimalFormat df = new DecimalFormat("#,###");
        SimpleDateFormat shortSdf = new SimpleDateFormat("dd/MM");
        int tongHoaDon = 0;
        double tongDoanhThu = 0;

        int stt = 1;
        for (Object[] row : listData) {
            Date ngay = (Date) row[0];
            int soHoaDon = (int) row[1]; // Đã thay đổi thành hóa đơn
            double doanhThu = (double) row[2];
            tongHoaDon += soHoaDon;
            tongDoanhThu += doanhThu;

            // Thêm vào Bảng
            tableModel.addRow(new Object[]{
                    stt++,
                    sdf.format(ngay),
                    soHoaDon, // Hiển thị số hóa đơn
                    formatCurrencyVnd(doanhThu)
            });

            // Thêm vào Biểu đồ
            chartDataset.addValue(doanhThu, "Doanh thu", shortSdf.format(ngay));
        }

        lblTongHoaDon.setText(df.format(tongHoaDon));
        lblTongDoanhThu.setText(formatCurrencyVnd(tongDoanhThu));
    }

    // HÀM XUẤT FILE EXCEL
    private void xuatFileExcel() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất Excel!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file Excel");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("ThongKeDoanhThu_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Doanh Thu");

                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                CellStyle headerCellStyle = workbook.createCellStyle();
                headerCellStyle.setFont(headerFont);

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(tableModel.getColumnName(i));
                    cell.setCellStyle(headerCellStyle);
                }

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object val = tableModel.getValueAt(i, j);
                        row.createCell(j).setCellValue(val != null ? val.toString() : "");
                    }
                }

                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream out = new FileOutputStream(filePath)) {
                    workbook.write(out);
                    JOptionPane.showMessageDialog(this, "Xuất file Excel thành công!\nLưu tại: " + filePath, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // HELPER UI CỦA HỆ THỐNG
    private JPanel makeCard(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(BG_CARD); p.setBorder(new ShadowBorder()); return p;
    }

    private JLabel makeLabel(String txt) {
        JLabel lbl = new JLabel(txt); lbl.setFont(F_LABEL); lbl.setForeground(TEXT_MID); return lbl;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(F_CELL); cb.setBackground(new Color(0xF8FAFD)); cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        cb.setPreferredSize(new Dimension(130, 36));
        return cb;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (style == BtnStyle.PRIMARY) {
                    g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT);
                } else if (style == BtnStyle.SUCCESS) {
                    g2.setColor(getModel().isRollover() ? BTN_SUCCESS_HVR : BTN_SUCCESS);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_LABEL); b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(130, 36));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        t.setRowHeight(36); t.setFont(F_CELL);
        t.setBackground(BG_CARD); t.setSelectionBackground(new Color(0xDDEEFF));
        t.setSelectionForeground(TEXT_DARK); t.setGridColor(BORDER);
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false); t.setFocusable(false);
        t.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(LEFT); }
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                l.setOpaque(true); l.setBackground(ACCENT); l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI",Font.BOLD,13)); l.setBorder(BorderFactory.createEmptyBorder(0,12,0,6)); return l;
            }
        });
        h.setPreferredSize(new Dimension(0, 40)); h.setReorderingAllowed(false);

        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
        for (int i = 0; i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(r);
        return t;
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI(){
            @Override protected void configureScrollBarColors(){thumbColor=new Color(0xC0D4EE);trackColor=BG_PAGE;}
            @Override protected JButton createDecreaseButton(int o){return zBtn();}
            @Override protected JButton createIncreaseButton(int o){return zBtn();}
            private JButton zBtn(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });
        sb.putClientProperty(FlatClientProperties.SCROLL_BAR_SHOW_BUTTONS, false);
    }

    // =========================================================================
    // SHADOW BORDER
    // =========================================================================
    private static class ShadowBorder extends AbstractBorder {
        private static final int S = 4;
        @Override public void paintBorder(Component c,Graphics g,int x,int y,int w,int h){
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            for(int i=S;i>0;i--){g2.setColor(new Color(100,140,200,(int)(20.0*(S-i)/S)));g2.drawRoundRect(x+i,y+i,w-2*i-1,h-2*i-1,12,12);}
            g2.setColor(new Color(0xE2EAF4));g2.drawRoundRect(x,y,w-1,h-1,12,12);
            g2.setColor(BG_CARD);g2.setClip(new RoundRectangle2D.Float(x+1,y+1,w-2,h-2,12,12));g2.fillRect(x+1,y+1,w-2,h-2);g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c){return new Insets(S,S,S,S);}
        @Override public Insets getBorderInsets(Component c,Insets ins){ins.set(S,S,S,S);return ins;}
    }

    // DATE PICKER CUSTOM (Mang sang từ TAB_LichTrinh)
    private class DatePickerField extends JPanel {
        private final JTextField   txt;
        private final Calendar     cal;
        private JPanel             pnlGrid;
        private JComboBox<String>  cbThang;
        private JComboBox<Integer> cbNam;
        private JWindow            popup;
        private boolean            isEnabled = true;

        private static final String[] TEN_THANG={"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6","Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};
        private static final String[] TEN_THU={"T2","T3","T4","T5","T6","T7","CN"};

        DatePickerField(String init){
            setLayout(new BorderLayout()); setOpaque(false);
            cal=Calendar.getInstance();
            if(init!=null&&!init.isEmpty()){try{cal.setTime(new SimpleDateFormat(DATE_FMT).parse(init));}catch(Exception ignored){}}
            String disp=init!=null&&!init.isEmpty()?init:new SimpleDateFormat(DATE_FMT).format(cal.getTime());

            txt=new JTextField(disp); txt.setFont(F_CELL); txt.setForeground(TEXT_DARK);
            txt.setBackground(new Color(0xF8FAFD)); txt.setEditable(false);
            txt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1,true),BorderFactory.createEmptyBorder(6,10,6,36)));

            JLabel ico=new JLabel(){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isEnabled ? TEXT_MID : TEXT_LIGHT);
                    int cx=getWidth()/2,cy=getHeight()/2;g2.drawRoundRect(cx-8,cy-7,16,14,3,3);g2.drawLine(cx-8,cy-4,cx+8,cy-4);g2.drawLine(cx-4,cy-10,cx-4,cy-5);g2.drawLine(cx+4,cy-10,cx+4,cy-5);
                    g2.fillOval(cx-6,cy-1,3,3);g2.fillOval(cx-1,cy-1,3,3);g2.fillOval(cx+4,cy-1,3,3);g2.fillOval(cx-6,cy+3,3,3);g2.fillOval(cx-1,cy+3,3,3);g2.dispose();
                }
            };
            ico.setPreferredSize(new Dimension(32,36)); ico.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JPanel wrap=new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.add(txt,BorderLayout.CENTER); wrap.add(ico,BorderLayout.EAST); add(wrap,BorderLayout.CENTER);

            MouseAdapter ma=new MouseAdapter(){
                @Override public void mouseClicked(MouseEvent e){ if(isEnabled) toggle(); }
            };
            txt.addMouseListener(ma); ico.addMouseListener(ma);
        }

        public void setEnabledField(boolean enabled) {
            this.isEnabled = enabled;
            txt.setBackground(enabled ? new Color(0xF8FAFD) : new Color(0xEEF2F8));
            txt.setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            repaint();
        }

        public void setDate(String date) { txt.setText(date); }
        public String getDate() { return txt.getText(); }

        private void toggle(){if(popup!=null&&popup.isVisible()){popup.dispose();popup=null;return;}showPop();}
        private void showPop(){
            popup=new JWindow(SwingUtilities.getWindowAncestor(this));popup.setLayout(new BorderLayout());
            JPanel p=new JPanel(new BorderLayout(0,6));p.setBackground(BG_CARD);
            p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1),BorderFactory.createEmptyBorder(12,12,12,12)));
            p.add(navBar(),BorderLayout.NORTH);pnlGrid=new JPanel(new GridLayout(0,7,2,2));pnlGrid.setBackground(BG_CARD);p.add(pnlGrid,BorderLayout.CENTER);
            fillGrid();popup.add(p);popup.pack();popup.setSize(Math.max(280,popup.getWidth()),popup.getHeight());
            Point loc=txt.getLocationOnScreen();popup.setLocation(loc.x,loc.y+txt.getHeight()+2);popup.setVisible(true);
            popup.addWindowFocusListener(new java.awt.event.WindowFocusListener(){
                @Override public void windowGainedFocus(java.awt.event.WindowEvent e){}
                @Override public void windowLostFocus(java.awt.event.WindowEvent e){if(popup!=null){popup.dispose();popup=null;}}
            });
        }
        private JPanel navBar(){
            JPanel nav=new JPanel(new BorderLayout(4,0));nav.setBackground(BG_CARD);
            JButton prev=navBtn("<");JButton next=navBtn(">");
            prev.addActionListener(e->{cal.add(Calendar.MONTH,-1);cbThang.setSelectedIndex(cal.get(Calendar.MONTH));cbNam.setSelectedItem(cal.get(Calendar.YEAR));fillGrid();});
            next.addActionListener(e->{cal.add(Calendar.MONTH, 1);cbThang.setSelectedIndex(cal.get(Calendar.MONTH));cbNam.setSelectedItem(cal.get(Calendar.YEAR));fillGrid();});
            cbThang=new JComboBox<>(TEN_THANG);cbThang.setFont(F_SMALL);cbThang.setSelectedIndex(cal.get(Calendar.MONTH));cbThang.setPreferredSize(new Dimension(82,26));
            cbThang.addActionListener(e->{cal.set(Calendar.MONTH,cbThang.getSelectedIndex());fillGrid();});
            int y=Calendar.getInstance().get(Calendar.YEAR);Integer[] yrs=new Integer[16];for(int i=0;i<16;i++)yrs[i]=y-5+i;
            cbNam=new JComboBox<>(yrs);cbNam.setFont(F_SMALL);cbNam.setSelectedItem(cal.get(Calendar.YEAR));cbNam.setPreferredSize(new Dimension(60,26));
            cbNam.addActionListener(e->{if(cbNam.getSelectedItem()!=null){cal.set(Calendar.YEAR,(Integer)cbNam.getSelectedItem());fillGrid();}});
            JPanel ctr=new JPanel(new FlowLayout(FlowLayout.CENTER,4,0));ctr.setBackground(BG_CARD);ctr.add(cbThang);ctr.add(cbNam);
            nav.add(prev,BorderLayout.WEST);nav.add(ctr,BorderLayout.CENTER);nav.add(next,BorderLayout.EAST);return nav;
        }
        private void fillGrid(){
            pnlGrid.removeAll();
            for(String th:TEN_THU){JLabel l=new JLabel(th,SwingConstants.CENTER);l.setFont(new Font("Segoe UI",Font.BOLD,11));l.setPreferredSize(new Dimension(32,24));l.setForeground(TEXT_MID);pnlGrid.add(l);}
            Calendar tmp=(Calendar)cal.clone();tmp.set(Calendar.DAY_OF_MONTH,1);int first=(tmp.get(Calendar.DAY_OF_WEEK)+5)%7;
            Calendar today=Calendar.getInstance();int todayD=today.get(Calendar.DAY_OF_MONTH);
            boolean sm=today.get(Calendar.MONTH)==cal.get(Calendar.MONTH)&&today.get(Calendar.YEAR)==cal.get(Calendar.YEAR);
            int chosen=-1;
            try{Calendar c=Calendar.getInstance();c.setTime(new SimpleDateFormat(DATE_FMT).parse(txt.getText()));if(c.get(Calendar.MONTH)==cal.get(Calendar.MONTH)&&c.get(Calendar.YEAR)==cal.get(Calendar.YEAR))chosen=c.get(Calendar.DAY_OF_MONTH);}catch(Exception ignored){}
            for(int i=0;i<first;i++)pnlGrid.add(new JLabel());
            int days=cal.getActualMaximum(Calendar.DAY_OF_MONTH);final int fc=chosen;
            for(int d=1;d<=days;d++){
                final int nd=d;boolean isT=sm&&d==todayD;boolean isSel=d==fc;
                JButton b=new JButton(String.valueOf(d)){
                    @Override protected void paintComponent(Graphics g){
                        Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                        if(isSel){g2.setColor(ACCENT);g2.fillOval(1,1,getWidth()-2,getHeight()-2);}
                        else if(getModel().isRollover()){g2.setColor(new Color(0xDDEEFF));g2.fillOval(1,1,getWidth()-2,getHeight()-2);}
                        else if(isT){g2.setColor(new Color(0xE8F1FB));g2.fillOval(1,1,getWidth()-2,getHeight()-2);}
                        g2.dispose();super.paintComponent(g);
                    }
                };
                b.setFont(new Font("Segoe UI",isT?Font.BOLD:Font.PLAIN,11));b.setForeground(isSel?Color.WHITE:isT?ACCENT:TEXT_DARK);
                b.setPreferredSize(new Dimension(32,32));b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setMargin(new Insets(0,0,0,0));
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.addActionListener(e->{cal.set(Calendar.DAY_OF_MONTH,nd);txt.setText(new SimpleDateFormat(DATE_FMT).format(cal.getTime()));if(popup!=null){popup.dispose();popup=null;}});
                pnlGrid.add(b);
            }
            pnlGrid.revalidate();pnlGrid.repaint();
        }
        private JButton navBtn(String t){
            JButton b=new JButton(t);b.setFont(new Font("Segoe UI",Font.BOLD,14));b.setForeground(ACCENT);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setMargin(new Insets(0,0,0,0));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setPreferredSize(new Dimension(32,32));return b;
        }
    }
}