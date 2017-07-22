package com.sixthsolution.easymssyncadapter.msauthenticator

import android.net.Uri
import com.microsoft.services.msa.OAuthConfig

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/12/2017.
 *
 * The configuration for an Microsoft OAuth2 v2.0 Endpoint
 */
class MicrosoftOAuth2Endpoint : OAuthConfig {
    companion object {
        //The current instance of this class
        private val sInstance = MicrosoftOAuth2Endpoint()

        /**
         * The current instance of this class
         * @return The instance
         */
        fun getInstance(): MicrosoftOAuth2Endpoint {
            return sInstance
        }
    }

    override fun getAuthorizeUri(): Uri {
        return Uri.parse("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
    }

    override fun getLogoutUri(): Uri {
        return Uri.parse("https://login.microsoftonline.com/common/oauth2/v2.0/logout")
    }

    override fun getDesktopUri(): Uri {
        return Uri.parse("urn:ietf:wg:oauth:2.0:oob")
    }

    override fun getTokenUri(): Uri {
        return Uri.parse("https://login.microsoftonline.com/common/oauth2/v2.0/token")
    }
}