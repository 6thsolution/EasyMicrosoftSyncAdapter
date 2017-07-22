Easy Microsoft Sync Adapter is an Android Library based on Microsoft Graph for syncing with all Microsoft based calendars.

Performing authentication and full duplex sync with **All of Microsoft calendars**.

## Features
* Supports Outlook, Office365, Exchange Server.
* Easy to use and implement.
* Fast and simple syncing.
* Use android builtin Authenticator.
* Simple Interface to communicate with.
* completely writen in kotlin (The future of android :)

## Installation
1) Configure your top-level `build.gradle` to include our repository
```groovy
allprojects {
    repositories {
        jcenter()
        maven { url "http://dl.bintray.com/6thsolution/public-maven" }
    }
}
```
Then config your app-level `build.gradle` to include the library as dependency:
``` groovy
compile 'com.sixthsolution:easymssyncadapter:1.0.0-beta1'
```

2) Config
Add *microsoft_client_id* metadata to your application tag in manifest
like this:
```xml
<meta-data
    android:name="microsoft_client_id"
    android:value="YOUR_MICROSOFT_CLIENT_ID"
    />
```

## Usage
* For complete example refer to `app` module and `MSContentProviderTest`

* **Add new account**:
Note that by adding new account any previous account will be terminated,
because microsoft graph library doesn't support multi login at this time.

For adding new account you must call `AccountManager#addAccount` .
it automatically open `LoginActivity` and handle add progress by itself.


```java
accountManager?.addAccount(authType, authType, null, null, this, { future ->
            try {
                val bnd: Bundle? = future?.result
                updateStatus(String.format(Locale.US, "New account added\n\n%s", bnd.toString()))
            } catch (ex: Exception) {
                updateStatus(ex.message)
            }
        }, null)
```
* **Login handling**:

The token will be handled by `microsoft graph` library.
before any request you must check for valid token. for this you can use
`IMSLoginHandler` interface, some of it's methods listed below

 1) **logout(callback: ICallback<Void>)** : logout from existing account
 and call the callback
 ```java
 loginHandler.logout(object : ICallback<Void> {
             override fun failure(ex: ClientException?) {
                 // TODO
             }

             override fun success(p0: Void?) {
                 // TODO
             }
         })
```

2) **login(activity: Activity, callback: ICallback<Void>)**: Use this
method to login with microsoft account. you can use any method that
connected to `Microsoft Graph`. The activity parameter is just for
 running something in `ui thread` and we don't keep any hard reference
 of it.

 *Note*: There is an internal login activity that after a successful
  login will show a dialog that you can enter a display name for your
  account. it contain a title, an EditText and a Button. if you want to
  change any of their text you can add this strings to your resource,
  with your value.

  ```xml
  <string name="display_name_title">Enter a Display name</string>
  <string name="display_name_hint">display name</string>
  <string name="display_name_ok">Ok</string>
  ```

 3) **loginSilent(callback: ICallback<Void>)**: If logged in before you
 can use this method to login silently (actually checking or getting new
 token) without showing any ui. **You Must call this method before
 using any of the library method like adding or deleting event
 or ...** . This method use a callback to inform you of login result.

 4) **loginSilentBlocking(): Void**: In some case you don't want use a
 callback for silent login. in this situation you can use this method
 and wait for return value.

* **Calendar operation handling**

For adding event, updating, deleting ... you can use
 `IMSCalendarManager` interface. this interface handle both operation
 on local database (via `ContentProvider`) and remote operation (via
 `SyncAdapter`). some of it's method and their description are listed
 below:

 1) **addNewEvent(account: Account, calendarId: String, event: MSEvent)**:
 For adding any new event use this method. the `account` is the logged in
 microsoft account, The `calendarId` is the id of calendar that event
 must be added to it. and finally `event` is the new event.
 This method first add the event to local database then try to add it to
 remote server.

 2) **getListOfAvailableCalendars(): List<MSCalendar>**: This method
 return the list of calendar from local database.

 3) **syncLocalEventsWithRemoteEvents(account: Account)**: Preforming a
  full sync between remote and local events. This will force the
  `SyncAdapter` to perform the sync operation right now.

 4) **getListOfAvailableEvents(calendarId: String): List<MSEvent>**:
 This Method return the list of all local event associated with specific
 `calendarId`

 5) **updateExistingEvent(account: Account, calendarId: String, eventId: String, event: MSEvent)**:
 This method first update the event in local database then try to update
 it with remote server. The method's parameters is self explanatory :)

 6) **deleteExistingEvent(account: Account, calendarId: String, eventId: String)**
 This method first delete the event from local database then try to delete
 it from remote server. The method's parameters is self explanatory :)

 * **Listen for sync status**

 There is an abstract class called `SyncReceiver` which is a `BroadcastReceiver`.
 You can create an instance of it and override
 `onSyncStatusChanged(syncStatus: SyncBroadcastStatus, bundle: Bundle?)`
 to get notified of any progress changes. The `SyncBroadcastStatus` is
 an enum with self explanatory members as follow:
 ```java
     SyncError
     SyncCanceled
     SyncFinished
     SyncLocalAndRemoteCalendars
     SyncEventsOfCalendar
     StartTokenRefresh
     EndTokenRefresh
     StartAddEvent_LocalDatabase
     EndAddEvent_LocalDatabase
     StartAddEvent_RemoteServer
     EndAddEvent_RemoteServer
     StartUpdateEvent_LocalDatabase
     EndUpdateEvent_LocalDatabase
     StartUpdateEvent_RemoteServer
     EndUpdateEvent_RemoteServer
     StartDeleteEvent_LocalDatabase
     EndDeleteEvent_LocalDatabase
     StartDeleteEvent_RemoteServer
     EndDeleteEvent_RemoteServer
 ```

* **Extra operation**
1) **Get list of available accounts**
You can use this code to get list of available accounts for microsoft.
as i said before microsoft graph library won't support multi login so
the list of accounts contain just one account:

```java
val authType = getString(R.string.auth_token_type_full_access)
val accounts: Array<Account>? = accountManager?.getAccountsByType(authType)
```

## License
```
Copyright 2016-2017 6thSolution Technologies Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```