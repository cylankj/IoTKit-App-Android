package com.cylan.jiafeigou.n.view.panorama

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.rtmp.youtube.util.EventData
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.support.oauth2.AuthStateManager
import com.cylan.jiafeigou.support.oauth2.Configuration
import com.cylan.jiafeigou.support.share.ShareManager
import com.cylan.jiafeigou.utils.ActivityUtils
import com.cylan.jiafeigou.utils.MiscUtils
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.cylan.jiafeigou.utils.ToastUtil
import com.google.android.gms.common.AccountPicker
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTubeScopes
import kotlinx.android.synthetic.main.layout_youtube.*
import net.openid.appauth.*
import okio.Okio
import org.json.JSONException
import org.json.JSONObject
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset

/**
 * Created by yanzhendong on 2017/9/7.
 */
class YouTubeLiveSettingFragment : BaseFragment<YouTubeLiveSetting.Presenter>(), YouTubeLiveSetting.View {


    companion object {
        private const val REQUEST_PERMISSION_GET_ACCOUNTS: Int = 5000
        private const val REQUEST_GOOGLE_PLAY_SERVICES: Int = 5001
        private const val REQUEST_ACCOUNT_PICKER: Int = 8000
        private const val REQUEST_AUTHORIZATION: Int = 8001
        private const val REQUEST_AUTHORIZATION_REQ: Int = 8002


        fun newInstance(uuid: String): YouTubeLiveSettingFragment {
            val fragment = YouTubeLiveSettingFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }

    }


    private var account: String? = null
        set(value) {
            field = value
            if (TextUtils.isEmpty(field)) {
                setting_youtube_account_item.subTitle = getString(R.string.LIVE_ACCOUNT_UNBOUND)
                setting_youtube_option_container.visibility = View.GONE
                PreferencesUtils.remove(JConstant.YOUTUBE_PREF_ACCOUNT_NAME)
                AuthStateManager.getInstance(context).replace(AuthState())
                PreferencesUtils.remove(JConstant.YOUTUBE_PREF_CONFIGURE + ":" + uuid)
            } else {
                setting_youtube_account_item.subTitle = field
                setting_youtube_option_container.visibility = View.VISIBLE
                PreferencesUtils.putString(JConstant.YOUTUBE_PREF_ACCOUNT_NAME, field)
            }
        }
        get() {
            field = field ?: PreferencesUtils.getString(JConstant.YOUTUBE_PREF_ACCOUNT_NAME, null)

            if (field != null) {
                setting_youtube_account_item.subTitle = field ?: getString(R.string.NO_SET)
                setting_youtube_option_container.visibility = if (field == null) View.GONE else View.VISIBLE
            } else if (manager.current.isAuthorized) {
                fetchUserInfo()
            }
            return field
        }


    private var youtubeEvent: EventData? = null
        private set
        get() {
            val broadcast = PreferencesUtils.getString(JConstant.YOUTUBE_PREF_CONFIGURE + ":" + uuid, null)
            if (!TextUtils.isEmpty(broadcast)) {
                field = try {
                    JacksonFactory.getDefaultInstance().fromString(broadcast, EventData::class.java)
                } catch (e: Exception) {
                    AppLogger.e(MiscUtils.getErr(e))
                    null
                }
            }
            return field
        }


//    private val youtubeCreateFragment by lazy {
//
//
//        fragment
//    }
//    private val youtubeDetailFragment by lazy { YouTubeLiveDetailFragment.newInstance(uuid) }




    override fun getContentViewID(): Int {
        return R.layout.layout_youtube
    }


    override fun initViewAndListener() {
        super.initViewAndListener()
        setting_youtube_account_item.title = getString(R.string.LIVE_ACCOUNT, getString(R.string.LIVE_PLATFORM_YOUTUBE))

    }

    override fun onStart() {
        super.onStart()
        loadLiveBroadCast()
    }

    private fun loadLiveBroadCast() {
        account = account;youtubeEvent = youtubeEvent
        if (account != null && youtubeEvent != null) {
            setting_youtube_option_container.visibility = View.VISIBLE
            live_event_container.visibility = View.VISIBLE
            live_event_description.text = youtubeEvent?.title ?: getString(R.string.LIVE_DETAIL_DEFAULT_CONTENT)
        }
    }

    @OnClick(R.id.youtube_create_live)
    fun showCreateYoutubeFragment() {
        if (youtubeEvent == null) {
            val youtubeCreateFragment = YouTubeLiveCreateFragment.newInstance(uuid)
            youtubeCreateFragment.listener = {
                loadLiveBroadCast()
                fragmentManager.popBackStack()
            }
            ActivityUtils.addFragmentSlideInFromRight(fragmentManager, youtubeCreateFragment, android.R.id.content)
        } else {
            AlertDialog.Builder(context)
                    .setMessage(R.string.LIVE_CREATE_NEW_LIVE)
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK, { dialog, _ ->
                        dialog.dismiss()
                        val youtubeCreateFragment = YouTubeLiveCreateFragment.newInstance(uuid)
                        youtubeCreateFragment.listener = {
                            loadLiveBroadCast()
                            fragmentManager.popBackStack()
                        }
                        ActivityUtils.addFragmentSlideInFromRight(fragmentManager, youtubeCreateFragment, android.R.id.content)
                    })
                    .setNegativeButton(R.string.CANCEL, { dialog, _ -> dialog.dismiss() })
                    .show()
        }
    }

    @OnClick(R.id.live_setting_youtube_share)
    fun shareLive() {
        ShareManager.byWeb(activity)
                .withTitle(youtubeEvent?.title ?: getString(R.string.LIVE_DETAIL_DEFAULT_CONTENT))
                .withDescription(youtubeEvent?.event?.snippet?.description ?: getString(R.string.LIVE_DETAIL_DEFAULT_CONTENT))
                .withUrl(youtubeEvent?.watchUri)
                .withThumbRes(R.mipmap.ic_launcher)
                .shareWithLink()
    }

    @OnClick(R.id.live_event_item_container)
    fun showLiveDetail() {
        val youtubeDetailFragment = YouTubeLiveDetailFragment.newInstance(uuid)
        ActivityUtils.addFragmentSlideInFromRight(fragmentManager, youtubeDetailFragment, android.R.id.content)
    }

    override fun onLiveEventResponse(rtmp: EventData) {
//        liveEvent = rtmp
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> {
                if (resultCode != Activity.RESULT_OK) {
                    AppLogger.w("谷歌服务未开启")
                } else if (account == null) {
                    chooseAccount()
                }

            }
            REQUEST_ACCOUNT_PICKER -> {

                if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                    account = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                }

            }
            REQUEST_AUTHORIZATION_REQ -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val response = AuthorizationResponse.fromIntent(data)
                        val ex = AuthorizationException.fromIntent(data)
                        manager.updateAfterAuthorization(response, ex)
                        if (response?.authorizationCode != null) run {
                            // authorization code exchange is required
                            manager.updateAfterAuthorization(response, ex)
                            exchangeAuthorizationCode(response)
                        }
                    } else {
                        //todo 这可能和手机有关,回调了 但是data 为空
                        AppLogger.e("获取 Youtube 网页授权结果失败了!!!")
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    AppLogger.w("用户取消了 YouTube 网页授权")
                }
            }
        }
    }

    @MainThread
    private fun performTokenRequest(
            request: TokenRequest,
            callback: AuthorizationService.TokenResponseCallback) {
        val clientAuthentication: ClientAuthentication
        try {
            clientAuthentication = manager.current.clientAuthentication
        } catch (ex: ClientAuthentication.UnsupportedAuthenticationMethod) {
            AppLogger.w("Token request cannot be made, client authentication for the token " + "endpoint could not be constructed (%s)" + ex.toString())
            return
        }

        authorizationService.performTokenRequest(
                request,
                clientAuthentication,
                callback)
    }

    private fun exchangeAuthorizationCode(authorizationResponse: AuthorizationResponse) {
        performTokenRequest(authorizationResponse.createTokenExchangeRequest(),
                AuthorizationService.TokenResponseCallback { tokenResponse, authException ->
                    manager.updateAfterTokenResponse(tokenResponse, authException)
                    if (manager.current.isAuthorized) {
                        fetchUserInfo()
                    }
                })
    }

    @MainThread
    private fun fetchUserInfo() {
        manager.current.performActionWithFreshTokens(authorizationService, { accessToken, idToken, ex ->
            if (ex != null) {
                AppLogger.e("Token refresh failed when fetching user info")
                activity.runOnUiThread { account = null }
                return@performActionWithFreshTokens
            }

            val discovery = manager.current.authorizationServiceConfiguration!!.discoveryDoc

            val userInfoEndpoint: URL
            try {
                userInfoEndpoint = URL(discovery!!.userinfoEndpoint!!.toString())
            } catch (urlEx: MalformedURLException) {
                AppLogger.e("Failed to construct user info endpoint URL" + urlEx)
                activity.runOnUiThread { account = null }
                return@performActionWithFreshTokens
            }

            Schedulers.newThread().createWorker().schedule {
                try {
                    val conn = userInfoEndpoint.openConnection() as HttpURLConnection
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken)
                    conn.instanceFollowRedirects = false
                    val response = Okio.buffer(Okio.source(conn.inputStream))
                            .readString(Charset.forName("UTF-8"))
                    AndroidSchedulers.mainThread().createWorker().schedule {
                        val jsonObject = JSONObject(response)
                        if (jsonObject.has("name")) {
                            account = jsonObject.getString("name")
                        }
                    }
                } catch (ioEx: IOException) {
                    AppLogger.e("Network error when querying userinfo endpoint" + ioEx)
                } catch (jsonEx: JSONException) {
                    AppLogger.e("Failed to parse userinfo response")
                }
            }
        })

    }

    private val authorizationService: AuthorizationService by lazy { AuthorizationService(context) }
    private val configuration: Configuration by lazy { Configuration.getInstance(context) }
    private val manager: AuthStateManager by lazy { AuthStateManager.getInstance(context) }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @OnClick(R.id.setting_youtube_account_item)
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    fun chooseAccount() {
        if (account != null) {
            AlertDialog.Builder(context)
                    .setMessage(getString(R.string.LIVE_UNBIND_ACCOUNT, getString(R.string.LIVE_PLATFORM_YOUTUBE)))
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK, { _, _ ->
                        account = null
                    })
                    .setNegativeButton(R.string.CANCEL, { dialog, _ ->
                        dialog.dismiss()
                    })
                    .show()
        } else if (isGooglePlayServicesAvailable()) {
            //有 Google 服务,优先使用 Google 服务
            if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
//            if (account != null && liveBroadcastId != null) {
//                presenter.getLiveList(mCredential, liveBroadcastId!!)
//            }
                if (TextUtils.isEmpty(account)) {
                    // Start a dialog from which the user can choose an account
                    if (isGooglePlayServicesAvailable()) {
                        startActivityForResult(AccountPicker.newChooseAccountIntent(
                                null,
                                null,
                                arrayOf(GoogleAccountManager.ACCOUNT_TYPE),
                                true,
                                null,
                                null,
                                null,
                                null),
                                REQUEST_ACCOUNT_PICKER)
                    } else {
                        acquireGooglePlayServices()
                    }
                }
            } else {
                // Request the GET_ACCOUNTS permission via a user dialog
                EasyPermissions.requestPermissions(this,
                        getString(R.string.permission_auth, getString(R.string.contacts_auth)),
                        REQUEST_PERMISSION_GET_ACCOUNTS,
                        Manifest.permission.GET_ACCOUNTS)
            }
        } else {
            if (manager.current.isAuthorized) {
                fetchUserInfo()
            } else {
                AuthorizationServiceConfiguration.fetchFromUrl(configuration.discoveryUri!!, { serviceConfiguration, ex ->
                    if (ex == null) {
                        manager.replace(AuthState(serviceConfiguration!!))
                        val authorizationRequest = AuthorizationRequest
                                .Builder(serviceConfiguration, configuration.clientId!!, ResponseTypeValues.CODE, configuration.redirectUri)
                                .setScopes(
                                        AuthorizationRequest.Scope.PROFILE,
                                        YouTubeScopes.YOUTUBE,
                                        YouTubeScopes.YOUTUBEPARTNER,
                                        YouTubeScopes.YOUTUBEPARTNER_CHANNEL_AUDIT,
                                        YouTubeScopes.YOUTUBE_READONLY,
                                        YouTubeScopes.YOUTUBE_UPLOAD)
                                .build()

                        val requestIntent = authorizationService.getAuthorizationRequestIntent(authorizationRequest)
                        startActivityForResult(requestIntent, REQUEST_AUTHORIZATION_REQ)
                    }
                })
            }
        }
    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (account == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1))
        } else {
//            presenter.getLiveList(mCredential, liveBroadcastId)
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private fun isDeviceOnline(): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    override fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(activity, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    override fun onUserRecoverableAuthIOException(error: UserRecoverableAuthIOException) {
        startActivityForResult(error.intent, REQUEST_AUTHORIZATION)
    }

    fun isYoutubeAccountBinded(): Boolean {
        return account != null
    }

}