package com.example.ftpsend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.example.ftpsend.ftp.FtpBuilder;


/**
 * The main Activity. Displays the message log and allows the user to upload a file or disconnect (going back to the config Activity).
 * Sends commands to the FTP Service through Intents and receives responses through Messages. The responses are added to the log.
 */
public class MainActivity extends AppCompatActivity {
    public class LogHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            Bundle bundle = message.getData();
            logger.log(bundle.getString("response"));
            listView.smoothScrollToPosition(logger.getCount() - 1);
        }
    }

    FtpBuilder builder;
    LogAdapter logger;
    ListView listView;
    LogHandler handler;
    Messenger messenger;

    /**
     * Sends an Intent describing an action to the Service. Populates the Intent with the necessary parameters.
     *
     * @param action the code of the action to take
     * @param file   the file to send if the action is a file send action
     */
    private void serviceAction(int action, Uri file) {
        Intent intent = new Intent(this, FtpService.class);
        intent.putExtra("messenger", messenger);
        intent.putExtra("ftp", builder);
        intent.putExtra("action", action);
        if (file != null) intent.putExtra("file", file);
        startService(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            logger = (LogAdapter) savedInstanceState.getSerializable("logger");
        else
            logger = new LogAdapter();
        handler = new LogHandler();
        messenger = new Messenger(handler);

        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.log);
        listView.setAdapter(logger);
    }

    /**
     * Starts the Activity. If started by the config Activity, initializes the Service, connects and logs into the FTP server.
     */
    @Override
    public void onStart() {
        super.onStart();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ftp")) {
            builder = (FtpBuilder) intent.getSerializableExtra("ftp");
            serviceAction(FtpService.SET_FTP_ACTION, null);
            serviceAction(FtpService.CONNECT_ACTION, null);
            serviceAction(FtpService.LOGIN_ACTION, null);
            setIntent(null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putSerializable("logger", logger);
    }

    /**
     * Disconnects from the FTP server and returns to the config Activity.
     */
    public void disconnect(View button) {
        logger.log("Disconnecting...");
        serviceAction(FtpService.DISCONNECT_ACTION, null);
        finish();
    }

    /**
     * Prompts the user to choose a file to upload. The chosen file will be delivered by an Intent to onActivityResult().
     */
    public void upload(View button) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/x-bittorrent");
        startActivityForResult(intent, 1);
    }

    /**
     * Receives the file chosen by the user and sends it to the FTP Service with an upload action.
     *
     * @param data the Intent containing the file selected
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return; //user did not select a file
        Uri path = data.getData();
        logger.log("Sending file...");
        serviceAction(FtpService.SEND_ACTION, path);
    }
}
