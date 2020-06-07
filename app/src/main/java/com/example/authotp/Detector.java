package com.example.authotp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

public class Detector extends BroadcastReceiver{

    public void onReceive(final Context context, Intent intent) {

        try {
            System.out.println("Receiver start");
            Toast.makeText(context, " Receiver start ", Toast.LENGTH_SHORT).show();


            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {

                Toast.makeText(context, "Ringing State Number is -" + incomingNumber, Toast.LENGTH_SHORT).show();
            }
            if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))) {
                Toast.makeText(context, "Received State", Toast.LENGTH_SHORT).show();
            }
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Toast.makeText(context, "Idle State", Toast.LENGTH_SHORT).show();

                notification2(context,incomingNumber);

            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void notification2(Context context,String incomingNumber) {
        // Builds your notification

        Intent notificationIntent = new Intent(context, Conformation.class);
        notificationIntent.putExtra("phoneNum", incomingNumber);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Intent broadcastIntent = new Intent(context, NotificationAction.class);
        broadcastIntent.putExtra("phoneNum",incomingNumber);
        PendingIntent actionIntent = PendingIntent.getBroadcast(context,
                0, broadcastIntent, 0);

        Intent intent = new Intent(context, CancelNotification.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("notification_id", 0);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Click here to confirm and send!")
                .setContentText("Do you want to share your details with " + incomingNumber)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher, "Send", actionIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Dismiss", dismissIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        manager.notify(0, builder.build());
    }

}