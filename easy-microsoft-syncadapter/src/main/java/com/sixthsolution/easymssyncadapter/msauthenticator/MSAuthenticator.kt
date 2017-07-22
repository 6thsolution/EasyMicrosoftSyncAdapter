package com.sixthsolution.easymssyncadapter.msauthenticator

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.AccountManager.KEY_BOOLEAN_RESULT
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.sixthsolution.easymssyncadapter.GlobalConstant
import java.util.*


/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/12/2017.
 */
class MSAuthenticator(ctx: Context) : AbstractAccountAuthenticator(ctx) {
    private val context: Context = ctx

    override fun confirmCredentials(response: AccountAuthenticatorResponse?, account: Account?, options: Bundle?): Bundle? {
        return null
    }

    override fun updateCredentials(response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?): Bundle? {
        return null
    }

    override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle? {
        return null
    }

    override fun addAccount(response: AccountAuthenticatorResponse?, accountType: String?, authTokenType: String?, requiredFeatures: Array<out String>?, options: Bundle?): Bundle {
        val intent = Intent(context, LoginActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authTokenType)
        intent.putExtra(GlobalConstant.IS_ADDING_NEW_ACCOUNT, true) // this might cause problem, because microsoft login with one account
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        return "Microsoft Calendar access"
    }

    override fun getAuthToken(response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?): Bundle {
        val am = AccountManager.get(context)
        val authToken = am.peekAuthToken(account, authTokenType) // Token is encrypted and saved
        var tokenStatus = TokenStatus.None
        var exDate = "does NOT exist"

        if (!TextUtils.isEmpty(authToken)) {
            val expireDate = am.getUserData(account, GlobalConstant.EXPIRE_TOKEN_DATE)
            if (TextUtils.isEmpty(expireDate)) {
                val expireTokenTime = expireDate.toLong()
                val exp = Date(expireTokenTime)
                exDate = exp.toString()

                if (exp.after(Date())) {
                    // token expired, login silently
                    tokenStatus = TokenStatus.NeedRefresh
                    val loginHandler: IMSLoginHandler = LocalMSLoginHandler(context.applicationContext as Application)
                    loginHandler.loginSilentBlocking()
                } else {
                    tokenStatus = TokenStatus.ValidToken
                }
            }
        }

        if (tokenStatus != TokenStatus.None) {
            // there is valid or refreshed token
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            result.putString(GlobalConstant.EXPIRE_TOKEN_DATE, exDate)
            return result
        }

        // no token and no refresh token, show login screen
        val intent = Intent(context, LoginActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        intent.putExtra(GlobalConstant.ACCOUNT_TYPE, account?.type)
        intent.putExtra(GlobalConstant.ACCOUNT_NAME, account?.name)
        intent.putExtra(GlobalConstant.IS_ADDING_NEW_ACCOUNT, false)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle

    }

    override fun hasFeatures(response: AccountAuthenticatorResponse?, account: Account?, features: Array<out String>?): Bundle {
        val bundle = Bundle()
        bundle.putBoolean(KEY_BOOLEAN_RESULT, false)
        return bundle
    }
}