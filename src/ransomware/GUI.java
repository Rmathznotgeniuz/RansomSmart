package ransomware;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;

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
        title.setFont(orbitronFont != null ? orbitronFont.deriveFont(Font.BOLD, 24f) : new Font("SansSerif", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        header.add(title, BorderLayout.WEST);

        logArea = new JTextArea();
        logArea.setFont(orbitronFont != null ? orbitronFont.deriveFont(Font.PLAIN, 14f) : new Font("Monospaced", Font.PLAIN, 14));
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
        refreshBtn.setFont(orbitronFont != null ? orbitronFont.deriveFont(Font.BOLD, 16f) : new Font("SansSerif", Font.BOLD, 16));
        refreshBtn.setForeground(Color.cyan);
        refreshBtn.setBackground(new Color(0, 100, 160));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.cyan, 2), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        refreshBtn.addActionListener(e -> {
            appendLog("Manual scan triggered...");
            new Thread(() -> new FileMonitor(this).scanNow(), "manual-file-scan").start();
        });
        bottomPanel.add(refreshBtn);

        JButton startMonitorBtn = new JButton("Start Monitoring");
        startMonitorBtn.setFont(orbitronFont != null ? orbitronFont.deriveFont(Font.BOLD, 16f) : new Font("SansSerif", Font.BOLD, 16));
        startMonitorBtn.setForeground(Color.cyan);
        startMonitorBtn.setBackground(new Color(0, 100, 160));
        startMonitorBtn.setFocusPainted(false);
        startMonitorBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.cyan, 2), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        startMonitorBtn.addActionListener(e -> {
            appendLog("Starting monitoring...");
            FileMonitor monitor = new FileMonitor(this);
            new Thread(monitor::startMonitoring).start();
        });
        bottomPanel.add(startMonitorBtn);

        JButton simulateRansomwareButton = new JButton("Simulate Ransomware on Decoy");
        simulateRansomwareButton.setFont(orbitronFont != null ? orbitronFont.deriveFont(Font.BOLD, 16f) : new Font("SansSerif", Font.BOLD, 16));
        simulateRansomwareButton.setForeground(Color.cyan);
        simulateRansomwareButton.setBackground(new Color(180, 30, 30));
        simulateRansomwareButton.setFocusPainted(false);
        simulateRansomwareButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.cyan, 2), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        simulateRansomwareButton.addActionListener(e -> {
            String monitoredDir = "monitor_folder";
            java.io.File decoyFile = new java.io.File(monitoredDir, "decoy_document.docx");
            DecoyFileSimulation simulation = new DecoyFileSimulation(decoyFile);
            simulation.simulateRansomwareAccess();
            appendLog("Simulated ransomware attack started on decoy!");
            JOptionPane.showMessageDialog(this, "Simulated ransomware attack started on decoy!");
        });
        bottomPanel.add(simulateRansomwareButton);

        JButton driveBtn = new JButton("Google Drive");
        driveBtn.setFont(orbitronFont != null ? orbitronFont.deriveFont(Font.BOLD, 16f) : new Font("SansSerif", Font.BOLD, 16));
        driveBtn.setForeground(Color.cyan);
        driveBtn.setBackground(new Color(80, 0, 160));
        driveBtn.setFocusPainted(false);
        driveBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.cyan, 2), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        driveBtn.addActionListener(e -> {
            appendLog("Connecting to Google Drive...");
            try {
                Drive driveService = GoogleDriveService.getDriveService();
                showDriveFilesPanel(driveService);
            } catch (Exception ex) {
                appendLog("Drive error: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Drive error: " + ex.getMessage());
            }
        });
        bottomPanel.add(driveBtn);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        redirectSystemStreams();

        appendLog("=== Anti-Ransomware System Started ===");
        appendLog("Ready to monitor folder for threats...");
    }

    private void showDriveFilesPanel(Drive driveService) {
        JDialog dialog = new JDialog(this, "Google Drive Files", true);
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(21, 32, 43));
        dialog.setLayout(new BorderLayout());

        String[] columns = { "File Name", "File ID" };
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        table.setBackground(new Color(33, 42, 62));
        table.setForeground(Color.CYAN);
        table.setFont(new Font("Consolas", Font.PLAIN, 14));
        table.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(21, 32, 43));
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(21, 32, 43));
        JButton uploadButton = new JButton("Upload");
        JButton downloadButton = new JButton("Download");
        uploadButton.setBackground(Color.DARK_GRAY);
        uploadButton.setForeground(Color.GREEN);
        downloadButton.setBackground(Color.DARK_GRAY);
        downloadButton.setForeground(Color.MAGENTA);
        buttonPanel.add(uploadButton);
        buttonPanel.add(downloadButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        uploadButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                try {
                    File driveFile = new File();
                    driveFile.setName(file.getName());
                    FileContent mediaContent = new FileContent(Files.probeContentType(file.toPath()), file);
                    driveService.files().create(driveFile, mediaContent)
                            .setFields("id")
                            .execute();
                    JOptionPane.showMessageDialog(dialog, "Uploaded: " + file.getName());
                    appendLog("Drive upload complete: " + file.getName());
                    // Reload the table
                    refreshDriveTable(driveService, tableModel);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Upload failed: " + ex.getMessage());
                }
            }
        });

        downloadButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String fileId = tableModel.getValueAt(row, 1).toString();
                String fileName = tableModel.getValueAt(row, 0).toString();

                try {
                    // Fetch file metadata to get mimeType
                    File fileMetadata = driveService.files().get(fileId).setFields("mimeType").execute();
                    String mimeType = fileMetadata.getMimeType();

                    JFileChooser chooser = new JFileChooser();
                    chooser.setSelectedFile(new java.io.File(fileName));
                    int result = chooser.showSaveDialog(dialog);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        java.io.File saveTo = chooser.getSelectedFile();

                        try (OutputStream os = new FileOutputStream(saveTo)) {
                            if (mimeType.equals("application/vnd.google-apps.document")) {
                                
                                driveService.files().export(fileId, "application/pdf").executeMediaAndDownloadTo(os);
                            } else if (mimeType.equals("application/vnd.google-apps.spreadsheet")) {
                                
                                driveService.files().export(fileId, "text/csv").executeMediaAndDownloadTo(os);
                            } else if (mimeType.equals("application/vnd.google-apps.presentation")) {
                                
                                driveService.files().export(fileId, "application/pdf").executeMediaAndDownloadTo(os);
                            } else {
                                
                                driveService.files().get(fileId).executeMediaAndDownloadTo(os);
                            }
                        }
                        JOptionPane.showMessageDialog(dialog, "Downloaded: " + fileName);
                        appendLog("Drive download complete: " + fileName);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Download failed: " + ex.getMessage());
                }
            }
        });

        refreshDriveTable(driveService, tableModel);
        dialog.setVisible(true);
    }

    private void refreshDriveTable(Drive driveService, DefaultTableModel tableModel) {
        try {
            FileList result = driveService.files().list()
                    .setPageSize(20)
                    .setFields("files(id, name)")
                    .execute();
            tableModel.setRowCount(0);
            for (File file : result.getFiles()) {
                tableModel.addRow(new Object[] { file.getName(), file.getId() });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load files: " + e.getMessage());
        }
    }

    private void loadOrbitronFont() {
        try {
            java.io.File fontFile = new java.io.File("resources/font/Orbitron-Regular.ttf");
            if (!fontFile.exists())
                fontFile = new java.io.File("font/Orbitron-Regular.ttf");
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
