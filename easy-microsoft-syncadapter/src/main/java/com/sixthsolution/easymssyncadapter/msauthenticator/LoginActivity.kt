package com.sixthsolution.easymssyncadapter.msauthenticator

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.sixthsolution.easymssyncadapter.GlobalConstant
import com.sixthsolution.easymssyncadapter.R

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/12/2017.
 */
class LoginActivity : AccountAuthenticatorActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        addNewAccount()
    }

    private fun addNewAccount() {
        // Microsoft Api just support one account at time, so we need to remove any other account and add new one
        // TODO 6/13/2017   show an alert to user so he knows that previous account will be removed

        var authType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        if (authType == null) {
            authType = getString(R.string.auth_token_type_full_access)
        }

        val accountManager = AccountManager.get(applicationContext)
        val accounts = accountManager.getAccountsByType(authType)
        for (account in accounts) {
            removeAccount(accountManager, account)
        }

        showMicrosoftLoginPage(authType)
    }

    /**
     * Remove existing account
     */
    @Suppress("DEPRECATION")
    private fun removeAccount(accountManager: AccountManager, account: Account) {
        val loginHandler: IMSLoginHandler = LocalMSLoginHandler(application)
        loginHandler.logout(object : ICallback<Void> {
            override fun failure(ex: ClientException?) {
                ex?.printStackTrace()
            }

            override fun success(p0: Void?) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
                    accountManager.removeAccountExplicitly(account)
                } else {
                    accountManager.removeAccount(account, null, null)
                }
            }
        })
    }

    /**
     * Trigger the microsoft login page so user can login with his account
     */
    private fun showMicrosoftLoginPage(authType: String) {
        val authenticator: IMSLoginHandler = LocalMSLoginHandler(application)
        authenticator.login(this, object : ICallback<Void> {
            override fun success(aVoid: Void?) {
                runOnUiThread({ askForUserName(authenticator, authType) })
            }

            override fun failure(ex: ClientException?) {
                val bundle = Bundle()
                bundle.putSerializable(GlobalConstant.SIGNIN_ERROR, ex)
                endActivity(bundle)
            }
        })
    }

    private fun askForUserName(authenticator: IMSLoginHandler, authType: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.user_name_layout)
        val name: EditText = dialog.findViewById(R.id.user_name) as EditText
        val ok = dialog.findViewById(R.id.ok)
        ok.setOnClickListener({
            dialog.dismiss()
            registerNewAccount(authenticator, name.text.toString(), authType)
        })

        dialog.show()
    }

    private fun registerNewAccount(authenticator: IMSLoginHandler,
                                   userName: String,
                                   authType: String) {
        val bundle = Bundle()
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, userName)
        val token = "token"  // There is no direct use for token (??), so i just save a fake token
        bundle.putString(AccountManager.KEY_AUTHTOKEN, token)
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, authType)

        bundle.putString(GlobalConstant.EXPIRE_TOKEN_DATE, authenticator.getTokenExpireDate()?.time.toString())

        val period: Long = 2 * 60 * 60
        val account = Account(userName, authType)
        ContentResolver.setIsSyncable(account, GlobalConstant.CONTENT_AUTHORITY, 1)
        ContentResolver.setSyncAutomatically(account, GlobalConstant.CONTENT_AUTHORITY, true)
        ContentResolver.addPeriodicSync(account, GlobalConstant.CONTENT_AUTHORITY, Bundle.EMPTY, period)

        val accountManager = AccountManager.get(applicationContext)
        // There is no direct usage for MS password so i just save a fake password
        accountManager.addAccountExplicitly(account, "password", bundle)
        accountManager.setAuthToken(account, authType, token)
        endActivity(bundle)
    }

    /**
     * send back the result and finish the current activity
     */
    private fun endActivity(bundle: Bundle) {
        Log.e("endActivity", "Bundle: $bundle")
        if (bundle.getSerializable(GlobalConstant.SIGNIN_ERROR) == null) {
            setAccountAuthenticatorResult(bundle)
            setResult(Activity.RESULT_OK, intent.putExtras(bundle))
        } else {
            setResult(Activity.RESULT_CANCELED, intent.putExtras(bundle))
        }

        finish()
    }
}