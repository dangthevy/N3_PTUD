package com.gui;

import com.dao.DAO_LoaiToa;
import com.dao.DAO_Toa;
import com.entities.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class Form_Toa extends JDialog {
	private static final Color ACCENT = new Color(0x1A5EAB);
	private static final Color TEXT_DARK = new Color(0x1E2B3C);
	private static final Color BORDER_CLR = new Color(0xE2EAF4);
	private static final Color DANGER = new Color(0xDC3545);

	private JTextField txtMaToa, txtTenToa, txtSoGhe;
	private JComboBox<LoaiToaWrapper> cbLoaiToa;
	private JLabel lblErrTen;
	private JButton btnSave; // Đưa ra biến toàn cục để khóa/mở

	private boolean confirmed = false;
	private Toa toaEntity;
	private List<LoaiToa> dsLoaiToa;

	// Biến cho Dirty check (Kiểm tra sự thay đổi)
	private boolean isEditMode = false;
	private String origTenToa = "";
	private String origMaLoaiToa = "";

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

	public Form_Toa(Frame parent, String title) {
		super(parent, title, true);
		setSize(500, 450);
		setLocationRelativeTo(parent);
		getContentPane().setBackground(Color.WHITE);

		JPanel pnlMain = new JPanel(new BorderLayout(15, 20));
		pnlMain.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
		pnlMain.setBackground(Color.WHITE);

		JLabel lblTitle = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
		lblTitle.setForeground(ACCENT);
		pnlMain.add(lblTitle, BorderLayout.NORTH);

		JPanel pnlForm = new JPanel(new GridBagLayout());
		pnlForm.setOpaque(false);
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(5, 5, 0, 5);

		txtMaToa = createTextField();
		txtTenToa = createTextField();
		txtSoGhe = createTextField();
		txtSoGhe.setEditable(false);
		txtSoGhe.setBackground(new Color(0xF4F7FB));
		lblErrTen = createErrorLabel();

		cbLoaiToa = new JComboBox<>();
		cbLoaiToa.setPreferredSize(new Dimension(0, 38));
		cbLoaiToa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		cbLoaiToa.setBackground(Color.WHITE);
		dsLoaiToa = new DAO_LoaiToa().getAllLoaiToa();
		for (LoaiToa lt : dsLoaiToa)
			cbLoaiToa.addItem(new LoaiToaWrapper(lt, lt.getSoHang() * lt.getSoCot()));

		cbLoaiToa.addActionListener(e -> {
			LoaiToaWrapper w = (LoaiToaWrapper) cbLoaiToa.getSelectedItem();
			if (w != null) {
				txtSoGhe.setText(w.ghe + "");
				if (!isEditMode) { // Chỉ tự động điền tên nếu là thêm mới
					String tenLoai = w.lt.getTenLoaiToa().toLowerCase();
					if (tenLoai.contains("nằm"))
						txtTenToa.setText("Toa giường nằm điều hòa");
					else if (tenLoai.contains("mềm"))
						txtTenToa.setText("Toa ghế ngồi mềm chất lượng cao");
					else
						txtTenToa.setText("Toa ghế ngồi cứng");
				}
				lblErrTen.setText(" ");
				checkSaveButtonState(); // Cập nhật trạng thái nút lưu
			}
		});
		if (cbLoaiToa.getItemCount() > 0 && !isEditMode) {
			txtSoGhe.setText(((LoaiToaWrapper) cbLoaiToa.getSelectedItem()).ghe + "");
			txtTenToa.setText("Toa ghế ngồi cứng");
		}

		int y = 0;
		addRow(pnlForm, "Mã toa (Auto):", txtMaToa, null, y++, gc);
		addRow(pnlForm, "Khuôn mẫu (Loại):", cbLoaiToa, null, y++, gc);
		addRow(pnlForm, "Tên/Mô tả toa (*):", txtTenToa, lblErrTen, y++, gc);
		addRow(pnlForm, "Sức chứa (Ghế):", txtSoGhe, null, y++, gc);

		pnlMain.add(pnlForm, BorderLayout.CENTER);

		JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
		pnlBottom.setOpaque(false);
		JButton btnCancel = createButton("Hủy Bỏ", new Color(108, 122, 137));
		btnSave = createButton("Lưu Dữ Liệu", ACCENT);
		btnSave.setEnabled(false); // Khóa nút lưu ban đầu

		btnCancel.addActionListener(e -> dispose());
		btnSave.addActionListener(e -> validateAndSave());
		pnlBottom.add(btnCancel);
		pnlBottom.add(btnSave);
		pnlMain.add(pnlBottom, BorderLayout.SOUTH);
		add(pnlMain);

		// Sử dụng LiveValidation thay cho ClearError
		addLiveValidationListener(txtTenToa, lblErrTen);
	}

	private void addRow(JPanel p, String l, JComponent c, JLabel err, int y, GridBagConstraints gc) {
		gc.gridy = y * 2;
		gc.gridx = 0;
		gc.weightx = 0.35;
		JLabel lbl = new JLabel(l);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lbl.setForeground(TEXT_DARK);
		p.add(lbl, gc);
		gc.gridx = 1;
		gc.weightx = 0.65;
		p.add(c, gc);
		if (err != null) {
			gc.gridy = y * 2 + 1;
			gc.gridx = 1;
			gc.insets = new Insets(0, 5, 10, 5);
			p.add(err, gc);
			gc.insets = new Insets(5, 5, 0, 5);
		} else {
			gc.gridy = y * 2 + 1;
			p.add(Box.createVerticalStrut(12), gc);
		}
	}

	// ================== REAL-TIME VALIDATION ==================
	private void checkSaveButtonState() {
		boolean isValid = true;
		String ten = txtTenToa.getText().trim();
		if (ten.isEmpty())
			isValid = false;

		boolean isChanged = true;
		if (isEditMode) {
			LoaiToaWrapper curLoai = (LoaiToaWrapper) cbLoaiToa.getSelectedItem();
			String curMaLoai = (curLoai != null) ? curLoai.lt.getMaLoaiToa() : "";
			if (ten.equals(origTenToa) && curMaLoai.equals(origMaLoaiToa)) {
				isChanged = false;
			}
		}
		btnSave.setEnabled(isValid && isChanged);
	}

	private void validateAndSave() {
		toaEntity = new Toa(txtMaToa.getText().trim(), txtTenToa.getText().trim(),
				Integer.parseInt(txtSoGhe.getText().trim()), ((LoaiToaWrapper) cbLoaiToa.getSelectedItem()).lt,
				"SAN_SANG");
		confirmed = true;
		dispose();
	}

	// --- UI HELPERS ---
	private JTextField createTextField() {
		JTextField tf = new JTextField();
		tf.setPreferredSize(new Dimension(0, 38));
		tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_CLR), new EmptyBorder(0, 10, 0, 10)));
		return tf;
	}

	private JLabel createErrorLabel() {
		JLabel lbl = new JLabel(" ");
		lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
		lbl.setForeground(DANGER);
		return lbl;
	}

	private JButton createButton(String text, Color bg) {
		JButton b = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (!isEnabled()) {
					g2.setColor(new Color(200, 200, 200));
				} else {
					g2.setColor(getModel().isRollover() ? bg.darker() : bg);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		b.setPreferredSize(new Dimension(130, 40));
		b.setForeground(Color.WHITE);
		b.setFont(new Font("Segoe UI", Font.BOLD, 14));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private void addLiveValidationListener(JTextField tf, JLabel lbl) {
		tf.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				lbl.setText(" ");
				checkSaveButtonState();
			}

			public void removeUpdate(DocumentEvent e) {
				lbl.setText(" ");
				checkSaveButtonState();
			}

			public void changedUpdate(DocumentEvent e) {
				lbl.setText(" ");
				checkSaveButtonState();
			}
		});
	}

	public void setupForAdd() {
		isEditMode = false;
		txtMaToa.setText(new DAO_Toa().phatSinhMaToa());
		txtMaToa.setEditable(false);
		txtMaToa.setBackground(new Color(0xF4F7FB));
		checkSaveButtonState();
	}

	public void setEntity(Toa t) {
		isEditMode = true;
		origTenToa = t.getTenToa();
		origMaLoaiToa = t.getLoaiToa().getMaLoaiToa();

		txtMaToa.setText(t.getMaToa());
		txtMaToa.setEditable(false);
		txtMaToa.setBackground(new Color(0xF4F7FB));
		txtTenToa.setText(origTenToa);
		for (int i = 0; i < cbLoaiToa.getItemCount(); i++)
			if (cbLoaiToa.getItemAt(i).lt.getMaLoaiToa().equals(origMaLoaiToa))
				cbLoaiToa.setSelectedIndex(i);

		// RÀNG BUỘC NGHIỆP VỤ: Khóa không cho đổi loại khi đã sinh toa vật lý
		cbLoaiToa.setEnabled(false);
		cbLoaiToa.setToolTipText("Khuôn mẫu đã được đúc thành Toa vật lý. Không thể thay đổi để tránh lệch sơ đồ ghế!");

		checkSaveButtonState();
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public Toa getEntity() {
		return toaEntity;
	}
}