import com.kutsyk.backup.run.Starter;
import com.kutsyk.backup.service.BackupService;

import java.net.URL;

public class Main {

    public static void main(String[] args) {
        Starter starter = new Starter();
        BackupService backupService = new BackupService();
        while(true) {
            backupService.doBackup();
        }
    }
}
