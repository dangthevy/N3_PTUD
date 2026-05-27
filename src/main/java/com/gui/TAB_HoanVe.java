package com.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TAB_HoanVe extends JPanel {
	// Constructor
	public TAB_HoanVe() {
		// Setup bố cục và màu nền
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);

		// === THÊM THỬ NỘI DUNG ĐỂ KIỂM TRA ===

		// 1. Một dòng chữ tiêu đề ở trên cùng
		JLabel lblTitle = new JLabel("HOÀN VÉ", SwingConstants.CENTER);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitle.setForeground(new Color(0x1A5EAB));
		lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // Tạo khoảng trống trên dưới

		add(lblTitle, BorderLayout.NORTH);

	}
}
