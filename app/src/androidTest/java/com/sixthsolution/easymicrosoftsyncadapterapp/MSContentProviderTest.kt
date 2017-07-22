package com.sixthsolution.easymicrosoftsyncadapterapp

import android.content.ContentValues
import android.support.test.runner.AndroidJUnit4
import android.test.ProviderTestCase2
import com.google.gson.Gson
import com.sixthsolution.easymssyncadapter.GlobalConstant
import com.sixthsolution.easymssyncadapter.database.DatabaseClient
import com.sixthsolution.easymssyncadapter.database.DatabaseClient.Companion.COLUMN_CAL_ID
import com.sixthsolution.easymssyncadapter.database.DatabaseClient.Companion.COLUMN_CAL_JSON
import com.sixthsolution.easymssyncadapter.database.DatabaseClient.Companion.COLUMN_EVENT_ID
import com.sixthsolution.easymssyncadapter.database.DatabaseClient.Companion.COLUMN_EVENT_JSON
import com.sixthsolution.easymssyncadapter.mssyncadapter.models.MSEvent
import com.sixthsolution.easymssyncadapter.provider.MSContentProvider
import com.sixthsolution.mssyncadapter.models.MSCalendar
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/18/2017.
 */
@RunWith(AndroidJUnit4::class)
class MSContentProviderTest : ProviderTestCase2<MSContentProvider>(MSContentProvider::class.java, GlobalConstant.CONTENT_AUTHORITY) {

    var json: Gson? = null

    val calendarId = "AQMkADAwATM0MDAAMS1iNjYBLTM0NABiLTAwAi0wMAoARgAAA0hxsVr2z35Lmv5x4AJQMGgHAAdeOKX0xX1HuapqFYeI3DIAAAIBBgAAAAdeOKX0xX1HuapqFYeI3DIAAAI4HAAAAA"
    val eventId = "AQMkADAwATM0MDAAMS1iNjYBLTM0NABiLTAwAi0wMAoARgAAA0hxsVr2z35Lmv5x4AJQMGgHAAdeOKX0xX1HuapqFYeI3DIAAAIBDQAAAAdeOKX0xX1HuapqFYeI3DIAAADS5HzoAAAA"

    private val calendarJson = """{
   "changeKey" : "B144pfTFfUe5qmoVh4jcMgAAAAA9IA==",
   "color" : "auto",
   "name" : "Calendar",
   "id" : "AQMkADAwATM0MDAAMS1iNjYBLTM0NABiLTAwAi0wMAoARgAAA0hxsVr2z35Lmv5x4AJQMGgHAAdeOKX0xX1HuapqFYeI3DIAAAIBBgAAAAdeOKX0xX1HuapqFYeI3DIAAAI4HAAAAA==",
   "@odata.type" : "microsoft.graph.calendar"
    }"""

    private val eventJson = """{
   "attendees" : [],
   "body" : {
      "content" : "",
      "contentType" : "html",
      "@odata.type" : "microsoft.graph.itemBody"
   },
   "bodyPreview" : "",
   "end" : {
      "dateTime" : "2017-06-15T07:00:00.0000000",
      "@odata.type" : "microsoft.graph.dateTimeTimeZone",
      "timeZone" : "UTC"
   },
   "hasAttachments" : false,
   "iCalUId" : "040000008200E00074C5B7101A82E00800000000D71AF6029DE5D2010000000000000000100000005E4271EB34B13A4EB1E2E26B34E1DD60",
   "importance" : "normal",
   "isAllDay" : false,
   "isCancelled" : false,
   "isOrganizer" : true,
   "isReminderOn" : true,
   "location" : {
      "address" : {
         "@odata.type" : "microsoft.graph.physicalAddress"
      },
      "displayName" : "",
      "@odata.type" : "microsoft.graph.location"
   },
   "organizer" : {
      "emailAddress" : {
         "address" : "mehdok@outlook.com",
         "name" : "mehdi sohrabi",
         "@odata.type" : "microsoft.graph.emailAddress"
      },
      "@odata.type" : "microsoft.graph.recipient"
   },
   "originalEndTimeZone" : "Iran Standard Time",
   "originalStartTimeZone" : "Iran Standard Time",
   "reminderMinutesBeforeStart" : 15,
   "responseRequested" : true,
   "responseStatus" : {
      "@odata.type" : "microsoft.graph.responseStatus",
      "response" : "organizer",
      "time" : {
         "year" : 1,
         "month" : 0,
         "dayOfMonth" : 1,
         "hourOfDay" : 0,
         "minute" : 0,
         "second" : 0
      }
   },
   "sensitivity" : "normal",
   "showAs" : "busy",
   "start" : {
      "dateTime" : "2017-06-15T06:30:00.0000000",
      "@odata.type" : "microsoft.graph.dateTimeTimeZone",
      "timeZone" : "UTC"
   },
   "subject" : "test",
   "type" : "singleInstance",
   "webLink" : "https://outlook.live.com/owa/?itemid=AQMkADAwATM0MDAAMS1iNjYBLTM0NABiLTAwAi0wMAoARgAAA0hxsVr2z35Lmv5x4AJQMGgHAAdeOKX0xX1HuapqFYeI3DIAAAIBDQAAAAdeOKX0xX1HuapqFYeI3DIAAADS5HzoAAAA&exvsurl=1&path=/calendar/item",
   "categories" : [],
   "changeKey" : "B144pfTFfUe5qmoVh4jcMgAA0v9hRQ==",
   "createdDateTime" : {
      "year" : 2017,
      "month" : 5,
      "dayOfMonth" : 15,
      "hourOfDay" : 6,
      "minute" : 2,
      "second" : 46
   },
   "lastModifiedDateTime" : {
      "year" : 2017,
      "month" : 5,
      "dayOfMonth" : 15,
      "hourOfDay" : 6,
      "minute" : 2,
      "second" : 46
   },
   "id" : "AQMkADAwATM0MDAAMS1iNjYBLTM0NABiLTAwAi0wMAoARgAAA0hxsVr2z35Lmv5x4AJQMGgHAAdeOKX0xX1HuapqFYeI3DIAAAIBDQAAAAdeOKX0xX1HuapqFYeI3DIAAADS5HzoAAAA",
   "@odata.type" : "microsoft.graph.event"
    }"""

    @Before
    @Throws(Exception::class)
    override public fun setUp() {
        super.setUp()

        json = Gson()

        // first insert some data
        val contentValueCal = ContentValues()
        contentValueCal.put(COLUMN_CAL_ID, calendarId)
        contentValueCal.put(COLUMN_CAL_JSON, calendarJson)
        mockContentResolver.insert(MSCalendar.Companion.CONTENT_URI, contentValueCal)

        val contentValueEvent = ContentValues()
        contentValueEvent.put(COLUMN_CAL_ID, calendarId)
        contentValueEvent.put(COLUMN_EVENT_ID, eventId)
        contentValueEvent.put(DatabaseClient.COLUMN_EVENT_JSON, eventJson)
        mockContentResolver.insert(MSEvent.CONTENT_URI, contentValueEvent)
    }

    @Test
    fun getTypeTest() {
        var type = mockContentResolver.getType(MSCalendar.Companion.CONTENT_URI)
        Assert.assertEquals(type, MSCalendar.Companion.CONTENT_TYPE)

        type = mockContentResolver.getType(MSEvent.CONTENT_URI)
        Assert.assertEquals(type, MSEvent.CONTENT_TYPE)

        type = mockContentResolver.getType(MSEvent.CONTENT_URI_DELETED)
        Assert.assertEquals(type, MSEvent.CONTENT_TYPE_DELETED)

        type = mockContentResolver.getType(MSCalendar.Companion.CONTENT_URI.buildUpon().appendPath("10").build())
        Assert.assertEquals(type, MSCalendar.Companion.CONTENT_ITEM_TYPE)

        type = mockContentResolver.getType(MSEvent.CONTENT_URI.buildUpon().appendPath("10").build())
        Assert.assertEquals(type, MSEvent.CONTENT_ITEM_TYPE)
    }

    @Test
    fun insertTest() {
        /** Add new calendar*/
        val contentValueCal = ContentValues()
        contentValueCal.put(COLUMN_CAL_ID, calendarId)
        contentValueCal.put(COLUMN_CAL_JSON, calendarJson)
        val calUri = mockContentResolver.insert(MSCalendar.Companion.CONTENT_URI, contentValueCal)

        // an item added to calendar table, so the returned type is item type
        Assert.assertEquals(MSCalendar.Companion.CONTENT_ITEM_TYPE, mockContentResolver.getType(calUri))

        /** Add new event to existing calendar*/
        val contentValueEvent = ContentValues()
        contentValueEvent.put(COLUMN_CAL_ID, calendarId)
        contentValueEvent.put(COLUMN_EVENT_ID, eventId)
        contentValueEvent.put(DatabaseClient.COLUMN_EVENT_JSON, eventJson)

        val eventUri = mockContentResolver.insert(MSEvent.CONTENT_URI, contentValueEvent)
        Assert.assertEquals(MSEvent.CONTENT_ITEM_TYPE, mockContentResolver.getType(eventUri))
    }

    @Test
    fun getCalendarsListTest() {
        // get all calendars
        var cursor = mockContentResolver.query(MSCalendar.Companion.CONTENT_URI,
                null,
                null,
                null,
                null)

        //check for null cursor
        assert(cursor != null)

        cursor.moveToFirst()
        val calId = cursor.getString(cursor.getColumnIndex(COLUMN_CAL_ID))
        Assert.assertEquals(calId, calendarId)

        val dbCalendarJson = cursor.getString(cursor.getColumnIndex(COLUMN_CAL_JSON))
        val dbCalendar = json?.fromJson(dbCalendarJson, MSCalendar::class.java)
        val originalCalendar = json?.fromJson(calendarJson, MSCalendar::class.java)

        val dbChangeCode = dbCalendar?.changeKey
        val originalChangeCode = originalCalendar?.changeKey
        Assert.assertEquals(dbChangeCode, originalChangeCode)

        cursor.close()
    }

    @Test
    fun getCalendarByIdTest() {
        val originalCalendar = json?.fromJson(calendarJson, MSCalendar::class.java)

        // check for calendar by id
        val cursor = mockContentResolver.query(MSCalendar.Companion.CONTENT_URI,
                null,
                "$COLUMN_CAL_ID=?",
                arrayOf(calendarId),
                null,
                null)

        assert(cursor != null)
        cursor.moveToFirst()
        val dbCalendarJsonById = cursor.getString(cursor.getColumnIndex(COLUMN_CAL_JSON))
        val dbCalendarById = json?.fromJson(dbCalendarJsonById, MSCalendar::class.java)

        val dbChangeCode = dbCalendarById?.changeKey
        val originalChangeCode = originalCalendar?.changeKey
        Assert.assertEquals(dbChangeCode, originalChangeCode)

        cursor.close()
    }

    @Test
    fun getEventsListTest() {
        // get all events
        var cursor = mockContentResolver.query(MSEvent.CONTENT_URI,
                null,
                null,
                null,
                null)

        //check for null cursor
        assert(cursor != null)

        cursor.moveToFirst()
        val calId = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_ID))
        Assert.assertEquals(calId, eventId)

        val dbEventJson = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_JSON))
        val dbEvent = json?.fromJson(dbEventJson, MSEvent::class.java)
        val originalEvent = json?.fromJson(eventJson, MSEvent::class.java)

        val dbChangeCode = dbEvent?.changeKey
        val originalChangeCode = originalEvent?.changeKey
        Assert.assertEquals(dbChangeCode, originalChangeCode)

        cursor.close()
    }

    @Test
    fun getEventByIdTest() {
        val originalEvent = json?.fromJson(eventJson, MSEvent::class.java)

        // check for event by cal id and event id, we can just search by event id
        val cursor = mockContentResolver.query(MSEvent.CONTENT_URI,
                null,
                "$COLUMN_CAL_ID=? AND $COLUMN_EVENT_ID=?",
                arrayOf(calendarId, eventId),
                null,
                null)

        assert(cursor != null)
        cursor.moveToFirst()
        val dbEventJsonById = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_JSON))
        val dbEventById = json?.fromJson(dbEventJsonById, MSEvent::class.java)

        val dbChangeCode = dbEventById?.changeKey
        val originalChangeCode = originalEvent?.changeKey
        Assert.assertEquals(dbChangeCode, originalChangeCode)

        cursor.close()
    }

    @Test
    fun updateCalendarTest() {
        val originalCalendar = json?.fromJson(calendarJson, MSCalendar::class.java)
        originalCalendar?.changeKey = "new_change_key"

        val newCalJson = json?.toJson(originalCalendar, MSCalendar::class.java)

        val contentValueCal = ContentValues()
        contentValueCal.put(COLUMN_CAL_JSON, newCalJson)

        val affectedRows = mockContentResolver.update(MSCalendar.Companion.CONTENT_URI,
                contentValueCal,
                "$COLUMN_CAL_ID=?",
                arrayOf(calendarId))

        Assert.assertEquals(affectedRows, 1)

        // then get the calendar from db and compare the change code
        val cursor = mockContentResolver.query(MSCalendar.Companion.CONTENT_URI,
                null,
                "$COLUMN_CAL_ID=?",
                arrayOf(calendarId),
                null,
                null)

        assert(cursor != null)
        cursor.moveToFirst()
        val dbCalendarJsonById = cursor.getString(cursor.getColumnIndex(COLUMN_CAL_JSON))
        val dbCalendarById = json?.fromJson(dbCalendarJsonById, MSCalendar::class.java)

        val dbChangeCode = dbCalendarById?.changeKey
        Assert.assertEquals(dbChangeCode, "new_change_key")

        cursor.close()
    }

    @Test
    fun UpdateEventTest() {
        val originalEvent = json?.fromJson(eventJson, MSEvent::class.java)
        originalEvent?.changeKey = "new_change_key"

        val newEventJson = json?.toJson(originalEvent, MSEvent::class.java)

        val contentValueEvent = ContentValues()
        contentValueEvent.put(COLUMN_EVENT_JSON, newEventJson)

        val affectedRows = mockContentResolver.update(MSEvent.CONTENT_URI,
                contentValueEvent,
                "$COLUMN_CAL_ID=? AND $COLUMN_EVENT_ID=?",
                arrayOf(calendarId, eventId))

        Assert.assertEquals(affectedRows, 1)

        // then get the event from db and compare the change code
        val cursor = mockContentResolver.query(MSEvent.CONTENT_URI,
                null,
                "$COLUMN_CAL_ID=? AND $COLUMN_EVENT_ID=?",
                arrayOf(calendarId, eventId),
                null,
                null)

        assert(cursor != null)
        cursor.moveToFirst()
        val dbEventJsonById = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_JSON))
        val dbEventById = json?.fromJson(dbEventJsonById, MSEvent::class.java)

        val dbChangeCode = dbEventById?.changeKey
        Assert.assertEquals(dbChangeCode, "new_change_key")

        cursor.close()
    }

    @Test
    fun deleteCalendarTest() {
        val affectedRows = mockContentResolver.delete(MSCalendar.Companion.CONTENT_URI,
                "$COLUMN_CAL_ID=?",
                arrayOf(calendarId))

        Assert.assertEquals(affectedRows, 1)
    }

    @Test
    fun deleteEventTest() {
        // when deleting an event, the event must go to the deleted table, the delete from event table
        val originalEvent = json?.fromJson(eventJson, MSEvent::class.java)

        val affectedRows = mockContentResolver.delete(MSEvent.CONTENT_URI,
                "$COLUMN_CAL_ID=? AND $COLUMN_EVENT_ID=?",
                arrayOf(calendarId, eventId))

        Assert.assertEquals(affectedRows, 1)

        // get the event from deleted table
        val cursor = mockContentResolver.query(MSEvent.CONTENT_URI_DELETED,
                null,
                "$COLUMN_CAL_ID=? AND $COLUMN_EVENT_ID=?",
                arrayOf(calendarId, eventId),
                null,
                null)

        assert(cursor != null && cursor.count > 0)
        cursor.moveToFirst()
        val dbEventJsonById = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_JSON))
        val dbEventById = json?.fromJson(dbEventJsonById, MSEvent::class.java)

        val originalChangeKey = originalEvent?.changeKey
        val dbChangeKey = dbEventById?.changeKey
        Assert.assertEquals(originalChangeKey, dbChangeKey)

        cursor.close()
    }
}
