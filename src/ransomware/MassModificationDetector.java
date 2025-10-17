package ransomware;

import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
public class MassModificationDetector 
{
    QuarantineService quarantine = new QuarantineService();
    private int modificationCount = 0;
    private long startTime;
    private volatile boolean alertRaised = false;

    public void checkModification() 
    {
        Path path = Paths.get("monitor_folder"); 

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) 
        {
            path.register(watchService, ENTRY_MODIFY);

            startTime = System.currentTimeMillis();
            System.out.println("[Detector] Watching folder for modifications: " + path.toAbsolutePath());

            while (true) 
            {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) 
                {
                    if (event.kind() == ENTRY_MODIFY) 
                    {
                        modificationCount++;
                        System.out.println("[Event] File modified: " + event.context());

                        if (System.currentTimeMillis() - startTime < 10000 && modificationCount > 5) 
                        {
                            if (!alertRaised) 
                            {
                                System.out.println("[ALERT] Mass modification detected!");
                                quarantine.quarantine(event.context().toString());
                                AlertLogger.log("Mass modification detected in: " + event.context());
                                DatabaseService.saveAlert("Mass modification detected in: " + event.context());
                                alertRaised = true;
                            }
                            modificationCount = 0;
                            startTime = System.currentTimeMillis();
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) 
                {
                    System.out.println("[Info] WatchKey no longer valid, stopping monitoring.");
                    break;
                }
            }
        } 
        catch (IOException | InterruptedException e) 
        {
            System.out.println("[Error] Monitoring failed: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }



    public boolean isAlertRaised() 
    {
        return alertRaised;
    }


    public void resetAlert() 
    {
        alertRaised = false;
    }
    

}

