package com.sherpa.inapp_update.download

import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.sherpa.inapp_update.R


/**
 * Created by caner on 18.08.2017.
 */
class DownloadApkManager(
    private val context: Activity,
    var downloadFileUrl: String,
    private val fileName: String
) : DownloadFile {
    private var downloadReference: Long = 0
    private var receiverDownloadComplete: BroadcastReceiver? = null
    var downloadManager: DownloadManager? = null
    override fun start(downloadListener: DownloadListener?) {
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(downloadFileUrl)
        val request = DownloadManager.Request(uri)
        request.setDescription("Downloading...")
            .setTitle(fileName)
        request.setDestinationInExternalFilesDir(
            context,
            Environment.DIRECTORY_DOWNLOADS,
            "$fileName.apk"
        )
//        request.setVisibleInDownloadsUi(true)
        request.setNotificationVisibility(View.VISIBLE)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        downloadReference = downloadManager!!.enqueue(request)


        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_layout)
        dialog.setCancelable(false)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtProgressPercent = dialog.findViewById<TextView>(R.id.txtProgressPercent)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)
        dialog.show()
       Thread {
            while (true) {
                val q = DownloadManager.Query()
                q.setFilterById(downloadReference)
                val cursor = downloadManager!!.query(q)
                cursor.moveToFirst()
                val bytesDownloaded: Int =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val bytesTotal: Int =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    dialog.dismiss()
                }
                val progress = (bytesDownloaded * 100L / bytesTotal).toInt()

                  context.runOnUiThread {
                      txtProgressPercent.text =
                          StringBuilder().append("Downloaded ").append(progress).append("%")
                      progressBar.progress = progress
                  }
                cursor.close()
            }
        }.start()


        /**
         *
         * filter for download - on completion
         *
         */
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        receiverDownloadComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val refernce = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadReference == refernce) {
                    //TODO do something with download file..
                    val query = DownloadManager.Query()
                    query.setFilterById(refernce)
                    val cursor = downloadManager!!.query(query)

                    /**
                     * get the status of download.
                     */
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    if (cursor.moveToFirst()) {

                        val status = cursor.getInt(columnIndex)
                        val fileNameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        val savedFilePath = cursor.getString(fileNameIndex)

                        /**
                         * get the reason-more detail on the status.
                         */
                        val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                        val reason = cursor.getInt(columnReason)
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                downloadListener?.OnSuccess(
                                    savedFilePath
                                )
                                Toast.makeText(context,"Download Completed",Toast.LENGTH_SHORT).show()
                            }
                            DownloadManager.STATUS_FAILED -> downloadListener?.OnFailed(reason.toString())
                            DownloadManager.STATUS_PAUSED -> downloadListener?.OnPaused(reason.toString())
                            DownloadManager.STATUS_PENDING -> downloadListener?.OnPending(reason.toString())
                        }
                        cursor.close()
                    }
                }
            }
        }
        context.registerReceiver(receiverDownloadComplete, intentFilter)
    }


}