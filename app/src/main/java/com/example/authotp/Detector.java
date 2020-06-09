package com.example.authotp;

import android.annotation.SuppressLint;
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

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

public class Detector extends BroadcastReceiver{

    boolean in_out = false;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(final Context context, Intent intent) {

        try {
            System.out.println("Receiver start");
            Toast.makeText(context, " Receiver start ", Toast.LENGTH_SHORT).show();
            //boolean outgoing = false;
            //boolean end = false;
            //boolean check4 = false;
            SharedPreferences sharedPreferences = context.getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
            String my_phone = sharedPreferences.getString("phone","");

            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            assert state != null;
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                //in_out = true;
                SharePreHelper.setName("true");
                Toast.makeText(context, "Ringing State Number is -" + incomingNumber, Toast.LENGTH_SHORT).show();
            }
            if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))) {
                Toast.makeText(context, "Received State", Toast.LENGTH_SHORT).show();
            }
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                //end = true;
                Toast.makeText(context, "Idle State", Toast.LENGTH_SHORT).show();
                if(SharePreHelper.getName().equals("true")){
                    notificationIn(context, incomingNumber, my_phone);
                    SharePreHelper.setName("");
                }else{
                    notificationOut(context, incomingNumber, my_phone);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notificationIn(Context context, String incomingNumber, String my_phone) {
        // Builds your notification

        Intent notificationIntent = new Intent(context, Conformation.class);
        notificationIntent.putExtra("phoneNum", incomingNumber);
        notificationIntent.putExtra("myPhone", my_phone);
        notificationIntent.putExtra("notification_id", 0);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, FLAG_CANCEL_CURRENT);

        Intent broadcastIntent = new Intent(context, NotificationAction.class);
        broadcastIntent.putExtra("phoneNum",incomingNumber);
        broadcastIntent.putExtra("myPhone", my_phone);
        broadcastIntent.putExtra("notification_id", FLAG_CANCEL_CURRENT);
        PendingIntent actionIntent = PendingIntent.getBroadcast(context,
                0, broadcastIntent, 0);

        Intent intent = new Intent(context, CancelNotification.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("notification_id", 0);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Incoming Notification AuthOTP")
                .setContentText("Do you want to share your details with " + incomingNumber)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher, "Send", actionIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Dismiss", dismissIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "IncomingCalls";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notify for Incoming Calls",
                    NotificationManager.IMPORTANCE_HIGH);
            assert manager != null;
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        assert manager != null;
        manager.notify(0, builder.build());
    }

    public void notificationOut(Context context, String incomingNumber, String my_phone) {
        // Builds your notification

        Intent notificationIntent = new Intent(context, Conformation.class);
        notificationIntent.putExtra("phoneNum", incomingNumber);
        notificationIntent.putExtra("myPhone", my_phone);
        notificationIntent.putExtra("notification_id", 0);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, FLAG_CANCEL_CURRENT);

        Intent broadcastIntent = new Intent(context, NotificationAction.class);
        broadcastIntent.putExtra("phoneNum",incomingNumber);
        broadcastIntent.putExtra("myPhone", my_phone);
        broadcastIntent.putExtra("notification_id", FLAG_CANCEL_CURRENT);
        PendingIntent actionIntent = PendingIntent.getBroadcast(context,
                0, broadcastIntent, 0);

        Intent intent = new Intent(context, CancelNotification.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("notification_id", 0);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Outgoing Notification AuthOTP")
                .setContentText("Do you want to share your details with " + incomingNumber)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher, "Send", actionIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Dismiss", dismissIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "OutgoingCalls";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notify for Outgoing Calls",
                    NotificationManager.IMPORTANCE_HIGH);
            assert manager != null;
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        assert manager != null;
        manager.notify(0, builder.build());
    }

}