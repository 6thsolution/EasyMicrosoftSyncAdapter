package com.sixthsolution.easymssyncadapter.mssyncadapter

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 7/12/2017.
 */
class MSSyncService : Service() {
    private val syncAdapterLock = Any()
    private var syncAdapter: MSSyncAdapter? = null

    override fun onCreate() {
        synchronized(syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = MSSyncAdapter(applicationContext, true)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return syncAdapter?.syncAdapterBinder!!
    }
}