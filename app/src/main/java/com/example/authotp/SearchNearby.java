package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SearchNearby extends AppCompatActivity {


    ConnectionsClient connectionsClient;
    Button connection;
    Button findConnection;
    Button sendInfo;
    EditText etName;
    Boolean isAdvertising;
    LinearLayout linearLayout;
    private static int ON_REQUEST_CONTACT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_nearby);
        setTitle("Search Nearby");
        connection = findViewById(R.id.btnHost);
        findConnection = findViewById(R.id.btnFind);
        etName = findViewById(R.id.etName);
        sendInfo= findViewById(R.id.btnSendInfo);
        connectionsClient = Nearby.getConnectionsClient(this);
        linearLayout = findViewById(R.id.linearLayout_searchNearby);
        sendInfo.setEnabled(false);

        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("name","");
        etName.setText(userName);
    }


    public void makeConnection(View v) {
        startAdvertising();
        Toast.makeText(this, "Starting Advertising", Toast.LENGTH_LONG).show();
        Log.i("Stutus :", "Starting Advertising and Discovery");
    }


    public void startSearch(View v){

        isAdvertising = false;
        startDiscovery();
        Toast.makeText(this, "Starting Discovery", Toast.LENGTH_LONG).show();
    }

    private void startAdvertising() {

        isAdvertising = true;
        disableDiscovery();
        sendInfo.setEnabled(false);
        connectionsClient.startAdvertising(
                etName.getText().toString(), getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build());

    }

    private void disableDiscovery() {

        findConnection.setEnabled(false);
        etName.setEnabled(false);
    }

    private void disableAdvertisement() {
        connection.setEnabled(false);
        etName.setEnabled(false);
    }

    private void startDiscovery() {

        disableAdvertisement();
        sendInfo.setEnabled(false);
        connectionsClient.startDiscovery(
                getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build());

    }


    // format : name, phoneNumber, email, website FOR CONTACT

    public void onSendInfo(View v){
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        User currentUser = getSharedPref(sharedPreferences);

        String details = currentUser.getName() + ", "+ currentUser.getPhonenumber() + ", "+ currentUser.getEmail() +", "+ currentUser.getWebsite() + ", "
                + currentUser.getInstagram() + ", "+ currentUser.getSnapchat() + ", "+ currentUser.getGitHub() + ", "+ currentUser.getLinkedIn();
        connectionsClient.sendPayload(
                opponentEndpointId, Payload.fromBytes(details.getBytes(UTF_8)));
        Toast.makeText(this,"sending Information",Toast.LENGTH_LONG).show();
    }

    private User getSharedPref(SharedPreferences sharedPreferences){
        User currentUser = new User();
        currentUser.setName(sharedPreferences.getString("name",""));
        currentUser.setPhonenumber(sharedPreferences.getString("phone",""));
        currentUser.setEmail(sharedPreferences.getString("email", ""));
        currentUser.setWebsite(sharedPreferences.getString("website", ""));
        currentUser.setInstagram(sharedPreferences.getString("insta",""));
        currentUser.setSnapchat(sharedPreferences.getString("snap",""));
        currentUser.setGitHub(sharedPreferences.getString("github",""));
        currentUser.setLinkedIn(sharedPreferences.getString("linkedIn",""));
        //currentUser.setFiles1(sharedPreferences.getString("file1",""));
        //currentUser.setFiles2(sharedPreferences.getString("file2",""));
        return currentUser;
    }


    @Override
    public void onBackPressed() {
        connectionsClient.stopAllEndpoints();
        finish();

    }

    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER; // need to check if this the best choice
    private String opponentEndpointId;

    /// *********************  The call back for Goggle Nearby ************
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    Toast.makeText(getApplicationContext(),"endpoint found, connecting",Toast.LENGTH_LONG).show();
                    Log.i("TAG", "onEndpointFound: endpoint found, connecting");

                    //viewConnections(endpointId,info);
                    connectionsClient.requestConnection(etName.getText().toString(), endpointId, connectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    System.out.println("We lost it ..!!");
                    Toast.makeText(getApplicationContext(),"Lost Connection",Toast.LENGTH_LONG).show();
                }
            };


    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i("TAG", "onConnectionInitiated: accepting connection");
                    Toast.makeText(getApplicationContext(),"accepting connection",Toast.LENGTH_LONG).show();
                    viewConnections(endpointId,connectionInfo);

                    /// if someone is advertising then it is not necessary for hi to select the name in linear layout
                    if(isAdvertising){
                        connectionsClient.acceptConnection(endpointId, payloadCallback);
                    }
                    //opponentName = connectionInfo.getEndpointName();
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {

                        sendInfo.setEnabled(true);
                        Log.i("TAG", "onConnectionResult: connection successful");

                        connectionsClient.stopDiscovery();
                        connectionsClient.stopAdvertising();

                        opponentEndpointId = endpointId;
                       // System.out.println(" opponent Id ------>>>"+opponentEndpointId);
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                        //setStatusText(getString(R.string.status_connected));
                        // setButtonState(true);
                    } else {
                        Log.i("TAG", "onConnectionResult: connection failed");
                        Toast.makeText(getApplicationContext(),"Failed to connect",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onDisconnected(@NonNull String s) {
                    Log.i("TAG", "onDisconnected: disconnected from the opponent");
                    Toast.makeText(getApplicationContext(),"Disconnected",Toast.LENGTH_LONG).show();

                }

            };


    private void viewConnections(String endpointId, ConnectionInfo info){
        //LinearLayout linearLayout = findViewById(R.id.lin);
        TextView textView = new TextView(this);
        // textView.setHeight(15);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(5,20,5,0);
        textView.setText("--------->>>  Endpoint Id :"+endpointId + " Name : "+ info.getEndpointName() +  "\n");
        textView.setTag(endpointId);
        textView.setLayoutParams(params);
        linearLayout.addView(textView);
        textView.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TextView textView = (TextView) view;
            Toast.makeText(getApplicationContext(),textView.getText().toString(),Toast.LENGTH_LONG).show();
            String endPoint = (String)textView.getTag();
            Toast.makeText(getApplicationContext(),endPoint,Toast.LENGTH_LONG).show();
            connectionsClient.acceptConnection(endPoint, payloadCallback);
            // opponentName = textView.getText().toString();
        }
    };

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, final Payload payload) {
                    Log.i("TAG", "On payload Received");

                    String name = new String(payload.asBytes(), UTF_8).split(", ")[0];
                    AlertDialog.Builder builder = new AlertDialog.Builder(SearchNearby.this);
                    builder.setMessage("Do you want to save the contact of "+ name)
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getInfo(payload);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.setTitle("Incoming Message ");
                    alert.show();

                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        Log.i("TAG", "On payload Transfer Update");
                    }
                }
            };

    private void getInfo(Payload payload){

        String received = new String(payload.asBytes(), UTF_8);
        String[] inforamtion = received.split(", ");
        String name = inforamtion[0];
        String phoneNumber = inforamtion[1];
        String email = inforamtion[2];
        String website = inforamtion[3];

        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL,email);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE,phoneNumber);
        intent.putExtra(ContactsContract.Intents.Insert.NAME,name);
        intent.putExtra(ContactsContract.Intents.Insert.NOTES,website);
        startActivity(intent);

        Toast.makeText(getApplicationContext()," Recieved Text is "+received,Toast.LENGTH_LONG).show();
    }


    private void saveToContact(String file1) {
        System.out.println(file1);
        String[] information = file1.split(", ");
        String name = information[0];
        String phoneNumber = information[1];
        String email = information[2];
        String website = information[3];
        String insta = information[4];
        String snap = information[5];
        String gitHub = information[6];
        String linkedin = information[7];
        String notes = "Website: " + website + "\nInstagram: "
                + insta + "\nSnapChat: " + snap + "\nGithub: " + gitHub + "\nLinkedIn: " + linkedin;




        boolean isExisting = false;

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if(cursor.moveToFirst()) {
            isExisting = true;
            long idContact = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, idContact);
            Intent i = new Intent(Intent.ACTION_EDIT,contactUri);

            ArrayList<ContentValues> data = setLabels(insta,snap,gitHub,linkedin,website);

            i.setData(contactUri);
            i.putExtra("finishActivityOnSaveCompleted", true);

            i.putExtra(ContactsContract.Intents.Insert.PHONE_ISPRIMARY,phoneNumber);
            i.putExtra(ContactsContract.Intents.Insert.EMAIL_ISPRIMARY,email);
            //i.putExtra(ContactsContract.Intents.Insert.NOTES, notes);

            i.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);

            startActivityForResult(i,ON_REQUEST_CONTACT);
        }



        if(isExisting == false){
             /*
               Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL,email);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE,phoneNumber);
            intent.putExtra(ContactsContract.Intents.Insert.NAME,name);
            intent.putExtra(ContactsContract.Intents.Insert.NOTES, notes);
             //intent.putExtra(ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM, notes);
             //intent.putExtra(ContactsContract.Intents.Insert)
              */


            ArrayList<ContentValues> data = setLabels(insta,snap,gitHub,linkedin,website);

            Intent intent_demo = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
            intent_demo.putExtra(ContactsContract.Intents.Insert.NAME, name);
            intent_demo.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
            intent_demo.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
            intent_demo.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
            startActivityForResult(intent_demo,ON_REQUEST_CONTACT);

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ON_REQUEST_CONTACT){
            System.out.println("Contact Saved ");
        }
    }

    private ArrayList<ContentValues> setLabels(String insta, String snap, String gitHub, String linkedin, String website){
        ArrayList<ContentValues> data = new ArrayList<ContentValues>();

        ContentValues row2 = new ContentValues();
        row2.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
        row2.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
        row2.put(ContactsContract.CommonDataKinds.Website.LABEL, "Instagram");
        row2.put(ContactsContract.CommonDataKinds.Website.URL, insta);
        data.add(row2);

        ContentValues row3 = new ContentValues();
        row3.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
        row3.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
        row3.put(ContactsContract.CommonDataKinds.Website.LABEL, "Snapchat");
        row3.put(ContactsContract.CommonDataKinds.Website.URL, snap);
        data.add(row3);

        ContentValues row4 = new ContentValues();
        row4.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
        row4.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
        row4.put(ContactsContract.CommonDataKinds.Website.LABEL, "GitHub");
        row4.put(ContactsContract.CommonDataKinds.Website.URL, gitHub);
        data.add(row4);

        ContentValues row5 = new ContentValues();
        row5.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
        row5.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
        row5.put(ContactsContract.CommonDataKinds.Website.LABEL, "LinkedIn");
        row5.put(ContactsContract.CommonDataKinds.Website.URL, linkedin);
        data.add(row5);

        ContentValues row6 = new ContentValues();
        row6.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
        row6.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
        row6.put(ContactsContract.CommonDataKinds.Website.LABEL, "Website");
        row6.put(ContactsContract.CommonDataKinds.Website.URL, website);
        data.add(row6);

        return data;
    }


}
