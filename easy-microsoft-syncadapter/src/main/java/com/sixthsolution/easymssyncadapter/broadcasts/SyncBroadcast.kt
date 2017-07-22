package com.sixthsolution.easymssyncadapter.broadcasts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 7/16/2017.
 */
class SyncBroadcast {
    companion object {
        // You can change this if you want to get app specific broadcast trigger
        // This is companion object (static in java) and you can change it directly
        val SYNC_ADAPTER_EXTRA_INFO = "com.sixthsolution.syncadapter.ms.SYNC_ADAPTER_EXTRA_INFO"
        val ACTION_SYNC_STATUS_CHANGED = "com.sixthsolution.syncadapter.ms.ACTION_SYNC_STATUS_CHANGED"
        val SYNC_STATUS = "com.sixthsolution.syncadapter.ms.SYNC_STATUS"

        fun fire(context: Context, syncStatus: SyncBroadcastStatus, extra: Bundle?) {
            val intent = Intent()
            intent.action = ACTION_SYNC_STATUS_CHANGED
            intent.putExtra(SYNC_STATUS, syncStatus)

            if (extra != null) {
                intent.putExtra(SYNC_ADAPTER_EXTRA_INFO, extra)
            }

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}