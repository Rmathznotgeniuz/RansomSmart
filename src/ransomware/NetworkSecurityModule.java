package ransomware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Observes active TCP connections on the local computer.  It does not block
 * traffic; it flags connections to ports commonly used by command-and-control
 * tooling and keeps the evidence in the normal alert stores.
 */
public class NetworkSecurityModule {
    private static final Set<Integer> HIGH_RISK_PORTS = Set.of(4444, 5555, 6667, 9050, 1080);

    private final Set<String> reportedConnections = new HashSet<>();
    private final AtomicBoolean monitoring = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;

    public void startMonitoring(GUI gui) {
        if (!monitoring.compareAndSet(false, true)) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "network-security-monitor");
            thread.setDaemon(true);
            return thread;
        });
        gui.appendLog("[Network] Security monitoring started (30-second interval).");
        scheduler.scheduleWithFixedDelay(() -> scanConnections(gui), 0, 30, TimeUnit.SECONDS);
    }

    public void scanConnections(GUI gui) {
        int activeConnections = 0;
        try {
            Process process = new ProcessBuilder("netstat", "-ano").redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 5 || !"TCP".equalsIgnoreCase(parts[0])
                            || !"ESTABLISHED".equalsIgnoreCase(parts[3])) {
                        continue;
                    }

                    activeConnections++;
                    String remoteEndpoint = parts[2];
                    Integer remotePort = extractPort(remoteEndpoint);
                    if (remotePort != null && !isLocalEndpoint(remoteEndpoint)
                            && HIGH_RISK_PORTS.contains(remotePort)) {
                        reportSuspiciousConnection(gui, remoteEndpoint, parts[4]);
                    }
                }
            }
            process.waitFor();
            gui.appendLog("[Network] Checked " + activeConnections + " established TCP connection(s).");
        } catch (IOException e) {
            gui.appendLog("[Network] Unable to inspect local connections: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void reportSuspiciousConnection(GUI gui, String endpoint, String processId) {
        String connection = endpoint + " (PID " + processId + ")";
        if (!reportedConnections.add(connection)) {
            return;
        }

        String message = "Suspicious outbound connection to " + connection;
        gui.appendLog("[ALERT] " + message);
        AlertLogger.log(message);
        DatabaseService.saveAlert(message);
    }

    private Integer extractPort(String endpoint) {
        int separator = endpoint.lastIndexOf(':');
        if (separator < 0 || separator == endpoint.length() - 1) {
            return null;
        }
        try {
            return Integer.parseInt(endpoint.substring(separator + 1));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean isLocalEndpoint(String endpoint) {
        String host = endpoint.substring(0, endpoint.lastIndexOf(':')).replace("[", "").replace("]", "");
        if (host.equals("0.0.0.0") || host.equals("::") || host.equals("::1") || host.startsWith("127.")) {
            return true;
        }
        if (host.startsWith("10.") || host.startsWith("192.168.") || host.startsWith("169.254.")) {
            return true;
        }
        if (host.startsWith("172.")) {
            String[] octets = host.split("\\.");
            if (octets.length > 1) {
                try {
                    int second = Integer.parseInt(octets[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    // The endpoint will simply be treated as non-local.
                }
            }
        }
        String lowerHost = host.toLowerCase();
        return lowerHost.startsWith("fe80:") || lowerHost.startsWith("fc") || lowerHost.startsWith("fd");
    }
}
