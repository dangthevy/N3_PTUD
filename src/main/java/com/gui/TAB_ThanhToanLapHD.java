package com.gui;

import com.connectDB.ConnectDB;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.FileOutputStream;

// Các thư viện quan trọng từ iText 5 để xử lý file PDF
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.FontFactory;

public class TAB_ThanhToanLapHD extends JPanel {
    private DefaultTableModel modelHD, modelCT;
    private JTable tableHD, tableCT;
    private JComboBox<Object> cbNhanVienLoc;
    private JDateChooser dateTuNgay, dateToiNgay;
    private JTextField txtTimKiemTenKH;
    private JButton btnXoaLoc;

    private JLabel lblMaHDVal, lblNgayLapVal, lblNhanVienVal, lblKhachHangVal, lblTongGiamVal, lblTongTienVal;
    private String currentMaHD = "";

    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final java.awt.Font FONT_TITLE = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 24);
    private final java.awt.Font FONT_NORMAL = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 13);
    private final java.awt.Font FONT_BOLD = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 14);

    public TAB_ThanhToanLapHD() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 240, 240));

        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        JLabel lblPageTitle = new JLabel("TRA CỨU HÓA ĐƠN", JLabel.CENTER);
        lblPageTitle.setFont(FONT_TITLE);
        lblPageTitle.setForeground(new Color(0, 51, 153));
        lblPageTitle.setBorder(new EmptyBorder(10, 0, 10, 0));

        pnlNorth.add(lblPageTitle, BorderLayout.NORTH);
        pnlNorth.add(createPanelFilter(), BorderLayout.CENTER);

        add(pnlNorth, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setLeftComponent(createPanelDanhSachHoaDon());
        splitPane.setRightComponent(createPanelChiTietDayDu());

        add(splitPane, BorderLayout.CENTER);

        loadNhanVienToCombo();
        loadDauSachHoaDon();
    }

    private JPanel createPanelFilter() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnl.setBorder(new TitledBorder("Bộ lọc"));

        txtTimKiemTenKH = new JTextField(12);
        txtTimKiemTenKH.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                loadDauSachHoaDon();
            }
        });

        dateTuNgay = new JDateChooser();
        dateTuNgay.setPreferredSize(new Dimension(120, 25));
        dateTuNgay.addPropertyChangeListener("date", e -> loadDauSachHoaDon());

        dateToiNgay = new JDateChooser();
        dateToiNgay.setPreferredSize(new Dimension(120, 25));
        dateToiNgay.addPropertyChangeListener("date", e -> loadDauSachHoaDon());

        cbNhanVienLoc = new JComboBox<>();
        cbNhanVienLoc.addActionListener(e -> loadDauSachHoaDon());

        btnXoaLoc = new JButton("Xóa bộ lọc");
        btnXoaLoc.setBackground(new Color(108, 117, 125));
        btnXoaLoc.setForeground(Color.WHITE);
        btnXoaLoc.setFont(FONT_BOLD);
        btnXoaLoc.addActionListener(e -> xoaBoLoc());

        pnl.add(new JLabel("Tên khách hàng:"));
        pnl.add(txtTimKiemTenKH);
        pnl.add(new JLabel("Từ ngày:"));
        pnl.add(dateTuNgay);
        pnl.add(new JLabel("Tới ngày:"));
        pnl.add(dateToiNgay);
        pnl.add(new JLabel("Nhân viên:"));
        pnl.add(cbNhanVienLoc);
        pnl.add(btnXoaLoc);

        return pnl;
    }

    private void xoaBoLoc() {
        txtTimKiemTenKH.setText("");
        dateTuNgay.setDate(null);
        dateToiNgay.setDate(null);
        if (cbNhanVienLoc.getItemCount() > 0) {
            cbNhanVienLoc.setSelectedIndex(0);
        }
        loadDauSachHoaDon();

        currentMaHD = "";
        lblMaHDVal.setText("-");
        lblTongTienVal.setText("0 VNĐ");
        lblTongGiamVal.setText("-");
        modelCT.setRowCount(0);
    }

    private JPanel createPanelDanhSachHoaDon() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(new TitledBorder("Hóa đơn đã giao dịch"));
        String[] cols = { "Mã HD", "Ngày lập", "Tên khách hàng" };
        modelHD = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tableHD = new JTable(modelHD);
        tableHD.setFont(FONT_NORMAL);
        tableHD.setRowHeight(30);

        tableHD.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableHD.getSelectedRow();
                if (row != -1) {
                    currentMaHD = tableHD.getValueAt(row, 0).toString();
                    hienThiFullChiTiet(currentMaHD);
                }
            }
        });
        pnl.add(new JScrollPane(tableHD), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createPanelChiTietDayDu() {
        JPanel pnlMain = new JPanel(new BorderLayout(0, 10));
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(new LineBorder(new Color(200, 200, 200)));

        JPanel pnlHeader = new JPanel(new GridLayout(2, 1));
        pnlHeader.setBackground(new Color(0, 102, 204));
        JLabel lblTitle = new JLabel("CHI TIẾT HÓA ĐƠN", JLabel.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 18));
        JLabel lblSub = new JLabel("Hệ Thống Bán Vé Tàu", JLabel.CENTER);
        lblSub.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSub);
        pnlHeader.setPreferredSize(new Dimension(0, 60));

        JPanel pnlInfo = new JPanel(new GridBagLayout());
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        lblMaHDVal = new JLabel("-");
        lblMaHDVal.setFont(FONT_BOLD);
        lblNgayLapVal = new JLabel("-");
        lblNhanVienVal = new JLabel("-");
        lblKhachHangVal = new JLabel("-");

        // Thay đổi Khuyến mãi thành Tổng tiền giảm (gom từ chi tiết)
        lblTongGiamVal = new JLabel("-");

        lblTongTienVal = new JLabel("0 VNĐ");
        lblTongTienVal.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 18));
        lblTongTienVal.setForeground(new Color(220, 53, 69));

        addInfoRow(pnlInfo, "Mã hóa đơn:", lblMaHDVal, 0, gbc);
        addInfoRow(pnlInfo, "Ngày lập:", lblNgayLapVal, 1, gbc);
        addInfoRow(pnlInfo, "Nhân viên:", lblNhanVienVal, 2, gbc);
        addInfoRow(pnlInfo, "Khách hàng:", lblKhachHangVal, 3, gbc);
        addInfoRow(pnlInfo, "Tổng KM giảm:", lblTongGiamVal, 4, gbc);

        // Bổ sung thêm các cột để hiển thị Item-Level Discount
        String[] cols = { "Mã Vé", "Loại Vé", "Tiền Gốc", "Tiền Giảm", "Thành Tiền" };
        modelCT = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tableCT = new JTable(modelCT);
        tableCT.setRowHeight(25);
        JScrollPane scrollTable = new JScrollPane(tableCT);
        scrollTable.setBorder(new TitledBorder("Danh sách vé"));

        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel pnlTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlTotal.setBackground(Color.WHITE);
        pnlTotal.add(new JLabel("TỔNG THANH TOÁN: "));
        pnlTotal.add(lblTongTienVal);

        JButton btnPDF = new JButton("XUẤT HÓA ĐƠN (PDF)");
        btnPDF.setBackground(new Color(40, 167, 69));
        btnPDF.setForeground(Color.WHITE);
        btnPDF.setFont(FONT_BOLD);
        btnPDF.setPreferredSize(new Dimension(0, 40));
        btnPDF.addActionListener(e -> xuatHoaDonPDF(currentMaHD));

        pnlBottom.add(pnlTotal, BorderLayout.NORTH);
        pnlBottom.add(btnPDF, BorderLayout.SOUTH);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.add(pnlInfo, BorderLayout.NORTH);
        pnlCenter.add(scrollTable, BorderLayout.CENTER);
        pnlMain.add(pnlCenter, BorderLayout.CENTER);
        pnlMain.add(pnlBottom, BorderLayout.SOUTH);

        return pnlMain;
    }

    private void xuatHoaDonPDF(String maHD) {
        if (maHD == null || maHD.isEmpty() || maHD.equals("-")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("HoaDon_" + maHD + ".pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();

            float width = 220;
            // Tăng chiều cao thêm một chút do in nhiều dòng thông tin chi tiết KM hơn
            float totalHeight = 350 + (modelCT.getRowCount() * 75);

            com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(width, totalHeight);
            Document document = new Document(pageSize, 10, 10, 10, 10);

            try {
                PdfWriter.getInstance(document, new FileOutputStream(path));
                document.open();

                com.itextpdf.text.Font fHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
                com.itextpdf.text.Font fTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
                com.itextpdf.text.Font fNorm = FontFactory.getFont(FontFactory.HELVETICA, 8);
                com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
                com.itextpdf.text.Font fSmall = FontFactory.getFont(FontFactory.HELVETICA, 7);

                Paragraph h1 = new Paragraph("DUONG SAT VIET NAM", fHead);
                h1.setAlignment(Element.ALIGN_CENTER);
                document.add(h1);

                Paragraph h2 = new Paragraph("HOA DON TAU", fTitle);
                h2.setAlignment(Element.ALIGN_CENTER);
                document.add(h2);

                document.add(new Paragraph("-----------------------------------------------------------", fSmall));
                document.add(new Paragraph("Ma HD: " + lblMaHDVal.getText(), fBold));
                document.add(new Paragraph("Ngay lap: " + lblNgayLapVal.getText(), fNorm));
                document.add(new Paragraph("Khach hang: " + lblKhachHangVal.getText(), fBold));

                document.add(new Paragraph("\nCHI TIET CAC VE:", fBold));
                document.add(new Paragraph("- - - - - - - - - - - - - - - - - - - - - - - - - - -", fSmall));

                for (int i = 0; i < modelCT.getRowCount(); i++) {
                    document.add(new Paragraph("Ma ve: " + modelCT.getValueAt(i, 0) + " (" + modelCT.getValueAt(i, 1) + ")", fBold));
                    document.add(new Paragraph("Gia goc: " + modelCT.getValueAt(i, 2) + " | Giam: " + modelCT.getValueAt(i, 3), fNorm));
                    document.add(new Paragraph("Thanh tien: " + modelCT.getValueAt(i, 4), fBold));
                    document.add(new Paragraph("- - - - - - - - - - - - - - - - - - - - - - - - - - -", fSmall));
                }

                // Ghi tổng tiền
                Paragraph tGiam = new Paragraph("\nTONG GIAM: " + lblTongGiamVal.getText(), fNorm);
                tGiam.setAlignment(Element.ALIGN_RIGHT);
                document.add(tGiam);

                Paragraph t = new Paragraph("TONG TIEN: " + lblTongTienVal.getText(), fTitle);
                t.setAlignment(Element.ALIGN_RIGHT);
                document.add(t);

                document.add(new Paragraph("-----------------------------------------------------------", fSmall));
                Paragraph fNote = new Paragraph("The nay khong co gia tri thanh toan.", fSmall);
                fNote.setAlignment(Element.ALIGN_CENTER);
                document.add(fNote);

                document.close();
                Desktop.getDesktop().open(new java.io.File(path));

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất PDF: " + e.getMessage());
            }
        }
    }

    private void hienThiFullChiTiet(String maHD) {
        modelCT.setRowCount(0);
        double tongTienGiam = 0; // Biến tính tổng KM toàn bộ Hóa Đơn

        try (Connection con = ConnectDB.getConnection()) {
            // 1. Lấy thông tin chung của hóa đơn (Bỏ join với bảng KhuyenMai cũ)
            String sqlHD = "SELECT h.maHD, h.ngayLap, h.tongTien, n.tenNV, k.tenKH "
                    + "FROM HoaDon h "
                    + "LEFT JOIN NhanVien n ON h.maNV = n.maNV "
                    + "LEFT JOIN KhachHang k ON h.maKH = k.maKH "
                    + "WHERE h.maHD = ?";
            PreparedStatement ps = con.prepareStatement(sqlHD);
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblMaHDVal.setText(rs.getString("maHD"));
                lblNgayLapVal.setText(sdf.format(rs.getTimestamp("ngayLap")));
                lblNhanVienVal.setText(rs.getString("tenNV"));
                lblKhachHangVal.setText(rs.getString("tenKH") != null ? rs.getString("tenKH") : "Khách lẻ");
                lblTongTienVal.setText(df.format(rs.getDouble("tongTien")));
            }

            // 2. Lấy danh sách các vé trong hóa đơn đó kèm giá gốc, giá giảm và thành tiền
            String sqlCT = "SELECT ct.maVe, v.maLoaiVe, ct.tienGoc, ct.tienGiam, ct.thanhTien "
                    + "FROM ChiTietHoaDon ct "
                    + "JOIN Ve v ON ct.maVe = v.maVe "
                    + "WHERE ct.maHD = ?";
            PreparedStatement ps2 = con.prepareStatement(sqlCT);
            ps2.setString(1, maHD);
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) {
                double tienGoc = rs2.getDouble("tienGoc");
                double tienGiam = rs2.getDouble("tienGiam");
                double thanhTien = rs2.getDouble("thanhTien");

                tongTienGiam += tienGiam; // Cộng dồn tiền giảm từ các vé

                modelCT.addRow(new Object[] {
                        rs2.getString("maVe"),
                        rs2.getString("maLoaiVe"),
                        df.format(tienGoc),
                        df.format(tienGiam),
                        df.format(thanhTien)
                });
            }

            // Cập nhật lên UI tổng số tiền đã được giảm
            lblTongGiamVal.setText(df.format(tongTienGiam));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDauSachHoaDon() {
        java.util.Date tuNgay = dateTuNgay.getDate();
        java.util.Date toiNgay = dateToiNgay.getDate();
        if (tuNgay != null && toiNgay != null && toiNgay.before(tuNgay)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc không được trước ngày bắt đầu!");
            dateToiNgay.setDate(null);
            return;
        }

        modelHD.setRowCount(0);
        StringBuilder sql = new StringBuilder("SELECT h.maHD, h.ngayLap, k.tenKH FROM HoaDon h "
                + "LEFT JOIN KhachHang k ON h.maKH = k.maKH WHERE 1=1 ");

        try (Connection con = ConnectDB.getConnection()) {
            if (!txtTimKiemTenKH.getText().trim().isEmpty())
                sql.append(" AND k.tenKH LIKE ? ");

            // Lấy Value từ Object NhanVienItem (nếu có sử dụng Wrapper Class)
            if (cbNhanVienLoc.getSelectedIndex() > 0 && cbNhanVienLoc.getSelectedItem() != null) {
                // Giả định hàm toString() hoặc getter trả về chuỗi/đối tượng phân biệt được
                sql.append(" AND h.maNV = ? ");
            }

            if (tuNgay != null) sql.append(" AND h.ngayLap >= ? ");
            if (toiNgay != null) sql.append(" AND h.ngayLap <= ? ");
            sql.append(" ORDER BY h.ngayLap DESC");

            PreparedStatement ps = con.prepareStatement(sql.toString());
            int idx = 1;

            if (!txtTimKiemTenKH.getText().trim().isEmpty())
                ps.setString(idx++, "%" + txtTimKiemTenKH.getText().trim() + "%");

            if (cbNhanVienLoc.getSelectedIndex() > 0 && cbNhanVienLoc.getSelectedItem() != null) {
                // Tùy thuộc vào class NhanVienItem của bạn, chỉnh lại dòng dưới nếu cần (vd: getItem().getMaNV())
                ps.setString(idx++, cbNhanVienLoc.getSelectedItem().toString().split(" - ")[0]); // Giả định toString: "NV01 - Tên"
            }

            if (tuNgay != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(tuNgay);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                ps.setTimestamp(idx++, new java.sql.Timestamp(c.getTimeInMillis()));
            }
            if (toiNgay != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(toiNgay);
                c.set(Calendar.HOUR_OF_DAY, 23);
                c.set(Calendar.MINUTE, 59);
                ps.setTimestamp(idx++, new java.sql.Timestamp(c.getTimeInMillis()));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String tenKH = rs.getString("tenKH");
                modelHD.addRow(new Object[] { rs.getString("maHD"), sdf.format(rs.getTimestamp("ngayLap")),
                        tenKH != null ? tenKH : "Khách lẻ" });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addInfoRow(JPanel pnl, String label, JLabel valLabel, int row, GridBagConstraints gbc) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.1;
        JLabel l = new JLabel(label);
        l.setForeground(Color.GRAY);
        pnl.add(l, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        pnl.add(valLabel, gbc);
    }

    private void loadNhanVienToCombo() {
        cbNhanVienLoc.removeAllItems();
        cbNhanVienLoc.addItem("--- Tất cả ---");
        try (Connection con = ConnectDB.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT maNV, tenNV FROM NhanVien");
            while (rs.next())
                // Lưu ý class NhanVienItem: Nếu đã tạo riêng thì sử dụng, còn ở đây tạm set toString() thành "Mã - Tên" để dễ query
                cbNhanVienLoc.addItem(rs.getString(1) + " - " + rs.getString(2));
        } catch (Exception e) {
        }
    }
}