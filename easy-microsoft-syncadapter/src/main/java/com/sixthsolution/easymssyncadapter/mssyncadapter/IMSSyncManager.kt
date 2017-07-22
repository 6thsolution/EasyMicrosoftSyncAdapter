package com.sixthsolution.easymssyncadapter.mssyncadapter

import android.content.SyncResult
import com.microsoft.graph.extensions.Calendar

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 7/11/2017.
 */
interface IMSSyncManager {

    /**
     * Check if any of the event deleted from local calendar and delete them from server calendar
     */
    fun processLocallyDeleted()

    /**
     * Check if any of the remote calendar changed {delete or insert not update}
     * If any of the remote calendar deleted, then the calendar and all of its events must be deleted
     *
     * NOTE: This method is BLOCKING
     *
     * @return list of calendars that their events need to update, either new calendar or just updated one
     */
    fun syncLocalAndRemoteCalendars(syncResult: SyncResult?): ArrayList<Calendar>?

    /**
     * Check if any of the remote events changed or deleted or newly added
     * Note: The local change include add, delete or update will take effect via another method instantly, this
     * method just focus on remote part
     */
    fun syncLocalAndRemoteEvents(calendarId: String, syncResult: SyncResult?)

    /**
     * THIS IS BLOCKING METHOD
     *
     * Add this new Event to server for specified calendar
     * Get the remote event id and change key and update the local database with those value
     *
     * Note: if database added locally so it has not any valid event id or change key, we just grab the event from
     * database based on it's column id
     */
    fun addNewEventToRemote(calendarId: String, eventColumnId: String, syncResult: SyncResult?)

    /**
     * THIS IS BLOCKING METHOD
     *
     * Update the remote event with this event
     * Get the change key from server and update the local database
     */
    fun updateRemoteEvent(calendarId: String, eventId: String, syncResult: SyncResult?)

    /**
     * Delete this event id from server
     */
    fun deleteEventFromServer(calendarId: String, eventId: String, syncResult: SyncResult?)
}