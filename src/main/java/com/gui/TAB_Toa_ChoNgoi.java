package com.gui;

import com.dao.*;
import com.entities.*;
import com.enums.TrangThaiCho;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.border.MatteBorder;
import java.awt.geom.RoundRectangle2D;

public class TAB_Toa_ChoNgoi extends JPanel {
	private CardLayout cardLayout;
	private JPanel pnlMainContainer;

	// Màn hình 1: Dashboard & Table
	private JTable tblToa;
	private DefaultTableModel modelToa;
	private JComboBox<Tau> cbTau;
	private JLabel lblSoToa, lblTongCho, lblDaDat, lblLapDay;

	// Màn hình 2: Sơ đồ ghế
	private JPanel pnlSeatView, pnlSeatsGrid;
	private JLabel lblDetailTitle;

	private DAO_Tau tauDAO = new DAO_Tau();
	private DAO_Toa toaDAO = new DAO_Toa();
	private DAO_ChoNgoi choNgoiDAO = new DAO_ChoNgoi();
	// Thêm biến cho Panel chi tiết mới
	private CustomDetailPanel pnlSeatDetail;
	private ChoNgoi selectedChoNgoi;

	// Màu sắc hệ thống
	private final Color COLOR_BG = new Color(240, 244, 248);
	private final Color COLOR_PRIMARY = new Color(2, 132, 199);
	private final Color COLOR_EDIT = new Color(205,133,63);
	//    private final Color COLOR_DELETE = new Color(139, 0, 0);
	private final Color COLOR_OCCUPIED = new Color(234, 88, 12); // Cam đỏ - Đã đặt
	private final Color COLOR_MAINTENANCE = new Color(250, 204, 21); // Vàng - Bảo trì
	private String currentLoaiToa = "";

	public TAB_Toa_ChoNgoi() {
		setLayout(new BorderLayout());
		cardLayout = new CardLayout();
		pnlMainContainer = new JPanel(cardLayout);

		initListToaScreen();
		initSeatViewScreen();

		add(pnlMainContainer);
		loadDataTau();


	}

	private void initListToaScreen() {
		JPanel pnlList = new JPanel(new BorderLayout(0, 20));
		pnlList.setBackground(COLOR_BG);
		pnlList.setBorder(new EmptyBorder(20, 20, 20, 20));

		// --- TIÊU ĐỀ TRANG ---
		JLabel lblMainTitle = new JLabel("QUẢN LÝ TOA & CHỖ NGỒI");
		lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
		lblMainTitle.setForeground(new Color(15, 23, 42)); // Màu xanh đen đậm

		// --- TOP: FILTER & TITLE ---
		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.setOpaque(false);

		// Gom tiêu đề vào một Panel bên trái
		JPanel pnlTitleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
		pnlTitleGroup.setOpaque(false);
		pnlTitleGroup.add(lblMainTitle);

		cbTau = new JComboBox<>();
		cbTau.setPreferredSize(new Dimension(280, 35));
		setupTauRenderer();
		cbTau.addActionListener(e -> updateDashboardAndTable());

		JButton btnAdd = createStyledButton("+ Thêm Toa Mới", new Color(34, 197, 94));
		btnAdd.addActionListener(e -> handleAddToa());

		// Layout lại phần Top: Tiêu đề ở trên, Filter và nút ở dưới
		JPanel pnlHeaderActions = new JPanel(new BorderLayout());
		pnlHeaderActions.setOpaque(false);
		pnlHeaderActions.add(cbTau, BorderLayout.WEST);
		pnlHeaderActions.add(btnAdd, BorderLayout.EAST);

		JPanel pnlFullTop = new JPanel(new GridLayout(2, 1, 0, 15));
		pnlFullTop.setOpaque(false);
		pnlFullTop.add(pnlTitleGroup);
		pnlFullTop.add(pnlHeaderActions);

		// --- DASHBOARD ---
		JPanel pnlStats = new JPanel(new GridLayout(1, 4, 15, 0));
		pnlStats.setOpaque(false);
		pnlStats.add(createStatCard("SỐ TOA", lblSoToa = new JLabel("0"), COLOR_PRIMARY));
		pnlStats.add(createStatCard("TỔNG CHỖ", lblTongCho = new JLabel("0"), new Color(56, 189, 248)));
		pnlStats.add(createStatCard("ĐÃ ĐẶT", lblDaDat = new JLabel("0"), COLOR_OCCUPIED));
		pnlStats.add(createStatCard("LẤP ĐẦY", lblLapDay = new JLabel("0%"), new Color(34, 197, 94)));

		// --- TABLE ---
		String[] columns = {"STT", "Mã Toa", "Tên Toa", "Loại Toa", "Số Ghế", "Thao Tác"};
		modelToa = new DefaultTableModel(columns, 0) {
			@Override public boolean isCellEditable(int r, int c) { return c == 5; }
		};
		tblToa = new JTable(modelToa);
		setupTableStyle();

		JScrollPane scroll = new JScrollPane(tblToa);
		scroll.setBorder(new LineBorder(new Color(226, 232, 240)));

		JPanel pnlContent = new JPanel(new BorderLayout(0, 15));
		pnlContent.setOpaque(false);
		pnlContent.add(pnlStats, BorderLayout.NORTH);
		pnlContent.add(scroll, BorderLayout.CENTER);

		pnlList.add(pnlFullTop, BorderLayout.NORTH); // Dùng pnlFullTop thay vì pnlTop cũ
		pnlList.add(pnlContent, BorderLayout.CENTER);
		pnlMainContainer.add(pnlList, "LIST");
	}

	private void initSeatViewScreen() {
		pnlSeatView = new JPanel(new BorderLayout(0, 20));
		pnlSeatView.setBackground(COLOR_BG);
		pnlSeatView.setBorder(new EmptyBorder(20, 20, 20, 20));

		JButton btnBack = createStyledButton("< Quay lại danh sách", new Color(100, 116, 139));
		btnBack.addActionListener(e -> cardLayout.show(pnlMainContainer, "LIST"));

		lblDetailTitle = new JLabel("SƠ ĐỒ THIẾT KẾ TOA");
		lblDetailTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

		JPanel pHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		pHeader.setOpaque(false);
		pHeader.add(btnBack); pHeader.add(lblDetailTitle);

		pnlSeatsGrid = new JPanel(new BorderLayout());
		pnlSeatsGrid.setBackground(Color.WHITE);

		// --- PANEL CHI TIẾT GHẾ (CARD DARK MODE) ---
		pnlSeatDetail = new CustomDetailPanel();
		pnlSeatDetail.setVisible(false); // Chỉ hiện khi chọn ghế

		// Panel chứa Card chi tiết, căn giữa để đẹp hơn
		JPanel pnlDetailWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlDetailWrapper.setOpaque(false);
		pnlDetailWrapper.add(pnlSeatDetail);

		pnlSeatView.add(pHeader, BorderLayout.NORTH);
		pnlSeatView.add(new JScrollPane(pnlSeatsGrid), BorderLayout.CENTER);
		pnlSeatView.add(pnlDetailWrapper, BorderLayout.SOUTH);
		pnlMainContainer.add(pnlSeatView, "SEATS");
	}

	// --- LOGIC HIỂN THỊ SƠ ĐỒ GHẾ ---
	private void showSeatLayout(int row) {
		String maToa = modelToa.getValueAt(row, 1).toString();
		String tenToa = modelToa.getValueAt(row, 2).toString();
		String loaiToa = modelToa.getValueAt(row, 3).toString();
		this.currentLoaiToa = loaiToa; // Lưu lại loại toa
		int soGhe = (int) modelToa.getValueAt(row, 4);

		lblDetailTitle.setText("Toa " + tenToa + " (" + loaiToa + ")");
		pnlSeatsGrid.removeAll();
		pnlSeatDetail.setVisible(false);

		List<ChoNgoi> ds = choNgoiDAO.getChoNgoiByToa(maToa);

		// Khung toa tàu
		JPanel pnlMapContainer = new JPanel(new BorderLayout());
		pnlMapContainer.setBackground(Color.WHITE);
		pnlMapContainer.setBorder(BorderFactory.createCompoundBorder(
				new EmptyBorder(20, 50, 20, 50),
				new LineBorder(COLOR_PRIMARY, 3, true)
		));

		if (loaiToa.toLowerCase().contains("giường nằm")) {
			pnlMapContainer.add(renderSleeperMap(ds), BorderLayout.CENTER);
		} else {
			pnlMapContainer.add(renderSeaterMap(ds, soGhe), BorderLayout.CENTER);
		}

		// Wrapper để bọc sơ đồ + chú thích
		JPanel pnlWrapper = new JPanel(new BorderLayout());
		pnlWrapper.setOpaque(false);
		pnlWrapper.add(pnlMapContainer, BorderLayout.CENTER);
		pnlWrapper.add(createLegendPanel(), BorderLayout.SOUTH);

		pnlSeatsGrid.add(pnlWrapper, BorderLayout.NORTH);
		pnlSeatsGrid.revalidate(); pnlSeatsGrid.repaint();
		cardLayout.show(pnlMainContainer, "SEATS");
	}

	private JPanel renderSeaterMap(List<ChoNgoi> list, int total) {
		// 5 hàng (4 hàng ghế + 1 hàng lối đi ở giữa)
		JPanel pnl = new JPanel(new GridLayout(5, 1, 0, 2));
		pnl.setOpaque(false);
		pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

		int perRow = total / 4; // Số ghế mỗi hàng (ví dụ 64/4 = 16)

		for (int i = 0; i < 5; i++) {
			if (i == 2) { // Lối đi ngang ở giữa
				JPanel pnlAisle = new JPanel();
				pnlAisle.setOpaque(false);
				pnlAisle.setPreferredSize(new Dimension(10, 12));
				pnl.add(pnlAisle);
				continue;
			}

			// Tạo một hàng ghế
			// Sử dụng GridLayout(1, perRow, hgap, vgap)
			// hgap = 8 là khoảng cách giữa các ghế theo chiều dọc tàu
			JPanel rowPanel = new JPanel(new GridLayout(1, perRow, 8, 0));
			rowPanel.setOpaque(false);

			// Giảm lề hai bên của hàng để chứa được nhiều ghế hơn
			rowPanel.setBorder(new EmptyBorder(0, 5, 0, 5));

			int rowIndex = (i > 2) ? i - 1 : i;

			for (int j = 0; j < perRow; j++) {
				int index = rowIndex + (j * 4);
				if (index < list.size()) {
					rowPanel.add(createSeatButton(list.get(index)));
				} else {
					// Thêm ô trống nếu không có ghế để giữ đúng cấu trúc lưới
					rowPanel.add(Box.createGlue());
				}
			}
			pnl.add(rowPanel);
		}
		return pnl;
	}

	private JPanel renderSleeperMap(List<ChoNgoi> list) {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		pnl.setOpaque(false);
		pnl.setBorder(new EmptyBorder(15, 10, 15, 10));
		int soKhoang = (int) Math.ceil(list.size() / 4.0);

		for (int k = 0; k < soKhoang; k++) {
			JPanel pnlKhoang = new JPanel(new BorderLayout(0, 5));
			pnlKhoang.setOpaque(false);
			JLabel lblKhoang = new JLabel("Khoang " + (k + 1), SwingConstants.CENTER);
			JPanel pnlGiuong = new JPanel(new GridLayout(2, 2, 5, 5));
			pnlGiuong.setOpaque(false);

			for (int i = 0; i < 4; i++) {
				int index = (k * 4) + i;
				if (index < list.size()) pnlGiuong.add(createSeatButton(list.get(index)));
			}
			pnlKhoang.add(lblKhoang, BorderLayout.NORTH);
			pnlKhoang.add(pnlGiuong, BorderLayout.CENTER);
			pnl.add(pnlKhoang);
			if (k < soKhoang - 1) pnl.add(new JSeparator(JSeparator.VERTICAL));
		}
		return pnl;
	}

	private JButton createSeatButton(ChoNgoi cn) {
		JButton btn = new JButton(cn.getTenCho());
		btn.setPreferredSize(new Dimension(42, 37));
		btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
		btn.setFocusPainted(false);

		// Đặt màu sắc dựa trên trạng thái
		if (cn.getTrangThai() == TrangThaiCho.DADAT) {
			btn.setBackground(COLOR_OCCUPIED); btn.setForeground(Color.WHITE);
		} else if (cn.getTrangThai() == TrangThaiCho.BAOTRI) {
			btn.setBackground(COLOR_MAINTENANCE); btn.setForeground(new Color(133, 77, 14));
		} else {
			btn.setBackground(Color.WHITE); btn.setForeground(new Color(30, 41, 59));
		}
		btn.setBorder(new LineBorder(new Color(226, 232, 240)));

		// --- LỖI Ở ĐÂY: THÊM SỰ KIỆN CLICK ---
		btn.addActionListener(e -> {
			this.selectedChoNgoi = cn;
			pnlSeatDetail.updateInfo(cn, currentLoaiToa); // Cập nhật thông tin vào Card
			pnlSeatDetail.setVisible(true); // Hiển thị Card
			pnlSeatDetail.revalidate();
			pnlSeatDetail.repaint();
		});

		return btn;
	}

	private JPanel createLegendPanel() {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
		pnl.setOpaque(false);
		pnl.add(createLegendItem("Trống", Color.WHITE));
		pnl.add(createLegendItem("Đã đặt", COLOR_OCCUPIED));
		pnl.add(createLegendItem("Bảo trì", COLOR_MAINTENANCE));
		pnl.add(createLegendItem("Đang chọn", new Color(34, 197, 94)));
		return pnl;
	}

	private JPanel createLegendItem(String text, Color color) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		p.setOpaque(false);
		JPanel box = new JPanel();
		box.setPreferredSize(new Dimension(20, 20));
		box.setBackground(color);
		box.setBorder(new LineBorder(Color.GRAY));
		p.add(box); p.add(new JLabel(text));
		return p;
	}

	// --- LOGIC XỬ LÝ DỮ LIỆU ---
	public void loadDataTau() {
		Tau currentSelected = (Tau) cbTau.getSelectedItem();
		cbTau.removeAllItems();
		List<Tau> ds = tauDAO.getAllTau();
		ds.forEach(cbTau::addItem);

		if (currentSelected != null) {
			for (int i = 0; i < cbTau.getItemCount(); i++) {
				if (cbTau.getItemAt(i).getMaTau().equals(currentSelected.getMaTau())) {
					cbTau.setSelectedIndex(i); break;
				}
			}
		}
		updateDashboardAndTable();
	}

	private void updateDashboardAndTable() {
		modelToa.setRowCount(0);
		Tau selected = (Tau) cbTau.getSelectedItem();
		if (selected == null) return;

		List<Toa> dsToa = toaDAO.getToaByMaTau(selected.getMaTau());
		int totalSeatsAll = 0, totalBookedAll = 0;

		for (int i = 0; i < dsToa.size(); i++) {
			Toa t = dsToa.get(i);
			int booked = choNgoiDAO.countGheByTrangThai(t.getMaToa(), TrangThaiCho.DADAT);
			modelToa.addRow(new Object[]{ i + 1, t.getMaToa(), t.getTenToa(), t.getLoaiToa().getTenLoaiToa(), t.getSoGhe(), "" });
			totalSeatsAll += t.getSoGhe();
			totalBookedAll += booked;
		}

		lblSoToa.setText(String.valueOf(dsToa.size()));
		lblTongCho.setText(String.valueOf(totalSeatsAll));
		lblDaDat.setText(String.valueOf(totalBookedAll));
		lblLapDay.setText(totalSeatsAll > 0 ? (totalBookedAll * 100 / totalSeatsAll) + "%" : "0%");
	}

	// --- CÁC HÀM PHỤ TRỢ UI ---
	private void setupTableStyle() {
		tblToa.setRowHeight(60);
		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(JLabel.CENTER);
		for(int i=0; i<5; i++) tblToa.getColumnModel().getColumn(i).setCellRenderer(center);
		tblToa.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
		tblToa.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
		tblToa.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && tblToa.getSelectedColumn() < 5) showSeatLayout(tblToa.getSelectedRow());
			}
		});
	}

	private void handleAddToa() {
		Tau selected = (Tau) cbTau.getSelectedItem();
		Form_Toa form = new Form_Toa((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Toa", selected);
		form.setVisible(true);
		if (form.isConfirmed()) {
			Toa newToa = form.getEntity();
			if (toaDAO.insertToa(newToa)) {
				choNgoiDAO.insertBatchGhe(newToa);
				updateDashboardAndTable();
				JOptionPane.showMessageDialog(this, "Thêm toa thành công!");
			}
		}
	}

	private void handleEditToa(int row) {
		String maToa = (String) modelToa.getValueAt(row, 1);
		Toa t = toaDAO.getToaById(maToa);
		Form_Toa form = new Form_Toa((Frame) SwingUtilities.getWindowAncestor(this), "Sửa Toa", (Tau)cbTau.getSelectedItem());
		form.setEntity(t);
		form.setVisible(true);
		if (form.isConfirmed()) {
			if (toaDAO.updateToa(form.getEntity())) {
				updateDashboardAndTable();
				JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
			}
		}
	}

//    private void handleDelToa(int row) {
//        String maToa = (String) modelToa.getValueAt(row, 1);
//        int opt = JOptionPane.showConfirmDialog(this, "Xóa toa " + maToa + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
//        if(opt == JOptionPane.YES_OPTION) {
//            choNgoiDAO.deleteGheByToa(maToa);
//            if (toaDAO.deleteToa(maToa)) updateDashboardAndTable();
//        }
//    }

	private void setupTauRenderer() {
		cbTau.setRenderer(new DefaultListCellRenderer() {
			@Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
				super.getListCellRendererComponent(l, v, i, s, f);
				if (v instanceof Tau) setText(((Tau) v).getTenTau());
				return this;
			}
		});
	}

	private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
		JPanel p = new JPanel(new BorderLayout());
		p.setBackground(new Color(255, 255, 255));
		p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(226, 232, 240)), new EmptyBorder(12, 18, 12, 18)));
		JLabel t = new JLabel(title);
		t.setFont(new Font("Segoe UI", Font.BOLD, 11));
		t.setForeground(new Color(100, 116, 139));
		lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblValue.setForeground(accent);
		p.add(t, BorderLayout.NORTH); p.add(lblValue, BorderLayout.CENTER);
		return p;
	}

	private JButton createStyledButton(String text, Color bg) {
		JButton btn = new JButton(text);
		btn.setBackground(bg); btn.setForeground(Color.WHITE);
		btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		btn.setFocusPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.setBorder(new EmptyBorder(10, 20, 10, 20));
		return btn;
	}

	private JButton createTableButton(String text, Color bg) {
		JButton btn = new JButton(text);
		btn.setBackground(bg); btn.setForeground(Color.WHITE);
		btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
		btn.setPreferredSize(new Dimension(70, 32));
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		return btn;
	}

	class ButtonRenderer extends JPanel implements TableCellRenderer {
		public ButtonRenderer() {
			setLayout(new FlowLayout(FlowLayout.CENTER, 8, 12));
			setOpaque(true);
			add(createTableButton("Sửa", COLOR_EDIT));
		}
		@Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
			setBackground(s ? t.getSelectionBackground() : t.getBackground());
			return this;
		}
	}

	class ButtonEditor extends DefaultCellEditor {
		private JPanel panel;
		private int currentRow;
		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
			JButton btnEdit = createTableButton("Sửa", COLOR_EDIT);

			// Chỉ xử lý sự kiện cho nút Sửa
			btnEdit.addActionListener(e -> {
				fireEditingStopped();
				handleEditToa(currentRow);
			});

			panel.add(btnEdit);
		}
		@Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
			this.currentRow = r;
			panel.setBackground(t.getSelectionBackground());
			return panel;
		}
		@Override public Object getCellEditorValue() { return ""; }
	}
	// --- CLASS NỘI BỘ: CARD CHI TIẾT GHẾ ---
	class CustomDetailPanel extends JPanel {
		private JLabel lblTenCho, lblMaGhe, lblLoaiGhe, lblTrangThai, lblViTri;
		private JButton btnEdit;

		public CustomDetailPanel() {
			setPreferredSize(new Dimension(380, 280));
			setBackground(new Color(26, 28, 35));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(20, 25, 20, 25));

			// Khởi tạo các label (BẮT BUỘC PHẢI CÓ)
			JLabel title = new JLabel("CHI TIẾT GHẾ");
			title.setForeground(new Color(100, 116, 139));
			title.setFont(new Font("Segoe UI", Font.BOLD, 11));

			lblTenCho = new JLabel("---");
			lblTenCho.setForeground(new Color(64, 150, 255));
			lblTenCho.setFont(new Font("Segoe UI", Font.BOLD, 26));

			add(title);
			add(Box.createVerticalStrut(5));
			add(lblTenCho);
			add(Box.createVerticalStrut(15));

			// Khởi tạo và thêm các hàng thông tin
			lblMaGhe = addInfoRow("Mã ghế:", "---");
			lblLoaiGhe = addInfoRow("Loại ghế:", "---");
			lblTrangThai = addInfoRow("Trạng thái:", "---");
			lblViTri = addInfoRow("Vị trí:", "---");

			add(Box.createVerticalGlue());

			btnEdit = new JButton("Đổi trạng thái");
			btnEdit.setAlignmentX(CENTER_ALIGNMENT);
			btnEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
			btnEdit.setBackground(new Color(37, 99, 235));
			btnEdit.setForeground(Color.WHITE);
			btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 13));
			btnEdit.setFocusPainted(false);
			btnEdit.setBorderPainted(false);
			btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));

			btnEdit.addActionListener(e -> handleQuickEditStatus());
			add(btnEdit);
		}

		private JLabel addInfoRow(String label, String value) {
			JPanel p = new JPanel(new BorderLayout());
			p.setOpaque(false);
			p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
			JLabel l1 = new JLabel(label); l1.setForeground(new Color(150, 150, 160));
			JLabel l2 = new JLabel(value); l2.setForeground(Color.WHITE);
			l2.setFont(new Font("Segoe UI", Font.BOLD, 13));
			p.add(l1, BorderLayout.WEST); p.add(l2, BorderLayout.EAST);
			add(p);
			add(Box.createVerticalStrut(8));
			return l2;
		}

		public void updateInfo(ChoNgoi cn, String loaiToa) {
			lblTenCho.setText(cn.getTenCho());
			lblMaGhe.setText(cn.getMaCho());
			lblLoaiGhe.setText(loaiToa); // Đã đổ loại toa thành công
			lblViTri.setText("Vị trí: " + cn.getTenCho());

			btnEdit.setEnabled(true);
			if(cn.getTrangThai() == TrangThaiCho.TRONG) {
				lblTrangThai.setText("Còn trống");
				lblTrangThai.setForeground(new Color(34, 197, 94));
				btnEdit.setText("Chuyển sang BẢO TRÌ");
			} else if(cn.getTrangThai() == TrangThaiCho.BAOTRI) {
				lblTrangThai.setText("Đang bảo trì");
				lblTrangThai.setForeground(COLOR_MAINTENANCE);
				btnEdit.setText("Kích hoạt HOẠT ĐỘNG");
			} else {
				lblTrangThai.setText("Đã đặt");
				lblTrangThai.setForeground(COLOR_OCCUPIED);
				btnEdit.setText("Không thể chỉnh sửa");
				btnEdit.setEnabled(false);
			}
		}

		private void handleQuickEditStatus() {
			TrangThaiCho statusMoi = (selectedChoNgoi.getTrangThai() == TrangThaiCho.BAOTRI)
					? TrangThaiCho.TRONG : TrangThaiCho.BAOTRI;

			int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận đổi trạng thái ghế?", "Xác nhận", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				// Gọi DAO cập nhật DB (đảm bảo hàm updateTrangThai tồn tại trong DAO)
				if (choNgoiDAO.updateTrangThai(selectedChoNgoi.getMaCho(), statusMoi)) {
					selectedChoNgoi.setTrangThai(statusMoi);
					updateInfo(selectedChoNgoi, currentLoaiToa);

					// Cập nhật lại màu sắc nút trên sơ đồ mà không làm mất Card
					refreshSeatMapColors();
					JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
				}
			}
		}

		// Hàm làm mới màu sắc ghế mà không đóng Card
		private void refreshSeatMapColors() {
			// Duyệt qua tất cả các component trong pnlSeatsGrid để tìm các nút JButton và đổi màu
			refreshComponentColors(pnlSeatsGrid);
		}

		private void refreshComponentColors(Container container) {
			for (Component c : container.getComponents()) {
				if (c instanceof JButton) {
					JButton b = (JButton) c;
					if (b.getText().equals(selectedChoNgoi.getTenCho())) {
						if (selectedChoNgoi.getTrangThai() == TrangThaiCho.BAOTRI) {
							b.setBackground(COLOR_MAINTENANCE); b.setForeground(new Color(133, 77, 14));
						} else if (selectedChoNgoi.getTrangThai() == TrangThaiCho.TRONG) {
							b.setBackground(Color.WHITE); b.setForeground(new Color(30, 41, 59));
						}
					}
				} else if (c instanceof Container) {
					refreshComponentColors((Container) c);
				}
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getBackground());
			g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
			g2.dispose();
		}
	}
}