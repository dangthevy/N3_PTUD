package com.gui;

import com.dao.DAO_ChiTietTau;
import com.dao.DAO_LoaiToa;
import com.dao.DAO_Tau;
import com.dao.DAO_Toa;
import com.entities.LoaiToa;
import com.entities.Tau;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TAB_QuanLyDoanTau extends JPanel {
	private static final Color BG_PAGE = new Color(0xF4F7FB);
	private static final Color ACCENT = new Color(0x1A5EAB);
	private static final Color ACCENT_LIGHT = new Color(0xE8F0FB);
	private static final Color C_WHITE = Color.WHITE;
	private static final Color C_SUCCESS = new Color(0x27AE60);
	private static final Color C_WARNING = new Color(0xE67E22);
	private static final Color C_DANGER = new Color(0xC0392B);
	private static final Color C_GRAY = new Color(0x7F8C8D);
	private static final Color C_BORDER = new Color(0xDDE6F5);

	private static final Color GHE_TRONG = new Color(0x2ECC71);
	private static final Color GHE_DAT = new Color(0xE74C3C);
	private static final Color GHE_CHON = new Color(0xF39C12);

	private static final Font F_HEADER = new Font("Segoe UI", Font.BOLD, 14);
	private static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 13);
	private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
	private static final Font F_SEAT = new Font("Segoe UI", Font.BOLD, 10);

	private CardLayout cardLayout;
	private JPanel pnlCards;

	private JTable tblTau, tblToa;
	private DefaultTableModel modTau, modToa;
	private JComboBox<String> cbKho;
	private JPanel pnlMap;
	private JPanel pnlTrainBar; // THANH CHỌN TOA TÀU ĐÃ ĐƯỢC PHỤC HỒI
	private JLabel lblMapTitle, lblSeatStats;

	private String currentTau = null;
	private String currentMaToa = null; // Biến lưu Toa đang được chọn để đổi màu Tàu
	private int quyDinhSoToa = 0;

	private DAO_Tau daoTau = new DAO_Tau();
	private DAO_Toa daoToa = new DAO_Toa();
	private DAO_ChiTietTau daoCT = new DAO_ChiTietTau();

	class LoaiToaWrapper {
		LoaiToa lt;
		int ghe;

		public LoaiToaWrapper(LoaiToa lt, int ghe) {
			this.lt = lt;
			this.ghe = ghe;
		}

		@Override
		public String toString() {
			return lt.getTenLoaiToa() + " - " + ghe + " chỗ (" + lt.getMaLoaiToa() + ")";
		}
	}

	public TAB_QuanLyDoanTau() {
		setLayout(new BorderLayout());
		cardLayout = new CardLayout();
		pnlCards = new JPanel(cardLayout);
		JPanel pnlMainView = createMainView();

		TAB_Toa tabToa = new TAB_Toa(() -> {
			cardLayout.show(pnlCards, "MAIN");
			loadDsTau();
			loadKho();
			if (currentTau != null)
				loadToaOfTau();
		});
		TAB_LoaiToa tabLoaiToa = new TAB_LoaiToa(() -> {
			cardLayout.show(pnlCards, "MAIN");
			loadDsTau();
			loadKho();
			if (currentTau != null)
				loadToaOfTau();
		});

		pnlCards.add(pnlMainView, "MAIN");
		pnlCards.add(tabToa, "TOA");
		pnlCards.add(tabLoaiToa, "LOAITOA");
		add(pnlCards, BorderLayout.CENTER);
	}

	private JPanel createMainView() {
		JPanel pnlMainView = new JPanel(new BorderLayout(15, 15));
		pnlMainView.setBackground(BG_PAGE);
		pnlMainView.setBorder(new EmptyBorder(15, 15, 15, 15));

		JPanel pnlHeader = new JPanel(new BorderLayout());
		pnlHeader.setOpaque(false);
		JLabel lblTitle = new JLabel("QUẢN LÝ ĐOÀN TÀU");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
		lblTitle.setForeground(ACCENT);
		JPanel pnlGlobalActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlGlobalActions.setOpaque(false);
		JButton btnKhoToa = makeBtn("Quản lý Kho Toa", new Color(44, 62, 80));
		btnKhoToa.setPreferredSize(new Dimension(160, 34));
		JButton btnLoaiToa = makeBtn("Quản lý loại toa", new Color(127, 140, 141));
		btnLoaiToa.setPreferredSize(new Dimension(180, 34));

		btnKhoToa.addActionListener(e -> cardLayout.show(pnlCards, "TOA"));
		btnLoaiToa.addActionListener(e -> cardLayout.show(pnlCards, "LOAITOA"));

		pnlGlobalActions.add(btnLoaiToa);
		pnlGlobalActions.add(btnKhoToa);
		pnlHeader.add(lblTitle, BorderLayout.WEST);
		pnlHeader.add(pnlGlobalActions, BorderLayout.EAST);

		JSplitPane splitMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitMain.setOpaque(false);
		splitMain.setBorder(null);
		splitMain.setDividerLocation(350);
		splitMain.setDividerSize(8);
		JSplitPane splitTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitTop.setOpaque(false);
		splitTop.setBorder(null);
		splitTop.setDividerLocation(380);
		splitTop.setDividerSize(8);

		// ================= TÀU ĐẦU KÉO =================
		JPanel pnlLeft = new JPanel(new BorderLayout(0, 10));
		pnlLeft.setBackground(Color.WHITE);
		pnlLeft.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(10, 10, 10, 10)));
		JPanel pnlTauHeader = new JPanel(new BorderLayout());
		pnlTauHeader.setOpaque(false);
		JLabel lblTau = new JLabel("1. DANH SÁCH TÀU");
		lblTau.setFont(F_HEADER);
		lblTau.setForeground(ACCENT);
		JButton btnAddTau = makeBtn("+ Thêm Tàu", ACCENT);
		btnAddTau.setPreferredSize(new Dimension(100, 30));
		btnAddTau.addActionListener(e -> {
			Form_Tau f = new Form_Tau(JOptionPane.getFrameForComponent(this), "Thêm Tàu Mới");
			f.setVisible(true);
			if (f.isConfirmed()) {
				daoTau.insertTau(f.getEntity());
				loadDsTau();
			}
		});
		pnlTauHeader.add(lblTau, BorderLayout.WEST);
		pnlTauHeader.add(btnAddTau, BorderLayout.EAST);

		modTau = new DefaultTableModel(new String[] { "Mã Tàu", "Tên", "số toa" }, 0);
		tblTau = buildTable(modTau);
		tblTau.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int r = tblTau.getSelectedRow();
				if (r >= 0) {
					currentTau = modTau.getValueAt(r, 0).toString();
					quyDinhSoToa = Integer.parseInt(modTau.getValueAt(r, 2).toString().split(" ")[0]);
					loadToaOfTau();
					clearSeatMap();
					if (e.getClickCount() == 2) {
						Tau t = daoTau.getTauByMa(currentTau);
						Form_Tau f = new Form_Tau(JOptionPane.getFrameForComponent(TAB_QuanLyDoanTau.this),
								"Sửa Thông Tin Tàu");
						f.setEntity(t);
						f.setVisible(true);
						if (f.isConfirmed()) {
							daoTau.updateTau(f.getEntity());
							loadDsTau();
						}
					}
				}
			}
		});
		pnlLeft.add(pnlTauHeader, BorderLayout.NORTH);
		pnlLeft.add(new JScrollPane(tblTau), BorderLayout.CENTER);

		// ================= LẮP RÁP =================
		JPanel pnlRight = new JPanel(new BorderLayout(0, 10));
		pnlRight.setBackground(Color.WHITE);
		pnlRight.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(10, 10, 10, 10)));
		JPanel pnlToaHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		pnlToaHeader.setOpaque(false);
		JLabel lblToa = new JLabel("2. LẮP RÁP TOA: ");
		lblToa.setFont(F_HEADER);
		lblToa.setForeground(ACCENT);
		cbKho = new JComboBox<>();
		cbKho.setPreferredSize(new Dimension(150, 32));
		cbKho.setFont(F_BODY);

		JButton btnIn = makeBtn("Gắn", ACCENT);
		btnIn.setPreferredSize(new Dimension(70, 32));
		JButton btnOut = makeBtn("Gỡ", ACCENT);
		btnOut.setPreferredSize(new Dimension(70, 32));

		JButton btnLen = makeArrowBtn(true, new Color(41, 128, 185));
		JButton btnXuong = makeArrowBtn(false, new Color(41, 128, 185));
		btnLen.setToolTipText("Chuyển toa lên trên");
		btnXuong.setToolTipText("Chuyển toa xuống dưới");

		JButton btnAuto = makeBtn("Auto Sinh Toa", new Color(142, 68, 173));
		btnAuto.setPreferredSize(new Dimension(130, 32));

		btnIn.addActionListener(e -> ganToa());
		btnOut.addActionListener(e -> goToa());
		btnLen.addActionListener(e -> doDoiChoToa(-1));
		btnXuong.addActionListener(e -> doDoiChoToa(+1));
		btnAuto.addActionListener(e -> autoGenerateToaPopup());

		pnlToaHeader.add(lblToa);
		pnlToaHeader.add(cbKho);
		pnlToaHeader.add(btnIn);
		pnlToaHeader.add(btnOut);
		pnlToaHeader.add(new JLabel("|"));
		pnlToaHeader.add(btnLen);
		pnlToaHeader.add(btnXuong);
		pnlToaHeader.add(new JLabel("|"));
		pnlToaHeader.add(btnAuto);

		modToa = new DefaultTableModel(
				new String[] { "Vị trí (#)", "Mã Toa", "Tên Toa (Loại)", "Loại Toa", "Sức chứa" }, 0);
		tblToa = buildTable(modToa);
		tblToa.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && tblToa.getSelectedRow() >= 0) {
				int thuTu = (int) modToa.getValueAt(tblToa.getSelectedRow(), 0);
				String maToa = modToa.getValueAt(tblToa.getSelectedRow(), 1).toString();
				String tenToa = modToa.getValueAt(tblToa.getSelectedRow(), 2).toString();
				currentMaToa = maToa;
				generateSeatMap(maToa, tenToa, thuTu);
			}
		});

		pnlRight.add(pnlToaHeader, BorderLayout.NORTH);
		pnlRight.add(new JScrollPane(tblToa), BorderLayout.CENTER);
		splitTop.setLeftComponent(pnlLeft);
		splitTop.setRightComponent(pnlRight);

		// ================= SƠ ĐỒ TRỰC QUAN VÀ HÌNH TÀU =================
		JPanel pnlBottom = new JPanel(new BorderLayout(0, 0));
		pnlBottom.setBackground(Color.WHITE);
		pnlBottom.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(10, 16, 12, 16)));

		// 3.1 Tiêu đề Sơ đồ
		JPanel hdrMap = new JPanel(new BorderLayout());
		hdrMap.setOpaque(false);
		JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		titleRow.setOpaque(false);
		lblMapTitle = new JLabel("3. SƠ ĐỒ GHẾ - Chọn Toa ở bảng Lắp ráp để xem");
		lblMapTitle.setFont(F_HEADER);
		lblMapTitle.setForeground(ACCENT);
		lblSeatStats = new JLabel("");
		lblSeatStats.setFont(F_SMALL);
		lblSeatStats.setForeground(C_GRAY);
		titleRow.add(lblMapTitle);
		titleRow.add(lblSeatStats);

		JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
		legend.setOpaque(false);
		legend.add(legendItem(GHE_TRONG, "Còn trống"));
		legend.add(legendItem(GHE_DAT, "Đã đặt/Bán"));
		hdrMap.add(titleRow, BorderLayout.WEST);
		hdrMap.add(legend, BorderLayout.EAST);

		// 3.2 Thanh Đoàn Tàu Đồ Họa (Graphics2D)
		pnlTrainBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 8));
		pnlTrainBar.setBackground(Color.WHITE);
		JScrollPane scrollTrain = new JScrollPane(pnlTrainBar);
		scrollTrain.setBorder(
				BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 1, 0, C_BORDER), new EmptyBorder(0, 0, 0, 0)));
		scrollTrain.setPreferredSize(new Dimension(0, 80));
		scrollTrain.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

		// 3.3 Sơ đồ ghế GridBagLayout
		pnlMap = new JPanel(new BorderLayout());
		pnlMap.setBackground(Color.WHITE);

		// Bọc ScrollTrain và Map lại chung
		JPanel mapWrapper = new JPanel(new BorderLayout(0, 8));
		mapWrapper.setOpaque(false);
		mapWrapper.add(scrollTrain, BorderLayout.NORTH);
		mapWrapper.add(new JScrollPane(pnlMap), BorderLayout.CENTER);

		pnlBottom.add(hdrMap, BorderLayout.NORTH);
		pnlBottom.add(mapWrapper, BorderLayout.CENTER);

		splitMain.setTopComponent(splitTop);
		splitMain.setBottomComponent(pnlBottom);
		pnlMainView.add(pnlHeader, BorderLayout.NORTH);
		pnlMainView.add(splitMain, BorderLayout.CENTER);

		pnlMainView.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				loadDsTau();
				loadKho();
			}
		});
		loadDsTau();
		loadKho();
		return pnlMainView;
	}

	private void ganToa() {
		if (currentTau == null) {
			JOptionPane.showMessageDialog(this, "Chọn tàu bên trái trước!");
			return;
		}
		if (modToa.getRowCount() >= quyDinhSoToa) {
			JOptionPane.showMessageDialog(this, "Tàu này đã gắn đủ " + quyDinhSoToa + " toa!");
			return;
		}
		if (cbKho.getSelectedItem() != null) {
			String maToa = cbKho.getSelectedItem().toString().split(" - ")[0].trim();
			if (daoCT.ganToaVaoTau(currentTau, maToa, modToa.getRowCount() + 1)) {
				loadToaOfTau();
				loadKho();
			}
		}
	}

	private void goToa() {
		int row = tblToa.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "Chọn một toa trong bảng để gỡ!", "Thông báo",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		String maToa = modToa.getValueAt(row, 1).toString();
		String tenToa = modToa.getValueAt(row, 2).toString();

		String[] options = { "Về kho (Sẵn sàng)", "Đem đi Bảo trì", "Hủy thao tác" };
		int choice = JOptionPane.showOptionDialog(this,
				"Gỡ toa \"" + tenToa + "\" khỏi tàu " + currentTau
						+ "?\n\nVui lòng chọn trạng thái tiếp theo của toa này:",
				"Xác nhận gỡ toa", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (choice == 2 || choice == JOptionPane.CLOSED_OPTION)
			return;

		if (!daoCT.goToaKhoiTau(currentTau, maToa)) {
			JOptionPane.showMessageDialog(this, "Không thể gỡ toa này!\nCó thể toa đang trong lịch trình bán vé.",
					"Thông báo", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String status = (choice == 1) ? "BAO_TRI" : "SAN_SANG";
		daoToa.updateTrangThai(maToa, status);

		List<String> remaining = new ArrayList<>();
		for (int i = 0; i < modToa.getRowCount(); i++)
			if (i != row)
				remaining.add(modToa.getValueAt(i, 1).toString());
		daoCT.capNhatThuTuSauKhiGo(currentTau, remaining);

		loadToaOfTau();
		loadKho();
		clearSeatMap();
	}

	private void doDoiChoToa(int delta) {
		int row = tblToa.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "Chọn một toa để di chuyển!", "Thông báo", JOptionPane.WARNING_MESSAGE);
			return;
		}
		int newRow = row + delta;
		if (newRow < 0 || newRow >= modToa.getRowCount())
			return;
		String maA = modToa.getValueAt(row, 1).toString();
		String maB = modToa.getValueAt(newRow, 1).toString();

		daoCT.hoanDoiThuTu(currentTau, maA, newRow + 1, maB, row + 1);
		loadToaOfTau();
		tblToa.setRowSelectionInterval(newRow, newRow);
	}

	private void autoGenerateToaPopup() {
		if (currentTau == null) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 tàu bên trái trước!");
			return;
		}
		if (modToa.getRowCount() > 0) {
			JOptionPane.showMessageDialog(this, "Chỉ được sinh tự động khi Tàu chưa có toa nào!");
			return;
		}

		List<LoaiToa> dsLoai = new DAO_LoaiToa().getAllLoaiToa();
		if (dsLoai.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Chưa có Khuôn mẫu Loại Toa nào trong hệ thống!");
			return;
		}

		List<LoaiToaWrapper> wrapperList = new ArrayList<>();
		LoaiToaWrapper defCung = null, defMem = null, defNam = null;
		for (LoaiToa lt : dsLoai) {
			LoaiToaWrapper w = new LoaiToaWrapper(lt, lt.getSoHang() * lt.getSoCot());
			wrapperList.add(w);
			if (defCung == null && lt.getTenLoaiToa().toLowerCase().contains("cứng"))
				defCung = w;
			if (defMem == null && lt.getTenLoaiToa().toLowerCase().contains("mềm"))
				defMem = w;
			if (defNam == null && lt.getTenLoaiToa().toLowerCase().contains("nằm"))
				defNam = w;
		}
		if (defCung == null)
			defCung = wrapperList.get(0);
		if (defMem == null)
			defMem = wrapperList.get(0);
		if (defNam == null)
			defNam = wrapperList.get(0);

		JDialog d = new JDialog(JOptionPane.getFrameForComponent(this),
				"Cấu hình " + quyDinhSoToa + " Toa Tự Động cho " + currentTau, true);
		d.setSize(500, 600);
		d.setLocationRelativeTo(this);
		JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
		pnlMain.setBorder(new EmptyBorder(10, 10, 10, 10));
		JLabel lblTop = new JLabel("Hãy chọn loại khuôn mẫu cho từng toa:", SwingConstants.CENTER);
		lblTop.setFont(new Font("Segoe UI", Font.BOLD, 16));
		pnlMain.add(lblTop, BorderLayout.NORTH);

		JPanel pnlList = new JPanel(new GridLayout(quyDinhSoToa, 2, 10, 10));
		JComboBox<LoaiToaWrapper>[] combos = new JComboBox[quyDinhSoToa];
		int p1 = quyDinhSoToa / 3;
		int p2 = quyDinhSoToa / 3;

		for (int i = 0; i < quyDinhSoToa; i++) {
			JLabel lblToa = new JLabel(" Toa Nối Thứ " + (i + 1) + ": ");
			lblToa.setFont(F_BODY);
			JComboBox<LoaiToaWrapper> cb = new JComboBox<>();
			for (LoaiToaWrapper w : wrapperList)
				cb.addItem(w);
			if (i < p1)
				cb.setSelectedItem(defCung);
			else if (i < p1 + p2)
				cb.setSelectedItem(defMem);
			else
				cb.setSelectedItem(defNam);
			combos[i] = cb;
			pnlList.add(lblToa);
			pnlList.add(cb);
		}

		JScrollPane scroll = new JScrollPane(pnlList);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		pnlMain.add(scroll, BorderLayout.CENTER);

		JButton btnXacNhan = makeBtn("Xác nhận Sinh Toa", C_SUCCESS);
		btnXacNhan.setPreferredSize(new Dimension(200, 36));
		btnXacNhan.addActionListener(e -> {
			List<Object[]> toaList = new ArrayList<>();
			for (int i = 0; i < quyDinhSoToa; i++) {
				int toaNum = i + 1;
				LoaiToaWrapper chon = (LoaiToaWrapper) combos[i].getSelectedItem();
				String tenLoai = chon.lt.getTenLoaiToa().toLowerCase();
				String tenToaChuyenDung = "Toa ghế ngồi cứng";
				if (tenLoai.contains("nằm"))
					tenToaChuyenDung = "Toa giường nằm điều hòa";
				else if (tenLoai.contains("mềm"))
					tenToaChuyenDung = "Toa ghế ngồi mềm chất lượng cao";

				toaList.add(new Object[] { chon.ghe, chon.lt.getMaLoaiToa(), toaNum, tenToaChuyenDung });
			}
			if (daoCT.autoSinhToaTransaction(currentTau, toaList)) {
				JOptionPane.showMessageDialog(d, "Đã sản xuất và lắp ráp thành công " + quyDinhSoToa + " toa!",
						"Thành công", JOptionPane.INFORMATION_MESSAGE);
				d.dispose();
				loadToaOfTau();
				loadKho();
			} else {
				JOptionPane.showMessageDialog(d, "Có lỗi xảy ra khi tạo toa tự động!", "Lỗi",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		JPanel pnlBot = new JPanel();
		pnlBot.add(btnXacNhan);
		pnlMain.add(pnlBot, BorderLayout.SOUTH);
		d.add(pnlMain);
		d.setVisible(true);
	}

	private void generateSeatMap(String maToa, String tenToa, int thuTu) {
		pnlMap.removeAll();
		lblMapTitle.setText("3. SƠ ĐỒ GHẾ CHIỀU NGANG: Toa Số " + thuTu + " - " + tenToa + " (" + maToa + ")");
		Set<String> bookedSeats = daoCT.getGheDaDat(maToa);
		Object[] thongTin = daoToa.getThongTinToaForMap(maToa);

		if (thongTin != null) {
			int soHang = (int) thongTin[0];
			int soCot = (int) thongTin[1];
			String kieu = (String) thongTin[2];
			int tongGhe = (int) thongTin[3];
			int soTrong = tongGhe - bookedSeats.size();
			lblSeatStats.setText(
					"   Còn trống: " + soTrong + "   Đã đặt: " + bookedSeats.size() + "   Tổng: " + tongGhe + " ghế");
			JPanel seatPanel = "GIUONG".equalsIgnoreCase(kieu) ? drawSleeperHorizontal(soHang, soCot, bookedSeats)
					: drawSeaterHorizontal(soHang, soCot, bookedSeats);
			pnlMap.add(seatPanel, BorderLayout.CENTER);
		}
		pnlMap.revalidate();
		pnlMap.repaint();
		refreshTrainBar();
	}

	private void clearSeatMap() {
		pnlMap.removeAll();
		lblMapTitle.setText("3. SƠ ĐỒ GHẾ CHIỀU NGANG - Chọn Toa ở bảng Lắp ráp để xem");
		lblSeatStats.setText("");
		currentMaToa = null;
		pnlMap.revalidate();
		pnlMap.repaint();
		refreshTrainBar();
	}

	// ================== THANH HIỂN THỊ ĐOÀN TÀU (GRAPHICS 2D) ==================
	private void refreshTrainBar() {
		pnlTrainBar.removeAll();
		if (currentTau == null || modToa.getRowCount() == 0) {
			JLabel lbl = new JLabel("  Vui lòng gắn toa để hiển thị hình ảnh đoàn tàu");
			lbl.setFont(F_BODY);
			lbl.setForeground(C_GRAY);
			pnlTrainBar.add(lbl);
		} else {
			// 1. Vẽ Đầu kéo tàu bên phải
			pnlTrainBar.add(new TrainCarPanel(true, false, currentTau, new Color(41, 128, 185)));

			// 2. Vẽ các toa xếp nối tiếp nhau (từ 1 đến N)
			for (int i = 0; i < modToa.getRowCount(); i++) {
				int thuTu = (int) modToa.getValueAt(i, 0);
				String ma = modToa.getValueAt(i, 1).toString();
				String loai = modToa.getValueAt(i, 3).toString();

				boolean isSelected = ma.equals(currentMaToa);
				Color carColor = new Color(93, 173, 226); // Màu Toa Cứng (Xanh dương)
				if (loai.toLowerCase().contains("mềm"))
					carColor = new Color(231, 76, 60); // Đỏ cam
				if (loai.toLowerCase().contains("nằm"))
					carColor = new Color(162, 217, 40); // Xanh lá

				TrainCarPanel carPanel = new TrainCarPanel(false, isSelected, String.valueOf(thuTu), carColor);
				carPanel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						for (int r = 0; r < modToa.getRowCount(); r++) {
							if (modToa.getValueAt(r, 1).toString().equals(ma)) {
								tblToa.setRowSelectionInterval(r, r);
								tblToa.scrollRectToVisible(tblToa.getCellRect(r, 0, true));
								break;
							}
						}
					}
				});
				pnlTrainBar.add(carPanel);
			}
		}
		pnlTrainBar.revalidate();
		pnlTrainBar.repaint();
	}

	// LỚP VẼ HÌNH TOA TÀU SIÊU ĐẸP (Không cần thư viện ảnh)
	class TrainCarPanel extends JPanel {
		private boolean isLocomotive, isSelected;
		private String labelText;
		private Color carColor;

		public TrainCarPanel(boolean isLocomotive, boolean isSelected, String labelText, Color carColor) {
			this.isLocomotive = isLocomotive;
			this.isSelected = isSelected;
			this.labelText = labelText;
			this.carColor = carColor;
			setPreferredSize(new Dimension(80, 60));
			setOpaque(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth() - 8;
			int h = 32;
			int y = 2; // Khung thân tàu

			// Vẽ bánh xe và trục liên kết
			g2.setColor(new Color(105, 110, 115));
			g2.fillOval(10, y + h - 4, 12, 12);
			g2.fillOval(w - 22, y + h - 4, 12, 12); // 2 bánh xe
			g2.setStroke(new BasicStroke(4f));
			g2.drawLine(15, y + h + 2, w - 15, y + h + 2); // Trục nối
			if (!isLocomotive) {
				g2.fillRect(w, y + h - 10, 8, 4);
			} // Khớp nối toa

			// Vẽ Thân tàu
			g2.setColor(isSelected ? new Color(241, 196, 15) : carColor);
			if (isLocomotive) {
				int[] px = { 0, w - 15, w, w, 0 };
				int[] py = { y, y, y + 15, y + h, y + h };
				g2.fillPolygon(px, py, 5); // Hình dáng đầu tàu Bullet Train
				g2.setColor(Color.WHITE);
				g2.fillRect(w - 28, y + 5, 10, 12);
				g2.fillRect(w - 14, y + 8, 8, 9); // Cửa kính buồng lái
			} else {
				g2.fillRoundRect(0, y, w, h, 10, 10); // Bo góc toa
				g2.setColor(Color.WHITE);
				int winW = (w - 25) / 3;
				g2.fillRect(6, y + 8, winW, 10);
				g2.fillRect(12 + winW, y + 8, winW, 10);
				g2.fillRect(18 + winW * 2, y + 8, winW, 10); // 3 Cửa sổ
				g2.setColor(new Color(0, 0, 0, 40));
				g2.fillRect(0, y + h - 8, w, 4); // Vạch kẻ thân toa
			}

			// Vẽ Chữ (Toa 1, Toa 2...)
			g2.setColor(isSelected ? new Color(211, 84, 0) : ACCENT);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
			FontMetrics fm = g2.getFontMetrics();
			int tx = (getWidth() - fm.stringWidth(labelText)) / 2;
			g2.drawString(labelText, tx, y + h + 22);
			g2.dispose();
		}
	}

	// ================= CÁC HÀM VẼ SƠ ĐỒ MÀU SẮC AUTO-SCALING =================
	private JPanel drawSeaterHorizontal(int soHang, int soCot, Set<String> booked) {
		JPanel outer = new JPanel(new BorderLayout(16, 0));
		outer.setBackground(new Color(0xFAFCFF));
		outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(52, 73, 94), 2, true),
				new EmptyBorder(12, 12, 12, 12)));
		JLabel capA = new JLabel(" ĐẦU TÀU ");
		capA.setFont(F_HEADER);
		capA.setForeground(C_GRAY);
		JLabel capB = new JLabel(" CUỐI TÀU");
		capB.setFont(F_HEADER);
		capB.setForeground(C_GRAY);
		int uiRows = soCot;
		int uiCols = soHang;
		int halfRows = Math.max(1, uiRows / 2);
		JPanel gridBody = new JPanel(new GridLayout(uiRows + 1, uiCols, 4, 4));
		gridBody.setOpaque(false);

		for (int r = 0; r < uiRows + 1; r++) {
			if (r == halfRows) {
				for (int c = 0; c < uiCols; c++) {
					JPanel aisle = new JPanel();
					aisle.setBackground(new Color(0xDDE6F5));
					gridBody.add(aisle);
				}
			} else {
				int actualRow = r > halfRows ? r - 1 : r;
				for (int c = 0; c < uiCols; c++) {
					int seatNum = (c * uiRows) + actualRow + 1;
					gridBody.add(seatBtn(seatNum, booked));
				}
			}
		}
		JPanel wrapper = new JPanel(new GridBagLayout());
		wrapper.setOpaque(false);
		wrapper.add(gridBody);
		outer.add(capA, BorderLayout.WEST);
		outer.add(wrapper, BorderLayout.CENTER);
		outer.add(capB, BorderLayout.EAST);
		return outer;
	}

	private JPanel drawSleeperHorizontal(int soHang, int soCot, Set<String> booked) {
		int khoang = Math.max(1, (soHang * soCot) / 4);
		JPanel outer = new JPanel(new BorderLayout(16, 0));
		outer.setBackground(new Color(0xFAFCFF));
		outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(52, 73, 94), 2, true),
				new EmptyBorder(12, 12, 12, 12)));
		JLabel capA = new JLabel(" ĐẦU TÀU ");
		capA.setFont(F_HEADER);
		capA.setForeground(C_GRAY);
		JLabel capB = new JLabel(" CUỐI TÀU");
		capB.setFont(F_HEADER);
		capB.setForeground(C_GRAY);
		JPanel gridBody = new JPanel(new GridLayout(1, khoang, 8, 0));
		gridBody.setOpaque(false);
		int idx = 1;
		for (int k = 1; k <= khoang; k++) {
			JPanel kp = new JPanel(new BorderLayout(0, 4));
			kp.setBackground(new Color(0xF0F4FA));
			kp.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xDDE6F5), 1, true),
					new EmptyBorder(6, 6, 6, 6)));
			JLabel lbl = new JLabel("Khoang " + k, SwingConstants.CENTER);
			lbl.setFont(F_SMALL);
			lbl.setForeground(C_GRAY);
			kp.add(lbl, BorderLayout.NORTH);
			JPanel grid = new JPanel(new GridLayout(2, 2, 4, 4));
			grid.setOpaque(false);
			grid.add(seatBtn(idx + 2, booked));
			grid.add(seatBtn(idx + 3, booked));
			grid.add(seatBtn(idx, booked));
			grid.add(seatBtn(idx + 1, booked));
			idx += 4;
			kp.add(grid, BorderLayout.CENTER);
			gridBody.add(kp);
		}
		JPanel wrapper = new JPanel(new GridBagLayout());
		wrapper.setOpaque(false);
		wrapper.add(gridBody);
		outer.add(capA, BorderLayout.WEST);
		outer.add(wrapper, BorderLayout.CENTER);
		outer.add(capB, BorderLayout.EAST);
		return outer;
	}

	private JButton seatBtn(int num, Set<String> booked) {
		boolean isDat = booked.contains(String.valueOf(num));
		JButton b = new JButton(String.valueOf(num));
		b.setPreferredSize(new Dimension(36, 32));
		b.setFont(F_SEAT);
		b.setMargin(new Insets(0, 0, 0, 0));
		b.setFocusPainted(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		b.setBackground(isDat ? GHE_DAT : GHE_TRONG);
		b.setForeground(Color.WHITE);
		b.setOpaque(true);
		b.setBorder(BorderFactory.createLineBorder(isDat ? GHE_DAT.darker() : GHE_TRONG.darker(), 1));
		b.setToolTipText("Ghế số " + num + (isDat ? " - ĐÃ BÁN" : " - CÒN TRỐNG"));
		b.addActionListener(e -> JOptionPane.showMessageDialog(this,
				isDat ? "Ghế số " + num + " ĐÃ CÓ NGƯỜI ĐẶT MUA!" : "Ghế số " + num + " đang TRỐNG.", "Thông tin ghế",
				isDat ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE));
		b.addMouseListener(new MouseAdapter() {
			Color orig = isDat ? GHE_DAT : GHE_TRONG;

			@Override
			public void mouseEntered(MouseEvent e) {
				b.setBackground(GHE_CHON);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				b.setBackground(orig);
			}
		});
		return b;
	}

	private void loadDsTau() {
		modTau.setRowCount(0);
		for (Tau t : daoTau.getAllTau())
			modTau.addRow(new Object[] { t.getMaTau(), t.getTenTau(), t.getSoToa() + " toa" });
	}

	private void loadToaOfTau() {
		modToa.setRowCount(0);
		if (currentTau == null)
			return;
		for (Object[] obj : daoCT.getToaOfTau(currentTau)) {
			modToa.addRow(new Object[] { obj[0], obj[1], obj[2], obj[3], obj[4] + " ghế" });
		}
		refreshTrainBar();
	}

	private void loadKho() {
		cbKho.removeAllItems();
		for (Object[] obj : daoToa.getToaTrongKhoSanSang())
			cbKho.addItem(obj[0] + " - " + obj[1] + " [" + obj[3] + " - " + obj[2] + " chỗ]");
	}

	private JPanel legendItem(Color color, String label) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		p.setOpaque(false);
		JLabel ic = new JLabel("  ");
		ic.setBackground(color);
		ic.setOpaque(true);
		ic.setPreferredSize(new Dimension(14, 14));
		JLabel tx = new JLabel(label);
		tx.setFont(F_SMALL);
		tx.setForeground(C_GRAY);
		p.add(ic);
		p.add(tx);
		return p;
	}

	private JButton makeBtn(String t, Color bg) {
		JButton b = new JButton(t) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isRollover() ? bg.darker() : bg);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		b.setForeground(Color.WHITE);
		b.setFont(new Font("Segoe UI", Font.BOLD, 12));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JButton makeArrowBtn(boolean isUp, Color bg) {
		JButton b = new JButton() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isRollover() ? bg.darker() : bg);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(Color.WHITE);
				int cx = getWidth() / 2;
				int cy = getHeight() / 2;
				int[] px = { cx - 5, cx + 5, cx };
				int[] py = isUp ? new int[] { cy + 3, cy + 3, cy - 5 } : new int[] { cy - 3, cy - 3, cy + 5 };
				g2.fillPolygon(px, py, 3);
				g2.dispose();
			}
		};
		b.setPreferredSize(new Dimension(42, 32));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JTable buildTable(DefaultTableModel m) {
		JTable t = new JTable(m) {
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		t.setRowHeight(35);
		t.setFont(F_BODY);
		t.setSelectionBackground(ACCENT_LIGHT);
		t.getTableHeader().setPreferredSize(new Dimension(0, 40));
		t.getTableHeader().setBackground(ACCENT);
		t.getTableHeader().setForeground(Color.WHITE);
		t.getTableHeader().setFont(F_HEADER);
		return t;
	}

	private static class ShadowBorder extends AbstractBorder {
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(C_BORDER);
			g2.drawRoundRect(x, y, w - 1, h - 1, 10, 10);
			g2.dispose();
		}

		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(1, 1, 1, 1);
		}
	}
}