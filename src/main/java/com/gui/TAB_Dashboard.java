package com.gui;

import com.dao.DAO_Ga;
import com.entities.Ga;
import com.toedter.calendar.JDateChooser;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TAB_Dashboard extends JPanel {
    private Color primaryColor = new Color(0, 122, 255);
    private Image backgroundImage;

    // Thành phần nhập liệu
    private JComboBox<String> cbDepPlace, cbDesPlace;
    private JDateChooser dateDep, dateReturn;
    private JPanel pnlReturnWrapper;
    private JRadioButton rdOneWay, rdRoundTrip;
    private JButton btnSearch;
    
    // Tham chiếu đến bộ điều khiển trung tâm
    private GUI_General parentController; 

    public TAB_Dashboard(GUI_General parent) {
        this.parentController = parent;
        
        // 1. Tải ảnh nền
        try {
            // Lưu ý: Đảm bảo file ảnh nằm trong folder src/main/resources/com/img/
            backgroundImage = new ImageIcon("src/main/img/bachgrough.png").getImage();
            } catch (Exception e) {
            System.err.println("Lưu ý: Không tìm thấy ảnh nền Dashboard.");
        }

        setLayout(new BorderLayout());

        // --- 2. GIAO DIỆN TÌM KIẾM (NORTH) ---
        initSearchEngine();

        // --- 3. GIAO DIỆN PHỔ BIẾN (CENTER) ---
        initPopularSection();

        // --- 4. CÀI ĐẶT SỰ KIỆN ---
        initEvents();
    }

    private void initSearchEngine() {
        // Panel bao phủ có ảnh nền và lớp phủ mờ
        JPanel pnlSearchWrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    g.setColor(new Color(0, 0, 0, 90)); 
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        pnlSearchWrapper.setPreferredSize(new Dimension(0, 450));

        // Khung nhập liệu màu trắng
        JPanel pnlSearchBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.dispose();
            }
        };
        pnlSearchBox.setOpaque(false);
        pnlSearchBox.setBackground(new Color(255, 255, 255, 250));
        pnlSearchBox.setLayout(new BoxLayout(pnlSearchBox, BoxLayout.Y_AXIS));
        pnlSearchBox.setPreferredSize(new Dimension(950, 170));
        pnlSearchBox.setBorder(new EmptyBorder(25, 40, 25, 40));

        // --- Hàng 1: Inputs ---
        JPanel pnlInputs = new JPanel(new GridLayout(1, 4, 20, 0));
        pnlInputs.setOpaque(false);

        cbDepPlace = new JComboBox<>();
        cbDesPlace = new JComboBox<>();
        cbDepPlace.setEditable(true);
        cbDesPlace.setEditable(true);
        
        // Áp dụng thư viện SwingX để gợi ý khi gõ
        AutoCompleteDecorator.decorate(cbDepPlace);
        AutoCompleteDecorator.decorate(cbDesPlace);
        
        loadLocationData(); // Đổ dữ liệu Tỉnh/Thành

        dateDep = new JDateChooser();
        dateDep.setDateFormatString("dd/MM/yyyy");
        dateDep.setDate(new Date());

        dateReturn = new JDateChooser();
        dateReturn.setDateFormatString("dd/MM/yyyy");

        pnlInputs.add(createInputGroup("Nơi đi (Tỉnh/Thành)", cbDepPlace));
        pnlInputs.add(createInputGroup("Nơi đến (Tỉnh/Thành)", cbDesPlace));
        pnlInputs.add(createInputGroup("Ngày đi", dateDep));

        pnlReturnWrapper = createInputGroup("Ngày về", dateReturn);
        pnlReturnWrapper.setVisible(false);
        pnlInputs.add(pnlReturnWrapper);

        // --- Hàng 2: Radio Buttons & Button Tìm Kiếm ---
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlRadio.setOpaque(false);
        rdOneWay = new JRadioButton("Một chiều", true);
        rdRoundTrip = new JRadioButton("Khứ hồi");
        rdOneWay.setOpaque(false);
        rdRoundTrip.setOpaque(false);
        rdOneWay.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        rdRoundTrip.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        ButtonGroup group = new ButtonGroup();
        group.add(rdOneWay);
        group.add(rdRoundTrip);
        pnlRadio.add(rdOneWay);
        pnlRadio.add(Box.createHorizontalStrut(30));
        pnlRadio.add(rdRoundTrip);

        btnSearch = new JButton("TÌM KIẾM CHUYẾN TÀU");
        btnSearch.setBackground(primaryColor);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSearch.setPreferredSize(new Dimension(260, 48));
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorder(null);

        pnlBottom.add(pnlRadio, BorderLayout.WEST);
        pnlBottom.add(btnSearch, BorderLayout.EAST);

        pnlSearchBox.add(pnlInputs);
        pnlSearchBox.add(pnlBottom);

        pnlSearchWrapper.add(pnlSearchBox, new GridBagConstraints());
        add(pnlSearchWrapper, BorderLayout.NORTH);
    }

    private void loadLocationData() {
        try {
            DAO_Ga dao = new DAO_Ga();
            List<Ga> list = dao.getAllGa();
            
            // Lấy danh sách địa chỉ duy nhất và sắp xếp
            List<String> locations = list.stream()
                                         .map(Ga::getDiaChi)
                                         .distinct()
                                         .sorted()
                                         .collect(Collectors.toList());
            
            cbDepPlace.removeAllItems();
            cbDesPlace.removeAllItems();
            for (String loc : locations) {
                cbDepPlace.addItem(loc);
                cbDesPlace.addItem(loc);
            }
        } catch (Exception e) {
            System.err.println("Lỗi load dữ liệu Ga: " + e.getMessage());
        }
    }

    private void initEvents() {
        // Ẩn hiện ngày về
        rdRoundTrip.addItemListener(e -> {
            pnlReturnWrapper.setVisible(e.getStateChange() == ItemEvent.SELECTED);
            revalidate();
            repaint();
        });

        // Xử lý nút tìm kiếm
        btnSearch.addActionListener(e -> {
            String di = (cbDepPlace.getSelectedItem() != null) ? cbDepPlace.getSelectedItem().toString().trim() : "";
            String den = (cbDesPlace.getSelectedItem() != null) ? cbDesPlace.getSelectedItem().toString().trim() : "";
            Date ngayDi = dateDep.getDate();

            // Kiểm tra nghiệp vụ
            if (di.isEmpty() || den.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ nơi đi và nơi đến!");
                return;
            }
            if (di.equalsIgnoreCase(den)) {
                JOptionPane.showMessageDialog(this, "Nơi đi và nơi đến không được trùng nhau!");
                return;
            }
            if (ngayDi == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày khởi hành!");
                return;
            }

            // Chuyển Tab thông qua GUI_General
            if (parentController != null) {
                // Hiển thị Tab Bán Vé
                parentController.showTab(parentController.getTabBanVe());
                
                // Đẩy dữ liệu sang Tab Bán Vé để lọc tự động
                parentController.getTabBanVe().filterTrains(di, den, ngayDi);
            }
        });
    }

    private JPanel createInputGroup(String labelName, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelName);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(50, 50, 50));

        comp.setPreferredSize(new Dimension(0, 42));
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void initPopularSection() {
        JPanel pnlPopular = new JPanel(new BorderLayout());
        pnlPopular.setBackground(new Color(242, 245, 250));
        pnlPopular.setBorder(new EmptyBorder(30, 60, 30, 60));

        JLabel lblTitle = new JLabel("Các chuyến tàu phổ biến");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setBorder(new EmptyBorder(0, 0, 25, 0));
        pnlPopular.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlGrid = new JPanel(new GridLayout(1, 4, 30, 0));
        pnlGrid.setOpaque(false);
        
        // Thêm các Card chuyến tàu (Bạn có thể lặp qua danh sách từ DB nếu muốn)
        pnlGrid.add(createTrainCard("Sài Gòn - Đà Nẵng", "Từ 450.000đ", "/com/img/danang.jpg"));
        pnlGrid.add(createTrainCard("Hà Nội - Hải Phòng", "Từ 125.000đ", "/com/img/hp.jpg"));
        pnlGrid.add(createTrainCard("Huế - Đà Nẵng", "Từ 110.000đ", "/com/img/hue.jpg"));
        pnlGrid.add(createTrainCard("Sài Gòn - Nha Trang", "Từ 390.000đ", "/com/img/nt.jpg"));

        pnlPopular.add(pnlGrid, BorderLayout.CENTER);
        add(new JScrollPane(pnlPopular), BorderLayout.CENTER);
    }

    private JPanel createTrainCard(String route, String price, String imgRes) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JLabel lblImg = new JLabel();
        lblImg.setPreferredSize(new Dimension(0, 180));
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(imgRes));
            Image img = icon.getImage().getScaledInstance(280, 180, Image.SCALE_SMOOTH);
            lblImg.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblImg.setText("No Image");
            lblImg.setOpaque(true);
            lblImg.setBackground(Color.LIGHT_GRAY);
        }

        JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlInfo.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnlInfo.setOpaque(false);

        JLabel lblRoute = new JLabel(route);
        lblRoute.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel lblPrice = new JLabel(price);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPrice.setForeground(new Color(255, 102, 0));

        pnlInfo.add(lblRoute);
        pnlInfo.add(lblPrice);

        card.add(lblImg, BorderLayout.NORTH);
        card.add(pnlInfo, BorderLayout.CENTER);
        return card;
    }
}