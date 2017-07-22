package com.sixthsolution.easymssyncadapter.msauthenticator

import android.app.Application


/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/13/2017.
 */
class LocalMSLoginHandler(application: Application) : MSLoginHandler(application) {
    override fun getScopes(): Array<String> {
        return arrayOf("https://graph.microsoft.com/Calendars.ReadWrite",
                "https://graph.microsoft.com/User.ReadBasic.All",
                "offline_access",
                "openid")
    }
}