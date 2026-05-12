package com.gui.banve;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Step2_ChonChoNgoi extends JPanel {
	private TAB_BanVe mainTab;
	private JPanel pnlDirectionToggle;
	private CardLayout routeCardLayout;
	private JPanel pnlRouteCards;
	private JToggleButton btnChieuDi, btnChieuVe;

	private RoutePanel pnlOutbound;
	private RoutePanel pnlReturn;
	private Timer realTimeTimer;

	private boolean isRoundTrip = false;
	private JButton btnCart;

	// === BẢNG MÀU HIỆN ĐẠI ===
	public static final Color BG_LIGHT = new Color(0xF8FAFC);
	public static final Color ACCENT = new Color(0x1A5EAB);
	public static final Color TEXT_DARK = new Color(0x1E2B3C);
	public static final Color TEXT_MID = new Color(0x5A6A7D);
	public static final Color BORDER_CLR = new Color(0xE2EAF4);

	// === MÀU TRẠNG THÁI GHẾ MỚI ===
	public static final Color SEAT_AVAILABLE_BG = new Color(0xE9EDF2);
	public static final Color SEAT_AVAILABLE_BORDER = new Color(0xD1D9E0);
	public static final Color SEAT_SELECTED_BG = new Color(0xF39C12); // Cam
	public static final Color SEAT_BOOKED_BG = new Color(0x1A5EAB); // Xanh dương chính
	public static final Color SEAT_MAINTENANCE_BG = new Color(0x95A5A6); // Xám

	public Step2_ChonChoNgoi(TAB_BanVe mainTab) {
		this.mainTab = mainTab;
		initUI();

		realTimeTimer = new Timer(3000, e -> {
			pnlOutbound.refreshCurrentSeatMap();
			pnlReturn.refreshCurrentSeatMap();
		});
		realTimeTimer.start();
	}

	private void initUI() {
		setLayout(new BorderLayout());
		setBackground(BG_LIGHT);

		// Header với Nút Giỏ hàng
		JPanel pnlHeader = new JPanel(new BorderLayout());
		pnlHeader.setOpaque(false);
		pnlHeader.add(UIHelper.createPageTitle("ĐẶT CHỖ", ""), BorderLayout.WEST);

		btnCart = new JButton("Giỏ hàng (0)") {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isRollover() ? new Color(192, 57, 43) : new Color(231, 76, 60));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		btnCart.setForeground(Color.WHITE);
		btnCart.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnCart.setContentAreaFilled(false);
		btnCart.setBorderPainted(false);
		btnCart.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnCart.addActionListener(e -> showCartPopup());

		JPanel pnlCartWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
		pnlCartWrapper.setOpaque(false);
		pnlCartWrapper.add(btnCart);
		pnlHeader.add(pnlCartWrapper, BorderLayout.EAST);

		add(pnlHeader, BorderLayout.NORTH);

		JPanel pnl = new JPanel(new BorderLayout(0, 5));
		pnl.setOpaque(false);
		pnlDirectionToggle = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		pnlDirectionToggle.setOpaque(false);

		btnChieuDi = UIHelper.createSelectionTab("CHIỀU ĐI", true);
		btnChieuVe = UIHelper.createSelectionTab("CHIỀU VỀ", false);
		btnChieuDi.setPreferredSize(new Dimension(200, 36));
		btnChieuVe.setPreferredSize(new Dimension(200, 36));

		ButtonGroup bgDir = new ButtonGroup();
		bgDir.add(btnChieuDi);
		bgDir.add(btnChieuVe);
		pnlDirectionToggle.add(btnChieuDi);
		pnlDirectionToggle.add(btnChieuVe);

		routeCardLayout = new CardLayout();
		pnlRouteCards = new JPanel(routeCardLayout);
		pnlRouteCards.setOpaque(false);

		pnlOutbound = new RoutePanel("DI");
		pnlReturn = new RoutePanel("VE");

		pnlRouteCards.add(pnlOutbound, "OUTBOUND");
		pnlRouteCards.add(pnlReturn, "RETURN");

		btnChieuDi.addActionListener(e -> routeCardLayout.show(pnlRouteCards, "OUTBOUND"));
		btnChieuVe.addActionListener(e -> routeCardLayout.show(pnlRouteCards, "RETURN"));

		pnl.add(pnlDirectionToggle, BorderLayout.NORTH);
		pnl.add(pnlRouteCards, BorderLayout.CENTER);
		add(pnl, BorderLayout.CENTER);
	}

	public void updateCartStatus() {
		List<Map<String, String>> seats = mainTab.getSelectedSeatsData();
		int countDi = 0, countVe = 0;
		for (Map<String, String> s : seats) {
			if ("DI".equals(s.get("chieu")))
				countDi++;
			if ("VE".equals(s.get("chieu")))
				countVe++;
		}
		btnCart.setText("Giỏ hàng (" + seats.size() + ")");
		if (isRoundTrip) {
			mainTab.setNextButtonEnabled(countDi > 0 && countVe > 0);
		} else {
			mainTab.setNextButtonEnabled(countDi > 0);
		}
	}

	private void showCartPopup() {
		JPopupMenu popup = new JPopupMenu();
		popup.setBorder(BorderFactory.createLineBorder(ACCENT, 2));
		List<Map<String, String>> seats = mainTab.getSelectedSeatsData();
		if (seats.isEmpty()) {
			JMenuItem empty = new JMenuItem("  Giỏ hàng đang trống  ");
			empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
			popup.add(empty);
		} else {
			long total = 0;
			for (Map<String, String> s : seats) {
				String chieuLabel = "DI".equals(s.get("chieu")) ? "Lượt đi" : "Lượt về";
				String lbl = String.format(" %s: %s - %s - Ghế %s ", chieuLabel, s.get("tenTau"), s.get("tenToa"),
						s.get("tenCho"));
				long gia = 0;
				try {
					gia = Long.parseLong(s.get("giaVe"));
					total += gia;
				} catch (Exception ignored) {
				}
				JMenuItem item = new JMenuItem(lbl + " | " + String.format("%,d đ", gia));
				item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
				popup.add(item);
			}
			popup.addSeparator();
			JMenuItem totalItem = new JMenuItem(" TỔNG TIỀN: " + String.format("%,d đ", total) + "  ");
			totalItem.setFont(new Font("Segoe UI", Font.BOLD, 15));
			totalItem.setForeground(new Color(0xE74C3C));
			popup.add(totalItem);
		}
		popup.show(btnCart, 0, btnCart.getHeight());
	}

	// =====================================================================
	// GIỮ NGUYÊN HÀM loadTrainData TỪ BẢN GỐC ĐỂ KHÔNG LỖI STEP 1
	// =====================================================================
	public boolean loadTrainData(String maGaDi, String maGaDen, String sqlNgayDi, boolean isRoundTrip, String strNgayVe,
			String tenGaDi, String tenGaDen) {

		this.isRoundTrip = isRoundTrip;
		updateCartStatus();

		pnlOutbound.clearData();
		pnlReturn.clearData();

		pnlOutbound.lblTitle.setText("CHIỀU ĐI: " + tenGaDi + " → " + tenGaDen);
		boolean hasOutbound = pnlOutbound.fetchDataTrains(maGaDi, maGaDen, sqlNgayDi);

		if (!hasOutbound) {
			JOptionPane.showMessageDialog(this, "Không tìm thấy chuyến tàu nào cho Chiều Đi vào ngày này!", "Thông báo",
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}

		if (isRoundTrip) {
			String sqlNgayVe = "";
			try {
				Date d = new SimpleDateFormat(UIHelper.DATE_FMT).parse(strNgayVe);
				sqlNgayVe = new SimpleDateFormat("yyyy-MM-dd").format(d);
			} catch (Exception e) {
			}

			pnlReturn.lblTitle.setText("CHIỀU VỀ: " + tenGaDen + " → " + tenGaDi);
			boolean hasReturn = pnlReturn.fetchDataTrains(maGaDen, maGaDi, sqlNgayVe);

			if (!hasReturn) {
				JOptionPane.showMessageDialog(this, "Không tìm thấy chuyến tàu nào cho Chiều Về vào ngày này!",
						"Thông báo", JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
		}

		pnlDirectionToggle.setVisible(isRoundTrip);
		btnChieuDi.setSelected(true);
		routeCardLayout.show(pnlRouteCards, "OUTBOUND");
		return true;
	}

	private class RoutePanel extends JPanel {
		String chieu;
		JLabel lblTitle, lblToaNameInfo;
		JPanel pnlTauList, pnlTrainBar, pnlSeatContent;
		ButtonGroup bgTau;
		String currentMaLT = "", currentTenTau = "", currentMaToa = "", currentTenToa = "";
		Map<String, JToggleButton> seatButtonsMap = new HashMap<>();

		public RoutePanel(String chieu) {
			this.chieu = chieu;
			setLayout(new BorderLayout(0, 4));
			setOpaque(false);

			JPanel pnlHeader = new JPanel(new BorderLayout());
			pnlHeader.setOpaque(false);
			lblTitle = new JLabel();
			lblTitle.setFont(UIHelper.F_H2);
			lblTitle.setForeground(ACCENT);
			pnlHeader.add(lblTitle, BorderLayout.WEST);

			JPanel pnlSelectionWrapper = UIHelper.makeCard(new BorderLayout(0, 4));
			pnlSelectionWrapper.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(),
					BorderFactory.createEmptyBorder(6, 10, 8, 10)));

			JPanel pnlTopControls = new JPanel(new BorderLayout(10, 0));
			pnlTopControls.setOpaque(false);
			JLabel lblChonTau = new JLabel(
					"<html><b style='color:#5A6A7D; font-size:12px;'>Chọn Tàu: &nbsp;</b></html>");
			pnlTopControls.add(lblChonTau, BorderLayout.WEST);

			pnlTauList = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
			pnlTauList.setOpaque(false);
			JScrollPane scrollTau = new JScrollPane(pnlTauList);
			scrollTau.setBorder(null);
			scrollTau.setOpaque(false);
			scrollTau.getViewport().setOpaque(false);
			scrollTau.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			pnlTopControls.add(scrollTau, BorderLayout.CENTER);

			JPanel pnlGheWrapper = new JPanel(new BorderLayout());
			pnlGheWrapper.setOpaque(false);

			JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
			pnlLegend.setOpaque(false);
			pnlLegend.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
			pnlLegend.add(legendItem(SEAT_AVAILABLE_BG, SEAT_AVAILABLE_BORDER, "Còn trống"));
			pnlLegend.add(legendItem(SEAT_BOOKED_BG, null, "Đã đặt"));
			pnlLegend.add(legendItem(SEAT_SELECTED_BG, null, "Đang chọn"));
			pnlLegend.add(legendItem(SEAT_MAINTENANCE_BG, null, "Bảo trì"));

			pnlTrainBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 4));
			pnlTrainBar.setBackground(Color.WHITE);
			JScrollPane scrollTrain = new JScrollPane(pnlTrainBar);
			scrollTrain.setBorder(new MatteBorder(1, 0, 1, 0, BORDER_CLR));
			scrollTrain.setPreferredSize(new Dimension(0, 55));

			JPanel pnlSeatArea = new JPanel(new BorderLayout());
			pnlSeatArea.setOpaque(false);
			lblToaNameInfo = new JLabel("Vui lòng chọn Toa", SwingConstants.CENTER);
			lblToaNameInfo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
			lblToaNameInfo.setForeground(new Color(0, 136, 204));

			pnlSeatContent = new JPanel(new GridBagLayout());
			pnlSeatContent.setBackground(Color.WHITE);

			pnlSeatArea.add(lblToaNameInfo, BorderLayout.NORTH);
			pnlSeatArea.add(new JScrollPane(pnlSeatContent), BorderLayout.CENTER);
			pnlSeatArea.add(pnlLegend, BorderLayout.SOUTH);

			JPanel pnlMapWrapper = new JPanel(new BorderLayout(0, 2));
			pnlMapWrapper.setOpaque(false);
			pnlMapWrapper.add(scrollTrain, BorderLayout.NORTH);
			pnlMapWrapper.add(pnlSeatArea, BorderLayout.CENTER);
			pnlGheWrapper.add(pnlMapWrapper, BorderLayout.CENTER);

			pnlSelectionWrapper.add(pnlTopControls, BorderLayout.NORTH);
			pnlSelectionWrapper.add(pnlGheWrapper, BorderLayout.CENTER);
			add(pnlHeader, BorderLayout.NORTH);
			add(pnlSelectionWrapper, BorderLayout.CENTER);
		}

		private JPanel legendItem(Color bg, Color border, String label) {
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
			p.setOpaque(false);
			JLabel ic = new JLabel("  ");
			ic.setBackground(bg);
			ic.setOpaque(true);
			ic.setPreferredSize(new Dimension(10, 10));
			if (border != null)
				ic.setBorder(new LineBorder(border));
			JLabel tx = new JLabel(label);
			tx.setFont(new Font("Segoe UI", Font.PLAIN, 11));
			tx.setForeground(TEXT_MID);
			p.add(ic);
			p.add(tx);
			return p;
		}

		public void clearData() {
			pnlTauList.removeAll();
			pnlTrainBar.removeAll();
			pnlSeatContent.removeAll();
			lblToaNameInfo.setText("");
			currentMaLT = "";
			currentMaToa = "";
			revalidate();
			repaint();
		}

		public void refreshCurrentSeatMap() {
			if (!currentMaLT.isEmpty() && !currentMaToa.isEmpty())
				applySeatStatuses(currentMaLT, currentMaToa);
		}

		public boolean fetchDataTrains(String maGaDi, String maGaDen, String ngay) {
			clearData();
			updateCartStatus();
			List<Map<String, Object>> dsChuyen = mainTab.getDaoBanVe().timChuyenTau(maGaDi, maGaDen, ngay);
			if (dsChuyen.isEmpty())
				return false;

			bgTau = new ButtonGroup();
			boolean isFirst = true;
			for (Map<String, Object> chuyen : dsChuyen) {
				TrainSelectionCard btnTau = new TrainSelectionCard(chuyen.get("tenTau").toString(),
						chuyen.get("tgDi") != null ? chuyen.get("tgDi").toString() : "--:--",
						chuyen.get("tgDen") != null ? chuyen.get("tgDen").toString() : "--:--",
						chuyen.get("slDat").toString(), chuyen.get("slTrong").toString());
				if (isFirst)
					btnTau.setSelected(true);
				bgTau.add(btnTau);
				pnlTauList.add(btnTau);

				btnTau.addActionListener(e -> {
					currentMaLT = chuyen.get("maLT").toString();
					currentTenTau = chuyen.get("tenTau").toString();
					fetchDataToa(chuyen.get("maTau").toString());
				});
				if (isFirst) {
					isFirst = false;
					currentMaLT = chuyen.get("maLT").toString();
					currentTenTau = chuyen.get("tenTau").toString();
					fetchDataToa(chuyen.get("maTau").toString());
				}
			}
			revalidate();
			repaint();
			return true;
		}

		private void fetchDataToa(String maTau) {
			pnlTrainBar.removeAll();
			pnlSeatContent.removeAll();
			lblToaNameInfo.setText("");
			currentMaToa = "";

			List<Map<String, Object>> dsToa = mainTab.getDaoBanVe().getDanhSachToa(maTau);
			if (dsToa.isEmpty())
				return;

			TrainCarPanel loco = new TrainCarPanel(true, false, currentTenTau, new Color(41, 128, 185));
			loco.setToolTipText("Đầu máy: " + currentTenTau); // KHÔI PHỤC TOOLTIP ĐẦU MÁY
			pnlTrainBar.add(loco);

			boolean isFirst = true;
			int index = 1;
			for (Map<String, Object> toa : dsToa) {
				String tenLoaiToa = toa.get("tenLoaiToa").toString();
				Color carColor = new Color(93, 173, 226);
				if (tenLoaiToa.toLowerCase().contains("mềm"))
					carColor = new Color(231, 76, 60);
				if (tenLoaiToa.toLowerCase().contains("nằm"))
					carColor = new Color(162, 217, 40);

				TrainCarPanel carPanel = new TrainCarPanel(false, isFirst, String.valueOf(index), carColor);
				carPanel.setToolTipText("Toa số " + index + ": " + tenLoaiToa); // KHÔI PHỤC TOOLTIP TOA
				carPanel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						currentMaToa = toa.get("maToa").toString();
						currentTenToa = toa.get("tenToa").toString();
						for (Component c : pnlTrainBar.getComponents())
							if (c instanceof TrainCarPanel && !((TrainCarPanel) c).isLocomotive)
								((TrainCarPanel) c).setSelected(false);
						carPanel.setSelected(true);
						pnlTrainBar.repaint();
						lblToaNameInfo.setText("Toa số " + carPanel.labelText + ": " + tenLoaiToa);
						fetchDataGhe(currentMaLT, currentMaToa);
					}
				});
				pnlTrainBar.add(carPanel);
				if (isFirst) {
					isFirst = false;
					currentMaToa = toa.get("maToa").toString();
					currentTenToa = toa.get("tenToa").toString();
					lblToaNameInfo.setText("Toa số 1: " + tenLoaiToa);
					fetchDataGhe(currentMaLT, currentMaToa);
				}
				index++;
			}
			pnlTrainBar.revalidate();
			pnlTrainBar.repaint();
		}

		private void fetchDataGhe(String maLT, String maToa) {
			pnlSeatContent.removeAll();
			seatButtonsMap.clear();
			List<Map<String, Object>> dsGhe = mainTab.getDaoBanVe().getDanhSachGhe(maLT, maToa);
			for (Map<String, Object> g : dsGhe) {
				JToggleButton btn = createSeatBtn(g, maLT, maToa);
				seatButtonsMap.put(g.get("tenCho").toString(), btn);
			}
			Object[] thongTin = new com.dao.DAO_Toa().getThongTinToaForMap(maToa);
			if (thongTin != null) {
				int soHang = (int) thongTin[0];
				int soCot = (int) thongTin[1];
				String kieu = (String) thongTin[2];
				JPanel seatPanel = "GIUONG".equalsIgnoreCase(kieu) ? drawSleeperHorizontal(soHang, soCot)
						: drawSeaterHorizontal(soHang, soCot);
				pnlSeatContent.add(seatPanel);
			}
			pnlSeatContent.revalidate();
			pnlSeatContent.repaint();
			applySeatStatuses(maLT, maToa);
		}

		private JToggleButton createSeatBtn(Map<String, Object> gheInfo, String maLT, String maToa) {
			String tenCho = gheInfo.get("tenCho").toString();
			String maCho = gheInfo.get("maCho").toString();
			ModernSeatButton b = new ModernSeatButton(tenCho);

			Object giaObj = gheInfo.get("giaVe") != null ? gheInfo.get("giaVe") : gheInfo.get("gia");
			String rawGiaStr = "0";
			String giaHienThi = "Chưa cập nhật";

			if (giaObj != null) {
				rawGiaStr = giaObj.toString().split("\\.")[0].replaceAll("[^0-9]", "");
				try {
					long giaLong = Long.parseLong(rawGiaStr);
					giaHienThi = String.format("%,d đ", giaLong);
				} catch (Exception e) {
				}
			}

			// LƯU GIÁ VÀO NÚT ĐỂ HIỂN THỊ TOOLTIP
			b.putClientProperty("giaHienThi", giaHienThi);

			final String finalGiaStr = rawGiaStr;
			b.addActionListener(e -> {
				if (b.isSelected()) {
					Map<String, String> seatData = new HashMap<>();
					seatData.put("maLT", maLT);
					seatData.put("maCho", maCho);
					seatData.put("tenCho", tenCho);
					seatData.put("tenToa", currentTenToa);
					seatData.put("tenTau", currentTenTau);
					seatData.put("giaVe", finalGiaStr);
					seatData.put("maToa", maToa);
					seatData.put("viTriGhe", tenCho);
					seatData.put("chieu", chieu);
					mainTab.getSelectedSeatsData().add(seatData);
				} else {
					mainTab.getSelectedSeatsData()
							.removeIf(s -> s.get("maCho").equals(maCho) && s.get("maLT").equals(maLT));
				}
				updateCartStatus();
			});
			return b;
		}

		private void applySeatStatuses(String maLT, String maToa) {
			List<Map<String, Object>> dsGhe = mainTab.getDaoBanVe().getDanhSachGhe(maLT, maToa);
			for (Map<String, Object> g : dsGhe) {
				String tenCho = g.get("tenCho").toString();
				String trangThai = g.get("trangThai").toString();
				JToggleButton b = seatButtonsMap.get(tenCho);
				if (b != null) {
					boolean isSelectedInSession = mainTab.getSelectedSeatsData().stream().anyMatch(
							s -> s.get("maCho").equals(g.get("maCho").toString()) && s.get("maLT").equals(maLT));

					b.setEnabled(!trangThai.equals("DADAT") && !trangThai.equals("BAOTRI"));
					b.setSelected(isSelectedInSession);

					String statusText = "Còn trống";

					if (trangThai.equals("BAOTRI")) {
						b.setBackground(SEAT_MAINTENANCE_BG);
						statusText = "Đang bảo trì";
					} else if (trangThai.equals("DADAT")) {
						b.setBackground(SEAT_BOOKED_BG);
						statusText = "Đã bán";
					} else if (isSelectedInSession) {
						b.setBackground(SEAT_SELECTED_BG);
						statusText = "Đang chọn";
					} else {
						b.setBackground(SEAT_AVAILABLE_BG);
					}

					// KHÔI PHỤC TOOLTIP HIỆN GIÁ VÀ TRẠNG THÁI CHO GHẾ
					String giaHienThi = (String) b.getClientProperty("giaHienThi");
					if (giaHienThi == null)
						giaHienThi = "Chưa cập nhật";

					String htmlTooltip = "<html><body style='padding: 3px; font-family: Segoe UI;'><b style='font-size: 12px; color: #1A5EAB;'>Ghế số: "
							+ tenCho + "</b><br/><span style='font-size: 11px;'>Trạng thái: " + statusText
							+ "</span><br/>";
					if (!giaHienThi.equals("Chưa cập nhật"))
						htmlTooltip += "<span style='font-size: 11px;'>Giá vé: <b style='color: #E74C3C;'>" + giaHienThi
								+ "</b></span>";
					htmlTooltip += "</body></html>";
					b.setToolTipText(htmlTooltip);
				}
			}
		}

		// 1. HÀM VẼ SƠ ĐỒ GHẾ NGỒI (ĐÃ FIX LỖI)
		private JPanel drawSeaterHorizontal(int soHang, int soCot) {
			JPanel outer = new JPanel(new BorderLayout(8, 0));
			outer.setBackground(Color.WHITE);
			outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_CLR, 1, true),
					new EmptyBorder(10, 10, 10, 10)));
			int uiRows = soCot, uiCols = soHang, halfRows = Math.max(1, uiRows / 2);
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
						addSeatToGrid(gridBody, String.valueOf(seatNum)); // Đã fix
					}
				}
			}
			outer.add(new JLabel(" ĐẦU TÀU ", SwingConstants.CENTER), BorderLayout.WEST);
			outer.add(gridBody, BorderLayout.CENTER);
			outer.add(new JLabel(" CUỐI TÀU", SwingConstants.CENTER), BorderLayout.EAST);
			return outer;
		}

		// 2. HÀM VẼ SƠ ĐỒ GIƯỜNG NẰM (ĐÃ FIX LỖI)
		private JPanel drawSleeperHorizontal(int soHang, int soCot) {
			int khoang = Math.max(1, soHang);
			int soTang = soCot / 2;
			JPanel outer = new JPanel(new BorderLayout(8, 0));
			outer.setBackground(Color.WHITE);
			outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_CLR, 1, true),
					new EmptyBorder(10, 10, 10, 10)));

			JPanel pnlTangLabels = new JPanel(new GridLayout(soTang, 1, 2, 2));
			pnlTangLabels.setOpaque(false);
			pnlTangLabels.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 5));
			for (int tang = soTang - 1; tang >= 0; tang--) {
				JLabel lbl = new JLabel("Tầng " + (tang + 1), SwingConstants.RIGHT);
				lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
				lbl.setForeground(TEXT_MID);
				pnlTangLabels.add(lbl);
			}

			JPanel gridBody = new JPanel(new GridLayout(1, khoang, 8, 0));
			gridBody.setOpaque(false);
			int idx = 1;
			for (int k = 1; k <= khoang; k++) {
				JPanel kp = new JPanel(new BorderLayout(0, 2));
				kp.setBackground(new Color(0xF0F4FA));
				kp.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xDDE6F5), 1, true),
						new EmptyBorder(5, 5, 5, 5)));
				kp.add(new JLabel("Khoang " + k, SwingConstants.CENTER), BorderLayout.NORTH);
				JPanel grid = new JPanel(new GridLayout(soTang, 2, 4, 4));
				grid.setOpaque(false);
				for (int tang = soTang - 1; tang >= 0; tang--) {
					addSeatToGrid(grid, String.valueOf(idx + 2 * tang)); // Đã fix
					addSeatToGrid(grid, String.valueOf(idx + 2 * tang + 1)); // Đã fix
				}
				idx += soCot;
				kp.add(grid, BorderLayout.CENTER);
				gridBody.add(kp);
			}
			JPanel wrapper = new JPanel(new BorderLayout());
			wrapper.setOpaque(false);
			wrapper.add(pnlTangLabels, BorderLayout.WEST);
			wrapper.add(gridBody, BorderLayout.CENTER);
			outer.add(new JLabel(" ĐẦU TÀU "), BorderLayout.WEST);
			outer.add(wrapper, BorderLayout.CENTER);
			outer.add(new JLabel(" CUỐI TÀU"), BorderLayout.EAST);
			return outer;
		}

		// 3. HÀM BỔ SUNG ĐỂ HỖ TRỢ VIỆC VẼ GHẾ (HOẶC VẼ KHOẢNG TRỐNG)
		private void addSeatToGrid(JPanel grid, String seatNumStr) {
			JToggleButton btn = seatButtonsMap.get(seatNumStr);
			if (btn != null)
				grid.add(btn);
			else {
				JPanel empty = new JPanel();
				empty.setOpaque(false);
				grid.add(empty);
			}
		}
	}

	class ModernSeatButton extends JToggleButton {
		public ModernSeatButton(String text) {
			super(text);
			setPreferredSize(new Dimension(36, 32));
			setFocusPainted(false);
			setBorderPainted(false);
			setOpaque(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			setFont(new Font("Segoe UI", Font.BOLD, 12));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Color bg = isSelected() ? Color.WHITE : (isEnabled() ? SEAT_AVAILABLE_BG : getBackground());
			Color border = isSelected() ? SEAT_SELECTED_BG : (isEnabled() ? SEAT_AVAILABLE_BORDER : bg.darker());
			g2.setColor(bg);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
			g2.setColor(border);
			g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
			g2.setColor(isSelected() ? SEAT_SELECTED_BG : (isEnabled() ? TEXT_MID : Color.WHITE));
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
					(getHeight() - fm.getHeight()) / 2 + fm.getAscent());
			g2.dispose();
		}
	}

	class TrainSelectionCard extends JToggleButton {
		String tenTau, tgDi, tgDen, slDat, slTrong;

		public TrainSelectionCard(String tenTau, String tgDi, String tgDen, String slDat, String slTrong) {
			this.tenTau = tenTau;
			this.tgDi = tgDi;
			this.tgDen = tgDen;
			this.slDat = slDat;
			this.slTrong = slTrong;
			setPreferredSize(new Dimension(120, 120));
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Color mainColor = isSelected() ? ACCENT : TEXT_MID;
			g2.setColor(Color.WHITE);
			g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 18, 15, 15);
			g2.setColor(mainColor);
			g2.setStroke(new BasicStroke(2f));
			g2.drawRoundRect(3, 3, getWidth() - 6, getHeight() - 18, 15, 15);
			g2.fillRoundRect(3, 3, getWidth() - 6, 25, 15, 15);
			g2.fillRect(3, 15, getWidth() - 6, 13);
			g2.setColor(Color.WHITE);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(tenTau, (getWidth() - fm.stringWidth(tenTau)) / 2, 20);
			g2.setColor(TEXT_DARK);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
			g2.drawString("TG đi", 10, 42);
			g2.drawString("TG đến", 10, 55);
			g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
			g2.drawString(tgDi, 50, 42);
			g2.drawString(tgDen, 50, 55);
			g2.setColor(Color.GRAY);
			g2.drawString("SL Đặt", 15, 75);
			g2.drawString("SL Trống", 70, 75);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
			g2.setColor(TEXT_DARK);
			g2.drawString(slDat, 22, 95);
			g2.drawString(slTrong, 78, 95);
			g2.setColor(mainColor);
			g2.fillOval(20, getHeight() - 15, 16, 16);
			g2.fillOval(getWidth() - 36, getHeight() - 15, 16, 16);
			g2.dispose();
		}
	}

	class TrainCarPanel extends JPanel {
		boolean isLocomotive, isSelected;
		String labelText;
		Color carColor;

		public TrainCarPanel(boolean isLocomotive, boolean isSelected, String labelText, Color carColor) {
			this.isLocomotive = isLocomotive;
			this.isSelected = isSelected;
			this.labelText = labelText;
			this.carColor = carColor;
			setPreferredSize(new Dimension(60, 48));
			setOpaque(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		public void setSelected(boolean sel) {
			this.isSelected = sel;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth() - 4, h = 20, y = 2;
			g2.setColor(new Color(105, 110, 115));
			g2.fillOval(6, y + h - 3, 8, 8);
			g2.fillOval(w - 14, y + h - 3, 8, 8);
			g2.setStroke(new BasicStroke(2f));
			g2.drawLine(10, y + h + 1, w - 10, y + h + 1);
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
			}
			g2.setColor(isSelected ? new Color(211, 84, 0) : ACCENT);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(labelText, (getWidth() - fm.stringWidth(labelText)) / 2, y + h + 12);
			g2.dispose();
		}
	}
}