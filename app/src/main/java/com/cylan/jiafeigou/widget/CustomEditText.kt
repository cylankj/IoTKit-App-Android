package com.cylan.jiafeigou.widget

import android.content.Context
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.FrameLayout
import butterknife.*
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.utils.ViewUtils
import kotlinx.android.synthetic.main.custom_edit_text.view.*

/**
 * Created by yanzhendong on 2017/11/20.
 */
class CustomEditText(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    var showClear: Boolean = true

    init {
        View.inflate(context, R.layout.custom_edit_text, this)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText)
        val resources = context.resources
        val textColor = attributes.getColor(R.styleable.CustomEditText_android_textColor, resources.getColor(R.color.color_666666))
        edit_text.setTextColor(textColor)

        val textSize = attributes.getDimension(R.styleable.CustomEditText_android_textSize, resources.getDimension(R.dimen.sp15))
        edit_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

        val textHint = attributes.getString(R.styleable.CustomEditText_android_hint)
        if (!TextUtils.isEmpty(textHint)) {
            edit_text.hint = textHint
        }

        val inputType = attributes.getInt(R.styleable.CustomEditText_android_inputType, android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)
        edit_text.inputType = inputType

        val maxLength = attributes.getInt(R.styleable.CustomEditText_android_maxLength, 65)
        edit_text.filters = arrayOf(InputFilter.LengthFilter(maxLength))

        val textColorHint = attributes.getColor(R.styleable.CustomEditText_android_textColorHint, resources.getColor(R.color.color_cecece))
        edit_text.setHintTextColor(textColorHint)

        val showPasswordIcon = attributes.getBoolean(R.styleable.CustomEditText_showPasswordIcon, true)
        show.visibility = if (showPasswordIcon) View.VISIBLE else View.GONE

        val textGravity = attributes.getInt(R.styleable.CustomEditText_android_gravity, Gravity.CENTER_VERTICAL or Gravity.START)
        edit_text.gravity = textGravity

        val checkPasswordIcon = attributes.getBoolean(R.styleable.CustomEditText_checkPasswordIcon, true)
        show.isChecked = checkPasswordIcon

        attributes.recycle()


        ButterKnife.bind(this, this)
    }

    @OnTextChanged(R.id.edit_text)
    fun onTextChanged(text: CharSequence?) {
        if (!showClear){
            return
        }
        clear.visibility = if (TextUtils.isEmpty(text?.trim())) View.GONE else View.VISIBLE
    }

    @OnFocusChange(R.id.edit_text)
    fun onEditFocusChanged(view: View, focus: Boolean) {
        if (!showClear){
            return
        }
        if (focus && !TextUtils.isEmpty(edit_text.text)) {
            clear.visibility = View.VISIBLE
        } else if (!focus || TextUtils.isEmpty(edit_text.text)) {
            clear.visibility = View.GONE
        }
    }

    @OnCheckedChanged(R.id.show)
    fun togglePassword(view: CompoundButton, checked: Boolean) {
        edit_text.requestFocus()
        ViewUtils.showPwd(edit_text, checked)
    }

    @OnClick(R.id.clear)
    fun clearText() {
        if (!showClear){
            return
        }
        edit_text.text.clear()
    }

    /**
     * Adds a TextWatcher to the list of those whose methods are called
     * whenever this TextView's text changes.
     *
     *
     * In 1.0, the [TextWatcher.afterTextChanged] method was erroneously
     * not called after [.setText] calls.  Now, doing [.setText]
     * if there are any text changed listeners forces the buffer type to
     * Editable if it would not otherwise be and does call this method.
     */
    fun addTextChangedListener(watcher: TextWatcher) {
        edit_text.addTextChangedListener(watcher)
    }

    /**
     * Removes the specified TextWatcher from the list of those whose
     * methods are called
     * whenever this TextView's text changes.
     */
    fun removeTextChangedListener(watcher: TextWatcher) {
        edit_text.removeTextChangedListener(watcher)
    }

    fun getEditer() = this.edit_text!!

    fun getEditText(): EditText = this.edit_text


}