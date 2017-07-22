package com.sixthsolution.easymssyncadapter.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sixthsolution.easymssyncadapter.broadcasts.SyncBroadcast.Companion.SYNC_ADAPTER_EXTRA_INFO
import com.sixthsolution.easymssyncadapter.broadcasts.SyncBroadcast.Companion.SYNC_STATUS

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 7/16/2017.
 */
abstract class SyncReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val syncStatus = intent?.extras?.getSerializable(SYNC_STATUS) as SyncBroadcastStatus
        var bundle: Bundle? = null
        if (intent.hasExtra(SYNC_ADAPTER_EXTRA_INFO)) {
            bundle = intent.getBundleExtra(SYNC_ADAPTER_EXTRA_INFO)
        }

        onSyncStatusChanged(syncStatus, bundle)
    }

    abstract fun onSyncStatusChanged(syncStatus: SyncBroadcastStatus, bundle: Bundle?)
}