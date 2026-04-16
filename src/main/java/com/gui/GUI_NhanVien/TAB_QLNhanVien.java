package com.gui.GUI_NhanVien;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class TAB_QLNhanVien extends JPanel{
    // Constructor
    public TAB_QLNhanVien() {
        // Setup bố cục và màu nền
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // === THÊM THỬ NỘI DUNG ĐỂ KIỂM TRA ===

        // 1. Một dòng chữ tiêu đề ở trên cùng
        JLabel lblTitle = new JLabel("Quản lý Nhân viên", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 122, 255));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // Tạo khoảng trống trên dưới

        add(lblTitle, BorderLayout.NORTH);

        setLayout(new BorderLayout(10, 10));

        // ===== TOP PANEL (Dashboard) =====
        add(createTopPanel(), BorderLayout.NORTH);

        // ===== CENTER PANEL =====
        add(createCenterPanel(), BorderLayout.CENTER);


    }

    private JPanel createTopPanel() {
        JPanel pnlLabel = new JPanel();
        pnlLabel.setLayout(new BorderLayout());
        JLabel lblTitle = new JLabel("Quản lý Nhân viên", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 122, 255));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // Tạo khoảng trống trên dưới

        pnlLabel.add(lblTitle, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 3, 15, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createCard("6", "Tổng Nhân Viên"));
        panel.add(createCard("5", "Đang Làm Việc"));
        panel.add(createCard("4", "NV Bán Vé"));

        pnlLabel.add(panel, BorderLayout.CENTER);

        return pnlLabel;
    }

    private JPanel createCard(String number, String label) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel numLabel = new JLabel(number);
        numLabel.setFont(new Font("Arial", Font.BOLD, 24));
        numLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel(label);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(numLabel);
        card.add(textLabel);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // ===== FILTER PANEL =====
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JTextField searchField = new JTextField(20);
        searchField.setText("Tìm theo tên...");

        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Vai Trò", "Admin", "Quản Lý", "NV Bán Vé"});
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Trạng Thái", "Đang Làm Việc", "Nghỉ"});

        JButton filterBtn = new JButton("Lọc");
        JButton addBtn = new JButton("+ Thêm Mới");

        filterPanel.add(searchField);
        filterPanel.add(roleBox);
        filterPanel.add(statusBox);
        filterPanel.add(filterBtn);
        filterPanel.add(addBtn);

        // ===== TABLE =====
        String[] columns = {"Họ Tên", "Mã NV", "Chức Vụ", "Điện Thoại", "Email", "Ngày Vào Làm", "Trạng Thái", "Chỉnh sửa"};

        Object[][] data = {
                {"Nguyễn Văn Admin", "NV001", "Admin", "0901234567", "admin@gmail.com", "23/03/2023", "Đang Làm Việc", "Edit"},
                {"Trần Thị Quản Lý", "NV002", "Quản Lý", "0912345678", "ql@gmail.com", "23/03/2024", "Đang Làm Việc", "Edit"},
                {"Lê Văn Bán Vé", "NV003", "NV Bán Vé", "0930000001", "nv1@gmail.com", "23/09/2025", "Đang Làm Việc", "Edit"},
        };

        DefaultTableModel model = new DefaultTableModel(data, columns);
        JTable table = new JTable(model);

        table.getColumn("Chỉnh sửa").setCellRenderer(new ButtonRenderer());
        table.getColumn("Chỉnh sửa").setCellEditor(new ButtonEditor(new JCheckBox(), table));;

        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

}
