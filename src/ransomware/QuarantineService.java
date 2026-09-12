package ransomware;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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
        Path monitorDirectory = Paths.get("monitor_folder").toAbsolutePath().normalize();
        Path sourcePath = monitorDirectory.resolve(filename).normalize();

        if (!sourcePath.startsWith(monitorDirectory)) {
            System.out.println("[QuarantineService] Refusing to quarantine a path outside the monitored directory.");
            return;
        }

        try {
            if (Files.exists(sourcePath)) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
                String safeName = sourcePath.getFileName().toString();
                String quarantinedName = timestamp + "-" + UUID.randomUUID().toString().substring(0, 8) + "-" + safeName;
                Path targetPath = Paths.get(QUARANTINE_DIR, quarantinedName);

                Files.move(sourcePath, targetPath);
                String message = "File quarantined: " + safeName + " -> " + quarantinedName;
                System.out.println("[QuarantineService] " + message);
                AlertLogger.log(message);
            } else {
                System.out.println("[QuarantineService] File not found for quarantine: " + filename);
            }
        } catch (IOException e) {
            System.out.println("[QuarantineService] Failed to quarantine file: " + filename + " - " + e.getMessage());
            AlertLogger.log("Failed to quarantine file: " + filename + " - " + e.getMessage());
        }
    }
}


