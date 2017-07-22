package com.sixthsolution.easymssyncadapter.msauthenticator

import android.app.Activity
import com.microsoft.graph.authentication.IAuthenticationProvider
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.extensions.IGraphServiceClient
import java.util.*

/**
 * @author Mehdi Sohrabi (mehdok@gmail.com) on 6/12/2017.
 */
interface IMSLoginHandler : IAuthenticationProvider {

    /**
     * Logs out the user
     *
     * @param callback The callback when the logout is complete or an error occurs
     */
    fun logout(callback: ICallback<Void>)

    /**
     * Login a user by popping UI
     *
     * @param activity The current activity
     * @param callback The callback when the login is complete or an error occurs
     */
    fun login(activity: Activity, callback: ICallback<Void>)

    /**
     * Login a user with no ui
     *
     * @param callback The callback when the login is complete or an error occurs
     */
    fun loginSilent(callback: ICallback<Void>)

    /**
     * Get the access token from live session
     *
     * @return access token
     */
    fun getAccessToken(): String?

    /**
     * Get the refresh token from live session
     *
     * @return refresh token
     */
    fun getRefreshToken(): String?

    /**
     * Get date of token expiration, so you can refresh it
     *
     * @return date of token expiration
     */
    fun getTokenExpireDate(): Date?

    /**
     * Get the authentication token from live session
     *
     * @return authentication token
     */
    fun getAuthenticationToken(): String?

    fun loginSilentBlocking(): Void

    fun getGraphServiceClient(): IGraphServiceClient
}