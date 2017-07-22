package com.sixthsolution.easymssyncadapter.msauthenticator

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.concurrency.SimpleWaiter
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.core.DefaultClientConfig
import com.microsoft.graph.core.GraphErrorCodes
import com.microsoft.graph.extensions.GraphServiceClient
import com.microsoft.graph.extensions.IGraphServiceClient
import com.microsoft.graph.http.IHttpRequest
import com.microsoft.graph.logger.DefaultLogger
import com.microsoft.graph.logger.ILogger
import com.microsoft.services.msa.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference


/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/12/2017.
 */
abstract class MSLoginHandler(application: Application) : IMSLoginHandler {
    companion object {
        // The authorization header name.
        val AUTHORIZATION_HEADER_NAME = "Authorization"

        // The bearer prefix.
        val OAUTH_BEARER_PREFIX = "bearer "
    }

    // The logger instance.
    private var mLogger: ILogger

    // The live auth client
    private val mLiveAuthClient: LiveAuthClient

    private var mGraphServiceClient: IGraphServiceClient? = null

    /**
     * The scopes for this application.
     * http://graph.microsoft.io/en-us/docs/authorization/permission_scopes
     *
     * @return The scopes for this application.
     */
    abstract fun getScopes(): Array<String>

    /**
     * The client id for this authenticator.
     * http://graph.microsoft.io/en-us/app-registration
     *
     * @return The client id.
     */
    fun getClientId(application: Application): String {
        try {
            val ai = application.packageManager.getApplicationInfo(application.packageName,
                    PackageManager.GET_META_DATA)
            val bundle = ai.metaData
            return bundle.getString("microsoft_client_id")
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    init {
        mLogger = DefaultLogger()
        application.baseContext
        mLiveAuthClient = LiveAuthClient(application.applicationContext,
                getClientId(application),
                getScopes().asIterable(),
                MicrosoftOAuth2Endpoint.Companion.getInstance())
    }


    /**
     * Set the logger to a specific instance
     *
     * @param logger The logger instance to use
     */
    fun setLogger(logger: ILogger) {
        mLogger = logger
    }

    override fun authenticateRequest(request: IHttpRequest) {
        mLogger.logDebug("Authenticating request, " + request.requestUrl)

        // If the request already has an authorization header, do not intercept it.
        for (option in request.headers) {
            if (option.name == (AUTHORIZATION_HEADER_NAME)) {
                mLogger.logDebug("Found an existing authorization header!")
                return
            }
        }

        if (hasValidSession()) {
            mLogger.logDebug("Found account information")
            if (mLiveAuthClient.session.isExpired) {
                mLogger.logDebug("Account access token is expired, refreshing");
                loginSilentBlocking()
            }

            val accessToken: String = mLiveAuthClient.session.accessToken
            request.addHeader(AUTHORIZATION_HEADER_NAME, OAUTH_BEARER_PREFIX + accessToken)
        } else {
            val message = "Unable to authenticate request, No active account found"
            val exception: ClientException = ClientException(message,
                    null,
                    GraphErrorCodes.AuthenticationFailure)
            mLogger.logError(message, exception)
            throw exception
        }
    }

    override fun logout(callback: ICallback<Void>) {
        mLogger.logDebug("Logout started")

        mLiveAuthClient.logout(object : LiveAuthListener {
            override fun onAuthError(exception: LiveAuthException?, userState: Any?) {
                val clientException = ClientException("Logout failure",
                        exception,
                        GraphErrorCodes.AuthenticationFailure)
                mLogger.logError(clientException.message, clientException)
                callback.failure(clientException)
            }

            override fun onAuthComplete(status: LiveStatus?, session: LiveConnectSession?, userState: Any?) {
                mLogger.logDebug("Logout complete")
                callback.success(null)
            }
        })
    }

    override fun login(activity: Activity, callback: ICallback<Void>) {
        mLogger.logDebug("Login started")

        if (hasValidSession()) {
            mLogger.logDebug("Already logged in")
            callback.success(null)
            return
        }

        val listener = object : LiveAuthListener {
            override fun onAuthComplete(status: LiveStatus?, session: LiveConnectSession?, userState: Any?) {
                mLogger.logDebug(String.format("LiveStatus: %s, LiveConnectSession good?: %s, UserState %s",
                        status,
                        (session == null),
                        userState))

                if (status == LiveStatus.NOT_CONNECTED) {
                    mLogger.logDebug("Received invalid login failure from silent authentication, ignoring.");
                    return
                }

                if (status == LiveStatus.CONNECTED) {
                    mLogger.logDebug("Login completed")
                    callback.success(null)
                    return
                }

                val clientException = ClientException("Unable to login successfully",
                        null,
                        GraphErrorCodes.AuthenticationFailure)
                mLogger.logError(clientException.message, clientException);
                callback.failure(clientException)
            }

            override fun onAuthError(exception: LiveAuthException?, userState: Any?) {
                val clientException = ClientException("Login failure",
                        exception,
                        GraphErrorCodes.AuthenticationFailure)
                mLogger.logError(clientException.message, clientException);
                callback.failure(clientException)
            }
        }

        // Make sure the login process is started with the current activity information
        activity.runOnUiThread { mLiveAuthClient.login(activity, listener) }
    }

    override fun loginSilent(callback: ICallback<Void>) {
        mLogger.logDebug("Login silent started")

        val listener = object : LiveAuthListener {
            override fun onAuthComplete(status: LiveStatus?, session: LiveConnectSession?, userState: Any?) {
                mLogger.logDebug(String.format("LiveStatus: %s, LiveConnectSession good?: %s, UserState %s",
                        status,
                        (session == null),
                        userState))

                if (status == LiveStatus.CONNECTED) {
                    mLogger.logDebug("Login completed")
                    callback.success(null)
                    return
                }

                val clientException = ClientException("Unable to login silently",
                        null,
                        GraphErrorCodes.AuthenticationFailure)

                mLogger.logError(clientException.message, clientException)
                callback.failure(clientException)
            }

            override fun onAuthError(exception: LiveAuthException?, userState: Any?) {
                val clientException = ClientException("Unable to login silently",
                        null,
                        GraphErrorCodes.AuthenticationFailure)
                mLogger.logError(clientException.message, clientException)
                callback.failure(clientException)
            }
        }

        mLiveAuthClient.loginSilent(listener)
    }

    override fun getAccessToken(): String? {
        if (hasValidSession()) {
            return mLiveAuthClient.session.accessToken
        }

        return null
    }

    override fun getRefreshToken(): String? {
        if (hasValidSession()) {
            return mLiveAuthClient.session.refreshToken
        }

        return null
    }

    override fun getTokenExpireDate(): Date? {
        if (hasValidSession()) {
            return mLiveAuthClient.session.expiresIn
        }

        return null
    }

    override fun getAuthenticationToken(): String? {
        if (hasValidSession()) {
            return mLiveAuthClient.session.authenticationToken
        }

        return null
    }

    private fun hasValidSession(): Boolean {
        return mLiveAuthClient.session != null && mLiveAuthClient.session.accessToken != null
    }

    /**
     * Login silently while blocking for the call to return
     *
     * @return the result of the login attempt
     * @throws ClientException The exception if there was an issue during the login attempt
     */
    @Throws(ClientException::class)
    override fun loginSilentBlocking(): Void {
        val waiter: SimpleWaiter = SimpleWaiter()
        val returnValue: AtomicReference<Void> = AtomicReference()
        val exceptionValue: AtomicReference<ClientException> = AtomicReference()

        loginSilent(object : ICallback<Void> {
            override fun success(aVoid: Void?) {
                returnValue.set(aVoid)
                waiter.signal()
            }

            override fun failure(ex: ClientException?) {
                exceptionValue.set(ex)
                waiter.signal()
            }
        })

        waiter.waitForSignal()

        //noinspection ThrowableResultOfMethodCallIgnored
        if (exceptionValue.get() != null) {
            throw exceptionValue.get()
        }

        return returnValue.get()
    }

    @Synchronized override fun getGraphServiceClient(): IGraphServiceClient {
        if (mGraphServiceClient == null) {
            val clientConfig = DefaultClientConfig.createWithAuthenticationProvider(this)
            mGraphServiceClient = GraphServiceClient.Builder().fromConfig(clientConfig).buildClient()
        }

        return mGraphServiceClient!!
    }
}
