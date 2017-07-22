package com.sixthsolution.easymssyncadapter.utils

import android.content.ContentResolver
import com.google.gson.Gson
import com.sixthsolution.easymssyncadapter.database.DatabaseClient
import com.sixthsolution.easymssyncadapter.mssyncadapter.models.MSEvent
import com.sixthsolution.mssyncadapter.models.MSCalendar

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 7/15/2017.
 */
class ContentProviderUtil {
    companion object {
        fun getListOfLocalCalendar(contentResolver: ContentResolver, gson: Gson): List<MSCalendar> {
            val localCalendar = ArrayList<MSCalendar>()

            val cursor = contentResolver.query(MSCalendar.CONTENT_URI,
                    null,
                    null,
                    null,
                    null,
                    null)

            if (cursor != null) {
                cursor.moveToFirst()

                val calendarColumnIndex = cursor.getColumnIndex(DatabaseClient.COLUMN_CAL_JSON)

                while (!cursor.isAfterLast) {
                    localCalendar.add(gson.fromJson(cursor.getString(calendarColumnIndex), MSCalendar::class.java))

                    cursor.moveToNext()
                }
                cursor.close()
            }

            return localCalendar
        }

        fun getMapOfLocalCalendar(contentResolver: ContentResolver, gson: Gson): Map<String, MSCalendar> {
            val calendars = getListOfLocalCalendar(contentResolver, gson)
            val result = HashMap<String, MSCalendar>(calendars.size)
            for (calendar in calendars) {
                result.put(calendar.id, calendar)
            }

            return result
        }

        fun getListOfDeletedEventsId(contentResolver: ContentResolver, gson: Gson): List<String> {
            val cursor = contentResolver.query(MSEvent.CONTENT_URI_DELETED,
                    null,
                    null,
                    null,
                    null,
                    null)

            val deletedEvents: ArrayList<String> = ArrayList()

            // Get list of deleted events from database
            if (cursor != null) {
                cursor.moveToFirst()

                val deletedEventColumnId = cursor.getColumnIndex(DatabaseClient.COLUMN_EVENT_ID)

                while (!cursor.isAfterLast) {
                    val event = gson.fromJson(
                            cursor.getString(deletedEventColumnId),
                            String::class.java)

                    deletedEvents.add(event)

                    cursor.moveToNext()
                }
                cursor.close()
            }

            return deletedEvents
        }

        fun getListOfLocalEvents(contentResolver: ContentResolver, gson: Gson, calendarId: String): List<MSEvent> {
            val localEvents = ArrayList<MSEvent>()

            val cursor = contentResolver.query(MSEvent.CONTENT_URI,
                    null,
                    "${DatabaseClient.COLUMN_CAL_ID}=?",
                    arrayOf(calendarId),
                    null,
                    null)

            if (cursor != null) {
                cursor.moveToFirst()

                val eventColumnIndex = cursor.getColumnIndex(DatabaseClient.COLUMN_EVENT_JSON)

                while (!cursor.isAfterLast) {
                    localEvents.add(gson.fromJson(cursor.getString(eventColumnIndex), MSEvent::class.java))

                    cursor.moveToNext()
                }
                cursor.close()
            }

            return localEvents
        }

        fun getMapOfLocalEvents(contentResolver: ContentResolver, gson: Gson, calendarId: String):
                Map<String, MSEvent> {
            val events = getListOfLocalEvents(contentResolver, gson, calendarId)
            val result = HashMap<String, MSEvent>(events.size)
            for (event in events) {
                result.put(event.id, event)
            }
            return result
        }

        fun getLocalEvent(contentResolver: ContentResolver, gson: Gson, eventColumnId: String): MSEvent? {
            val cursor = contentResolver.query(MSEvent.CONTENT_URI,
                    null,
                    "${DatabaseClient.COLUMN_ID}=?",
                    arrayOf(eventColumnId),
                    null,
                    null)

            if (cursor != null) {
                cursor.moveToFirst()
                val event = gson.fromJson(cursor.getString(cursor.getColumnIndex(DatabaseClient.COLUMN_EVENT_JSON)),
                        MSEvent::class.java)
                cursor.close()
                return event
            }

            return null
        }
    }
}