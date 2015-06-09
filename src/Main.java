import com.kutsyk.backup.service.BackupService;

public class Main {

    public static void main(String[] args) {
        BackupService backupService = new BackupService();
        backupService.doBackup();
    }
}
