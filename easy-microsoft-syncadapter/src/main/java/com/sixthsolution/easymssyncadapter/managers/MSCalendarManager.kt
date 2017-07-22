package com.sixthsolution.easymssyncadapter.managers

import android.accounts.Account
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.sixthsolution.easymssyncadapter.GlobalConstant
import com.sixthsolution.easymssyncadapter.broadcasts.SyncBroadcast
import com.sixthsolution.easymssyncadapter.broadcasts.SyncBroadcastStatus
import com.sixthsolution.easymssyncadapter.database.DatabaseClient
import com.sixthsolution.easymssyncadapter.mssyncadapter.SyncEvent
import com.sixthsolution.easymssyncadapter.mssyncadapter.models.MSEvent
import com.sixthsolution.easymssyncadapter.utils.ContentProviderUtil
import com.sixthsolution.mssyncadapter.models.MSCalendar


/**
 * This class is responsible for communicating between developer and internal api for manipulating calendars and
 * events. the api must not use via direct access, so this is the bridge between them
 *
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 7/13/2017.
 */
class MSCalendarManager constructor(val context: Context) : IMSCalendarManager {
    private val resolver = context.contentResolver
    private val gson = Gson()

    override fun addNewEvent(account: Account, calendarId: String,
                             event: MSEvent) {
        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.StartAddEvent_LocalDatabase, null)

        val contentValueEvent = ContentValues()
        contentValueEvent.put(DatabaseClient.Companion.COLUMN_CAL_ID, calendarId)
        contentValueEvent.put(DatabaseClient.Companion.COLUMN_EVENT_ID, event.id)
        contentValueEvent.put(DatabaseClient.Companion.COLUMN_EVENT_JSON, gson.toJson(event, MSEvent::class.java))

        val eventUri = resolver.insert(MSEvent.Companion.CONTENT_URI, contentValueEvent)
        val eventColumnId = ContentUris.parseId(eventUri)

        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.EndAddEvent_LocalDatabase, null)

        val params = Bundle()
        params.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
        params.putInt(GlobalConstant.SYNC_ADAPTER_STATE, SyncEvent.AddEvent)
        params.putString(GlobalConstant.SYNC_ADAPTER_CAL_ID, calendarId)
        params.putString(GlobalConstant.SYNC_ADAPTER_EVENT_ID, eventColumnId.toString())

        ContentResolver.requestSync(account, GlobalConstant.CONTENT_AUTHORITY, params)
    }

    override fun getListOfAvailableCalendars(): List<MSCalendar> {
        return ContentProviderUtil.getListOfLocalCalendar(resolver, gson)
    }

    override fun syncLocalEventsWithRemoteEvents(account: Account) {
        val params = Bundle()
        params.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)

        ContentResolver.requestSync(account, GlobalConstant.CONTENT_AUTHORITY, params)
    }

    private fun getSyncMask(): Int {
        return ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE or ContentResolver.SYNC_OBSERVER_TYPE_PENDING
    }

    override fun getListOfAvailableEvents(calendarId: String): List<MSEvent> {
        return ContentProviderUtil.getListOfLocalEvents(resolver, gson, calendarId)
    }

    override fun updateExistingEvent(account: Account, calendarId: String, eventId: String, event: MSEvent) {
        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.StartUpdateEvent_LocalDatabase, null)

        val contentValueEvent = ContentValues()
        contentValueEvent.put(DatabaseClient.Companion.COLUMN_CAL_ID, calendarId)
        contentValueEvent.put(DatabaseClient.Companion.COLUMN_EVENT_ID, event.id)
        contentValueEvent.put(DatabaseClient.Companion.COLUMN_EVENT_JSON, gson.toJson(event, MSEvent::class.java))

        resolver.update(MSEvent.Companion.CONTENT_URI, contentValueEvent,
                "${DatabaseClient.Companion.COLUMN_CAL_ID}=? AND ${DatabaseClient.Companion.COLUMN_EVENT_ID}=?",
                arrayOf(calendarId, eventId))

        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.EndUpdateEvent_LocalDatabase, null)

        val params = Bundle()
        params.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
        params.putInt(GlobalConstant.SYNC_ADAPTER_STATE, SyncEvent.UpdateEvent)
        params.putString(GlobalConstant.SYNC_ADAPTER_CAL_ID, calendarId)
        params.putString(GlobalConstant.SYNC_ADAPTER_EVENT_ID, eventId)

        ContentResolver.requestSync(account, GlobalConstant.CONTENT_AUTHORITY, params)
    }

    override fun deleteExistingEvent(account: Account, calendarId: String, eventId: String) {
        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.StartDeleteEvent_LocalDatabase, null)

        resolver.delete(MSEvent.Companion.CONTENT_URI,
                "${DatabaseClient.Companion.COLUMN_CAL_ID}=? AND ${DatabaseClient.Companion.COLUMN_EVENT_ID}=?",
                arrayOf(calendarId, eventId))

        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.EndDeleteEvent_LocalDatabase, null)

        val params = Bundle()
        params.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
        params.putInt(GlobalConstant.SYNC_ADAPTER_STATE, SyncEvent.DeleteEvent)
        params.putString(GlobalConstant.SYNC_ADAPTER_CAL_ID, calendarId)
        params.putString(GlobalConstant.SYNC_ADAPTER_EVENT_ID, eventId)

        ContentResolver.requestSync(account, GlobalConstant.CONTENT_AUTHORITY, params)
    }
}