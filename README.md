
# Android In-App Update Library [![Build Status](https://travis-ci.com/dnKaratzas/android-inapp-update.svg?branch=master)](https://travis-ci.com/dnKaratzas/android-inapp-update) [ ![Download](https://api.bintray.com/packages/dkaratzas/maven/android-inapp-update/images/download.svg) ](https://bintray.com/dkaratzas/maven/android-inapp-update/_latestVersion)
  

This is a simple implementation of the Android In-App Update API.   
For more information on InApp Updates you can check the official [documentation](https://developer.android.com/guide/app-bundle/in-app-updates)

[JavaDocs](https://dnkaratzas.github.io/android-inapp-update/javadoc/)  and a [sample app](https://github.com/dnKaratzas/android-inapp-update/tree/master/app/src/main/java/eu/dkaratzas/android/inapp/update/sample) with examples implemented are available.

# Getting Started

## Requirements
* You project should build against Android 4.0 (API level 14) SDK at least.
* In-app updates works only with devices running Android 5.0 (API level 21) or higher.

## Add to project
* Add to your project's root `build.gradle` file:  
```groovy
buildscript {  
    repositories {
        jcenter()  
    }
}
```
* Add the dependency to your app `build.gradle` file
```groovy
dependencies {  
    implementation 'eu.dkaratzas:android-inapp-update:1.0.5'
}
```
