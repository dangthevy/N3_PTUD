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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class Form_LichSuBaoTri extends JDialog {
    private static final Color ACCENT = new Color(0x1A5EAB);
    private static final Color TEXT_DARK = new Color(0x1E2B3C);
    private static final Color BORDER_CLR = new Color(0xE2EAF4);
    
    // Placeholder cho thanh tìm kiếm
    private final String SEARCH_PLACEHOLDER = "Tra cứu theo mã, lý do, tên NV...";
    
    private JTextField txtLyDo, txtChiPhi;
    private DefaultTableModel model;
    private JTable table;
    
    private JTextField txtSearch;
    private JComboBox<String> cbFilterLoai, cbFilterStatus;
    private JLabel lblTongChiPhi;
    
    private String loaiTaiSan, maTaiSan;
    private String maNhanVien; 
    private boolean isHoanTat; 
    
    private DAO_LichSuBaoTri daoBaoTri = new DAO_LichSuBaoTri();
    private boolean isConfirmed = false;

    public Form_LichSuBaoTri(Frame parent, String title, String loaiTaiSan, String maTaiSan, boolean modeInputOnly, String maNhanVien, boolean isHoanTat) {
        super(parent, title, true);
        this.loaiTaiSan = loaiTaiSan;
        this.maTaiSan = maTaiSan;
        this.maNhanVien = maNhanVien; 
        this.isHoanTat = isHoanTat;
        
        setSize("ALL".equals(loaiTaiSan) ? 980 : 720, modeInputOnly ? 320 : 660); // Tăng form ra một xíu cho rộng rãi
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(15, 15));

        // ----- HEADER TITLE -----
        JLabel lblTitle = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ACCENT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(lblTitle, BorderLayout.NORTH);

        // ----- MAIN CONTENT PANEL -----
        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));

        // 1. TẠO FORM NHẬP ĐỘNG
        if (!"ALL".equals(loaiTaiSan)) {
            JPanel pnlForm = new JPanel(new GridBagLayout());
            pnlForm.setOpaque(false);
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(6, 6, 6, 6);

            if (!isHoanTat) { 
                txtLyDo = createTextField();
                addFormRow(pnlForm, "Lý do bảo trì (*):", txtLyDo, 0, gc);
            } else {          
                txtChiPhi = createTextField();
                txtChiPhi.setText("0");
                addFormRow(pnlForm, "Chi phí sửa chữa thực tế (VNĐ):", txtChiPhi, 0, gc);
            }
            pnlCenter.add(pnlForm);
        }

        // 2. BẢNG HIỂN THỊ LỊCH SỬ & BỘ LỌC HIỆN ĐẠI
        if (!modeInputOnly) {
            if (!"ALL".equals(loaiTaiSan)) {
                pnlCenter.add(Box.createVerticalStrut(15));
            }
            
            // ==========================================
            // UI BỘ LỌC ĐƯỢC THIẾT KẾ LẠI (REDESIGNED)
            // ==========================================
            if ("ALL".equals(loaiTaiSan)) {
                JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
                pnlFilter.setOpaque(false);
                pnlFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlFilter.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
                
                // Ô tìm kiếm có Placeholder giả
                txtSearch = new JTextField(SEARCH_PLACEHOLDER, 18);
                txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                txtSearch.setPreferredSize(new Dimension(280, 38));
                txtSearch.setForeground(Color.GRAY);
                txtSearch.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_CLR, 1, true), 
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                
                // Hiệu ứng Placeholder
                txtSearch.addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        if (txtSearch.getText().equals(SEARCH_PLACEHOLDER)) {
                            txtSearch.setText("");
                            txtSearch.setForeground(TEXT_DARK);
                        }
                    }
                    public void focusLost(FocusEvent e) {
                        if (txtSearch.getText().isEmpty()) {
                            txtSearch.setForeground(Color.GRAY);
                            txtSearch.setText(SEARCH_PLACEHOLDER);
                        }
                    }
                });
                
                // ComboBox phân loại có thêm biểu tượng
                cbFilterLoai = new JComboBox<>(new String[]{"Tất cả loại tài sản", "Tàu", "Toa", "Ghế"});
                styleComboBox(cbFilterLoai, 180);
                
                // ComboBox trạng thái có thêm biểu tượng
                cbFilterStatus = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đang sửa chữa", "Đã hoàn tất"});
                styleComboBox(cbFilterStatus, 170);

                pnlFilter.add(txtSearch);
                pnlFilter.add(cbFilterLoai); 
                pnlFilter.add(cbFilterStatus);
                
                pnlCenter.add(pnlFilter);

                // Lắng nghe sự kiện
                txtSearch.getDocument().addDocumentListener(new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) { loadHistoryData(); }
                    public void removeUpdate(DocumentEvent e) { loadHistoryData(); }
                    public void changedUpdate(DocumentEvent e) { loadHistoryData(); }
                });
                cbFilterStatus.addActionListener(e -> loadHistoryData());
                cbFilterLoai.addActionListener(e -> loadHistoryData());
            }
            // ==========================================
            
            JLabel lblHistory = new JLabel("ALL".equals(loaiTaiSan) ? "Dữ liệu nhật ký bảo trì toàn hệ thống:" : "Nhật ký sửa chữa trước đây của tài sản này:");
            lblHistory.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblHistory.setForeground(TEXT_DARK);
            lblHistory.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlCenter.add(lblHistory);
            pnlCenter.add(Box.createVerticalStrut(8));

            String[] cols;
            if ("ALL".equals(loaiTaiSan)) {
                cols = new String[] { "Loại", "Mã Tài Sản", "Ngày bắt đầu", "Ngày hoàn tất", "Lý do hỏng hóc", "Chi phí", "Người thực hiện" };
            } else {
                cols = new String[] { "Ngày bắt đầu", "Ngày hoàn tất", "Lý do hỏng hóc", "Chi phí", "Người thực hiện" };
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
            
            loadHistoryData(); 
        }
        add(pnlCenter, BorderLayout.CENTER);

        // ----- BOTTOM ACTION BUTTONS & TỔNG CHI PHÍ -----
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));

        lblTongChiPhi = new JLabel("Tổng chi phí: 0 VNĐ");
        lblTongChiPhi.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongChiPhi.setForeground(new Color(220, 53, 69)); 
        
        if ("ALL".equals(loaiTaiSan)) {
            pnlBottom.add(lblTongChiPhi, BorderLayout.WEST);
        }

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

    // --- HÀM TRANG TRÍ RIÊNG CHO COMBOBOX ---
    private void styleComboBox(JComboBox<String> cb, int width) {
        cb.setPreferredSize(new Dimension(width, 38));
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setBackground(Color.WHITE);
        cb.setForeground(TEXT_DARK);
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cb.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_CLR, 1, true), 
            BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        
        // Custom Renderer để list danh sách có khoảng cách (padding) đẹp hơn
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); // Padding rộng
                if (isSelected) {
                    lbl.setBackground(new Color(0xE8F0FB)); // Màu highlight xanh dương nhạt
                    lbl.setForeground(ACCENT);
                } else {
                    lbl.setBackground(Color.WHITE);
                }
                return lbl;
            }
        });
    }

    private String getTenNhanVienByMa(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return "—";
        String sql = "SELECT tenNV FROM NhanVien WHERE maNV = ?";
        try (Connection con = com.connectDB.ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tenNV");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maNV; 
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
        
        // Cập nhật để loại trừ text của placeholder ra khỏi bộ lọc
        String searchTxt = "";
        if (txtSearch != null) {
            String raw = txtSearch.getText().trim();
            if (!raw.equals(SEARCH_PLACEHOLDER)) {
                searchTxt = raw.toLowerCase();
            }
        }
        
        int statusIdx = cbFilterStatus != null ? cbFilterStatus.getSelectedIndex() : 0; 
        int loaiIdx = cbFilterLoai != null ? cbFilterLoai.getSelectedIndex() : 0; 

        double totalCost = 0;

        if ("ALL".equals(loaiTaiSan)) {
            try (Connection con = com.connectDB.ConnectDB.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT * FROM LichSuBaoTri ORDER BY ngayBatDau DESC");
                 ResultSet rs = ps.executeQuery()) {
                 
                while (rs.next()) {
                    String loai = rs.getString("loaiTaiSan");
                    String maTS = rs.getString("maTaiSan");
                    java.sql.Timestamp endTS = rs.getTimestamp("ngayKetThuc");
                    double chiPhi = rs.getDouble("chiPhi");
                    String maNV = rs.getString("nguoiThucHien");
                    
                    if (loaiIdx == 1 && !loai.equalsIgnoreCase("TAU")) continue;
                    if (loaiIdx == 2 && !loai.equalsIgnoreCase("TOA")) continue;
                    if (loaiIdx == 3 && !loai.equalsIgnoreCase("GHE")) continue;
                    if (statusIdx == 1 && endTS != null) continue; 
                    if (statusIdx == 2 && endTS == null) continue; 
                    
                    String tenNV = getTenNhanVienByMa(maNV);

                    if (!searchTxt.isEmpty()) {
                        String lyDo = rs.getString("lyDo").toLowerCase();
                        if (!maTS.toLowerCase().contains(searchTxt) && 
                            !lyDo.contains(searchTxt) && 
                            !tenNV.toLowerCase().contains(searchTxt)) {
                            continue;
                        }
                    }

                    totalCost += chiPhi;
                    String start = rs.getTimestamp("ngayBatDau") != null ? sdf.format(rs.getTimestamp("ngayBatDau")) : "—";
                    String end = endTS != null ? sdf.format(endTS) : "Đang sửa chữa...";
                    
                    model.addRow(new Object[]{ loai, maTS, start, end, rs.getString("lyDo"), df.format(chiPhi), tenNV });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            List<LichSuBaoTri> list = daoBaoTri.getLichSuByTaiSan(loaiTaiSan, maTaiSan);
            for (LichSuBaoTri log : list) {
                boolean isDone = log.getNgayKetThuc() != null;
                if (statusIdx == 1 && isDone) continue;
                if (statusIdx == 2 && !isDone) continue;
                
                String tenNV = getTenNhanVienByMa(log.getNguoiThucHien());

                if (!searchTxt.isEmpty() && 
                    !log.getLyDo().toLowerCase().contains(searchTxt) && 
                    !tenNV.toLowerCase().contains(searchTxt)) {
                    continue;
                }
                
                totalCost += log.getChiPhi();
                String start = log.getNgayBatDau() != null ? sdf.format(log.getNgayBatDau()) : "—";
                String end = isDone ? sdf.format(log.getNgayKetThuc()) : "Đang sửa chữa...";
                
                model.addRow(new Object[]{ start, end, log.getLyDo(), df.format(log.getChiPhi()), tenNV });
            }
        }
        
        if (lblTongChiPhi != null) {
            lblTongChiPhi.setText("Tổng chi phí: " + df.format(totalCost));
        }
    }

    private void handleSave() {
        if (!isHoanTat) {
            String lyDo = txtLyDo.getText().trim();
            if (lyDo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập lý do bảo trì thiết bị!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LichSuBaoTri log = new LichSuBaoTri(loaiTaiSan, maTaiSan, lyDo, 0, this.maNhanVien);
            
            if (daoBaoTri.ghiNhanBaoTri(log)) {
                isConfirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi kết nối hệ thống dữ liệu bảo trì tài sản!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            double chiPhi = 0;
            try {
                chiPhi = Double.parseDouble(txtChiPhi.getText().trim());
                if (chiPhi < 0) throw new Exception();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Chi phí dự toán phải là chữ số nguyên dương hợp lệ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (daoBaoTri.hoanTatBaoTri(loaiTaiSan, maTaiSan, chiPhi)) {
                isConfirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật hoàn tất bảo trì xuống hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(0, 36));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_CLR), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
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
        b.setPreferredSize(new Dimension(140, 40));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setRowHeight(38);
        t.setShowVerticalLines(false);
        t.setGridColor(BORDER_CLR);
        t.setFocusable(false);
        t.getTableHeader().setPreferredSize(new Dimension(0, 40));
        t.getTableHeader().setBackground(ACCENT);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        if ("ALL".equals(loaiTaiSan)) {
            t.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); 
            t.getColumnModel().getColumn(0).setPreferredWidth(60);
        }
    }

    public boolean isConfirmed() { return isConfirmed; }
}