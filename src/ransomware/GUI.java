package ransomware;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

public class GUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextArea logArea;
    private Font orbitronFont;

    public GUI() {
        loadOrbitronFont();

        setTitle("RANSOMSMART Anti-Ransomware System");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(10, 10, 30));

        JPanel header = new JPanel();
        header.setBackground(new Color(20, 20, 50));
        header.setPreferredSize(new Dimension(700, 50));
        header.setLayout(new BorderLayout());

        URL iconUrl = getClass().getResource("/icon/redhat.png");
        ImageIcon icon = null;
        if (iconUrl != null) {
            Image scaledImg = new ImageIcon(iconUrl).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImg);
        }

        JLabel title = new JLabel(" Anti-Ransomware Dashboard", icon, JLabel.LEADING);
        title.setForeground(Color.cyan);
        title.setFont(orbitronFont != null ? orbitronFont.deriveFont(Font.BOLD, 24f)
                                          : new Font("SansSerif", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        header.add(title, BorderLayout.WEST);

        
        logArea = new JTextArea();
        logArea.setFont(orbitronFont != null ? orbitronFont.deriveFont(Font.PLAIN, 14f)
                                             : new Font("Monospaced", Font.PLAIN, 14));
        logArea.setForeground(Color.green);
        logArea.setBackground(new Color(5, 5, 20));
        logArea.setEditable(false);
        logArea.setBorder(new LineBorder(Color.cyan, 2, true));

        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(20, 20, 50));

        
        JButton refreshBtn = new JButton("Scan Now");
        refreshBtn.setFont(orbitronFont != null ? orbitronFont.deriveFont(Font.BOLD, 16f)
                                                : new Font("SansSerif", Font.BOLD, 16));
        refreshBtn.setForeground(Color.cyan);
        refreshBtn.setBackground(new Color(0, 100, 160));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.cyan, 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        refreshBtn.addActionListener(e -> appendLog("Manual scan triggered..."));
        bottomPanel.add(refreshBtn);

        JButton startMonitorBtn = new JButton("Start Monitoring");
        startMonitorBtn.setFont(orbitronFont != null ? orbitronFont.deriveFont(Font.BOLD, 16f)
                                                    : new Font("SansSerif", Font.BOLD, 16));
        startMonitorBtn.setForeground(Color.cyan);
        startMonitorBtn.setBackground(new Color(0, 100, 160));
        startMonitorBtn.setFocusPainted(false);
        startMonitorBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.cyan, 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        startMonitorBtn.addActionListener(e -> {
            appendLog("Starting monitoring...");
            FileMonitor monitor = new FileMonitor(this);
            new Thread(monitor::startMonitoring).start();
        });
        bottomPanel.add(startMonitorBtn);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        
        redirectSystemStreams();

        appendLog("=== Anti-Ransomware System Started ===");
        appendLog("Ready to monitor folder for threats...");
    }

    private void loadOrbitronFont() {
        try {
            File fontFile = new File("resources/font/Orbitron-Regular.ttf");
            if (!fontFile.exists())
                fontFile = new File("font/Orbitron-Regular.ttf");
            orbitronFont = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(fontFile));
        } catch (Exception e) {
            System.out.println("Failed to load Orbitron font. Using default.");
            orbitronFont = null;
        }
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                appendText(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                appendText(new String(b, off, len));
            }

            private void appendText(final String text) {
                SwingUtilities.invokeLater(() -> logArea.append(text));
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}


