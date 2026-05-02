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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.connectDB.ConnectDB;

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
	private JPanel pnlTrainBar;
	private JLabel lblMapTitle, lblSeatStats;

	private String currentTau = null;
	private String currentMaToa = null;
	private String currentTenToa = null;
	private int currentThuTu = 0;
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

		JButton btnAddTau = makeBtn("Thêm Tàu", ACCENT);
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
		// --- SỰ KIỆN CLICK BẢNG TÀU ---
		tblTau.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int r = tblTau.getSelectedRow();
				if (r >= 0) {
					currentTau = modTau.getValueAt(r, 0).toString();
					quyDinhSoToa = Integer.parseInt(modTau.getValueAt(r, 2).toString().split(" ")[0]);
					loadToaOfTau();
					clearSeatMap();

					// DOUBLE CLICK ĐỂ SỬA
					if (e.getClickCount() == 2) {
						Tau tDao = daoTau.getTauByMa(currentTau);
						Form_Tau f = new Form_Tau(JOptionPane.getFrameForComponent(TAB_QuanLyDoanTau.this),
								"Cập Nhật Tàu");
						f.setEntity(tDao);
						f.setVisible(true);

						if (f.isConfirmed()) {
							Tau tUpdate = f.getEntity();

							// 1. Ràng buộc: Không được giảm số toa quy định nhỏ hơn số toa đang gắn
							if (tUpdate.getSoToa() < modToa.getRowCount()) {
								JOptionPane.showMessageDialog(TAB_QuanLyDoanTau.this,
										"⚠️ LỖI RÀNG BUỘC: Không thể giảm quy định xuống " + tUpdate.getSoToa()
												+ " toa vì tàu đang gắn " + modToa.getRowCount() + " toa thực tế.\n"
												+ "Vui lòng gỡ bớt toa ra khỏi tàu trước!",
										"Lỗi", JOptionPane.ERROR_MESSAGE);
								return;
							}

							// 2. Ràng buộc: Thay đổi trạng thái
							String oldStatus = tDao.getTrangThaiTau().name();
							String newStatus = tUpdate.getTrangThaiTau().name();

							if (oldStatus.equals("HOATDONG") && !newStatus.equals("HOATDONG")) {
								// Kiểm tra Lịch trình tương lai
								if (isTauCoLichTrinhTuongLai(tUpdate.getMaTau())) {
									JOptionPane.showMessageDialog(TAB_QuanLyDoanTau.this,
											"⚠️ TỪ CHỐI CẬP NHẬT TRẠNG THÁI!\n\n" + "Tàu [" + tUpdate.getTenTau()
													+ "] đang được phân công chạy các lịch trình trong tương lai.\n"
													+ "Không thể cho tàu đi bảo trì hoặc ngưng hoạt động lúc này.",
											"Lỗi Ràng Buộc Hệ Thống", JOptionPane.ERROR_MESSAGE);
									return;
								}

								// Nếu không có lịch trình, nhưng đang gắn toa -> Hỏi gỡ toa
								if (modToa.getRowCount() > 0) {
									int ans = JOptionPane.showConfirmDialog(TAB_QuanLyDoanTau.this,
											"Chuyển tàu sang trạng thái Nghỉ/Bảo trì yêu cầu phải tháo dỡ toàn bộ Toa.\n"
													+ "Bạn có đồng ý để hệ thống tự động gỡ " + modToa.getRowCount()
													+ " Toa này về Kho (Sẵn sàng) không?",
											"Xác nhận tháo dỡ Toa", JOptionPane.YES_NO_OPTION,
											JOptionPane.QUESTION_MESSAGE);

									if (ans == JOptionPane.YES_OPTION) {
										goToanBoToaVeKho(tUpdate.getMaTau());
									} else {
										JOptionPane.showMessageDialog(TAB_QuanLyDoanTau.this,
												"Cập nhật trạng thái bị hủy.", "Thông báo",
												JOptionPane.INFORMATION_MESSAGE);
										return; // Hủy lưu
									}
								}
							}

							// Nếu mọi thứ hợp lệ -> Lưu
							if (daoTau.updateTau(tUpdate)) {
								loadDsTau();
								loadToaOfTau();
								loadKho();
								clearSeatMap();
								JOptionPane.showMessageDialog(TAB_QuanLyDoanTau.this, "Cập nhật Tàu thành công!");
							}
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

		modToa = new DefaultTableModel(new String[] { "Vị trí", "Mã Toa", "Tên Toa", "Loại Toa", "Sức chứa" }, 0);
		tblToa = buildTable(modToa);
		tblToa.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && tblToa.getSelectedRow() >= 0) {
				currentThuTu = (int) modToa.getValueAt(tblToa.getSelectedRow(), 0);
				currentMaToa = modToa.getValueAt(tblToa.getSelectedRow(), 1).toString();
				currentTenToa = modToa.getValueAt(tblToa.getSelectedRow(), 2).toString();
				generateSeatMap(currentMaToa, currentTenToa, currentThuTu);
			}
		});

		pnlRight.add(pnlToaHeader, BorderLayout.NORTH);
		pnlRight.add(new JScrollPane(tblToa), BorderLayout.CENTER);
		splitTop.setLeftComponent(pnlLeft);
		splitTop.setRightComponent(pnlRight);

		// ================= SƠ ĐỒ TRỰC QUAN =================
		JPanel pnlBottom = new JPanel(new BorderLayout(0, 0));
		pnlBottom.setBackground(Color.WHITE);
		pnlBottom.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(10, 16, 12, 16)));

		JPanel hdrMap = new JPanel(new BorderLayout());
		hdrMap.setOpaque(false);
		JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		titleRow.setOpaque(false);
		lblMapTitle = new JLabel("3. SƠ ĐỒ GHẾ VẬT LÝ - Chọn Toa ở bảng Lắp ráp để xem");
		lblMapTitle.setFont(F_HEADER);
		lblMapTitle.setForeground(ACCENT);
		lblSeatStats = new JLabel("");
		lblSeatStats.setFont(F_SMALL);
		lblSeatStats.setForeground(C_GRAY);
		titleRow.add(lblMapTitle);
		titleRow.add(lblSeatStats);

		JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
		legend.setOpaque(false);
		legend.add(legendItem(new Color(0x2ECC71), "Đang hoạt động")); // Xanh lá
		legend.add(legendItem(Color.GRAY, "Bảo trì")); // Xám
		hdrMap.add(titleRow, BorderLayout.WEST);
		//hdrMap.add(legend, BorderLayout.EAST);

		pnlTrainBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 8));
		pnlTrainBar.setBackground(Color.WHITE);
		JScrollPane scrollTrain = new JScrollPane(pnlTrainBar);
		scrollTrain.setBorder(
				BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 1, 0, C_BORDER), new EmptyBorder(0, 0, 0, 0)));
		scrollTrain.setPreferredSize(new Dimension(0, 55));
		scrollTrain.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

		pnlMap = new JPanel(new BorderLayout());
		pnlMap.setBackground(Color.WHITE);

		JPanel mapWrapper = new JPanel(new BorderLayout(0, 8));
		mapWrapper.setOpaque(false);
		mapWrapper.add(scrollTrain, BorderLayout.NORTH);
		mapWrapper.add(new JScrollPane(pnlMap), BorderLayout.CENTER);

		pnlBottom.add(hdrMap, BorderLayout.NORTH);
		mapWrapper.add(legend, BorderLayout.SOUTH);
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

	// ====================================================================
	// HÀM KIỂM TRA TÀU HOẠT ĐỘNG TRƯỚC KHI CHO LẮP RÁP (VÁ LỖI)
	// ====================================================================
	private boolean checkTauHoatDong() {
		if (currentTau == null)
			return false;
		Tau t = daoTau.getTauByMa(currentTau);
		if (t != null && !t.getTrangThaiTau().name().equals("HOATDONG")) {
			JOptionPane.showMessageDialog(this,
					"Tàu đang trong trạng thái Bảo trì hoặc Ngưng hoạt động!\nKhông thể thực hiện thao tác lắp ráp hay thay đổi cấu trúc toa lúc này.",
					"Ràng Buộc Nghiệp Vụ", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return true;
	}

	// ====================================================================
	// KIỂM TRA XEM TÀU CÓ ĐANG DÍNH LỊCH TRÌNH TRONG TƯƠNG LAI KHÔNG
	// ====================================================================
	private boolean isTauCoLichTrinhTuongLai(String maTau) {
		String sql = "SELECT COUNT(*) FROM LichTrinh lt JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen "
				+ "WHERE ct.maTau = ? AND (lt.ngayKhoiHanh > CAST(GETDATE() AS DATE) "
				+ "OR (lt.ngayKhoiHanh = CAST(GETDATE() AS DATE) AND lt.gioKhoiHanh > CAST(GETDATE() AS TIME)))";
		try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, maTau);
			ResultSet rs = ps.executeQuery();
			if (rs.next() && rs.getInt(1) > 0)
				return true; // Có lịch trình -> Không an toàn
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// ====================================================================
	// HÀM KIỂM TRA LỊCH TRÌNH TƯƠNG LAI (CHECK NGẦM)
	// ====================================================================
	private boolean isSeatBookedInFuture(String maToa, String viTri) {
		String sql = "SELECT COUNT(*) FROM GheLichTrinh gl JOIN LichTrinh lt ON gl.maLT = lt.maLT "
				+ "WHERE gl.maToa = ? AND gl.viTri = ? AND gl.trangThai IN ('DADAT', 'GIUCHO') "
				+ "AND (lt.ngayKhoiHanh > CAST(GETDATE() AS DATE) OR (lt.ngayKhoiHanh = CAST(GETDATE() AS DATE) AND lt.gioKhoiHanh > CAST(GETDATE() AS TIME)))";
		try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, maToa);
			ps.setString(2, viTri);
			ResultSet rs = ps.executeQuery();
			if (rs.next() && rs.getInt(1) > 0)
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void goToanBoToaVeKho(String maTau) {
		List<Object[]> attachedToa = daoCT.getToaOfTau(maTau);
		for (Object[] obj : attachedToa) {
			String maToa = obj[1].toString();
			daoCT.goToaKhoiTau(maTau, maToa);
			daoToa.updateTrangThai(maToa, "SAN_SANG");
		}
	}

	// ====================================================================
	// VẼ SƠ ĐỒ VẬT LÝ VÀ SỰ KIỆN CLICK GHẾ BẢO TRÌ
	// ====================================================================
	private void generateSeatMap(String maToa, String tenToa, int thuTu) {
		pnlMap.removeAll();
		lblMapTitle.setText("3. SƠ ĐỒ GHẾ VẬT LÝ: Toa Số " + thuTu + " - " + tenToa + " (" + maToa + ")");

		Set<String> maintenanceSeats = daoToa.getGheBaoTri(maToa);
		Object[] thongTin = daoToa.getThongTinToaForMap(maToa);

		if (thongTin != null) {
			int soHang = (int) thongTin[0];
			int soCot = (int) thongTin[1];
			String kieu = (String) thongTin[2];
			int tongGhe = (int) thongTin[3];

			lblSeatStats
					.setText("   Tổng sức chứa: " + tongGhe + " ghế   |   Đang bảo trì: " + maintenanceSeats.size());

			JPanel seatPanel = "GIUONG".equalsIgnoreCase(kieu)
					? drawSleeperHorizontal(soHang, soCot, maintenanceSeats, maToa)
					: drawSeaterHorizontal(soHang, soCot, maintenanceSeats, maToa);
			pnlMap.add(seatPanel, BorderLayout.CENTER);
		}
		pnlMap.revalidate();
		pnlMap.repaint();
		refreshTrainBar();
	}

	private JButton seatBtn(int num, Set<String> maintenanceSeats, String maToa) {
		String viTri = String.valueOf(num);
		boolean isBaoTri = maintenanceSeats.contains(viTri);

		JButton b = new JButton(viTri);
		b.setPreferredSize(new Dimension(32, 28));
		b.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Giảm nhẹ font
		b.setMargin(new Insets(0, 0, 0, 0));
		b.setFocusPainted(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		Color C_TRONG = new Color(0x2ECC71); // Xanh lá
		Color C_BAOTRI = Color.GRAY; // Xám

		b.setBackground(isBaoTri ? C_BAOTRI : C_TRONG);
		b.setForeground(Color.WHITE);
		b.setOpaque(true);
		b.setBorder(BorderFactory.createLineBorder((isBaoTri ? C_BAOTRI : C_TRONG).darker(), 1));
		b.setToolTipText("Ghế số " + num + (isBaoTri ? " - ĐANG BẢO TRÌ" : " - HOẠT ĐỘNG"));

		// LOGIC HIDDEN VALIDATION KHI CLICK
		b.addActionListener(e -> {
			if (isBaoTri) {
				int ans = JOptionPane.showConfirmDialog(this, "Mở khóa bảo trì cho ghế số " + num + "?", "Mở khóa ghế",
						JOptionPane.YES_NO_OPTION);
				if (ans == JOptionPane.YES_OPTION) {
					daoToa.removeGheBaoTri(maToa, viTri);
					generateSeatMap(currentMaToa, currentTenToa, currentThuTu);
				}
			} else {
				if (isSeatBookedInFuture(maToa, viTri)) {
					JOptionPane.showMessageDialog(this,
							"⚠️ Không thể bảo trì!\n\nGhế này đã có hành khách mua vé trong các chuyến đi tương lai.\n"
									+ "Vui lòng báo bộ phận CSKH dời chỗ cho khách trước khi khóa ghế.",
							"Cảnh báo hệ thống", JOptionPane.WARNING_MESSAGE);
				} else {
					int ans = JOptionPane.showConfirmDialog(this, "Khóa ghế số " + num + " chuyển sang BẢO TRÌ?",
							"Khóa ghế", JOptionPane.YES_NO_OPTION);
					if (ans == JOptionPane.YES_OPTION) {
						daoToa.addGheBaoTri(maToa, viTri);
						generateSeatMap(currentMaToa, currentTenToa, currentThuTu);
					}
				}
			}
		});
		return b;
	}

	private JPanel drawSeaterHorizontal(int soHang, int soCot, Set<String> maintenanceSeats, String maToa) {
		JPanel outer = new JPanel(new BorderLayout(8, 0)); // Giảm padding
		outer.setBackground(new Color(0xFAFCFF));
		outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(160, 174, 192), 1, true),
				new EmptyBorder(4, 4, 4, 4))); // Ép nhỏ viền
		JLabel capA = new JLabel(" ĐẦU TÀU ");
		capA.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Nhỏ font chữ đầu đuôi
		capA.setForeground(new Color(0x7F8C8D));
		JLabel capB = new JLabel(" CUỐI TÀU");
		capB.setFont(new Font("Segoe UI", Font.BOLD, 11));
		capB.setForeground(new Color(0x7F8C8D));

		int uiRows = soCot;
		int uiCols = soHang;
		int halfRows = Math.max(1, uiRows / 2);
		JPanel gridBody = new JPanel(new GridLayout(uiRows + 1, uiCols, 2, 2));
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
					gridBody.add(seatBtn(seatNum, maintenanceSeats, maToa));
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

	private JPanel drawSleeperHorizontal(int soHang, int soCot, Set<String> maintenanceSeats, String maToa) {
		int khoang = Math.max(1, (soHang * soCot) / 4);
		JPanel outer = new JPanel(new BorderLayout(8, 0)); // Giảm padding
		outer.setBackground(new Color(0xFAFCFF));
		outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(160, 174, 192), 1, true),
				new EmptyBorder(4, 4, 4, 4))); // Ép nhỏ viền
		JLabel capA = new JLabel(" ĐẦU TÀU ");
		capA.setFont(new Font("Segoe UI", Font.BOLD, 11));
		capA.setForeground(new Color(0x7F8C8D));
		JLabel capB = new JLabel(" CUỐI TÀU");
		capB.setFont(new Font("Segoe UI", Font.BOLD, 11));
		capB.setForeground(new Color(0x7F8C8D));

		JPanel gridBody = new JPanel(new GridLayout(1, khoang, 4, 0)); // Thu hẹp khoảng cách khoang
		gridBody.setOpaque(false);
		int idx = 1;
		for (int k = 1; k <= khoang; k++) {
			JPanel kp = new JPanel(new BorderLayout(0, 2));
			kp.setBackground(new Color(0xF0F4FA));
			kp.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xDDE6F5), 1, true),
					new EmptyBorder(2, 2, 2, 2))); // Ép nhỏ viền khoang
			JLabel lbl = new JLabel("Khoang " + k, SwingConstants.CENTER);
			lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Font khoang nhỏ gọn
			lbl.setForeground(new Color(0x7F8C8D));
			kp.add(lbl, BorderLayout.NORTH);

			JPanel grid = new JPanel(new GridLayout(2, 2, 2, 2)); // Thu hẹp khoảng cách giường
			grid.setOpaque(false);
			grid.add(seatBtn(idx + 2, maintenanceSeats, maToa));
			grid.add(seatBtn(idx + 3, maintenanceSeats, maToa));
			grid.add(seatBtn(idx, maintenanceSeats, maToa));
			grid.add(seatBtn(idx + 1, maintenanceSeats, maToa));
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

	private void ganToa() {
		if (currentTau == null) {
			JOptionPane.showMessageDialog(this, "Chọn tàu bên trái trước!");
			return;
		}
		if (!checkTauHoatDong())
			return; // VÁ LỖI TẠI ĐÂY

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
		if (!checkTauHoatDong())
			return; // VÁ LỖI TẠI ĐÂY

		int row = tblToa.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "Chọn một toa trong bảng để gỡ!", "Thông báo",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		String maToa = modToa.getValueAt(row, 1).toString();
		String tenToa = modToa.getValueAt(row, 2).toString();

		if (!daoCT.checkKhongCoVeDaBan(currentTau, maToa)) {
			JOptionPane.showMessageDialog(this,
					"Không thể gỡ toa này!\nĐang có hành khách mua vé toa này trong các chuyến đi sắp tới.",
					"Thông báo", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (isTauCoLichTrinhTuongLai(currentTau)) {
			int warn = JOptionPane.showConfirmDialog(this,
					"⚠️ CẢNH BÁO NGHIỆP VỤ:\nTàu này đang có lịch trình tương lai. Việc gỡ toa sẽ làm khuyết vị trí toa trên hệ thống bán vé sắp tới.\nBạn có chắc chắn muốn gỡ, và sẽ chịu trách nhiệm lắp toa khác thay thế?",
					"Cảnh báo Lịch Trình", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (warn != JOptionPane.YES_OPTION)
				return;
		}

		String[] options = { "Về kho (Sẵn sàng)", "Đem đi Bảo trì", "Hủy thao tác" };
		int choice = JOptionPane.showOptionDialog(this,
				"Gỡ toa \"" + tenToa + "\" khỏi tàu " + currentTau
						+ "?\n\nVui lòng chọn trạng thái tiếp theo của toa này:",
				"Xác nhận gỡ toa", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (choice == 2 || choice == JOptionPane.CLOSED_OPTION)
			return;

		if (!daoCT.goToaKhoiTau(currentTau, maToa)) {
			JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi gỡ toa.", "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
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
		if (!checkTauHoatDong()) return;

		int row = tblToa.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "Chọn một toa để di chuyển!", "Thông báo", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		int newRow = row + delta;
		// Kiểm tra nếu chạm nóc (lên quá dòng 1) hoặc chạm đáy (xuống quá dòng cuối)
		if (newRow < 0 || newRow >= modToa.getRowCount()) return;

		// 1. Lấy mã của 2 toa cần đổi chỗ
		String maA = modToa.getValueAt(row, 1).toString();
		String maB = modToa.getValueAt(newRow, 1).toString();

		// 2. Cập nhật hoán đổi dưới Database
		boolean isSuccess = daoCT.hoanDoiThuTu(currentTau, maA, newRow + 1, maB, row + 1);

		if (isSuccess) {
			// 3. HOÁN ĐỔI TRỰC TIẾP TRÊN GIAO DIỆN (Không cần load lại Database)
			// Chúng ta sẽ lặp qua các cột để đổi chữ (Bỏ qua cột 0 vì cột 0 là số thứ tự 1,2,3... phải giữ nguyên)
			for (int col = 1; col < modToa.getColumnCount(); col++) {
				Object temp = modToa.getValueAt(row, col);
				modToa.setValueAt(modToa.getValueAt(newRow, col), row, col);
				modToa.setValueAt(temp, newRow, col);
			}

			// 4. Bám đuôi bôi đen theo toa vừa di chuyển
			tblToa.setRowSelectionInterval(newRow, newRow);
			
			// 5. Cập nhật lại hình ảnh đoàn tàu nhỏ bên dưới
			refreshTrainBar();
		} else {
			JOptionPane.showMessageDialog(this, "Lỗi khi đổi chỗ dưới Cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void autoGenerateToaPopup() {
		if (currentTau == null) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 tàu bên trái trước!");
			return;
		}
		if (!checkTauHoatDong())
			return; // VÁ LỖI TẠI ĐÂY

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

	private void clearSeatMap() {
		pnlMap.removeAll();
		lblMapTitle.setText("3. SƠ ĐỒ GHẾ VẬT LÝ - Chọn Toa ở bảng Lắp ráp để xem");
		lblSeatStats.setText("");
		currentMaToa = null;
		pnlMap.revalidate();
		pnlMap.repaint();
		refreshTrainBar();
	}

	private void refreshTrainBar() {
		pnlTrainBar.removeAll();
		if (currentTau == null || modToa.getRowCount() == 0) {
			JLabel lbl = new JLabel("  Vui lòng gắn toa để hiển thị hình ảnh đoàn tàu");
			lbl.setFont(F_BODY);
			lbl.setForeground(C_GRAY);
			pnlTrainBar.add(lbl);
		} else {
			pnlTrainBar.add(new TrainCarPanel(true, false, currentTau, new Color(41, 128, 185)));
			for (int i = 0; i < modToa.getRowCount(); i++) {
				int thuTu = (int) modToa.getValueAt(i, 0);
				String ma = modToa.getValueAt(i, 1).toString();
				String loai = modToa.getValueAt(i, 3).toString();
				boolean isSelected = ma.equals(currentMaToa);
				Color carColor = new Color(93, 173, 226);
				if (loai.toLowerCase().contains("mềm"))
					carColor = new Color(231, 76, 60);
				if (loai.toLowerCase().contains("nằm"))
					carColor = new Color(162, 217, 40);

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
		for (Object[] obj : daoToa.getToaTrongKhoSanSang()) {
			// Kiểm tra an toàn: Nếu DAO đã được cập nhật (có >= 5 cột dữ liệu)
			if (obj.length > 4) {
				if (obj[4] != null && obj[4].toString().equals("SAN_SANG")) {
					cbKho.addItem(obj[0] + " - " + obj[1] + " [" + obj[3] + " - " + obj[2] + " chỗ]");
				}
			} else {
				// Nếu DAO cũ chỉ có 4 cột (chưa có cột trạng thái), thì nạp thẳng vào ComboBox
				cbKho.addItem(obj[0] + " - " + obj[1] + " [" + obj[3] + " - " + obj[2] + " chỗ]");
			}
		}
	}

	private JPanel legendItem(Color color, String label) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		p.setOpaque(false);
		JLabel ic = new JLabel("  ");
		ic.setBackground(color);
		ic.setOpaque(true);
		ic.setPreferredSize(new Dimension(10, 10)); // Thu nhỏ icon legend
		JLabel tx = new JLabel(label);
		tx.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Thu nhỏ text legend
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

	// =========================================================================
	// LỚP VẼ HÌNH TOA TÀU NHỎ BÊN DƯỚI (ĐÃ THU NHỎ SIZE)
	// =========================================================================
	class TrainCarPanel extends JPanel {
		private boolean isLocomotive, isSelected;
		private String labelText;
		private Color carColor;

		public TrainCarPanel(boolean isLocomotive, boolean isSelected, String labelText, Color carColor) {
			this.isLocomotive = isLocomotive;
			this.isSelected = isSelected;
			this.labelText = labelText;
			this.carColor = carColor;
			// ĐÃ SỬA: Ép nhỏ hình Toa (55x40)
			setPreferredSize(new Dimension(65, 45));
			setOpaque(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		public void setSelected(boolean sel) {
			this.isSelected = sel;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth() - 4;
			int h = 20;
			int y = 2;

			// Vẽ Bánh xe & Khớp nối
			g2.setColor(new Color(105, 110, 115));
			g2.fillOval(6, y + h - 3, 8, 8);
			g2.fillOval(w - 14, y + h - 3, 8, 8);
			g2.setStroke(new BasicStroke(2f));
			g2.drawLine(10, y + h + 1, w - 10, y + h + 1);
			if (!isLocomotive) {
				g2.fillRect(w, y + h - 6, 4, 2);
			}

			// Vẽ Thân
			g2.setColor(isSelected ? new Color(241, 196, 15) : carColor);
			if (isLocomotive) {
			    int[] px = { 10, w, w, 0, 0 };
			    int[] py = { y, y, y + h, y + h, y + 10 };
			    g2.fillPolygon(px, py, 5);
			    g2.setColor(Color.WHITE);
			    g2.fillRect(12, y + 3, 6, 8);
			    g2.fillRect(6, y + 5, 4, 6);
			} else {
				g2.fillRoundRect(0, y, w, h, 6, 6);
				g2.setColor(Color.WHITE);
				int winW = (w - 16) / 3;
				g2.fillRect(4, y + 4, winW, 6);
				g2.fillRect(8 + winW, y + 4, winW, 6);
				g2.fillRect(12 + winW * 2, y + 4, winW, 6);
				g2.setColor(new Color(0, 0, 0, 40));
				g2.fillRect(0, y + h - 4, w, 2);
			}

			// Vẽ Số Toa
			g2.setColor(isSelected ? new Color(211, 84, 0) : ACCENT);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Font nhỏ hơn
			FontMetrics fm = g2.getFontMetrics();
			int tx = (getWidth() - fm.stringWidth(labelText)) / 2;
			g2.drawString(labelText, tx, y + h + 12);
			g2.dispose();
		}
	}
}