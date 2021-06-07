
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
