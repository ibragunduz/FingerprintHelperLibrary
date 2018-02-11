### FingerprintHelperLibrary
### Created to this app:
<a href="https://play.google.com/store/apps/details?id=com.eywinapps.applocker">
	<img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="60" />
</a>

### Gradle Dependency:


***Add this in your root `build.gradle` file (**not** your module `build.gradle` file):****
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

***Add this in your module `build.gradle` file:***  

```gradle

dependencies {
	        compile 'com.github.ibragunduz:FingerprintHelperLibrary:0.1'
}
```
### Implement FingerPrintSetupListener and FingerPrintAuthenticationListener
```java
public class MainActivity extends Activity implements IbraFingerPrintHelper.FingerPrintSetupListener,IbraFingerPrintHelper.FingerPrintAuthenticationListener{

```
### Create an insance with createHelper() method
```java 
IbraFingerPrintHelper fingerPrintHelper = IbraFingerPrintHelper.createHelper(getApplicationContext(),"YOUR_KEY",this,this);
	fingerPrintHelper.start();
```
### Override FingerPrintSetupListener methods

```java

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
    
```
### And Override FingerPrintAuthenticationListener methods

```java

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

```



