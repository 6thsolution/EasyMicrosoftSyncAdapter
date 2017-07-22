package com.sixthsolution.easymssyncadapter.mssyncadapter

import android.accounts.Account
import android.app.Application
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.sixthsolution.easymssyncadapter.GlobalConstant.Companion.EXTRA_SYNC_ADAPTER_CAL_ID
import com.sixthsolution.easymssyncadapter.GlobalConstant.Companion.SYNC_ADAPTER_CAL_ID
import com.sixthsolution.easymssyncadapter.GlobalConstant.Companion.SYNC_ADAPTER_EVENT_ID
import com.sixthsolution.easymssyncadapter.GlobalConstant.Companion.SYNC_ADAPTER_STATE
import com.sixthsolution.easymssyncadapter.broadcasts.SyncBroadcast
import com.sixthsolution.easymssyncadapter.broadcasts.SyncBroadcastStatus
import com.sixthsolution.easymssyncadapter.msauthenticator.IMSLoginHandler
import com.sixthsolution.easymssyncadapter.msauthenticator.LocalMSLoginHandler


/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/17/2017.
 */
class MSSyncAdapter constructor(ctx: Context, autoInit: Boolean, parallelSync: Boolean) :
        AbstractThreadedSyncAdapter(ctx, autoInit, parallelSync) {
    private val loginHandler: IMSLoginHandler
    private val syncManager: IMSSyncManager

    init {
        loginHandler = LocalMSLoginHandler(ctx.applicationContext as Application)
        syncManager = MSSyncManager(loginHandler, ctx)
    }

    constructor(ctx: Context, autoInit: Boolean) : this(ctx, autoInit, false)

    override fun onPerformSync(account: Account?,
                               extras: Bundle?,
                               authority: String?,
                               provider: ContentProviderClient?,
                               syncResult: SyncResult?) {

        var syncState: Int = -1
        if (extras?.containsKey(SYNC_ADAPTER_STATE)!!) {
            syncState = extras.getInt(SYNC_ADAPTER_STATE, -1)
        }

        // try to login silently because user must not see anything
        loginHandler.loginSilent(object : ICallback<Void> {
            override fun failure(p0: ClientException?) {
                // if login failed it means something went wrong with microsoft login library
                // TODO 7/11/2017 try to show user a notification or anything, so he knows there is a need
                // to re login

                syncResult?.stats?.numAuthExceptions?.inc()
            }

            override fun success(p0: Void?) {
                try {
                    if (syncState == -1) {
                        // This is general sync
                        startSyncSequence(syncResult)
                    } else {
                        // This is specified sync
                        when (syncState) {
                            SyncEvent.AddEvent -> {
                                // Add new event to remote server immediately
                                val eventColumnId = extras.getString(SYNC_ADAPTER_EVENT_ID)
                                val calendarId = extras.getString(SYNC_ADAPTER_CAL_ID)
                                if (eventColumnId != null && calendarId != null) {
                                    syncManager.addNewEventToRemote(calendarId, eventColumnId, syncResult)
                                }
                            }

                            SyncEvent.UpdateEvent -> {
                                // Update existing event in remote server
                                val eventColumnId = extras.getString(SYNC_ADAPTER_EVENT_ID)
                                val calendarId = extras.getString(SYNC_ADAPTER_CAL_ID)
                                if (eventColumnId != null && calendarId != null) {
                                    syncManager.updateRemoteEvent(calendarId, eventColumnId, syncResult)
                                }
                            }

                            SyncEvent.DeleteEvent -> {
                                // Delete existing event from remote server
                                val eventColumnId = extras.getString(SYNC_ADAPTER_EVENT_ID)
                                val calendarId = extras.getString(SYNC_ADAPTER_CAL_ID)
                                if (eventColumnId != null) {
                                    syncManager.deleteEventFromServer(calendarId, eventColumnId, syncResult)
                                }
                            }

                        }
                    }
                } catch (e: Exception) {
                    syncResult?.stats?.numIoExceptions?.inc()
                }
            }
        })
    }

    private fun startSyncSequence(syncResult: SyncResult?) {
        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.SyncLocalAndRemoteCalendars, null)

        // Sync local and remote calendar and get list of updated calendars
        val updatedCalendars = syncManager.syncLocalAndRemoteCalendars(syncResult)

        // if sync is canceled return immediately
        if (Thread.interrupted()) {
            // inform any registered receiver of sync status
            SyncBroadcast.fire(context, SyncBroadcastStatus.SyncCanceled, null)
            return
        }

        if (updatedCalendars != null && updatedCalendars.size > 0) {
            for (calendar in updatedCalendars) {

                // inform any registered receiver of sync status
                val bundle = Bundle()
                bundle.putString(EXTRA_SYNC_ADAPTER_CAL_ID, calendar.id)
                SyncBroadcast.fire(context, SyncBroadcastStatus.SyncEventsOfCalendar, bundle)

                syncManager.syncLocalAndRemoteEvents(calendar.id, syncResult)

                // if sync is canceled return immediately
                if (Thread.interrupted()) {
                    // inform any registered receiver of sync status
                    SyncBroadcast.fire(context, SyncBroadcastStatus.SyncCanceled, null)
                    return
                }
            }
        }

        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.SyncFinished, null)
    }
}