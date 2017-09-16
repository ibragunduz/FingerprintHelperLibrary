package com.ibragunduz.fingerprinthelperlibrary;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.ibragunduz.fingerprinthelper.FingerPrintHelper;

public class MainActivity extends Activity {
    FingerPrintHelper fph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(MainActivity.this, "Touch the fingerprint reader please", Toast.LENGTH_SHORT).show();

        fph = new FingerPrintHelper(this);
        doControl();

    }
    private void doControl(){
        fph.setControllerToAuth(new FingerPrintHelper.FingerPrintSettingsController() {


            @Override
            public void isDeviceNotSupportFingerPrint() {
                Toast.makeText(MainActivity.this, "isDeviceNotSupportFingerPrint", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void isPermissionNotGranted() {
                Toast.makeText(MainActivity.this, "isPermissionNotGranted", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void isThereNotAnyRegisteredFingerPrint() {
                Toast.makeText(MainActivity.this, "isThereNotAnyRegisteredFingerPrint", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void isKeyguardNotSecure() {
                Toast.makeText(MainActivity.this, "isKeyguardNotSecure", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void isReadyToAuth() {
                startListening();

            }

        });

    }
    private void startListening(){


        fph .setFingerPrintListener(new FingerPrintHelper.FingerPrintListener() {

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, "Stop! don't touch my phone", Toast.LENGTH_SHORT).show();

            }


            @Override
            public void onCorrect() {
                Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                doControl();
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, "Error detected", Toast.LENGTH_SHORT).show();
                doControl();
            }
        });
    }


}
