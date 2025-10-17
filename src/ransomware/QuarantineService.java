package ransomware;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class QuarantineService {
    private static final String QUARANTINE_DIR = "quarantine";

    public QuarantineService() {
        File quarantineFolder = new File(QUARANTINE_DIR);
        if (!quarantineFolder.exists()) {
            boolean created = quarantineFolder.mkdirs();
            if (created) {
                System.out.println("[QuarantineService] Quarantine directory created at: " + quarantineFolder.getAbsolutePath());
            } else {
                System.out.println("[QuarantineService] Failed to create quarantine directory.");
            }
        }
    }


    public void quarantine(String filename) {
        Path sourcePath = Paths.get("monitor_folder", filename);
        Path targetPath = Paths.get(QUARANTINE_DIR, filename);

        try {
            if (Files.exists(sourcePath)) {
                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[QuarantineService] File quarantined: " + filename);
                AlertLogger.log("File quarantined: " + filename);
            } else {
                System.out.println("[QuarantineService] File not found for quarantine: " + filename);
            }
        } catch (IOException e) {
            System.out.println("[QuarantineService] Failed to quarantine file: " + filename + " - " + e.getMessage());
            AlertLogger.log("Failed to quarantine file: " + filename + " - " + e.getMessage());
        }
    }
}


