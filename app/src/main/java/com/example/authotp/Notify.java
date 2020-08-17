package com.example.authotp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.authotp.MainActivity;
import com.example.authotp.R;

import static com.example.authotp.App.CHANNEL_ID;


public class Notify extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        Intent notificationIntent = new Intent(this, Dashboard.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(input)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2, notification);
        //do heavy work on a background thread
        //stopSelf();
        return START_REDELIVER_INTENT;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}