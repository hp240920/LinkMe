package com.example.authotp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import static androidx.core.app.ActivityCompat.*;

public class Permissions {

    private static final int REQUEST_CODE = 3055;

    protected static boolean check_contacts_permission(Context context){
        if( ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) +
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    protected static void get_contacts_permission(Context context){
        requestPermissions((Activity)context, new String[] {
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                }, REQUEST_CODE);
    }

    protected static boolean check_storage_permission(Context context){
        if( ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    protected static void get_storage_permission(Context context){
        requestPermissions((Activity)context, new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }


    protected static boolean check_phone_permission(Context context){
        if( ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) +
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    protected static void get_phone_permission(Context context){
        requestPermissions((Activity)context, new String[] {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
               }, REQUEST_CODE);
    }

    protected static boolean check_location_permission(Context context){
        if( ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) +
                ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE)+
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) +
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) +
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    protected static void get_location_permission(Context context){
        requestPermissions((Activity)context, new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                }, REQUEST_CODE);
    }





}
