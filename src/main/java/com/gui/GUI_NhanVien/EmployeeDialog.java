package com.gui.GUI_NhanVien;

import javax.swing.*;
import java.awt.*;

public class EmployeeDialog extends JDialog {

    public EmployeeDialog(JFrame parent, JTable table, int row,
                          String name, String id, String role,
                          String phone, String email,
                          String date, String status) {

        super(parent, "Chi Tiết Nhân Viên", true);
        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(8, 2, 10, 10));

        JTextField txtName = new JTextField(name);
        JTextField txtId = new JTextField(id);
        JTextField txtRole = new JTextField(role);
        JTextField txtPhone = new JTextField(phone);
        JTextField txtEmail = new JTextField(email);
        JTextField txtDate = new JTextField(date);
        JTextField txtStatus = new JTextField(status);

        add(new JLabel("Họ tên:"));
        add(txtName);

        add(new JLabel("Mã NV:"));
        add(txtId);

        add(new JLabel("Chức vụ:"));
        add(txtRole);

        add(new JLabel("Điện thoại:"));
        add(txtPhone);

        add(new JLabel("Email:"));
        add(txtEmail);

        add(new JLabel("Ngày bắt đầu:"));
        add(txtDate);

        add(new JLabel("Trạng thái:"));
        add(txtStatus);

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        add(btnSave);
        add(btnCancel);

        // ===== SAVE =====
        btnSave.addActionListener(e -> {
            table.setValueAt(txtName.getText(), row, 0);
            table.setValueAt(txtId.getText(), row, 1);
            table.setValueAt(txtRole.getText(), row, 2);
            table.setValueAt(txtPhone.getText(), row, 3);
            table.setValueAt(txtEmail.getText(), row, 4);
            table.setValueAt(txtDate.getText(), row, 5);
            table.setValueAt(txtStatus.getText(), row, 6);

            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            dispose();
        });

        // ===== CANCEL =====
        btnCancel.addActionListener(e -> dispose());
    }
}