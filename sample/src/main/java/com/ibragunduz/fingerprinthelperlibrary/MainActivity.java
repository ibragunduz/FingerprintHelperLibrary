package com.ibragunduz.fingerprinthelperlibrary;

import android.app.Activity;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.ibragunduz.fingerprinthelper.IbraFingerPrintHelper;


/**
 * Created by ibrahim Gündüz on 10.02.2018.
 */

public class MainActivity extends Activity implements IbraFingerPrintHelper.FingerPrintSetupListener,IbraFingerPrintHelper.FingerPrintAuthenticationListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            IbraFingerPrintHelper aa = IbraFingerPrintHelper.createHelper(getApplicationContext(),"YOUR_KEY",this,this);
            aa.start();
        }



    }


    @Override
    public void onDeviceNotSupportFingerPrint() {
        showToast("Device not support.");
    }

    @Override
    public void onPermissionDenied() {
        showToast("Permission denied");

    }

    @Override
    public void onThereAreNoRegisteredFingerPrint() {
        showToast("Please add a finger print from settings.");
    }

    @Override
    public void onSucces() {
        showToast("SUCCESSFUL!");

    }

    @Override
    public void onFailed() {
        showToast("Failed! Try another finger print");
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showToast("Sensor dirty, please clean it.");
    }

    @Override
    public void onError(int errMsgId, CharSequence errString) {
        showToast("Error! Try again later");
    }

    private void showToast(String message){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
