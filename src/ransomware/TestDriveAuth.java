package ransomware;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class TestDriveAuth {
    public static void main(String[] args) {
        try {
            Drive driveService = GoogleDriveService.getDriveService();
            System.out.println("Authentication successful! Google Drive service is ready.");

            FileList result = driveService.files().list()
                    .setPageSize(10)
                    .setFields("files(id, name)")
                    .execute();

            System.out.println("Files in your drive:");
            for (File file : result.getFiles()) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        } catch (Exception e) {
            System.out.println("Authentication failed.");
            e.printStackTrace();
        }
    }
}


