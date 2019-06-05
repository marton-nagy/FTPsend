package com.example.ftpsend;

import java.text.DateFormat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Adapter class for the ListView that displays the logs.
 */
public class LogAdapter extends BaseAdapter implements Serializable {
    ArrayList<String> logs;

    public LogAdapter() {
        logs = new ArrayList<String>();
    }

    @Override
    public int getCount() {
        return logs.size();
    }

    @Override
    public String getItem(int position) {
        return logs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public TextView getView(int position, View convertView, ViewGroup parent) {
        TextView r;
        if (convertView == null)
            r = new TextView(parent.getContext());
        else
            r = (TextView) convertView;
        r.setText(getItem(position));
        r.setTextColor(0xFFFFFFFF);
        return r;
    }

    /**
     * Adds a log message with a timestamp
     *
     * @param log the log message
     */
    public void log(String log) {
        String timestamp = DateFormat.getTimeInstance().format(new Date());
        logs.add(timestamp + " " + log);
        notifyDataSetChanged();
    }
}
