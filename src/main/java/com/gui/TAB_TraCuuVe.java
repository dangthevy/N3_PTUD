package com.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TAB_TraCuuVe extends JPanel {
	// Constructor
	public TAB_TraCuuVe() {
		// Setup bố cục và màu nền
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);

		// === THÊM THỬ NỘI DUNG ĐỂ KIỂM TRA ===

		// 1. Một dòng chữ tiêu đề ở trên cùng
		JLabel lblTitle = new JLabel("TRA CỨU VÉ", SwingConstants.CENTER);
		lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
		lblTitle.setForeground(new Color(0, 122, 255));
		lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // Tạo khoảng trống trên dưới

		add(lblTitle, BorderLayout.NORTH);

	}
}
