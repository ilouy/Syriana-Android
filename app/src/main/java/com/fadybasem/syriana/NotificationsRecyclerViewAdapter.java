package com.fadybasem.syriana;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationsRecyclerViewAdapter extends RecyclerView.Adapter<NotificationsRecyclerViewAdapter.ViewHolder> {
    ArrayList<HashMap<String, String>> notifications;

    public NotificationsRecyclerViewAdapter(@NonNull ArrayList<HashMap<String, String>> notifications) {
        this.notifications = notifications;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView bodyTextView = holder.bodyTextView;
        TextView timeTextView = holder.timeTextView;

        bodyTextView.setText(notifications.get(notifications.size()-1-position).get("body"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        try {
            Date date = formatter.parse(notifications.get(notifications.size()-1-position).get("datetime"));
            Instant localTime = date.toInstant().plus(2, ChronoUnit.HOURS);

            if (localTime.isAfter(Instant.now().minusSeconds(60))) {
                timeTextView.setText("Just Now");
            } else if (localTime.isAfter(Instant.now().minus(1, ChronoUnit.DAYS))) {
                timeTextView.setText(ChronoUnit.HOURS.between(localTime, Instant.now()) + " hours ago");
            } else {
                DateTimeFormatter _formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .withLocale(Locale.ENGLISH)
                        .withZone(ZoneId.of("GMT"));

                timeTextView.setText(_formatter.format(localTime));
            }
        } catch (Exception e) {
            e.printStackTrace();

            timeTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        TextView bodyTextView;
        TextView timeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            bodyTextView = itemView.findViewById(R.id.notificationBody);
            timeTextView = itemView.findViewById(R.id.notificationTime);
        }
    }
}
