
# Android In-App Update Library
  

This is a simple implementation of the Android In-App Update API.   
For more information on InApp Updates you can check the official [documentation](https://developer.android.com/guide/app-bundle/in-app-updates)


# Getting Started

## Requirements
* You project should build against Android 4.0 (API level 14) SDK at least.
* In-app updates works only with devices running Android 5.0 (API level 21) or higher.

## Add to project
* Add to your project's root `build.gradle` file:  
```groovy
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
* Add the dependency to your app `build.gradle` file
```groovy
dependencies {  
	        implementation 'com.github.tenzein:InappUpdate:1.0.0'
}
```
implementing in Activity


       val inAppUpdateManager = InAppUpdateManager.Builder(this, REQ_CODE_VERSION_UPDATE)
            ?.resumeUpdates(true)
            ?.setMode(Constants.UpdateMode.FLEXIBLE)
            ?.snackBarMessage("An Update has just been downloaded")
            ?.snackBarAction("Restart")
            ?.setHandler(this) 
	    inAppUpdateManager?.checkForAppUpdate()
	    
	
implement handler to get the status of the update
	
	class MainActivity : AppCompatActivity(), InAppUpdateManager.InAppUpdateHandler {
	
	
	}
	
	override fun onInAppUpdateError(code: Int, error: Throwable?) {
        /*
          * Called when some error occurred. See Constants class for more details
          */
        Log.d(TAG, "code: " + code, error)
    }

    override fun onInAppUpdateStatus(status: InAppUpdateStatus?) {
        /*
       * Called when the update status change occurred. See Constants class for more details
       */

        if (status != null) {
            if (status.isDownloaded) {

            }
        }
    }
	
	
NOTE: 
Test with internal app sharing
Use internal app sharing to test in-app updates by performing the following steps:

Make sure your test device has a version of your app installed that supports in-app updates and was installed using an internal app sharing URL.

Follow the Play Console instructions to share your app internally. Upload a version of your app that uses a version code that is higher than the one you already have installed on the test device.

On the test device, click the internal app sharing link for the updated version of your app but do not install the app from the Play Store page that appears after you click the link.

Open the app from the device's app drawer or home screen. The update should now be available to your app, and you can test your implementation of in-app updates.
