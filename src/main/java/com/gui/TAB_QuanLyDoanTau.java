package com.gui;

import com.dao.DAO_ChiTietTau;
import com.dao.DAO_LoaiToa;
import com.dao.DAO_Tau;
import com.dao.DAO_Toa;
import com.entities.LoaiToa;
import com.entities.NhanVien;
import com.entities.Tau;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
	private static final Color C_GRAY = new Color(0x7F8C8D);
	private static final Color C_BORDER = new Color(0xDDE6F5);

	private static final Font F_HEADER = new Font("Segoe UI", Font.BOLD, 14);
	private static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 13);
	private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

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
	private NhanVien nhanVienHienTai;

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

	public TAB_QuanLyDoanTau(NhanVien nv) {
		this.nhanVienHienTai = nv;
		setLayout(new BorderLayout());
		cardLayout = new CardLayout();
		pnlCards = new JPanel(cardLayout);
		JPanel pnlMainView = createMainView();

		// Truyền tiếp nv xuống cho TAB_Toa quản lý
		TAB_Toa tabToa = new TAB_Toa(nv, () -> {
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
		JButton btnKhoToa = makeBtn("Quản lý Kho Toa", new Color(0x103667));
		btnKhoToa.setPreferredSize(new Dimension(160, 34));
		JButton btnLoaiToa = makeBtn("Quản lý loại toa", new Color(0x666666));
		btnLoaiToa.setPreferredSize(new Dimension(140, 34));
		JButton btnLichSu = makeBtn("Lịch sử bảo trì", new Color(0x993333));
		btnLichSu.setPreferredSize(new Dimension(140, 34));
		btnLichSu.addActionListener(e -> {
			com.gui.Form_LichSuBaoTri frm = new com.gui.Form_LichSuBaoTri(
					(Frame) SwingUtilities.getWindowAncestor(this), "TỔNG HỢP NHẬT KÝ BẢO TRÌ HỆ THỐNG", "ALL", "",
					false, this.nhanVienHienTai != null ? this.nhanVienHienTai.getMaNV() : "NV001", false // <--- MỚI
																											// THÊM
			);
			frm.setVisible(true);
		});

		btnKhoToa.addActionListener(e -> cardLayout.show(pnlCards, "TOA"));
		btnLoaiToa.addActionListener(e -> cardLayout.show(pnlCards, "LOAITOA"));

		pnlGlobalActions.add(btnLoaiToa);
		pnlGlobalActions.add(btnKhoToa);
		pnlGlobalActions.add(btnLichSu);
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
						f.setNhanVien(TAB_QuanLyDoanTau.this.nhanVienHienTai);
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
									return; // Chặn lại, không mở form ghi nhật ký bảo trì
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

							// =================================================================
							// 3. ĐÁNH CHẶN GHI NHẬN LỊCH SỬ BẢO TRÌ TÀU
							// =================================================================
							if (!oldStatus.equals(newStatus)) {
								// Trường hợp 1: Đưa tàu đi bảo trì (Chỉ nhập LÝ DO)
								if ("BAOTRI".equals(newStatus)) {
									Form_LichSuBaoTri frm = new Form_LichSuBaoTri(
											(Frame) SwingUtilities.getWindowAncestor(TAB_QuanLyDoanTau.this),
											"Ghi nhận lý do bảo trì tàu " + tUpdate.getMaTau(), "TAU",
											tUpdate.getMaTau(), false,
											TAB_QuanLyDoanTau.this.nhanVienHienTai != null
													? TAB_QuanLyDoanTau.this.nhanVienHienTai.getMaNV()
													: "NV001",
											false // isHoanTat = false
									);
									frm.setVisible(true);
									if (!frm.isConfirmed())
										return;
								}
								// Trường hợp 2: Tàu sửa xong quay lại hoạt động (Chỉ nhập CHI PHÍ thực tế)
								else if ("BAOTRI".equals(oldStatus)) {
									Form_LichSuBaoTri frm = new Form_LichSuBaoTri(
											(Frame) SwingUtilities.getWindowAncestor(TAB_QuanLyDoanTau.this),
											"Nghiệm thu chi phí sửa chữa tàu " + tUpdate.getMaTau(), "TAU",
											tUpdate.getMaTau(), false,
											TAB_QuanLyDoanTau.this.nhanVienHienTai != null
													? TAB_QuanLyDoanTau.this.nhanVienHienTai.getMaNV()
													: "NV001",
											true // isHoanTat = true
									);
									frm.setVisible(true);
									if (!frm.isConfirmed())
										return;
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

		JButton btnAuto = makeBtn("Auto Sinh Toa", new Color(0x947BD3));
		btnAuto.setPreferredSize(new Dimension(130, 32));

		btnIn.addActionListener(e -> ganToa());
		btnOut.addActionListener(e -> goToa());
		btnLen.addActionListener(e -> doDoiChoToa(-1));
		btnXuong.addActionListener(e -> doDoiChoToa(1));
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

		// Căn giữa chú thích và thêm viền đệm phía trên
		JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
		legend.setOpaque(false);
		legend.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		// --- CẬP NHẬT MÀU MỚI: Xám nhạt (Sẵn sàng) và Đỏ (Bảo trì) ---
		legend.add(legendItem(new Color(0xE9EDF2), new Color(0xD1D9E0), "Sẵn sàng"));
		legend.add(legendItem(new Color(0xDC3545), new Color(0xDC3545).darker(), "Đang bảo trì"));
		hdrMap.add(titleRow, BorderLayout.WEST);

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

	private void goToanBoToaVeKho(String maTau) {
		List<Object[]> attachedToa = daoCT.getToaOfTau(maTau);
		for (Object[] obj : attachedToa) {
			String maToa = obj[1].toString();
			daoCT.goToaKhoiTau(maTau, maToa);
			daoToa.updateTrangThai(maToa, "SAN_SANG");
		}
	}

	// ====================================================================
	// HÀM MỚI: LẤY 1 LẦN DUY NHẤT DANH SÁCH GHẾ ĐÃ ĐẶT (CHỐNG LỖI TREO MÁY)
	// ====================================================================
	private java.util.Set<String> getGheDaDatTrongTuongLai(String maToa) {
		java.util.Set<String> bookedSeats = new java.util.HashSet<>();
		String sql = "SELECT gl.viTri FROM GheLichTrinh gl JOIN LichTrinh lt ON gl.maLT = lt.maLT "
				+ "WHERE gl.maToa = ? AND gl.trangThai IN ('DADAT', 'GIUCHO') "
				+ "AND (lt.ngayKhoiHanh > CAST(GETDATE() AS DATE) OR (lt.ngayKhoiHanh = CAST(GETDATE() AS DATE) AND lt.gioKhoiHanh > CAST(GETDATE() AS TIME)))";
		try (java.sql.Connection c = com.connectDB.ConnectDB.getConnection();
				java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, maToa);
			java.sql.ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				bookedSeats.add(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bookedSeats;
	}

	// ====================================================================
	// VẼ SƠ ĐỒ VẬT LÝ VÀ SỰ KIỆN CLICK GHẾ BẢO TRÌ
	// ====================================================================
	private void generateSeatMap(String maToa, String tenToa, int thuTu) {
		pnlMap.removeAll();
		lblMapTitle.setText("3. SƠ ĐỒ GHẾ VẬT LÝ: Toa Số " + thuTu + " - " + tenToa + " (" + maToa + ")");

		Set<String> maintenanceSeats = daoToa.getGheBaoTri(maToa);

		// ĐÃ FIX: Lấy danh sách ghế đã bán 1 lần duy nhất
		Set<String> bookedSeats = getGheDaDatTrongTuongLai(maToa);

		Object[] thongTin = daoToa.getThongTinToaForMap(maToa);

		if (thongTin != null) {
			int soHang = (int) thongTin[0];
			int soCot = (int) thongTin[1];
			String kieu = (String) thongTin[2];
			int tongGhe = (int) thongTin[3];

			lblSeatStats
					.setText("   Tổng sức chứa: " + tongGhe + " ghế   |   Đang bảo trì: " + maintenanceSeats.size());

			// Truyền thêm bookedSeats vào các hàm vẽ
			JPanel seatPanel = "GIUONG".equalsIgnoreCase(kieu)
					? drawSleeperHorizontal(soHang, soCot, maintenanceSeats, bookedSeats, maToa)
					: drawSeaterHorizontal(soHang, soCot, maintenanceSeats, bookedSeats, maToa);
			pnlMap.add(seatPanel, BorderLayout.CENTER);
		}
		pnlMap.revalidate();
		pnlMap.repaint();
		refreshTrainBar();
	}

	// ĐÃ FIX: Thêm tham số bookedSeats
	// Vẫn truyền bookedSeats vào để check ngầm, nhưng không dùng để tô màu!
	private JButton seatBtn(int num, java.util.Set<String> maintenanceSeats, java.util.Set<String> bookedSeats,
			String maToa) {
		String viTri = String.valueOf(num);
		boolean isBaoTri = maintenanceSeats.contains(viTri);
		boolean isBooked = bookedSeats.contains(viTri); // Chỉ lưu trạng thái ngầm trên RAM

		JButton b = new JButton(viTri) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				// CHỈ CÒN 2 TRẠNG THÁI VẬT LÝ: Đỏ (Bảo trì) và Xanh xám nhạt (Sẵn sàng)
				Color bg = isBaoTri ? new Color(0xDC3545) : new Color(0xE9EDF2);
				Color border = isBaoTri ? new Color(0xDC3545).darker() : new Color(0xD1D9E0);
				Color text = isBaoTri ? Color.WHITE : new Color(0x5A6A7D);

				g2.setColor(bg);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
				g2.setColor(border);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);

				g2.setColor(text);
				FontMetrics fm = g2.getFontMetrics();
				int tx = (getWidth() - fm.stringWidth(getText())) / 2;
				int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
				g2.drawString(getText(), tx, ty);
				g2.dispose();
			}
		};

		b.setPreferredSize(new Dimension(32, 28));
		b.setFont(new Font("Segoe UI", Font.BOLD, 12));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Tooltip cũng chỉ hiển thị 2 trạng thái vật lý
		b.setToolTipText("Ghế số " + num + (isBaoTri ? " - ĐANG BẢO TRÌ" : " - SẴN SÀNG"));

		b.addActionListener(e -> {
			if (isBaoTri) {
				// 1. MỞ KHÓA: Nhập chi phí
				Form_LichSuBaoTri frm = new Form_LichSuBaoTri((Frame) SwingUtilities.getWindowAncestor(this),
						"Nghiệm thu chi phí sửa ghế " + num, "GHE", maToa + "_" + viTri, false,
						this.nhanVienHienTai != null ? this.nhanVienHienTai.getMaNV() : "NV001", true);
				frm.setVisible(true);

				if (frm.isConfirmed()) {
					daoToa.removeGheBaoTri(maToa, viTri);
					generateSeatMap(currentMaToa, currentTenToa, currentThuTu);
					JOptionPane.showMessageDialog(this, "Ghế số " + num + " đã được sửa xong và đưa vào khai thác!");
				}
			} else {
				// 2. ĐƯA VÀO BẢO TRÌ: Lúc này biến ngầm isBooked mới phát huy tác dụng
				if (isBooked) {
					JOptionPane.showMessageDialog(this,
							"⚠️ TỪ CHỐI BẢO TRÌ!\n\nGhế số " + num
									+ " đã có hành khách mua vé trong các chuyến đi tương lai.\n"
									+ "Vui lòng báo bộ phận CSKH dời chỗ cho khách trước khi khóa ghế đưa vào xưởng.",
							"Ràng buộc hệ thống", JOptionPane.WARNING_MESSAGE);
				} else {
					Form_LichSuBaoTri frm = new Form_LichSuBaoTri((Frame) SwingUtilities.getWindowAncestor(this),
							"Ghi nhận lý do hỏng ghế " + num, "GHE", maToa + "_" + viTri, false,
							this.nhanVienHienTai != null ? this.nhanVienHienTai.getMaNV() : "NV001", false);
					frm.setVisible(true);

					if (frm.isConfirmed()) {
						daoToa.addGheBaoTri(maToa, viTri);
						generateSeatMap(currentMaToa, currentTenToa, currentThuTu);
					}
				}
			}
		});
		return b;
	}

	// ĐÃ FIX: Thêm tham số bookedSeats
	private JPanel drawSeaterHorizontal(int soHang, int soCot, Set<String> maintenanceSeats, Set<String> bookedSeats,
			String maToa) {
		JPanel outer = new JPanel(new BorderLayout(8, 0));
		outer.setBackground(Color.WHITE);
		outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xE2EAF4), 1, true),
				new EmptyBorder(10, 10, 10, 10)));

		JLabel capA = new JLabel(" ĐẦU TÀU ");
		capA.setFont(new Font("Segoe UI", Font.BOLD, 11));
		capA.setForeground(new Color(0x7F8C8D));
		JLabel capB = new JLabel(" CUỐI TÀU");
		capB.setFont(new Font("Segoe UI", Font.BOLD, 11));
		capB.setForeground(new Color(0x7F8C8D));

		int uiRows = soCot;
		int uiCols = soHang;
		int halfRows = Math.max(1, uiRows / 2);

		JPanel gridBody = new JPanel(new GridLayout(uiRows + 1, uiCols, 5, 5));
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
					// Gọi hàm seatBtn mới với tham số bookedSeats
					gridBody.add(seatBtn(seatNum, maintenanceSeats, bookedSeats, maToa));
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

	// ĐÃ FIX: Thêm tham số bookedSeats
	private JPanel drawSleeperHorizontal(int soHang, int soCot, Set<String> maintenanceSeats, Set<String> bookedSeats,
			String maToa) {
		int khoang = Math.max(1, soHang);
		int soTang = soCot / 2;

		JPanel outer = new JPanel(new BorderLayout(8, 0));
		outer.setBackground(Color.WHITE);
		outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xE2EAF4), 1, true),
				new EmptyBorder(10, 10, 10, 10)));

		JLabel capA = new JLabel(" ĐẦU TÀU ");
		capA.setFont(new Font("Segoe UI", Font.BOLD, 11));
		capA.setForeground(new Color(0x7F8C8D));
		JLabel capB = new JLabel(" CUỐI TÀU");
		capB.setFont(new Font("Segoe UI", Font.BOLD, 11));
		capB.setForeground(new Color(0x7F8C8D));

		JPanel pnlTangLabels = new JPanel(new GridLayout(soTang, 1, 2, 2));
		pnlTangLabels.setOpaque(false);
		pnlTangLabels.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 5));
		for (int tang = soTang - 1; tang >= 0; tang--) {
			JLabel lblTang = new JLabel("Tầng " + (tang + 1), SwingConstants.RIGHT);
			lblTang.setFont(new Font("Segoe UI", Font.BOLD, 10));
			lblTang.setForeground(new Color(0x5A6A7D));
			pnlTangLabels.add(lblTang);
		}

		JPanel gridBody = new JPanel(new GridLayout(1, khoang, 8, 0));
		gridBody.setOpaque(false);

		int idx = 1;
		for (int k = 1; k <= khoang; k++) {
			JPanel kp = new JPanel(new BorderLayout(0, 2));
			kp.setBackground(new Color(0xF0F4FA));
			kp.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xDDE6F5), 1, true),
					new EmptyBorder(5, 5, 5, 5)));

			JLabel lbl = new JLabel("Khoang " + k, SwingConstants.CENTER);
			lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
			lbl.setForeground(new Color(0x7F8C8D));
			kp.add(lbl, BorderLayout.NORTH);

			JPanel grid = new JPanel(new GridLayout(soTang, 2, 4, 4));
			grid.setOpaque(false);

			for (int tang = soTang - 1; tang >= 0; tang--) {
				// Gọi hàm seatBtn mới với tham số bookedSeats
				grid.add(seatBtn(idx + 2 * tang, maintenanceSeats, bookedSeats, maToa));
				grid.add(seatBtn(idx + 2 * tang + 1, maintenanceSeats, bookedSeats, maToa));
			}

			idx += soCot;
			kp.add(grid, BorderLayout.CENTER);
			gridBody.add(kp);
		}

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setOpaque(false);
		wrapper.add(pnlTangLabels, BorderLayout.WEST);
		wrapper.add(gridBody, BorderLayout.CENTER);

		JPanel centerAlignWrapper = new JPanel(new GridBagLayout());
		centerAlignWrapper.setOpaque(false);
		centerAlignWrapper.add(wrapper);

		outer.add(capA, BorderLayout.WEST);
		outer.add(centerAlignWrapper, BorderLayout.CENTER);
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
			return;

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

		// =================================================================
		// ĐÃ UPDATE: Đánh chặn mở Form ghi lý do bảo trì nếu chọn đem đi sửa
		// =================================================================
		String status = "SAN_SANG";
		if (choice == 1) {
			Form_LichSuBaoTri frm = new Form_LichSuBaoTri((Frame) SwingUtilities.getWindowAncestor(this),
					"Ghi nhận lý do bảo trì toa " + maToa, "TOA", maToa, false,
					this.nhanVienHienTai != null ? this.nhanVienHienTai.getMaNV() : "NV001", false // isHoanTat = false
																									// (Chỉ bắt nhập lý
																									// do hỏng)
			);
			frm.setVisible(true);

			// Nếu tắt form hoặc bấm hủy bỏ -> Ngắt luồng không thực hiện gỡ toa dưới DB
			if (!frm.isConfirmed()) {
				return;
			}
			status = "BAO_TRI";
		}

		// Thực thi tháo dỡ mối liên kết Toa - Tàu dưới Database
		if (!daoCT.goToaKhoiTau(currentTau, maToa)) {
			JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi gỡ toa.", "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Cập nhật trạng thái vật lý tương ứng của Toa
		daoToa.updateTrangThai(maToa, status);

		// Sắp xếp, dồn lại số thứ tự các toa còn lại phía sau
		List<String> remaining = new ArrayList<>();
		for (int i = 0; i < modToa.getRowCount(); i++)
			if (i != row)
				remaining.add(modToa.getValueAt(i, 1).toString());
		daoCT.capNhatThuTuSauKhiGo(currentTau, remaining);

		// Đồng bộ lại giao diện
		loadToaOfTau();
		loadKho();
		clearSeatMap();
	}

	private void doDoiChoToa(int delta) {
		if (!checkTauHoatDong()) {
			return;
		}
		// =================================================================
		// ĐÃ THÊM: KHÓA CHỨC NĂNG DI CHUYỂN NẾU TÀU ĐANG DÍNH LỊCH TRÌNH
		// =================================================================
		if (isTauCoLichTrinhTuongLai(currentTau)) {
			JOptionPane.showMessageDialog(this,
					"⚠️ TỪ CHỐI THAO TÁC!\n\nĐoàn tàu này đang được phân công chạy trong các lịch trình tương lai.\n"
							+ "Việc thay đổi thứ tự toa sẽ làm sai lệch vị trí Toa/Ghế trên vé mà hành khách đã mua.\n"
							+ "Chỉ được phép sắp xếp lại thứ tự khi tàu ở trạng thái trống (chưa có lịch trình).",
					"Ràng buộc toàn vẹn dữ liệu", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int row = tblToa.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "Chọn một toa trên đoàn tàu để di chuyển!", "Thông báo",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		int targetRow = row + delta;
		// Kiểm tra nếu chạm nóc (lên quá dòng đầu) hoặc chạm đáy (xuống quá dòng cuối)
		if (targetRow < 0 || targetRow >= modToa.getRowCount()) {
			return;
		}

		// =========================================================
		// CHIẾN THUẬT MỚI: HOÁN ĐỔI TRÊN RAM ĐỂ TRÁNH LỖI SQL CONSTRAINT
		// =========================================================

		// 1. Rút danh sách mã toa hiện tại ra một mảng ảo (RAM)
		List<String> listMaToa = new ArrayList<>();
		for (int i = 0; i < modToa.getRowCount(); i++) {
			listMaToa.add(modToa.getValueAt(i, 1).toString()); // Cột 1 là Mã Toa
		}

		// 2. Hoán đổi vị trí của 2 toa trong mảng ảo
		String temp = listMaToa.get(row);
		listMaToa.set(row, listMaToa.get(targetRow));
		listMaToa.set(targetRow, temp);

		// 3. Gọi hàm capNhatThuTuSauKhiGo để Database xóa trắng thứ tự cũ và đánh lại
		// từ 1 -> N
		// Lưu ý: Đảm bảo trong hàm DAO này bạn đã viết logic an toàn (xóa/update tuần
		// tự)
		daoCT.capNhatThuTuSauKhiGo(currentTau, listMaToa);

		// 4. Reload lại toàn bộ dữ liệu lên Table để đảm bảo UI đồng bộ 100% với
		// Database
		loadToaOfTau();
		clearSeatMap(); // Xóa sơ đồ ghế cũ tránh lỗi click nhầm

		// Nếu trong class bạn có hàm vẽ lại đồ họa tàu thì gọi lại nó
		// refreshTrainBar();

		// 5. Bám đuôi bôi đen theo toa vừa di chuyển để người dùng có thể bấm click
		// liên tục
		tblToa.setRowSelectionInterval(targetRow, targetRow);
	}

	private void autoGenerateToaPopup() {
		if (currentTau == null) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 tàu bên trái trước!");
			return;
		}
		if (!checkTauHoatDong())
			return;

		// [VÁ LỖI LOGIC]: Tự động tính số lượng toa còn thiếu thay vì ép tàu phải rỗng
		int soToaHienTai = modToa.getRowCount();
		int soToaCanSinh = quyDinhSoToa - soToaHienTai;

		if (soToaCanSinh <= 0) {
			JOptionPane.showMessageDialog(this, "Tàu này đã được gắn đủ " + quyDinhSoToa + " toa!");
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

		// Khởi tạo Dialog với giao diện nền trắng giống Tab Nhân Viên
		JDialog d = new JDialog(JOptionPane.getFrameForComponent(this), "Cấu hình Toa Tự Động", true);
		d.getContentPane().setBackground(Color.WHITE);

		// ===== HEADER (Tiêu đề lớn bên góc trái) =====
		JPanel pnlHeader = new JPanel(new BorderLayout());
		pnlHeader.setOpaque(false);
		pnlHeader.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
		JLabel lblTitle = new JLabel("Sinh tự động " + soToaCanSinh + " Toa cho tàu " + currentTau);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitle.setForeground(ACCENT);
		pnlHeader.add(lblTitle, BorderLayout.WEST);

		// ===== BODY FORM (Sử dụng GridBagLayout giống Form Nhân Viên) =====
		JPanel form = new JPanel(new GridBagLayout());
		form.setBackground(Color.WHITE);
		form.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(8, 6, 8, 6);

		JComboBox<LoaiToaWrapper>[] combos = new JComboBox[soToaCanSinh];
		int p1 = soToaCanSinh / 3;
		int p2 = soToaCanSinh / 3;

		for (int i = 0; i < soToaCanSinh; i++) {
			int thuTuToa = soToaHienTai + i + 1;

			// Label font giống Form Nhân viên
			JLabel lblToa = new JLabel("Toa nối thứ " + thuTuToa + ": ");
			lblToa.setFont(new Font("Segoe UI", Font.BOLD, 13));
			lblToa.setForeground(new Color(0x5A6A7D));

			JComboBox<LoaiToaWrapper> cb = new JComboBox<>();
			for (LoaiToaWrapper w : wrapperList)
				cb.addItem(w);

			if (i < p1)
				cb.setSelectedItem(defCung);
			else if (i < p1 + p2)
				cb.setSelectedItem(defMem);
			else
				cb.setSelectedItem(defNam);

			// Style cho ComboBox khớp chuẩn UI
			cb.setPreferredSize(new Dimension(0, 36));
			cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
			cb.setBackground(new Color(0xF8FAFD));
			cb.setForeground(new Color(0x1E2B3C));
			cb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xE2EAF4), 1, true),
					BorderFactory.createEmptyBorder(0, 4, 0, 4)));

			// Tuỳ chỉnh giao diện danh sách xổ xuống của ComboBox
			cb.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
				JLabel lbl = new JLabel(value.toString());
				lbl.setOpaque(true);
				lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
				if (isSelected) {
					lbl.setBackground(new Color(0xDDEEFF));
				} else {
					lbl.setBackground(index % 2 == 0 ? Color.WHITE : new Color(0xF7FAFF));
				}
				return lbl;
			});

			combos[i] = cb;

			gc.gridx = 0;
			gc.gridy = i;
			gc.weightx = 0.2;
			gc.gridwidth = 1;
			form.add(lblToa, gc);

			gc.gridx = 1;
			gc.gridy = i;
			gc.weightx = 0.8;
			gc.gridwidth = 1;
			form.add(cb, gc);
		}

		JScrollPane scroll = new JScrollPane(form);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(Color.WHITE);
		scroll.getVerticalScrollBar().setUnitIncrement(16);

		// ===== BOTTOM ACTIONS (Căn giữa, Nút bấm lớn) =====
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 16));
		bottom.setBackground(Color.WHITE);

		JButton btnXacNhan = makeBtn("Xác nhận Sinh Toa", ACCENT);
		btnXacNhan.setPreferredSize(new Dimension(160, 38));
		btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 14));

		JButton btnHuy = makeBtn("Hủy Bỏ", new Color(149, 165, 166));
		btnHuy.setPreferredSize(new Dimension(120, 38));
		btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 14));

		btnHuy.addActionListener(e -> d.dispose());
		btnXacNhan.addActionListener(e -> {
			List<Object[]> toaList = new ArrayList<>();
			for (int i = 0; i < soToaCanSinh; i++) {
				int toaNum = soToaHienTai + i + 1;
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
				JOptionPane.showMessageDialog(d, "Đã sản xuất và lắp ráp thành công " + soToaCanSinh + " toa!",
						"Thành công", JOptionPane.INFORMATION_MESSAGE);
				d.dispose();
				loadToaOfTau();
				loadKho();
				clearSeatMap();
			} else {
				JOptionPane.showMessageDialog(d, "Có lỗi xảy ra khi tạo toa tự động!", "Lỗi",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		bottom.add(btnHuy);
		bottom.add(btnXacNhan);

		d.setLayout(new BorderLayout());
		d.add(pnlHeader, BorderLayout.NORTH);
		d.add(scroll, BorderLayout.CENTER);
		d.add(bottom, BorderLayout.SOUTH);

		d.pack();
		int height = Math.min(600, d.getHeight() + 40); // Tính toán độ cao linh hoạt
		d.setSize(550, height);
		d.setLocationRelativeTo(this);
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
			// obj[0]: maToa, obj[1]: tenToa, obj[3]: tenLoaiToa, obj[2]: soGhe
			cbKho.addItem(obj[0] + " - " + obj[1] + " [" + obj[3] + " - " + obj[2] + " chỗ]");
		}
	}

	private JPanel legendItem(Color bg, Color border, String label) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		p.setOpaque(false);
		JLabel ic = new JLabel("  ");
		ic.setBackground(bg);
		ic.setOpaque(true);
		ic.setPreferredSize(new Dimension(12, 12));
		if (border != null) {
			ic.setBorder(BorderFactory.createLineBorder(border, 1));
		}
		JLabel tx = new JLabel(label);
		tx.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		tx.setForeground(new Color(0x5A6A7D)); // Chữ màu xám đen đồng bộ UI
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