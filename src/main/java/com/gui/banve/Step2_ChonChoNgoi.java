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

	public Step2_ChonChoNgoi(TAB_BanVe mainTab) {
		this.mainTab = mainTab;
		initUI();
	}

	private void initUI() {
		setLayout(new BorderLayout());
		setOpaque(false);
		add(UIHelper.createPageTitle("CHỌN CHUYẾN & GHẾ", "Lựa chọn chuyến tàu và vị trí ghế ngồi phù hợp với bạn"),
				BorderLayout.NORTH);

		JPanel pnl = new JPanel(new BorderLayout(0, 10)); // Giảm gap
		pnl.setOpaque(false);

		pnlDirectionToggle = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		pnlDirectionToggle.setOpaque(false);

		btnChieuDi = UIHelper.createSelectionTab("CHIỀU ĐI", true);
		btnChieuVe = UIHelper.createSelectionTab("CHIỀU VỀ", false);
		btnChieuDi.setPreferredSize(new Dimension(200, 40));
		btnChieuVe.setPreferredSize(new Dimension(200, 40));

		ButtonGroup bgDir = new ButtonGroup();
		bgDir.add(btnChieuDi);
		bgDir.add(btnChieuVe);

		pnlDirectionToggle.add(btnChieuDi);
		pnlDirectionToggle.add(btnChieuVe);

		routeCardLayout = new CardLayout();
		pnlRouteCards = new JPanel(routeCardLayout);
		pnlRouteCards.setOpaque(false);

		pnlOutbound = new RoutePanel();
		pnlReturn = new RoutePanel();

		pnlRouteCards.add(pnlOutbound, "OUTBOUND");
		pnlRouteCards.add(pnlReturn, "RETURN");

		btnChieuDi.addActionListener(e -> routeCardLayout.show(pnlRouteCards, "OUTBOUND"));
		btnChieuVe.addActionListener(e -> routeCardLayout.show(pnlRouteCards, "RETURN"));

		pnl.add(pnlDirectionToggle, BorderLayout.NORTH);
		pnl.add(pnlRouteCards, BorderLayout.CENTER);

		add(pnl, BorderLayout.CENTER);
	}

	public boolean loadTrainData(String maGaDi, String maGaDen, String sqlNgayDi, boolean isRoundTrip, String strNgayVe,
			String tenGaDi, String tenGaDen) {
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
			} catch (Exception e) {}

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

	// =========================================================================
	// INNER CLASS xử lý Tàu và Ghế
	// =========================================================================
	private class RoutePanel extends JPanel {
		JLabel lblTitle;
		JPanel pnlTauList, pnlTrainBar, pnlSeatContent;
		JLabel lblToaNameInfo; 
		ButtonGroup bgTau;
		String currentMaLT = "", currentTenTau = "", currentTenToa = "";

		public RoutePanel() {
			setLayout(new BorderLayout(0, 5)); // Giảm padding dọc
			setOpaque(false);

			JPanel pnlHeader = new JPanel(new BorderLayout());
			pnlHeader.setOpaque(false);
			pnlHeader.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			lblTitle = new JLabel();
			lblTitle.setFont(UIHelper.F_H2); // Chuyển từ H1 xuống H2 cho gọn
			lblTitle.setForeground(UIHelper.ACCENT);
			pnlHeader.add(lblTitle, BorderLayout.WEST);

			JPanel pnlSelectionWrapper = UIHelper.makeCard(new BorderLayout(0, 5));
			pnlSelectionWrapper.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(),
					BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Giảm từ 15 xuống 10

			// 1. CHỌN TÀU
			JPanel pnlTopControls = new JPanel(new BorderLayout(10, 0));
			pnlTopControls.setOpaque(false);
			JLabel lblChonTau = new JLabel("<html><b style='color:#5A6A7D'>Chọn Tàu: &nbsp;</b></html>");
			lblChonTau.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			pnlTopControls.add(lblChonTau, BorderLayout.WEST);
			
			pnlTauList = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
			pnlTauList.setOpaque(false);
			JScrollPane scrollTau = new JScrollPane(pnlTauList);
			scrollTau.setBorder(null); scrollTau.setOpaque(false); scrollTau.getViewport().setOpaque(false);
			scrollTau.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			pnlTopControls.add(scrollTau, BorderLayout.CENTER);

			// 2. SƠ ĐỒ TÀU & GHẾ
			JPanel pnlGheWrapper = new JPanel(new BorderLayout());
			pnlGheWrapper.setOpaque(false);
			pnlGheWrapper.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIHelper.BORDER),
					"Sơ đồ tàu & ghế", 0, 0, UIHelper.F_LABEL, UIHelper.ACCENT));

			JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); // Giảm padding
			pnlLegend.setOpaque(false);
			pnlLegend.add(legendItem(new Color(0x2ECC71), "Còn trống"));
			pnlLegend.add(legendItem(new Color(0xE74C3C), "Đã đặt"));
			pnlLegend.add(legendItem(new Color(0xF39C12), "Đang chọn"));
			pnlGheWrapper.add(pnlLegend, BorderLayout.NORTH);

			pnlTrainBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 5));
			pnlTrainBar.setBackground(Color.WHITE);
			JScrollPane scrollTrain = new JScrollPane(pnlTrainBar);
			scrollTrain.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 1, 0, UIHelper.BORDER),
					new EmptyBorder(0, 0, 0, 0)));
			scrollTrain.setPreferredSize(new Dimension(0, 70)); // Thu nhỏ thanh tàu từ 80 -> 70
			scrollTrain.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

			// Vùng hiển thị Tên Toa và Lưới Ghế
			JPanel pnlSeatArea = new JPanel(new BorderLayout());
			pnlSeatArea.setOpaque(false);
			
			lblToaNameInfo = new JLabel("Vui lòng chọn Toa", SwingConstants.CENTER);
			lblToaNameInfo.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Giảm từ 20 xuống 16
			lblToaNameInfo.setForeground(new Color(0, 136, 204));
			lblToaNameInfo.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Giảm khoảng cách tên toa

			pnlSeatContent = new JPanel(new GridBagLayout());
			pnlSeatContent.setBackground(Color.WHITE);
			JScrollPane mapScroll = new JScrollPane(pnlSeatContent);
			mapScroll.setBorder(null);
			mapScroll.getViewport().setBackground(Color.WHITE);
			
			pnlSeatArea.add(lblToaNameInfo, BorderLayout.NORTH);
			pnlSeatArea.add(mapScroll, BorderLayout.CENTER);

			JPanel pnlMapWrapper = new JPanel(new BorderLayout(0, 2)); // Chỉnh 8 -> 2
			pnlMapWrapper.setOpaque(false);
			pnlMapWrapper.add(scrollTrain, BorderLayout.NORTH);
			pnlMapWrapper.add(pnlSeatArea, BorderLayout.CENTER);

			pnlGheWrapper.add(pnlMapWrapper, BorderLayout.CENTER);

			pnlSelectionWrapper.add(pnlTopControls, BorderLayout.NORTH);
			pnlSelectionWrapper.add(pnlGheWrapper, BorderLayout.CENTER);
			add(pnlHeader, BorderLayout.NORTH);
			add(pnlSelectionWrapper, BorderLayout.CENTER);
		}

		private JPanel legendItem(Color color, String label) {
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
			p.setOpaque(false);
			JLabel ic = new JLabel("  ");
			ic.setBackground(color);
			ic.setOpaque(true);
			ic.setPreferredSize(new Dimension(12, 12));
			JLabel tx = new JLabel(label);
			tx.setFont(new Font("Segoe UI", Font.PLAIN, 12));
			tx.setForeground(UIHelper.TEXT_MID);
			p.add(ic);
			p.add(tx);
			return p;
		}

		public void clearData() {
			pnlTauList.removeAll();
			pnlTrainBar.removeAll();
			pnlSeatContent.removeAll();
			lblToaNameInfo.setText("");
			revalidate();
			repaint();
		}

		public boolean fetchDataTrains(String maGaDi, String maGaDen, String ngay) {
			clearData();
			List<Map<String, Object>> dsChuyen = mainTab.getDaoBanVe().timChuyenTau(maGaDi, maGaDen, ngay);
			if (dsChuyen.isEmpty()) return false;

			bgTau = new ButtonGroup();
			boolean isFirst = true;
			for (Map<String, Object> chuyen : dsChuyen) {
				String maTau = chuyen.get("maTau").toString();
				String tenTau = chuyen.get("tenTau").toString();
				String maLT = chuyen.get("maLT").toString();
				String tgDi = chuyen.get("tgDi").toString();
				String tgDen = chuyen.get("tgDen").toString();
				String slDat = chuyen.get("slDat").toString();
				String slTrong = chuyen.get("slTrong").toString();

				// Thẻ Chọn Tàu Đã Được Căn Chỉnh Gọn Hơn
				TrainSelectionCard btnTau = new TrainSelectionCard(tenTau, tgDi, tgDen, slDat, slTrong);
				if (isFirst) btnTau.setSelected(true);
				
				bgTau.add(btnTau);
				pnlTauList.add(btnTau);

				btnTau.addActionListener(e -> {
					currentMaLT = maLT;
					currentTenTau = tenTau;
					fetchDataToa(maTau);
				});

				if (isFirst) {
					isFirst = false;
					currentMaLT = maLT;
					currentTenTau = tenTau;
					fetchDataToa(maTau);
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
			pnlSeatContent.revalidate();
			pnlSeatContent.repaint();

			List<Map<String, Object>> dsToa = mainTab.getDaoBanVe().getDanhSachToa(maTau);
			if (dsToa.isEmpty()) {
				pnlTrainBar.revalidate(); pnlTrainBar.repaint(); return;
			}

			TrainCarPanel loco = new TrainCarPanel(true, false, currentTenTau, new Color(41, 128, 185));
			loco.setToolTipText("Đầu máy: " + currentTenTau);
			pnlTrainBar.add(loco);

			boolean isFirst = true;
			int index = 1;
			for (Map<String, Object> toa : dsToa) {
				String maToa = toa.get("maToa").toString();
				String tenToa = toa.get("tenToa").toString();
				String tenLoaiToa = toa.get("tenLoaiToa").toString();
				String tenToaMoTa = tenToa + " (" + tenLoaiToa + ")";

				Color carColor = new Color(93, 173, 226); 
				if (tenLoaiToa.toLowerCase().contains("mềm")) carColor = new Color(231, 76, 60);
				if (tenLoaiToa.toLowerCase().contains("nằm")) carColor = new Color(162, 217, 40);

				TrainCarPanel carPanel = new TrainCarPanel(false, isFirst, String.valueOf(index), carColor);
				carPanel.setToolTipText("Toa số " + index + ": " + tenToaMoTa);
				
				int finalIndex = index;
				carPanel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						currentTenToa = tenToa;
						for (Component c : pnlTrainBar.getComponents()) {
							if (c instanceof TrainCarPanel && !((TrainCarPanel) c).isLocomotive) {
								((TrainCarPanel) c).setSelected(false);
							}
						}
						carPanel.setSelected(true);
						pnlTrainBar.repaint();
						
						lblToaNameInfo.setText("Toa số " + finalIndex + ": " + tenToaMoTa);
						fetchDataGhe(currentMaLT, maToa);
					}
				});
				pnlTrainBar.add(carPanel);
				if (isFirst) {
					isFirst = false;
					currentTenToa = tenToa;
					lblToaNameInfo.setText("Toa số " + finalIndex + ": " + tenToaMoTa);
					fetchDataGhe(currentMaLT, maToa);
				}
				index++;
			}
			pnlTrainBar.revalidate();
			pnlTrainBar.repaint();
		}

		private void fetchDataGhe(String maLT, String maToa) {
			pnlSeatContent.removeAll();
			List<Map<String, Object>> dsGhe = mainTab.getDaoBanVe().getDanhSachGhe(maLT, maToa);

			Map<String, Map<String, Object>> seatDataMap = new HashMap<>();
			for (Map<String, Object> g : dsGhe)
				seatDataMap.put(g.get("tenCho").toString(), g);

			Object[] thongTin = new com.dao.DAO_Toa().getThongTinToaForMap(maToa);
			if (thongTin != null) {
				int soHang = (int) thongTin[0];
				int soCot = (int) thongTin[1];
				String kieu = (String) thongTin[2];

				JPanel seatPanel = "GIUONG".equalsIgnoreCase(kieu) ? drawSleeperHorizontal(soHang, soCot, seatDataMap)
						: drawSeaterHorizontal(soHang, soCot, seatDataMap);
				pnlSeatContent.add(seatPanel);
			}
			pnlSeatContent.revalidate();
			pnlSeatContent.repaint();
		}

		// =====================================================================
		// THUẬT TOÁN VẼ SƠ ĐỒ NGANG & NÚT GHẾ TƯƠNG TÁC (ĐÃ THU NHỎ SIZE)
		// =====================================================================
		private JPanel drawSeaterHorizontal(int soHang, int soCot, Map<String, Map<String, Object>> seatDataMap) {
			JPanel outer = new JPanel(new BorderLayout(10, 0));
			outer.setBackground(new Color(0xFAFCFF));
			outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(52, 73, 94), 2, true),
					new EmptyBorder(8, 8, 8, 8))); // Giảm padding
			JLabel capA = new JLabel(" ĐẦU TÀU ");
			capA.setFont(new Font("Segoe UI", Font.BOLD, 13));
			capA.setForeground(new Color(0x7F8C8D));
			JLabel capB = new JLabel(" CUỐI TÀU");
			capB.setFont(new Font("Segoe UI", Font.BOLD, 13));
			capB.setForeground(new Color(0x7F8C8D));

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
						gridBody.add(seatBtn(seatNum, seatDataMap));
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

		private JPanel drawSleeperHorizontal(int soHang, int soCot, Map<String, Map<String, Object>> seatDataMap) {
			int khoang = Math.max(1, (soHang * soCot) / 4);
			JPanel outer = new JPanel(new BorderLayout(10, 0));
			outer.setBackground(new Color(0xFAFCFF));
			outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(52, 73, 94), 2, true),
					BorderFactory.createEmptyBorder(8, 8, 8, 8)));
			JLabel capA = new JLabel(" ĐẦU TÀU ");
			capA.setFont(new Font("Segoe UI", Font.BOLD, 13));
			capA.setForeground(new Color(0x7F8C8D));
			JLabel capB = new JLabel(" CUỐI TÀU");
			capB.setFont(new Font("Segoe UI", Font.BOLD, 13));
			capB.setForeground(new Color(0x7F8C8D));

			JPanel gridBody = new JPanel(new GridLayout(1, khoang, 6, 0));
			gridBody.setOpaque(false);
			int idx = 1;
			for (int k = 1; k <= khoang; k++) {
				JPanel kp = new JPanel(new BorderLayout(0, 4));
				kp.setBackground(new Color(0xF0F4FA));
				kp.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xDDE6F5), 1, true),
						BorderFactory.createEmptyBorder(4, 4, 4, 4)));
				JLabel lbl = new JLabel("Khoang " + k, SwingConstants.CENTER);
				lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
				lbl.setForeground(new Color(0x7F8C8D));
				kp.add(lbl, BorderLayout.NORTH);
				JPanel grid = new JPanel(new GridLayout(2, 2, 4, 4));
				grid.setOpaque(false);
				grid.add(seatBtn(idx + 2, seatDataMap));
				grid.add(seatBtn(idx + 3, seatDataMap));
				grid.add(seatBtn(idx, seatDataMap));
				grid.add(seatBtn(idx + 1, seatDataMap));
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

		private JToggleButton seatBtn(int num, Map<String, Map<String, Object>> seatDataMap) {
			String numStr = String.valueOf(num);
			Map<String, Object> gheInfo = seatDataMap.get(numStr);
			
			JToggleButton b = new JToggleButton(numStr);
			// Thu gọn size nút từ (36, 32) xuống (32, 28)
			b.setPreferredSize(new Dimension(32, 28));
			b.setFont(new Font("Segoe UI", Font.BOLD, 10));
			b.setMargin(new Insets(0, 0, 0, 0));
			b.setFocusPainted(false);
			b.setOpaque(true);

			if (gheInfo == null) return b;

			String maCho = gheInfo.get("maCho").toString();
			String tenCho = gheInfo.get("tenCho").toString();
			String trangThai = gheInfo.get("trangThai").toString();

			boolean isDat = trangThai.equals("DADAT") || trangThai.equals("GIUCHO") || trangThai.equals("BAOTRI");
			boolean isAlreadySelectedInSession = mainTab.getSelectedSeatsData().stream()
					.anyMatch(s -> s.get("maCho").equals(maCho) && s.get("maLT").equals(currentMaLT));

			b.setCursor(Cursor.getPredefinedCursor(isDat ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));

			Color C_TRONG = new Color(0x2ECC71); Color C_DAT = new Color(0xE74C3C); Color C_CHON = new Color(0xF39C12);

			if (isDat) {
				b.setBackground(C_DAT); b.setForeground(Color.WHITE); b.setEnabled(false);
				b.setBorder(BorderFactory.createLineBorder(C_DAT.darker(), 1));
			} else {
				if (isAlreadySelectedInSession) {
					b.setBackground(C_CHON); b.setForeground(Color.WHITE); b.setSelected(true);
					b.setBorder(BorderFactory.createLineBorder(C_CHON.darker(), 1));
				} else {
					b.setBackground(C_TRONG); b.setForeground(Color.WHITE);
					b.setBorder(BorderFactory.createLineBorder(C_TRONG.darker(), 1));
				}

				b.addItemListener(e -> {
					if (b.isSelected()) {
						b.setBackground(C_CHON); b.setForeground(Color.WHITE); b.setBorder(BorderFactory.createLineBorder(C_CHON.darker(), 1));
						Map<String, String> seatData = new HashMap<>();
						seatData.put("maLT", currentMaLT);
						seatData.put("maCho", maCho);
						seatData.put("tenCho", tenCho);
						seatData.put("tenToa", currentTenToa);
						seatData.put("tenTau", currentTenTau);
						mainTab.getSelectedSeatsData().add(seatData);
					} else {
						b.setBackground(C_TRONG); b.setForeground(Color.WHITE); b.setBorder(BorderFactory.createLineBorder(C_TRONG.darker(), 1));
						mainTab.getSelectedSeatsData()
								.removeIf(s -> s.get("maCho").equals(maCho) && s.get("maLT").equals(currentMaLT));
					}
				});
			}
			return b;
		}
	}

	// =========================================================================
	// LỚP VẼ HÌNH CARD CHỌN TÀU (ĐÃ THU GỌN CHIỀU CAO XUỐNG 135px)
	// =========================================================================
	class TrainSelectionCard extends JToggleButton {
		String tenTau, tgDi, tgDen, slDat, slTrong;

		public TrainSelectionCard(String tenTau, String tgDi, String tgDen, String slDat, String slTrong) {
			this.tenTau = tenTau;
			this.tgDi = tgDi;
			this.tgDen = tgDen;
			this.slDat = slDat;
			this.slTrong = slTrong;
			setPreferredSize(new Dimension(130, 135)); // Ép nhỏ lại cho vừa khung nhìn
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			boolean sel = isSelected();
			Color mainColor = sel ? new Color(0, 136, 204) : new Color(149, 153, 157);
			int w = getWidth();
			int h = getHeight() - 15; // Còn 120px cho thân, 15px cho bánh

			// Thân
			g2.setColor(Color.WHITE);
			g2.fillRoundRect(5, 5, w - 10, h - 5, 20, 20);
			g2.setColor(mainColor);
			g2.setStroke(new BasicStroke(2f));
			g2.drawRoundRect(5, 5, w - 10, h - 5, 20, 20);

			// Header Banner
			g2.fillRoundRect(5, 5, w - 10, 25, 20, 20);
			g2.fillRect(5, 15, w - 10, 15);

			// Tên Tàu
			g2.setColor(Color.WHITE);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(tenTau, (w - fm.stringWidth(tenTau)) / 2, 22);

			// Nội dung chữ Text
			g2.setColor(Color.BLACK);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
			g2.drawString("TG đi", 12, 48);
			g2.drawString("TG đến", 12, 63);

			g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
			g2.drawString(tgDi, 55, 48);
			g2.drawString(tgDen, 55, 63);

			g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
			g2.setColor(Color.GRAY);
			g2.drawString("SL chỗ đặt", 12, 85);
			g2.drawString("SL chỗ trống", 65, 85);

			// Số liệu
			g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
			g2.setColor(Color.BLACK);
			g2.drawString(slDat, 25, 105);
			g2.drawString(slTrong, 85, 105);

			// Vẽ Bánh xe & Đường ray
			g2.setColor(mainColor);
			g2.fillOval(25, h, 18, 18);
			g2.fillOval(w - 43, h, 18, 18);
			g2.setColor(Color.WHITE);
			g2.fillOval(30, h + 4, 8, 8);
			g2.fillOval(w - 38, h + 4, 8, 8);

			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(3f));
			g2.drawLine(10, h + 15, w - 10, h + 15);
			g2.drawLine(30, h + 5, 15, h + 15);
			g2.drawLine(w - 30, h + 5, w - 15, h + 15);

			g2.dispose();
		}
	}

	// =========================================================================
	// LỚP VẼ HÌNH TOA TÀU NHỎ BÊN DƯỚI
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
			setPreferredSize(new Dimension(70, 50)); // Thu gọn nhẹ
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
			int w = getWidth() - 6;
			int h = 26;
			int y = 2;

			g2.setColor(new Color(105, 110, 115));
			g2.fillOval(8, y + h - 3, 10, 10);
			g2.fillOval(w - 18, y + h - 3, 10, 10);
			g2.setStroke(new BasicStroke(3f));
			g2.drawLine(12, y + h + 2, w - 12, y + h + 2);
			if (!isLocomotive) {
				g2.fillRect(w, y + h - 8, 6, 3);
			}

			g2.setColor(isSelected ? new Color(241, 196, 15) : carColor);
			if (isLocomotive) {
				int[] px = { 0, w - 12, w, w, 0 };
				int[] py = { y, y, y + 12, y + h, y + h };
				g2.fillPolygon(px, py, 5);
				g2.setColor(Color.WHITE);
				g2.fillRect(w - 24, y + 4, 8, 10);
				g2.fillRect(w - 12, y + 6, 6, 8);
			} else {
				g2.fillRoundRect(0, y, w, h, 8, 8);
				g2.setColor(Color.WHITE);
				int winW = (w - 20) / 3;
				g2.fillRect(5, y + 6, winW, 8);
				g2.fillRect(10 + winW, y + 6, winW, 8);
				g2.fillRect(15 + winW * 2, y + 6, winW, 8);
				g2.setColor(new Color(0, 0, 0, 40));
				g2.fillRect(0, y + h - 6, w, 3);
			}

			g2.setColor(isSelected ? new Color(211, 84, 0) : UIHelper.ACCENT);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
			FontMetrics fm = g2.getFontMetrics();
			int tx = (getWidth() - fm.stringWidth(labelText)) / 2;
			g2.drawString(labelText, tx, y + h + 18);
			g2.dispose();
		}
	}
}