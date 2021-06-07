package com.sherpa.inappupdate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.sherpa.inapp_update.Constants
import com.sherpa.inapp_update.InAppUpdateManager
import com.sherpa.inapp_update.InAppUpdateStatus


class MainActivity : AppCompatActivity(), InAppUpdateManager.InAppUpdateHandler {
    private val REQ_CODE_VERSION_UPDATE = 530
    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inAppUpdateManager = InAppUpdateManager.Builder(this, REQ_CODE_VERSION_UPDATE)
            ?.resumeUpdates(true)
            ?.setMode(Constants.UpdateMode.FLEXIBLE)
            ?.snackBarMessage("An Update has just been downloaded")
            ?.snackBarAction("Restart")
            ?.setHandler(this)

        inAppUpdateManager?.checkForAppUpdate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        if (requestCode == REQ_CODE_VERSION_UPDATE) {
            if (resultCode != RESULT_OK) {
                // If the update is cancelled or fails,
                // you can request to start the update again.
                Log.d(TAG, "Update flow failed! Result code: $resultCode")
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

        if (status != null) {
            if (status.isDownloaded) {

            }
        }
    }
}