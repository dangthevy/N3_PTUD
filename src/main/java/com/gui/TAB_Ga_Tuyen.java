package com.gui;

import com.dao.DAO_Ga;
import com.dao.DAO_Tuyen;
import com.entities.Ga;
import com.entities.Tuyen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class TAB_Ga_Tuyen extends JPanel implements ActionListener {
    // Dành cho Ga
    private DefaultTableModel dataModel_Ga;
    private JTable tableGa;
    private JTextField txtSearchGa;

    // Dành cho Tuyến
    private DefaultTableModel dataModel_Tuyen;
    private JTable tableTuyen;
    private JTextField txtMaTuyen;
    private JTextField txtTenTuyen;
    private JComboBox<String> cbGio;
    private JComboBox<String> cbPhut;
    private JComboBox<String> cbGaDi;
    private JComboBox<String> cbGaDen;

    DAO_Ga dsGa = new DAO_Ga();
    DAO_Tuyen dsTuyen  = new DAO_Tuyen();

    public TAB_Ga_Tuyen() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tiêu đề
        JLabel lblTitle = new JLabel("QUẢN LÝ GA VÀ TUYẾN ĐƯỜNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 122, 255));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel leftPanel_Ga = createGaPanel();
        JPanel rightPanel_Tuyen = createTuyenPanel();

        leftPanel_Ga.setPreferredSize(new Dimension(450, 0));

        add(leftPanel_Ga, BorderLayout.WEST);
        add(rightPanel_Tuyen, BorderLayout.CENTER);

        // Gọi hàm load dữ liệu ban đầu
        updateTableData_Ga();
        updateComboBox();
        updateTableData_Tuyen();
        xoaRongFormTuyen();
    }

    // ================= KHUNG BÊN TRÁI: QUẢN LÝ GA =================
    private JPanel createGaPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Danh sách Ga"));
        panel.setBackground(Color.WHITE);

        // 1. Thanh tìm kiếm
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));


        searchPanel.add(new JLabel("Tìm kiếm: "), BorderLayout.WEST);
        txtSearchGa = new JTextField();
        searchPanel.add(txtSearchGa, BorderLayout.CENTER);

        JButton btnSearchGa = new JButton("Lọc");
        btnSearchGa.setBackground(new Color(0, 122, 255));
        btnSearchGa.setForeground(Color.WHITE);

        JButton btnlamMoi = new JButton("Làm mới");
        btnSearchGa.setBackground(new Color(0, 122, 255));
        btnSearchGa.setForeground(Color.WHITE);

        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        actionPanel.add(btnSearchGa);
        actionPanel.add(btnlamMoi);

        searchPanel.add(actionPanel, BorderLayout.EAST);

        panel.add(searchPanel, BorderLayout.NORTH);

        // 2. Bảng dữ liệu Ga
        String[] cols_Ga = {"Mã Ga", "Tên Ga", "Địa chỉ"};
        dataModel_Ga = new DefaultTableModel(cols_Ga, 0);

        tableGa = new JTable(dataModel_Ga);
        tableGa.setRowHeight(25);
        tableGa.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tableGa.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tableGa);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Tính năng live search (Tìm kiếm khi gõ)
        // Gắn DocumentListener vào ô nhập liệu txtSearchGa
        txtSearchGa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                thucHienTimKiem(); // Gọi khi gõ thêm chữ
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                thucHienTimKiem(); // Gọi khi xóa bớt chữ (Backspace)
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                thucHienTimKiem(); // Gọi khi có thay đổi định dạng
            }

            // Hàm xử lý chung để không phải viết lại code 3 lần
            private void thucHienTimKiem() {
                String tuKhoa = txtSearchGa.getText().trim();

                // Lấy danh sách kết quả từ DAO (dùng hàm timKiemGa đa năng bạn đã có)
                List<Ga> listKetQua = dsGa.timKiemGa(tuKhoa);

                // Xóa dữ liệu cũ trên bảng
                dataModel_Ga.setRowCount(0);

                // Nạp dữ liệu mới vào bảng ngay lập tức
                for (Ga ga : listKetQua) {
                    dataModel_Ga.addRow(new Object[]{
                            ga.getMaGa(),
                            ga.getTenGa(),
                            ga.getDiaChi()
                    });
                }
            }
        });

        // Vì đã tìm kiếm tự động, có thể tắt hoặc ẩn nút "Lọc" đi cho gọn
        // btnSearchGa.setVisible(false);


        // Sự kiện btnSearchGa
        btnSearchGa.addActionListener(e->{
            String tuKhoa = txtSearchGa.getText().trim();

            // Gọi hàm tìm kiếm
            List<Ga> listKQ = dsGa.timKiemGa(tuKhoa);

            // Xóa bảng cũ
            dataModel_Ga.setRowCount(0);

            // Update lại bảng vs dữ liệu tìm đc
            for (Ga ga : listKQ) {
                dataModel_Ga.addRow(new Object[]{
                        ga.getMaGa(),
                        ga.getTenGa(),
                        ga.getDiaChi()
                });
            }

            if(listKQ.isEmpty()){
                JOptionPane.showMessageDialog(this, "Không tìm thấy ga");
            }
        });

        btnlamMoi.addActionListener(e->{
            lamMoiGa();
        });

        tableGa.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = tableGa.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    tableGa.setRowSelectionInterval(row, row);
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                        hienThiDialogGa(row); // Bật form Sửa Ga
                    }
                }
            }
        });

        return panel;
    }

    // Đưa dữ liệu vào bảng Ga
    private void updateTableData_Ga() {
        List<Ga> list = dsGa.getAllGa();

        dataModel_Ga.setRowCount(0); // Xóa sạch dữ liệu cũ

        for (Ga ga : list) {
            String[] rowData = {
                    ga.getMaGa(),
                    ga.getTenGa(),
                    ga.getDiaChi()
            };
            dataModel_Ga.addRow(rowData);
        }
        tableGa.revalidate();
        tableGa.repaint();
    }

    // 1. Đưa dữ liệu vào ComboBox Ga đi và ga đến của Tuyến
    private void updateComboBox() {
        List<Ga> list = dsGa.getAllGa();

        // Xóa dữ liệu cũ trước khi thêm mới
        cbGaDi.removeAllItems();
        cbGaDen.removeAllItems();

        for (Ga ga : list) {
            cbGaDi.addItem(ga.getMaGa());
            cbGaDen.addItem(ga.getMaGa());
        }
    }

    // Làm mới bảng Ga
    private void lamMoiGa(){
        txtSearchGa.setText("");
        updateTableData_Ga();
        txtSearchGa.requestFocus();
    }

    // Hàm hiển thị Popup Form nổi lên của Ga
    private void hienThiDialogGa(int row) {
        // Lấy mã Ga từ bảng
        String maGa = tableGa.getValueAt(row, 0).toString();

        Ga gaHienTai = dsGa.getGaByMa(maGa);

        if (gaHienTai == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu ga này trong CSDL!");
            return;
        }

        // Khởi tạo Popup (JDialog)
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Form Ga", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(450, 250);
        dialog.setLocationRelativeTo(this); // Canh giữa màn hình
        dialog.setLayout(new BorderLayout());

        // Tạo phần Form nhập liệu cho Popup
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JTextField txtMa = new JTextField(gaHienTai.getMaGa());
        txtMa.setEditable(false); // Khóa không cho sửa Mã
        txtMa.setBackground(new Color(233, 236, 239)); // Đổi màu nền xám nhạt

        JTextField txtTen = new JTextField(gaHienTai.getTenGa());
        txtTen.setEditable(false);
        txtTen.setBackground(new Color(233, 236, 239));

        JTextField txtDiaChi = new JTextField(gaHienTai.getDiaChi());
        txtDiaChi.setEditable(false);
        txtDiaChi.setBackground(new Color(233, 236, 239));

        formPanel.add(new JLabel("Mã Ga:"));
        formPanel.add(txtMa);
        formPanel.add(new JLabel("Tên Ga:"));
        formPanel.add(txtTen);
        formPanel.add(new JLabel("Địa chỉ:"));
        formPanel.add(txtDiaChi);

        // Ráp mọi thứ vào Dialog và hiển thị
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ================= KHUNG BÊN PHẢI: QUẢN LÝ TUYẾN =================
    private JPanel createTuyenPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Thông tin Tuyến"));
        panel.setBackground(Color.WHITE);

        // 1. Khởi tạo Form nhập liệu Tuyến
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.WHITE);

        txtMaTuyen = new JTextField();
        txtTenTuyen = new JTextField();
        cbGaDi = new JComboBox<>();
        cbGaDen = new JComboBox<>();

        formPanel.add(new JLabel("Mã Tuyến:"));
        formPanel.add(txtMaTuyen);

        formPanel.add(new JLabel("Tên tuyến:"));
        formPanel.add(txtTenTuyen);

        // --- Thời gian chạy ---
        // Khởi tạo mảng dữ liệu gốc
        String[] allHours = new String[100];
        for (int i = 0; i <= 99; i++) allHours[i] = String.format("%02d", i);

        String[] allMinutes = new String[60];
        for (int i = 0; i <= 59; i++) allMinutes[i] = String.format("%02d", i);

        cbGio = new JComboBox<>(allHours);
        cbPhut = new JComboBox<>(allMinutes);

        cbGio.setEditable(true);
        cbPhut.setEditable(true);

        setupAutoSelectAll(cbGio);
        setupAutoSelectAll(cbPhut);

        // 2. Gọi hàm lọc thông minh cho cả Giờ và Phút
        applySmartFilter(cbGio, allHours);
        applySmartFilter(cbPhut, allMinutes);

        // 3. Đóng gói vào Panel nhỏ
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        timePanel.setBackground(Color.WHITE);
        timePanel.add(cbGio);
        timePanel.add(new JLabel("Giờ"));
        timePanel.add(Box.createHorizontalStrut(30));
        timePanel.add(cbPhut);
        timePanel.add(new JLabel("Phút"));

        // Đưa timePanel vào form
        formPanel.add(new JLabel("Thời gian chạy:"));
        formPanel.add(timePanel);

        formPanel.add(new JLabel("Ga Đi:"));
        formPanel.add(cbGaDi);

        formPanel.add(new JLabel("Ga Đến:"));
        formPanel.add(cbGaDen);

        // 2. Nút chức năng Tuyến
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);

        JPanel gridButtonPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        gridButtonPanel.setBackground(Color.WHITE);

        JButton btnLuu = new JButton("Lưu Tuyến");
        btnLuu.addActionListener(this); // Gắn sự kiện

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.addActionListener(this); // Gắn sự kiện

        gridButtonPanel.add(btnLuu);
        gridButtonPanel.add(btnLamMoi);

        btnPanel.add(gridButtonPanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);
        panel.add(topPanel, BorderLayout.NORTH);

        // 3. Khởi tạo Bảng danh sách Tuyến
        String[] cols = {"Mã Tuyến", "Tên Tuyến", "Thời gian chạy", "Ga Đi", "Ga Đến"};
        dataModel_Tuyen = new DefaultTableModel(cols, 0);

        tableTuyen = new JTable(dataModel_Tuyen);
        tableTuyen.setRowHeight(25);
        tableTuyen.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tableTuyen);
        panel.add(scrollPane, BorderLayout.CENTER);

        // --- SỰ KIỆN CLICK VÀO BẢNG ĐỂ MỞ POPUP SỬA ---
        tableTuyen.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // Lấy ra dòng mà con trỏ chuột vừa click trúng
                int row = tableTuyen.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    // Ép bảng bôi đen đúng dòng đó ngay lập tức
                    tableTuyen.setRowSelectionInterval(row, row);

                    // Click chuột trái 2 lần
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                        hienThiDialogSuaTuyen(row); // Bật form lên luôn
                    }
                }
            }
        });

        // Sự kiện lưu Tuyến
        btnLuu.addActionListener(e -> {
            Tuyen tuyen = reverTuyenFromTextFile();

            if(tuyen == null){
                return;
            }

            if (dsTuyen.addTuyen(tuyen)) {
                JOptionPane.showMessageDialog(this, "Thêm tuyến mới thành công!");
                // Gọi thẳng các hàm cập nhật giao diện, KHÔNG DÙNG THREAD
                xoaRongFormTuyen();
                updateComboBox();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại! Vui lòng kiểm tra lại (Có thể trùng Mã Tuyến).", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }

        });

        btnLamMoi.addActionListener(e -> {
            xoaRongFormTuyen();
        });

        return panel;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    private Tuyen reverTuyenFromTextFile() {
        String maTuyen = txtMaTuyen.getText().trim();
        int thoiGian = 0;
        try {
            int gio = Integer.parseInt(cbGio.getSelectedItem().toString().trim());
            int phut = Integer.parseInt(cbPhut.getSelectedItem().toString().trim());
            thoiGian = (gio * 60) + phut; // Lưu tổng thời gian bằng phút
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Thời gian chạy không hợp lệ. Vui lòng kiểm tra lại Giờ/Phút!");
            return null;
        }

        Object selectedGaDi = cbGaDi.getSelectedItem();
        Object selectedGaDen = cbGaDen.getSelectedItem();

        if (selectedGaDi == null || selectedGaDen == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Ga Đi và Ga Đến");
            return null;
        }

        String maGaDi = selectedGaDi.toString();
        String maGaDen = selectedGaDen.toString();

        // 1. Dùng DAO_Ga để lấy thông tin ga
        Ga gaDi = dsGa.getGaByMa(maGaDi);
        Ga gaDen = dsGa.getGaByMa(maGaDen);

        // 2. Tự động tạo tên tuyến
        String tenTuyen = (gaDi != null ? gaDi.getTenGa() : maGaDi) + " - " +
                (gaDen != null ? gaDen.getTenGa() : maGaDen);

        // 3. Khởi tạo đối tượng Tuyen
        return new Tuyen(maTuyen, tenTuyen, thoiGian, gaDi, gaDen);
    }

    // Làm sạch form nhập liệu Tuyến
    private void xoaRongFormTuyen() {
        txtMaTuyen.setText(dsTuyen.phatSinhMaTuyen());

        // Khóa ô nhập mã lại
        txtMaTuyen.setEditable(false);
        txtMaTuyen.setBackground(new Color(233, 236, 239));
        txtTenTuyen.setEditable(false);
        txtTenTuyen.setBackground(new Color(233, 236, 239));

        // Reset thời gian về 00
        cbGio.setSelectedItem("00");
        cbPhut.setSelectedItem("00");
        updateTableData_Tuyen();
    }

    // Đưa dữ liệu vào table (Dành cho bảng Tuyến)
    private void updateTableData_Tuyen() {
        List<Tuyen> list = dsTuyen.getAllTuyen();

        dataModel_Tuyen.setRowCount(0); // Xóa sạch dữ liệu cũ trên bảng

        for (Tuyen tuyen : list) {
            // Chuyển tổng phút thành "X giờ Y phút" để hiển thị cho đẹp
            int tongPhut = tuyen.getThoiGianChay();
            int gio = tongPhut / 60;
            int phut = tongPhut % 60;
            String thoiGianHienThi = gio + "h " + phut + "m";

            String[] rowData = {
                    tuyen.getMaTuyen(),
                    tuyen.getTenTuyen(),
                    thoiGianHienThi, // Hiển thị chuỗi đã format
                    tuyen.getGaDi() != null ? tuyen.getGaDi().getMaGa() : "",
                    tuyen.getGaDen() != null ? tuyen.getGaDen().getMaGa() : ""
            };
            dataModel_Tuyen.addRow(rowData);
        }
        
        // Nếu bảng chưa có model hoặc model khác thì set model
        if (tableTuyen.getModel() != dataModel_Tuyen) {
            tableTuyen.setModel(dataModel_Tuyen);
        }
        
        tableTuyen.revalidate(); // Yêu cầu bảng tính toán lại kích thước
        tableTuyen.repaint();    // Yêu cầu bảng vẽ lại toàn bộ
    }

    /**
     * Hàm hỗ trợ tạo bộ lọc thông minh (Autocomplete) cho JComboBox.
     * Tự động lọc các danh sách bắt đầu bằng ký tự người dùng vừa gõ.
     */
    private void applySmartFilter(JComboBox<String> cb, String[] fullList) {
        JTextField editor = (JTextField) cb.getEditor().getEditorComponent();

        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                // Bỏ qua các phím chức năng để không làm loạn Dropdown
                int code = e.getKeyCode();
                if (code == java.awt.event.KeyEvent.VK_UP || code == java.awt.event.KeyEvent.VK_DOWN ||
                        code == java.awt.event.KeyEvent.VK_ENTER || code == java.awt.event.KeyEvent.VK_ESCAPE ||
                        code == java.awt.event.KeyEvent.VK_LEFT || code == java.awt.event.KeyEvent.VK_RIGHT) {
                    return;
                }

                String textTyped = editor.getText();
                DefaultComboBoxModel<String> filteredModel = new DefaultComboBoxModel<>();

                // Quét toàn bộ danh sách, số nào bắt đầu bằng ký tự vừa gõ thì nạp vào model mới
                for (String item : fullList) {
                    if (item.startsWith(textTyped)) {
                        filteredModel.addElement(item);
                    }
                }

                // Cập nhật lại danh sách xổ xuống
                cb.setModel(filteredModel);
                editor.setText(textTyped); // Giữ nguyên chữ người dùng vừa gõ (tránh bị tự động chọn đè)

                if (filteredModel.getSize() > 0) {
                    cb.showPopup(); // Mở danh sách ra nếu có kết quả
                } else {
                    cb.hidePopup(); // Đóng lại nếu không tìm thấy (ví dụ gõ chữ 'A' vào ô số)
                }
            }
        });
    }

    /**
     * Hàm hỗ trợ tự động bôi đen (phủ khối) toàn bộ chữ khi click chuột
     * hoặc dùng phím Tab nhảy vào ô nhập liệu của ComboBox.
     */
    private void setupAutoSelectAll(JComboBox<String> cb) {
        JTextField editor = (JTextField) cb.getEditor().getEditorComponent();

        // 1. Xử lý khi dùng phím Tab di chuyển vào ô
        editor.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // Dùng invokeLater để đảm bảo lệnh bôi đen chạy sau khi Swing đã hoàn tất việc focus
                SwingUtilities.invokeLater(editor::selectAll);
            }
        });

        // 2. Xử lý khi click chuột vào ô
        editor.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                SwingUtilities.invokeLater(editor::selectAll);
            }
        });
    }

    // Hàm hiển thị Popup Form nổi lên để Sửa Tuyến
    private void hienThiDialogSuaTuyen(int row) {
        // Lấy mã tuyến từ bảng
        String maTuyen = tableTuyen.getValueAt(row, 0).toString();

        Tuyen tuyenHienTai = dsTuyen.getTuyenByMa(maTuyen);

        if (tuyenHienTai == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu tuyến này trong CSDL!");
            return;
        }

        // Khởi tạo Popup (JDialog)
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Form Tuyến", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this); // Canh giữa màn hình
        dialog.setLayout(new BorderLayout());

        // 4. Tạo phần Form nhập liệu cho Popup
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JTextField txtMaSua = new JTextField(tuyenHienTai.getMaTuyen());
        txtMaSua.setEditable(false); // Khóa không cho sửa Mã
        txtMaSua.setBackground(new Color(233, 236, 239)); // Đổi màu nền xám nhạt

        JTextField txtTenSua = new JTextField(tuyenHienTai.getTenTuyen());
        txtTenSua.setEditable(false); // Khóa không cho sửa tên tuyến
        txtTenSua.setBackground(new Color(233, 236, 239));

        JComboBox<String> cbGaDiSua = new JComboBox<>();
        JComboBox<String> cbGaDenSua = new JComboBox<>();

        // Đổ dữ liệu Ga vào ComboBox của form sửa
        for (Ga ga : dsGa.getAllGa()) {
            cbGaDiSua.addItem(ga.getMaGa());
            cbGaDenSua.addItem(ga.getMaGa());
        }
        cbGaDiSua.setSelectedItem(tuyenHienTai.getGaDi().getMaGa());
        cbGaDenSua.setSelectedItem(tuyenHienTai.getGaDen().getMaGa());

        // Xử lý giờ phút
        JComboBox<String> cbGioSua = new JComboBox<>();
        JComboBox<String> cbPhutSua = new JComboBox<>();
        for (int i = 0; i <= 99; i++) cbGioSua.addItem(String.format("%02d", i));
        for (int i = 0; i <= 59; i++) cbPhutSua.addItem(String.format("%02d", i));

        // Tính thời gian từ csdl lên
        int tongPhut = tuyenHienTai.getThoiGianChay();
        int gio = tongPhut / 60;
        int phut = tongPhut % 60;

        // Cài đặt lên ComboBox
        cbGioSua.setSelectedItem(String.format("%02d", gio));
        cbPhutSua.setSelectedItem(String.format("%02d", phut));

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        timePanel.add(cbGioSua);
        timePanel.add(new JLabel(" Giờ   "));
        timePanel.add(cbPhutSua);
        timePanel.add(new JLabel(" Phút"));

        formPanel.add(new JLabel("Mã Tuyến:"));
        formPanel.add(txtMaSua);
        formPanel.add(new JLabel("Nhập tên tuyến:"));
        formPanel.add(txtTenSua);
        formPanel.add(new JLabel("Thời gian chạy:"));
        formPanel.add(timePanel);
        formPanel.add(new JLabel("Chọn Ga Đi:"));
        formPanel.add(cbGaDiSua);
        formPanel.add(new JLabel("Chọn Ga Đến:"));
        formPanel.add(cbGaDenSua);

        // Tạo cụm nút "Sửa" và "Hủy" ở dưới cùng
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Ép sang góc phải
        JButton btnHuy = new JButton("Hủy");
        JButton btnSua = new JButton("Sửa");

        btnHuy.setBackground(Color.WHITE);
        btnSua.setBackground(new Color(40, 167, 69)); // Màu xanh lá
        btnSua.setForeground(Color.WHITE);

        btnPanel.add(btnHuy);
        btnPanel.add(btnSua);

        // Xử lý sự kiện nút Hủy
        btnHuy.addActionListener(e -> dialog.dispose()); // Tắt form popup

        // Xử lý sự kiện nút Sửa
        btnSua.addActionListener(e -> {
            if (cbGaDiSua.getSelectedItem().equals(cbGaDenSua.getSelectedItem())) {
                JOptionPane.showMessageDialog(dialog, "Ga đi và Ga đến không được trùng nhau!");
                return;
            }

            int phutSua = (Integer.parseInt(cbGioSua.getSelectedItem().toString()) * 60)
                    + Integer.parseInt(cbPhutSua.getSelectedItem().toString());

            Ga gaDiMoi = dsGa.getGaByMa(cbGaDiSua.getSelectedItem().toString());
            Ga gaDeNMoi = dsGa.getGaByMa(cbGaDenSua.getSelectedItem().toString());

            String tenTuyenMoi = gaDiMoi.getTenGa() + " - " + gaDeNMoi.getTenGa();

            Tuyen tuyenCapNhat = new Tuyen(txtMaSua.getText(), tenTuyenMoi, phutSua, gaDiMoi, gaDeNMoi);

            if (dsTuyen.updateTuyen(tuyenCapNhat)) {
                dialog.dispose();

                SwingUtilities.invokeLater(() -> {
                    updateTableData_Tuyen();
                    updateComboBox();
                    JOptionPane.showMessageDialog(TAB_Ga_Tuyen.this, "Cập nhật tuyến thành công!");
                });
            } else {
                JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Ráp mọi thứ vào Dialog và hiển thị
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}