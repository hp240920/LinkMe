package com.example.authotp;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
public class App extends Application {
    public static final String CHANNEL_ID = "exampleServiceChannel";
    public static SharedPreferences preferences;
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        preferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}