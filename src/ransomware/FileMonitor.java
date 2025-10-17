package ransomware;

import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.util.HashSet;
import java.util.Set;

public class FileMonitor {
    private GUI gui;
    MassModificationDetector massDetector = new MassModificationDetector();
    RansomNoteDetector noteDetector = new RansomNoteDetector();
    ExtensionChangeDetector extDetector = new ExtensionChangeDetector();
    NetworkActivityMonitor netMonitor = new NetworkActivityMonitor();

    private Set<String> handledFiles = new HashSet<>();

    public FileMonitor(GUI gui) {
        this.gui = gui;
    }

    public void startMonitoring() {
        Path path = Paths.get("monitor_folder");
        gui.appendLog("[Monitor] Monitoring started on: " + path.toAbsolutePath());

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            new Thread(() -> massDetector.checkModification()).start();

            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path fileName = (Path) event.context();
                    String fullPath = path.resolve(fileName).toString();

                    gui.appendLog("[Event] " + kind.name() + ": " + fileName);

                    boolean alertRaised = false;

                    if ((kind == ENTRY_CREATE || kind == ENTRY_MODIFY) && !handledFiles.contains(fileName.toString())) {
                        if (kind == ENTRY_CREATE) {
                            try {
                                Thread.sleep(200); 
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        if (extDetector.checkExtensionChange(fileName.toString())) alertRaised = true;
                        if (noteDetector.scanFile(fullPath)) alertRaised = true;
                        if (massDetector.isAlertRaised()) alertRaised = true;

                        if (alertRaised) {
                            netMonitor.scanConnections();
                            massDetector.resetAlert();
                            handledFiles.add(fileName.toString());
                        }
                    }
                }

                if (!key.reset()) {
                    gui.appendLog("[Monitor] WatchKey invalid, stopping.");
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            gui.appendLog("[Error] Monitoring failed: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}






