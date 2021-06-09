package com.sherpa.inappupdate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.sherpa.inapp_update.Constants.UpdateMode
import com.sherpa.inapp_update.InAppUpdateManager
import com.sherpa.inapp_update.InAppUpdateStatus
import com.sherpa.inappupdate.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), InAppUpdateManager.InAppUpdateHandler {
    private val REQ_CODE_VERSION_UPDATE = 530
    private val TAG = MainActivity::class.java.simpleName

    private lateinit var binding:ActivityMainBinding

    private var inAppUpdateManager: InAppUpdateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        inAppUpdateManager = InAppUpdateManager.Builder(this, REQ_CODE_VERSION_UPDATE)
            ?.resumeUpdates(true)
            ?.setHandler(this);


        binding.toggleButtonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (checkedId == binding.tbImmediateCheck.id && isChecked) {
                inAppUpdateManager!!.setMode(UpdateMode.IMMEDIATE)
            } else {
                inAppUpdateManager
                    ?.setMode(UpdateMode.FLEXIBLE)
            }
        }

        binding.btUpdate.setOnClickListener { view -> inAppUpdateManager!!.checkForAppUpdate() }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        if (requestCode == REQ_CODE_VERSION_UPDATE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // If the update is cancelled by the user,
                // you can request to start the update again.
                inAppUpdateManager?.checkForAppUpdate();

                Log.d(TAG, "Update flow failed! Result code: " + resultCode);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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

        binding.tvAvailableVersion.text = java.lang.String.format(
            "Available version code: %d",
            status.availableVersionCode()
        )
        binding.tvUpdateAvailable.text = String.format(
            "Update available: %s", java.lang.String.valueOf(
                status.isUpdateAvailable
            )
        )

        if (status.isDownloaded) {
            binding.btUpdate.text = "Complete Update"
            binding.btUpdate.setOnClickListener { view -> inAppUpdateManager!!.completeUpdate() }
        }
    }
}