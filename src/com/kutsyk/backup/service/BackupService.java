package com.kutsyk.backup.service;

import com.kutsyk.backup.ftp.FTPException;
import com.kutsyk.backup.ftp.FTPUtility;
import com.kutsyk.backup.zip.ZipDirectory;

import java.io.*;
import java.lang.management.BufferPoolMXBean;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

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

    public BackupService() {
        zipDirectory = new ZipDirectory();
        localFolder = System.getProperty("user.dir");
        fillFtpData();
    }

    void fillFtpData() {
        Properties defaultProps = new Properties();
        try {
            FileInputStream in = new FileInputStream("properties.txt");
            defaultProps.load(in);
            server = defaultProps.getProperty("ftp");
            user = defaultProps.getProperty("user");
            pass = defaultProps.getProperty("pass");
            ftpPath = defaultProps.getProperty("path");
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doBackup() {
        zipFolder();
        sendToFTP();
    }

    void zipFolder() {
        File directoryToZip = new File(localFolder);
        List<File> fileList = new ArrayList<File>();
        zipDirectory.getAllFiles(directoryToZip, fileList);
        System.out.println("Local: " + localFolder);
        System.out.println(directoryToZip);
        zipDirectory.writeZipFile(directoryToZip, fileList);
        localFolder += directoryToZip + ".zip";
    }

    void sendToFTP() {
        FTPUtility utility = new FTPUtility(server, port, user, pass);
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String today = sdf.format(currentDate);
        String remoteFile = ftpPath + "/" + today + ".zip";
        try {
            utility.connect();
            File uploadFile = new File(localFolder);
            utility.uploadFile(uploadFile, remoteFile);
            FileInputStream inputStream = new FileInputStream(uploadFile);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                utility.writeFileBytes(buffer, 0, bytesRead);
            }
            inputStream.close();
            utility.finish();
            System.out.println("GINISH");
        } catch (Exception ex) {
        } finally {
            try {
                utility.disconnect();
            } catch (FTPException e) {
                e.printStackTrace();
            }
        }
        System.out.println("END");
    }
}
