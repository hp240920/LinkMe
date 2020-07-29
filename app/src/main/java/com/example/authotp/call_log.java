package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class call_log extends AppCompatActivity {

    private static final int REQUEST_CODE = 3055;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log);
        setTitle("Call Log");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Permissions.check_phone_permission(call_log.this)){
                requestPermissions(new String[] {
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG,
                }, REQUEST_CODE);
            }
        }
        displayRecentCalls();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), call_log.class);
                startActivity(intent);
                finish();
            }else{
                finish();
            }
        }
    }

    private void displayRecentCalls() {
        ArrayList<Call_Row> call_rows = new ArrayList<>();
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
                //writeTextView(user);
                Call_Row call_row = new Call_Row(name, num, R.drawable.ic_baseline_send_24);
                call_rows.add(call_row);
                counter++;
            }
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        ListView lv = findViewById(R.id.list_call_log);
        RecentCall_ListAdapter recentCall_listAdapter = new RecentCall_ListAdapter(this, call_rows);
        lv.setAdapter(recentCall_listAdapter);

    }
    private void writeTextView(User toDisplay){
        System.out.println(toDisplay);
        TextView textView = new TextView(this);
        textView.setText(toDisplay.getCalllogUserString(toDisplay));
        //textView.setOnClickListener(onClickListener);
        TableLayout ll = null;
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

class RecentCall_ListAdapter extends BaseAdapter{

    Activity context;
    ArrayList<Call_Row> rows;
    private static LayoutInflater inflater = null;

    public RecentCall_ListAdapter(Activity context, ArrayList<Call_Row> rows){
        this.context = context;
        this.rows = rows;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View itemView = view;
        if(itemView == null){
            itemView = inflater.inflate(R.layout.call_log_xml, null);
        }
        TextView textView = itemView.findViewById(R.id.tv_recent_call);
        String name = rows.get(i).getName();
        if(name == null || name.isEmpty()){
            textView.setText(rows.get(i).getPhone());
        }else{
            textView.setText(name);
        }
        ImageButton btn = itemView.findViewById(R.id.btn_recent_send);
        btn.setImageResource(rows.get(i).getSend_img());
        btn.setTag(rows.get(i).getPhone());
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view instanceof ImageButton){
                    ImageButton btn = (ImageButton) view;
                    String selectedUserNumber = btn.getTag().toString();
                    Toast.makeText(context, selectedUserNumber, Toast.LENGTH_SHORT).show();
                    Intent selectFile = new Intent(context, com.example.authotp.selectFile.class);
                    User.lastestNumber = selectedUserNumber;
                    context.startActivity(selectFile);
                }
            }
        });
        return itemView;
    }
}
