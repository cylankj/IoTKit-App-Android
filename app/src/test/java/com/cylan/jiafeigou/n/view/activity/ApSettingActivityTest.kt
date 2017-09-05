package com.cylan.jiafeigou.n.view.activity


import android.text.InputFilter
import android.text.Spanned
import android.widget.TextView

import com.cylan.jiafeigou.utils.ContextUtils
import com.cylan.jiafeigou.utils.RandomUtils

/**
 * Created by hds on 17-9-5.
 */
class ApSettingActivityTest {


    fun test() {
        val tv = TextView(ContextUtils.getContext())
        tv.setOnClickListener { v ->
            val r = RandomUtils.getRandom(20)
            if (r > 20) {
                println("")
                return@setOnClickListener
            }
            println("")
        }
        tv.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend -> null }, InputFilter { source, start, end, dest, dstart, dend -> null })
    }
}