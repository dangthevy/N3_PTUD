package com;

import com.gui.GUI_Login; // Bắt buộc phải Import cái này để lấy code từ thư mục gui

import javax.swing.*;

public class main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Hệ thống quản lý bán vé ga tàu - Đăng nhập");

			GUI_Login loginPanel = new GUI_Login();

			loginPanel.setParentFrame(frame);
			frame.setContentPane(loginPanel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			// Xóa dòng frame.setSize(1080, 600) đi vì hàm pack() ở dưới sẽ tự động ôm khít
			// giao diện
			frame.pack();
			frame.setLocationRelativeTo(null); // Giữa màn hình
			frame.setResizable(false);
			frame.setVisible(true); // Hiển thị lên!
		});
	}
}