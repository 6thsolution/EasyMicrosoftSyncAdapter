package com.sixthsolution.easymicrosoftsyncadapterapp

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 7/13/2017.
 */
class CalendarData {
    companion object {
        val calendarJson = """{
            "changeKey" : "B144pfTFfUe5qmoVh4jcMgAAAAA9IA==",
            "color" : "auto",
            "name" : "Calendar",
            "id" : "AQMkADAwATM0MDAAMS1iNjYBLTM0NABiLTAwAi0wMAoARgAAA0hxsVr2z35Lmv5x4AJQMGgHAAdeOKX0xX1HuapqFYeI3DIAAAIBBgAAAAdeOKX0xX1HuapqFYeI3DIAAAI4HAAAAA==",
            "@odata.type" : "microsoft.graph.calendar"
            }"""

        val eventJson = """{
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
    }
}