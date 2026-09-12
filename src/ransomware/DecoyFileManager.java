package ransomware;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DecoyFileManager {
    private final File decoyDir;

    public DecoyFileManager(String directoryPath) {
        this.decoyDir = new File(directoryPath);
        if (!decoyDir.exists()) {
            decoyDir.mkdirs();
        }
    }

    public void createDecoyFile(String fileName, String content) {
        try {
            File decoyFile = new File(decoyDir, fileName);
            if (!decoyFile.exists()) {
                Files.write(decoyFile.toPath(), content.getBytes());
                System.out.println("Decoy file created: " + decoyFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

