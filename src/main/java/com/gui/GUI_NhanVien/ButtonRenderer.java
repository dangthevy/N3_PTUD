package com.gui.GUI_NhanVien;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

class ButtonRenderer extends JButton implements TableCellRenderer {

    public ButtonRenderer() {
        setText("Chỉnh sửa");
        setFocusPainted(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        return this;
    }
}
