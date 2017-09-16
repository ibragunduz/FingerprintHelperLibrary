package com.ibragunduz.fingerprinthelper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Created by ibrahim on 16.09.2017.
 */

public class FingerPrintHelper {
    private static final FingerPrintHelper ourInstance = new FingerPrintHelper();
    static Context context;
    static KeyguardManager keyguardManager;
    static FingerprintManager fingerprintManager;

    public FingerPrintHelper(Context con ) {
        context = con;
        keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);
    }

    private FingerPrintHelper() {
    }


    @TargetApi(Build.VERSION_CODES.M)
private boolean isDeviceSupportFingerPrint() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return fingerprintManager.isHardwareDetected();

    }

    private boolean isPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;

    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isThereAnyRegisteredFingerPrint() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
            return fingerprintManager.hasEnrolledFingerprints();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isKeyguardSecure(){
       return  keyguardManager.isKeyguardSecure();
        }

    private Cipher cipher;
    private KeyStore keyStore;


    public boolean setControllerToAuth(FingerPrintSettingsController fingerPrintSettingsController){
        if (!isDeviceSupportFingerPrint()){
            fingerPrintSettingsController.isDeviceNotSupportFingerPrint();}
        if (!isPermissionGranted()){
            fingerPrintSettingsController.isPermissionNotGranted();}
        if (!isThereAnyRegisteredFingerPrint()){
            fingerPrintSettingsController.isThereNotAnyRegisteredFingerPrint();}
        if (!isKeyguardSecure()){
            fingerPrintSettingsController.isKeyguardNotSecure();}
        if (isThereAnyRegisteredFingerPrint()&&isKeyguardSecure()&&isDeviceSupportFingerPrint()&&isPermissionGranted()){
            fingerPrintSettingsController.isReadyToAuth();
            return true;
        }

    return false;
    }


    @TargetApi(Build.VERSION_CODES.M)
    public boolean setFingerPrintListener(FingerPrintListener fingerPrintListener) {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }


        KeyGenerator keyGenerator;

        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        final String KEY_NAME = "ibragunduz";
        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }

        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);



            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
            FingerprintHandler helper = new FingerprintHandler(context,fingerPrintListener);
            helper.startAuth(fingerprintManager, cryptoObject);


            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }    }
    
    


@TargetApi(Build.VERSION_CODES.M)
 class FingerprintHandler extends FingerprintManager.AuthenticationCallback {


    private Context context;
    private FingerPrintListener fingerPrintListener;

    // Constructor
    public FingerprintHandler(Context mContext,FingerPrintListener fingerPrintListener) {
        context = mContext;
        this.fingerPrintListener = fingerPrintListener;

    }


    @TargetApi(Build.VERSION_CODES.M)
    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        CancellationSignal cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }


    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        fingerPrintListener.onError();
    }


    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
    }


    @Override
    public void onAuthenticationFailed() {
        fingerPrintListener.onFailed();
    }


    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        fingerPrintListener.onCorrect();
    }



}


    public interface FingerPrintListener {
        void onCorrect();
        void onFailed();
        void onError();

    }

    public interface FingerPrintSettingsController {
        void isDeviceNotSupportFingerPrint();
        void isPermissionNotGranted();
        void isThereNotAnyRegisteredFingerPrint();
        void isKeyguardNotSecure();
        void isReadyToAuth();

    }

}
