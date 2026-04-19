package com.gui.banve;

import com.dao.DAO_Ve;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Step5_SuccessPanel extends JPanel {
    private TAB_BanVe mainTab;
    private final DAO_Ve daoVe = new DAO_Ve();

    private static class TicketPrintData {
        String maVe;
        String maKH = "";
        String tenKH = "Khách lẻ";
        String cccd = "";
        String tenTau = "";
        String gaKhoiHanh = "";
        String gaDen = "";
        String ngayDi = "";
        String gioDi = "";
        String maToa = "";
        String viTri = "";
        String loaiCho = "";
        String loaiVe = "";
        String trangThai = "CHUASUDUNG";
        double giaVeRaw = 0;
        List<DAO_Ve.KhuyenMaiInfo> dsKM = new ArrayList<>();
    }

    public Step5_SuccessPanel(TAB_BanVe mainTab) {
        this.mainTab = mainTab;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel card = UIHelper.makeCard(new BorderLayout(0, 20));
        card.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(), BorderFactory.createEmptyBorder(40, 60, 40, 60)));

        JLabel lblIcon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight()); int cx = getWidth() / 2; int cy = getHeight() / 2;

                g2.setColor(new Color(40, 167, 69, 30)); g2.fillOval(cx - size/2, cy - size/2, size, size);
                g2.setColor(UIHelper.SUCCESS); g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] xPoints = {cx - 15, cx - 3, cx + 18}; int[] yPoints = {cy + 2, cy + 14, cy - 14};
                g2.drawPolyline(xPoints, yPoints, 3);
                g2.dispose();
            }
        };
        lblIcon.setPreferredSize(new Dimension(80, 80));

        JLabel lblMsg = new JLabel("<html><center><b style='font-size:26px'>Đặt vé thành công!</b><br/><br/><span style='color:gray; font-size:15px'>Vé đã được lưu vào hệ thống và có thể in ngay.</span></center></html>", SwingConstants.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); pnlBtn.setOpaque(false);
        JButton btnIn = UIHelper.makeBtn("In Vé", true);
        JButton btnMoi = UIHelper.makeBtn("Đặt vé mới", false);

        btnIn.addActionListener(e -> inVeSauThanhToan());
        btnMoi.addActionListener(e -> mainTab.resetProcess());

        pnlBtn.add(btnMoi); pnlBtn.add(btnIn);

        JPanel pnlIconWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER)); pnlIconWrapper.setOpaque(false);
        pnlIconWrapper.add(lblIcon);

        card.add(pnlIconWrapper, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(pnlBtn, BorderLayout.SOUTH);

        add(card);
    }

    private File getVeOutputDir() {
        File dir = new File(System.getProperty("user.dir"), "src/main/resources/Ve");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private void inVeSauThanhToan() {
        Step4_ThanhToan step4 = mainTab.getStep4();
        if (step4 == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu bước thanh toán!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> maVeList = step4.getCurrentMaVeList();
        if (maVeList == null || maVeList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có vé nào để in.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File outDir = getVeOutputDir();
        int okCount = 0;

        for (String maVe : maVeList) {
            try {
                TicketPrintData d = loadTicketData(maVe);
                if (d == null) {
                    continue;
                }
                File dest = new File(outDir, "Ve_" + maVe + ".pdf");
                buildBoardingPassPDF(dest, d);
                okCount++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        JOptionPane.showMessageDialog(this,
                "Đã in " + okCount + "/" + maVeList.size() + " vé vào:\n" + outDir.getAbsolutePath(),
                "In vé", JOptionPane.INFORMATION_MESSAGE);

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(outDir);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private TicketPrintData loadTicketData(String maVe) {
        try {
            TicketPrintData d = new TicketPrintData();
            d.maVe = maVe;
            ResultSet rs = daoVe.getChiTietVe(maVe);
            if (rs == null || !rs.next()) {
                return null;
            }

            d.maKH = nvl(rs.getString("maKH"));
            d.tenKH = nvl(rs.getString("tenKH"), "Khách lẻ");
            d.cccd = nvl(rs.getString("cccd"));

            String tenTuyen = nvl(rs.getString("tenTuyen"));
            if (tenTuyen.contains("-")) {
                String[] parts = tenTuyen.split("-", 2);
                d.gaKhoiHanh = parts[0].trim();
                d.gaDen = parts[1].trim();
            } else {
                d.gaKhoiHanh = tenTuyen;
            }

            String tenChuyen = nvl(rs.getString("tenChuyen"));
            d.tenTau = tenChuyen.contains(":") ? tenChuyen.split(":", 2)[0].trim() : tenChuyen;

            java.sql.Date ngay = rs.getDate("ngayKhoiHanh");
            java.sql.Time gio = rs.getTime("gioKhoiHanh");
            d.ngayDi = ngay != null ? new SimpleDateFormat("dd/MM/yyyy").format(ngay) : "";
            d.gioDi = gio != null ? new SimpleDateFormat("HH:mm").format(gio) : "";

            d.maToa = nvl(rs.getString("maToa"));
            d.viTri = nvl(rs.getString("viTriGhe"));
            d.loaiCho = nvl(rs.getString("tenLoaiToa"));
            d.loaiVe = nvl(rs.getString("tenLoaiVe"));
            d.giaVeRaw = rs.getDouble("giaVe");
            d.trangThai = nvl(rs.getString("trangThaiVe"), "CHUASUDUNG");
            d.dsKM = daoVe.getKhuyenMaiCuaVe(maVe);
            return d;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String nvl(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s;
    }

    private void buildBoardingPassPDF(File dest, TicketPrintData d) throws Exception {
        DeviceRgb BLACK = new DeviceRgb(0, 0, 0);
        DeviceRgb GRAY = new DeviceRgb(80, 80, 80);
        DeviceRgb LGRAY = new DeviceRgb(180, 180, 180);

        PageSize ps = PageSize.A5;
        PdfWriter writer = new PdfWriter(new FileOutputStream(dest));
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, ps);
        doc.setMargins(28, 28, 28, 28);

        PdfFont fontBold = loadVietnameseFont(true);
        PdfFont fontPlain = loadVietnameseFont(false);

        float pageW = ps.getWidth();
        float pageH = ps.getHeight();
        float margin = 28f;
        float cw = pageW - margin * 2;

        PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());

        float y = pageH - margin;
        drawCenteredText(canvas, fontBold, 9f, "CÔNG TY CỔ PHẦN VẬN TẢI", BLACK, margin, pageW, y);
        y -= 14;
        drawCenteredText(canvas, fontBold, 9f, "ĐƯỜNG SẮT VIỆT NAM", BLACK, margin, pageW, y);
        y -= 16;
        drawCenteredText(canvas, fontBold, 12f, "THẺ LÊN TÀU HỎA / BOARDING PASS", BLACK, margin, pageW, y);
        y -= 10;
        drawHLine(canvas, margin, pageW - margin, y, 0.5f, LGRAY);
        y -= 16;

        float bcH = 32f;
        drawBarcodeStripes(canvas, margin + 10, y - bcH, cw - 20, bcH, d.maVe);
        y -= (bcH + 14);
        drawHLine(canvas, margin, pageW - margin, y, 0.5f, LGRAY);
        y -= 14;

        drawCenteredText(canvas, fontPlain, 8.5f, "Mã vé / TicketID:  " + d.maVe, BLACK, margin, pageW, y);
        y -= 20;

        float col1X = margin;
        float col2X = pageW / 2f;
        float labelX = margin;
        float valueX = margin + 120f;
        float lineH = 17f;

        canvas.setFillColor(GRAY);
        canvas.beginText();
        canvas.setFontAndSize(fontPlain, 8f);
        canvas.moveText(col1X, y);
        canvas.showText("Ga đi");
        canvas.endText();

        canvas.beginText();
        canvas.setFontAndSize(fontPlain, 8f);
        canvas.moveText(col2X, y);
        canvas.showText("Ga đến");
        canvas.endText();
        y -= 14;

        String gaDi = d.gaKhoiHanh.isEmpty() ? "N/A" : d.gaKhoiHanh.toUpperCase();
        String gaDn = d.gaDen.isEmpty() ? "N/A" : d.gaDen.toUpperCase();
        canvas.setFillColor(BLACK);
        canvas.beginText();
        canvas.setFontAndSize(fontBold, 14f);
        canvas.moveText(col1X, y);
        canvas.showText(gaDi);
        canvas.endText();

        canvas.beginText();
        canvas.setFontAndSize(fontBold, 14f);
        canvas.moveText(col2X, y);
        canvas.showText(gaDn);
        canvas.endText();
        y -= 16;

        drawHLine(canvas, margin, pageW - margin, y, 0.4f, LGRAY);
        y -= 14;

        java.text.DecimalFormat dfMoney = new java.text.DecimalFormat("#,##0");
        dfMoney.setDecimalFormatSymbols(new java.text.DecimalFormatSymbols(new java.util.Locale("vi", "VN")));

        double tongGiam = 0;
        for (DAO_Ve.KhuyenMaiInfo km : d.dsKM) {
            tongGiam += km.tienGiamThucTe;
        }
        double giaSau = d.giaVeRaw - tongGiam;

        String[][] rows = {
                { "Tàu / Train:", d.tenTau.isEmpty() ? "N/A" : d.tenTau },
                { "Ngày đi / Date:", d.ngayDi.isEmpty() ? "N/A" : d.ngayDi },
                { "Giờ đi / Time:", d.gioDi.isEmpty() ? "N/A" : d.gioDi },
                { "Toa / Coach:  " + d.maToa, "Chỗ / Seat:  " + d.viTri },
                { "Loại chỗ / Class:", d.loaiCho.isEmpty() ? d.loaiVe : d.loaiCho },
                { "Loại vé / Ticket:", d.loaiVe.isEmpty() ? "N/A" : d.loaiVe },
                { "Họ tên / Name:", d.tenKH.isEmpty() ? "xxxxxxxx" : d.tenKH },
                { "", "" },
                { "Giấy tờ / Passport:", d.cccd.isEmpty() ? "xxxxxxxx" : d.cccd }
        };

        for (String[] row : rows) {
            if (row[0].isEmpty() && row[1].isEmpty()) {
                y -= 6;
                continue;
            }

            if (row[0].startsWith("Toa / Coach:")) {
                canvas.setFillColor(GRAY);
                canvas.beginText();
                canvas.setFontAndSize(fontPlain, 8.5f);
                canvas.moveText(labelX, y);
                canvas.showText(row[0]);
                canvas.endText();

                canvas.setFillColor(BLACK);
                canvas.beginText();
                canvas.setFontAndSize(fontBold, 8.5f);
                canvas.moveText(col2X, y);
                canvas.showText(row[1]);
                canvas.endText();
            } else {
                canvas.setFillColor(GRAY);
                canvas.beginText();
                canvas.setFontAndSize(fontPlain, 8.5f);
                canvas.moveText(labelX, y);
                canvas.showText(row[0]);
                canvas.endText();

                canvas.setFillColor(BLACK);
                canvas.beginText();
                canvas.setFontAndSize(fontBold, 8.5f);
                canvas.moveText(valueX, y);
                canvas.showText(row[1]);
                canvas.endText();
            }
            y -= lineH;
        }

        DeviceRgb GREEN = new DeviceRgb(0x16, 0x63, 0x34);
        DeviceRgb RED = new DeviceRgb(0xDC, 0x26, 0x26);
        if (!d.dsKM.isEmpty()) {
            canvas.setFillColor(GRAY);
            canvas.beginText();
            canvas.setFontAndSize(fontPlain, 8.5f);
            canvas.moveText(labelX, y);
            canvas.showText("Khuyến mãi:");
            canvas.endText();

            DAO_Ve.KhuyenMaiInfo km0 = d.dsKM.get(0);
            String loai0 = km0.loaiKM.equals("GIAM_PHAN_TRAM")
                    ? String.format("Giảm %.0f%%", km0.giaTri)
                    : String.format("Giảm %s VND", dfMoney.format((long) km0.giaTri));
            canvas.setFillColor(new DeviceRgb(0x03, 0x69, 0xA1));
            canvas.beginText();
            canvas.setFontAndSize(fontBold, 8.5f);
            canvas.moveText(valueX, y);
            canvas.showText(km0.tenKM + "  (" + loai0 + ")");
            canvas.endText();
            y -= lineH;

            for (int i = 1; i < d.dsKM.size(); i++) {
                DAO_Ve.KhuyenMaiInfo kmi = d.dsKM.get(i);
                String loaii = kmi.loaiKM.equals("GIAM_PHAN_TRAM")
                        ? String.format("Giảm %.0f%%", kmi.giaTri)
                        : String.format("Giảm %s VND", dfMoney.format((long) kmi.giaTri));
                canvas.setFillColor(new DeviceRgb(0x03, 0x69, 0xA1));
                canvas.beginText();
                canvas.setFontAndSize(fontBold, 8.5f);
                canvas.moveText(valueX, y);
                canvas.showText(kmi.tenKM + "  (" + loaii + ")");
                canvas.endText();
                y -= lineH;
            }

            canvas.setFillColor(GREEN);
            canvas.beginText();
            canvas.setFontAndSize(fontPlain, 8f);
            canvas.moveText(valueX, y);
            canvas.showText("Tiết kiệm: -" + dfMoney.format((long) tongGiam) + " VND");
            canvas.endText();
            y -= lineH;
        }

        canvas.setFillColor(GRAY);
        canvas.beginText();
        canvas.setFontAndSize(fontPlain, 8.5f);
        canvas.moveText(labelX, y);
        canvas.showText("Giá / Price:");
        canvas.endText();

        if (tongGiam > 0) {
            String gocStr = dfMoney.format((long) d.giaVeRaw) + " VND";
            canvas.setFillColor(GRAY);
            canvas.beginText();
            canvas.setFontAndSize(fontPlain, 8f);
            canvas.moveText(valueX, y);
            canvas.showText(gocStr);
            canvas.endText();

            float gocW = fontPlain.getWidth(gocStr, 8f);
            canvas.setStrokeColor(GRAY);
            canvas.setLineWidth(0.6f);
            canvas.moveTo(valueX, y + 3.5f);
            canvas.lineTo(valueX + gocW, y + 3.5f);
            canvas.stroke();
            y -= lineH;

            canvas.setFillColor(RED);
            canvas.beginText();
            canvas.setFontAndSize(fontBold, 10f);
            canvas.moveText(valueX, y);
            canvas.showText(dfMoney.format((long) giaSau) + " VND");
            canvas.endText();
        } else {
            canvas.setFillColor(BLACK);
            canvas.beginText();
            canvas.setFontAndSize(fontBold, 8.5f);
            canvas.moveText(valueX, y);
            canvas.showText(dfMoney.format((long) d.giaVeRaw) + " VND");
            canvas.endText();
        }
        y -= lineH;

        y -= 6;
        drawDashedHLine(canvas, margin, pageW - margin, y, LGRAY);
        y -= 14;

        drawCenteredText(canvas, fontBold, 7.5f,
                d.maVe + "   |   " + d.tenTau + "   |   " + d.ngayDi + "   " + d.gioDi
                        + "   |   Toa " + d.maToa + "  Ghế " + d.viTri,
                GRAY, margin, pageW, y);
        y -= 11;

        String ngayIn = "Ngày in: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        canvas.setFillColor(LGRAY);
        canvas.beginText();
        canvas.setFontAndSize(fontPlain, 7f);
        canvas.moveText(margin, y);
        canvas.showText(ngayIn);
        canvas.endText();

        canvas.setFillColor(LGRAY);
        canvas.beginText();
        canvas.setFontAndSize(fontPlain, 7f);
        String thanks = "Cảm ơn quý khách! / Thank you!";
        float tw = fontPlain.getWidth(thanks, 7f);
        canvas.moveText(pageW - margin - tw, y);
        canvas.showText(thanks);
        canvas.endText();

        canvas.setStrokeColor(LGRAY);
        canvas.setLineWidth(0.8f);
        canvas.rectangle(margin - 8, margin - 8,
                pageW - (margin - 8) * 2,
                pageH - (margin - 8) * 2);
        canvas.stroke();

        canvas.release();
        doc.close();
    }

    private PdfFont loadVietnameseFont(boolean bold) throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase();
        String[][] candidates;
        if (os.contains("win")) {
            candidates = new String[][]{
                    {"C:/Windows/Fonts/arial.ttf", "C:/Windows/Fonts/arialbd.ttf"},
                    {"C:/Windows/Fonts/tahoma.ttf", "C:/Windows/Fonts/tahomabd.ttf"}
            };
        } else if (os.contains("mac")) {
            candidates = new String[][]{
                    {"/Library/Fonts/Arial.ttf", "/Library/Fonts/Arial Bold.ttf"}
            };
        } else {
            candidates = new String[][]{
                    {"/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"}
            };
        }

        int idx = bold ? 1 : 0;
        for (String[] pair : candidates) {
            File f = new File(pair[idx]);
            if (!f.exists() && idx == 1) {
                f = new File(pair[0]);
            }
            if (f.exists()) {
                return PdfFontFactory.createFont(
                        f.getAbsolutePath(),
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
        }

        return PdfFontFactory.createFont(
                bold ? com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD
                        : com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
    }

    private void drawCenteredText(PdfCanvas c, PdfFont font, float size, String text,
                                  DeviceRgb color, float marginL, float pageW, float y) throws Exception {
        float tw = font.getWidth(text, size);
        float x = marginL + (pageW - marginL * 2 - tw) / 2f;
        c.setFillColor(color);
        c.beginText();
        c.setFontAndSize(font, size);
        c.moveText(x, y);
        c.showText(text);
        c.endText();
    }

    private void drawHLine(PdfCanvas c, float x1, float x2, float y, float lw, DeviceRgb color) {
        c.setStrokeColor(color);
        c.setLineWidth(lw);
        c.moveTo(x1, y);
        c.lineTo(x2, y);
        c.stroke();
    }

    private void drawDashedHLine(PdfCanvas c, float x1, float x2, float y, DeviceRgb color) {
        c.setStrokeColor(color);
        c.setLineWidth(0.5f);
        c.setLineDash(3f, 3f, 0f);
        c.moveTo(x1, y);
        c.lineTo(x2, y);
        c.stroke();
        c.setLineDash(0f);
    }

    private void drawBarcodeStripes(PdfCanvas c, float x, float y, float w, float h, String seed) {
        long hash = seed.hashCode() & 0xFFFFFFFFL;
        java.util.Random rng = new java.util.Random(hash);
        float cx = x;
        DeviceRgb black = new DeviceRgb(0, 0, 0);
        DeviceRgb white = new DeviceRgb(255, 255, 255);
        c.setFillColor(white);
        c.rectangle(x, y, w, h);
        c.fill();
        while (cx < x + w - 1) {
            float stripeW = rng.nextBoolean() ? 1.5f : 0.8f;
            float gap = rng.nextFloat() * 2.2f + 0.6f;
            if (cx + stripeW > x + w) {
                break;
            }
            c.setFillColor(black);
            c.rectangle(cx, y, stripeW, h);
            c.fill();
            cx += stripeW + gap;
        }
    }
}