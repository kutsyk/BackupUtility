package com.kutsyk.backup.service;

import com.kutsyk.backup.ftp.FTPException;
import com.kutsyk.backup.ftp.FTPUtility;
import com.kutsyk.backup.zip.ZipDirectory;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by KutsykV on 09.06.2015.
 */
public class BackupService {

    private ZipDirectory zipDirectory;
    private String localFolder = "";
    private String localZipPath = "";
    private String ftpPath = "";

    private String server = "";
    private int port = 21;
    private String user = "";
    private String pass = "";
    private String time = "";
    private FTPUtility utility;

    public BackupService() {
        zipDirectory = new ZipDirectory();
        File currentDirectory = new File(new File(".").getAbsolutePath());
        try {
            localFolder = currentDirectory.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fillFtpData();
        utility = new FTPUtility(server, port, user, pass);
        try {
            utility.connect();
        } catch (FTPException e) {
            e.printStackTrace();
        }
    }

    void fillFtpData() {
        Properties defaultProps = new Properties();
        try {
            FileInputStream in = new FileInputStream(localFolder+"\\properties.txt");
            defaultProps.load(in);
            server = defaultProps.getProperty("ftp");
            port = Integer.parseInt(defaultProps.getProperty("port", "21"));
            user = defaultProps.getProperty("user");
            pass = defaultProps.getProperty("pass");
            ftpPath = defaultProps.getProperty("path");
            time = defaultProps.getProperty("time");
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doBackup() {
        System.out.println(existsTodaysBackup());
//        if(!existsTodaysBackup() && timeToStartBackup()) {
            zipFolder();
            sendToFTP();
//            deleteZipFolder();
//        }
    }

    private boolean existsTodaysBackup(){
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(currentDate);
        return utility.dirExists(ftpPath,today);
    }

    private boolean timeToStartBackup(){
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String today = sdf.format(currentDate);
        return today.equals(time);
    }

    void zipFolder() {
        localZipPath = localFolder + "\\"+localFolder.substring(localFolder.lastIndexOf("\\")+1)+".zip";
        File zip = new File(localZipPath);
        if(zip.exists())
            zip.delete();
        File directoryToZip = new File(localFolder);
        List<File> fileList = new ArrayList<File>();
        zipDirectory.getAllFiles(directoryToZip, fileList);
        zipDirectory.writeZipFile(directoryToZip, fileList);
//        deleteFiles(fileList);
    }

    private void deleteFiles(List<File> fileList) {
        for (File file : fileList) {
            if (!file.getName().equals("properties.txt"))
                file.delete();
        }
    }

    private void deleteZipFolder(){
        File file = new File(localZipPath);
        file.delete();
    }

    void sendToFTP() {
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(currentDate);
        String remoteFile = ftpPath + "/" + today;
        try {
            utility.connect();
            File uploadFile = new File(localZipPath);
            utility.uploadFile(uploadFile, remoteFile);
            FileInputStream inputStream = new FileInputStream(uploadFile);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                utility.writeFileBytes(buffer, 0, bytesRead);
            }
            inputStream.close();
            utility.finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                utility.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
