package ransomware;

public class ExtensionChangeDetector 
{
    QuarantineService quarantine = new QuarantineService();
    public boolean checkExtensionChange(String file) 
    {
        if (file != null &&
           (file.endsWith(".locked") || file.endsWith(".encrypted"))) 
        {
            System.out.println("[ALERT] Suspicious extension change: " + file);
            quarantine.quarantine(file);
            AlertLogger.log("Suspicious extension detected: " + file);
            DatabaseService.saveAlert("Suspicious extension detected: " + file);
            return true;
        }
        return false;
    }
}
