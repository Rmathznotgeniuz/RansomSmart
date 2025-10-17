package ransomware;

public class NetworkActivityMonitor {

    public void scanConnections() {
        System.out.println("[Network] Suspicious connection attempt detected (192.168.1.50:4444)");
        AlertLogger.log("Suspicious network activity detected");
        DatabaseService.saveAlert("Suspicious network activity detected");
    }
}
