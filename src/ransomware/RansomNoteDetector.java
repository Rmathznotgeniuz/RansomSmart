package ransomware;

import java.io.*;
public class RansomNoteDetector 
{
    QuarantineService quarantine = new QuarantineService();
    public boolean scanFile(String filename) {
        boolean foundRansom = false;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.contains("ransom") || lowerLine.contains("bitcoin") || lowerLine.contains("decrypt")) {
                    System.out.println("[ALERT] Ransom note keywords detected in: " + filename);
                    foundRansom = true;
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("[Error] Unable to scan file: " + filename);
        }

        if (foundRansom) {
            try {
                Thread.sleep(50); 
            } catch (InterruptedException ignored) {}

            quarantine.quarantine(new File(filename).getName());
            AlertLogger.log("Ransom note found in: " + filename);
            DatabaseService.saveAlert("Ransom note found in: " + filename);
            return true;
        }
        return false;
    }

}


