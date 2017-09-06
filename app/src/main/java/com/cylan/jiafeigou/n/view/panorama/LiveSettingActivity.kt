package com.cylan.jiafeigou.n.view.panorama

import android.Manifest
import android.accounts.AccountManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.view.View
import butterknife.OnClick
import com.androidkun.xtablayout.XTabLayout
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.ActivityComponent
import com.cylan.jiafeigou.base.wrapper.BaseActivity
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.support.softkeyboard.util.KPSwitchConflictUtil
import com.cylan.jiafeigou.support.softkeyboard.util.KeyboardUtil
import com.cylan.jiafeigou.utils.ActivityUtils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import kotlinx.android.synthetic.main.activity_live_setting.*
import kotlinx.android.synthetic.main.layout_rtmp.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*

@RuntimePermissions
class LiveSettingActivity : BaseActivity<LiveSettingContact.Presenter>(), LiveSettingContact.View, XTabLayout.OnTabSelectedListener {
    lateinit var mCredential: GoogleAccountCredential

    private val REQUEST_ACCOUNT_PICKER = 8000
    private val REQUEST_AUTHORIZATION = 8001
    private val SCOPES = arrayOf(YouTubeScopes.YOUTUBE_READONLY)
    override fun setActivityComponent(activityComponent: ActivityComponent?) {
        activityComponent!!.inject(this)
    }

    override fun getContentViewID(): Int {
        return R.layout.activity_live_setting

    }


    override fun initViewAndListener() {
        super.initViewAndListener()
        initKeyBoard()
        rtmp_type_tabs.setOnTabSelectedListener(this)
        switchRtmpPage(rtmp_type_tabs.selectedTabPosition)




        initFacebookInfo()

        initYouTubeInfo()

        initWeiboInfo()
    }

    private fun initWeiboInfo() {

    }

    private fun initFacebookInfo() {

    }

    private fun initYouTubeInfo() {
        mCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Arrays.asList(*SCOPES))
                .setBackOff(ExponentialBackOff())
        getResultsFromApi()

    }

    @OnPermissionDenied(Manifest.permission.GET_ACCOUNTS_PRIVILEGED)
    fun onAccountPermissionDenied() {

    }

    @NeedsPermission(Manifest.permission.GET_ACCOUNTS_PRIVILEGED)
    fun initAccountInfo(): Unit {

    }

    override fun onTabReselected(tab: XTabLayout.Tab?) {

    }

    override fun onTabUnselected(tab: XTabLayout.Tab?) {
    }

    override fun onTabSelected(tab: XTabLayout.Tab?) {
        switchRtmpPage(tab!!.position)
    }

    private fun switchRtmpPage(position: Int) {
        when (position) {
            0 -> {
                page_facebook.visibility = View.VISIBLE
                page_youtube.visibility = View.GONE
                page_weibo.visibility = View.GONE
                page_rtmp.visibility = View.GONE
            }
            1 -> {
                page_facebook.visibility = View.GONE
                page_youtube.visibility = View.VISIBLE
                page_weibo.visibility = View.GONE
                page_rtmp.visibility = View.GONE
            }
            2 -> {
                page_facebook.visibility = View.GONE
                page_youtube.visibility = View.GONE
                page_weibo.visibility = View.VISIBLE
                page_rtmp.visibility = View.GONE
            }
            3 -> {
                page_facebook.visibility = View.GONE
                page_youtube.visibility = View.GONE
                page_weibo.visibility = View.GONE
                page_rtmp.visibility = View.VISIBLE
            }
        }
        var accountManager: AccountManager = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager


    }

    fun setup(v: View) {

    }

    private fun initKeyBoard() {
        KeyboardUtil.attach(this, panel_root) { isShowing ->
            //            if (suggestionAdapter == null) return@KeyboardUtil.attach
//            mRvMineSuggestion.scrollToPosition(suggestionAdapter.getItemCount() - 1)
        }
        KPSwitchConflictUtil.attach(panel_root, page_rtmp, KPSwitchConflictUtil.SwitchClickListener {
            AppLogger.w("KPSwitchConflictUtil:" + it)
            if (it) {
                rtmp_et_miyao.clearFocus()
            } else {
                rtmp_et_miyao.requestFocus()
            }
        })

//        mRvMineSuggestion.setOnTouchListener({ v, event ->
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot)
//            }
//            false
//        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        LiveSettingActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
    }


    /*Facebook*/

    @OnClick(R.id.facebook_permission)
    fun setFacebookPermission() {
        val instance = LivePermissionFragment.newInstance()
        instance.setCallBack {

        }
        ActivityUtils.addFragmentSlideInFromRight(supportFragmentManager, instance, android.R.id.content)
    }

    /*YouTube*/
    @OnClick(R.id.youtube_create_live)
    fun showCreateYoutubeFragment() {
        val fragment = YouTubeLiveCreateFragment.newInstance()

        ActivityUtils.addFragmentSlideInFromRight(supportFragmentManager, fragment, android.R.id.content)
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
        } else if (mCredential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
//            mOutputText.setText("No network connection available.")
        } else {
            MakeRequestTask(mCredential).execute()
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private fun isDeviceOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

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
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(JConstant.YOUTUBE_PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName)
                getResultsFromApi()
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER)
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
//            EasyPermissions.requestPermissions(
//                    this,
//                    "This app needs to access your Google account (via Contacts).",
//                    REQUEST_PERMISSION_GET_ACCOUNTS,
//                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
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
    private fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
//        val apiAvailability = GoogleApiAvailability.getInstance()
//        val dialog = apiAvailability.getErrorDialog(
//                this,
//                connectionStatusCode,
//                REQUEST_GOOGLE_PLAY_SERVICES)
//        dialog.show()
    }


    /**
     * An asynchronous task that handles the YouTube Data API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private inner class MakeRequestTask internal constructor(credential: GoogleAccountCredential) : AsyncTask<Void, Void, List<String>>() {
        private var mService: com.google.api.services.youtube.YouTube? = null
        private var mLastError: Exception? = null

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            mService = com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build()
        }

        /**
         * Background task to call YouTube Data API.
         *
         * @param params no parameters needed for this task.
         */
        override fun doInBackground(vararg params: Void): List<String>? {
            try {
                return dataFromApi
            } catch (e: Exception) {
                mLastError = e
                cancel(true)
                return null
            }

        }

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         *
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */
        private // Get a list of up to 10 files.
        val dataFromApi: List<String>
            @Throws(IOException::class)
            get() {
                val channelInfo = ArrayList<String>()
                val result = mService!!.channels().list("snippet,contentDetails,statistics")
                        .setForUsername("GoogleDevelopers")
                        .execute()
                val channels = result.items
                if (channels != null) {
                    val channel = channels[0]
                    channelInfo.add("This channel's ID is " + channel.id + ". " +
                            "Its title is '" + channel.snippet.title + ", " +
                            "and it has " + channel.statistics.viewCount + " views.")
                }
                return channelInfo
            }


        override fun onPreExecute() {
//            mOutputText.setText("")
//            mProgress.show()
        }

        override fun onPostExecute(output: List<String>?) {
//            mProgress.hide()
            if (output == null || output.size == 0) {
//                mOutputText.setText("No results returned.")
            } else {
//                output.add(0, "Data retrieved using the YouTube Data API:")
//                mOutputText.setText(TextUtils.join("\n", output))
            }
        }

        override fun onCancelled() {
//            mProgress.hide()
            if (mLastError != null) {
                if (mLastError is GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            (mLastError as GooglePlayServicesAvailabilityIOException)
                                    .connectionStatusCode)
                } else if (mLastError is UserRecoverableAuthIOException) {
                    startActivityForResult(
                            (mLastError as UserRecoverableAuthIOException).intent,
                            REQUEST_AUTHORIZATION)
                } else {
//                    mOutputText.setText("The following error occurred:\n" + mLastError!!.message)
                }
            } else {
//                mOutputText.setText("Request cancelled.")
            }
        }
    }


    /*Weibo*/

    /*Rtmp*/
}
