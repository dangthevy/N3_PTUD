package com.gui.banve;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UIHelper {
	// =========================================================================
	// MÀU SẮC & FONT
	// =========================================================================
	public static final Color BG_PAGE = new Color(0xF4F7FB);
	public static final Color BG_CARD = Color.WHITE;
	public static final Color ACCENT = new Color(0x1A5EAB);
	public static final Color ACCENT_HVR = new Color(0x1F62C8);
	public static final Color TEXT_DARK = new Color(0x1E2B3C);
	public static final Color TEXT_MID = new Color(0x5A6A7D);
	public static final Color TEXT_LIGHT = new Color(0xA0AEC0);
	public static final Color BORDER = new Color(0xE2EAF4);
	public static final Color SUCCESS = new Color(0x28A745);
	public static final Color DANGER = new Color(0xDC3545);

	public static final Font F_H1 = new Font("Segoe UI", Font.BOLD, 22);
	public static final Font F_H2 = new Font("Segoe UI", Font.BOLD, 18);
	public static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 14);
	public static final Font F_CELL = new Font("Segoe UI", Font.PLAIN, 14);
	public static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
	public static final String DATE_FMT = "dd/MM/yyyy";

	// ── Enum BtnStyle ───────────────────────────────────
	public enum BtnStyle {
		PRIMARY, SECONDARY, DANGER, SUCCESS
	}

	// =========================================================================
	// HELPER COMPONENTS
	// =========================================================================
	public static JPanel createPageTitle(String title, String subTitle) {
		JPanel pnl = new JPanel(new BorderLayout(0, 5));
		pnl.setOpaque(false);
		pnl.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 5));

		JLabel lblTitle = new JLabel(title.toUpperCase());
		lblTitle.setFont(F_H1);
		lblTitle.setForeground(ACCENT);
		pnl.add(lblTitle, BorderLayout.NORTH);

		if (subTitle != null && !subTitle.isEmpty()) {
			JLabel lblSub = new JLabel(subTitle);
			lblSub.setFont(F_CELL);
			lblSub.setForeground(TEXT_MID);
			pnl.add(lblSub, BorderLayout.CENTER);
		}
		return pnl;
	}

	public static JPanel makeCard(LayoutManager lm) {
		JPanel p = new JPanel(lm);
		p.setBackground(BG_CARD);
		p.setBorder(new ShadowBorder());
		return p;
	}

	public static JTextField makeField(String ph) {
		JTextField tf = new JTextField();
		tf.setFont(F_CELL);
		tf.setForeground(TEXT_DARK);
		tf.setBackground(new Color(0xF8FAFD));
		tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true),
				BorderFactory.createEmptyBorder(8, 12, 8, 12)));
		tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, ph);
		tf.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
		return tf;
	}

	public static JComboBox<String> makeCombo(String[] items) {
		JComboBox<String> cb = new JComboBox<>(items);
		cb.setFont(F_CELL);
		cb.setBackground(new Color(0xF8FAFD));
		cb.setForeground(TEXT_DARK);
		cb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true),
				BorderFactory.createEmptyBorder(4, 8, 4, 8)));
		return cb;
	}

	public static JButton makeBtn(String text, BtnStyle style) {
		JButton b = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (style == BtnStyle.PRIMARY)
					g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT);
				else if (style == BtnStyle.DANGER)
					g2.setColor(getModel().isRollover() ? new Color(0xC82333) : DANGER);
				else if (style == BtnStyle.SUCCESS)
					g2.setColor(getModel().isRollover() ? new Color(0x218838) : SUCCESS);
				else
					g2.setColor(getModel().isRollover() ? BORDER : BG_CARD);

				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

				if (style == BtnStyle.SECONDARY) {
					g2.setColor(BORDER);
					g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
				}
				g2.dispose();
				super.paintComponent(g);
			}
		};
		b.setFont(F_LABEL);
		b.setForeground(style != BtnStyle.SECONDARY ? Color.WHITE : TEXT_DARK);
		b.setPreferredSize(new Dimension(200, 42));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return b;
	}

	public static JButton makeBtn(String text, boolean isPrimary) {
		return makeBtn(text, isPrimary ? BtnStyle.PRIMARY : BtnStyle.SECONDARY);
	}

	public static JToggleButton createSelectionTab(String text, boolean isSelected) {
		JToggleButton btn = new JToggleButton(text, isSelected) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (isSelected())
					g2.setColor(ACCENT);
				else
					g2.setColor(getModel().isRollover() ? BORDER : BG_CARD);

				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				if (!isSelected()) {
					g2.setColor(BORDER);
					g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
				}
				g2.dispose();
				super.paintComponent(g);
			}
		};
		btn.setFont(F_LABEL);
		btn.setForeground(isSelected ? Color.WHITE : TEXT_DARK);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.setPreferredSize(new Dimension(110, 35));
		btn.addItemListener(e -> btn.setForeground(btn.isSelected() ? Color.WHITE : TEXT_DARK));
		return btn;
	}

	public static GridBagConstraints defaultGC() {
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(8, 10, 8, 10);
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.HORIZONTAL;
		return gc;
	}

	public static void addFormRow(JPanel form, GridBagConstraints gc, int row, String l1, JComponent c1, String l2,
			JComponent c2) {
		gc.gridy = row;
		gc.gridx = 0;
		gc.weightx = 0;
		JLabel lbl1 = new JLabel(l1);
		lbl1.setFont(F_LABEL);
		lbl1.setForeground(TEXT_MID);
		form.add(lbl1, gc);
		gc.gridx = 1;
		gc.weightx = 1;
		c1.setPreferredSize(new Dimension(250, 40));
		form.add(c1, gc);

		if (!l2.isEmpty()) {
			gc.gridx = 2;
			gc.weightx = 0;
			JLabel lbl2 = new JLabel(l2);
			lbl2.setFont(F_LABEL);
			lbl2.setForeground(TEXT_MID);
			form.add(lbl2, gc);
			gc.gridx = 3;
			gc.weightx = 1;
			c2.setPreferredSize(new Dimension(250, 40));
			form.add(c2, gc);
		}
	}

	public static class ShadowBorder extends AbstractBorder {
		private static final int S = 4;

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (int i = S; i > 0; i--) {
				g2.setColor(new Color(100, 140, 200, (int) (20.0 * (S - i) / S)));
				g2.drawRoundRect(x + i, y + i, w - 2 * i - 1, h - 2 * i - 1, 12, 12);
			}
			g2.setColor(new Color(0xE2EAF4));
			g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
			g2.setColor(BG_CARD);
			g2.setClip(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, 12, 12));
			g2.fillRect(x + 1, y + 1, w - 2, h - 2);
			g2.dispose();
		}
		@Override
		public Insets getBorderInsets(Component c, Insets ins) {
			ins.set(S, S, S, S);
			return ins;
		}
	}

	public static class DatePickerField extends JPanel {
		private final JTextField   txt;
		private final Calendar     cal;
		private JPanel             pnlGrid;
		private JComboBox<String>  cbThang;
		private JComboBox<Integer> cbNam;
		private JWindow            popup;
		private boolean            isEnabled = true;

		// THÊM CÔNG TẮC KHÓA NGÀY QUÁ KHỨ
		private boolean            disablePastDates = false;

		private static final String[] TEN_THANG={"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6","Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};
		private static final String[] TEN_THU={"T2","T3","T4","T5","T6","T7","CN"};

		public DatePickerField(String init){
			setLayout(new BorderLayout()); setOpaque(false);
			cal=Calendar.getInstance();
			if(init!=null&&!init.isEmpty()){try{cal.setTime(new SimpleDateFormat(DATE_FMT).parse(init));}catch(Exception ignored){}}
			String disp=init!=null&&!init.isEmpty()?init:new SimpleDateFormat(DATE_FMT).format(cal.getTime());

			txt=new JTextField(disp); txt.setFont(F_CELL); txt.setForeground(TEXT_DARK);
			txt.setBackground(new Color(0xF8FAFD)); txt.setEditable(false);
			txt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1,true),BorderFactory.createEmptyBorder(6,10,6,36)));

			JLabel ico=new JLabel(){
				@Override protected void paintComponent(Graphics g){
					Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setColor(isEnabled ? TEXT_MID : TEXT_LIGHT);
					int cx=getWidth()/2,cy=getHeight()/2;g2.drawRoundRect(cx-8,cy-7,16,14,3,3);g2.drawLine(cx-8,cy-4,cx+8,cy-4);g2.drawLine(cx-4,cy-10,cx-4,cy-5);g2.drawLine(cx+4,cy-10,cx+4,cy-5);
					g2.fillOval(cx-6,cy-1,3,3);g2.fillOval(cx-1,cy-1,3,3);g2.fillOval(cx+4,cy-1,3,3);g2.fillOval(cx-6,cy+3,3,3);g2.fillOval(cx-1,cy+3,3,3);g2.dispose();
				}
			};
			ico.setPreferredSize(new Dimension(32,36)); ico.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			JPanel wrap=new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.add(txt,BorderLayout.CENTER); wrap.add(ico,BorderLayout.EAST); add(wrap,BorderLayout.CENTER);

			MouseAdapter ma=new MouseAdapter(){
				@Override public void mouseClicked(MouseEvent e){ if(isEnabled) toggle(); }
			};
			txt.addMouseListener(ma); ico.addMouseListener(ma);
		}

		public void setEnabledField(boolean enabled) {
			this.isEnabled = enabled;
			txt.setBackground(enabled ? new Color(0xF8FAFD) : new Color(0xEEF2F8));
			txt.setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			repaint();
		}

		// THÊM HÀM ĐỂ BẬT TÍNH NĂNG KHÓA NGÀY
		public void setDisablePastDates(boolean disablePastDates) {
			this.disablePastDates = disablePastDates;
		}

		public void setDate(String date) { txt.setText(date); }
		public String getDate() { return txt.getText(); }

		private void toggle(){if(popup!=null&&popup.isVisible()){popup.dispose();popup=null;return;}showPop();}
		private void showPop(){
			popup=new JWindow(SwingUtilities.getWindowAncestor(this));popup.setLayout(new BorderLayout());
			JPanel p=new JPanel(new BorderLayout(0,6));p.setBackground(BG_CARD);
			p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1),BorderFactory.createEmptyBorder(12,12,12,12)));
			p.add(navBar(),BorderLayout.NORTH);pnlGrid=new JPanel(new GridLayout(0,7,2,2));pnlGrid.setBackground(BG_CARD);p.add(pnlGrid,BorderLayout.CENTER);
			fillGrid();popup.add(p);popup.pack();popup.setSize(Math.max(280,popup.getWidth()),popup.getHeight());
			Point loc=txt.getLocationOnScreen();popup.setLocation(loc.x,loc.y+txt.getHeight()+2);popup.setVisible(true);
			popup.addWindowFocusListener(new java.awt.event.WindowFocusListener(){
				@Override public void windowGainedFocus(java.awt.event.WindowEvent e){}
				@Override public void windowLostFocus(java.awt.event.WindowEvent e){if(popup!=null){popup.dispose();popup=null;}}
			});
		}
		private JPanel navBar(){
			JPanel nav=new JPanel(new BorderLayout(4,0));nav.setBackground(BG_CARD);
			JButton prev=navBtn("<");JButton next=navBtn(">");
			prev.addActionListener(e->{cal.add(Calendar.MONTH,-1);cbThang.setSelectedIndex(cal.get(Calendar.MONTH));cbNam.setSelectedItem(cal.get(Calendar.YEAR));fillGrid();});
			next.addActionListener(e->{cal.add(Calendar.MONTH, 1);cbThang.setSelectedIndex(cal.get(Calendar.MONTH));cbNam.setSelectedItem(cal.get(Calendar.YEAR));fillGrid();});
			cbThang=new JComboBox<>(TEN_THANG);cbThang.setFont(F_SMALL);cbThang.setSelectedIndex(cal.get(Calendar.MONTH));cbThang.setPreferredSize(new Dimension(82,26));
			cbThang.addActionListener(e->{cal.set(Calendar.MONTH,cbThang.getSelectedIndex());fillGrid();});
			int y=Calendar.getInstance().get(Calendar.YEAR);Integer[] yrs=new Integer[16];for(int i=0;i<16;i++)yrs[i]=y-5+i;
			cbNam=new JComboBox<>(yrs);cbNam.setFont(F_SMALL);cbNam.setSelectedItem(cal.get(Calendar.YEAR));cbNam.setPreferredSize(new Dimension(60,26));
			cbNam.addActionListener(e->{if(cbNam.getSelectedItem()!=null){cal.set(Calendar.YEAR,(Integer)cbNam.getSelectedItem());fillGrid();}});
			JPanel ctr=new JPanel(new FlowLayout(FlowLayout.CENTER,4,0));ctr.setBackground(BG_CARD);ctr.add(cbThang);ctr.add(cbNam);
			nav.add(prev,BorderLayout.WEST);nav.add(ctr,BorderLayout.CENTER);nav.add(next,BorderLayout.EAST);return nav;
		}
		private void fillGrid(){
			pnlGrid.removeAll();
			for(String th:TEN_THU){JLabel l=new JLabel(th,SwingConstants.CENTER);l.setFont(new Font("Segoe UI",Font.BOLD,11));l.setPreferredSize(new Dimension(32,24));l.setForeground(TEXT_MID);pnlGrid.add(l);}
			Calendar tmp=(Calendar)cal.clone();tmp.set(Calendar.DAY_OF_MONTH,1);int first=(tmp.get(Calendar.DAY_OF_WEEK)+5)%7;
			Calendar today=Calendar.getInstance();int todayD=today.get(Calendar.DAY_OF_MONTH);

			// XÓA GIỜ/PHÚT CỦA NGÀY HIỆN TẠI ĐỂ SO SÁNH CHÍNH XÁC
			Calendar todayClear = Calendar.getInstance();
			todayClear.set(Calendar.HOUR_OF_DAY, 0); todayClear.set(Calendar.MINUTE, 0); todayClear.set(Calendar.SECOND, 0); todayClear.set(Calendar.MILLISECOND, 0);

			boolean sm=today.get(Calendar.MONTH)==cal.get(Calendar.MONTH)&&today.get(Calendar.YEAR)==cal.get(Calendar.YEAR);
			int chosen=-1;
			try{Calendar c=Calendar.getInstance();c.setTime(new SimpleDateFormat(DATE_FMT).parse(txt.getText()));if(c.get(Calendar.MONTH)==cal.get(Calendar.MONTH)&&c.get(Calendar.YEAR)==cal.get(Calendar.YEAR))chosen=c.get(Calendar.DAY_OF_MONTH);}catch(Exception ignored){}
			for(int i=0;i<first;i++)pnlGrid.add(new JLabel());
			int days=cal.getActualMaximum(Calendar.DAY_OF_MONTH);final int fc=chosen;

			for(int d=1;d<=days;d++){
				final int nd=d;
				boolean isT=sm&&d==todayD;
				boolean isSel=d==fc;

				// KIỂM TRA XEM NGÀY NÀY CÓ PHẢI LÀ QUÁ KHỨ KHÔNG
				Calendar cellCal = (Calendar) cal.clone();
				cellCal.set(Calendar.DAY_OF_MONTH, d);
				cellCal.set(Calendar.HOUR_OF_DAY, 0); cellCal.set(Calendar.MINUTE, 0); cellCal.set(Calendar.SECOND, 0); cellCal.set(Calendar.MILLISECOND, 0);
				boolean isPast = disablePastDates && cellCal.before(todayClear);

				JButton b=new JButton(String.valueOf(d)){
					@Override protected void paintComponent(Graphics g){
						Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
						if(isSel){g2.setColor(ACCENT);g2.fillOval(1,1,getWidth()-2,getHeight()-2);}
						else if(getModel().isRollover() && !isPast){g2.setColor(new Color(0xDDEEFF));g2.fillOval(1,1,getWidth()-2,getHeight()-2);}
						else if(isT && !isPast){g2.setColor(new Color(0xE8F1FB));g2.fillOval(1,1,getWidth()-2,getHeight()-2);}
						g2.dispose();super.paintComponent(g);
					}
				};
				b.setFont(new Font("Segoe UI",isT?Font.BOLD:Font.PLAIN,11));
				b.setPreferredSize(new Dimension(32,32));b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setMargin(new Insets(0,0,0,0));

				// NẾU LÀ NGÀY QUÁ KHỨ -> ĐỔI MÀU XÁM, KHÔNG CHO BẤM
				if (isPast) {
					b.setForeground(new Color(200, 200, 200));
					b.setEnabled(false);
				} else {
					b.setForeground(isSel?Color.WHITE:isT?ACCENT:TEXT_DARK);
					b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					b.addActionListener(e->{cal.set(Calendar.DAY_OF_MONTH,nd);txt.setText(new SimpleDateFormat(DATE_FMT).format(cal.getTime()));if(popup!=null){popup.dispose();popup=null;}});
				}

				pnlGrid.add(b);
			}
			pnlGrid.revalidate();pnlGrid.repaint();
		}
		private JButton navBtn(String t){
			JButton b=new JButton(t);b.setFont(new Font("Segoe UI",Font.BOLD,14));b.setForeground(ACCENT);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setMargin(new Insets(0,0,0,0));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setPreferredSize(new Dimension(32,32));return b;
		}
	}
}