package com.gui;

import com.dao.DAO_Tau;
import com.entities.Tau;
import com.enums.TrangThaiTau;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class TAB_Tau extends JPanel {
	private JTable table;
	private DefaultTableModel model;
	private JTextField txtSearch;
	private JButton btnAdd, btnRefresh, btnSearch;

	// Dashboard Labels
	private JLabel lblTotal, lblActive, lblMaintenance, lblStopped;

	private DAO_Tau tauDAO = new DAO_Tau();

	// Bảng màu Ocean Blue
	private final Color COLOR_BG = new Color(248, 250, 252);
	private final Color COLOR_PRIMARY = new Color(37, 99, 235);
	private final Color COLOR_BORDER = new Color(226, 232, 240);
	private final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

	public TAB_Tau() {
		setLayout(new BorderLayout(0, 20));
		setBackground(COLOR_BG);
		setBorder(new EmptyBorder(25, 25, 25, 25));

		initUI();
		initEvents();
		loadDataFromDatabase();
	}

	private void initUI() {
		// --- 1. TOP: TITLE & DASHBOARD ---
		JPanel pnlTop = new JPanel(new BorderLayout(0, 20));
		pnlTop.setOpaque(false);

		JLabel lblTitle = new JLabel("QUẢN LÝ ĐOÀN TÀU");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
		lblTitle.setForeground(new Color(30, 41, 59));
		pnlTop.add(lblTitle, BorderLayout.NORTH);

		JPanel pnlDashboard = new JPanel(new GridLayout(1, 4, 20, 0));
		pnlDashboard.setOpaque(false);
		pnlDashboard.add(createStatCard("TỔNG SỐ TÀU", lblTotal = new JLabel("0"), COLOR_PRIMARY));
		pnlDashboard.add(createStatCard("ĐANG HOẠT ĐỘNG", lblActive = new JLabel("0"), new Color(34, 197, 94)));
		pnlDashboard.add(createStatCard("ĐANG BẢO TRÌ", lblMaintenance = new JLabel("0"), new Color(245, 158, 11)));
		pnlDashboard.add(createStatCard("NGƯNG HOẠT ĐỘNG", lblStopped = new JLabel("0"), new Color(239, 68, 68)));
		pnlTop.add(pnlDashboard, BorderLayout.CENTER);

		// --- 2. CENTER: TOOLBAR & TABLE ---
		JPanel pnlCenter = new JPanel(new BorderLayout(0, 15));
		pnlCenter.setOpaque(false);

		JPanel pnlToolbar = new JPanel(new BorderLayout());
		pnlToolbar.setOpaque(false);

		// Thanh tìm kiếm bên trái
		JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pnlSearch.setOpaque(false);
		txtSearch = new JTextField();
		txtSearch.setPreferredSize(new Dimension(300, 38));
		btnSearch = createStyledButton("Tìm kiếm", COLOR_PRIMARY, true);
		pnlSearch.add(txtSearch);
		pnlSearch.add(Box.createHorizontalStrut(10));
		pnlSearch.add(btnSearch);

		// Nhóm nút bên phải (Chỉ còn Thêm và Làm mới)
		JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlButtons.setOpaque(false);
		btnAdd = createStyledButton("+ Thêm Mới", new Color(34, 197, 94), false);
		btnRefresh = createStyledButton("Làm Mới", new Color(100, 116, 139), false);

		pnlButtons.add(btnAdd);
		pnlButtons.add(btnRefresh);

		pnlToolbar.add(pnlSearch, BorderLayout.WEST);
		pnlToolbar.add(pnlButtons, BorderLayout.EAST);

		// Cấu trúc bảng
		String[] columns = {"STT", "Mã Tàu", "Tên Tàu", "Số Toa", "Trạng thái"};
		model = new DefaultTableModel(columns, 0) {
			@Override public boolean isCellEditable(int r, int c) { return false; }
		};
		table = new JTable(model);
		setupTableAppearance();

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(new LineBorder(COLOR_BORDER));
		scrollPane.getViewport().setBackground(Color.WHITE);

		pnlCenter.add(pnlToolbar, BorderLayout.NORTH);
		pnlCenter.add(scrollPane, BorderLayout.CENTER);

		add(pnlTop, BorderLayout.NORTH);
		add(pnlCenter, BorderLayout.CENTER);
	}

	private void initEvents() {
		// Tìm kiếm tự động khi gõ
		txtSearch.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) { performSearch(); }
		});

		// Double-click để Cập nhật (Cách duy nhất để sửa/vô hiệu hóa tàu)
		table.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
					openUpdateForm();
				}
			}
		});

		// Sự kiện Thêm mới
		btnAdd.addActionListener(e -> {
			Form_Tau form = new Form_Tau((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Tàu Mới");
			form.setVisible(true);
			if (form.isConfirmed()) {
				Tau newTau = form.getEntity();
				if (tauDAO.getTauByMa(newTau.getMaTau()) != null) {
					JOptionPane.showMessageDialog(this, "Mã tàu đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (tauDAO.insertTau(newTau)) {
					loadDataFromDatabase();
					JOptionPane.showMessageDialog(this, "Thêm tàu thành công!");
				}
			}
		});

		btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadDataFromDatabase(); });
		btnSearch.addActionListener(e -> performSearch());
	}

	private void openUpdateForm() {
		int row = table.getSelectedRow();
		String ma = (String) model.getValueAt(row, 1);
		Tau tauData = tauDAO.getTauByMa(ma);

		if (tauData != null) {
			Form_Tau form = new Form_Tau((Frame) SwingUtilities.getWindowAncestor(this), "Cập Nhật Tàu");
			form.setEntity(tauData);
			form.setVisible(true);

			// Sau khi đóng Form, cập nhật lại bảng dù có bấm xác nhận hay không để đồng bộ trạng thái
			loadDataFromDatabase();
		}
	}

	private void performSearch() {
		renderTable(tauDAO.searchTau(txtSearch.getText().trim()));
	}

	private void loadDataFromDatabase() {
		renderTable(tauDAO.getAllTau());
	}

	private void renderTable(List<Tau> list) {
		model.setRowCount(0);
		int stt = 1, active = 0, maintenance = 0, stopped = 0;

		for (Tau t : list) {
			model.addRow(new Object[]{
					stt++, t.getMaTau(), t.getTenTau(), t.getSoToa(), t.getTrangThaiTau().getMoTa()
			});
			if (t.getTrangThaiTau() == TrangThaiTau.HOATDONG) active++;
			else if (t.getTrangThaiTau() == TrangThaiTau.BAOTRI) maintenance++;
			else stopped++;
		}

		lblTotal.setText(String.valueOf(list.size()));
		lblActive.setText(String.valueOf(active));
		lblMaintenance.setText(String.valueOf(maintenance));
		lblStopped.setText(String.valueOf(stopped));
	}

	private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
		JPanel p = new JPanel(new BorderLayout(5, 5));
		p.setBackground(new Color(255, 255, 255));
		p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(15, 20, 15, 20)));
		JLabel lblT = new JLabel(title);
		lblT.setForeground(COLOR_TEXT_MUTED);
		lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblValue.setForeground(accent);
		lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
		p.add(lblT, BorderLayout.NORTH); p.add(lblValue, BorderLayout.CENTER);
		return p;
	}

	private void setupTableAppearance() {
		JTableHeader header = table.getTableHeader();
		header.setFont(new Font("Segoe UI", Font.BOLD, 13));
		header.setBackground(Color.WHITE);
		header.setPreferredSize(new Dimension(0, 45));
		table.setRowHeight(45);
		table.setSelectionBackground(new Color(239, 246, 255));
		table.setSelectionForeground(COLOR_PRIMARY);
		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(center);
		table.getColumnModel().getColumn(3).setCellRenderer(center);
		table.getColumnModel().getColumn(4).setCellRenderer(center);
	}

	private JButton createStyledButton(String text, Color color, boolean isSmall) {
		JButton btn = new JButton(text);
		btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
		btn.setBackground(color);
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setPreferredSize(new Dimension(isSmall ? 100 : 120, 38));
		btn.setBorder(BorderFactory.createEmptyBorder());
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return btn;
	}
}