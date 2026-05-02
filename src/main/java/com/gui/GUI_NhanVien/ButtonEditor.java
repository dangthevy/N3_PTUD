package com.gui.GUI_NhanVien;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;

class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private JTable table;
    private int selectedRow;

    public ButtonEditor(JCheckBox checkBox, JTable table) {
        super(checkBox);
        this.table = table;

        button = new JButton("Chỉnh sửa");
        button.setFocusPainted(false);

        button.addActionListener(e -> openDialog());
    }

    private void openDialog() {
        selectedRow = table.getSelectedRow();

        String name = table.getValueAt(selectedRow, 0).toString();
        String id = table.getValueAt(selectedRow, 1).toString();
        String role = table.getValueAt(selectedRow, 2).toString();
        String phone = table.getValueAt(selectedRow, 3).toString();
        String email = table.getValueAt(selectedRow, 4).toString();
        String date = table.getValueAt(selectedRow, 5).toString();
        String status = table.getValueAt(selectedRow, 6).toString();

        new EmployeeDialog(
                (JFrame) SwingUtilities.getWindowAncestor(table),
                table, selectedRow,
                name, id, role, phone, email, date, status
        ).setVisible(true);

        fireEditingStopped();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        selectedRow = row;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return "Chỉnh sửa";
    }
}
