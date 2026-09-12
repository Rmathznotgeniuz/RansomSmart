package ransomware;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class FileMonitor {
    private GUI gui;
    MassModificationDetector massDetector = new MassModificationDetector();
    RansomNoteDetector noteDetector = new RansomNoteDetector();
    ExtensionChangeDetector extDetector = new ExtensionChangeDetector();
    NetworkSecurityModule networkSecurity = new NetworkSecurityModule();

    private Set<String> handledFiles = new HashSet<>();

    public FileMonitor(GUI gui) {
        this.gui = gui;
    }

    public void startMonitoring() {
        Path path = Paths.get("monitor_folder");
        gui.appendLog("[Monitor] Monitoring started on: " + path.toAbsolutePath());
        networkSecurity.startMonitoring(gui);

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

                    if ((kind == ENTRY_CREATE || kind == ENTRY_MODIFY) && !handledFiles.contains(fileName.toString())) {
                        if (kind == ENTRY_CREATE) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        inspectFile(Paths.get(fullPath), true, true);
                        handledFiles.add(fileName.toString());
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

    /** Performs a one-off scan of every regular file currently in monitor_folder. */
    public void scanNow() {
        Path directory = Paths.get("monitor_folder");
        gui.appendLog("[Scan] Scanning " + directory.toAbsolutePath());
        if (!Files.isDirectory(directory)) {
            gui.appendLog("[Scan] Monitored directory does not exist.");
            return;
        }

        try (Stream<Path> files = Files.list(directory)) {
            files.filter(Files::isRegularFile).forEach(file -> inspectFile(file, false, false));
            networkSecurity.scanConnections(gui);
            gui.appendLog("[Scan] Completed.");
        } catch (IOException e) {
            gui.appendLog("[Scan] Failed: " + e.getMessage());
        }
    }

    private void inspectFile(Path file, boolean includeMassModificationState, boolean decoyEvent) {
        boolean alertRaised = false;
        String filename = file.getFileName().toString();

        if (decoyEvent && filename.contains("decoy_document")) {
            String message = "Decoy file accessed or modified: " + filename;
            gui.appendLog("[ALERT] " + message);
            AlertLogger.log(message);
            DatabaseService.saveAlert(message);
            alertRaised = true;
        }

        // Extension detection may move the file, so do not subsequently try to read it.
        if (extDetector.checkExtensionChange(filename)) {
            alertRaised = true;
        } else if (noteDetector.scanFile(file.toString())) {
            alertRaised = true;
        }
        if (includeMassModificationState && massDetector.isAlertRaised()) {
            alertRaised = true;
        }

        if (alertRaised) {
            networkSecurity.scanConnections(gui);
            massDetector.resetAlert();
        }
    }
}







