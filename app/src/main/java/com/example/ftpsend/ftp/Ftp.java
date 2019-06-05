package com.example.ftpsend.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;


/**
 * Class that encapsulates an FTP client and the data necessary to interact with the FTP server.
 * The methods implement FTP operations, and return values that can be used to determine whether the operation was successful.
 */
public class Ftp {
    public static int IOEXCEPTION_CODE = -1;
    public static int SOCKETEXCEPTION_CODE = -2;
    public static int FTPCONNECTIONCLOSEDEXCEPTION_CODE = -3;

    FTPClient client;
    InetAddress address;
    int port;
    String user;
    String password;
    int disconnectRetries;
    int logoutRetries;


    Ftp(FTPClient client, InetAddress address, int port, String user, String password, int disconnectRetries, int logoutRetries) {
        this.client = client;
        this.address = address;
        this.port = port;
        this.user = user;
        this.password = password;
        this.disconnectRetries = disconnectRetries;
        this.logoutRetries = logoutRetries;
    }

    /**
     * Checks whether the last FTP reply code is a positive completion (2xx)
     *
     * @return true if the last FTP reply code is a positive completion
     */
    public boolean goodReply() {
        return FTPReply.isPositiveCompletion(client.getReplyCode());
    }

    /**
     * Connects to the FTP server and enters passive mode.
     *
     * @return the FTP reply code or a negative number if an exception occurred (FTP reply codes are positive)
     */
    public int connect() {
        try {
            client.connect(address, port);
        } catch (SocketException e) {
            return SOCKETEXCEPTION_CODE;
        } catch (IOException e) {
            return IOEXCEPTION_CODE;
        }
        int reply = client.getReplyCode();
        client.enterLocalPassiveMode();
        return reply;
    }

    /**
     * Logs into the server.
     *
     * @return true on success, false on failure
     */
    public boolean login() {
        try {
            boolean login = client.login(user, password);
            boolean fileType = client.setFileType(FTP.BINARY_FILE_TYPE);
            return login & fileType;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Sends a file to the server.
     *
     * @param file an InputStream on the file
     * @param path the path and filename on the server we want to send the file to
     * @return 0 on success, 1 on failure, negative on exception
     */
    public int sendFile(InputStream file, String path) {
        try {
            if (client.storeFile(path, file)) return 0;
            else return 1;
        } catch (FTPConnectionClosedException e) {
            return FTPCONNECTIONCLOSEDEXCEPTION_CODE;
        } catch (IOException e2) {
            return IOEXCEPTION_CODE;
        }
    }

    /**
     * Logs out of the server. Retries a few times on failure.
     *
     * @return true on success, false on failure after a number of retries
     */
    public boolean logout() {
        for (int i = 0; i < logoutRetries; i++) {
            try {
                if (!client.logout()) continue;
                return true;
            } catch (IOException e) {
            }
        }
        return false;
    }

    /**
     * Disconnects from the server. Logs out before disconnecting. Retries a few times on failure.
     *
     * @return true on success, false on failure after a number of retries
     */
    public boolean disconnect() {
        logout();
        for (int i = 0; i < disconnectRetries; i++) {
            try {
                client.disconnect();
                if (goodReply()) return true;
            } catch (IOException e) {
            }
        }
        return false;
    }
}
