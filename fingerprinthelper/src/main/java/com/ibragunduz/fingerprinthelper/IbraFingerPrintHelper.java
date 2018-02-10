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
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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
 * Created by ibrahim Gündüz on 10.02.2018.
 */


@RequiresApi(23)
public class IbraFingerPrintHelper {



    private KeyguardManager keyguardManager;
    private  FingerprintManager fingerprintManager;
    private  static Context context;
    private static String KEY;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerPrintAuthenticationListener fingerPrintAuthenticationListener;

    private FingerprintManager.CryptoObject cryptoObject;
    private  FingerprintHandler helper;

    /** @hide */
    private IbraFingerPrintHelper(FingerPrintAuthenticationListener fingerPrintAuthenticationListene,FingerPrintSetupListener fingerPrintSetupListene) {

        if (Build.VERSION_CODES.M<=Build.VERSION.SDK_INT) {
            keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
            fingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);

            fingerPrintAuthenticationListener = fingerPrintAuthenticationListene;

            setFingerPrintSetupListener(fingerPrintSetupListene);
            if (!isDeviceNotSupportFingerPrint() && !isThereAreNoRegisteredFingerPrint() && !isPermissionDenied()) {


                try {
                    keyStore = KeyStore.getInstance("AndroidKeyStore");
                    keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                    keyStore.load(null);
                    keyGenerator.init(new
                            KeyGenParameterSpec.Builder(KEY,
                            KeyProperties.PURPOSE_ENCRYPT |
                                    KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(
                                    KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .build());

                    keyGenerator.generateKey();
                    cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                    keyStore.load(null);
                    SecretKey key = (SecretKey) keyStore.getKey(KEY,
                            null);
                    cipher.init(Cipher.ENCRYPT_MODE, key);

                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
                cryptoObject = new FingerprintManager.CryptoObject(cipher);
                helper = new FingerprintHandler(context, fingerPrintAuthenticationListener);

            }
        }
        else{
            fingerPrintSetupListene.onDeviceNotSupportFingerPrint();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static IbraFingerPrintHelper createHelper(@NonNull Context CONTEXT, @NonNull String KEY_NAME, @NonNull FingerPrintAuthenticationListener fingerPrintAuthenticationListener, @NonNull FingerPrintSetupListener fingerPrintSetupListener){
        context = CONTEXT;
        KEY = KEY_NAME;
        return new IbraFingerPrintHelper(fingerPrintAuthenticationListener,fingerPrintSetupListener);
    }



    public void start(){
        if (helper!=null){
            helper.startAuth(fingerprintManager, cryptoObject);
        }
    }
    public void stop(){
        if (helper!=null){
            helper.stopAuth();
        }

    }


    private void setFingerPrintSetupListener(FingerPrintSetupListener fingerPrintSetupListener){
        if (isDeviceNotSupportFingerPrint())fingerPrintSetupListener.onDeviceNotSupportFingerPrint();
        if (isPermissionDenied())fingerPrintSetupListener.onPermissionDenied();
        if (isThereAreNoRegisteredFingerPrint())fingerPrintSetupListener.onThereAreNoRegisteredFingerPrint();
    }

    public boolean isDeviceNotSupportFingerPrint(){
        if (fingerprintManager!=null)
            return !fingerprintManager.isHardwareDetected() || Build.VERSION_CODES.M > Build.VERSION.SDK_INT;
        else return false;
    }
    public boolean isPermissionDenied(){
        if (fingerprintManager!=null)

            return   ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED;
        else return false;

    }
    public   boolean isThereAreNoRegisteredFingerPrint(){
        if (fingerprintManager!=null)
            return !fingerprintManager.hasEnrolledFingerprints();
        else return true;
    }


    @TargetApi(Build.VERSION_CODES.M)
    private class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

        private CancellationSignal cancellationSignal;
        private Context context;
        private FingerPrintAuthenticationListener fingerPrintListener;

        public FingerprintHandler(Context mContext,FingerPrintAuthenticationListener fingerPrintListener) {
            context = mContext;
            this.fingerPrintListener = fingerPrintListener;

        }


        @TargetApi(Build.VERSION_CODES.M)
        private void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
            cancellationSignal = new CancellationSignal();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);

        }

        private void stopAuth(){
            cancellationSignal.cancel();
        }

        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            fingerPrintListener.onError(errMsgId,errString);
        }


        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            fingerPrintListener.onAuthenticationHelp(helpMsgId,helpString);
        }


        @Override
        public void onAuthenticationFailed() {
            fingerPrintListener.onFailed();
        }


        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            fingerPrintListener.onSucces();

        }

    }

    public interface FingerPrintSetupListener{
        void onDeviceNotSupportFingerPrint();
        void onPermissionDenied();
        void onThereAreNoRegisteredFingerPrint();
    }
    public interface FingerPrintAuthenticationListener{
        void onSucces();
        void onFailed();
        void onAuthenticationHelp(int helpMsgId, CharSequence helpString);
        void onError(int errMsgId, CharSequence errString);
    }



}
