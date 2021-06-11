package com.sherpa.inappupdate

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.sherpa.inapp_update.InAppUpdateManager
import com.sherpa.inapp_update.InAppUpdateStatus
import com.sherpa.inapp_update.download.DownloadApkManager
import com.sherpa.inapp_update.download.DownloadListener
import com.sherpa.inappupdate.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity(), InAppUpdateManager.InAppUpdateHandler {
    private val REQ_CODE_VERSION_UPDATE = 530
    private val TAG = MainActivity::class.java.simpleName
    private val PERMISSION_REQUEST_CODE = 200

    private lateinit var binding: ActivityMainBinding

    private lateinit var inAppUpdateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

/*
* if you want to download the apk from certain link to update your app
* */
        if (checkPermission()) {
            val apkDownload = DownloadApkManager(
                this,
                "",
                "live_MOVIES_35_1614170668"
            )
            apkDownload.start(object : DownloadListener {
                override fun OnSuccess(dataPath: String?) {
                    // the file saved in your device..
                    //dataPath--> android/{your app package}/files/Download

                    Log.e("OnSuccess: ", dataPath.toString())
                    installApk(dataPath.toString())

                }

                override fun OnFailed(message: String?) {}
                override fun OnPaused(message: String?) {}
                override fun OnPending(message: String?) {}
            })
        } else {
            requestPermission()
        }

        /*
        * for inapp update
        * */
        /*inAppUpdateManager = InAppUpdateManager.Builder(this, REQ_CODE_VERSION_UPDATE)
            .resumeUpdates(true)
            .setHandler(this);


        binding.toggleButtonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (checkedId == binding.tbImmediateCheck.id && isChecked) {
                inAppUpdateManager.setMode(Constants.UpdateMode.IMMEDIATE)
            } else {
                inAppUpdateManager
                    .setMode(Constants.UpdateMode.FLEXIBLE)
            }
        }

        binding.btUpdate.setOnClickListener { view -> inAppUpdateManager.checkForAppUpdate() }*/

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                val locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (locationAccepted && cameraAccepted) {
                    val apkDownload = DownloadApkManager(
                        this,
                        "http://iptv.worldondemand.net/storage/uploads/market_app/apks/live_MOVIES_35_1614170668.apk",
                        "new_apk"
                    )
                    apkDownload.start(object : DownloadListener {
                        override fun OnSuccess(dataPath: String?) {
                            // the file saved in your device..
                            //dataPath--> android/{your app package}/files/Download

                            Log.e("OnSuccess: ", dataPath.toString())
                            installApk(dataPath.toString())

                        }

                        override fun OnFailed(message: String?) {}
                        override fun OnPaused(message: String?) {}
                        override fun OnPending(message: String?) {}
                    })
                }
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }


    private fun installApk(filePath: String) {
        try {

            val file = File(filePath)
            val intent = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT >= 24) {
                val downloaded_apk = FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName.toString() + ".provider",
                    file
                )
                intent.setDataAndType(downloaded_apk, "application/vnd.android.package-archive")
                val resInfoList: List<ResolveInfo> = packageManager
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                for (resolveInfo in resInfoList) {
                    grantUriPermission(
                        applicationContext.packageName.toString() + ".provider",
                        downloaded_apk,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(intent)
            } else {
                intent.action = Intent.ACTION_VIEW
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        if (requestCode == REQ_CODE_VERSION_UPDATE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // If the update is cancelled by the user,
                // you can request to start the update again.
                inAppUpdateManager.checkForAppUpdate()

                Log.d(TAG, "Update flow failed! Result code: " + resultCode)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
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


        /*
         * Called when the update status change occurred.
         */


        /*
         * Called when the update status change occurred.
         */
        binding.progressBar.visibility = if (status!!.isDownloading) View.VISIBLE else View.GONE

        binding.tvAvailableVersion.text =
            StringBuilder().append("current verson code").append(BuildConfig.VERSION_CODE)
                .append("\nAvailable version code: ").append(status.availableVersionCode())

        binding.tvUpdateAvailable.text = String.format(
            "Update available: %s", java.lang.String.valueOf(
                status.isUpdateAvailable
            )
        )

        if (status.isDownloaded) {
            binding.btUpdate.text = "Complete Update"
            binding.btUpdate.setOnClickListener { view -> inAppUpdateManager.completeUpdate() }
        }
    }
}