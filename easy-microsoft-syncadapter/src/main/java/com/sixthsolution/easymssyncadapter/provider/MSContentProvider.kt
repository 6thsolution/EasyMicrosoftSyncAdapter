package com.sixthsolution.easymssyncadapter.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.sixthsolution.easymssyncadapter.GlobalConstant
import com.sixthsolution.easymssyncadapter.database.DatabaseClient
import com.sixthsolution.easymssyncadapter.database.DatabaseClient.Companion.COLUMN_CAL_ID
import com.sixthsolution.easymssyncadapter.database.DatabaseClient.Companion.COLUMN_EVENT_ID
import com.sixthsolution.easymssyncadapter.database.DatabaseClient.Companion.COLUMN_EVENT_JSON
import com.sixthsolution.easymssyncadapter.database.DatabaseClient.Companion.COLUMN_ID
import com.sixthsolution.easymssyncadapter.database.DatabaseClient.Companion.TABLE_CALENDARS
import com.sixthsolution.easymssyncadapter.database.DatabaseClient.Companion.TABLE_DELETED_EVENTS
import com.sixthsolution.easymssyncadapter.database.DatabaseClient.Companion.TABLE_EVENTS
import com.sixthsolution.easymssyncadapter.mssyncadapter.models.MSEvent
import com.sixthsolution.mssyncadapter.models.MSCalendar

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/17/2017.
 */
class MSContentProvider : ContentProvider() {
    companion object {
        private val TYPE_CALENDAR = 1
        private val TYPE_CALENDAR_ID = 2
        private val TYPE_EVENT = 3
        private val TYPE_EVENT_ID = 4
        private val TYPE_DELETED_EVENTS = 5

        private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        // init the uri matcher to find the specific type based on uri
        init {
            uriMatcher.addURI(GlobalConstant.CONTENT_AUTHORITY, GlobalConstant.PATH_CALENDARS, TYPE_CALENDAR)
            uriMatcher.addURI(GlobalConstant.CONTENT_AUTHORITY, GlobalConstant.PATH_CALENDARS + "/#", TYPE_CALENDAR_ID)
            uriMatcher.addURI(GlobalConstant.CONTENT_AUTHORITY, GlobalConstant.PATH_EVENTS, TYPE_EVENT)
            uriMatcher.addURI(GlobalConstant.CONTENT_AUTHORITY, GlobalConstant.PATH_EVENTS + "/#", TYPE_EVENT_ID)
            uriMatcher.addURI(GlobalConstant.CONTENT_AUTHORITY, GlobalConstant.PATH_DELETED_EVENTS, TYPE_DELETED_EVENTS)
        }
    }

    override fun onCreate(): Boolean {
        DatabaseClient.getInstance(context)
        return true
    }

    /**
     * Get the content type based on request Uri or throw Exception if we dont handle that kind of uri
     */
    @Throws(IllegalArgumentException::class)
    override fun getType(uri: Uri?): String {
        when (uriMatcher.match(uri)) {
            TYPE_CALENDAR -> return MSCalendar.Companion.CONTENT_TYPE
            TYPE_CALENDAR_ID -> return MSCalendar.Companion.CONTENT_ITEM_TYPE
            TYPE_EVENT -> return MSEvent.Companion.CONTENT_TYPE
            TYPE_EVENT_ID -> return MSEvent.Companion.CONTENT_ITEM_TYPE
            TYPE_DELETED_EVENTS -> return MSEvent.Companion.CONTENT_TYPE_DELETED
            else -> throw IllegalArgumentException("Invalid URI!")
        }
    }

    /**
     * Compare thr input uri with our type and insert the values into right table then create a content uri and
     * return it
     */
    @Throws(IllegalArgumentException::class)
    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        val returnUri: Uri
        val _id: Long
        when (uriMatcher.match(uri)) {
            TYPE_CALENDAR -> {
                _id = DatabaseClient.getInstance(context).insert(TABLE_CALENDARS, null, values)
                returnUri = ContentUris.withAppendedId(MSCalendar.Companion.CONTENT_URI, _id)
            }
            TYPE_EVENT -> {
                _id = DatabaseClient.getInstance(context).insert(TABLE_EVENTS, null, values)
                returnUri = ContentUris.withAppendedId(MSEvent.Companion.CONTENT_URI, _id)
            }
            TYPE_DELETED_EVENTS -> {
                _id = DatabaseClient.getInstance(context).insert(TABLE_DELETED_EVENTS, null, values)
                returnUri = ContentUris.withAppendedId(MSEvent.Companion.CONTENT_URI_DELETED, _id)
            }
            else -> {
                throw IllegalArgumentException("Invalid Uri")
            }
        }

        // Notify any observers to update the UI
        assert(context != null)
        context.contentResolver.notifyChange(returnUri, null)
        return returnUri
    }

    @Throws(IllegalArgumentException::class)
    override fun query(uri: Uri?, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor {
        var cursor: Cursor
        when (uriMatcher.match(uri)) {
        // selecting all calendars
            TYPE_CALENDAR -> cursor = DatabaseClient.getInstance(context).query(TABLE_CALENDARS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder)
            TYPE_CALENDAR_ID -> {
                val _id = ContentUris.parseId(uri)
                cursor = DatabaseClient.getInstance(context).query(TABLE_CALENDARS,
                        projection,
                        COLUMN_ID + "=?",
                        arrayOf(_id.toString()),
                        null, null,
                        sortOrder)
            }
            TYPE_EVENT -> cursor = DatabaseClient.getInstance(context).query(TABLE_EVENTS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder)
            TYPE_EVENT_ID -> {
                val _id = ContentUris.parseId(uri)
                cursor = DatabaseClient.getInstance(context).query(TABLE_EVENTS,
                        projection,
                        COLUMN_ID + "=?",
                        arrayOf(_id.toString()),
                        null, null,
                        sortOrder)
            }
            TYPE_DELETED_EVENTS -> cursor = DatabaseClient.getInstance(context).query(TABLE_DELETED_EVENTS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder)
            else -> throw IllegalArgumentException("Invalid Uri")
        }
        // Tell the cursor to register a content observer to observe changes to the URI or its descendants.
        assert(context != null)
        cursor.setNotificationUri(context.contentResolver, uri)
        return cursor
    }

    @Throws(IllegalArgumentException::class)
    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val rows: Int
        when (uriMatcher.match(uri)) {
            TYPE_CALENDAR -> rows = DatabaseClient.getInstance(context).update(TABLE_CALENDARS,
                    values,
                    selection,
                    selectionArgs)
            TYPE_EVENT -> rows = DatabaseClient.getInstance(context).update(TABLE_EVENTS,
                    values,
                    selection,
                    selectionArgs)
            TYPE_DELETED_EVENTS -> rows = DatabaseClient.getInstance(context).update(TABLE_DELETED_EVENTS,
                    values,
                    selection,
                    selectionArgs)

            else -> throw IllegalArgumentException("Invalid Uri")
        }

        // Notify any observers to update the UI
        if (rows != 0) {
            assert(context != null)
            context.contentResolver.notifyChange(uri, null)
        }

        return rows
    }

    @Throws(IllegalArgumentException::class)
    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<String>?): Int {
        val rows: Int
        when (uriMatcher.match(uri)) {
            TYPE_CALENDAR -> rows = DatabaseClient.getInstance(context).delete(TABLE_CALENDARS,
                    selection,
                    selectionArgs)
            TYPE_EVENT -> {
                putEventsIntoDeletedTable(uri, selection, selectionArgs)
                rows = DatabaseClient.getInstance(context).delete(TABLE_EVENTS,
                        selection,
                        selectionArgs)
            }
            TYPE_DELETED_EVENTS -> rows = DatabaseClient.getInstance(context).delete(TABLE_DELETED_EVENTS,
                    selection,
                    selectionArgs)
            else -> throw IllegalArgumentException("Invalid Uri")
        }

        // Notify any observers to update the UI
        if (rows != 0) {
            assert(context != null)
            context.contentResolver.notifyChange(uri, null)
        }

        return rows
    }

    /**
     * search for any events in data base and add them to the deleted events table
     */
    private fun putEventsIntoDeletedTable(uri: Uri?, selection: String?, selectionArgs: Array<String>?) {
        val cursor = query(uri, null, selection, selectionArgs, null)

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val contentValue = ContentValues()
            contentValue.put(COLUMN_CAL_ID, cursor.getString(cursor.getColumnIndex(COLUMN_CAL_ID)))
            contentValue.put(COLUMN_EVENT_ID, cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_ID)))
            contentValue.put(COLUMN_EVENT_JSON, cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_JSON)))
            insert(MSEvent.Companion.CONTENT_URI_DELETED, contentValue)

            cursor.moveToNext()
        }

        cursor.close()
    }
}