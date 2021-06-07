package com.sherpa.inapp_update

import android.content.IntentSender
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task

/**
 *
 * For more information about In-App Updates you can check the official
 * [documentation](https://developer.android.com/guide/app-bundle/in-app-updates)
 *
 *
 */
class InAppUpdateManager : LifecycleObserver {
    /**
     * Callback methods where update events are reported.
     */
    interface InAppUpdateHandler {
        /**
         * On update error.
         *
         * @param code  the code
         * @param error the error
         */
        fun onInAppUpdateError(code: Int, error: Throwable?)

        /**
         * Monitoring the update state of the flexible downloads.
         * For immediate updates, Google Play takes care of downloading and installing the update for you.
         *
         * @param status the status
         */
        fun onInAppUpdateStatus(status: InAppUpdateStatus?)
    }

    private var activity: AppCompatActivity
    private var appUpdateManager: AppUpdateManager? = null
    private var requestCode = 64534
    private var snackBarMessage = "An update has just been downloaded."
    private var snackBarAction = "RESTART"
    private var mode: Constants.UpdateMode = Constants.UpdateMode.FLEXIBLE
    private var resumeUpdates = true
    private var useCustomNotification = false
    private var handler: InAppUpdateHandler? = null
    private var snackbar: Snackbar? = null
    private val inAppUpdateStatus = InAppUpdateStatus()
    private val installStateUpdatedListener: InstallStateUpdatedListener =
        InstallStateUpdatedListener { installState ->
            inAppUpdateStatus.setInstallState(installState)
            reportStatus()

            // Show module progress, log state, or install the update.
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                popupSnackBarForUserConfirmation()
            }
        }

    private constructor(activity: AppCompatActivity) {
        this.activity = activity
        setupSnackBar()
        init()
    }

    private constructor(activity: AppCompatActivity, requestCode: Int) {
        this.activity = activity
        this.requestCode = requestCode
        init()
    }

    private fun init() {
        setupSnackBar()
        appUpdateManager = AppUpdateManagerFactory.create(activity)
        activity.lifecycle.addObserver(this)
        if (mode === Constants.UpdateMode.FLEXIBLE) appUpdateManager!!.registerListener(
            installStateUpdatedListener
        )
        checkForUpdate(false)
    }

    /**
     * Set the update mode.
     *
     * @param mode the update mode
     * @return the update manager instance
     */
    fun setMode(mode: Constants.UpdateMode): InAppUpdateManager {
        this.mode = mode
        return this
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * If the update is downloaded but not installed, will notify
     * the user to complete the update.
     *
     * @param resumeUpdates the resume updates
     * @return the update manager instance
     */
    fun resumeUpdates(resumeUpdates: Boolean): InAppUpdateManager {
        this.resumeUpdates = resumeUpdates
        return this
    }

    /**
     * Set the callback handler
     *
     * @param handler the handler
     * @return the update manager instance
     */
    fun setHandler(handler: InAppUpdateHandler?): InAppUpdateManager {
        this.handler = handler
        return this
    }

    fun snackBarMessage(snackBarMessage: String): InAppUpdateManager {
        this.snackBarMessage = snackBarMessage
        setupSnackBar()
        return this
    }

    fun snackBarAction(snackBarAction: String): InAppUpdateManager {
        this.snackBarAction = snackBarAction
        setupSnackBar()
        return this
    }

    fun snackBarActionColor(color: Int): InAppUpdateManager {
        snackbar?.setActionTextColor(color)
        return this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (resumeUpdates) checkNewAppVersionState()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        unregisterListener()
    }

    /**
     * Check for update availability. If there will be an update available
     * will start the update process with the selected [UpdateMode].
     */
    fun checkForAppUpdate() {
        checkForUpdate(true)
    }

    /**
     * Triggers the completion of the app update for the flexible flow.
     */
    fun completeUpdate() {
        appUpdateManager?.completeUpdate()
    }

    /**
     * Check for update availability. If there will be an update available
     * will start the update process with the selected [UpdateMode].
     */
    private fun checkForUpdate(startUpdate: Boolean) {

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager!!.appUpdateInfo


        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            inAppUpdateStatus.setAppUpdateInfo(appUpdateInfo)
            if (startUpdate) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {

                    // Request the update.
                    if (mode === Constants.UpdateMode.FLEXIBLE && appUpdateInfo.isUpdateTypeAllowed(
                            AppUpdateType.FLEXIBLE
                        )
                    ) {
                        // Start an update.
                        startAppUpdateFlexible(appUpdateInfo)
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        // Start an update.
                        startAppUpdateImmediate(appUpdateInfo)
                    }
                    Log.d(
                        LOG_TAG,
                        "checkForAppUpdate(): Update available. Version Code: " + appUpdateInfo.availableVersionCode()
                    )
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                    Log.d(
                        LOG_TAG,
                        "checkForAppUpdate(): No Update available. Code: " + appUpdateInfo.updateAvailability()
                    )
                }
            }
            reportStatus()
        }
    }

    private fun startAppUpdateImmediate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager?.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,  // The current activity making the update request.
                activity,  // Include a request code to later monitor this update request.
                requestCode
            )
        } catch (e: IntentSender.SendIntentException) {
            Log.e(LOG_TAG, "error in startAppUpdateImmediate", e)
            reportUpdateError(Constants.UPDATE_ERROR_START_APP_UPDATE_IMMEDIATE, e)
        }
    }

    private fun startAppUpdateFlexible(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager?.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,  // The current activity making the update request.
                activity,  // Include a request code to later monitor this update request.
                requestCode
            )
        } catch (e: IntentSender.SendIntentException) {
            Log.e(LOG_TAG, "error in startAppUpdateFlexible", e)
            reportUpdateError(Constants.UPDATE_ERROR_START_APP_UPDATE_FLEXIBLE, e)
        }
    }

    /**
     * Displays the snackBar notification and call to action.
     * Needed only for Flexible app update
     */
    private fun popupSnackBarForUserConfirmation() {
        if (!useCustomNotification) {
            if (snackbar != null && snackbar!!.isShownOrQueued) snackbar!!.dismiss()
            snackbar?.show()
        }
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */
    private fun checkNewAppVersionState() {
        appUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener { appUpdateInfo ->
                inAppUpdateStatus.setAppUpdateInfo(appUpdateInfo)

                //FLEXIBLE:
                // If the update is downloaded but not installed,
                // notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackBarForUserConfirmation()
                    reportStatus()
                    Log.d(
                        LOG_TAG,
                        "checkNewAppVersionState(): resuming flexible update. Code: " + appUpdateInfo.updateAvailability()
                    )
                }

                //IMMEDIATE:
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // If an in-app update is already running, resume the update.
                    startAppUpdateImmediate(appUpdateInfo)
                    Log.d(
                        LOG_TAG,
                        "checkNewAppVersionState(): resuming immediate update. Code: " + appUpdateInfo.updateAvailability()
                    )
                }
            }
    }

    private fun setupSnackBar() {
        val rootView: View = activity.window.decorView.findViewById<View>(R.id.content)
        snackbar = Snackbar.make(
            rootView,
            snackBarMessage,
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar!!.setAction(
            snackBarAction,
            View.OnClickListener { // Triggers the completion of the update of the app for the flexible flow.
                appUpdateManager?.completeUpdate()
            })
    }

    private fun unregisterListener() {
        if (appUpdateManager != null) appUpdateManager!!.unregisterListener(
            installStateUpdatedListener
        )
    }

    private fun reportUpdateError(errorCode: Int, error: Throwable) {
        if (handler != null) {
            handler!!.onInAppUpdateError(errorCode, error)
        }
    }

    private fun reportStatus() {
        if (handler != null) {
            handler!!.onInAppUpdateStatus(inAppUpdateStatus)
        }
    }

    companion object {
        private const val LOG_TAG = "InAppUpdateManager"

        private var instance: InAppUpdateManager? = null

        /**
         * Creates a builder that uses the default requestCode.
         *
         * @param activity the activity
         * @return a new [InAppUpdateManager] instance
         */
        fun Builder(activity: AppCompatActivity): InAppUpdateManager? {
            if (instance == null) {
                instance = InAppUpdateManager(activity)
            }
            return instance
        }

        /**
         * Creates a builder
         *
         * @param activity    the activity
         * @param requestCode the request code to later monitor this update request via onActivityResult()
         * @return a new [InAppUpdateManager] instance
         */
        fun Builder(activity: AppCompatActivity, requestCode: Int): InAppUpdateManager? {
            if (instance == null) {
                instance = InAppUpdateManager(activity, requestCode)
            }
            return instance
        }
    }
}