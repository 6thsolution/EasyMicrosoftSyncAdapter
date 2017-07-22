package com.sixthsolution.easymicrosoftsyncadapterapp

import android.accounts.Account
import android.accounts.AccountManager
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import com.google.gson.Gson
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.sixthsolution.easymssyncadapter.GlobalConstant
import com.sixthsolution.easymssyncadapter.broadcasts.SyncBroadcast.Companion.ACTION_SYNC_STATUS_CHANGED
import com.sixthsolution.easymssyncadapter.broadcasts.SyncBroadcastStatus
import com.sixthsolution.easymssyncadapter.broadcasts.SyncReceiver
import com.sixthsolution.easymssyncadapter.managers.IMSCalendarManager
import com.sixthsolution.easymssyncadapter.managers.MSCalendarManager
import com.sixthsolution.easymssyncadapter.msauthenticator.IMSLoginHandler
import com.sixthsolution.easymssyncadapter.msauthenticator.LocalMSLoginHandler
import com.sixthsolution.easymssyncadapter.mssyncadapter.models.MSEvent
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    var accountManager: AccountManager? = null
    var loginHandler: IMSLoginHandler? = null
    var authType: String? = null
    var calendarManager: IMSCalendarManager? = null

    var statusBox: EditText? = null
    var progress: ProgressBar? = null

    enum class EventAction {
        AddEvent,
        UpdateEvent,
        DeleteEvent,
        ShowEvent
    }

    val syncStatusReceiver = object : SyncReceiver() {
        override fun onSyncStatusChanged(syncStatus: SyncBroadcastStatus, bundle: Bundle?) {
            when (syncStatus) {
                SyncBroadcastStatus.SyncError -> updateStatus("Something went wrong")
                SyncBroadcastStatus.SyncCanceled -> updateStatus("Sync operation canceled")
                SyncBroadcastStatus.SyncFinished -> updateStatus("Sync operation finished")
                SyncBroadcastStatus.SyncLocalAndRemoteCalendars -> updateStatus("SyncLocalAndRemoteCalendars", true)
                SyncBroadcastStatus.SyncEventsOfCalendar -> {
                    val calId = bundle?.getString(GlobalConstant.EXTRA_SYNC_ADAPTER_CAL_ID)
                    updateStatus(String.format(Locale.US, "Sync Events Of Calendar: %s", calId), true)
                }
                SyncBroadcastStatus.StartTokenRefresh -> updateStatus("Start Token Refresh", true)
                SyncBroadcastStatus.EndTokenRefresh -> updateStatus("End Token Refresh")
                SyncBroadcastStatus.StartAddEvent_LocalDatabase -> updateStatus("StartAddEvent_LocalDatabase", true)
                SyncBroadcastStatus.EndAddEvent_LocalDatabase -> updateStatus("EndAddEvent_LocalDatabase", true)
                SyncBroadcastStatus.StartAddEvent_RemoteServer -> updateStatus("StartAddEvent_RemoteServer", true)
                SyncBroadcastStatus.EndAddEvent_RemoteServer -> updateStatus("EndAddEvent_RemoteServer")
                SyncBroadcastStatus.StartUpdateEvent_LocalDatabase -> updateStatus("StartUpdateEvent_LocalDatabase", true)
                SyncBroadcastStatus.EndUpdateEvent_LocalDatabase -> updateStatus("EndUpdateEvent_LocalDatabase", true)
                SyncBroadcastStatus.StartUpdateEvent_RemoteServer -> updateStatus("StartUpdateEvent_RemoteServer", true)
                SyncBroadcastStatus.EndUpdateEvent_RemoteServer -> updateStatus("EndUpdateEvent_RemoteServer")
                SyncBroadcastStatus.StartDeleteEvent_LocalDatabase -> updateStatus("StartDeleteEvent_LocalDatabase", true)
                SyncBroadcastStatus.EndDeleteEvent_LocalDatabase -> updateStatus("EndDeleteEvent_LocalDatabase", true)
                SyncBroadcastStatus.StartDeleteEvent_RemoteServer -> updateStatus("StartDeleteEvent_RemoteServer", true)
                SyncBroadcastStatus.EndDeleteEvent_RemoteServer -> updateStatus("EndDeleteEvent_RemoteServer")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(this).registerReceiver(syncStatusReceiver,
                IntentFilter(ACTION_SYNC_STATUS_CHANGED))
    }

    override fun onPause() {
        super.onPause()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(syncStatusReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusBox = findViewById(R.id.status_box) as EditText
        progress = findViewById(R.id.progress) as ProgressBar

        authType = getString(R.string.auth_token_type_full_access)

        accountManager = AccountManager.get(applicationContext)
        loginHandler = LocalMSLoginHandler(application)

        calendarManager = MSCalendarManager(this)

        findViewById(R.id.add_account).setOnClickListener({ addNewAccount() })
        findViewById(R.id.refresh_token).setOnClickListener({ refreshToken() })
        findViewById(R.id.add_event).setOnClickListener({ showCalendarPicker(EventAction.AddEvent) })
        findViewById(R.id.update_event).setOnClickListener({ showCalendarPicker(EventAction.UpdateEvent) })
        findViewById(R.id.delete_event).setOnClickListener({ showCalendarPicker(EventAction.DeleteEvent) })
        findViewById(R.id.sync).setOnClickListener({ sync() })
        findViewById(R.id.get_calendars).setOnClickListener({ getCalendars() })
        findViewById(R.id.get_events).setOnClickListener({ showCalendarPicker(EventAction.ShowEvent) })
    }

    private fun addNewAccount() {
        showProgress(true)

        accountManager?.addAccount(authType, authType, null, null, this, { future ->
            try {
                val bnd: Bundle? = future?.result
                updateStatus(String.format(Locale.US, "New account added\n\n%s", bnd.toString()))
            } catch (ex: Exception) {
                updateStatus(ex.message)
            }
        }, null)
    }

    private fun refreshToken() {
        showProgress(true)

        loginHandler?.loginSilent(object : ICallback<Void> {
            override fun success(aVoid: Void?) {
                updateStatus(String.format(Locale.US, "Token refreshed\n\n%s", loginHandler?.getRefreshToken()))
            }

            override fun failure(ex: ClientException?) {
                updateStatus(String.format(Locale.US, "There is some error\nDID you added new account??\n\n%s",
                        ex?.message))
            }
        })
    }

    private fun addEventToCalendar(account: Account, calendarId: String) {
        // crate a test event
        val json = Gson()
        val event: MSEvent = json.fromJson(CalendarData.eventJson, MSEvent::class.java)

        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
        val date = Date()
        event.subject = String.format(Locale.US, "new Event Title %s", dateFormat.format(date))
        event.body.content = String.format(Locale.US, "new Event Body %s", dateFormat.format(date))
        event.bodyPreview = String.format(Locale.US, "new Event Body Preview %s", dateFormat.format(date))
        event.id = dateFormat.format(date) // reset id
        event.changeKey = dateFormat.format(date)
        event.createdDateTime = Calendar.getInstance() // set create date to now
        event.lastModifiedDateTime = Calendar.getInstance() // set Modified date to now
        val startEndDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        event.start.dateTime = startEndDateFormat.format(date)
        event.end.dateTime = startEndDateFormat.format(date)

        calendarManager?.addNewEvent(account, calendarId, event)
    }

    private fun updateEvent(account: Account, calendarId: String, event: MSEvent) {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
        val date = Date()
        event.subject = String.format(Locale.US, "Updated event Title: %s", dateFormat.format(date))
        event.body.content = String.format(Locale.US, "Updated event Body: %s", dateFormat.format(date))
        event.bodyPreview = String.format(Locale.US, "Updated event Body Preview: %s", dateFormat.format(date))
        event.lastModifiedDateTime = Calendar.getInstance() // set Modified date to now

        calendarManager?.updateExistingEvent(account, calendarId, event.id, event)
    }

    private fun deleteEvent(account: Account, calendarId: String, event: MSEvent) {
        calendarManager?.deleteExistingEvent(account, calendarId, event.id)
    }

    private fun sync() {
        showProgress(true)

        val error = "1) Add an Account\n" +
                "2) Sync it with remote server\n" +
                "3) Then add new account"

        // Check for any registered account, if there is no one return immediately
        val accounts: Array<Account>? = accountManager?.getAccountsByType(authType)
        if (accounts == null || accounts.isEmpty()) {
            updateStatus(String.format(Locale.US, "There in no Microsoft account\n%s", error))
            return
        }

        // Microsoft current library wont allow multi login, so we must use accounts[0] to
        // getting one and only registered account
        calendarManager?.syncLocalEventsWithRemoteEvents(accounts[0])
    }

    private fun getCalendars() {
        showProgress(true)
        val error = "1) Add an Account\n" +
                "2) Sync it with remote server\n" +
                "3) Then request for showing calendar list"

        // Check for any registered account, if there is no one return immediately
        val accounts: Array<Account>? = accountManager?.getAccountsByType(authType)
        if (accounts == null || accounts.isEmpty()) {
            updateStatus(String.format(Locale.US, "There in no Microsoft account\n%s", error))
            return
        }

        // Get list of all local calendars
        val calendars = calendarManager?.getListOfAvailableCalendars()
        if (calendars!!.isEmpty()) {
            updateStatus(String.format(Locale.US, "There in no Calendars in local storage\n%s", error))
            return
        }

        var status: String = "List of available calendars:"
        for (calendar in calendars) {
            status = String.format(Locale.US, "%s\n\nname: %s\nid: %s...\nchangekey: %s...\n%s\n",
                    status,
                    calendar.name,
                    calendar.id.substring(0, 10),
                    calendar.changeKey.substring(0, 10),
                    "********")
        }

        updateStatus(status)
    }

    private fun showEventList(calendarId: String) {
        val eventList = calendarManager?.getListOfAvailableEvents(calendarId)

        if (eventList!!.isEmpty()) {
            updateStatus("There is no event in this calendar")
            return
        }

        var status: String = "List of available events: " + eventList.size
        for (event in eventList) {
            status = String.format(Locale.US, "%s\n\ntitle: %s\nbody: %s\nid: %s...\nchangeKey: %s...\n%s",
                    status,
                    event.subject,
                    event.bodyPreview,
                    event.id.substring(0, 10),
                    event.changeKey.substring(0, 10),
                    "**********")
        }

        updateStatus(status)
    }

    private fun updateStatus(msg: String?, showProgress: Boolean = false) {
        runOnUiThread({
            showProgress(showProgress)
            statusBox?.setText(msg)
        })
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            progress?.visibility = View.VISIBLE
        } else {
            progress?.visibility = View.INVISIBLE
        }
    }

    private fun showCalendarPicker(action: EventAction) {
        showProgress(true)

        val error = String.format(Locale.US, "1) Add an Account\n" +
                "2) Sync it with remote server\n" +
                "3) Then %s", action.name)

        // Check for any registered account, if there is no one return immediately
        val accounts: Array<Account>? = accountManager?.getAccountsByType(authType)
        if (accounts == null || accounts.isEmpty()) {
            updateStatus(String.format(Locale.US, "There in no Microsoft account\n%s", error))
            return
        }

        // Get list of all local calendars
        val calendars = calendarManager?.getListOfAvailableCalendars()
        if (calendars!!.isEmpty()) {
            updateStatus(String.format(Locale.US, "There in no Calendars in local storage\n%s", error))
            return
        }

        val name = arrayOfNulls<String>(calendars.size)
        for (i in 0..calendars.size - 1) {
            name[i] = calendars[i].name
        }

        val alertDialog = AlertDialog.Builder(this).setTitle("Pick a calendar")
                .setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name),
                        {
                            _, which ->
                            when (action) {
                                EventAction.AddEvent -> addEventToCalendar(accounts[0], calendars[which].id)
                                EventAction.DeleteEvent -> showEventPicker(accounts[0], action, calendars[which].id)
                                EventAction.UpdateEvent -> showEventPicker(accounts[0], action, calendars[which].id)
                                EventAction.ShowEvent -> showEventList(calendars[which].id)
                            }
                            // Microsoft current library wont allow multi login, so we must use accounts[0] to
                            // getting one and only registered account
                        })
                .create()
        alertDialog.show()
    }

    private fun showEventPicker(account: Account, action: EventAction, calendarId: String) {
        val eventList = calendarManager?.getListOfAvailableEvents(calendarId)

        if (eventList!!.isEmpty()) {
            updateStatus(String.format(Locale.US, "There in no Event for this calendar"))
            return
        }

        val name = arrayOfNulls<String>(eventList.size)
        for (i in 0..eventList.size - 1) {
            name[i] = eventList[i].subject
        }

        val alertDialog = AlertDialog.Builder(this).setTitle("Pick a event ID")
                .setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name),
                        {
                            _, which ->
                            when (action) {
                                EventAction.AddEvent -> addEventToCalendar(account, calendarId)
                                EventAction.DeleteEvent -> deleteEvent(account, calendarId, eventList[which])
                                EventAction.UpdateEvent -> updateEvent(account, calendarId, eventList[which])
                            }
                        })
                .create()
        alertDialog.show()
    }
}
