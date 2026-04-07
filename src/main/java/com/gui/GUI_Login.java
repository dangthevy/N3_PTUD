package com.gui;

import com.dao.DAO_NhanVien;
import com.entities.NhanVien;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Properties;
import java.util.Random;
import java.util.prefs.Preferences;
import javax.mail.*;
import javax.mail.internet.*;

public class GUI_Login extends JPanel implements ActionListener {

    private JFrame parentFrame;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JToggleButton btnShowPass;
    private JButton btnLogin;

    private DAO_NhanVien nv_dao;

    private JLabel lblUserError;
    private JLabel lblPassError;
    private Image backgroundImage;

    private int failedAttempts = 0;
    private boolean isPermanentlyLocked = false;
    private Timer countdownTimer;

    private Preferences prefs = Preferences.userNodeForPackage(GUI_Login.class);
    private final String HISTORY_KEY = "login_history";

    private JPopupMenu suggestionMenu;
    private JList<String> listSuggestions;

    private String currentOtpGenerated;

    public GUI_Login() {
        try {
            java.net.URL imgURL = getClass().getResource("/com/img/doantau.png");
            if (imgURL != null) {
                backgroundImage = new ImageIcon(imgURL).getImage();
            }
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh: /com/img/doantau.png");
        }

        this.nv_dao = new DAO_NhanVien(com.connectDB.ConnectDB.getConnection());

        setLayout(new GridBagLayout());

        JPanel loginPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 190));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                super.paintComponent(g);
            }
        };
        loginPanel.setOpaque(false);
        loginPanel.setPreferredSize(new Dimension(420, 560));
        loginPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 5, 0);
        gc.gridx = 0;

        JLabel lblApp = new JLabel("GA TÀU SÀI GÒN", SwingConstants.CENTER);
        lblApp.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblApp.setForeground(new Color(255, 193, 7));

        JLabel lblLoginNow = new JLabel("LOGIN NOW", SwingConstants.CENTER);
        lblLoginNow.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblLoginNow.setForeground(Color.WHITE);

        txtUsername = createStyledTextField();
        lblUserError = createErrorLabel();
        lblPassError = createErrorLabel();

        // Thiết lập gợi ý thông minh
        setupSuggestionMenu();

        txtUsername.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (txtUsername.isEditable() && txtUsername.getText().isEmpty()) {
                    updateSuggestions("");
                }
            }
        });

        txtUsername.addActionListener(e -> txtPassword.requestFocus());

        JLabel lblForgotPass = new JLabel("Quên mật khẩu?");
        lblForgotPass.setForeground(new Color(255, 193, 7));
        lblForgotPass.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblForgotPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgotPass.setHorizontalAlignment(SwingConstants.RIGHT);
        lblForgotPass.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleForgotPassword();
            }
        });

        btnLogin = new JButton("ĐĂNG NHẬP");
        styleButton(btnLogin);
        btnLogin.addActionListener(e -> handleLogin());

        gc.gridy = 0;
        loginPanel.add(lblApp, gc);
        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 30, 0);
        loginPanel.add(lblLoginNow, gc);
        gc.gridy = 2;
        gc.insets = new Insets(5, 0, 2, 0);
        JLabel uLabel = new JLabel("Tài khoản");
        uLabel.setForeground(Color.LIGHT_GRAY);
        loginPanel.add(uLabel, gc);
        gc.gridy = 3;
        loginPanel.add(txtUsername, gc);
        gc.gridy = 4;
        loginPanel.add(lblUserError, gc);
        gc.gridy = 5;
        JLabel pLabel = new JLabel("Mật khẩu");
        pLabel.setForeground(Color.LIGHT_GRAY);
        loginPanel.add(pLabel, gc);
        gc.gridy = 6;
        loginPanel.add(createPasswordPanel(), gc);
        gc.gridy = 7;
        loginPanel.add(lblPassError, gc);
        gc.gridy = 8;
        gc.insets = new Insets(-5, 0, 20, 0);
        loginPanel.add(lblForgotPass, gc);
        gc.gridy = 10;
        gc.insets = new Insets(10, 0, 10, 0);
        loginPanel.add(btnLogin, gc);

        add(loginPanel);
    }

    // --- LOGIC GỢI Ý THÔNG MINH ---
    private void setupSuggestionMenu() {
        suggestionMenu = new JPopupMenu();
        suggestionMenu.setFocusable(false); // Không chiếm focus để người dùng gõ tiếp được

        listSuggestions = new JList<>();
        listSuggestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSuggestions.setBackground(Color.WHITE);
        listSuggestions.setFont(new Font("SansSerif", Font.PLAIN, 14));

        listSuggestions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selected = listSuggestions.getSelectedValue();
                if (selected != null) {
                    txtUsername.setText(selected);
                    suggestionMenu.setVisible(false);
                    lblUserError.setText(" ");
                    updateFieldBorder(txtUsername, new Color(255, 193, 7));
                    txtPassword.requestFocus();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(listSuggestions);
        scroll.setBorder(null);
        suggestionMenu.add(scroll);

        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Xử lý lỗi trống
                String input = txtUsername.getText().trim();
                if (input.isEmpty()) {
                    lblUserError.setText("Tài khoản không được để trống!");
                    updateFieldBorder(txtUsername, Color.RED);
                } else {
                    lblUserError.setText(" ");
                    updateFieldBorder(txtUsername, new Color(255, 193, 7));
                }

                // Phím mũi tên xuống để chọn gợi ý
                if (e.getKeyCode() == KeyEvent.VK_DOWN && suggestionMenu.isVisible()) {
                    listSuggestions.requestFocus();
                    listSuggestions.setSelectedIndex(0);
                    return;
                }

                // Cập nhật gợi ý theo nội dung nhập
                updateSuggestions(input);
            }
        });
    }

    private void updateSuggestions(String input) {
        String history = prefs.get(HISTORY_KEY, "");
        if (history.isEmpty()) return;

        String[] allUsers = history.split(",");
        DefaultListModel<String> model = new DefaultListModel<>();
        boolean hasMatch = false;

        for (String user : allUsers) {
            // Nếu input trống thì hiện tất cả, nếu có input thì lọc
            if (input.isEmpty() || user.toLowerCase().startsWith(input.toLowerCase())) {
                model.addElement(user);
                hasMatch = true;
            }
        }

        if (hasMatch) {
            listSuggestions.setModel(model);
            suggestionMenu.setPreferredSize(new Dimension(txtUsername.getWidth(), Math.min(model.getSize() * 30, 150)));
            if (!suggestionMenu.isVisible()) {
                suggestionMenu.show(txtUsername, 0, txtUsername.getHeight());
            }
            txtUsername.requestFocus(); // Đảm bảo focus vẫn ở ô nhập liệu
        } else {
            suggestionMenu.setVisible(false);
        }
    }

    private void saveToHistory(String user) {
        String history = prefs.get(HISTORY_KEY, "");
        LinkedHashSet<String> userSet = new LinkedHashSet<>();
        userSet.add(user);

        if (!history.isEmpty()) {
            Collections.addAll(userSet, history.split(","));
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String s : userSet) {
            if (count > 0) sb.append(",");
            sb.append(s);
            if (++count >= 5) break;
        }
        prefs.put(HISTORY_KEY, sb.toString());
    }

    private JPanel createPasswordPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        txtPassword = new JPasswordField(15);
        txtPassword.setBackground(new Color(255, 255, 255, 240));
        txtPassword.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, new Color(255, 193, 7)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        btnShowPass = new JToggleButton("🔒");
        btnShowPass.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        btnShowPass.setPreferredSize(new Dimension(45, 0));
        btnShowPass.setFocusPainted(false);
        btnShowPass.setBackground(new Color(255, 255, 255, 240));
        btnShowPass.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, new Color(255, 193, 7)));
        btnShowPass.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnShowPass.addActionListener(e -> {
            if (btnShowPass.isSelected()) {
                txtPassword.setEchoChar((char) 0);
                btnShowPass.setText("👁");
            } else {
                txtPassword.setEchoChar('•');
                btnShowPass.setText("🔒");
            }
        });

        txtPassword.addActionListener(e -> btnLogin.doClick());

        pnl.add(txtPassword, BorderLayout.CENTER);
        pnl.add(btnShowPass, BorderLayout.EAST);
        return pnl;
    }

    private void handleLogin() {
        if (isPermanentlyLocked) {
            showPermanentLockAlert();
            return;
        }

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty()) {
            lblUserError.setText("Tài khoản không được để trống!");
            updateFieldBorder(txtUsername, Color.RED);
            txtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            lblPassError.setText("Mật khẩu không được để trống!");
            ((JComponent) txtPassword.getParent()).setBorder(BorderFactory.createLineBorder(Color.RED));
            txtPassword.requestFocus();
            return;
        }

        NhanVien nv = nv_dao.checkLogin(username, password);
        if (nv != null) {
            saveToHistory(username);
            failedAttempts = 0;
            openMainWindow(nv);
        } else {
            failedAttempts++;
            processFailedAttempt();
        }
    }

    private void openMainWindow(NhanVien nv) {
        Window currentWindow = SwingUtilities.getWindowAncestor(this);
        SwingUtilities.invokeLater(() -> {
            JFrame mainFrame = new JFrame("HỆ THỐNG QUẢN LÝ GA TÀU SÀI GÒN");
            GUI_General mainContent = new GUI_General(nv);
            mainFrame.setContentPane(mainContent);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            mainFrame.setVisible(true);
            if (currentWindow != null)
                currentWindow.dispose();
        });
    }

    private void sendEmail(String recipientEmail, String otp) {
        final String myEmail = "ngbathien3101@gmail.com";
        final String myAppPassword = "iklducoetztezmmi";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myEmail, myAppPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("MÃ OTP KHÔI PHỤC MẬT KHẨU - GA TÀU SÀI GÒN");
            String content = "Mã OTP của bạn là: " + otp + "\n\nTrân trọng!";
            message.setText(content);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void handleForgotPassword() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtResetUser = new JTextField();
        JTextField txtResetEmail = new JTextField();
        txtResetUser.addActionListener(e -> txtResetEmail.requestFocus());

        panel.add(new JLabel("Nhập tài khoản:"));
        panel.add(txtResetUser);
        panel.add(new JLabel("Nhập Email đăng ký:"));
        panel.add(txtResetEmail);

        int result = JOptionPane.showConfirmDialog(this, panel, "Khôi phục mật khẩu", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String taiKhoan = txtResetUser.getText().trim();
            String email = txtResetEmail.getText().trim();

            if (nv_dao.verifyUserByEmail(taiKhoan, email)) {
                showOtpVerificationDialog(taiKhoan, email);
            } else {
                JOptionPane.showMessageDialog(this, "Thông tin tài khoản hoặc email không đúng!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showOtpVerificationDialog(String taiKhoan, String email) {
        currentOtpGenerated = String.format("%04d", new Random().nextInt(10000));
        new Thread(() -> sendEmail(email, currentOtpGenerated)).start();

        JDialog otpDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Xác thực OTP", true);
        otpDialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblMsg = new JLabel("<html><center>Mã OTP đã được gửi đến:<br><b>" + email + "</b></center></html>");
        lblMsg.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField txtOtpInput = new JTextField(6);
        txtOtpInput.setFont(new Font("SansSerif", Font.BOLD, 18));
        txtOtpInput.setHorizontalAlignment(JTextField.CENTER);

        JButton btnConfirm = new JButton("Xác nhận");
        JButton btnResend = new JButton("Gửi lại (30s)");
        btnResend.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnResend.setEnabled(false);

        final int[] timeLeft = { 30 };
        Timer resendTimer = new Timer(1000, null);
        resendTimer.addActionListener(e -> {
            timeLeft[0]--;
            if (timeLeft[0] > 0) {
                btnResend.setText("Gửi lại (" + timeLeft[0] + "s)");
            } else {
                btnResend.setText("Gửi lại mã");
                btnResend.setEnabled(true);
                ((Timer) e.getSource()).stop();
            }
        });
        resendTimer.start();

        otpDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                resendTimer.stop();
            }
        });

        btnResend.addActionListener(e -> {
            currentOtpGenerated = String.format("%04d", new Random().nextInt(10000));
            new Thread(() -> sendEmail(email, currentOtpGenerated)).start();
            JOptionPane.showMessageDialog(otpDialog, "Đã gửi lại mã mới!");
            timeLeft[0] = 30;
            btnResend.setEnabled(false);
            resendTimer.start();
        });

        btnConfirm.addActionListener(e -> {
            if (txtOtpInput.getText().trim().equals(currentOtpGenerated)) {
                resendTimer.stop();
                otpDialog.dispose();
                String newPass = JOptionPane.showInputDialog(this, "Nhập mật khẩu mới:");
                if (newPass != null && !newPass.isEmpty()) {
                    if (nv_dao.updatePassword(taiKhoan, newPass)) {
                        JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(otpDialog, "Mã OTP không chính xác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridy = 0;
        otpDialog.add(lblMsg, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 50, 5, 50);
        otpDialog.add(txtOtpInput, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pnlBtns.add(btnConfirm);
        pnlBtns.add(btnResend);
        otpDialog.add(pnlBtns, gbc);

        otpDialog.pack();
        otpDialog.setResizable(false);
        otpDialog.setLocationRelativeTo(this);
        otpDialog.setVisible(true);
    }

    private void processFailedAttempt() {
        if (failedAttempts == 3) {
            JOptionPane.showMessageDialog(this, "Sai 3 lần! Tạm khóa 5 phút.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            startCountdown(5);
        } else if (failedAttempts >= 6) {
            isPermanentlyLocked = true;
            if (countdownTimer != null)
                countdownTimer.stop();
            showPermanentLockAlert();
        } else {
            lblPassError.setText("Sai mật khẩu! (Lần " + failedAttempts + ")");
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    private void startCountdown(int minutes) {
        btnLogin.setEnabled(false);
        txtPassword.setEditable(false);
        btnShowPass.setEnabled(false);
        txtUsername.setEditable(true);

        final int[] secondsLeft = { minutes * 60 };

        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }

        countdownTimer = new Timer(1000, e -> {
            secondsLeft[0]--;
            if (secondsLeft[0] > 0) {
                int m = secondsLeft[0] / 60;
                int s = secondsLeft[0] % 60;
                btnLogin.setText(String.format("Thử lại sau (%02d:%02d)", m, s));
            } else {
                ((Timer) e.getSource()).stop();
                btnLogin.setEnabled(true);
                btnLogin.setText("ĐĂNG NHẬP");
                txtPassword.setEditable(true);
                btnShowPass.setEnabled(true);
                lblPassError.setText(" ");
                failedAttempts = 0;
            }
        });

        countdownTimer.setInitialDelay(0);
        countdownTimer.start();
    }

    private void showPermanentLockAlert() {
        JOptionPane.showMessageDialog(this, "Hệ thống bị khóa vĩnh viễn! LH Quản lý: 0859495852", "KHÓA",
                JOptionPane.ERROR_MESSAGE);
        txtUsername.setEnabled(false);
        txtPassword.setEnabled(false);
        btnLogin.setEnabled(false);
    }

    private void updateFieldBorder(JTextField field, Color color) {
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField(15);
        tf.setBackground(new Color(255, 255, 255, 240));
        updateFieldBorder(tf, new Color(255, 193, 7));
        return tf;
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setForeground(new Color(255, 100, 100));
        lbl.setFont(new Font("Arial", Font.ITALIC, 12));
        return lbl;
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(new Color(255, 193, 7));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 45));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g.setColor(new Color(0, 0, 0, 60));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }
}
