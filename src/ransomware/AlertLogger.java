package ransomware;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlertLogger {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = timestamp + " - " + message;

        System.out.println("[AlertLogger] " + logEntry);

        try (FileWriter fw = new FileWriter("alerts.log", true)) {
            fw.write(logEntry + "\n");
        } catch (IOException e) {
            System.out.println("[Error] Failed to log alert: " + e.getMessage());
        }
    }
}


