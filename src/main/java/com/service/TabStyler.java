package com.service;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;

public final class TabStyler {
    private static final String HEADER_PROPERTY = "tabHeader";

    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font CONTENT_FONT = new Font("Segoe UI", Font.PLAIN, 16);

    private static final Color HEADER_COLOR = new Color(0, 90, 200);

    private TabStyler() {
    }
    public static JPanel createHeader(String title) {
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(HEADER_FONT);
        lblTitle.setForeground(HEADER_COLOR);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        header.add(lblTitle);
        header.putClientProperty(HEADER_PROPERTY, Boolean.TRUE);
        return header;
    }

    /**
     * Ensures every component inside the tab uses the shared content font unless it has
     * been explicitly styled.
     */
    public static void applyContentFont(Component component) {
        if (component == null) {
            return;
        }
        if (component instanceof JComponent) {
            JComponent jComponent = (JComponent) component;
            Object flag = jComponent.getClientProperty(HEADER_PROPERTY);
            if (Boolean.TRUE.equals(flag)) {
                return;
            }
        }
        Font font = component.getFont();
        if (font == null || font instanceof FontUIResource) {
            component.setFont(CONTENT_FONT);
        }
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                applyContentFont(child);
            }
        }
    }

    public static void markCustomFont(JComponent component) {
        if (component != null) {
            component.putClientProperty(HEADER_PROPERTY, Boolean.TRUE);
        }
    }

    public static TitledBorder createSectionBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(SECTION_FONT);
        border.setTitleColor(HEADER_COLOR);
        return border;
    }

    public static void applySectionTitleFont(TitledBorder border) {
        if (border != null) {
            border.setTitleFont(SECTION_FONT);
            border.setTitleColor(HEADER_COLOR);
        }
    }
}
