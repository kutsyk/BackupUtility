package com.kutsyk.backup.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

/**
 * A utility class that provides functionality for uploading files to a FTP
 * server.
 *
 * @author www.codejava.net
 * @author kutsyk
 */
public class FTPUtility {

    private String host;
    private int port;
    private String username;
    private String password;

    private FTPClient ftpClient = new FTPClient();
    private int replyCode;

    private OutputStream outputStream;

    public FTPUtility(String host, int port, String user, String pass) {
        this.host = host;
        this.port = port;
        this.username = user;
        this.password = pass;
    }

    /**
     * Connect and login to the server.
     *
     * @throws FTPException
     */
    public void connect() throws FTPException {
        try {
            ftpClient.connect(host, port);
            replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new FTPException("FTP serve refused connection.");
            }

            boolean logged = ftpClient.login(username, password);
            if (!logged) {
                // failed to login
                ftpClient.disconnect();
                throw new FTPException("Could not login to the server.");
            }

            ftpClient.enterLocalPassiveMode();
        } catch (IOException ex) {
            throw new FTPException("I/O error: " + ex.getMessage());
        }
    }

    /**
     * * This utility class provides a method that creates a nested directory
     * * structure on a FTP server, based on Apache Commons Net library.
     * * @author www.codejava.net
     * *
     */
    public boolean makeDirectories(String dirPath) throws IOException {
        String[] pathElements = dirPath.split("/");
        if (pathElements != null && pathElements.length > 0) {
            for (String singleDir : pathElements) {
                if (singleDir.isEmpty())
                    continue;
                boolean existed = ftpClient.changeWorkingDirectory(singleDir);
                if (!existed) {
                    boolean created = ftpClient.makeDirectory(singleDir);
                    if (created) {
                        ftpClient.changeWorkingDirectory(singleDir);
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean dirExists(String parent, String dir) {
        if (ftpClient.isConnected())
            try {
                for(FTPFile ftpFile: ftpClient.listDirectories(parent))
                    if(ftpFile.getName().equals(dir))
                        return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        return false;
    }

    /**
     * Start uploading a file to the server
     *
     * @param uploadFile the file to be uploaded
     * @param destDir    destination directory on the server
     *                   where the file is stored
     * @throws FTPException if client-server communication error occurred
     */
    public void uploadFile(File uploadFile, String destDir) throws FTPException {
        try {
            boolean success = ftpClient.changeWorkingDirectory(destDir);
            if (!success)
                makeDirectories(destDir);
            success = ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            if (!success) {
                throw new FTPException("Could not set binary file type.");
            }
            outputStream = ftpClient.storeFileStream(uploadFile.getName());
        } catch (IOException ex) {
            throw new FTPException("Error uploading file: " + ex.getMessage());
        }
    }

    /**
     * Write an array of bytes to the output stream.
     */
    public void writeFileBytes(byte[] bytes, int offset, int length)
            throws IOException {
        outputStream.write(bytes, offset, length);
    }

    /**
     * Complete the upload operation.
     */
    public void finish() throws IOException {
        outputStream.close();
        ftpClient.completePendingCommand();
    }

    /**
     * Log out and disconnect from the server
     */
    public void disconnect() throws FTPException {
        if (ftpClient.isConnected()) {
            try {
                if (!ftpClient.logout()) {
                    throw new FTPException("Could not log out from the server");
                }
                ftpClient.disconnect();
            } catch (IOException ex) {
                throw new FTPException("Error disconnect from the server: "
                        + ex.getMessage());
            }
        }
    }
}