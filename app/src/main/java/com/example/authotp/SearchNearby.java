package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import static java.nio.charset.StandardCharsets.UTF_8;

public class SearchNearby extends AppCompatActivity {


    ConnectionsClient connectionsClient;
    Button connection;
    Button findConnection;
    Button sendInfo;
    EditText etName;
    Boolean isAdvertising;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_nearby);

        connection = findViewById(R.id.btnHost);
        findConnection = findViewById(R.id.btnFind);
        etName = findViewById(R.id.etName);
        sendInfo= findViewById(R.id.btnSendInfo);
        connectionsClient = Nearby.getConnectionsClient(this);
        linearLayout = findViewById(R.id.linearLayout_searchNearby);
        sendInfo.setEnabled(false);

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

        String details = currentUser.getName() + ", "+ currentUser.getPhonenumber() + ", "+ currentUser.getEmail() +", "+ currentUser.getWebsite();
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
        String recieved = new String(payload.asBytes(), UTF_8);
        String[] inforamtion = recieved.split(", ");
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

        Toast.makeText(getApplicationContext()," Recieved Text is "+recieved,Toast.LENGTH_LONG).show();
    }

}
