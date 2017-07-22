package com.sixthsolution.easymssyncadapter.broadcasts

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 7/16/2017.
 */
enum class SyncBroadcastStatus(val status: String) {
    SyncError("com.sixthsolution.easymssyncadapter.ms.SyncError"),
    SyncCanceled("com.sixthsolution.easymssyncadapter.ms.SyncCanceled"),
    SyncFinished("com.sixthsolution.easymssyncadapter.ms.SyncFinished"),
    SyncLocalAndRemoteCalendars("com.sixthsolution.easymssyncadapter.ms.SyncLocalAndRemoteCalendars"),
    SyncEventsOfCalendar("com.sixthsolution.easymssyncadapter.ms.SyncEventsOfCalendar"),
    StartTokenRefresh("com.sixthsolution.easymssyncadapter.ms.StartTokenRefresh"),
    EndTokenRefresh("com.sixthsolution.easymssyncadapter.ms.EndTokenRefresh"),
    StartAddEvent_LocalDatabase("com.sixthsolution.easymssyncadapter.ms.StartAddEvent_LocalDatabase"),
    EndAddEvent_LocalDatabase("com.sixthsolution.easymssyncadapter.ms.EndAddEvent_LocalDatabase"),
    StartAddEvent_RemoteServer("com.sixthsolution.easymssyncadapter.ms.StartAddEvent_RemoteServer"),
    EndAddEvent_RemoteServer("com.sixthsolution.easymssyncadapter.ms.EndAddEvent_RemoteServer"),
    StartUpdateEvent_LocalDatabase("com.sixthsolution.easymssyncadapter.ms.StartUpdateEvent_LocalDatabase"),
    EndUpdateEvent_LocalDatabase("com.sixthsolution.easymssyncadapter.ms.EndUpdateEvent_LocalDatabase"),
    StartUpdateEvent_RemoteServer("com.sixthsolution.easymssyncadapter.ms.StartUpdateEvent_RemoteServer"),
    EndUpdateEvent_RemoteServer("com.sixthsolution.easymssyncadapter.ms.EndUpdateEvent_RemoteServer"),
    StartDeleteEvent_LocalDatabase("com.sixthsolution.easymssyncadapter.ms.StartDeleteEvent_LocalDatabase"),
    EndDeleteEvent_LocalDatabase("com.sixthsolution.easymssyncadapter.ms.EndDeleteEvent_LocalDatabase"),
    StartDeleteEvent_RemoteServer("com.sixthsolution.easymssyncadapter.ms.StartDeleteEvent_RemoteServer"),
    EndDeleteEvent_RemoteServer("com.sixthsolution.easymssyncadapter.ms.EndDeleteEvent_RemoteServer")
}