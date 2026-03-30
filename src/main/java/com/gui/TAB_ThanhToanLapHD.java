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
import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.FontFactory;

public class TAB_ThanhToanLapHD extends JPanel {
    private DefaultTableModel modelHD, modelCT;
    private JTable tableHD, tableCT;
    private JComboBox<Object> cbNhanVienLoc;
    private JDateChooser dateLoc;
    private JTextField txtTimKiemMaHD;

    private JLabel lblMaHDVal, lblNgayLapVal, lblNhanVienVal, lblKhachHangVal, lblKhuyenMaiVal, lblTongTienVal;
    private String currentMaHD = "";

    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public TAB_ThanhToanLapHD() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 240, 240));

        // --- PANEL PHÍA BẮC: TIÊU ĐỀ VÀ BỘ LỌC ---
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        JLabel lblPageTitle = new JLabel("THANH TOÁN VÀ LẬP HÓA ĐƠN", JLabel.CENTER);
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblPageTitle.setForeground(new Color(0, 51, 153));
        lblPageTitle.setBorder(new EmptyBorder(10, 0, 10, 0));

        pnlNorth.add(lblPageTitle, BorderLayout.NORTH);
        pnlNorth.add(createPanelFilter(), BorderLayout.CENTER);

        add(pnlNorth, BorderLayout.NORTH);

        // --- PHẦN THÂN: DANH SÁCH VÀ CHI TIẾT ---
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
        pnl.setBorder(new TitledBorder("Bộ lọc nhanh"));
        
        txtTimKiemMaHD = new JTextField(10);
        txtTimKiemMaHD.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { loadDauSachHoaDon(); }
        });
        cbNhanVienLoc = new JComboBox<>();
        cbNhanVienLoc.addActionListener(e -> loadDauSachHoaDon());
        dateLoc = new JDateChooser();
        dateLoc.setPreferredSize(new Dimension(120, 25));
        dateLoc.addPropertyChangeListener("date", e -> loadDauSachHoaDon());
        
        pnl.add(new JLabel("Mã HD:")); pnl.add(txtTimKiemMaHD);
        pnl.add(new JLabel("Nhân viên:")); pnl.add(cbNhanVienLoc);
        pnl.add(new JLabel("Ngày:")); pnl.add(dateLoc);
        return pnl;
    }

    private JPanel createPanelDanhSachHoaDon() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(new TitledBorder("Lịch sử giao dịch"));
        String[] cols = {"Mã HD", "Ngày lập", "Thành tiền"};
        modelHD = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableHD = new JTable(modelHD);
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
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel lblSub = new JLabel("Ga tàu hỏa Việt Nam", JLabel.CENTER);
        lblSub.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle); pnlHeader.add(lblSub);
        pnlHeader.setPreferredSize(new Dimension(0, 60));

        JPanel pnlInfo = new JPanel(new GridBagLayout());
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        lblMaHDVal = new JLabel("-"); lblMaHDVal.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblNgayLapVal = new JLabel("-");
        lblNhanVienVal = new JLabel("-");
        lblKhachHangVal = new JLabel("-");
        lblKhuyenMaiVal = new JLabel("-");
        lblTongTienVal = new JLabel("0 VNĐ");
        lblTongTienVal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTongTienVal.setForeground(new Color(220, 53, 69));

        addInfoRow(pnlInfo, "Mã hóa đơn:", lblMaHDVal, 0, gbc);
        addInfoRow(pnlInfo, "Ngày lập:", lblNgayLapVal, 1, gbc);
        addInfoRow(pnlInfo, "Nhân viên:", lblNhanVienVal, 2, gbc);
        addInfoRow(pnlInfo, "Khách hàng:", lblKhachHangVal, 3, gbc);
        addInfoRow(pnlInfo, "Khuyến mãi:", lblKhuyenMaiVal, 4, gbc);

        String[] cols = {"Mã Vé", "Loại Vé", "Đơn Giá"};
        modelCT = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
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
        btnPDF.setFont(new Font("SansSerif", Font.BOLD, 14));
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

    private void addInfoRow(JPanel pnl, String label, JLabel valLabel, int row, GridBagConstraints gbc) {
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0.1;
        JLabel l = new JLabel(label); l.setForeground(Color.GRAY);
        pnl.add(l, gbc);
        gbc.gridx = 1; gbc.weightx = 0.9;
        pnl.add(valLabel, gbc);
    }

    private void hienThiFullChiTiet(String maHD) {
        modelCT.setRowCount(0);
        try (Connection con = ConnectDB.getConnection()) {
            String sqlHD = "SELECT h.*, n.tenNV, k.tenKH, km.tenKM FROM HoaDon h " +
                           "LEFT JOIN NhanVien n ON h.maNV = n.maNV " +
                           "LEFT JOIN KhachHang k ON h.maKH = k.maKH " +
                           "LEFT JOIN KhuyenMai km ON h.maKM = km.maKM WHERE h.maHD = ?";
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
            String sqlCT = "SELECT ct.maVe, v.maLoaiVe, ct.donGia FROM ChiTietHoaDon ct " +
                           "JOIN Ve v ON ct.maVe = v.maVe WHERE ct.maHD = ?";
            PreparedStatement ps2 = con.prepareStatement(sqlCT);
            ps2.setString(1, maHD);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                modelCT.addRow(new Object[]{ rs2.getString(1), rs2.getString(2), df.format(rs2.getDouble(3)) });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void xuatHoaDonPDF(String maHD) {
        if (maHD == null || maHD.isEmpty() || maHD.equals("-")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn từ danh sách!");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file PDF");
        fileChooser.setSelectedFile(new java.io.File("HoaDon_" + maHD + ".pdf"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(path));
                document.open();
                com.itextpdf.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("HOA DON THANH TOAN GA TAU", boldFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Ma HD: " + lblMaHDVal.getText()));
                document.add(new Paragraph("Ngay lap: " + lblNgayLapVal.getText()));
                document.add(new Paragraph("Khach hang: " + lblKhachHangVal.getText()));
                document.add(new Paragraph("Nhan vien: " + lblNhanVienVal.getText()));
                document.add(new Paragraph(" "));
                PdfPTable pdfTable = new PdfPTable(3);
                pdfTable.setWidthPercentage(100);
                pdfTable.addCell("Ma Ve"); pdfTable.addCell("Loai Ve"); pdfTable.addCell("Don Gia");
                for (int i = 0; i < modelCT.getRowCount(); i++) {
                    pdfTable.addCell(modelCT.getValueAt(i, 0).toString());
                    pdfTable.addCell(modelCT.getValueAt(i, 1).toString());
                    pdfTable.addCell(modelCT.getValueAt(i, 2).toString());
                }
                document.add(pdfTable);
                Paragraph total = new Paragraph("\nTONG THANH TOAN: " + lblTongTienVal.getText(), boldFont);
                total.setAlignment(Element.ALIGN_RIGHT);
                document.add(total);
                document.close();
                JOptionPane.showMessageDialog(this, "Đã xuất PDF thành công!");
                Desktop.getDesktop().open(new java.io.File(path));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void loadDauSachHoaDon() {
        modelHD.setRowCount(0);
        StringBuilder sql = new StringBuilder("SELECT maHD, ngayLap, thanhTien FROM HoaDon WHERE 1=1 ");
        try (Connection con = ConnectDB.getConnection()) {
            if (!txtTimKiemMaHD.getText().trim().isEmpty()) sql.append(" AND maHD LIKE ? ");
            if (cbNhanVienLoc.getSelectedItem() instanceof NhanVienItem) sql.append(" AND maNV = ? ");
            if (dateLoc.getDate() != null) sql.append(" AND CAST(ngayLap AS DATE) = ? ");
            sql.append(" ORDER BY ngayLap DESC");
            PreparedStatement ps = con.prepareStatement(sql.toString());
            int idx = 1;
            if (!txtTimKiemMaHD.getText().trim().isEmpty()) ps.setString(idx++, "%" + txtTimKiemMaHD.getText().trim() + "%");
            if (cbNhanVienLoc.getSelectedItem() instanceof NhanVienItem) ps.setString(idx++, ((NhanVienItem) cbNhanVienLoc.getSelectedItem()).getMaNV());
            if (dateLoc.getDate() != null) ps.setDate(idx++, new java.sql.Date(dateLoc.getDate().getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelHD.addRow(new Object[]{ rs.getString(1), sdf.format(rs.getTimestamp(2)), df.format(rs.getDouble(3)) });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadNhanVienToCombo() {
        cbNhanVienLoc.addItem("--- Tất cả ---");
        try (Connection con = ConnectDB.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT maNV, tenNV FROM NhanVien");
            while (rs.next()) cbNhanVienLoc.addItem(new NhanVienItem(rs.getString(1), rs.getString(2)));
        } catch (Exception e) {}
    }
}