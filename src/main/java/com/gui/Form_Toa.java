package com.gui;

import com.dao.DAO_LoaiToa;
import com.entities.LoaiToa;
import com.entities.Tau;
import com.entities.Toa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class Form_Toa extends JDialog {
    private JTextField txtMaToa, txtTenToa, txtSoGhe;
    private JComboBox<LoaiToa> cbLoaiToa;
    private JLabel lblTauHienTai;
    private JButton btnConfirm, btnCancel;
    
    private boolean confirmed = false;
    private Toa toaEntity;
    private Tau tauSelected;
    
    private DAO_LoaiToa loaiToaDAO = new DAO_LoaiToa();

    public Form_Toa(Frame parent, String title, Tau tau) {
        super(parent, title, true);
        this.tauSelected = tau;
        
        setLayout(new BorderLayout());
        setSize(450, 300);
        setLocationRelativeTo(parent);
        
        initUI();
        loadLoaiToa();
        initEvents();
    }

    private void initUI() {
        JPanel pnlMain = new JPanel(new GridBagLayout());
        pnlMain.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnlMain.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1; 
        gbc.weightx = 1.0; // Cho phép ô này dãn rộng ra theo chiều ngang
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // --- Hàng 1: Tàu sở hữu (ReadOnly) ---
        gbc.gridx = 0; gbc.gridy = 0;
        pnlMain.add(new JLabel("Thuộc đoàn tàu:"), gbc);
        gbc.gridx = 1;
        lblTauHienTai = new JLabel(tauSelected.getTenTau() + " (" + tauSelected.getMaTau() + ")");
        lblTauHienTai.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlMain.add(lblTauHienTai, gbc);

        // --- Hàng 2: Mã Toa ---
        gbc.gridx = 0; gbc.gridy = 1;
        pnlMain.add(new JLabel("Mã Toa:"), gbc);
        gbc.gridx = 1;
        txtMaToa = new JTextField(15);
        pnlMain.add(txtMaToa, gbc);

        // --- Hàng 3: Tên Toa ---
        gbc.gridx = 0; gbc.gridy = 2;
        pnlMain.add(new JLabel("Tên Toa:"), gbc);
        gbc.gridx = 1;
        txtTenToa = new JTextField(15);
        pnlMain.add(txtTenToa, gbc);

        // --- Hàng 4: Loại Toa ---
        gbc.gridx = 0; gbc.gridy = 3;
        pnlMain.add(new JLabel("Loại Toa:"), gbc);
        gbc.gridx = 1;
        cbLoaiToa = new JComboBox<>();
        pnlMain.add(cbLoaiToa, gbc);

        // --- Hàng 5: Số Ghế ---
        gbc.gridx = 0; gbc.gridy = 4;
        pnlMain.add(new JLabel("Số Ghế:"), gbc);
        gbc.gridx = 1;
        txtSoGhe = new JTextField(15);
        pnlMain.add(txtSoGhe, gbc);

        // --- SOUTH: BUTTONS ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.setBackground(new Color(248, 250, 252));
        btnConfirm = new JButton("Xác nhận");
        btnConfirm.setBackground(new Color(37, 99, 235));
        btnConfirm.setForeground(Color.WHITE);
        
        btnCancel = new JButton("Hủy bỏ");
        
        pnlButtons.add(btnConfirm);
        pnlButtons.add(btnCancel);

        add(pnlMain, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void loadLoaiToa() {
        List<LoaiToa> list = loaiToaDAO.getAllLoaiToa();
        for (LoaiToa lt : list) {
            cbLoaiToa.addItem(lt);
        }
        // Custom renderer để hiển thị tên loại toa
        cbLoaiToa.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LoaiToa) {
                    setText(((LoaiToa) value).getTenLoaiToa());
                }
                return this;
            }
        });
    }

    private void initEvents() {
        btnConfirm.addActionListener(e -> {
            if (validateInput()) {
                toaEntity = new Toa();
                toaEntity.setMaToa(txtMaToa.getText().trim());
                toaEntity.setTenToa(txtTenToa.getText().trim());
                toaEntity.setSoGhe(Integer.parseInt(txtSoGhe.getText().trim()));
                toaEntity.setLoaiToa((LoaiToa) cbLoaiToa.getSelectedItem());
                toaEntity.setTau(tauSelected);
                
                confirmed = true;
                dispose();
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    private boolean validateInput() {
        if (txtMaToa.getText().isEmpty() || txtTenToa.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã và Tên toa!");
            return false;
        }
        try {
            int ghe = Integer.parseInt(txtSoGhe.getText());
            if (ghe <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số ghế phải là số nguyên dương!");
            return false;
        }
        return true;
    }

    // Gán dữ liệu khi Sửa
    public void setEntity(Toa t) {
        this.toaEntity = t;
        txtMaToa.setText(t.getMaToa());
        txtMaToa.setEditable(false); // Không cho sửa mã PK
        txtTenToa.setText(t.getTenToa());
        txtSoGhe.setText(String.valueOf(t.getSoGhe()));
        // Chọn đúng loại toa trong combo
        for (int i = 0; i < cbLoaiToa.getItemCount(); i++) {
            if (cbLoaiToa.getItemAt(i).getMaLoaiToa().equals(t.getLoaiToa().getMaLoaiToa())) {
                cbLoaiToa.setSelectedIndex(i);
                break;
            }
        }
    }

    public Toa getEntity() { return toaEntity; }
    public boolean isConfirmed() { return confirmed; }
}