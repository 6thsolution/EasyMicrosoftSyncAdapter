package com.sixthsolution.mssyncadapter.models

import com.microsoft.graph.extensions.Calendar
import com.sixthsolution.easymssyncadapter.GlobalConstant.Companion.BASE_CONTENT_URI
import com.sixthsolution.easymssyncadapter.GlobalConstant.Companion.PATH_CALENDARS

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/17/2017.
 */
class MSCalendar : Calendar() {
    companion object {
        // ContentProvider information for MSCalendar
        val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CALENDARS).build()
        val CONTENT_TYPE = "vnd.android.cursor.dir/$CONTENT_URI"
        val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/$CONTENT_URI"
    }
}