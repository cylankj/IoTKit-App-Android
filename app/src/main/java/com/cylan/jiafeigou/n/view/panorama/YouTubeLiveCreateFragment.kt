package com.cylan.jiafeigou.n.view.panorama

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.rtmp.youtube.util.EventData
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.cylan.jiafeigou.utils.TimeUtils
import com.cylan.jiafeigou.utils.ToastUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import kotlinx.android.synthetic.main.fragment_youtube_create_live.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

/**
 * Created by yanzhendong on 2017/9/6.
 */
class YouTubeLiveCreateFragment : BaseFragment<YouTubeLiveCreateContract.Presenter>(), YouTubeLiveCreateContract.View {


    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {
        fragmentComponent!!.inject(this)
    }

    override fun getContentViewID(): Int {
        return R.layout.fragment_youtube_create_live
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        custom_toolbar.setBackAction { activity.onBackPressed() }
        custom_toolbar.setRightAction { createLiveEvent() }
    }

    private val title: String
        get() {
            return if (youtube_create_live_title.text.isNullOrEmpty()) {
                youtube_create_live_title.hint.toString()
            } else {
                youtube_create_live_title.text.toString()
            }
        }
    private val description: String?
        get() {
            return if (youtube_create_live_description.text.isNullOrEmpty()) {
                youtube_create_live_description.hint.toString()
            } else {
                youtube_create_live_description.text.toString()
            }
        }

    private var startTime: Long = System.currentTimeMillis()
        set(value) {
            field = value
            youtube_create_live_start_time.subTitle = TimeUtils.getYMDHM(field)
        }
    private var endTime: Long = 0
        set(value) {
            field = value
            if (field != 0L) {
                youtube_create_live_end_time.subTitle = TimeUtils.getYMDHM(field)
            }
        }

    private fun createLiveEvent() {
        presenter.createLiveBroadcast(mCredential, title, description, startTime, endTime)

    }

    @OnClick(R.id.youtube_create_live_start_time)
    fun selectStartTime() {
        AppLogger.w("选择开始时间")
        val timeStartPicker = TimePickerFragment.newInstance(uuid, getString(R.string.Tap1_CameraFun_Timelapse_StartTime), System.currentTimeMillis())
        timeStartPicker.onTimePickerResult { startTime = it }
        timeStartPicker.show(fragmentManager, TimePickerFragment::class.java.simpleName)
    }

    @OnClick(R.id.youtube_create_live_end_time)
    fun selectEndTime() {
        AppLogger.w("选择结束时间")
        val timeEndPicker = TimePickerFragment.newInstance(uuid, getString(R.string.TO), startTime)
        timeEndPicker.onTimePickerResult { endTime = it }
        timeEndPicker.show(fragmentManager, TimePickerFragment::class.java.simpleName)
    }

    @OnClick(R.id.youtube_create_live_manager_end_time)
    fun managerEndTime() {
        AppLogger.w("管理结束时间")
        endTime = 0
        if (youtube_create_live_end_time.isShown) {
            youtube_create_live_end_time.visibility = View.GONE
            youtube_create_live_manager_end_time.text = getString(R.string.LIVE_DETAIL_ADD_END_TIME)
        } else {
            youtube_create_live_end_time.subTitle = getString(R.string.NO_SET)
            youtube_create_live_end_time.visibility = View.VISIBLE
            youtube_create_live_manager_end_time.text = getString(R.string.LIVE_DETAIL_DEL_END_TIME)
        }
    }

    private val mCredential: GoogleAccountCredential by lazy {
        val credential = GoogleAccountCredential.usingOAuth2(
                activity.applicationContext, YouTubeScopes.all())
                .setBackOff(ExponentialBackOff())
        credential.selectedAccountName = account
        credential
    }

    private var account: String? = null
        set(value) {
            field = value
            mCredential.selectedAccountName = field
            if (field != null) {
                PreferencesUtils.putString(JConstant.YOUTUBE_PREF_ACCOUNT_NAME, field)
            } else {
                PreferencesUtils.remove(JConstant.YOUTUBE_PREF_ACCOUNT_NAME)
            }
        }
        get() {
            field = field ?: PreferencesUtils.getString(JConstant.YOUTUBE_PREF_ACCOUNT_NAME, null)
            return field
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
                } else {
                    getResultsFromApi()
                }

            }

            REQUEST_ACCOUNT_PICKER -> {

                if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                    account = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                }

            }


            REQUEST_AUTHORIZATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    getResultsFromApi()
                }

            }

        }
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
        } else if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
            if (account != null && mCredential.selectedAccountName != null) {
                presenter.createLiveBroadcast(mCredential, title, description, startTime, endTime)
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(this,
                    getString(R.string.permission_auth, getString(R.string.contacts_auth)),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
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
        } else if (mCredential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1))
        } else {
            presenter.createLiveBroadcast(mCredential, title, description, startTime, endTime)
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

    override fun onCreateLiveBroadcastSuccess(eventData: EventData?) {
        listener?.invoke()
    }

    var listener: (() -> Unit)? = null

    companion object {
        private const val REQUEST_PERMISSION_GET_ACCOUNTS = 5000
        private const val REQUEST_GOOGLE_PLAY_SERVICES = 5001
        private const val REQUEST_ACCOUNT_PICKER = 8000
        private const val REQUEST_AUTHORIZATION = 8001
        fun newInstance(uuid: String): YouTubeLiveCreateFragment {
            val fragment = YouTubeLiveCreateFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }
}