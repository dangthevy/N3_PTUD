package com.gui.banve;

import com.connectDB.ConnectDB;
import com.dao.DAO_BanVe;
import com.entities.NhanVien;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TAB_BanVe extends JPanel {
	private CardLayout cardLayout;
	private JPanel pnlCards;
	private StepProgressPanel stepProgress;
	private JButton btnBack, btnNext;
	private int currentStep = 0;
	private final String[] STEP_NAMES = { "Tìm kiếm", "Chọn chuyến & ghế", "Thông tin KH", "Thanh toán", "Hoàn tất" };
	private Timer holdTimer;
	private int timeLeft = 900;

	// === DỮ LIỆU DÙNG CHUNG ===
	private DAO_BanVe daoBanVe = new DAO_BanVe();
	private List<Map<String, String>> selectedSeatsData = new ArrayList<>();
	private Map<String, Map<String, String>> passengerDataMap = new HashMap<>();
	private boolean isRoundTrip = false;

	// Thông tin nhân viên trực quầy
	private NhanVien nhanVienHienTai;

	public void setNhanVien(NhanVien nv) {
		this.nhanVienHienTai = nv;
	}

	public NhanVien getNhanVienHienTai() {
		return nhanVienHienTai;
	}

	// === CÁC PANEL CON ===
	private Step1_TimKiem step1;
	private Step2_ChonChoNgoi step2;
	private Step3_NhapThongTinKH step3;
	private Step4_ThanhToan step4;
	private Step5_SuccessPanel step5;

	public TAB_BanVe() {
		setLayout(new BorderLayout());
		setBackground(UIHelper.BG_PAGE);
		setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
		stepProgress = new StepProgressPanel(STEP_NAMES);
		add(stepProgress, BorderLayout.NORTH);

		cardLayout = new CardLayout();
		pnlCards = new JPanel(cardLayout);
		pnlCards.setOpaque(false);
		pnlCards.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

		// Khởi tạo các Step và truyền this vào
		step1 = new Step1_TimKiem(this);
		step2 = new Step2_ChonChoNgoi(this);
		step3 = new Step3_NhapThongTinKH(this);
		step4 = new Step4_ThanhToan(this);
		step5 = new Step5_SuccessPanel(this);

		pnlCards.add(step1, "STEP_0");
		pnlCards.add(step2, "STEP_1");
		pnlCards.add(step3, "STEP_2");
		pnlCards.add(step4, "STEP_3");
		pnlCards.add(step5, "STEP_4");
		add(pnlCards, BorderLayout.CENTER);

		JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
		pnlFooter.setOpaque(false);

		btnBack = UIHelper.makeBtn("Quay lại", false);
		btnNext = UIHelper.makeBtn("Tiếp tục", true);
		btnBack.setVisible(false);
		btnNext.setVisible(false);

		pnlFooter.add(btnBack);
		pnlFooter.add(btnNext);
		add(pnlFooter, BorderLayout.SOUTH);

		btnNext.addActionListener(e -> nextStep());
		btnBack.addActionListener(e -> prevStep());

		setupTimer();
	}

	// === GETTERS & SETTERS CHO CÁC STEP SỬ DỤNG ===
	public Map<String, Map<String, String>> getPassengerDataMap() {
		return passengerDataMap;
	}

	public DAO_BanVe getDaoBanVe() {
		return daoBanVe;
	}

	public List<Map<String, String>> getSelectedSeatsData() {
		return selectedSeatsData;
	}

	public boolean isRoundTrip() {
		return isRoundTrip;
	}

	public void setRoundTrip(boolean roundTrip) {
		isRoundTrip = roundTrip;
	}

	public Step2_ChonChoNgoi getStep2() {
		return step2;
	}

	// =========================================================================
	// QUẢN LÝ DATABASE TRẠNG THÁI GHẾ (GIỮ CHỖ / GIẢI PHÓNG)
	// =========================================================================
	private void updateSeatsStatusDB(String targetStatus, String requiredCurrentStatus) {
		if (selectedSeatsData == null || selectedSeatsData.isEmpty())
			return;

		String sql = "UPDATE GheLichTrinh SET trangThai = ? WHERE maLT = ? AND maToa = ? AND viTri = ? AND trangThai = ?";
		try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			for (Map<String, String> seat : selectedSeatsData) {
				String maLT = seat.get("maLT");
				String maToa = seat.get("maToa");
				String viTri = seat.get("viTriGhe");

				// Dự phòng nếu maToa/viTriGhe chưa được truyền rõ ràng mà chỉ có maCho (VD:
				// TOA1_12)
				if (maToa == null || viTri == null) {
					String maCho = seat.get("maCho");
					if (maCho != null && maCho.contains("_")) {
						maToa = maCho.split("_")[0];
						viTri = maCho.split("_")[1];
					}
				}

				if (maLT != null && maToa != null && viTri != null) {
					ps.setString(1, targetStatus);
					ps.setString(2, maLT);
					ps.setString(3, maToa);
					ps.setString(4, viTri);
					ps.setString(5, requiredCurrentStatus);
					ps.executeUpdate();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// =========================================================================
	// LOGIC ĐIỀU HƯỚNG VÀ RÀNG BUỘC (VALIDATION)
	// =========================================================================
	public void resetProcess() {
		// [QUAN TRỌNG]: Giải phóng ghế đang giữ dưới Database trước khi reset
		updateSeatsStatusDB("TRONG", "GIUCHO");

		if (holdTimer != null)
			holdTimer.stop();

		currentStep = 0;
		selectedSeatsData.clear();
		passengerDataMap.clear();
		switchCard();
	}

	public void setNextButtonEnabled(boolean enabled) {
		if (btnNext != null) {
			btnNext.setEnabled(enabled);
			if (enabled) {
				btnNext.setToolTipText("Dữ liệu hợp lệ. Nhấn để tiếp tục.");
			} else {
				btnNext.setToolTipText("Vui lòng hoàn tất quá trình hiện tại.");
			}
		}
	}

	public void nextStep() {
		// === BƯỚC 1: TÌM KIẾM -> CHỌN GHẾ ===
		if (currentStep == 0) {
			setNextButtonEnabled(true);
		}

		// === BƯỚC 2: CHỌN GHẾ -> NHẬP THÔNG TIN ===
		else if (currentStep == 1) {
			if (selectedSeatsData.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 ghế để tiếp tục!", "Thông báo",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			// [QUAN TRỌNG]: Đẩy trạng thái ghế xuống DB thành "GIUCHO" để khóa người khác
			// mua
			updateSeatsStatusDB("GIUCHO", "TRONG");

			// Gọi giao diện Step 3 cập nhật form nhập liệu dựa trên số ghế vừa chọn
			try {
				step3.updatePassengerForms();
			} catch (Exception e) {
				/* Fallback */ }
		}

		// === BƯỚC 3: NHẬP THÔNG TIN -> THANH TOÁN ===
		else if (currentStep == 2) {
			// Validation kiểm tra hành khách ở Step 3
			try {
				if (!step3.validateAllPassengerInfo())
					return;
			} catch (Exception e) {
				// Dự phòng nếu trong Step 3 chưa đổi kịp tên hàm
				try {
					if (!step3.isAllPassengersFilled()) {
						JOptionPane.showMessageDialog(this,
								"Vui lòng nhập đầy đủ và XÁC NHẬN thông tin tất cả hành khách!");
						return;
					}
				} catch (Exception ex) {
				}
			}
		}

		// Logic chuyển trang chung
		if (currentStep < 4) {
			currentStep++;

			// Load dữ liệu cho Bước Thanh Toán
			if (currentStep == 3) {
				step4.loadDataFromSession();
			}

			switchCard();

			// Nếu vừa bước vào Step 3 (Nhập thông tin), bắt đầu đếm ngược giữ chỗ
			if (currentStep == 2) {
				timeLeft = 900; // 15 Phút
				if (holdTimer != null)
					holdTimer.restart();
			}
		}
	}

	private void prevStep() {
		if (currentStep > 0) {
			// [QUAN TRỌNG]: Nếu đang ở Bước Nhập Thông Tin mà bấm Quay Lại -> Bỏ giữ chỗ
			if (currentStep == 2) {
				updateSeatsStatusDB("TRONG", "GIUCHO");
				if (holdTimer != null)
					holdTimer.stop();
			}

			currentStep--;
			switchCard();
		}
	}

	private void switchCard() {
		cardLayout.show(pnlCards, "STEP_" + currentStep);
		stepProgress.updateStep(currentStep);

		btnBack.setVisible(currentStep > 0 && currentStep < 4);

		if (currentStep == 0) {
			btnNext.setVisible(false);
		} else if (currentStep == 1) {
			btnNext.setVisible(true);
			btnNext.setText("Tiếp tục");
		} else if (currentStep == 2) {
			btnNext.setVisible(true);
			btnNext.setText("Chuyển đến Thanh toán");
		} else if (currentStep == 3 || currentStep == 4) {
			// Màn hình Thanh toán và Thành công không dùng nút Tiếp tục chung này nữa
			btnNext.setVisible(false);
		}
	}

	private void setupTimer() {
		holdTimer = new Timer(1000, e -> {
			if (timeLeft > 0) {
				timeLeft--;
				int min = timeLeft / 60;
				int sec = timeLeft % 60;
				try {
					step3.updateTimerDisplay(String.format("Thời gian giữ chỗ: %02d:%02d", min, sec));
				} catch (Exception ex) {
				}
			} else {
				holdTimer.stop();
				JOptionPane.showMessageDialog(this,
						"Đã hết thời gian giữ chỗ. Vé đã được tự động hủy, vui lòng đặt lại từ đầu!", "Hết giờ",
						JOptionPane.WARNING_MESSAGE);
				resetProcess();
			}
		});
	}

	// =========================================================================
	// COMPONENT PROGRESS BAR (THANH TIẾN ĐỘ)
	// =========================================================================
	private class StepProgressPanel extends JPanel {
		private String[] steps;
		private int current = 0;

		public StepProgressPanel(String[] steps) {
			this.steps = steps;
			setOpaque(false);
			setPreferredSize(new Dimension(0, 80));
		}

		public void updateStep(int step) {
			this.current = step;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int width = getWidth();
			int height = getHeight();
			int stepCount = steps.length;
			int paddingX = 100;
			int spacing = (width - paddingX * 2) / (stepCount - 1);
			int circleRadius = 25;
			int cy = height / 2 - 10;

			g2.setColor(UIHelper.BORDER);
			g2.setStroke(new BasicStroke(4));
			g2.drawLine(paddingX, cy, width - paddingX, cy);
			g2.setColor(UIHelper.ACCENT);

			if (current > 0) {
				g2.drawLine(paddingX, cy, paddingX + (spacing * current), cy);
			}

			for (int i = 0; i < stepCount; i++) {
				int cx = paddingX + (i * spacing);

				if (i <= current)
					g2.setColor(UIHelper.ACCENT);
				else
					g2.setColor(UIHelper.BORDER);

				g2.fillOval(cx - circleRadius / 2, cy - circleRadius / 2, circleRadius, circleRadius);

				g2.setColor(i <= current ? Color.WHITE : UIHelper.TEXT_MID);
				g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
				FontMetrics fm = g2.getFontMetrics();
				String num = String.valueOf(i + 1);
				g2.drawString(num, cx - fm.stringWidth(num) / 2, cy + fm.getAscent() / 2 - 1);

				g2.setColor(i <= current ? UIHelper.ACCENT : UIHelper.TEXT_MID);
				g2.setFont(new Font("Segoe UI", i == current ? Font.BOLD : Font.PLAIN, 13));
				fm = g2.getFontMetrics();
				g2.drawString(steps[i], cx - fm.stringWidth(steps[i]) / 2, cy + circleRadius + 10);
			}
			g2.dispose();
		}
	}
}
