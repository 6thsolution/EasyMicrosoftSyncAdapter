package com.sixthsolution.easymssyncadapter.mssyncadapter.models

import com.microsoft.graph.extensions.Event
import com.sixthsolution.easymssyncadapter.GlobalConstant.Companion.BASE_CONTENT_URI
import com.sixthsolution.easymssyncadapter.GlobalConstant.Companion.PATH_DELETED_EVENTS
import com.sixthsolution.easymssyncadapter.GlobalConstant.Companion.PATH_EVENTS

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/17/2017.
 */
class MSEvent : Event() {
    companion object {
        // ContentProvider information for MSEvent
        val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build()
        val CONTENT_TYPE = "vnd.android.cursor.dir/${CONTENT_URI}"
        val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/${CONTENT_URI}"

        val CONTENT_URI_DELETED = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DELETED_EVENTS).build()
        val CONTENT_TYPE_DELETED = "vnd.android.cursor.dir/${CONTENT_URI_DELETED}"
    }
}