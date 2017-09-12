package com.kutsyk.backup.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by KutsykV on 10.06.2015.
 */
public class Starter {

    private String startUpFolder = "";

    public Starter(){
        startUpFolder = System.getProperty("user.home")+"\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
        File batFile = new File(startUpFolder+"\\CHWBackup.bat");
        if(!batFile.exists()) {
            try {
                batFile.createNewFile();
                createBatFile(batFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }

    public void createBatFile(File batFile){
        String localPath = "";
        File currentDirectory = new File(new File(".").getAbsolutePath());
        try {
            localPath = currentDirectory.getCanonicalPath();
            PrintWriter writer = new PrintWriter(batFile);
            writer.println("cd " + localPath);
            writer.println(localPath.substring(0, localPath.indexOf(":")+1));
            writer.println("javaw -jar "+localPath+"\\BackupUtility.jar");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
