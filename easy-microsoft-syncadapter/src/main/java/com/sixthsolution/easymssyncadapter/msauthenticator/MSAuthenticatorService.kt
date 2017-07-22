package com.sixthsolution.easymssyncadapter.msauthenticator

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/12/2017.
 */
class MSAuthenticatorService : Service() {
    override fun onBind(intent: Intent?): IBinder {
        val authenticator = MSAuthenticator(this)
        return authenticator.iBinder
    }
}