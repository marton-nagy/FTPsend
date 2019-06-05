package com.example.ftpsend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ftpsend.ftp.FtpBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;


/**
 * An Activity that allows the user to set up the parameters of the FTP connection.
 */
public class ConfigActivity extends AppCompatActivity {
    /**
     * Resolves the domain name of the server to an IP address.
     */
    static class ResolveAddress extends AsyncTask<String, Void, InetAddress> {
        @Override
        protected InetAddress doInBackground(String... strings) {
            try {
                return InetAddress.getByName(strings[0]);
            } catch (UnknownHostException e) {
                return null;
            }
        }
    }

    String address;
    int port;
    String user;
    String password;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        address = preferences.getString(address, "");
        port = preferences.getInt("port", 21);
        user = preferences.getString("username", "anonymous");
        password = preferences.getString("password", "");

        setContentView(R.layout.activity_config);

        ((EditText) findViewById(R.id.address)).setText(address);
        ((EditText) findViewById(R.id.port)).setText(Integer.toString(port));
        ((EditText) findViewById(R.id.user)).setText(user);
        ((EditText) findViewById(R.id.password)).setText(password);
    }

    /**
     * Reads in the values of the input fields.
     */
    private void readInput() {
        address = ((EditText) findViewById(R.id.address)).getText().toString();
        port = Integer.valueOf(((EditText) findViewById(R.id.port)).getText().toString());
        user = ((EditText) findViewById(R.id.user)).getText().toString();
        password = ((EditText) findViewById(R.id.password)).getText().toString();
    }

    @Override
    public void onPause() {
        super.onPause();

        readInput();

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("address", address);
        editor.putInt("port", port);
        editor.putString("username", user);
        editor.putString("password", password);
        editor.apply();
    }

    /**
     * Resolves the server address, packages the data into an FTPBuilder and sends it to the main Activity
     */
    public void onConnect(View button) {
        readInput();

        InetAddress ip = null;
        try {
            ip = new ResolveAddress().execute(address).get();
        } catch (InterruptedException | ExecutionException e) {
            Toast.makeText(this, "Failed to resolve address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ip == null) {
            Toast.makeText(this, "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FtpBuilder builder = new FtpBuilder();
        builder.address = ip;
        builder.port = port;
        builder.user = user;
        builder.password = password;

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ftp", builder);
        startActivity(intent);
    }
}
