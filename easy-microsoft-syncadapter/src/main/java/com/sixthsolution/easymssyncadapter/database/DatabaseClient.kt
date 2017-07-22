package com.sixthsolution.easymssyncadapter.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/17/2017.
 */
class DatabaseClient private constructor(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private var db: SQLiteDatabase

    companion object {
        val DB_NAME: String = "ms_calendar_db"
        val DB_VERSION = 1

        val TABLE_CALENDARS = "calendars"
        val TABLE_EVENTS = "events"
        val TABLE_DELETED_EVENTS = "deleted_events"

        val COLUMN_ID = "_id"
        val COLUMN_CAL_ID = "cal_id"
        val COLUMN_CAL_JSON = "cal_json"
        val COLUMN_EVENT_ID = "event_id"
        val COLUMN_EVENT_JSON = "event_json"

        private var instance: DatabaseClient? = null

        fun getInstance(context: Context): DatabaseClient {
            if (instance == null) {
                instance = DatabaseClient(context)
            }

            return instance!!
        }
    }

    init {
        db = writableDatabase
    }

    override fun onCreate(db: SQLiteDatabase?) {
        createCalendarTable(db)
        createEventTable(db)
        createDeletedEventTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //TODO 6/17/2017 do something
    }

    private fun createCalendarTable(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS ${TABLE_CALENDARS} (${COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${COLUMN_CAL_ID} TEXT NOT NULL UNIQUE, ${COLUMN_CAL_JSON} TEXT);")
    }

    private fun createEventTable(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS ${TABLE_EVENTS} (${COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${COLUMN_CAL_ID} TEXT, ${COLUMN_EVENT_ID} TEXT NOT NULL UNIQUE, ${COLUMN_EVENT_JSON} TEXT);")
    }

    private fun createDeletedEventTable(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS ${TABLE_DELETED_EVENTS} (${COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${COLUMN_CAL_ID} TEXT, ${COLUMN_EVENT_ID} TEXT NOT NULL UNIQUE, ${COLUMN_EVENT_JSON} TEXT);")
    }

    /**
     * Search in database
     */
    fun query(table: String, columns: Array<String>?, selection: String?,
              selectionArgs: Array<String>?, groupBy: String?, having: String?,
              orderBy: String?): Cursor {
        return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
    }

    /**
     * Add new value to database
     */
    fun insert(table: String, nullColumnHack: String?, values: ContentValues?): Long {
        return db.insert(table, nullColumnHack, values)
    }

    /**
     * update existing field in database
     */
    fun update(table: String, values: ContentValues?, whereClause: String?, whereArgs: Array<String>?): Int {
        return db.update(table, values, whereClause, whereArgs)
    }

    /**
     * delete existing field from database
     */
    fun delete(table: String, whereClause: String?, whereArgs: Array<String>?): Int {
        return db.delete(table, whereClause, whereArgs)
    }
}