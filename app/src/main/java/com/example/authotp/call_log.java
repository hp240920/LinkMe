package com.example.authotp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class call_log extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log);
        setTitle("Call Log");
        displayRecentCalls();
    }

    private void displayRecentCalls() {

        //TableLayout tableLayout = findViewById(R.id.tableLayout);

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
                while (c.moveToNext() && counter < 15){

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
                    //String display = user.getCalllogUserString(user);
                    writeTextView(user);
                    counter++;

                }

            c.close();

            // sendMessage("123454","101"); "\n Name  " + name + "\n duration " + duration + "\n type " + type + " \n date "+ callDate +

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void writeTextView(User toDisplay){
        System.out.println(toDisplay);
        TextView textView = new TextView(this);
        textView.setText(toDisplay.getCalllogUserString(toDisplay));
        //textView.setOnClickListener(onClickListener);
        TableLayout ll = (TableLayout) findViewById(R.id.tableLayout_callLog);
        ll.setColumnStretchable(0, true);
        ll.setColumnStretchable(1, true);
       // ll.setColumnStretchable(2, true);
        TableRow row= new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        Button addBtn = new Button(this);
        addBtn.setText("SEND INFO");
        addBtn.setOnClickListener(onClickListener);
        addBtn.setTag(toDisplay.getPhonenumber());
       // Button downloadBtn = new Button(this);
        //downloadBtn.setText("Download File");
        row.addView(textView);
        row.addView(addBtn);
        //row.addView(downloadBtn);
        ll.addView(row);
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view instanceof Button){
                Button btn = (Button) view;
                String selectedUserNumber = btn.getTag().toString();
                Toast.makeText(getApplicationContext(), selectedUserNumber, Toast.LENGTH_SHORT).show();
                Intent selectFile = new Intent(call_log.this, com.example.authotp.selectFile.class);
                User.lastestNumber = selectedUserNumber;
                startActivity(selectFile);
            }
        }
    };
}
