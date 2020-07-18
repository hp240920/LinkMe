package com.example.authotp;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

public class Detector extends BroadcastReceiver{

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(final Context context, Intent intent) {

        try {
            System.out.println("Receiver start");

            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            SharedPreferences sharedPreferences = context.getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
            String myNumber  = sharedPreferences.getString("phone","");

            User.lastestNumber = incomingNumber;

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
                // incomingNumber = getlastCall(context);
                String incomingNumber1 = User.lastestNumber;
                notificationOut(context,incomingNumber1,myNumber);

                if(SharePreHelper.getName().equals("true")){
                    notificationIn(context, incomingNumber1, myNumber);
                    SharePreHelper.setName("");
                }else{
                    notificationOut(context, incomingNumber1, myNumber);
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }



private String getlastCall(Context context){
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return null;
    }
    Cursor c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);

    c.moveToLast();

    int intNum = c.getColumnIndex(CallLog.Calls.NUMBER);
    int intname = c.getColumnIndex(CallLog.Calls.CACHED_NAME);
    int intduration = c.getColumnIndex(CallLog.Calls.DURATION);
    int intdate = c.getColumnIndex(CallLog.Calls.DATE);


    final String num = c.getString(intNum);// for  number
    String name = c.getString(intname);// for name
    String duration = c.getString(intduration);// for duration
    String callDate = c.getString(intdate); // for date
    int type = Integer.parseInt(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)));// for call

    //display.setText("Num " + num + "\n Name  " + name + "\n duration " + duration + "\n type " + type + " \n date "+ callDate);
    c.close();

    return num;
}

    public void notificationOut(Context context, String incomingNumber, String my_phone) {
        // Builds your notification

        Intent notificationIntent = new Intent(context, selectFile.class);
        notificationIntent.putExtra("phoneNum", incomingNumber);
        notificationIntent.putExtra("myPhone", my_phone);
        notificationIntent.putExtra("notification_id", 0);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, FLAG_CANCEL_CURRENT);

        /*
        Intent broadcastIntent = new Intent(context, NotificationAction.class);
        broadcastIntent.putExtra("phoneNum",incomingNumber);
        broadcastIntent.putExtra("myPhone", my_phone);
        broadcastIntent.putExtra("notification_id", 0);
        PendingIntent actionIntent = PendingIntent.getBroadcast(context,
                0, broadcastIntent, 0);
         */


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
                .addAction(R.mipmap.ic_launcher, "Send", contentIntent)
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

    public void notificationIn(Context context, String incomingNumber, String my_phone) {
        // Builds your notification

        Intent notificationIntent = new Intent(context, selectFile.class);
        notificationIntent.putExtra("phoneNum", incomingNumber);
        notificationIntent.putExtra("myPhone", my_phone);
        notificationIntent.putExtra("notification_id", 0);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, FLAG_CANCEL_CURRENT);

        /*
           Intent broadcastIntent = new Intent(context, NotificationAction.class);
        broadcastIntent.putExtra("phoneNum",incomingNumber);
        broadcastIntent.putExtra("myPhone", my_phone);
       // broadcastIntent.putExtra("notification_id", 0);
        PendingIntent actionIntent = PendingIntent.getBroadcast(context,
                0, broadcastIntent, FLAG_CANCEL_CURRENT);
         */


        Intent intent = new Intent(context, CancelNotification.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("notification_id", 0);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(context, 0, intent, FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Incoming Notification AuthOTP")
                .setContentText("Do you want to share your details with " + incomingNumber)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher, "Send", contentIntent)
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

}