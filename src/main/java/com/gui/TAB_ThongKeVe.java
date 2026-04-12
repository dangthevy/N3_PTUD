package com.gui;

import com.dao.DAO_ThongKeVe;
import com.formdev.flatlaf.FlatClientProperties;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

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

public class TAB_ThongKeVe extends JPanel {

    // =========================================================================
    // MÀU SẮC & FONT CHUẨN
    // =========================================================================
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

    private static final Color BTN_SUCCESS      = new Color(40, 167, 69);
    private static final Color BTN_SUCCESS_HVR  = new Color(33, 136, 56);

    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  18);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final String DATE_FMT = "dd/MM/yyyy";

    private enum BtnStyle { PRIMARY, SUCCESS }

    // THÀNH PHẦN GIAO DIỆN
    private JComboBox<String> cboTieuChi;
    private JComboBox<String> cboThongKe;
    private DatePickerField dcTuNgay;
    private DatePickerField dcDenNgay;
    private JButton btnThongKe;
    private JButton btnXuatExcel;

    private JLabel lblTongVe, lblDaSuDung, lblHetHan;

    private JTable tableTKVe;
    private DefaultTableModel tableModel;
    private DefaultPieDataset pieDataset;

    public TAB_ThongKeVe() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // KHU VỰC HEADER (TOP)
        JPanel topWrapper = new JPanel(new BorderLayout(0, 12));
        topWrapper.setOpaque(false);

        JLabel lblTitle = new JLabel("THỐNG KÊ VÉ BÁN RA");
        lblTitle.setFont(F_TITLE);
        lblTitle.setForeground(TEXT_DARK);
        topWrapper.add(lblTitle, BorderLayout.NORTH);

        // --- YÊU CẦU: Khung KPI nằm trên phần chức năng lọc ---
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        kpiPanel.setOpaque(false);

        lblTongVe = new JLabel("0", SwingConstants.CENTER);
        lblDaSuDung = new JLabel("0", SwingConstants.CENTER);
        lblHetHan = new JLabel("0", SwingConstants.CENTER);

        kpiPanel.add(createKpiCard("Tổng Vé Bán Ra", lblTongVe, new Color(0, 122, 255)));
        kpiPanel.add(createKpiCard("Vé Đã Sử Dụng", lblDaSuDung, new Color(40, 167, 69)));
        kpiPanel.add(createKpiCard("Vé Hủy/Hết Hạn", lblHetHan, new Color(220, 53, 69)));

        topWrapper.add(kpiPanel, BorderLayout.CENTER);

        // --- Bộ lọc chức năng ---
        JPanel filterCard = makeCard(new FlowLayout(FlowLayout.LEFT, 15, 12));

        filterCard.add(makeLabel("Thống kê theo:"));
        String[] tieuChi = {"Trạng thái vé", "Loại vé", "Tuyến đi"};
        cboTieuChi = makeCombo(tieuChi);
        filterCard.add(cboTieuChi);

        filterCard.add(makeLabel("Thời gian:"));
        String[] options = {"Tùy chọn", "Tuần này", "Tháng này", "Quý này", "Năm này"};
        cboThongKe = makeCombo(options);
        cboThongKe.setPreferredSize(new Dimension(110, 36));
        filterCard.add(cboThongKe);

        filterCard.add(makeLabel("Từ ngày:"));
        dcTuNgay = new DatePickerField("");
        dcTuNgay.setPreferredSize(new Dimension(148, 36));
        filterCard.add(dcTuNgay);

        filterCard.add(makeLabel("Đến ngày:"));
        dcDenNgay = new DatePickerField("");
        dcDenNgay.setPreferredSize(new Dimension(148, 36));
        filterCard.add(dcDenNgay);

        btnThongKe = makeBtn("Thống kê", BtnStyle.PRIMARY);
        btnThongKe.setPreferredSize(new Dimension(105, 36));
        btnXuatExcel = makeBtn("Xuất Excel", BtnStyle.SUCCESS);
        btnXuatExcel.setPreferredSize(new Dimension(105, 36));

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionGroup.setOpaque(false);
        actionGroup.add(btnThongKe);
        actionGroup.add(btnXuatExcel);

        filterCard.add(Box.createHorizontalStrut(10));
        filterCard.add(actionGroup);

        topWrapper.add(filterCard, BorderLayout.SOUTH);
        add(topWrapper, BorderLayout.NORTH);

        // ================= 2. KHU VỰC CHÍNH: BIỂU ĐỒ & BẢNG =================
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1.0;

        // --- A. BIỂU ĐỒ TRÒN ---
        JPanel chartCard = makeCard(new BorderLayout());
        chartCard.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        pieDataset = new DefaultPieDataset();
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Tỷ lệ % Số lượng Vé", pieDataset, true, true, false);

        // Làm đẹp biểu đồ tròn chuẩn Flat Design
        pieChart.setBackgroundPaint(BG_CARD);
        pieChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        pieChart.getTitle().setPaint(TEXT_DARK);

        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setBackgroundPaint(BG_CARD);
        plot.setOutlineVisible(false); // Bỏ viền đen
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(Color.WHITE); // Bỏ viền của nhãn
        plot.setLabelShadowPaint(null); // Bỏ bóng của nhãn
        plot.setShadowXOffset(0); // Bỏ bóng 3D của hình tròn
        plot.setShadowYOffset(0);
        plot.setLabelFont(F_CELL);

        ChartPanel jfreeChartPanel = new ChartPanel(pieChart);
        jfreeChartPanel.setOpaque(false);
        chartCard.add(jfreeChartPanel, BorderLayout.CENTER);

        gc.gridx = 0; gc.weightx = 0.45;
        gc.insets = new Insets(0, 0, 0, 8);
        centerWrapper.add(chartCard, gc);

        // --- B. BẢNG CHI TIẾT ---
        JPanel tableCard = makeCard(new BorderLayout());
        String[] cols = {"STT", "Tiêu chí", "Số lượng vé", "Tỷ lệ (%)"};
        tableModel = new DefaultTableModel(cols, 0);
        tableTKVe = buildTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableTKVe);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CARD);
        styleScrollBar(scrollPane.getVerticalScrollBar());

        tableCard.add(scrollPane, BorderLayout.CENTER);

        gc.gridx = 1; gc.weightx = 0.55;
        gc.insets = new Insets(0, 8, 0, 0);
        centerWrapper.add(tableCard, gc);

        add(centerWrapper, BorderLayout.CENTER);

        // ================= GÁN SỰ KIỆN =================
        btnThongKe.addActionListener(e -> loadDuLieuThongKe());

        btnXuatExcel.addActionListener(e -> xuatFileExcel());

        cboThongKe.addActionListener(e -> {
            boolean isCustom = cboThongKe.getSelectedIndex() == 0; // index 0 là "Tùy chọn"

            // 1. Bật/Tắt ô nhập ngày
            dcTuNgay.setEnabledField(isCustom);
            dcDenNgay.setEnabledField(isCustom);

            // 2. Xóa trắng ngày hiển thị mỗi khi thay đổi lựa chọn thời gian
            dcTuNgay.setDate("");
            dcDenNgay.setDate("");

            // 3. Nếu chọn "Tùy chọn", reset luôn Bảng, Biểu đồ và thẻ KPI về 0
            if (isCustom) {
                tableModel.setRowCount(0); // Xóa dữ liệu bảng
                pieDataset.clear();        // Xóa dữ liệu biểu đồ tròn

                // Trả các thẻ số liệu về 0
                lblTongVe.setText("0");
                lblDaSuDung.setText("0");
                lblHetHan.setText("0");
            }
        });
    }

    private JPanel createKpiCard(String title, JLabel lblValue, Color color) {
        JPanel card = makeCard(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_MID);

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValue.setForeground(color);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    // XỬ LÝ LẤY DỮ LIỆU TỪ DATABASE
    private void loadDuLieuThongKe() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT);
        Date fromDate = null;
        Date toDate = null;

        Calendar calToday = Calendar.getInstance();
        calToday.set(Calendar.HOUR_OF_DAY, 0); calToday.set(Calendar.MINUTE, 0);
        calToday.set(Calendar.SECOND, 0); calToday.set(Calendar.MILLISECOND, 0);
        Date today = calToday.getTime();

        int type = cboThongKe.getSelectedIndex();
        Calendar cal = Calendar.getInstance();

        try {
            if (type == 0) { // Tùy chọn
                String sFrom = dcTuNgay.getDate();
                String sTo = dcDenNgay.getDate();
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

            // KIỂM TRA RÀNG BUỘC
            if (fromDate != null && fromDate.after(today)) {
                JOptionPane.showMessageDialog(this, "Từ ngày không được lớn hơn ngày hiện tại!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }
            if (toDate != null && toDate.after(today)) {
                JOptionPane.showMessageDialog(this, "Đến ngày không được lớn hơn ngày hiện tại!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }
            if (fromDate != null && toDate != null && toDate.before(fromDate)) {
                JOptionPane.showMessageDialog(this, "Đến ngày phải lớn hơn hoặc bằng Từ ngày!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày tháng!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
        }

        // KẾT NỐI DAO
        DAO_ThongKeVe dao = new DAO_ThongKeVe();

        // Cập nhật KPI
        DecimalFormat dfKpi = new DecimalFormat("#,###");
        int[] kpi = dao.getKpiData(fromDate, toDate);
        lblTongVe.setText(dfKpi.format(kpi[0]));
        lblDaSuDung.setText(dfKpi.format(kpi[1]));
        lblHetHan.setText(dfKpi.format(kpi[2]));

        // Cập nhật Bảng và Biểu đồ
        int tieuChiIndex = cboTieuChi.getSelectedIndex();
        List<Object[]> listData = dao.getChiTietThongKe(tieuChiIndex, fromDate, toDate);

        tableModel.setRowCount(0);
        pieDataset.clear();

        if (listData == null || listData.isEmpty() || kpi[0] == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu vé trong khoảng thời gian này!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Tính tổng để chia phần trăm
        double tongSoLuong = 0;
        for (Object[] row : listData) {
            tongSoLuong += (int) row[1];
        }

        int stt = 1;
        for (Object[] row : listData) {
            String tieuChi = (String) row[0];
            int soLuong = (int) row[1];
            double tyLe = (soLuong / tongSoLuong) * 100;

            // Thêm vào bảng
            tableModel.addRow(new Object[]{
                    stt++,
                    tieuChi,
                    dfKpi.format(soLuong),
                    String.format("%.1f%%", tyLe)
            });

            // Thêm vào biểu đồ tròn
            pieDataset.setValue(tieuChi, soLuong);
        }
    }

    // HÀM XUẤT EXCEL
    private void xuatFileExcel() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất Excel!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file Excel");
        fileChooser.setSelectedFile(new File("ThongKeVe_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".xlsx")) filePath += ".xlsx";

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Chi Tiet Ve");

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

                for (int i = 0; i < tableModel.getColumnCount(); i++) sheet.autoSizeColumn(i);

                try (FileOutputStream out = new FileOutputStream(filePath)) {
                    workbook.write(out);
                    JOptionPane.showMessageDialog(this, "Xuất file Excel thành công!\nLưu tại: " + filePath, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // HELPER UI
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
        cb.setPreferredSize(new Dimension(130, 36)); return cb;
    }
    private JButton makeBtn(String text, BtnStyle style) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (style == BtnStyle.PRIMARY) g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT);
                else g2.setColor(getModel().isRollover() ? BTN_SUCCESS_HVR : BTN_SUCCESS);
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
        t.setRowHeight(36); t.setFont(F_CELL); t.setBackground(BG_CARD);
        t.setSelectionBackground(new Color(0xDDEEFF)); t.setSelectionForeground(TEXT_DARK);
        t.setGridColor(BORDER); t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setFocusable(false); t.setIntercellSpacing(new Dimension(0, 0));

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

        DefaultTableCellRenderer r = new DefaultTableCellRenderer(); r.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
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

    // =========================================================================
    // DATE PICKER CUSTOM
    // =========================================================================
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
            MouseAdapter ma=new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){ if(isEnabled) toggle(); }};
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