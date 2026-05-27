package com.gui;

import com.dao.DAO_ThongKeTuyen;
import com.formdev.flatlaf.FlatClientProperties;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TAB_ThongKeTuyen extends JPanel {

    private static final Color BG_PAGE = new Color(0xF4F7FB);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color ACCENT = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR = new Color(0x2270CC);
    private static final Color TEXT_DARK = new Color(0x1E2B3C);
    private static final Color TEXT_MID = new Color(0x5A6A7D);
    private static final Color TEXT_LIGHT = new Color(0xA0AEC0);
    private static final Color BORDER = new Color(0xE2EAF4);
    private static final Color ROW_ALT = new Color(0xF7FAFF);
    private static final Color BTN_SUCCESS = new Color(40, 167, 69);
    private static final Color BTN_SUCCESS_HVR = new Color(33, 136, 56);
    private static final Color CLR_CHUA = new Color(0xE67E22);
    private static final Color CLR_DANG = new Color(0x27AE60);
    private static final Color CLR_HOAN = new Color(0x2980B9);
    private static final Color CLR_HUY = new Color(0xC0392B);

    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final String DATE_FMT = "dd/MM/yyyy";

    private enum BtnStyle { PRIMARY, SUCCESS }

    private JComboBox<String> cboThongKe;
    private JComboBox<String> cboTuyen;
    private JComboBox<String> cboTrangThai;
    private DatePickerField dcNgayBD;
    private DatePickerField dcNgayKT;
    private JButton btnThongKe;
    private JButton btnXuatExcel;

    private JTable tableTKTuyen;
    private DefaultTableModel tableModel;
    private JLabel lblTongChuyen;
    private JLabel lblChuyenDaHoanThanh;
    private JLabel lblChuyenChuaHoanThanh;
    private final Map<String, String> mapTuyen = new LinkedHashMap<>();

    public TAB_ThongKeTuyen() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel topWrapper = new JPanel(new BorderLayout(0, 12));
        topWrapper.setOpaque(false);

        JLabel lblTitle = new JLabel("THỐNG KÊ TUYẾN");
        lblTitle.setFont(F_TITLE);
        lblTitle.setForeground(ACCENT);
        topWrapper.add(lblTitle, BorderLayout.CENTER);

        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        lblTongChuyen = new JLabel("0");
        lblChuyenDaHoanThanh = new JLabel("0");
        lblChuyenChuaHoanThanh = new JLabel("0");
        kpiPanel.add(createKpiCard("TỔNG CHUYẾN TÀU", lblTongChuyen, ACCENT, "train_total"));
        kpiPanel.add(createKpiCard("CHUYẾN TÀU ĐÃ HOÀN THÀNH", lblChuyenDaHoanThanh, new Color(40, 167, 69), "completed"));
        kpiPanel.add(createKpiCard("CHUYẾN TÀU CHƯA HOÀN THÀNH", lblChuyenChuaHoanThanh, new Color(255, 140, 0), "pending"));
        topWrapper.add(kpiPanel, BorderLayout.NORTH);

        JPanel filterCard = makeCard(new FlowLayout(FlowLayout.LEFT, 15, 12));
        filterCard.add(makeLabel("Thời gian:"));
        cboThongKe = makeCombo(new String[]{"Tùy chọn", "Tuần này", "Tháng này", "Quý này", "Năm này"});
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
        btnThongKe.setPreferredSize(new Dimension(130, 36));
        btnThongKe.setIcon(createButtonIcon(BtnStyle.PRIMARY));
        btnThongKe.setIconTextGap(8);

        btnXuatExcel = makeBtn("Xuất Excel", BtnStyle.SUCCESS);
        btnXuatExcel.setPreferredSize(new Dimension(130, 36));
        btnXuatExcel.setIcon(createButtonIcon(BtnStyle.SUCCESS));
        btnXuatExcel.setIconTextGap(8);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionGroup.setOpaque(false);
        actionGroup.add(btnThongKe);
        actionGroup.add(btnXuatExcel);
        filterCard.add(Box.createHorizontalStrut(20));
        filterCard.add(actionGroup);

        JPanel tuyenFilterCard = makeCard(new FlowLayout(FlowLayout.LEFT, 15, 12));
        tuyenFilterCard.add(makeLabel("Tuyến:"));
        cboTuyen = makeCombo(new String[]{"Tất cả tuyến"});
        cboTuyen.setPreferredSize(new Dimension(220, 36));
        tuyenFilterCard.add(cboTuyen);
        tuyenFilterCard.add(makeLabel("Trạng thái:"));
        cboTrangThai = makeCombo(new String[]{"Tất cả trạng thái", "Chưa khởi hành", "Đang khởi hành", "Đã hoàn thành"});
        cboTrangThai.setPreferredSize(new Dimension(190, 36));
        tuyenFilterCard.add(cboTrangThai);

        JPanel filterSection = new JPanel();
        filterSection.setOpaque(false);
        filterSection.setLayout(new BoxLayout(filterSection, BoxLayout.Y_AXIS));
        filterSection.add(filterCard);
        filterSection.add(Box.createVerticalStrut(10));
        filterSection.add(tuyenFilterCard);

        topWrapper.add(filterSection, BorderLayout.SOUTH);
        add(topWrapper, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);

        JPanel tableCard = makeCard(new BorderLayout());
        String[] cols = {
                "STT",
                "Tên chuyến",
                "Ngày khởi hành",
                "Giờ khởi hành",
                "Ngày đến",
                "Giờ đến",
                "Trạng thái"
        };
        tableModel = new DefaultTableModel(cols, 0);
        tableTKTuyen = buildTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableTKTuyen);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CARD);
        styleScrollBar(scrollPane.getVerticalScrollBar());
        tableCard.add(scrollPane, BorderLayout.CENTER);
        centerWrapper.add(tableCard, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        btnThongKe.addActionListener(e -> loadDuLieuThongKe());
        btnXuatExcel.addActionListener(e -> xuatFileExcelDongBo());
        cboThongKe.addActionListener(e -> {
            boolean isCustom = cboThongKe.getSelectedIndex() == 0;
            dcNgayBD.setEnabledField(isCustom);
            dcNgayKT.setEnabledField(isCustom);
            dcNgayBD.setDate("");
            dcNgayKT.setDate("");
            if (isCustom) {
                tableModel.setRowCount(0);
                lblTongChuyen.setText("0");
                lblChuyenDaHoanThanh.setText("0");
                lblChuyenChuaHoanThanh.setText("0");
            }
        });

        loadDanhSachTuyen();
        loadDuLieuThongKe();
    }

    private JPanel createKpiCard(String title, JLabel lblValue, Color color, String iconType) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));


        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(TEXT_MID);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblValue.setForeground(color);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel ico = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;

                if ("train_total".equals(iconType)) {
                    g2.drawRoundRect(cx - 8, cy - 8, 16, 12, 3, 3);
                    g2.drawLine(cx - 4, cy - 8, cx - 4, cy - 3);
                    g2.drawLine(cx + 4, cy - 8, cx + 4, cy - 3);
                    g2.drawOval(cx - 6, cy + 4, 3, 3);
                    g2.drawOval(cx + 3, cy + 4, 3, 3);
                } else if ("completed".equals(iconType)) {
                    g2.drawOval(cx - 7, cy - 7, 14, 14);
                    g2.drawLine(cx - 4, cy, cx - 1, cy + 3);
                    g2.drawLine(cx - 1, cy + 3, cx + 5, cy - 3);
                } else {
                    g2.drawOval(cx - 7, cy - 7, 14, 14);
                    g2.drawLine(cx, cy - 3, cx, cy + 2);
                    g2.drawLine(cx, cy + 5, cx, cy + 5);
                }
                g2.dispose();
            }
        };
        ico.setPreferredSize(new Dimension(38, 38));
        ico.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(lblTitle, BorderLayout.WEST);
        topRow.add(ico, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private void loadDuLieuThongKe() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT);
        Date fromDate = null;
        Date toDate = null;

        Calendar calToday = Calendar.getInstance();
        calToday.set(Calendar.HOUR_OF_DAY, 0);
        calToday.set(Calendar.MINUTE, 0);
        calToday.set(Calendar.SECOND, 0);
        calToday.set(Calendar.MILLISECOND, 0);
        Date today = calToday.getTime();

        int type = cboThongKe.getSelectedIndex();
        Calendar cal = Calendar.getInstance();
        try {
            if (type == 0) {
                String sFrom = dcNgayBD.getDate();
                String sTo = dcNgayKT.getDate();
                if (!sFrom.isEmpty()) fromDate = sdf.parse(sFrom);
                if (!sTo.isEmpty()) toDate = sdf.parse(sTo);
            } else if (type == 1) {
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                fromDate = cal.getTime();
                toDate = today;
            } else if (type == 2) {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                fromDate = cal.getTime();
                toDate = today;
            } else if (type == 3) {
                int quarter = cal.get(Calendar.MONTH) / 3;
                cal.set(Calendar.MONTH, quarter * 3);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                fromDate = cal.getTime();
                toDate = today;
            } else {
                cal.set(Calendar.DAY_OF_YEAR, 1);
                fromDate = cal.getTime();
                toDate = today;
            }

            if (fromDate != null && fromDate.after(today)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được lớn hơn ngày hiện tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
//            if (toDate != null && toDate.after(today)) {
//                JOptionPane.showMessageDialog(this, "Ngày kết thúc không được lớn hơn ngày hiện tại!", "Lôi", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
            if (fromDate != null && toDate != null && toDate.before(fromDate)) {
                JOptionPane.showMessageDialog(this, "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày tháng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DAO_ThongKeTuyen dao = new DAO_ThongKeTuyen();
        String maTuyenDaChon = getMaTuyenDaChon();
        List<Object[]> listData = dao.getDsTheoTuyen(fromDate, toDate, maTuyenDaChon);
        int[] kpi = dao.getKpiData(fromDate, toDate, maTuyenDaChon);

        DecimalFormat df = new DecimalFormat("#,###");
        lblTongChuyen.setText(df.format(kpi[0]));
        lblChuyenDaHoanThanh.setText("0");
        lblChuyenChuaHoanThanh.setText("0");
        tableModel.setRowCount(0);

        if (listData == null || listData.isEmpty()) {
            return;
        }

        int stt = 1;
        int soChuyenDaHoanThanh = 0;
        int soChuyenChuaHoanThanh = 0;
        for (Object[] row : listData) {
            String tenChuyen = String.valueOf(row[1]);
            String ngayKhoiHanh = String.valueOf(row[3]);
            String gioKhoiHanh = formatGioHienThi(row[4]);
            String ngayDen = formatNgayHienThi(row[5]);
            String gioDen = formatGioHienThi(row[6]);
            String trangThai = String.valueOf(row[7]);
            String trangThaiLoc = cboTrangThai != null && cboTrangThai.getSelectedItem() != null
                    ? cboTrangThai.getSelectedItem().toString()
                    : "Tất cả trạng thái";

            if (!"Tất cả trạng thái".equals(trangThaiLoc) && !trangThai.equalsIgnoreCase(trangThaiLoc)) {
                continue;
            }

            if ("Đã hoàn thành".equalsIgnoreCase(trangThai)) {
                soChuyenDaHoanThanh++;
            } else {
                soChuyenChuaHoanThanh++;
            }

            tableModel.addRow(new Object[]{
                    stt++,
                    tenChuyen,
                    ngayKhoiHanh,
                    gioKhoiHanh,
                    ngayDen,
                    gioDen,
                    trangThai
            });
        }

        lblChuyenDaHoanThanh.setText(df.format(soChuyenDaHoanThanh));
        lblChuyenChuaHoanThanh.setText(df.format(soChuyenChuaHoanThanh));
    }

    private String formatNgayHienThi(Object raw) {
        if (raw == null) return "";
        String value = raw.toString().trim();
        if (value.isEmpty() || "null".equalsIgnoreCase(value)) return "";
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private String formatGioHienThi(Object raw) {
        if (raw == null) return "--:--";
        String value = raw.toString().trim();
        if (value.isEmpty() || "null".equalsIgnoreCase(value)) return "--:--";
        return value.length() >= 5 ? value.substring(0, 5) : value;
    }

    private void loadDanhSachTuyen() {
        mapTuyen.clear();
        if (cboTuyen == null) return;

        cboTuyen.removeAllItems();
        cboTuyen.addItem("Tất cả tuyến");
        mapTuyen.put("Tất cả tuyến", "");

        DAO_ThongKeTuyen dao = new DAO_ThongKeTuyen();
        List<Object[]> dsTuyen = dao.getDanhSachTuyen();
        if (dsTuyen == null) return;

        for (Object[] row : dsTuyen) {
            String maTuyen = row[0] == null ? "" : row[0].toString().trim();
            String tenTuyen = row[1] == null ? "" : row[1].toString().trim();
            if (maTuyen.isEmpty()) continue;

            String display = maTuyen + " - " + tenTuyen;
            cboTuyen.addItem(display);
            mapTuyen.put(display, maTuyen);
        }
        cboTuyen.setSelectedIndex(0);
    }

    private String getMaTuyenDaChon() {
        if (cboTuyen == null || cboTuyen.getSelectedItem() == null) return "";
        String display = cboTuyen.getSelectedItem().toString();
        String maTuyen = mapTuyen.get(display);
        return maTuyen == null ? "" : maTuyen;
    }

    private void xuatFileExcel() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất Excel!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file Excel");
        fileChooser.setSelectedFile(new File("ThongKeTuyen_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xlsx"));
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String filePath = fileChooser.getSelectedFile().getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".xlsx")) filePath += ".xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Thống Kê Tuyến");
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
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất file Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatFileExcelDongBo() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất Excel!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file Excel");
        fileChooser.setSelectedFile(new File("ThongKeTuyen_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xlsx"));
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String filePath = fileChooser.getSelectedFile().getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".xlsx")) filePath += ".xlsx";

        final int colCount = tableModel.getColumnCount();
        final int rowCount = tableModel.getRowCount();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet("Thống Kê Tuyến");

            byte[] accentRgb = { 0x1A, 0x5E, (byte) 0xAB };
            XSSFColor accentColor = new XSSFColor(accentRgb, null);
            XSSFColor lineColor = new XSSFColor(new byte[] { (byte) 0xE2, (byte) 0xEA, (byte) 0xF4 }, null);
            XSSFColor altRowColor = new XSSFColor(new byte[] { (byte) 0xF7, (byte) 0xFA, (byte) 0xFF }, null);

            CellStyle styleTitle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font fTitle = wb.createFont();
            fTitle.setBold(true);
            fTitle.setFontHeightInPoints((short) 14);
            fTitle.setColor(IndexedColors.WHITE.getIndex());
            styleTitle.setFont(fTitle);
            styleTitle.setFillForegroundColor(accentColor);
            styleTitle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleTitle.setAlignment(HorizontalAlignment.CENTER);
            styleTitle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle styleSub = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font fSub = wb.createFont();
            fSub.setItalic(true);
            fSub.setFontHeightInPoints((short) 10);
            fSub.setColor(IndexedColors.WHITE.getIndex());
            styleSub.setFont(fSub);
            styleSub.setFillForegroundColor(accentColor);
            styleSub.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleSub.setAlignment(HorizontalAlignment.CENTER);

            CellStyle styleHeader = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font fHeader = wb.createFont();
            fHeader.setBold(true);
            fHeader.setFontHeightInPoints((short) 11);
            fHeader.setColor(IndexedColors.WHITE.getIndex());
            styleHeader.setFont(fHeader);
            styleHeader.setFillForegroundColor(accentColor);
            styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleHeader.setAlignment(HorizontalAlignment.CENTER);
            styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            styleHeader.setBorderBottom(BorderStyle.THIN);
            styleHeader.setBottomBorderColor(IndexedColors.WHITE.getIndex());

            CellStyle styleData = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font fData = wb.createFont();
            fData.setFontHeightInPoints((short) 11);
            styleData.setFont(fData);
            styleData.setBorderBottom(BorderStyle.THIN);
            ((XSSFCellStyle) styleData).setBottomBorderColor(lineColor);

            CellStyle styleDataAlt = wb.createCellStyle();
            styleDataAlt.cloneStyleFrom(styleData);
            styleDataAlt.setFillForegroundColor(altRowColor);
            styleDataAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle styleCenter = wb.createCellStyle();
            styleCenter.cloneStyleFrom(styleData);
            styleCenter.setAlignment(HorizontalAlignment.CENTER);

            CellStyle styleCenterAlt = wb.createCellStyle();
            styleCenterAlt.cloneStyleFrom(styleDataAlt);
            styleCenterAlt.setAlignment(HorizontalAlignment.CENTER);

            CellStyle styleFooter = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font fFooter = wb.createFont();
            fFooter.setItalic(true);
            fFooter.setFontHeightInPoints((short) 9);
            fFooter.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            styleFooter.setFont(fFooter);
            styleFooter.setAlignment(HorizontalAlignment.RIGHT);

            Row rTitle = sh.createRow(0);
            rTitle.setHeightInPoints(28);
            Cell cTitle = rTitle.createCell(0);
            cTitle.setCellValue("BÁO CÁO THỐNG KÊ TUYẾN");
            cTitle.setCellStyle(styleTitle);
            for (int c = 1; c < colCount; c++) rTitle.createCell(c).setCellStyle(styleTitle);
            sh.addMergedRegion(new CellRangeAddress(0, 0, 0, colCount - 1));

            Row rSub = sh.createRow(1);
            rSub.setHeightInPoints(18);
            Cell cSub = rSub.createCell(0);
            cSub.setCellValue("Hệ Thống Bán Vé Tàu Hỏa Việt Nam");
            cSub.setCellStyle(styleSub);
            for (int c = 1; c < colCount; c++) rSub.createCell(c).setCellStyle(styleSub);
            sh.addMergedRegion(new CellRangeAddress(1, 1, 0, colCount - 1));

            Row rHeader = sh.createRow(2);
            rHeader.setHeightInPoints(24);
            for (int c = 0; c < colCount; c++) {
                Cell cell = rHeader.createCell(c);
                cell.setCellValue(tableModel.getColumnName(c));
                cell.setCellStyle(styleHeader);
            }

            int dataStart = 3;
            for (int i = 0; i < rowCount; i++) {
                Row row = sh.createRow(dataStart + i);
                row.setHeightInPoints(20);
                boolean alt = (i % 2 == 1);

                CellStyle csText = alt ? styleDataAlt : styleData;
                CellStyle csCenter = alt ? styleCenterAlt : styleCenter;

                for (int c = 0; c < colCount; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(String.valueOf(tableModel.getValueAt(i, c)));
                    if (c == 0 || c == 2 || c == 3 || c == 4 || c == 5 || c == 6) {
                        cell.setCellStyle(csCenter);
                    } else {
                        cell.setCellStyle(csText);
                    }
                }
            }

            Row footerRow = sh.createRow(dataStart + rowCount + 1);
            Cell footerCell = footerRow.createCell(0);
            footerCell.setCellValue("Ngày xuất: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            footerCell.setCellStyle(styleFooter);
            sh.addMergedRegion(new CellRangeAddress(dataStart + rowCount + 1, dataStart + rowCount + 1, 0, colCount - 1));

            sh.setColumnWidth(0, 8 * 256);
            sh.setColumnWidth(1, 34 * 256);
            sh.setColumnWidth(2, 16 * 256);
            sh.setColumnWidth(3, 14 * 256);
            sh.setColumnWidth(4, 16 * 256);
            sh.setColumnWidth(5, 14 * 256);
            sh.setColumnWidth(6, 16 * 256);
            sh.createFreezePane(0, 3);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }

            int opt = JOptionPane.showConfirmDialog(this, "Xuất Excel thành công!\nMở file?", "Thành công",
                    JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (opt == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(filePath));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất file Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel makeCard(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG_CARD);
        p.setBorder(new ShadowBorder());
        return p;
    }

    private JLabel makeLabel(String txt) {
        JLabel lbl = new JLabel(txt);
        lbl.setFont(F_LABEL);
        lbl.setForeground(TEXT_MID);
        return lbl;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(F_CELL);
        cb.setBackground(new Color(0xF8FAFD));
        cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        cb.setPreferredSize(new Dimension(130, 36));
        return cb;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (style == BtnStyle.PRIMARY) {
                    g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT);
                } else {
                    g2.setColor(getModel().isRollover() ? BTN_SUCCESS_HVR : BTN_SUCCESS);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(F_LABEL);
        b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(130, 36));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private Icon createButtonIcon(BtnStyle style) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return 14;
            }

            @Override
            public int getIconHeight() {
                return 14;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (style == BtnStyle.PRIMARY) {
                    g2.drawLine(x + 1, y + 13, x + 13, y + 13);
                    g2.drawLine(x + 1, y + 13, x + 1, y + 1);
                    g2.fillRoundRect(x + 3, y + 8, 2, 5, 1, 1);
                    g2.fillRoundRect(x + 7, y + 6, 2, 7, 1, 1);
                    g2.fillRoundRect(x + 11, y + 3, 2, 10, 1, 1);
                } else {
                    g2.drawRoundRect(x + 1, y + 1, 12, 12, 2, 2);
                    g2.drawLine(x + 1, y + 5, x + 13, y + 5);
                    g2.drawLine(x + 1, y + 9, x + 13, y + 9);
                    g2.drawLine(x + 5, y + 1, x + 5, y + 13);
                    g2.drawLine(x + 9, y + 1, x + 9, y + 13);
                }
                g2.dispose();
            }
        };
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        t.setRowHeight(36);
        t.setFont(F_CELL);
        t.setBackground(BG_CARD);
        t.setSelectionBackground(new Color(0xDDEEFF));
        t.setSelectionForeground(TEXT_DARK);
        t.setGridColor(BORDER);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setFocusable(false);
        t.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(LEFT);
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                l.setOpaque(true);
                l.setBackground(ACCENT);
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
                return l;
            }
        });
        h.setPreferredSize(new Dimension(0, 40));
        h.setReorderingAllowed(false);

        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
        for (int i = 0; i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(r);
        t.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiTuyenRenderer());
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.getColumnModel().getColumn(0).setPreferredWidth(55);   // STT
        t.getColumnModel().getColumn(1).setPreferredWidth(290);  // Ten chuyen
        t.getColumnModel().getColumn(2).setPreferredWidth(120);  // Ngay khoi hanh
        t.getColumnModel().getColumn(3).setPreferredWidth(95);   // Gio khoi hanh
        t.getColumnModel().getColumn(4).setPreferredWidth(120);  // Ngay den
        t.getColumnModel().getColumn(5).setPreferredWidth(95);   // Gio den
        t.getColumnModel().getColumn(6).setPreferredWidth(130);  // Trang thai
        return t;
    }

    private static class TrangThaiTuyenRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String val = v != null ? v.toString().trim() : "";
            if ("Chưa Khởi Hành".equalsIgnoreCase(val)) {
                l.setForeground(CLR_CHUA);
            } else if ("Đang Khởi Hành".equalsIgnoreCase(val)) {
                l.setForeground(CLR_DANG);
            } else if ("Đã Hoàn Thành".equalsIgnoreCase(val)) {
                l.setForeground(CLR_HOAN);
            } else if ("Đã Hủy".equalsIgnoreCase(val)) {
                l.setForeground(CLR_HUY);
            } else {
                l.setForeground(TEXT_DARK);
            }
            if (!sel) l.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
            return l;
        }
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(0xC0D4EE);
                trackColor = BG_PAGE;
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                return zBtn();
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                return zBtn();
            }

            private JButton zBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        sb.putClientProperty(FlatClientProperties.SCROLL_BAR_SHOW_BUTTONS, false);
    }

    private static class ShadowBorder extends AbstractBorder {
        private static final int S = 4;

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = S; i > 0; i--) {
                g2.setColor(new Color(100, 140, 200, (int) (20.0 * (S - i) / S)));
                g2.drawRoundRect(x + i, y + i, w - 2 * i - 1, h - 2 * i - 1, 12, 12);
            }
            g2.setColor(new Color(0xE2EAF4));
            g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
            g2.setColor(BG_CARD);
            g2.setClip(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, 12, 12));
            g2.fillRect(x + 1, y + 1, w - 2, h - 2);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(S, S, S, S);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets ins) {
            ins.set(S, S, S, S);
            return ins;
        }
    }

    private class DatePickerField extends JPanel {
        private final JTextField txt;
        private final Calendar cal;
        private JPanel pnlGrid;
        private JComboBox<String> cbThang;
        private JComboBox<Integer> cbNam;
        private JWindow popup;
        private boolean isEnabled = true;

        private static final String[] TEN_THANG = {
                "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        };
        private static final String[] TEN_THU = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        DatePickerField(String init) {
            setLayout(new BorderLayout());
            setOpaque(false);
            cal = Calendar.getInstance();
            if (init != null && !init.isEmpty()) {
                try {
                    cal.setTime(new SimpleDateFormat(DATE_FMT).parse(init));
                } catch (Exception ignored) {
                }
            }
            String disp = init != null && !init.isEmpty() ? init : new SimpleDateFormat(DATE_FMT).format(cal.getTime());

            txt = new JTextField(disp);
            txt.setFont(F_CELL);
            txt.setForeground(TEXT_DARK);
            txt.setBackground(new Color(0xF8FAFD));
            txt.setEditable(false);
            txt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(6, 10, 6, 36)));

            JLabel ico = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isEnabled ? TEXT_MID : TEXT_LIGHT);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.drawRoundRect(cx - 8, cy - 7, 16, 14, 3, 3);
                    g2.drawLine(cx - 8, cy - 4, cx + 8, cy - 4);
                    g2.drawLine(cx - 4, cy - 10, cx - 4, cy - 5);
                    g2.drawLine(cx + 4, cy - 10, cx + 4, cy - 5);
                    g2.fillOval(cx - 6, cy - 1, 3, 3);
                    g2.fillOval(cx - 1, cy - 1, 3, 3);
                    g2.fillOval(cx + 4, cy - 1, 3, 3);
                    g2.fillOval(cx - 6, cy + 3, 3, 3);
                    g2.fillOval(cx - 1, cy + 3, 3, 3);
                    g2.dispose();
                }
            };
            ico.setPreferredSize(new Dimension(32, 36));
            ico.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.add(txt, BorderLayout.CENTER);
            wrap.add(ico, BorderLayout.EAST);
            add(wrap, BorderLayout.CENTER);

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isEnabled) toggle();
                }
            };
            txt.addMouseListener(ma);
            ico.addMouseListener(ma);
        }

        public void setEnabledField(boolean enabled) {
            this.isEnabled = enabled;
            txt.setBackground(enabled ? new Color(0xF8FAFD) : new Color(0xEEF2F8));
            txt.setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            repaint();
        }

        public void setDate(String date) {
            txt.setText(date);
        }

        public String getDate() {
            return txt.getText();
        }

        private void toggle() {
            if (popup != null && popup.isVisible()) {
                popup.dispose();
                popup = null;
                return;
            }
            showPop();
        }

        private void showPop() {
            popup = new JWindow(SwingUtilities.getWindowAncestor(this));
            popup.setLayout(new BorderLayout());
            JPanel p = new JPanel(new BorderLayout(0, 6));
            p.setBackground(BG_CARD);
            p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1), BorderFactory.createEmptyBorder(12, 12, 12, 12)));
            p.add(navBar(), BorderLayout.NORTH);
            pnlGrid = new JPanel(new GridLayout(0, 7, 2, 2));
            pnlGrid.setBackground(BG_CARD);
            p.add(pnlGrid, BorderLayout.CENTER);
            fillGrid();
            popup.add(p);
            popup.pack();
            popup.setSize(Math.max(280, popup.getWidth()), popup.getHeight());
            Point loc = txt.getLocationOnScreen();
            popup.setLocation(loc.x, loc.y + txt.getHeight() + 2);
            popup.setVisible(true);
            popup.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
                @Override
                public void windowGainedFocus(java.awt.event.WindowEvent e) {
                }

                @Override
                public void windowLostFocus(java.awt.event.WindowEvent e) {
                    if (popup != null) {
                        popup.dispose();
                        popup = null;
                    }
                }
            });
        }

        private JPanel navBar() {
            JPanel nav = new JPanel(new BorderLayout(4, 0));
            nav.setBackground(BG_CARD);
            JButton prev = navBtn("<");
            JButton next = navBtn(">");
            prev.addActionListener(e -> {
                cal.add(Calendar.MONTH, -1);
                cbThang.setSelectedIndex(cal.get(Calendar.MONTH));
                cbNam.setSelectedItem(cal.get(Calendar.YEAR));
                fillGrid();
            });
            next.addActionListener(e -> {
                cal.add(Calendar.MONTH, 1);
                cbThang.setSelectedIndex(cal.get(Calendar.MONTH));
                cbNam.setSelectedItem(cal.get(Calendar.YEAR));
                fillGrid();
            });
            cbThang = new JComboBox<>(TEN_THANG);
            cbThang.setFont(F_SMALL);
            cbThang.setSelectedIndex(cal.get(Calendar.MONTH));
            cbThang.setPreferredSize(new Dimension(82, 26));
            cbThang.addActionListener(e -> {
                cal.set(Calendar.MONTH, cbThang.getSelectedIndex());
                fillGrid();
            });
            int y = Calendar.getInstance().get(Calendar.YEAR);
            Integer[] yrs = new Integer[16];
            for (int i = 0; i < 16; i++) yrs[i] = y - 5 + i;
            cbNam = new JComboBox<>(yrs);
            cbNam.setFont(F_SMALL);
            cbNam.setSelectedItem(cal.get(Calendar.YEAR));
            cbNam.setPreferredSize(new Dimension(60, 26));
            cbNam.addActionListener(e -> {
                if (cbNam.getSelectedItem() != null) {
                    cal.set(Calendar.YEAR, (Integer) cbNam.getSelectedItem());
                    fillGrid();
                }
            });
            JPanel ctr = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            ctr.setBackground(BG_CARD);
            ctr.add(cbThang);
            ctr.add(cbNam);
            nav.add(prev, BorderLayout.WEST);
            nav.add(ctr, BorderLayout.CENTER);
            nav.add(next, BorderLayout.EAST);
            return nav;
        }

        private void fillGrid() {
            pnlGrid.removeAll();
            for (String th : TEN_THU) {
                JLabel l = new JLabel(th, SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l.setPreferredSize(new Dimension(32, 24));
                l.setForeground(TEXT_MID);
                pnlGrid.add(l);
            }

            Calendar tmp = (Calendar) cal.clone();
            tmp.set(Calendar.DAY_OF_MONTH, 1);
            int first = (tmp.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            Calendar today = Calendar.getInstance();
            int todayD = today.get(Calendar.DAY_OF_MONTH);
            boolean sm = today.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && today.get(Calendar.YEAR) == cal.get(Calendar.YEAR);
            int chosen = -1;
            try {
                Calendar c = Calendar.getInstance();
                c.setTime(new SimpleDateFormat(DATE_FMT).parse(txt.getText()));
                if (c.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && c.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                    chosen = c.get(Calendar.DAY_OF_MONTH);
                }
            } catch (Exception ignored) {
            }

            for (int i = 0; i < first; i++) pnlGrid.add(new JLabel());
            int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            final int fc = chosen;
            for (int d = 1; d <= days; d++) {
                final int nd = d;
                boolean isT = sm && d == todayD;
                boolean isSel = d == fc;
                JButton b = new JButton(String.valueOf(d)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        if (isSel) {
                            g2.setColor(ACCENT);
                            g2.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
                        } else if (getModel().isRollover()) {
                            g2.setColor(new Color(0xDDEEFF));
                            g2.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
                        } else if (isT) {
                            g2.setColor(new Color(0xE8F1FB));
                            g2.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
                        }
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                b.setFont(new Font("Segoe UI", isT ? Font.BOLD : Font.PLAIN, 11));
                b.setForeground(isSel ? Color.WHITE : (isT ? ACCENT : TEXT_DARK));
                b.setPreferredSize(new Dimension(32, 32));
                b.setContentAreaFilled(false);
                b.setBorderPainted(false);
                b.setFocusPainted(false);
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.addActionListener(e -> {
                    cal.set(Calendar.DAY_OF_MONTH, nd);
                    txt.setText(new SimpleDateFormat(DATE_FMT).format(cal.getTime()));
                    if (popup != null) {
                        popup.dispose();
                        popup = null;
                    }
                });
                pnlGrid.add(b);
            }
            pnlGrid.revalidate();
            pnlGrid.repaint();
        }

        private JButton navBtn(String t) {
            JButton b = new JButton(t);
            b.setFont(new Font("Segoe UI", Font.BOLD, 14));
            b.setForeground(ACCENT);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setMargin(new Insets(0, 0, 0, 0));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(32, 32));
            return b;
        }
    }
}
