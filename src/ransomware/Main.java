package ransomware;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        String monitoredDir = "monitor_folder"; 
        DecoyFileManager decoyManager = new DecoyFileManager(monitoredDir);
        decoyManager.createDecoyFile("decoy_document.docx", "This is a decoy file.");
       

        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}







