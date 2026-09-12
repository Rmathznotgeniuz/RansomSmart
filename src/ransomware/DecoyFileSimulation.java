package ransomware;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DecoyFileSimulation {
    private final File decoyFile;

    public DecoyFileSimulation(File decoyFile) {
        this.decoyFile = decoyFile;
    }

   
    public void simulateRansomwareAccess() {
        new Thread(() -> {
            try {
                if (decoyFile.exists()) {
                    Files.write(decoyFile.toPath(), "Encrypted by ransomware simulation.".getBytes());
                    System.out.println("Ransomware simulation: Decoy file modified.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}


