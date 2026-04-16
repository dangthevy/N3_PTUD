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
		setOpaque(false);
		add(UIHelper.createPageTitle("CHỌN CHUYẾN & GHẾ", ""), BorderLayout.NORTH);

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
		JLabel lblTitle, lblToaNameInfo;
		JPanel pnlTauList, pnlTrainBar, pnlSeatContent;
		ButtonGroup bgTau;
		String currentMaLT = "", currentTenTau = "", currentMaToa = "", currentTenToa = "";
		Map<String, JToggleButton> seatButtonsMap = new HashMap<>();

		public RoutePanel() {
			setLayout(new BorderLayout(0, 4));
			setOpaque(false);
			JPanel pnlHeader = new JPanel(new BorderLayout());
			pnlHeader.setOpaque(false);
			pnlHeader.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			lblTitle = new JLabel();
			lblTitle.setFont(UIHelper.F_H2);
			lblTitle.setForeground(UIHelper.ACCENT);
			pnlHeader.add(lblTitle, BorderLayout.WEST);

			JPanel pnlSelectionWrapper = UIHelper.makeCard(new BorderLayout(0, 4));
			pnlSelectionWrapper.setBorder(BorderFactory.createCompoundBorder(new UIHelper.ShadowBorder(),
					BorderFactory.createEmptyBorder(6, 10, 8, 10)));

			JPanel pnlTopControls = new JPanel(new BorderLayout(10, 0));
			pnlTopControls.setOpaque(false);
			JLabel lblChonTau = new JLabel(
					"<html><b style='color:#5A6A7D; font-size:12px;'>Chọn Tàu: &nbsp;</b></html>");
			lblChonTau.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
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
			JPanel pnlGheHeader = new JPanel(new BorderLayout());
			pnlGheHeader.setOpaque(false);
			JLabel lblSodoTitle = new JLabel("Sơ đồ tàu & ghế");
			lblSodoTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
			lblSodoTitle.setForeground(UIHelper.ACCENT);
			pnlGheHeader.add(lblSodoTitle, BorderLayout.WEST);

			JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
			pnlLegend.setOpaque(false);
			pnlLegend.add(legendItem(new Color(0x2ECC71), "Còn trống"));
			pnlLegend.add(legendItem(new Color(0xE74C3C), "Đã đặt"));
			pnlLegend.add(legendItem(new Color(0xF39C12), "Đang chọn"));
			pnlLegend.add(legendItem(Color.GRAY, "Bảo trì"));
			pnlGheHeader.add(pnlLegend, BorderLayout.EAST);
			pnlGheWrapper.add(pnlGheHeader, BorderLayout.NORTH);

			pnlTrainBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 4));
			pnlTrainBar.setBackground(Color.WHITE);
			JScrollPane scrollTrain = new JScrollPane(pnlTrainBar);
			scrollTrain.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 1, 0, UIHelper.BORDER),
					new EmptyBorder(0, 0, 0, 0)));
			scrollTrain.setPreferredSize(new Dimension(0, 55));
			scrollTrain.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

			JPanel pnlSeatArea = new JPanel(new BorderLayout());
			pnlSeatArea.setOpaque(false);
			lblToaNameInfo = new JLabel("Vui lòng chọn Toa", SwingConstants.CENTER);
			lblToaNameInfo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
			lblToaNameInfo.setForeground(new Color(0, 136, 204));
			lblToaNameInfo.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
			pnlSeatContent = new JPanel(new GridBagLayout());
			pnlSeatContent.setBackground(Color.WHITE);
			JScrollPane mapScroll = new JScrollPane(pnlSeatContent);
			mapScroll.setBorder(null);
			mapScroll.getViewport().setBackground(Color.WHITE);
			pnlSeatArea.add(lblToaNameInfo, BorderLayout.NORTH);
			pnlSeatArea.add(mapScroll, BorderLayout.CENTER);

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

		private JPanel legendItem(Color color, String label) {
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
			p.setOpaque(false);
			JLabel ic = new JLabel("  ");
			ic.setBackground(color);
			ic.setOpaque(true);
			ic.setPreferredSize(new Dimension(10, 10));
			JLabel tx = new JLabel(label);
			tx.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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
			List<Map<String, Object>> dsChuyen = mainTab.getDaoBanVe().timChuyenTau(maGaDi, maGaDen, ngay);
			if (dsChuyen.isEmpty())
				return false;

			bgTau = new ButtonGroup();
			boolean isFirst = true;
			for (Map<String, Object> chuyen : dsChuyen) {
				String maTau = chuyen.get("maTau").toString();
				String tenTau = chuyen.get("tenTau").toString();
				String maLT = chuyen.get("maLT").toString();
				String tgDi = chuyen.get("tgDi") != null ? chuyen.get("tgDi").toString() : "--:--";
				String tgDen = chuyen.get("tgDen") != null ? chuyen.get("tgDen").toString() : "--:--";
				String slDat = chuyen.get("slDat") != null ? chuyen.get("slDat").toString() : "0";
				String slTrong = chuyen.get("slTrong") != null ? chuyen.get("slTrong").toString() : "0";

				TrainSelectionCard btnTau = new TrainSelectionCard(tenTau, tgDi, tgDen, slDat, slTrong);
				if (isFirst)
					btnTau.setSelected(true);
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
			currentMaToa = "";
			pnlSeatContent.revalidate();
			pnlSeatContent.repaint();
			List<Map<String, Object>> dsToa = mainTab.getDaoBanVe().getDanhSachToa(maTau);
			if (dsToa.isEmpty()) {
				pnlTrainBar.revalidate();
				pnlTrainBar.repaint();
				return;
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
				String tenToaMoTa = "Toa số " + index + ": " + tenLoaiToa;
				Color carColor = new Color(93, 173, 226);
				if (tenLoaiToa.toLowerCase().contains("mềm"))
					carColor = new Color(231, 76, 60);
				if (tenLoaiToa.toLowerCase().contains("nằm"))
					carColor = new Color(162, 217, 40);

				TrainCarPanel carPanel = new TrainCarPanel(false, isFirst, String.valueOf(index), carColor);
				carPanel.setToolTipText(tenToaMoTa);
				carPanel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						currentMaToa = maToa;
						currentTenToa = tenToa;
						for (Component c : pnlTrainBar.getComponents())
							if (c instanceof TrainCarPanel && !((TrainCarPanel) c).isLocomotive)
								((TrainCarPanel) c).setSelected(false);
						carPanel.setSelected(true);
						pnlTrainBar.repaint();
						lblToaNameInfo.setText(tenToaMoTa);
						fetchDataGhe(currentMaLT, maToa);
					}
				});
				pnlTrainBar.add(carPanel);
				if (isFirst) {
					isFirst = false;
					currentMaToa = maToa;
					currentTenToa = tenToa;
					lblToaNameInfo.setText(tenToaMoTa);
					fetchDataGhe(currentMaLT, maToa);
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
				JToggleButton btn = createSeatBtn(g, maLT);
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

		private JToggleButton createSeatBtn(Map<String, Object> gheInfo, String maLT) {
			String tenCho = gheInfo.get("tenCho") != null ? gheInfo.get("tenCho").toString() : "";
			String maCho = gheInfo.get("maCho") != null ? gheInfo.get("maCho").toString() : "";
			String giaStr = "";
			String giaHienThi = "Chưa cập nhật";
			Object giaObj = gheInfo.get("giaVe");
			if (giaObj == null)
				giaObj = gheInfo.get("gia");
			if (giaObj != null && !giaObj.toString().isEmpty() && !giaObj.toString().equals("0")) {
				giaStr = giaObj.toString();
				try {
					long giaLong = (long) Double.parseDouble(giaStr);
					giaHienThi = String.format("%,d đ", giaLong);
				} catch (Exception e) {
				}
			}

			JToggleButton b = new JToggleButton(tenCho);
			b.setPreferredSize(new Dimension(32, 28));
			b.setFont(new Font("Segoe UI", Font.BOLD, 11));
			b.setMargin(new Insets(0, 0, 0, 0));
			b.setFocusPainted(false);
			b.setOpaque(true);
			b.putClientProperty("giaHienThi", giaHienThi);

			if (gheInfo == null)
				return b;
			String trangThai = gheInfo.get("trangThai").toString();
			boolean isDat = trangThai.equals("DADAT") || trangThai.equals("GIUCHO") || trangThai.equals("BAOTRI");
			b.setCursor(Cursor.getPredefinedCursor(isDat ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));

			final String finalGiaStr = giaStr;
			b.addActionListener(e -> {
				if (b.isSelected()) {
					b.setBackground(new Color(0xF39C12));
					b.setForeground(Color.WHITE);
					b.setBorder(BorderFactory.createLineBorder(new Color(0xF39C12).darker(), 1));
					Map<String, String> seatData = new HashMap<>();
					seatData.put("maLT", maLT);
					seatData.put("maCho", maCho);
					seatData.put("tenCho", tenCho);
					seatData.put("tenToa", currentTenToa);
					seatData.put("tenTau", currentTenTau);
					seatData.put("giaVe", finalGiaStr);
					mainTab.getSelectedSeatsData().add(seatData);
				} else {
					b.setBackground(new Color(0x2ECC71));
					b.setForeground(Color.WHITE);
					b.setBorder(BorderFactory.createLineBorder(new Color(0x2ECC71).darker(), 1));
					mainTab.getSelectedSeatsData()
							.removeIf(s -> s.get("maCho").equals(maCho) && s.get("maLT").equals(maLT));
				}
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
					boolean isDat = trangThai.equals("DADAT") || trangThai.equals("GIUCHO");
					boolean isBaoTri = trangThai.equals("BAOTRI");
					boolean isAlreadySelectedInSession = mainTab.getSelectedSeatsData().stream().anyMatch(
							s -> s.get("maCho").equals(g.get("maCho").toString()) && s.get("maLT").equals(maLT));

					// [VÁ LỖI GIỎ HÀNG MA]: Dọn ghế bị người khác hốt
					if (isDat || isBaoTri) {
						mainTab.getSelectedSeatsData().removeIf(
								s -> s.get("maCho").equals(g.get("maCho").toString()) && s.get("maLT").equals(maLT));
						isAlreadySelectedInSession = false;
					}

					String giaHienThi = (String) b.getClientProperty("giaHienThi");
					if (giaHienThi == null)
						giaHienThi = "Chưa cập nhật";

					String statusText = isDat ? "Đã bán" : (isBaoTri ? "Đang bảo trì" : "Còn trống");
					String htmlTooltip = "<html><body style='padding: 3px; font-family: Segoe UI;'><b style='font-size: 12px; color: #1A5EAB;'>Ghế số: "
							+ tenCho + "</b><br/><span style='font-size: 11px;'>Trạng thái: " + statusText
							+ "</span><br/>";
					if (!giaHienThi.equals("Chưa cập nhật"))
						htmlTooltip += "<span style='font-size: 11px;'>Giá vé: <b style='color: #E74C3C;'>" + giaHienThi
								+ "</b></span>";
					htmlTooltip += "</body></html>";
					b.setToolTipText(htmlTooltip);

					b.setCursor(Cursor
							.getPredefinedCursor((isDat || isBaoTri) ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
					if (isBaoTri) {
						b.setBackground(Color.GRAY);
						b.setForeground(Color.WHITE);
						b.setEnabled(false);
						b.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
					} else if (isDat) {
						b.setBackground(new Color(0xE74C3C));
						b.setForeground(Color.WHITE);
						b.setEnabled(false);
						b.setBorder(BorderFactory.createLineBorder(new Color(0xC0392B), 1));
					} else {
						b.setEnabled(true);
						if (isAlreadySelectedInSession) {
							b.setBackground(new Color(0xF39C12));
							b.setForeground(Color.WHITE);
							b.setSelected(true);
							b.setBorder(BorderFactory.createLineBorder(new Color(0xF39C12).darker(), 1));
						} else {
							b.setBackground(new Color(0x2ECC71));
							b.setForeground(Color.WHITE);
							b.setSelected(false);
							b.setBorder(BorderFactory.createLineBorder(new Color(0x2ECC71).darker(), 1));
						}
					}
				}
			}
		}

		private JPanel drawSeaterHorizontal(int soHang, int soCot) {
			JPanel outer = new JPanel(new BorderLayout(8, 0));
			outer.setBackground(new Color(0xFAFCFF));
			outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(160, 174, 192), 1, true),
					new EmptyBorder(4, 4, 4, 4)));
			JLabel capA = new JLabel(" ĐẦU TÀU ");
			capA.setFont(new Font("Segoe UI", Font.BOLD, 11));
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
						JToggleButton btn = seatButtonsMap.get(String.valueOf(seatNum));
						if (btn != null)
							gridBody.add(btn);
						else {
							JPanel empty = new JPanel();
							empty.setOpaque(false);
							gridBody.add(empty);
						}
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

		private JPanel drawSleeperHorizontal(int soHang, int soCot) {
			int khoang = Math.max(1, (soHang * soCot) / 4);
			JPanel outer = new JPanel(new BorderLayout(8, 0));
			outer.setBackground(new Color(0xFAFCFF));
			outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(160, 174, 192), 1, true),
					BorderFactory.createEmptyBorder(4, 4, 4, 4)));
			JLabel capA = new JLabel(" ĐẦU TÀU ");
			capA.setFont(new Font("Segoe UI", Font.BOLD, 11));
			capA.setForeground(new Color(0x7F8C8D));
			JLabel capB = new JLabel(" CUỐI TÀU");
			capB.setFont(new Font("Segoe UI", Font.BOLD, 11));
			capB.setForeground(new Color(0x7F8C8D));

			JPanel gridBody = new JPanel(new GridLayout(1, khoang, 4, 0));
			gridBody.setOpaque(false);
			int idx = 1;
			for (int k = 1; k <= khoang; k++) {
				JPanel kp = new JPanel(new BorderLayout(0, 2));
				kp.setBackground(new Color(0xF0F4FA));
				kp.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xDDE6F5), 1, true),
						BorderFactory.createEmptyBorder(2, 2, 2, 2)));
				JLabel lbl = new JLabel("Khoang " + k, SwingConstants.CENTER);
				lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
				lbl.setForeground(new Color(0x7F8C8D));
				kp.add(lbl, BorderLayout.NORTH);
				JPanel grid = new JPanel(new GridLayout(2, 2, 2, 2));
				grid.setOpaque(false);
				addSeatToGrid(grid, String.valueOf(idx + 2));
				addSeatToGrid(grid, String.valueOf(idx + 3));
				addSeatToGrid(grid, String.valueOf(idx));
				addSeatToGrid(grid, String.valueOf(idx + 1));
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
			boolean sel = isSelected();
			Color mainColor = sel ? new Color(0, 136, 204) : new Color(149, 153, 157);
			int w = getWidth();
			int h = getHeight() - 15;
			g2.setColor(Color.WHITE);
			g2.fillRoundRect(3, 3, w - 6, h - 3, 15, 15);
			g2.setColor(mainColor);
			g2.setStroke(new BasicStroke(2f));
			g2.drawRoundRect(3, 3, w - 6, h - 3, 15, 15);
			g2.fillRoundRect(3, 3, w - 6, 25, 15, 15);
			g2.fillRect(3, 15, w - 6, 13);
			g2.setColor(Color.WHITE);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(tenTau, (w - fm.stringWidth(tenTau)) / 2, 20);
			g2.setColor(Color.BLACK);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
			g2.drawString("TG đi", 10, 42);
			g2.drawString("TG đến", 10, 55);
			g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
			g2.drawString(tgDi, 50, 42);
			g2.drawString(tgDen, 50, 55);
			g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
			g2.setColor(Color.GRAY);
			g2.drawString("SL chỗ đặt", 10, 75);
			g2.drawString("SL chỗ trống", 55, 75);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
			g2.setColor(Color.BLACK);
			g2.drawString(slDat, 20, 95);
			g2.drawString(slTrong, 75, 95);
			g2.setColor(mainColor);
			g2.fillOval(20, h, 16, 16);
			g2.fillOval(w - 36, h, 16, 16);
			g2.setColor(Color.WHITE);
			g2.fillOval(24, h + 4, 8, 8);
			g2.fillOval(w - 32, h + 4, 8, 8);
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(3f));
			g2.drawLine(8, h + 13, w - 8, h + 13);
			g2.drawLine(25, h + 5, 12, h + 13);
			g2.drawLine(w - 25, h + 5, w - 12, h + 13);
			g2.dispose();
		}
	}

	class TrainCarPanel extends JPanel {
		private boolean isLocomotive, isSelected;
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
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth() - 4;
			int h = 20;
			int y = 2;
			g2.setColor(new Color(105, 110, 115));
			g2.fillOval(6, y + h - 3, 8, 8);
			g2.fillOval(w - 14, y + h - 3, 8, 8);
			g2.setStroke(new BasicStroke(2f));
			g2.drawLine(10, y + h + 1, w - 10, y + h + 1);
			if (!isLocomotive)
				g2.fillRect(w, y + h - 6, 4, 2);
			g2.setColor(isSelected ? new Color(241, 196, 15) : carColor);
			if (isLocomotive) {
				int[] px = { 0, w - 10, w, w, 0 };
				int[] py = { y, y, y + 10, y + h, y + h };
				g2.fillPolygon(px, py, 5);
				g2.setColor(Color.WHITE);
				g2.fillRect(w - 18, y + 3, 6, 8);
				g2.fillRect(w - 10, y + 5, 4, 6);
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
			g2.setColor(isSelected ? new Color(211, 84, 0) : UIHelper.ACCENT);
			g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
			FontMetrics fm = g2.getFontMetrics();
			int tx = (getWidth() - fm.stringWidth(labelText)) / 2;
			g2.drawString(labelText, tx, y + h + 12);
			g2.dispose();
		}
	}
}