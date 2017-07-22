package com.sixthsolution.easymssyncadapter.mssyncadapter

import android.content.*
import com.google.gson.Gson
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.concurrency.SimpleWaiter
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.core.DefaultClientConfig
import com.microsoft.graph.core.IClientConfig
import com.microsoft.graph.extensions.*
import com.sixthsolution.easymssyncadapter.GlobalConstant
import com.sixthsolution.easymssyncadapter.broadcasts.SyncBroadcast
import com.sixthsolution.easymssyncadapter.broadcasts.SyncBroadcastStatus
import com.sixthsolution.easymssyncadapter.database.DatabaseClient
import com.sixthsolution.easymssyncadapter.msauthenticator.IMSLoginHandler
import com.sixthsolution.easymssyncadapter.mssyncadapter.models.MSEvent
import com.sixthsolution.easymssyncadapter.utils.ContentProviderUtil
import com.sixthsolution.mssyncadapter.models.MSCalendar
import java.util.concurrent.atomic.AtomicReference


/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 7/11/2017.
 */
class MSSyncManager constructor(val loginHandler: IMSLoginHandler, val context: Context) : IMSSyncManager {
    private val contentResolver: ContentResolver = context.contentResolver
    private val gson = Gson()
    private val clientConfig: IClientConfig = DefaultClientConfig.createWithAuthenticationProvider(loginHandler)
    private val client: IGraphServiceClient

    init {
        client = GraphServiceClient.Builder().fromConfig(clientConfig).buildClient()
    }

    override fun processLocallyDeleted() {
        // get list of deleted events from database
        val deletedEvents = ContentProviderUtil.getListOfDeletedEventsId(contentResolver, gson)

        // Delete list of deleted events from database
        for (eventId in deletedEvents) {
            client.me.getEvents(eventId).buildRequest().delete()
        }
    }


    override fun syncLocalAndRemoteCalendars(syncResult: SyncResult?): ArrayList<Calendar>? {
        val remoteCalendars: HashMap<String, Calendar> = getListOfRemoteCalendarBlocking() ?: return null
        val localCalendars: Map<String, MSCalendar> = ContentProviderUtil.getMapOfLocalCalendar(contentResolver, gson)
        val toUpdate = ArrayList<Calendar>()
        val toAdd = ArrayList<Calendar>()

        for (localCalId in localCalendars.keys) {
            val remoteCal = remoteCalendars[localCalId]

            if (remoteCal == null) {
                // Remote calendar is not on server anymore, delete it on local
                deleteLocalCalendar(localCalId)
                syncResult?.stats?.numDeletes?.inc()
            } else {
                // calendar is still on server, check whether it has been updated remotely
                val localCal = localCalendars[localCalId]
                if (localCal?.changeKey == remoteCal.changeKey) {
                    syncResult?.stats?.numSkippedEntries?.inc()
                } else {
                    // The calendar updated on server
                    toUpdate.add(remoteCal)
                }

                // remote entry has been seen, remove from list
                remoteCalendars.remove(localCalId)
            }
        }

        // add all of new calendar that added to server
        if (!remoteCalendars.isEmpty()) {
            toAdd.addAll(remoteCalendars.values)
        }

        val batch = ArrayList<ContentProviderOperation>()

        // Update the calendar data, maybe its properties was changed
        for (calendar in toUpdate) {
            batch.add(ContentProviderOperation.newUpdate(MSCalendar.Companion.CONTENT_URI)
                    .withSelection(DatabaseClient.Companion.COLUMN_CAL_ID + "='" + calendar.id + "'", null)
                    .withValue(DatabaseClient.Companion.COLUMN_CAL_JSON, gson.toJson(calendar, Calendar::class.java))
                    .build())

            syncResult?.stats?.numUpdates?.inc()
        }

        // insert any new calendar into database
        for (calendar in toAdd) {
            batch.add(ContentProviderOperation.newInsert(MSCalendar.Companion.CONTENT_URI)
                    .withValue(DatabaseClient.Companion.COLUMN_CAL_ID, calendar.id)
                    .withValue(DatabaseClient.Companion.COLUMN_CAL_JSON, gson.toJson(calendar, Calendar::class.java))
                    .build())

            syncResult?.stats?.numInserts?.inc()
        }

        contentResolver.applyBatch(GlobalConstant.CONTENT_AUTHORITY, batch)
        contentResolver.notifyChange(MSCalendar.Companion.CONTENT_URI, null, false)

        // Microsoft wont change calendar change key if some of events changes, so we need to sync events of all
        // remote calendar
        return ArrayList(remoteCalendars.values)
    }

    private fun getListOfRemoteCalendarBlocking(): HashMap<String, Calendar>? {
        val waiter: SimpleWaiter = SimpleWaiter()
        val returnValue: AtomicReference<MutableList<Calendar>> = AtomicReference()
        val exceptionValue: AtomicReference<Exception> = AtomicReference()

        client.me.calendars.buildRequest()
                .get(object : ICallback<ICalendarCollectionPage> {
                    override fun failure(p0: ClientException?) {
                        exceptionValue.set(p0)
                        waiter.signal()
                    }

                    override fun success(p0: ICalendarCollectionPage?) {
                        returnValue.set(p0?.currentPage)
                        waiter.signal()
                    }

                })

        waiter.waitForSignal()

        if (exceptionValue.get() != null) {
            exceptionValue.get().printStackTrace()
            return null
        }

        val result = HashMap<String, Calendar>(returnValue.get().size)
        for (cal in returnValue.get()) {
            result.put(cal.id, cal)
        }

        return result
    }

    override fun syncLocalAndRemoteEvents(calendarId: String, syncResult: SyncResult?) {
        val remoteEvents: HashMap<String, Event> = getListOfRemoteEvents(calendarId) ?: return
        val localEvents: Map<String, MSEvent> = ContentProviderUtil.getMapOfLocalEvents(contentResolver, gson, calendarId)
        val toUpdate = ArrayList<Event>()
        val toAdd = ArrayList<Event>()

        for (localEventId in localEvents.keys) {
            val remoteEvent = remoteEvents[localEventId]

            if (remoteEvent == null) {
                // Remote event is not on server anymore, delete it on local
                deleteLocalEvent(localEventId)
                syncResult?.stats?.numDeletes?.inc()
            } else {
                // Event is still on server, check whether it has been updated remotely
                val localEvent = localEvents[localEventId]
                if (localEvent?.changeKey == remoteEvent.changeKey) {
                    syncResult?.stats?.numSkippedEntries?.inc()
                } else {
                    // The Event updated on server
                    toUpdate.add(remoteEvent)
                }

                // remote entry has been seen, remove from list
                remoteEvents.remove(localEventId)
            }
        }

        // add all of new events that added to server
        if (!remoteEvents.isEmpty()) {
            toAdd.addAll(remoteEvents.values)
        }

        val batch = ArrayList<ContentProviderOperation>()

        // Update the event data
        for (event in toUpdate) {
            batch.add(ContentProviderOperation.newUpdate(MSEvent.Companion.CONTENT_URI)
                    .withSelection(DatabaseClient.Companion.COLUMN_EVENT_ID + "='" + event.id + "'", null)
                    .withValue(DatabaseClient.Companion.COLUMN_EVENT_JSON, gson.toJson(event, Event::class.java))
                    .build())

            syncResult?.stats?.numUpdates?.inc()
        }

        // insert any new event into database
        for (event in toAdd) {
            batch.add(ContentProviderOperation.newInsert(MSEvent.Companion.CONTENT_URI)
                    .withValue(DatabaseClient.Companion.COLUMN_CAL_ID, calendarId)
                    .withValue(DatabaseClient.Companion.COLUMN_EVENT_ID, event.id)
                    .withValue(DatabaseClient.Companion.COLUMN_EVENT_JSON, gson.toJson(event, Event::class.java))
                    .build())

            syncResult?.stats?.numInserts?.inc()
        }

        contentResolver.applyBatch(GlobalConstant.CONTENT_AUTHORITY, batch)
        contentResolver.notifyChange(MSEvent.Companion.CONTENT_URI, null, false)
    }

    private fun getListOfRemoteEvents(calendarId: String): HashMap<String, Event>? {
        val waiter: SimpleWaiter = SimpleWaiter()
        val returnValue: AtomicReference<MutableList<Event>> = AtomicReference()
        val exceptionValue: AtomicReference<Exception> = AtomicReference()

        client.me.getCalendars(calendarId).events.buildRequest()
                .get(object : ICallback<IEventCollectionPage> {
                    override fun success(p0: IEventCollectionPage?) {
                        returnValue.set(p0?.currentPage)
                        waiter.signal()
                    }

                    override fun failure(p0: ClientException?) {
                        exceptionValue.set(p0)
                        waiter.signal()
                    }
                })

        waiter.waitForSignal()

        if (exceptionValue.get() != null) {
            exceptionValue.get().printStackTrace()
            return null
        }

        val result = HashMap<String, Event>(returnValue.get().size)
        for (event in returnValue.get()) {
            result.put(event.id, event)
        }

        return result
    }

    override fun addNewEventToRemote(calendarId: String, eventColumnId: String, syncResult: SyncResult?) {
        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.StartAddEvent_RemoteServer, null)

        val event = ContentProviderUtil.getLocalEvent(contentResolver, gson, eventColumnId)
        if (event == null) {
            // The event is not exist and i can't go forward, there is no need to reschedule sync for this event
            // i just skip it
            syncResult?.stats?.numSkippedEntries?.inc()
            return
        }

        val waiter: SimpleWaiter = SimpleWaiter()
        val returnValue: AtomicReference<Event> = AtomicReference()
        val exceptionValue: AtomicReference<Exception> = AtomicReference()

        // Add new events to server
        client.me.getCalendars(calendarId).events.buildRequest()
                .post(event, object : ICallback<Event> {
                    override fun failure(p0: ClientException?) {
                        exceptionValue.set(p0)
                        waiter.signal()
                    }

                    override fun success(p0: Event?) {
                        returnValue.set(p0)
                        waiter.signal()
                    }
                })

        waiter.waitForSignal()

        if (exceptionValue.get() != null) {
            exceptionValue.get().printStackTrace()
            // There is an exception in server side (maybe internet problem), so i tell the sync adapter that
            // there is an hard error and i need to reschedule this sync operation in near future
            syncResult?.databaseError = true
            syncResult?.stats?.numIoExceptions?.inc()

            // inform any registered receiver of sync status
            SyncBroadcast.fire(context, SyncBroadcastStatus.SyncError, null)

            return
        }

        val remoteEvent = returnValue.get()

        // Update database with valid ids value
        val values = ContentValues()
        values.put(DatabaseClient.Companion.COLUMN_EVENT_ID, remoteEvent.id)
        values.put(DatabaseClient.Companion.COLUMN_EVENT_JSON, gson.toJson(remoteEvent, Event::class.java))

        val updatedRow = contentResolver.update(MSEvent.Companion.CONTENT_URI,
                values,
                "${DatabaseClient.Companion.COLUMN_ID}=?",
                arrayOf(eventColumnId))

        // The update operation done successfully, so i tell the sync adapter that every thing is ok and he can
        // notify any registered listener
        syncResult?.stats?.numInserts?.inc()
        contentResolver.notifyChange(MSEvent.Companion.CONTENT_URI, null, false)

        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.EndAddEvent_RemoteServer, null)
    }

    override fun updateRemoteEvent(calendarId: String, eventId: String, syncResult: SyncResult?) {
        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.StartUpdateEvent_RemoteServer, null)

        //Get the event from database
        val event = getUpdateEventFromDatabase(eventId)

        if (event == null) {
            // The event is not exist and i can't go forward, there is no need to reschedule sync for this event
            // i just skip it
            syncResult?.stats?.numSkippedEntries?.inc()
            return
        }

        val waiter: SimpleWaiter = SimpleWaiter()
        val returnValue: AtomicReference<Event> = AtomicReference()
        val exceptionValue: AtomicReference<Exception> = AtomicReference()

        // Add new events to server
        client.me.getCalendars(calendarId).getEvents(eventId).buildRequest()
                .patch(event, object : ICallback<Event> {
                    override fun failure(p0: ClientException?) {
                        exceptionValue.set(p0)
                        waiter.signal()
                    }

                    override fun success(p0: Event?) {
                        returnValue.set(p0)
                        waiter.signal()
                    }
                })

        waiter.waitForSignal()

        if (exceptionValue.get() != null) {
            exceptionValue.get().printStackTrace()
            // There is an exception in server side (maybe internet problem), so i tell the sync adapter that
            // there is an hard error and i need to reschedule this sync operation in near future
            syncResult?.databaseError = true
            syncResult?.stats?.numIoExceptions?.inc()

            // inform any registered receiver of sync status
            SyncBroadcast.fire(context, SyncBroadcastStatus.SyncError, null)

            return
        }

        val remoteEvent = returnValue.get()

        // Update database with valid ids value
        val values = ContentValues()
        values.put(DatabaseClient.Companion.COLUMN_EVENT_ID, remoteEvent.id)
        values.put(DatabaseClient.Companion.COLUMN_EVENT_JSON, gson.toJson(remoteEvent, Event::class.java))

        val updatedRow = contentResolver.update(MSEvent.Companion.CONTENT_URI,
                values,
                "${DatabaseClient.Companion.COLUMN_EVENT_ID}=?",
                arrayOf(eventId))

        // The update operation done successfully, so i tell the sync adapter that every thing is ok and he can
        // notify any registered listener
        syncResult?.stats?.numInserts?.inc()
        contentResolver.notifyChange(MSEvent.Companion.CONTENT_URI, null, false)

        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.EndUpdateEvent_RemoteServer, null)
    }

    private fun getUpdateEventFromDatabase(eventId: String): MSEvent? {
        val cursor = contentResolver.query(MSEvent.Companion.CONTENT_URI,
                null,
                "${DatabaseClient.Companion.COLUMN_EVENT_ID}=?",
                arrayOf(eventId),
                null,
                null)

        if (cursor != null) {
            cursor.moveToFirst()
            val event = gson.fromJson(cursor.getString(cursor.getColumnIndex(DatabaseClient.Companion.COLUMN_EVENT_JSON)),
                    MSEvent::class.java)
            cursor.close()
            return event
        }

        return null
    }

    override fun deleteEventFromServer(calendarId: String, eventId: String, syncResult: SyncResult?) {
        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.StartDeleteEvent_RemoteServer, null)

        val waiter: SimpleWaiter = SimpleWaiter()
        val returnValue: AtomicReference<Void> = AtomicReference()
        val exceptionValue: AtomicReference<Exception> = AtomicReference()

        // Delete the event from remote server
        client.me.getCalendars(calendarId).getEvents(eventId).buildRequest()
                .delete(object : ICallback<Void> {
                    override fun failure(p0: ClientException?) {
                        exceptionValue.set(p0)
                        waiter.signal()
                    }

                    override fun success(p0: Void?) {
                        returnValue.set(p0)
                        waiter.signal()
                    }
                })

        waiter.waitForSignal()

        if (exceptionValue.get() != null) {
            exceptionValue.get().printStackTrace()
            // There is an exception in server side (maybe internet problem), so i tell the sync adapter that
            // there is an hard error and i need to reschedule this sync operation in near future
            syncResult?.databaseError = true
            syncResult?.stats?.numIoExceptions?.inc()

            // inform any registered receiver of sync status
            SyncBroadcast.fire(context, SyncBroadcastStatus.SyncError, null)

            return
        }

        // The delete operation done successfully, so i tell the sync adapter that every thing is ok and he can
        // notify any registered listener
        syncResult?.stats?.numDeletes?.inc()
        contentResolver.notifyChange(MSEvent.Companion.CONTENT_URI, null, false)

        // inform any registered receiver of sync status
        SyncBroadcast.fire(context, SyncBroadcastStatus.EndDeleteEvent_RemoteServer, null)
    }

    /**
     * delete calendar and all of it's events from database
     */
    fun deleteLocalCalendar(calendarId: String) {
        contentResolver.delete(MSCalendar.Companion.CONTENT_URI, "${DatabaseClient.Companion.COLUMN_CAL_ID}=?", arrayOf(calendarId))
        contentResolver.delete(MSEvent.Companion.CONTENT_URI, "${DatabaseClient.Companion.COLUMN_CAL_ID}=?", arrayOf(calendarId))
    }

    /**
     * delete the local event from database
     */
    fun deleteLocalEvent(eventId: String) {
        contentResolver.delete(MSEvent.Companion.CONTENT_URI, "${DatabaseClient.Companion.COLUMN_EVENT_ID}=?", arrayOf(eventId))
    }

}