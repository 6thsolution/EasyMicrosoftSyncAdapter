package com.sixthsolution.easymssyncadapter.managers

import android.accounts.Account
import com.sixthsolution.easymssyncadapter.mssyncadapter.models.MSEvent
import com.sixthsolution.mssyncadapter.models.MSCalendar

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 7/13/2017.
 */
interface IMSCalendarManager {

    /**
     * This method will add the event to the local database, then add it to remote server, the update the data base
     * with event is received from server
     *
     * @return returning the uri of new event in local database
     */
    fun addNewEvent(account: Account, calendarId: String, event: MSEvent)

    /**
     * Get the list of all calendars stored in local database
     */
    fun getListOfAvailableCalendars(): List<MSCalendar>

    /**
     * get any changed -or new- events from remote server and update the local events database
     */
    fun syncLocalEventsWithRemoteEvents(account: Account)

    /**
     * Get the list of all events stored in local database
     */
    fun getListOfAvailableEvents(calendarId: String): List<MSEvent>

    /**
     * Update the existing event first in database then update remote server
     */
    fun updateExistingEvent(account: Account, calendarId: String, eventId: String, event: MSEvent)

    /**
     * Delete existing event first from local database then from remote server
     */
    fun deleteExistingEvent(account: Account, calendarId: String, eventId: String)
}