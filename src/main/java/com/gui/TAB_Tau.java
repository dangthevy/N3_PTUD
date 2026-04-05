package com.gui;

import com.connectDB.ConnectDB;
import com.dao.DAO_Tau;
import com.dao.DAO_Toa;
import com.dao.DAO_ChoNgoi;
import com.entities.ChoNgoi;
import com.entities.Tau;
import com.entities.Toa;
import com.enums.TrangThaiCho;
import com.enums.TrangThaiTau;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TAB_Tau extends JPanel {
    // ================= COLOR =================
    private static final Color BG_PAGE     = new Color(0xF4F7FB);
    private static final Color BG_CARD     = Color.WHITE;
    private static final Color ACCENT      = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR  = new Color(0x2270CC);
    private static final Color ACCENT_FOC  = new Color(0x4D9DE0);
    private static final Color TEXT_DARK   = new Color(0x1E2B3C);
    private static final Color TEXT_MID    = new Color(0x5A6A7D);
    private static final Color TEXT_LIGHT  = new Color(0xA0AEC0);
    private static final Color BORDER      = new Color(0xE2EAF4);
    private static final Color ROW_ALT     = new Color(0xF7FAFF);
    private static final Color ROW_SEL     = new Color(0xDDEEFF);
    private static final Color BTN2_BG     = new Color(0xF0F4FA);
    private static final Color BTN2_FG     = new Color(0x3A5A8C);

    // ================= FONT =================
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER, SUCCESS, WARNING }

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JButton btnAdd, btnRefresh, btnSearch, btnDelete, btnUndo;

    private JLabel lblTotal, lblActive, lblMaintenance, lblStopped;
    private DAO_Tau tauDAO = new DAO_Tau();
    
    // --- LƯU TRỮ DỮ LIỆU ĐỂ HOÀN TÁC ---
    private Tau deletedTau = null;
    private List<Toa> deletedToaList = null;
    private Map<String, List<ChoNgoi>> deletedChoNgoiMap = new HashMap<>();

    public TAB_Tau() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initUI();
        initEvents();
        loadDataFromDatabase();
    }

    private void initUI() {
        JPanel pnlTop = new JPanel(new BorderLayout(0, 20));
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ ĐOÀN TÀU");
        lblTitle.setFont(F_TITLE);
        lblTitle.setForeground(ACCENT);
        pnlTop.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlDashboard = new JPanel(new GridLayout(1, 4, 20, 0));
        pnlDashboard.setOpaque(false);
        pnlDashboard.add(createStatCard("TỔNG SỐ TÀU", lblTotal = new JLabel("0"), ACCENT));
        pnlDashboard.add(createStatCard("ĐANG HOẠT ĐỘNG", lblActive = new JLabel("0"), new Color(39, 174, 96)));
        pnlDashboard.add(createStatCard("ĐANG BẢO TRÌ", lblMaintenance = new JLabel("0"), new Color(243, 156, 18)));
        pnlDashboard.add(createStatCard("NGƯNG HOẠT ĐỘNG", lblStopped = new JLabel("0"), new Color(192, 57, 43)));
        pnlTop.add(pnlDashboard, BorderLayout.CENTER);

        JPanel pnlCenter = makeCard(new BorderLayout(0, 15));
        pnlCenter.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(), new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel pnlToolbar = new JPanel(new BorderLayout());
        pnlToolbar.setOpaque(false);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlSearch.setOpaque(false);
        txtSearch = makeField("Nhập tên tàu để tìm...");
        txtSearch.setPreferredSize(new Dimension(300, 36));
        btnSearch = makeBtn("Tìm kiếm", BtnStyle.PRIMARY);
        pnlSearch.add(txtSearch);
        pnlSearch.add(Box.createHorizontalStrut(10));
        pnlSearch.add(btnSearch);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);
        
        btnUndo = makeBtn("↩ Hoàn tác", BtnStyle.WARNING);
        btnUndo.setVisible(false); 
        btnDelete = makeBtn("- Xóa Tàu", BtnStyle.DANGER);
        btnAdd = makeBtn("+ Thêm Mới", BtnStyle.SUCCESS);
        btnRefresh = makeBtn("Làm Mới", BtnStyle.SECONDARY);
        
        pnlButtons.add(btnUndo);
        pnlButtons.add(btnDelete);
        pnlButtons.add(btnAdd);
        pnlButtons.add(btnRefresh);

        pnlToolbar.add(pnlSearch, BorderLayout.WEST);
        pnlToolbar.add(pnlButtons, BorderLayout.EAST);

        String[] columns = {"STT", "Mã Tàu", "Tên Tàu", "Số Toa", "Trạng thái"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER));
        scrollPane.getViewport().setBackground(BG_CARD);
        styleScrollBar(scrollPane.getVerticalScrollBar());

        pnlCenter.add(pnlToolbar, BorderLayout.NORTH);
        pnlCenter.add(scrollPane, BorderLayout.CENTER);

        add(pnlTop, BorderLayout.NORTH);
        add(pnlCenter, BorderLayout.CENTER);
    }

    private void initEvents() {
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { performSearch(); }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) openUpdateForm();
            }
        });

        btnAdd.addActionListener(e -> {
            Form_Tau form = new Form_Tau((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Tàu Mới");
            form.setVisible(true);
            if (form.isConfirmed()) {
                Tau newTau = form.getEntity();
                if (tauDAO.getTauByMa(newTau.getMaTau()) != null) {
                    JOptionPane.showMessageDialog(this, "Mã tàu đã tồn tại trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (tauDAO.insertTau(newTau)) {
                    loadDataFromDatabase();
                    
                    // Xóa bộ nhớ hoàn tác để tránh lỗi trùng lặp khi vừa Xóa xong lại Thêm mới
                    clearUndoCache();
                    
                    JOptionPane.showMessageDialog(this, "Đã thêm tàu mới thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadDataFromDatabase(); });
        btnSearch.addActionListener(e -> performSearch());
        
        btnDelete.addActionListener(e -> handleDeleteTau());
        btnUndo.addActionListener(e -> handleUndoDelete());
    }

    // ================= XỬ LÝ NGHIỆP VỤ XÓA & HOÀN TÁC =================
    private void handleDeleteTau() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 tàu trong bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String maTau = (String) model.getValueAt(row, 1);
        
        DAO_Toa toaDAO = new DAO_Toa();
        DAO_ChoNgoi cnDAO = new DAO_ChoNgoi();
        List<Toa> listToa = toaDAO.getToaByMaTau(maTau);
        
        int totalSold = 0;
        for (Toa t : listToa) {
            totalSold += cnDAO.countGheByTrangThai(t.getMaToa(), TrangThaiCho.DADAT);
        }
        
        if (totalSold > 0) {
            JOptionPane.showMessageDialog(this, "Không thể xóa! Tàu này đang có " + totalSold + " ghế đã được bán vé.", "Lỗi Ràng Buộc", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa Tàu " + maTau + " và toàn bộ sơ đồ toa/ghế của nó?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if(confirm != JOptionPane.YES_OPTION) return;

        // Lưu trữ lại dữ liệu trước khi xóa
        deletedTau = tauDAO.getTauByMa(maTau);
        deletedToaList = listToa;
        deletedChoNgoiMap.clear();
        for (Toa t : listToa) {
            deletedChoNgoiMap.put(t.getMaToa(), cnDAO.getChoNgoiByToa(t.getMaToa()));
        }
        
        // Thực hiện xóa Cascade (SQL Server sẽ tự động xóa Toa và Chỗ Ngồi nhờ ON DELETE CASCADE)
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Ta chỉ cần xóa Tàu, DB sẽ tự động xóa các bảng con
                PreparedStatement ps = con.prepareStatement("DELETE FROM Tau WHERE maTau = ?");
                ps.setString(1, maTau); 
                ps.executeUpdate();
                
                con.commit();
                
                loadDataFromDatabase();
                btnUndo.setVisible(true); // Hiện nút hoàn tác
                JOptionPane.showMessageDialog(this, "Đã xóa tàu thành công! Bạn có thể hoàn tác nếu muốn.");
            } catch (Exception ex) {
                con.rollback();
                JOptionPane.showMessageDialog(this, "Xóa thất bại do lỗi hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleUndoDelete() {
        if (deletedTau == null) return;
        
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Phục hồi Tàu
                PreparedStatement psTau = con.prepareStatement("INSERT INTO Tau (maTau, tenTau, soToa, trangThai) VALUES (?, ?, ?, ?)");
                psTau.setString(1, deletedTau.getMaTau());
                psTau.setString(2, deletedTau.getTenTau());
                psTau.setInt(3, deletedTau.getSoToa());
                psTau.setString(4, deletedTau.getTrangThaiTau().name());
                psTau.executeUpdate();
                
                // 2. Phục hồi Toa
                PreparedStatement psToa = con.prepareStatement("INSERT INTO Toa (maToa, tenToa, soGhe, maTau, maLoaiToa) VALUES (?, ?, ?, ?, ?)");
                for (Toa t : deletedToaList) {
                    psToa.setString(1, t.getMaToa());
                    psToa.setString(2, t.getTenToa());
                    psToa.setInt(3, t.getSoGhe());
                    psToa.setString(4, t.getTau().getMaTau());
                    psToa.setString(5, t.getLoaiToa().getMaLoaiToa());
                    psToa.addBatch();
                }
                psToa.executeBatch();
                
                // 3. Phục hồi Ghế
                PreparedStatement psGhe = con.prepareStatement("INSERT INTO ChoNgoi (maCho, tenCho, maToa, trangThai) VALUES (?, ?, ?, ?)");
                for (List<ChoNgoi> listGhe : deletedChoNgoiMap.values()) {
                    for (ChoNgoi cn : listGhe) {
                        psGhe.setString(1, cn.getMaCho());
                        psGhe.setString(2, cn.getTenCho());
                        // Pharse mã toa từ mã ghế
                        String maToa = cn.getMaCho().split("-")[0];
                        psGhe.setString(3, maToa);
                        psGhe.setString(4, cn.getTrangThai().name());
                        psGhe.addBatch();
                    }
                }
                psGhe.executeBatch();
                
                con.commit();
                
                clearUndoCache();
                loadDataFromDatabase();
                JOptionPane.showMessageDialog(this, "Hoàn tác thành công! Toàn bộ dữ liệu tàu đã được phục hồi.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                con.rollback();
                JOptionPane.showMessageDialog(this, "Hoàn tác thất bại do lỗi dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void openUpdateForm() {
        int row = table.getSelectedRow();
        if(row < 0) return;
        
        String ma = (String) model.getValueAt(row, 1);
        Tau tauData = tauDAO.getTauByMa(ma);
        if (tauData != null) {
            Form_Tau form = new Form_Tau((Frame) SwingUtilities.getWindowAncestor(this), "Cập Nhật Thông Tin Tàu");
            form.setEntity(tauData);
            form.setVisible(true);
            
            if (form.isConfirmed()) {
                Tau updatedTau = form.getEntity();
                if (tauDAO.updateTau(updatedTau)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thông tin tàu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    clearUndoCache(); // Xóa bộ nhớ hoàn tác để tránh xung đột
                    loadDataFromDatabase();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại, vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void clearUndoCache() {
        deletedTau = null;
        deletedToaList = null;
        deletedChoNgoiMap.clear();
        btnUndo.setVisible(false);
    }

    private void performSearch() { renderTable(tauDAO.searchTau(txtSearch.getText().trim())); }
    private void loadDataFromDatabase() { renderTable(tauDAO.getAllTau()); }

    private void renderTable(List<Tau> list) {
        model.setRowCount(0);
        int stt = 1, active = 0, maintenance = 0, stopped = 0;
        for (Tau t : list) {
            model.addRow(new Object[]{ stt++, t.getMaTau(), t.getTenTau(), t.getSoToa(), t.getTrangThaiTau().getMoTa() });
            if (t.getTrangThaiTau() == TrangThaiTau.HOATDONG) active++;
            else if (t.getTrangThaiTau() == TrangThaiTau.BAOTRI) maintenance++;
            else stopped++;
        }
        lblTotal.setText(String.valueOf(list.size()));
        lblActive.setText(String.valueOf(active));
        lblMaintenance.setText(String.valueOf(maintenance));
        lblStopped.setText(String.valueOf(stopped));
    }

    // ================= CĂN CHỈNH BẢNG (ALIGNMENT PERFECT) =================
    private JTable buildTable() {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        t.setRowHeight(38); t.setFont(F_CELL);
        t.setBackground(BG_CARD); t.setSelectionBackground(ROW_SEL); t.setSelectionForeground(TEXT_DARK);
        t.setGridColor(BORDER); t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setFocusable(false); t.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                l.setOpaque(true); l.setBackground(ACCENT); l.setForeground(Color.WHITE); l.setFont(F_LABEL);
                if (col == 0 || col == 3 || col == 4) {
                    l.setHorizontalAlignment(CENTER);
                    l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                } else {
                    l.setHorizontalAlignment(LEFT);
                    l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
                }
                return l;
            }
        });
        h.setPreferredSize(new Dimension(0, 40)); h.setReorderingAllowed(false);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        DefaultTableCellRenderer leftPaddingRenderer = new DefaultTableCellRenderer();
        leftPaddingRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftPaddingRenderer.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));

        t.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); 
        t.getColumnModel().getColumn(1).setCellRenderer(leftPaddingRenderer); 
        t.getColumnModel().getColumn(2).setCellRenderer(leftPaddingRenderer); 
        t.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); 
        
        t.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setHorizontalAlignment(CENTER); 
                String val = v.toString().toLowerCase();
                if(val.contains("ngưng")) l.setForeground(new Color(192, 57, 43)); 
                else if(val.contains("bảo trì")) l.setForeground(new Color(243, 156, 18)); 
                else l.setForeground(new Color(39, 174, 96)); 
                return l;
            }
        });
        
        return t;
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
        JPanel p = makeCard(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(15, 20, 15, 20)));
        JLabel lblT = new JLabel(title);
        lblT.setForeground(TEXT_MID);
        lblT.setFont(F_LABEL);
        lblValue.setForeground(accent);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        p.add(lblT, BorderLayout.NORTH); p.add(lblValue, BorderLayout.CENTER);
        return p;
    }

    private JPanel makeCard(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(BG_CARD); return p;
    }

    private JTextField makeField(String hint) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(TEXT_LIGHT);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    g2.drawString(hint, getInsets().left + 4, getHeight() / 2 + 5); g2.dispose();
                }
            }
        };
        tf.setFont(F_CELL); tf.setForeground(TEXT_DARK); tf.setBackground(new Color(0xF8FAFD));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ACCENT_FOC, 2, true), BorderFactory.createEmptyBorder(5, 9, 5, 9))); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(6, 10, 6, 10))); }
        });
        return tf;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                switch (style) {
                    case PRIMARY -> { g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); }
                    case SUCCESS -> { g2.setColor(getModel().isRollover() ? new Color(46, 204, 113) : new Color(39, 174, 96)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); }
                    case DANGER  -> { g2.setColor(getModel().isRollover() ? new Color(231, 76, 60) : new Color(192, 57, 43)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); }
                    case WARNING -> { g2.setColor(getModel().isRollover() ? new Color(241, 196, 15) : new Color(243, 156, 18)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); }
                    default      -> { g2.setColor(getModel().isRollover() ? new Color(0xE0ECFF) : BTN2_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); g2.setColor(BORDER); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8); }
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_LABEL); b.setForeground(style == BtnStyle.SECONDARY ? BTN2_FG : Color.WHITE);
        b.setPreferredSize(new Dimension(110, 36));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() { thumbColor = new Color(0xC0D4EE); trackColor = BG_PAGE; }
            @Override protected JButton createDecreaseButton(int o) { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
            @Override protected JButton createIncreaseButton(int o) { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        });
    }

    private static class ShadowBorder extends AbstractBorder {
        private static final int S = 4;
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = S; i > 0; i--) {
                g2.setColor(new Color(100, 140, 200, (int) (20.0 * (S - i) / S)));
                g2.drawRoundRect(x + i, y + i, w - 2 * i - 1, h - 2 * i - 1, 12, 12);
            }
            g2.setColor(BORDER); g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
            g2.setColor(BG_CARD); g2.setClip(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, 12, 12)); g2.fillRect(x + 1, y + 1, w - 2, h - 2);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(S, S, S, S); }
    }
}