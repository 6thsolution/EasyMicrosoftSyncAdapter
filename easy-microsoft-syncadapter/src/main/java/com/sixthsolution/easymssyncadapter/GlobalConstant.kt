package com.sixthsolution.easymssyncadapter

import android.net.Uri

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/12/2017.
 */
class GlobalConstant {
    companion object {
        val ACCOUNT_TYPE = "ACCOUNT_TYPE"
        val IS_ADDING_NEW_ACCOUNT = "IS_ADDING_NEW_ACCOUNT"
        val ACCOUNT_NAME = "ACCOUNT_NAME"
        val SIGNIN_ERROR = "SIGNIN_ERROR"
        val EXPIRE_TOKEN_DATE = "EXPIRE_TOKEN_DATE"
        val TOKEN_STATUS = "TOKEN_STATUS"

        val CONTENT_AUTHORITY = "com.sixthsolution.mssyncadapter.content_provider"
        val BASE_CONTENT_URI = Uri.parse("content://${CONTENT_AUTHORITY}")
        val PATH_CALENDARS = "calendars"
        val PATH_EVENTS = "events"
        val PATH_DELETED_EVENTS = "deleted"

        val SYNC_ADAPTER_STATE = "SYNC_ADAPTER_STATE"
        val SYNC_ADAPTER_CAL_ID = "SYNC_ADAPTER_CAL_ID"
        val SYNC_ADAPTER_EVENT_ID = "SYNC_ADAPTER_EVENT_ID"

        val EXTRA_SYNC_ADAPTER_CAL_ID = "EXTRA_SYNC_ADAPTER_CAL_ID"
    }
}