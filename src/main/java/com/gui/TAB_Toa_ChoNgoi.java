package com.gui;

import com.dao.*;
import com.entities.*;
import com.enums.TrangThaiCho;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.awt.geom.RoundRectangle2D;

public class TAB_Toa_ChoNgoi extends JPanel {
	// ================= COLOR =================
	private static final Color BG_PAGE = new Color(0xF4F7FB);
	private static final Color BG_CARD = Color.WHITE;
	private static final Color ACCENT = new Color(0x1A5EAB);
	private static final Color TEXT_DARK = new Color(0x1E2B3C);
	private static final Color TEXT_MID = new Color(0x5A6A7D);
	private static final Color BORDER = new Color(0xE2EAF4);
	private static final Color ROW_ALT = new Color(0xF7FAFF);
	private static final Color ROW_SEL = new Color(0xDDEEFF);
	private static final Color BTN2_BG = new Color(0xF0F4FA);
	private static final Color BTN2_FG = new Color(0x3A5A8C);

	private final Color COLOR_OCCUPIED = new Color(231, 76, 60);
	private final Color COLOR_MAINTENANCE = new Color(243, 156, 18);
	private final Color COLOR_SUCCESS = new Color(39, 174, 96);
	private final Color COLOR_AUTO = new Color(155, 89, 182);

	// ================= FONT =================
	private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
	private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
	private static final Font F_CELL = new Font("Segoe UI", Font.PLAIN, 13);

	private enum BtnStyle {
		PRIMARY, SECONDARY, AUTO, DANGER, WARNING, SUCCESS
	}

	private CardLayout cardLayout;
	private JPanel pnlMainContainer;
	private JTable tblToa;
	private DefaultTableModel modelToa;
	private JComboBox<Tau> cbTau;
	private JLabel lblSoToa, lblTongCho, lblDaDat, lblLapDay;

	private JPanel pnlSeatView, pnlSeatsGrid;
	private JLabel lblDetailTitle;
	private CustomDetailPanel pnlSeatDetail;
	private ChoNgoi selectedChoNgoi;
	private String currentLoaiToa = "";
	private String currentMaToaMap = "";

	private JButton btnAutoAdd, btnManualAdd, btnDeleteToa;
	private JButton btnMaintainAll, btnActiveAll;

	private DAO_Tau tauDAO = new DAO_Tau();
	private DAO_Toa toaDAO = new DAO_Toa();
	private DAO_ChoNgoi choNgoiDAO = new DAO_ChoNgoi();

	public TAB_Toa_ChoNgoi() {
		setLayout(new BorderLayout());
		cardLayout = new CardLayout();
		pnlMainContainer = new JPanel(cardLayout);

		initListToaScreen();
		initSeatViewScreen();

		add(pnlMainContainer);
		loadDataTau();

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				loadDataTau();
			}
		});
	}

	private void initListToaScreen() {
		JPanel pnlList = new JPanel(new BorderLayout(0, 20));
		pnlList.setBackground(BG_PAGE);
		pnlList.setBorder(new EmptyBorder(24, 24, 24, 24));

		JLabel lblMainTitle = new JLabel("QUẢN LÝ TOA & CHỖ NGỒI");
		lblMainTitle.setFont(F_TITLE);
		lblMainTitle.setForeground(ACCENT);

		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.setOpaque(false);
		JPanel pnlTitleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
		pnlTitleGroup.setOpaque(false);
		pnlTitleGroup.add(lblMainTitle);

		cbTau = new JComboBox<>();
		cbTau.setPreferredSize(new Dimension(250, 36));
		cbTau.setFont(F_CELL);
		cbTau.setBackground(new Color(0xF8FAFD));
		cbTau.setBorder(
				BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(2, 4, 2, 4)));
		setupTauRenderer();
		cbTau.addActionListener(e -> updateDashboardAndTable());

		cbTau.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
				Tau currentSelected = (Tau) cbTau.getSelectedItem();
				loadDataTau();
				if (currentSelected != null && cbTau.getItemCount() > 0) {
					for (int i = 0; i < cbTau.getItemCount(); i++) {
						if (cbTau.getItemAt(i).getMaTau().equals(currentSelected.getMaTau())) {
							cbTau.setSelectedIndex(i);
							break;
						}
					}
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
			}
		});

		btnDeleteToa = makeBtn("- Xóa Toa", BtnStyle.DANGER);
		btnAutoAdd = makeBtn("⚡ Tạo Tự Động", BtnStyle.AUTO);
		btnManualAdd = makeBtn("+ Thêm Thủ Công", BtnStyle.PRIMARY);

		btnDeleteToa.addActionListener(e -> handleDeleteToa());
		btnAutoAdd.addActionListener(e -> handleAutoAddToa());
		btnManualAdd.addActionListener(e -> handleAddToa());

		JPanel pnlHeaderActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlHeaderActions.setOpaque(false);
		pnlHeaderActions.add(cbTau);
		pnlHeaderActions.add(btnDeleteToa);
		pnlHeaderActions.add(btnAutoAdd);
		pnlHeaderActions.add(btnManualAdd);

		JPanel pnlFullTop = new JPanel(new GridLayout(2, 1, 0, 15));
		pnlFullTop.setOpaque(false);
		pnlFullTop.add(pnlTitleGroup);
		pnlFullTop.add(pnlHeaderActions);

		JPanel pnlStats = new JPanel(new GridLayout(1, 4, 15, 0));
		pnlStats.setOpaque(false);
		pnlStats.add(createStatCard("SỐ TOA", lblSoToa = new JLabel("0"), ACCENT));
		pnlStats.add(createStatCard("TỔNG CHỖ", lblTongCho = new JLabel("0"), new Color(52, 152, 219)));
		pnlStats.add(createStatCard("ĐÃ ĐẶT", lblDaDat = new JLabel("0"), COLOR_OCCUPIED));
		pnlStats.add(createStatCard("LẤP ĐẦY", lblLapDay = new JLabel("0%"), COLOR_SUCCESS));

		String[] columns = { "STT", "Mã Toa", "Tên Toa", "Loại Toa", "Số Ghế", "Thao Tác" };
		modelToa = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return c == 5;
			}
		};
		tblToa = buildTable();

		JScrollPane scroll = new JScrollPane(tblToa);
		scroll.setBorder(new LineBorder(BORDER));
		scroll.getViewport().setBackground(BG_CARD);
		styleScrollBar(scroll.getVerticalScrollBar());

		JPanel pnlContent = new JPanel(new BorderLayout(0, 15));
		pnlContent.setOpaque(false);
		pnlContent.add(pnlStats, BorderLayout.NORTH);
		pnlContent.add(scroll, BorderLayout.CENTER);

		pnlList.add(pnlFullTop, BorderLayout.NORTH);
		pnlList.add(pnlContent, BorderLayout.CENTER);
		pnlMainContainer.add(pnlList, "LIST");
	}

	private void initSeatViewScreen() {
		pnlSeatView = new JPanel(new BorderLayout(0, 20));
		pnlSeatView.setBackground(BG_PAGE);
		pnlSeatView.setBorder(new EmptyBorder(24, 24, 24, 24));

		JButton btnBack = makeBtn("< Quay lại", BtnStyle.SECONDARY);
		btnBack.setPreferredSize(new Dimension(110, 36));
		btnBack.addActionListener(e -> cardLayout.show(pnlMainContainer, "LIST"));

		lblDetailTitle = new JLabel("SƠ ĐỒ THIẾT KẾ TOA");
		lblDetailTitle.setFont(F_TITLE);
		lblDetailTitle.setForeground(ACCENT);

		btnMaintainAll = makeBtn("🛠 Bảo trì toàn toa", BtnStyle.WARNING);
		btnActiveAll = makeBtn("✅ Mở lại toàn toa", BtnStyle.SUCCESS);
		btnMaintainAll.setPreferredSize(new Dimension(160, 36));
		btnActiveAll.setPreferredSize(new Dimension(160, 36));

		btnMaintainAll.addActionListener(e -> handleBulkUpdateSeats(TrangThaiCho.BAOTRI,
				"Bạn có chắc chắn muốn chuyển tất cả ghế trống sang trạng thái BẢO TRÌ?"));
		btnActiveAll.addActionListener(e -> handleBulkUpdateSeats(TrangThaiCho.TRONG,
				"Bạn có chắc chắn muốn mở lại (TRỐNG) tất cả các ghế đang bảo trì?"));

		JPanel pHeaderLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		pHeaderLeft.setOpaque(false);
		pHeaderLeft.add(btnBack);
		pHeaderLeft.add(lblDetailTitle);

		JPanel pHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pHeaderRight.setOpaque(false);
		pHeaderRight.add(btnMaintainAll);
		pHeaderRight.add(btnActiveAll);

		JPanel pHeader = new JPanel(new BorderLayout());
		pHeader.setOpaque(false);
		pHeader.add(pHeaderLeft, BorderLayout.WEST);
		pHeader.add(pHeaderRight, BorderLayout.EAST);

		pnlSeatsGrid = new JPanel(new BorderLayout());
		pnlSeatsGrid.setBackground(BG_CARD);
		pnlSeatsGrid.setBorder(new ShadowBorder());

		pnlSeatDetail = new CustomDetailPanel();
		pnlSeatDetail.setVisible(false);

		JPanel pnlDetailWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlDetailWrapper.setOpaque(false);
		pnlDetailWrapper.add(pnlSeatDetail);

		pnlSeatView.add(pHeader, BorderLayout.NORTH);

		JScrollPane scrollSeats = new JScrollPane(pnlSeatsGrid);
		scrollSeats.setBorder(BorderFactory.createEmptyBorder());
		styleScrollBar(scrollSeats.getVerticalScrollBar());

		pnlSeatView.add(scrollSeats, BorderLayout.CENTER);
		pnlSeatView.add(pnlDetailWrapper, BorderLayout.SOUTH);
		pnlMainContainer.add(pnlSeatView, "SEATS");
	}

	private void showSeatLayout(int row) {
		String maToa = modelToa.getValueAt(row, 1).toString();
		String tenToa = modelToa.getValueAt(row, 2).toString();
		String loaiToa = modelToa.getValueAt(row, 3).toString();
		this.currentLoaiToa = loaiToa;
		this.currentMaToaMap = maToa;
		int soGhe = (int) modelToa.getValueAt(row, 4);

		lblDetailTitle.setText("Toa " + tenToa + " (" + loaiToa + ")");
		pnlSeatsGrid.removeAll();
		pnlSeatDetail.setVisible(false);

		List<ChoNgoi> ds = choNgoiDAO.getChoNgoiByToa(maToa);

		JPanel pnlMapContainer = new JPanel(new BorderLayout());
		pnlMapContainer.setBackground(BG_CARD);
		pnlMapContainer.setBorder(new EmptyBorder(20, 50, 20, 50));

		if (loaiToa.toLowerCase().contains("giường nằm") || loaiToa.toLowerCase().contains("nằm")) {
			pnlMapContainer.add(renderSleeperMap(ds), BorderLayout.CENTER);
		} else {
			pnlMapContainer.add(renderSeaterMap(ds, soGhe), BorderLayout.CENTER);
		}

		JPanel pnlWrapper = new JPanel(new BorderLayout());
		pnlWrapper.setOpaque(false);
		pnlWrapper.add(pnlMapContainer, BorderLayout.CENTER);
		pnlWrapper.add(createLegendPanel(), BorderLayout.SOUTH);

		pnlSeatsGrid.add(pnlWrapper, BorderLayout.NORTH);
		pnlSeatsGrid.revalidate();
		pnlSeatsGrid.repaint();
		cardLayout.show(pnlMainContainer, "SEATS");
	}

	private void handleBulkUpdateSeats(TrangThaiCho statusMoi, String message) {
		if (currentMaToaMap.isEmpty())
			return;
		int confirm = JOptionPane.showConfirmDialog(this, message, "Xác nhận hàng loạt", JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			if (choNgoiDAO.updateTrangThaiToanToa(currentMaToaMap, statusMoi)) {
				JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái toàn toa thành công!", "Thành công",
						JOptionPane.INFORMATION_MESSAGE);
				for (int i = 0; i < tblToa.getRowCount(); i++) {
					if (tblToa.getValueAt(i, 1).toString().equals(currentMaToaMap)) {
						showSeatLayout(i);
						break;
					}
				}
			} else {
				JOptionPane.showMessageDialog(this, "Cập nhật thất bại hoặc không có ghế nào hợp lệ để đổi trạng thái.",
						"Thông báo", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private JPanel renderSeaterMap(List<ChoNgoi> list, int total) {
		JPanel pnl = new JPanel(new GridLayout(5, 1, 0, 4));
		pnl.setOpaque(false);
		pnl.setBorder(new EmptyBorder(10, 10, 10, 10));
		int perRow = total / 4;
		for (int i = 0; i < 5; i++) {
			if (i == 2) {
				JPanel pnlAisle = new JPanel();
				pnlAisle.setOpaque(false);
				pnlAisle.setPreferredSize(new Dimension(10, 20));
				pnl.add(pnlAisle);
				continue;
			}
			JPanel rowPanel = new JPanel(new GridLayout(1, perRow, 8, 0));
			rowPanel.setOpaque(false);
			rowPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
			int rowIndex = (i > 2) ? i - 1 : i;
			for (int j = 0; j < perRow; j++) {
				int index = rowIndex + (j * 4);
				if (index < list.size())
					rowPanel.add(createSeatButton(list.get(index)));
				else
					rowPanel.add(Box.createGlue());
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
			lblKhoang.setFont(F_LABEL);
			lblKhoang.setForeground(TEXT_MID);
			JPanel pnlGiuong = new JPanel(new GridLayout(2, 2, 8, 8));
			pnlGiuong.setOpaque(false);
			for (int i = 0; i < 4; i++) {
				int index = (k * 4) + i;
				if (index < list.size())
					pnlGiuong.add(createSeatButton(list.get(index)));
			}
			pnlKhoang.add(lblKhoang, BorderLayout.NORTH);
			pnlKhoang.add(pnlGiuong, BorderLayout.CENTER);
			pnl.add(pnlKhoang);
			if (k < soKhoang - 1)
				pnl.add(new JSeparator(JSeparator.VERTICAL));
		}
		return pnl;
	}

	private JButton createSeatButton(ChoNgoi cn) {
		JButton btn = new JButton(cn.getTenCho());
		btn.setPreferredSize(new Dimension(46, 42));
		btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
		btn.setFocusPainted(false);
		if (cn.getTrangThai() == TrangThaiCho.DADAT) {
			btn.setBackground(COLOR_OCCUPIED);
			btn.setForeground(Color.WHITE);
		} else if (cn.getTrangThai() == TrangThaiCho.BAOTRI) {
			btn.setBackground(COLOR_MAINTENANCE);
			btn.setForeground(Color.WHITE);
		} else {
			btn.setBackground(new Color(241, 245, 249));
			btn.setForeground(TEXT_DARK);
		}
		btn.setBorder(new LineBorder(BORDER));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.addActionListener(e -> {
			this.selectedChoNgoi = cn;
			pnlSeatDetail.updateInfo(cn, currentLoaiToa);
			pnlSeatDetail.setVisible(true);
			pnlSeatDetail.revalidate();
			pnlSeatDetail.repaint();
		});
		return btn;
	}

	private JPanel createLegendPanel() {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
		pnl.setOpaque(false);
		pnl.add(createLegendItem("Trống", new Color(241, 245, 249)));
		pnl.add(createLegendItem("Đã đặt", COLOR_OCCUPIED));
		pnl.add(createLegendItem("Bảo trì", COLOR_MAINTENANCE));
		return pnl;
	}

	private JPanel createLegendItem(String text, Color color) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		p.setOpaque(false);
		JPanel box = new JPanel();
		box.setPreferredSize(new Dimension(18, 18));
		box.setBackground(color);
		box.setBorder(new LineBorder(BORDER));
		JLabel lbl = new JLabel(text);
		lbl.setFont(F_CELL);
		lbl.setForeground(TEXT_DARK);
		p.add(box);
		p.add(lbl);
		return p;
	}

	public void loadDataTau() {
		Tau currentSelected = (Tau) cbTau.getSelectedItem();
		ActionListener[] listeners = cbTau.getActionListeners();
		for (ActionListener l : listeners)
			cbTau.removeActionListener(l);

		cbTau.removeAllItems();
		List<Tau> ds = tauDAO.getAllTau();
		ds.forEach(cbTau::addItem);

		if (currentSelected != null && cbTau.getItemCount() > 0) {
			for (int i = 0; i < cbTau.getItemCount(); i++) {
				if (cbTau.getItemAt(i).getMaTau().equals(currentSelected.getMaTau())) {
					cbTau.setSelectedIndex(i);
					break;
				}
			}
		}
		for (ActionListener l : listeners)
			cbTau.addActionListener(l);
		updateDashboardAndTable();
	}

	private void updateDashboardAndTable() {
		modelToa.setRowCount(0);
		Tau selected = (Tau) cbTau.getSelectedItem();
		if (selected == null)
			return;

		List<Toa> dsToa = toaDAO.getToaByMaTau(selected.getMaTau());
		int totalSeatsAll = 0, totalBookedAll = 0;

		for (int i = 0; i < dsToa.size(); i++) {
			Toa t = dsToa.get(i);
			int booked = choNgoiDAO.countGheByTrangThai(t.getMaToa(), TrangThaiCho.DADAT);
			modelToa.addRow(new Object[] { i + 1, t.getMaToa(), t.getTenToa(), t.getLoaiToa().getTenLoaiToa(),
					t.getSoGhe(), "" });
			totalSeatsAll += t.getSoGhe();
			totalBookedAll += booked;
		}

		lblSoToa.setText(String.valueOf(dsToa.size()) + " / " + selected.getSoToa());
		lblTongCho.setText(String.valueOf(totalSeatsAll));
		lblDaDat.setText(String.valueOf(totalBookedAll));
		lblLapDay.setText(totalSeatsAll > 0 ? (totalBookedAll * 100 / totalSeatsAll) + "%" : "0%");

		int maxCars = selected.getSoToa();
		int currentCars = dsToa.size();

		if (currentCars >= maxCars) {
			btnAutoAdd.setVisible(false);
			btnManualAdd.setVisible(false);
		} else if (currentCars == 0) {
			btnAutoAdd.setVisible(true);
			btnManualAdd.setVisible(true);
		} else {
			btnAutoAdd.setVisible(false);
			btnManualAdd.setVisible(true);
		}
	}

	private void handleDeleteToa() {
		int row = tblToa.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 toa trong bảng để xóa!", "Thông báo",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		String maToa = modelToa.getValueAt(row, 1).toString();
		String tenToa = modelToa.getValueAt(row, 2).toString();

		int booked = choNgoiDAO.countGheByTrangThai(maToa, TrangThaiCho.DADAT);
		if (booked > 0) {
			JOptionPane.showMessageDialog(this, "Không thể xóa! Toa này đang có " + booked + " vé đã được bán.",
					"Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"Xác nhận xóa " + tenToa + " (" + maToa + ") và toàn bộ ghế bên trong?", "Xóa Toa",
				JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			choNgoiDAO.deleteGheByToa(maToa);
			if (toaDAO.deleteToa(maToa)) {
				JOptionPane.showMessageDialog(this, "Đã xóa toa thành công!");
				updateDashboardAndTable();
			} else {
				JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi xóa toa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void handleAutoAddToa() {
		Tau selected = (Tau) cbTau.getSelectedItem();
		if (selected == null)
			return;
		int soToaCanTao = selected.getSoToa();
		if (soToaCanTao <= 0) {
			JOptionPane.showMessageDialog(this, "Tàu này không có quy định số toa để tạo tự động!", "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		Form_TaoToaTuDong form = new Form_TaoToaTuDong((Frame) SwingUtilities.getWindowAncestor(this),
				"Tạo Tự Động Chia Phân Khúc Toa", selected);
		form.setVisible(true);

		if (form.isConfirmed()) {
			updateDashboardAndTable();
			JOptionPane.showMessageDialog(this, "Đã khởi tạo tự động " + soToaCanTao + " toa thành công!", "Thành công",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

//    private void handleAddToa() {
//        Tau selected = (Tau) cbTau.getSelectedItem();
//        int currentCars = toaDAO.getToaByMaTau(selected.getMaTau()).size();
//        if (currentCars >= selected.getSoToa()) {
//             JOptionPane.showMessageDialog(this, "Tàu này đã đủ số lượng " + selected.getSoToa() + " toa theo quy định. Không thể tạo thêm!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
//             return;
//        }
//        
//        Form_Toa form = new Form_Toa((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Toa Mới", selected);
//        form.setVisible(true);
//        if (form.isConfirmed()) {
//            Toa newToa = form.getEntity();
//            if (toaDAO.insertToa(newToa)) {
//                choNgoiDAO.insertBatchGhe(newToa); 
//                JOptionPane.showMessageDialog(this, "Đã thêm toa thủ công thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
//                updateDashboardAndTable();
//            }
//        }
//    }
	private void handleAddToa() {
		Tau selected = (Tau) cbTau.getSelectedItem();
		int currentCars = toaDAO.getToaByMaTau(selected.getMaTau()).size();
		if (currentCars >= selected.getSoToa()) {
			JOptionPane.showMessageDialog(this,
					"Tàu này đã đủ số lượng " + selected.getSoToa() + " toa theo quy định. Không thể tạo thêm!",
					"Cảnh báo", JOptionPane.WARNING_MESSAGE);
			return;
		}

		Form_Toa form = new Form_Toa((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Toa Mới", selected);
		form.setupForAdd(); // MỚI THÊM: Gọi hàm sinh mã trước khi hiển thị
		form.setVisible(true);
		if (form.isConfirmed()) {
			Toa newToa = form.getEntity();
			if (toaDAO.insertToa(newToa)) {
				choNgoiDAO.insertBatchGhe(newToa);
				JOptionPane.showMessageDialog(this, "Đã thêm toa thủ công thành công!", "Thành công",
						JOptionPane.INFORMATION_MESSAGE);
				updateDashboardAndTable();
			}
		}
	}

	private void handleEditToa(int row) {
		String maToa = (String) modelToa.getValueAt(row, 1);
		Toa oldToa = toaDAO.getToaById(maToa);
		int booked = choNgoiDAO.countGheByTrangThai(maToa, TrangThaiCho.DADAT);

		Form_Toa form = new Form_Toa((Frame) SwingUtilities.getWindowAncestor(this), "Sửa Thông Tin Toa",
				(Tau) cbTau.getSelectedItem());
		form.setEntity(oldToa);
		form.setVisible(true);

		if (form.isConfirmed()) {
			Toa updatedToa = form.getEntity();
			boolean isStructureChanged = (updatedToa.getSoGhe() != oldToa.getSoGhe())
					|| (!updatedToa.getLoaiToa().getMaLoaiToa().equals(oldToa.getLoaiToa().getMaLoaiToa()));

			if (isStructureChanged && booked > 0) {
				JOptionPane.showMessageDialog(this,
						"Toa này đang có " + booked
								+ " ghế được đặt. Không thể thay đổi cấu trúc Số lượng ghế hoặc Loại toa!",
						"Lỗi Logic", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (toaDAO.updateToa(updatedToa)) {
				if (isStructureChanged) {
					choNgoiDAO.deleteGheByToa(maToa);
					choNgoiDAO.insertBatchGhe(updatedToa);
				}
				JOptionPane.showMessageDialog(this, "Cập nhật thông tin toa thành công!", "Thành công",
						JOptionPane.INFORMATION_MESSAGE);
				updateDashboardAndTable();
			} else {
				JOptionPane.showMessageDialog(this, "Cập nhật thất bại, vui lòng thử lại!", "Lỗi",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void setupTauRenderer() {
		cbTau.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
				super.getListCellRendererComponent(l, v, i, s, f);
				if (v instanceof Tau)
					setText(((Tau) v).getTenTau());
				return this;
			}
		});
	}

	private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
		JPanel p = new JPanel(new BorderLayout());
		p.setBackground(BG_CARD);
		p.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(15, 20, 15, 20)));
		JLabel lblT = new JLabel(title);
		lblT.setForeground(TEXT_MID);
		lblT.setFont(F_LABEL);
		lblValue.setForeground(accent);
		lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
		p.add(lblT, BorderLayout.NORTH);
		p.add(lblValue, BorderLayout.CENTER);
		return p;
	}

	private JButton makeBtn(String text, BtnStyle style) {
		JButton b = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (style == BtnStyle.PRIMARY) {
					g2.setColor(getModel().isRollover() ? new Color(46, 204, 113) : COLOR_SUCCESS);
				} else if (style == BtnStyle.AUTO) {
					g2.setColor(getModel().isRollover() ? new Color(142, 68, 173) : COLOR_AUTO);
				} else if (style == BtnStyle.DANGER) {
					g2.setColor(getModel().isRollover() ? new Color(231, 76, 60) : new Color(192, 57, 43));
				} else if (style == BtnStyle.WARNING) {
					g2.setColor(getModel().isRollover() ? new Color(241, 196, 15) : new Color(243, 156, 18));
				} else {
					g2.setColor(getModel().isRollover() ? new Color(0xE0ECFF) : BTN2_BG);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				if (style == BtnStyle.SECONDARY) {
					g2.setColor(BORDER);
					g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
				}
				g2.dispose();
				super.paintComponent(g);
			}
		};
		b.setFont(F_LABEL);
		b.setForeground(style != BtnStyle.SECONDARY ? Color.WHITE : BTN2_FG);
		b.setPreferredSize(new Dimension(140, 36));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JTable buildTable() {
		JTable t = new JTable(modelToa) {
			@Override
			public Component prepareRenderer(TableCellRenderer r, int row, int col) {
				Component c = super.prepareRenderer(r, row, col);
				if (!isRowSelected(row))
					c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
				return c;
			}
		};
		t.setRowHeight(44);
		t.setFont(F_CELL);
		t.setBackground(BG_CARD);
		t.setSelectionBackground(ROW_SEL);
		t.setSelectionForeground(TEXT_DARK);
		t.setGridColor(BORDER);
		t.setShowHorizontalLines(true);
		t.setShowVerticalLines(false);
		t.setFocusable(false);
		t.setIntercellSpacing(new Dimension(0, 0));

		JTableHeader h = t.getTableHeader();
		h.setDefaultRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row,
					int col) {
				JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
				l.setOpaque(true);
				l.setBackground(ACCENT);
				l.setForeground(Color.WHITE);
				l.setFont(F_LABEL);
				if (col == 0 || col == 4 || col == 5) {
					l.setHorizontalAlignment(CENTER);
					l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
				} else {
					l.setHorizontalAlignment(LEFT);
					l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
				}
				return l;
			}
		});
		h.setPreferredSize(new Dimension(0, 42));
		h.setReorderingAllowed(false);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		DefaultTableCellRenderer leftPaddingRenderer = new DefaultTableCellRenderer();
		leftPaddingRenderer.setHorizontalAlignment(JLabel.LEFT);
		leftPaddingRenderer.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));

		t.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		t.getColumnModel().getColumn(1).setCellRenderer(leftPaddingRenderer);
		t.getColumnModel().getColumn(2).setCellRenderer(leftPaddingRenderer);
		t.getColumnModel().getColumn(3).setCellRenderer(leftPaddingRenderer);
		t.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

		// Gắn Nút Sửa "Ghost Button" Mới
		t.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
		t.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

		t.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && t.getSelectedColumn() < 5) {
					showSeatLayout(t.getSelectedRow());
				}
			}
		});
		return t;
	}

	private void styleScrollBar(JScrollBar sb) {
		sb.setUI(new BasicScrollBarUI() {
			@Override
			protected void configureScrollBarColors() {
				thumbColor = new Color(0xC0D4EE);
				trackColor = BG_PAGE;
			}

			@Override
			protected JButton createDecreaseButton(int o) {
				JButton b = new JButton();
				b.setPreferredSize(new Dimension(0, 0));
				return b;
			}

			@Override
			protected JButton createIncreaseButton(int o) {
				JButton b = new JButton();
				b.setPreferredSize(new Dimension(0, 0));
				return b;
			}
		});
	}

	// --- NÚT "SỬA" KIỂU GHOST (TRONG SUỐT, HOVER LÊN MÀU CAM) ---
	class ButtonRenderer extends JPanel implements TableCellRenderer {
		public ButtonRenderer() {
			setLayout(new GridBagLayout());
			setOpaque(true);

			JButton b = new JButton("✎ Sửa");
			b.setFont(new Font("Segoe UI", Font.BOLD, 12));
			b.setForeground(new Color(243, 156, 18)); // Màu cam mặc định
			b.setPreferredSize(new Dimension(70, 30));
			b.setContentAreaFilled(false);
			b.setBorderPainted(false);
			b.setFocusPainted(false);
			add(b);
		}

		@Override
		public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
			setBackground(s ? t.getSelectionBackground() : (r % 2 == 0 ? BG_CARD : ROW_ALT));
			return this;
		}
	}

	class ButtonEditor extends DefaultCellEditor {
		private JPanel panel;
		private int currentRow;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			panel = new JPanel(new GridBagLayout());

			JButton btnEdit = new JButton("✎ Sửa") {
				@Override
				protected void paintComponent(Graphics g) {
					if (getModel().isRollover()) {
						Graphics2D g2 = (Graphics2D) g.create();
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2.setColor(new Color(243, 156, 18)); // Đổ nền màu cam khi hover
						g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
						g2.dispose();
						setForeground(Color.WHITE); // Chữ trắng khi hover
					} else {
						setForeground(new Color(243, 156, 18)); // Chữ cam khi bình thường
					}
					super.paintComponent(g);
				}
			};
			btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
			btnEdit.setPreferredSize(new Dimension(70, 30));
			btnEdit.setContentAreaFilled(false);
			btnEdit.setBorderPainted(false);
			btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnEdit.addActionListener(e -> {
				fireEditingStopped();
				handleEditToa(currentRow);
			});
			panel.add(btnEdit);
		}

		@Override
		public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
			this.currentRow = r;
			panel.setBackground(t.getSelectionBackground());
			return panel;
		}

		@Override
		public Object getCellEditorValue() {
			return "";
		}
	}

	// =========================================================================
	// DIALOG TẠO TỰ ĐỘNG CHIA 3 PHẦN
	// =========================================================================
	class Form_TaoToaTuDong extends JDialog {
		private boolean confirmed = false;
		private Tau tau;

		public Form_TaoToaTuDong(Frame parent, String title, Tau tau) {
			super(parent, title, true);
			this.tau = tau;
			setLayout(new BorderLayout());
			getContentPane().setBackground(BG_PAGE);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

			JPanel form = new JPanel(new GridBagLayout());
			form.setOpaque(false);
			form.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
			GridBagConstraints gc = new GridBagConstraints();
			gc.insets = new Insets(8, 6, 8, 6);
			gc.anchor = GridBagConstraints.WEST;
			gc.fill = GridBagConstraints.HORIZONTAL;

			int n = tau.getSoToa();
			int part1 = n / 3;
			int part2 = n / 3;
			int part3 = n - part1 - part2;

			int r = 0;
			gc.gridx = 0;
			gc.gridy = r;
			gc.gridwidth = 2;
			gc.weightx = 1;
			JLabel lTitle = new JLabel("Hệ thống sẽ tự động tạo " + n + " toa theo cấu trúc sau:");
			lTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
			lTitle.setForeground(ACCENT);
			form.add(lTitle, gc);

			r++;
			gc.gridy = r;
			String textP1 = (part1 > 0) ? "• Toa 1 đến " + part1 + ": Ghế cứng (64 ghế)" : "• Không có toa Ghế cứng";
			JLabel lP1 = new JLabel(textP1);
			lP1.setFont(F_LABEL);
			lP1.setForeground(TEXT_DARK);
			form.add(lP1, gc);

			r++;
			gc.gridy = r;
			String textP2 = (part2 > 0) ? "• Toa " + (part1 + 1) + " đến " + (part1 + part2) + ": Ghế mềm (64 ghế)"
					: "• Không có toa Ghế mềm";
			JLabel lP2 = new JLabel(textP2);
			lP2.setFont(F_LABEL);
			lP2.setForeground(TEXT_DARK);
			form.add(lP2, gc);

			r++;
			gc.gridy = r;
			String textP3 = (part3 > 0) ? "• Toa " + (part1 + part2 + 1) + " đến " + n + ": Giường nằm (32 ghế)"
					: "• Không có toa Giường nằm";
			JLabel lP3 = new JLabel(textP3);
			lP3.setFont(F_LABEL);
			lP3.setForeground(TEXT_DARK);
			form.add(lP3, gc);

			add(form, BorderLayout.CENTER);

			JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
			bar.setOpaque(false);
			JButton btnSave = makeBtn("Xác nhận tạo", BtnStyle.PRIMARY);
			JButton btnCancel = makeBtn("Hủy", BtnStyle.SECONDARY);
			bar.add(btnCancel);
			bar.add(btnSave);
			add(bar, BorderLayout.SOUTH);

			btnCancel.addActionListener(e -> dispose());
			btnSave.addActionListener(e -> {
				List<LoaiToa> dsLoai = new DAO_LoaiToa().getAllLoaiToa();
				LoaiToa ltCung = dsLoai.stream().filter(l -> l.getTenLoaiToa().toLowerCase().contains("cứng"))
						.findFirst().orElse(dsLoai.get(0));
				LoaiToa ltMem = dsLoai.stream().filter(l -> l.getTenLoaiToa().toLowerCase().contains("mềm")).findFirst()
						.orElse(dsLoai.get(0));
				LoaiToa ltNam = dsLoai.stream().filter(l -> l.getTenLoaiToa().toLowerCase().contains("nằm")).findFirst()
						.orElse(dsLoai.get(0));

				for (int i = 1; i <= n; i++) {
					Toa newToa = new Toa();
					newToa.setMaToa(tau.getMaTau() + String.format("_T_%02d", i));
					newToa.setTenToa("Toa " + i);
					newToa.setTau(tau);

					if (i <= part1) {
						newToa.setLoaiToa(ltCung);
						newToa.setSoGhe(64);
					} else if (i <= part1 + part2) {
						newToa.setLoaiToa(ltMem);
						newToa.setSoGhe(64);
					} else {
						newToa.setLoaiToa(ltNam);
						newToa.setSoGhe(32);
					}

					if (toaDAO.insertToa(newToa)) {
						choNgoiDAO.insertBatchGhe(newToa);
					}
				}
				confirmed = true;
				dispose();
			});

			pack();
			setMinimumSize(new Dimension(450, getHeight()));
			setResizable(false);
			setLocationRelativeTo(parent);
		}

		public boolean isConfirmed() {
			return confirmed;
		}
	}

	// --- CARD CHI TIẾT (LIGHT THEME) ---
	class CustomDetailPanel extends JPanel {
		private JLabel lblTenCho, lblMaGhe, lblLoaiGhe, lblTrangThai, lblViTri;
		private JButton btnEdit;

		public CustomDetailPanel() {
			setPreferredSize(new Dimension(380, 280));
			setBackground(BG_CARD);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(20, 25, 20, 25)));

			JLabel title = new JLabel("CHI TIẾT GHẾ");
			title.setForeground(TEXT_MID);
			title.setFont(new Font("Segoe UI", Font.BOLD, 11));

			lblTenCho = new JLabel("---");
			lblTenCho.setForeground(ACCENT);
			lblTenCho.setFont(new Font("Segoe UI", Font.BOLD, 26));

			add(title);
			add(Box.createVerticalStrut(5));
			add(lblTenCho);
			add(Box.createVerticalStrut(15));

			lblMaGhe = addInfoRow("Mã ghế:", "---");
			lblLoaiGhe = addInfoRow("Loại ghế:", "---");
			lblTrangThai = addInfoRow("Trạng thái:", "---");
			lblViTri = addInfoRow("Vị trí:", "---");

			add(Box.createVerticalGlue());

			btnEdit = new JButton("Đổi trạng thái");
			btnEdit.setAlignmentX(CENTER_ALIGNMENT);
			btnEdit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
			btnEdit.setBackground(ACCENT);
			btnEdit.setForeground(Color.WHITE);
			btnEdit.setFont(F_LABEL);
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
			JLabel l1 = new JLabel(label);
			l1.setForeground(TEXT_MID);
			l1.setFont(F_CELL);
			JLabel l2 = new JLabel(value);
			l2.setForeground(TEXT_DARK);
			l2.setFont(F_LABEL);
			p.add(l1, BorderLayout.WEST);
			p.add(l2, BorderLayout.EAST);
			add(p);
			add(Box.createVerticalStrut(8));
			return l2;
		}

		public void updateInfo(ChoNgoi cn, String loaiToa) {
			lblTenCho.setText(cn.getTenCho());
			lblMaGhe.setText(cn.getMaCho());
			lblLoaiGhe.setText(loaiToa);
			lblViTri.setText("Vị trí: " + cn.getTenCho());

			btnEdit.setEnabled(true);
			if (cn.getTrangThai() == TrangThaiCho.TRONG) {
				lblTrangThai.setText("Còn trống");
				lblTrangThai.setForeground(COLOR_SUCCESS);
				btnEdit.setText("Chuyển sang BẢO TRÌ");
				btnEdit.setBackground(COLOR_MAINTENANCE);
			} else if (cn.getTrangThai() == TrangThaiCho.BAOTRI) {
				lblTrangThai.setText("Đang bảo trì");
				lblTrangThai.setForeground(COLOR_MAINTENANCE);
				btnEdit.setText("Kích hoạt HOẠT ĐỘNG");
				btnEdit.setBackground(COLOR_SUCCESS);
			} else {
				lblTrangThai.setText("Đã đặt");
				lblTrangThai.setForeground(COLOR_OCCUPIED);
				btnEdit.setText("Không thể chỉnh sửa");
				btnEdit.setBackground(Color.GRAY);
				btnEdit.setEnabled(false);
			}
		}

		private void handleQuickEditStatus() {
			TrangThaiCho statusMoi = (selectedChoNgoi.getTrangThai() == TrangThaiCho.BAOTRI) ? TrangThaiCho.TRONG
					: TrangThaiCho.BAOTRI;
			int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận đổi trạng thái ghế?", "Xác nhận",
					JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				if (choNgoiDAO.updateTrangThai(selectedChoNgoi.getMaCho(), statusMoi)) {
					selectedChoNgoi.setTrangThai(statusMoi);
					updateInfo(selectedChoNgoi, currentLoaiToa);
					refreshComponentColors(pnlSeatsGrid);
					JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
				}
			}
		}

		private void refreshComponentColors(Container container) {
			for (Component c : container.getComponents()) {
				if (c instanceof JButton) {
					JButton b = (JButton) c;
					if (b.getText().equals(selectedChoNgoi.getTenCho())) {
						if (selectedChoNgoi.getTrangThai() == TrangThaiCho.BAOTRI) {
							b.setBackground(COLOR_MAINTENANCE);
							b.setForeground(Color.WHITE);
						} else if (selectedChoNgoi.getTrangThai() == TrangThaiCho.TRONG) {
							b.setBackground(new Color(241, 245, 249));
							b.setForeground(TEXT_DARK);
						}
					}
				} else if (c instanceof Container)
					refreshComponentColors((Container) c);
			}
		}
	}

	private static class ShadowBorder extends AbstractBorder {
		private static final int S = 4;

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (int i = S; i > 0; i--) {
				g2.setColor(new Color(100, 140, 200, (int) (20.0 * (S - i) / S)));
				g2.drawRoundRect(x + i, y + i, w - 2 * i - 1, h - 2 * i - 1, 12, 12);
			}
			g2.setColor(BORDER);
			g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
			g2.setColor(BG_CARD);
			g2.setClip(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, 12, 12));
			g2.fillRect(x + 1, y + 1, w - 2, h - 2);
			g2.dispose();
		}

		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(S, S, S, S);
		}
	}
}