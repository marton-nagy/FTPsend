package com.example.ftpsend.ftp;

import org.apache.commons.net.ftp.FTPClient;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Builder for the Ftp class.
 */
public class FtpBuilder implements Serializable {
    /**
     * Address of the FTP server
     */
    public InetAddress address;

    /**
     * Command port
     */
    public int port;

    /**
     * Username
     */
    public String user;

    /**
     * Password
     */
    public String password;

    /**
     * Number of times a failed logout will be retried
     */
    public int logoutRetries;

    /**
     * Number of times a failed disconnect will be retried
     */
    public int disconnectRetries;

    public FtpBuilder() {
        port = 21;
        user = "anonymous";
        password = "";
        logoutRetries = 5;
        disconnectRetries = 5;
    }

    public FtpBuilder(FtpBuilder o) {
        address = o.address;
        port = o.port;
        user = o.user;
        password = o.password;
        logoutRetries = o.logoutRetries;
        disconnectRetries = o.disconnectRetries;
    }

    public FtpBuilder(InetAddress address, int port, boolean ftps, boolean explicit, String user, String password, int logoutRetries, int disconnectRetries) {
        this.address = address;
        this.port = port;
        this.user = user;
        this.password = password;
        this.logoutRetries = logoutRetries;
        this.disconnectRetries = disconnectRetries;
    }

    public Ftp build() {
        FTPClient client = new FTPClient();
        return new Ftp(client, address, port, user, password, logoutRetries, disconnectRetries);
    }

}
