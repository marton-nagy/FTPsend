package com.example.ftpsend;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.example.ftpsend.ftp.Ftp;
import com.example.ftpsend.ftp.FtpBuilder;

import java.io.FileNotFoundException;
import java.io.InputStream;


/**
 * The service that handles network and disk operations. Receives commands from the Activity using Intents. Each Intent has an extra field called 'action' which selects
 * which action the Service takes. They also have a Messenger that allows the Service to reply. Some actions require extra parameters which are also included in the Intent.
 */
public class FtpService extends IntentService {
    public static final int SET_FTP_ACTION = 0;
    public static final int CONNECT_ACTION = 1;
    public static final int LOGIN_ACTION = 2;
    public static final int SEND_ACTION = 3;
    public static final int LOGOUT_ACTION = 4;
    public static final int DISCONNECT_ACTION = 5;

    private static Ftp ftp;
    private Messenger messenger;

    public FtpService() {
        super("ftp");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Sends a response back to the Activity, to be added to the log.
     *
     * @param response a string response
     */
    private void sendResponse(String response) {
        Bundle bundle = new Bundle();
        bundle.putString("response", response);
        Message message = Message.obtain();
        message.setData(bundle);
        try {
            messenger.send(message);
        } catch (RemoteException e) {
        }
    }

    /**
     * Connects to the server, and sends back appropriate responses on success and failure.
     */
    private void connect() {
        ftp.connect();
        if (ftp.goodReply()) sendResponse("Connected");
        else sendResponse("Could not connect to server");
    }

    /**
     * Gets the Messenger from the Intent, which will be used to send responses. Gets the action code from the Intent, performs the appropriate action and reports back
     * responses to the Activity.
     *
     * @param intent the incoming Intent. Must contain an int extra field called 'action' and a Messenger extra field called 'messenger'. Some actions may require further data.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        messenger = intent.getParcelableExtra("messenger");
        int action = intent.getIntExtra("action", 0);
        switch (action) {
            //Initializes the FTP client. Must be called before other actions.
            case SET_FTP_ACTION:
                FtpBuilder builder = (FtpBuilder) intent.getSerializableExtra("ftp");
                ftp = builder.build();
                break;
            //Connects to the FTP server.
            case CONNECT_ACTION:
                connect();
                break;
            //Logs into the FTP server.
            case LOGIN_ACTION:
                if (ftp.login()) sendResponse("Logged in");
                else sendResponse("Failed to log in, error: ");
                break;
            //Uploads a file to the FTP server. Will try to reconnect if the connection has been closed.
            case SEND_ACTION:
                Uri path = intent.getParcelableExtra("file");
                InputStream stream;
                try {
                    stream = getContentResolver().openInputStream(path);
                } catch (FileNotFoundException e) {
                    sendResponse("Could not open file " + path + ", " + e.toString());
                    break;
                }
                String[] strings = path.getPath().split("/");
                String fileName = strings[strings.length - 1];
                int reply = ftp.sendFile(stream, fileName);
                if (reply == 0) {
                    sendResponse("File uploaded");
                    break;
                }
                if (reply == Ftp.FTPCONNECTIONCLOSEDEXCEPTION_CODE) { //try to reconnect if connection has been closed
                    sendResponse("Connection closed, reconnecting...");
                    connect();
                    reply = ftp.sendFile(stream, fileName);
                    if (reply == 0) sendResponse("File uploaded");
                    else sendResponse("Failed to send file");
                    break;
                } else sendResponse("Failed to send file");
                break;
            //Logs out of the FTP server.
            case LOGOUT_ACTION:
                if (ftp.logout()) sendResponse("Logged out");
                else sendResponse("Failed to log out");
                break;
            //Disconnects from the FTP server.
            case DISCONNECT_ACTION:
                if (ftp.disconnect()) sendResponse("Disconnected");
                else sendResponse("Error while trying to disconnect");
                break;
        }
    }
}
