package com.sherpa.inapp_update.download


/**
 * Created by Sherpa on 10,June,2021.
 * tenzingsherpaaa@gmail.com
 */
interface DownloadListener {
    fun OnSuccess(dataPath: String?)

    fun OnFailed(message: String?)

    fun OnPaused(message: String?)

    fun OnPending(message: String?)

}