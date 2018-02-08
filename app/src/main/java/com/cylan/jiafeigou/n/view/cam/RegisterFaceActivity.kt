package com.cylan.jiafeigou.n.view.cam

import android.Manifest
import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseActivity
import com.cylan.jiafeigou.misc.AlertDialogManager
import com.cylan.jiafeigou.misc.JConstant.MEDIA_PATH
import com.cylan.jiafeigou.module.GlideApp
import com.cylan.jiafeigou.n.mvp.contract.cam.RegisterFaceContract
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.support.photoselect.ClipImageActivity
import com.cylan.jiafeigou.support.photoselect.activities.AlbumSelectActivity
import com.cylan.jiafeigou.support.photoselect.helpers.Constants
import com.cylan.jiafeigou.utils.ActivityUtils
import com.cylan.jiafeigou.utils.IMEUtils
import com.cylan.jiafeigou.utils.ToastUtil
import com.cylan.jiafeigou.utils.ViewUtils
import com.cylan.jiafeigou.widget.dialog.PickImageFragment
import kotlinx.android.synthetic.main.activity_register_face.*
import kotlinx.android.synthetic.main.custom_edit_text.view.*
import permissions.dispatcher.*
import java.io.File

@RuntimePermissions
class
RegisterFaceActivity : BaseActivity<RegisterFaceContract.Presenter>(), RegisterFaceContract.View, TextWatcher {
    override fun onRegisterErrorInvalidParams() {
        AppLogger.w("onRegisterErrorInvalidParams")
        ToastUtil.showToast(getString(R.string.REGISTRATION_FAILED))
    }

    override fun onRegisterErrorServerInternalError() {
        AppLogger.w("onRegisterErrorServerInternalError")
        ToastUtil.showToast(getString(R.string.REGISTRATION_FAILED))
    }

    override fun onRegisterErrorNoFaceError() {
        AppLogger.w("onRegisterErrorNoFaceError")
        ToastUtil.showToast(getString(R.string.FACE_NOT_RECOGNIZED))
    }

    override fun onRegisterErrorFaceSmallError() {
        AppLogger.w("onRegisterErrorFaceSmallError")
        ToastUtil.showToast(getString(R.string.REGFACE_FACESMALL))
    }

    override fun onRegisterErrorMultiFaceError() {
        AppLogger.w("onRegisterErrorMultiFaceError")
        ToastUtil.showToast(getString(R.string.REGFACE_MULTIFACE))
    }

    override fun onRegisterErrorNoFeaturesInFaceError() {
        AppLogger.w("onRegisterErrorNoFeaturesInFaceError")
        ToastUtil.showToast(getString(R.string.REGISTRATION_FAILED))
    }

    override fun onRegisterErrorRegUserError() {
        AppLogger.w("onRegisterErrorRegUserError")
        ToastUtil.showToast(getString(R.string.REGISTRATION_FAILED))
    }

    override fun onRegisterErrorRegisterFailed() {
        AppLogger.w("onRegisterErrorRegisterFailed")
        ToastUtil.showToast(getString(R.string.REGISTRATION_FAILED))
    }

    override fun onRegisterErrorPermissionDenied() {
        AppLogger.w("onRegisterErrorPermissionDenied")
        ToastUtil.showToast(getString(R.string.REGISTRATION_FAILED))
    }

    override fun onRegisterErrorNoNetwork() {
        AppLogger.w("onRegisterErrorNoNetwork")
        ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1))
    }

    override fun onRegisterErrorWrongPictureFormat() {
        AppLogger.w("onRegisterErrorWrongPictureFormat")
    }

    override fun onRegisterErrorDetectionFailed() {
        AppLogger.w("onRegisterErrorDetectionFailed")
        ToastUtil.showToast(getString(R.string.REGISTRATION_FAILED))
    }

    override fun onRegisterSuccessful() {
        AppLogger.w("onRegisterSuccessful")
        IMEUtils.hide(this)
        setResult(Activity.RESULT_OK)
        val fragment = RegisterFaceSuccessFragment.newInstance(uuid)
        fragment.setCallBack {
            resetDefaultLayout()
        }
        ActivityUtils.addFragmentSlideInFromRight(supportFragmentManager, fragment, android.R.id.content)
    }

    private fun resetDefaultLayout() {
        img_photo.setImageResource(R.drawable.icon_register_face1)
        img_select_photo.visibility = View.INVISIBLE
        photo_nick_name.getEditer().text.clear()

    }

    override fun onRegisterTimeout() {
        AppLogger.w("onRegisterTimeout")
        ToastUtil.showToast(getString(R.string.REGISTRATION_FAILED))
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        refreshFinishEnable()
    }

    private var outPutUri: Uri? = null
    private var tempFile: File? = null
    private var cropFileUri: Uri? = null
    override fun onSetContentView(): Boolean {
        setContentView(R.layout.activity_register_face)
        return true
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        custom_toolbar.setBackAction { finish() }
        custom_toolbar.setRightAction(this::onFinishedClicked)
        photo_nick_name.showClear = false
        photo_nick_name.addTextChangedListener(this)
    }

    fun refreshSelectedFace() {
        img_select_photo.visibility = if (cropFileUri == null) View.INVISIBLE else View.VISIBLE
        refreshFinishEnable()
        if (cropFileUri == null) return
        val cropImagePath = getRealFilePathFromUri(context, cropFileUri)
        GlideApp.with(this).load(cropImagePath)
                .into(img_photo)
    }

    fun refreshFinishEnable() {
        custom_toolbar.setRightEnable(cropFileUri != null && !TextUtils.isEmpty(photo_nick_name.getEditer().text?.trim()))
    }

    fun onFinishedClicked(view: View) {
        IMEUtils.hide(this)
        presenter.performRegisterFaceAction(photo_nick_name.getEditer().text?.toString()?.trim(), getRealFilePathFromUri(context, cropFileUri))
    }

    override fun onResume() {
        super.onResume()
        refreshSelectedFace()
    }

    override fun onStop() {
        super.onStop()
        IMEUtils.hide(this)
    }

    /**
     * 弹出选择头像的对话框
     */
    @OnClick(R.id.img_photo, R.id.img_select_photo)
    fun onSelectFaceClicked(v: View) {
        AppLogger.w("onSelectFaceClicked")
        ViewUtils.deBounceClick(v)
        val fragment = PickImageFragment.newInstance(null)
        fragment.setClickListener({ vv ->
            //打开相机
            RegisterFaceActivityPermissionsDispatcher.openCameraWithPermissionWithCheck(this@RegisterFaceActivity)
        }) { cc -> openGallery() }
        fragment.show(supportFragmentManager, "pickImageDialog")
    }


    @OnNeverAskAgain(Manifest.permission.CAMERA)
    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun showOpenCameraPermissionDialog() {
        showPermissionDialog(getString(R.string.CAMERA))
    }

    private val OPEN_CAMERA: Int = 999
    private val REQUEST_CROP_PHOTO = 998

    @NeedsPermission(Manifest.permission.CAMERA)
    fun openCameraWithPermission() {
        if (PermissionUtils.hasSelfPermissions(this, Manifest.permission.CAMERA)) {
            createCameraTempFile(null)
            val contentValues = ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, tempFile!!.absolutePath)
            outPutUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri)
            startActivityForResult(intent, OPEN_CAMERA)
        } else {
            showPermissionDialog(getString(R.string.camera_auth))
        }
    }

    fun showPermissionDialog(permission: String) {
        val builder = AlertDialogManager.getInstance().getCustomDialog(this)
        builder.setMessage(getString(R.string.permission_auth, permission))
                .setNegativeButton(getString(R.string.CANCEL)) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                .setPositiveButton(getString(R.string.SETTINGS)) { dialog: DialogInterface, which: Int -> openSetting() }
        AlertDialogManager.getInstance().showDialog("showSetPermissionDialog", this, builder)
    }

    private fun openSetting() {
        val settingIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        settingIntent.data = Uri.parse("package:" + packageName)
        startActivity(settingIntent)
    }

    /**
     * 打开相册
     */
    private fun openGallery() {
        val intent = Intent(context, AlbumSelectActivity::class.java)
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 3)
        startActivityForResult(intent, Constants.REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        RegisterFaceActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == Constants.REQUEST_CODE && data != null) {
                gotoClipActivity(Uri.fromFile(File(data.getStringExtra(Constants.INTENT_EXTRA_IMAGES))))
            } else if (requestCode == REQUEST_CROP_PHOTO && data != null) {
                cropFileUri = data.data
                refreshSelectedFace()
            } else if (requestCode == OPEN_CAMERA) {
                if (resultCode == Activity.RESULT_OK) {
                    gotoClipActivity(outPutUri)
                }
            }
        }
    }

    /**
     * 打开截图界面
     *
     * @param uri
     */
    fun gotoClipActivity(uri: Uri?) {
        if (uri == null) {
            return
        }
        val intent = Intent()
        intent.setClass(context, ClipImageActivity::class.java)
        intent.putExtra("type", 1)
        intent.putExtra("just_crop", true)
        intent.data = uri
        startActivityForResult(intent, REQUEST_CROP_PHOTO)
    }

    /**
     * @param context
     * @param uri
     * @return the file path or null
     */
    fun getRealFilePathFromUri(context: Context, uri: Uri?): String? {
        if (null == uri) {
            return null
        }
        val scheme = uri.scheme
        var data: String? = null
        if (scheme == null) {
            data = uri.path
        } else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }
        return data
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putSerializable("tempFile", tempFile)
    }

    /**
     * 创建调用系统照相机待存储的临时文件
     *
     * @param savedInstanceState
     */
    private fun createCameraTempFile(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && savedInstanceState.containsKey("tempFile")) {
            tempFile = savedInstanceState.getSerializable("tempFile") as File
        } else {
            tempFile = File(MEDIA_PATH, System.currentTimeMillis().toString() + ".jpg")
            val parentFile = tempFile!!.parentFile
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photo_nick_name.isFocusable = true
        photo_nick_name.isFocusableInTouchMode = true
        photo_nick_name.getEditer().setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                setEditTextFocusable(false)
            }
            return@setOnKeyListener false
        }
        photo_nick_name.edit_text.filters = arrayOf(InputFilter.LengthFilter(24))
//        photo_nick_name.edit_text.filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
//            val originWidth = BoringLayout.getDesiredWidth("$dest", photo_nick_name.edit_text.paint)
//            val measuredWidth = photo_nick_name.edit_text.measuredWidth
//            var result = "$source"
//            var width = BoringLayout.getDesiredWidth(result, photo_nick_name.edit_text.paint)
//
//            Log.i(JConstant.CYLAN_TAG, "source:$source,dest:$dest,usedWidth:$originWidth inputWidth:$width,acceptWidth:${photo_nick_name.edit_text.measuredWidth}")
//
//            while (originWidth + width > measuredWidth) {
//                result = result.dropLast(1)
//                width = BoringLayout.getDesiredWidth(result, photo_nick_name.edit_text.paint)
//            }
//            result
//        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val currentFocus = currentFocus
            if (isShouldHideInput(currentFocus, ev)) {
                setEditTextFocusable(false)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun setEditTextFocusable(focusable: Boolean) {
        if (focusable) {
            photo_nick_name.getEditer().isFocusableInTouchMode = true
            photo_nick_name.getEditer().requestFocus()
        } else {
            hideInputMethod(this)
        }
        photo_nick_name.getEditer().clearFocus()
    }

    fun isShouldHideInput(v: View?, ev: MotionEvent): Boolean {
        if (v != null && (v is EditText)) {
            var leftTop: IntArray = intArrayOf(0, 0)
            v.getLocationInWindow(leftTop);
            val left = leftTop[0]
            val top = leftTop[1]
            val bottom = top + v.height
            val right = left + v.width
            return !(ev.x > left && ev.x < right && ev.y > top && ev.y < bottom)
        }
        return false
    }

    fun hideInputMethod(context: Context): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        return imm?.hideSoftInputFromWindow(photo_nick_name?.windowToken, 0) ?: false
    }

}
