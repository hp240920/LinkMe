package com.example.authotp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class call_log extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log);

        displayRecentCalls();
    }

    private void displayRecentCalls() {

        LinearLayout linearLayout = findViewById(R.id.linearLayout_callLog);

        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Cursor c = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");

            //c.moveToFirst();

            int counter=0;
                while (c.moveToNext() && counter<10){

                    int intNum = c.getColumnIndex(CallLog.Calls.NUMBER);
                    int intname = c.getColumnIndex(CallLog.Calls.CACHED_NAME);
                    int intduration = c.getColumnIndex(CallLog.Calls.DURATION);
                    int intdate = c.getColumnIndex(CallLog.Calls.DATE);


                    final String num = c.getString(intNum);// for  number
                    String name = c.getString(intname);// for name
                    String duration = c.getString(intduration);// for duration
                    String callDate = c.getString(intdate); // for date
                    int type = Integer.parseInt(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)));// for call

                    User user = new User(name,num);
                    String display = user.getCalllogUserString(user)+  "\n" + "******************************************************\n";
                    writeTextView(display,linearLayout);
                    counter++;

                }

            c.close();

            // sendMessage("123454","101"); "\n Name  " + name + "\n duration " + duration + "\n type " + type + " \n date "+ callDate +

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void writeTextView(String toDisplay, LinearLayout linearLayout){

        System.out.println(toDisplay);

        TextView textView = new TextView(this);
        textView.setText(toDisplay);
        textView.setOnClickListener(onClickListener);
        linearLayout.addView(textView);
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view instanceof TextView){
                TextView tv = (TextView)view;
                String selectedUserNumber = tv.getText().toString();
               // getFileFromNumber(selectedUserNumber);
                // Move to the select file which you want to send ....!! here
                Toast.makeText(getApplicationContext(), selectedUserNumber, Toast.LENGTH_SHORT).show();
            }
        }
    };
}