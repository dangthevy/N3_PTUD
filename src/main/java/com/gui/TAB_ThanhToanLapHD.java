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
    // Khai báo các thành phần giao diện (Tables, ComboBox, Date Choosers...)
    private DefaultTableModel modelHD, modelCT;
    private JTable tableHD, tableCT;
    private JComboBox<Object> cbNhanVienLoc;
    private JDateChooser dateTuNgay, dateToiNgay;
    private JTextField txtTimKiemTenKH;
    private JButton btnXoaLoc;

    // Các nhãn hiển thị thông tin chi tiết hóa đơn bên phải
    private JLabel lblMaHDVal, lblNgayLapVal, lblNhanVienVal, lblKhachHangVal, lblKhuyenMaiVal, lblTongTienVal;
    private String currentMaHD = ""; // Lưu mã hóa đơn đang được chọn

    // Định dạng số tiền và ngày tháng cho đẹp
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Định dạng Font chữ cho giao diện Swing
    private final java.awt.Font FONT_TITLE = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 24);
    private final java.awt.Font FONT_NORMAL = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 13);
    private final java.awt.Font FONT_BOLD = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 14);

    public TAB_ThanhToanLapHD() {
        // Thiết lập layout chính và màu nền
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 240, 240));

        // --- PHẦN TRÊN (NORTH): Tiêu đề và Bộ lọc ---
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        JLabel lblPageTitle = new JLabel("TRA CỨU HÓA ĐƠN", JLabel.CENTER);
        lblPageTitle.setFont(FONT_TITLE);
        lblPageTitle.setForeground(new Color(0, 51, 153));
        lblPageTitle.setBorder(new EmptyBorder(10, 0, 10, 0));

        pnlNorth.add(lblPageTitle, BorderLayout.NORTH);
        pnlNorth.add(createPanelFilter(), BorderLayout.CENTER);

        add(pnlNorth, BorderLayout.NORTH);

        // --- PHẦN GIỮA (CENTER): Chia làm 2 bên (Bên trái: Danh sách HD, Bên phải: Chi
        // tiết) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450); // Độ rộng bên trái
        splitPane.setLeftComponent(createPanelDanhSachHoaDon());
        splitPane.setRightComponent(createPanelChiTietDayDu());

        add(splitPane, BorderLayout.CENTER);

        // Tải dữ liệu ban đầu
        loadNhanVienToCombo();
        loadDauSachHoaDon();
    }

    /**
     * Tạo Panel chứa các thành phần tìm kiếm: Tên KH, Ngày tháng, Nhân viên
     */
    private JPanel createPanelFilter() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnl.setBorder(new TitledBorder("Bộ lọc"));

        txtTimKiemTenKH = new JTextField(12);
        // Sự kiện: Khi gõ phím trong ô tìm kiếm thì tự động tải lại danh sách
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
        // Reset ô nhập tên khách hàng về rỗng
        txtTimKiemTenKH.setText("");

        // Reset hai ô chọn ngày về null (không chọn ngày)
        dateTuNgay.setDate(null);
        dateToiNgay.setDate(null);

        // Đưa ComboBox nhân viên về lựa chọn đầu tiên (thường là "--- Tất cả ---")
        if (cbNhanVienLoc.getItemCount() > 0) {
            cbNhanVienLoc.setSelectedIndex(0);
        }
        loadDauSachHoaDon();

        // (Tùy chọn) Xóa thông tin hiển thị chi tiết bên phải
        currentMaHD = "";
        lblMaHDVal.setText("-");
        lblTongTienVal.setText("0 VNĐ");
        modelCT.setRowCount(0);
    }

    /**
     * Tạo Panel chứa bảng hiển thị danh sách các hóa đơn đã giao dịch
     */
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

        // Sự kiện: Khi người dùng click chọn một dòng trong bảng hóa đơn
        tableHD.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableHD.getSelectedRow();
                if (row != -1) {
                    currentMaHD = tableHD.getValueAt(row, 0).toString();
                    hienThiFullChiTiet(currentMaHD); // Lấy chi tiết vé từ DB lên
                }
            }
        });
        pnl.add(new JScrollPane(tableHD), BorderLayout.CENTER);
        return pnl;
    }

    /**
     * Tạo Panel hiển thị thông tin chi tiết một hóa đơn (bên phải)
     */
    private JPanel createPanelChiTietDayDu() {
        JPanel pnlMain = new JPanel(new BorderLayout(0, 10));
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(new LineBorder(new Color(200, 200, 200)));

        // Header của tờ hóa đơn chi tiết
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

        // Khu vực thông tin khách hàng/nhân viên
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
        lblKhuyenMaiVal = new JLabel("-");
        lblTongTienVal = new JLabel("0 VNĐ");
        lblTongTienVal.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 18));
        lblTongTienVal.setForeground(new Color(220, 53, 69));

        addInfoRow(pnlInfo, "Mã hóa đơn:", lblMaHDVal, 0, gbc);
        addInfoRow(pnlInfo, "Ngày lập:", lblNgayLapVal, 1, gbc);
        addInfoRow(pnlInfo, "Nhân viên:", lblNhanVienVal, 2, gbc);
        addInfoRow(pnlInfo, "Khách hàng:", lblKhachHangVal, 3, gbc);
        addInfoRow(pnlInfo, "Khuyến mãi:", lblKhuyenMaiVal, 4, gbc);

        // Bảng danh sách các vé thuộc hóa đơn này
        String[] cols = { "Mã Vé", "Loại Vé", "Đơn Giá" };
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

        // Phần đáy: Tổng tiền và Nút xuất PDF
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
        btnPDF.addActionListener(e -> xuatHoaDonPDF(currentMaHD)); // Gọi hàm xuất PDF

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

    /**
     * Hàm quan trọng: Thực hiện Logic xuất PDF hóa đơn dạng cuộn dài
     */
    private void xuatHoaDonPDF(String maHD) {
        if (maHD == null || maHD.isEmpty() || maHD.equals("-")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn!");
            return;
        }

        // Hộp thoại chọn nơi lưu file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("HoaDon_" + maHD + ".pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();

            // --- BẮT ĐẦU TÍNH TOÁN KÍCH THƯỚC TRANG ---
            // Đơn vị trong iText là 'points' (72 points = 1 inch)
            float width = 220; // Chiều rộng cố định ~7.7cm (Vừa khổ giấy máy in nhiệt K80)

            // Chiều cao = 350 (cho header, footer và info) + 60 cho mỗi dòng vé có trong
            // bảng
            float totalHeight = 350 + (modelCT.getRowCount() * 60);

            com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(width, totalHeight);

            // Tạo Document với kích thước vừa tính và Margin (lề) nhỏ (10pt)
            Document document = new Document(pageSize, 10, 10, 10, 10);

            try {
                PdfWriter.getInstance(document, new FileOutputStream(path));
                document.open();

                // Tạo các Font chữ sử dụng trong PDF
                com.itextpdf.text.Font fHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
                com.itextpdf.text.Font fTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
                com.itextpdf.text.Font fNorm = FontFactory.getFont(FontFactory.HELVETICA, 8);
                com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
                com.itextpdf.text.Font fSmall = FontFactory.getFont(FontFactory.HELVETICA, 7);

                // Ghi nội dung Header
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

                // Ghi nội dung Body (Duyệt qua từng dòng trong bảng chi tiết vé)
                document.add(new Paragraph("\nCHI TIET CAC VE:", fBold));
                document.add(new Paragraph("- - - - - - - - - - - - - - - - - - - - - - - - - - -", fSmall));

                for (int i = 0; i < modelCT.getRowCount(); i++) {
                    document.add(new Paragraph("Ma ve: " + modelCT.getValueAt(i, 0), fBold));
                    document.add(new Paragraph(
                            "Loai ve: " + modelCT.getValueAt(i, 1) + " | Gia: " + modelCT.getValueAt(i, 2), fNorm));
                    document.add(new Paragraph("- - - - - - - - - - - - - - - - - - - - - - - - - - -", fSmall));
                }

                // Ghi tổng tiền
                Paragraph t = new Paragraph("\nTONG TIEN: " + lblTongTienVal.getText(), fTitle);
                t.setAlignment(Element.ALIGN_RIGHT);
                document.add(t);

                // Ghi Footer
                document.add(new Paragraph("-----------------------------------------------------------", fSmall));
                Paragraph fNote = new Paragraph("The nay khong co gia tri thanh toan.", fSmall);
                fNote.setAlignment(Element.ALIGN_CENTER);
                document.add(fNote);

                document.close();

                // Tự động mở file PDF vừa tạo bằng ứng dụng mặc định của Windows/MacOS
                Desktop.getDesktop().open(new java.io.File(path));

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất PDF: " + e.getMessage());
            }
        }
    }

    /**
     * Truy vấn Database để lấy toàn bộ thông tin HD và chi tiết vé khi click chọn
     * bảng
     */
    private void hienThiFullChiTiet(String maHD) {
        modelCT.setRowCount(0); // Xóa bảng chi tiết cũ
        try (Connection con = ConnectDB.getConnection()) {
            // 1. Lấy thông tin chung của hóa đơn
            String sqlHD = "SELECT h.*, n.tenNV, k.tenKH, km.tenKM FROM HoaDon h "
                    + "LEFT JOIN NhanVien n ON h.maNV = n.maNV " + "LEFT JOIN KhachHang k ON h.maKH = k.maKH "
                    + "LEFT JOIN KhuyenMai km ON h.maKM = km.maKM WHERE h.maHD = ?";
            PreparedStatement ps = con.prepareStatement(sqlHD);
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblMaHDVal.setText(rs.getString("maHD"));
                lblNgayLapVal.setText(sdf.format(rs.getTimestamp("ngayLap")));
                lblNhanVienVal.setText(rs.getString("tenNV"));
                lblKhachHangVal.setText(rs.getString("tenKH"));
                lblKhuyenMaiVal.setText(rs.getString("tenKM") != null ? rs.getString("tenKM") : "Không có");
                lblTongTienVal.setText(df.format(rs.getDouble("thanhTien")));
            }

            // 2. Lấy danh sách các vé trong hóa đơn đó
            String sqlCT = "SELECT ct.maVe, v.maLoaiVe, ct.donGia FROM ChiTietHoaDon ct "
                    + "JOIN Ve v ON ct.maVe = v.maVe WHERE ct.maHD = ?";
            PreparedStatement ps2 = con.prepareStatement(sqlCT);
            ps2.setString(1, maHD);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                modelCT.addRow(new Object[] { rs2.getString(1), rs2.getString(2), df.format(rs2.getDouble(3)) });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tải danh sách hóa đơn lên bảng bên trái dựa theo các bộ lọc tìm kiếm
     */
    private void loadDauSachHoaDon() {
        // Kiểm tra logic ngày tháng hợp lệ
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

        // Xây dựng câu SQL động dựa vào các ô nhập liệu
        try (Connection con = ConnectDB.getConnection()) {
            if (!txtTimKiemTenKH.getText().trim().isEmpty())
                sql.append(" AND k.tenKH LIKE ? ");
            if (cbNhanVienLoc.getSelectedItem() instanceof NhanVienItem)
                sql.append(" AND h.maNV = ? ");
            if (tuNgay != null)
                sql.append(" AND h.ngayLap >= ? ");
            if (toiNgay != null)
                sql.append(" AND h.ngayLap <= ? ");
            sql.append(" ORDER BY h.ngayLap DESC");

            PreparedStatement ps = con.prepareStatement(sql.toString());
            int idx = 1;
            // Truyền tham số vào câu SQL
            if (!txtTimKiemTenKH.getText().trim().isEmpty())
                ps.setString(idx++, "%" + txtTimKiemTenKH.getText().trim() + "%");
            if (cbNhanVienLoc.getSelectedItem() instanceof NhanVienItem)
                ps.setString(idx++, ((NhanVienItem) cbNhanVienLoc.getSelectedItem()).getMaNV());

            // Xử lý thời gian từ 00:00:00 của ngày bắt đầu đến 23:59:59 của ngày kết thúc
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

    // Các hàm phụ trợ khác (Add info row, Load nhân viên vào combo...)
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
                cbNhanVienLoc.addItem(new NhanVienItem(rs.getString(1), rs.getString(2)));
        } catch (Exception e) {
        }
    }
}