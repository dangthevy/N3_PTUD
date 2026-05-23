package com.gui;

import com.dao.DAO_LichSuBaoTri;
import com.entities.LichSuBaoTri;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class Form_LichSuBaoTri extends JDialog {
    private static final Color ACCENT = new Color(0x1A5EAB);
    private static final Color TEXT_DARK = new Color(0x1E2B3C);
    private static final Color BORDER_CLR = new Color(0xE2EAF4);
    
    private JTextField txtLyDo, txtChiPhi;
    private DefaultTableModel model;
    private JTable table;
    
    // UI Lọc & Thống kê
    private JTextField txtSearch;
    private JComboBox<String> cbFilterLoai, cbFilterStatus;
    private JLabel lblTongChiPhi;
    
    private String loaiTaiSan, maTaiSan;
    private String maNhanVien; 
    
    private DAO_LichSuBaoTri daoBaoTri = new DAO_LichSuBaoTri();
    private boolean isConfirmed = false;

    public Form_LichSuBaoTri(Frame parent, String title, String loaiTaiSan, String maTaiSan, boolean modeInputOnly, String maNhanVien) {
        super(parent, title, true);
        this.loaiTaiSan = loaiTaiSan;
        this.maTaiSan = maTaiSan;
        this.maNhanVien = maNhanVien; 
        
        setSize("ALL".equals(loaiTaiSan) ? 850 : 650, modeInputOnly ? 320 : 620);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(15, 15));

        // ----- HEADER TITLE -----
        JLabel lblTitle = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ACCENT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(lblTitle, BorderLayout.NORTH);

        // ----- MAIN CONTENT PANEL -----
        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));

        // 1. Form ghi nhận thông tin (Chỉ hiện khi thao tác bảo trì cụ thể)
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 6, 6, 6);

        txtLyDo = createTextField();
        txtChiPhi = createTextField();
        txtChiPhi.setText("0");

        addFormRow(pnlForm, "Lý do bảo trì (*):", txtLyDo, 0, gc);
        addFormRow(pnlForm, "Dự toán chi phí (VNĐ):", txtChiPhi, 1, gc);
        pnlCenter.add(pnlForm);

        if ("ALL".equals(loaiTaiSan)) {
            pnlForm.setVisible(false); // Ẩn form nhập nếu đang ở chế độ xem TẤT CẢ
        }

        // 2. Bảng hiển thị lịch sử & Bộ lọc
        if (!modeInputOnly) {
            if (!"ALL".equals(loaiTaiSan)) {
                pnlCenter.add(Box.createVerticalStrut(15));
            }
            
            // =================================================================
            // ĐÃ FIX: CHỈ HIỂN THỊ KHU VỰC BỘ LỌC KHI Ở CHẾ ĐỘ XEM TẤT CẢ ("ALL")
            // =================================================================
            if ("ALL".equals(loaiTaiSan)) {
                JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                pnlFilter.setOpaque(false);
                pnlFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JLabel lblFilter = new JLabel("Tra cứu:");
                lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
                pnlFilter.add(lblFilter);
                
                txtSearch = new JTextField(12);
                txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                txtSearch.setPreferredSize(new Dimension(150, 32));
                txtSearch.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_CLR), new EmptyBorder(0, 5, 0, 5)));
                
                cbFilterStatus = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đang sửa chữa", "Đã hoàn tất"});
                cbFilterStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                cbFilterStatus.setPreferredSize(new Dimension(140, 32));
                cbFilterStatus.setBackground(Color.WHITE);
                
                cbFilterLoai = new JComboBox<>(new String[]{"Tất cả loại", "Tàu (TAU)", "Toa (TOA)", "Ghế (GHE)"});
                cbFilterLoai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                cbFilterLoai.setPreferredSize(new Dimension(120, 32));
                cbFilterLoai.setBackground(Color.WHITE);

                pnlFilter.add(txtSearch);
                pnlFilter.add(cbFilterLoai); 
                pnlFilter.add(cbFilterStatus);
                
                pnlCenter.add(pnlFilter);
                pnlCenter.add(Box.createVerticalStrut(10));

                // Gắn sự kiện lọc dữ liệu
                txtSearch.getDocument().addDocumentListener(new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) { loadHistoryData(); }
                    public void removeUpdate(DocumentEvent e) { loadHistoryData(); }
                    public void changedUpdate(DocumentEvent e) { loadHistoryData(); }
                });
                cbFilterStatus.addActionListener(e -> loadHistoryData());
                cbFilterLoai.addActionListener(e -> loadHistoryData());
            }
            
            // --- TẠO TIÊU ĐỀ BẢNG ---
            JLabel lblHistory = new JLabel("ALL".equals(loaiTaiSan) ? "Dữ liệu nhật ký bảo trì toàn hệ thống:" : "Nhật ký sửa chữa trước đây của tài sản này:");
            lblHistory.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblHistory.setForeground(TEXT_DARK);
            lblHistory.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlCenter.add(lblHistory);
            pnlCenter.add(Box.createVerticalStrut(8));

            // Đổi cột nếu đang xem tất cả
            String[] cols;
            if ("ALL".equals(loaiTaiSan)) {
                cols = new String[] { "Loại", "Mã Tài Sản", "Ngày bắt đầu", "Ngày hoàn tất", "Lý do hỏng hóc", "Chi phí" };
            } else {
                cols = new String[] { "Ngày bắt đầu", "Ngày hoàn tất", "Lý do hỏng hóc", "Chi phí" };
            }
            
            model = new DefaultTableModel(cols, 0);
            table = new JTable(model) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            styleTable(table);

            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
            scroll.getViewport().setBackground(Color.WHITE);
            pnlCenter.add(scroll);
            
            loadHistoryData(); // Tải dữ liệu lần đầu
        }
        add(pnlCenter, BorderLayout.CENTER);

        // ----- BOTTOM ACTION BUTTONS & TỔNG CHI PHÍ -----
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));

        // Nhãn tổng chi phí ở góc trái
        lblTongChiPhi = new JLabel("Tổng chi phí: 0 VNĐ");
        lblTongChiPhi.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTongChiPhi.setForeground(new Color(220, 53, 69)); // Màu đỏ cảnh báo
        if ("ALL".equals(loaiTaiSan)) {
            pnlBottom.add(lblTongChiPhi, BorderLayout.WEST);
        }

        // Các nút bấm ở góc phải
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);
        JButton btnCancel = createButton("ALL".equals(loaiTaiSan) ? "Đóng Báo Cáo" : "Hủy Bỏ", new Color(149, 165, 166));
        JButton btnSave = createButton("Xác Nhận Lệnh", ACCENT);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());

        pnlBtns.add(btnCancel);
        if (!"ALL".equals(loaiTaiSan)) {
            pnlBtns.add(btnSave); 
        }
        pnlBottom.add(pnlBtns, BorderLayout.EAST);
        
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel p, String text, JComponent c, int row, GridBagConstraints gc) {
        gc.gridy = row; gc.gridx = 0; gc.weightx = 0.3;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_DARK);
        p.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 0.7;
        p.add(c, gc);
    }

    private void loadHistoryData() {
        if (model == null) return;
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        DecimalFormat df = new DecimalFormat("#,##0 VNĐ");
        
        // Lấy giá trị từ các bộ lọc
        String searchTxt = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        int statusIdx = cbFilterStatus != null ? cbFilterStatus.getSelectedIndex() : 0; // 0: Tất cả, 1: Đang sửa, 2: Đã xong
        int loaiIdx = cbFilterLoai != null ? cbFilterLoai.getSelectedIndex() : 0; // 0: Tất cả, 1: TAU, 2: TOA, 3: GHE

        double totalCost = 0;

        if ("ALL".equals(loaiTaiSan)) {
            try (java.sql.Connection con = com.connectDB.ConnectDB.getConnection();
                 java.sql.PreparedStatement ps = con.prepareStatement("SELECT * FROM LichSuBaoTri ORDER BY ngayBatDau DESC");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                 
                while (rs.next()) {
                    String loai = rs.getString("loaiTaiSan");
                    String maTS = rs.getString("maTaiSan");
                    java.sql.Timestamp endTS = rs.getTimestamp("ngayKetThuc");
                    double chiPhi = rs.getDouble("chiPhi");
                    
                    // --- ÁP DỤNG BỘ LỌC TẠI ĐÂY ---
                    // 1. Lọc theo Loại
                    if (loaiIdx == 1 && !loai.equalsIgnoreCase("TAU")) continue;
                    if (loaiIdx == 2 && !loai.equalsIgnoreCase("TOA")) continue;
                    if (loaiIdx == 3 && !loai.equalsIgnoreCase("GHE")) continue;
                    
                    // 2. Lọc theo Trạng thái
                    if (statusIdx == 1 && endTS != null) continue; // Đang sửa -> endTS phải null
                    if (statusIdx == 2 && endTS == null) continue; // Đã xong -> endTS phải khác null
                    
                    // 3. Lọc theo Text tìm kiếm (Mã hoặc Lý do)
                    if (!searchTxt.isEmpty()) {
                        String lyDo = rs.getString("lyDo").toLowerCase();
                        if (!maTS.toLowerCase().contains(searchTxt) && !lyDo.contains(searchTxt)) {
                            continue;
                        }
                    }

                    // Vượt qua bộ lọc -> Đưa lên bảng và cộng tiền
                    totalCost += chiPhi;
                    String start = rs.getTimestamp("ngayBatDau") != null ? sdf.format(rs.getTimestamp("ngayBatDau")) : "—";
                    String end = endTS != null ? sdf.format(endTS) : "Đang sửa chữa...";
                    
                    model.addRow(new Object[]{ loai, maTS, start, end, rs.getString("lyDo"), df.format(chiPhi) });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Xem cho 1 tài sản cụ thể
            List<LichSuBaoTri> list = daoBaoTri.getLichSuByTaiSan(loaiTaiSan, maTaiSan);
            for (LichSuBaoTri log : list) {
                // Áp dụng bộ lọc trạng thái & tìm kiếm
                boolean isDone = log.getNgayKetThuc() != null;
                if (statusIdx == 1 && isDone) continue;
                if (statusIdx == 2 && !isDone) continue;
                
                if (!searchTxt.isEmpty() && !log.getLyDo().toLowerCase().contains(searchTxt)) {
                    continue;
                }
                
                totalCost += log.getChiPhi();
                String start = log.getNgayBatDau() != null ? sdf.format(log.getNgayBatDau()) : "—";
                String end = isDone ? sdf.format(log.getNgayKetThuc()) : "Đang sửa chữa...";
                model.addRow(new Object[]{ start, end, log.getLyDo(), df.format(log.getChiPhi()) });
            }
        }
        
        // Cập nhật nhãn tổng tiền
        if (lblTongChiPhi != null) {
            lblTongChiPhi.setText("Tổng chi phí: " + df.format(totalCost));
        }
    }

    private void handleSave() {
        String lyDo = txtLyDo.getText().trim();
        if (lyDo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập lý do bảo trì thiết bị!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double chiPhi = 0;
        try {
            chiPhi = Double.parseDouble(txtChiPhi.getText().trim());
            if (chiPhi < 0) throw new Exception();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Chi phí dự toán phải là chữ số nguyên dương!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LichSuBaoTri log = new LichSuBaoTri(loaiTaiSan, maTaiSan, lyDo, chiPhi, this.maNhanVien);
        if (daoBaoTri.ghiNhanBaoTri(log)) {
            isConfirmed = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối hệ thống dữ liệu bảo trì tài sản!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(0, 36));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_CLR), BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        return tf;
    }

    private JButton createButton(String text, Color bg) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setPreferredSize(new Dimension(130, 36));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(36);
        t.setShowVerticalLines(false);
        t.setGridColor(BORDER_CLR);
        t.setFocusable(false);
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));
        t.getTableHeader().setBackground(ACCENT);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Căn giữa nội dung cột loại tài sản nếu có
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        if ("ALL".equals(loaiTaiSan)) {
            t.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Cột Loại (TAU/TOA/GHE)
            t.getColumnModel().getColumn(0).setPreferredWidth(60);
        }
    }

    public boolean isConfirmed() { return isConfirmed; }
}