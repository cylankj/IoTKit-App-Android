package com.cylan.jiafeigou.n.view.panorama

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.TimeUtils
import com.cylan.jiafeigou.widget.pick.adapters.AbstractWheelTextAdapter
import kotlinx.android.synthetic.main.layout_time_picker.*
import java.util.*

/**
 * Created by yanzhendong on 2017/9/8.
 */
class TimePickerFragment : DialogFragment() {

    /**
     * Temporary instance to avoid multiple instantiations.
     */
    private lateinit var calendar: Calendar
    private lateinit var minCalendar: Calendar
    private lateinit var tempCalendar: Calendar
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.layout_time_picker, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewAndListener()
    }

    fun initViewAndListener() {
        calendar = Calendar.getInstance()
        minCalendar = Calendar.getInstance()
        tempCalendar = Calendar.getInstance()

        minCalendar.timeInMillis = arguments.getLong("minTime", minCalendar.timeInMillis)
        title.text = arguments?.getString("title", getString(R.string.Tap1_CameraFun_Timelapse_StartTime))

        isCancelable = false
        time_picker_year.viewAdapter = initYearAdapter()
        time_picker_month_day.viewAdapter = initMonthDayAdapter()
        time_picker_hour.viewAdapter = initHourAdapter()
        time_picker_minute.viewAdapter = initHourAdapter()

        btn_cancel.setOnClickListener { dismiss() }
        btn_ok.setOnClickListener { select() }
    }

    fun select() {
        if (calendar.before(minCalendar)) {
            AppLogger.w("非法的时间")
        } else {
            resultListener?.invoke(calendar.timeInMillis)
            dismiss()
        }
    }

    private var resultListener: ((Long) -> Unit)? = null

    fun onTimePickerResult(listener: (Long) -> Unit) {
        resultListener = listener
    }

    private fun initYearAdapter(): AbstractWheelTextAdapter {
        val adapter = object : AbstractWheelTextAdapter(context) {
            override fun getItemsCount(): Int {
                return 3
            }

            override fun getItemText(index: Int): CharSequence {
                return "${minCalendar.get(Calendar.YEAR) + index}"
            }

        }
        adapter.itemResource = R.layout.item_text_view
        time_picker_year.setInterpolator(AnticipateOvershootInterpolator())
        time_picker_year.visibleItems = 3
        time_picker_year.addChangingListener { _, oldValue, newValue ->
            val day = calendar.get(Calendar.DAY_OF_YEAR)
            calendar.roll(Calendar.YEAR, newValue - oldValue)
            val newDay = calendar.get(Calendar.DAY_OF_YEAR)
            if (day != newDay) {
                time_picker_month_day.invalidateItemsLayout(true)
                time_picker_month_day.currentItem = newDay
            }
        }
        return adapter
    }

    private fun initMonthDayAdapter(): AbstractWheelTextAdapter {
        val adapter = object : AbstractWheelTextAdapter(context) {
            override fun getItemsCount(): Int {
                return calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
            }

            override fun getItemText(index: Int): CharSequence {
                tempCalendar.timeInMillis = calendar.timeInMillis
                tempCalendar.set(Calendar.DAY_OF_YEAR, index + 1)
                return TimeUtils.getDatePickFormat(calendar.timeInMillis, TimeZone.getDefault())
            }

        }
        adapter.itemResource = R.layout.item_text_view
        time_picker_month_day.setInterpolator(AnticipateOvershootInterpolator())
        time_picker_month_day.visibleItems = 3
        time_picker_month_day.addChangingListener { _, oldValue, newValue ->
            calendar.roll(Calendar.DAY_OF_YEAR, newValue - oldValue)
        }
        return adapter
    }


    private fun initHourAdapter(): AbstractWheelTextAdapter {
        val adapter = object : AbstractWheelTextAdapter(context) {
            override fun getItemsCount(): Int {
                return 24
            }

            override fun getItemText(index: Int): CharSequence {
                return String.format(" %02d ", index)
            }

        }
        adapter.itemResource = R.layout.item_text_view
        time_picker_hour.setInterpolator(AnticipateOvershootInterpolator())
        time_picker_hour.visibleItems = 3
        time_picker_hour.addChangingListener { _, oldValue, newValue ->
            calendar.roll(Calendar.HOUR_OF_DAY, newValue - oldValue)
        }
        return adapter
    }

    private fun initMinuteAdapter(): AbstractWheelTextAdapter {
        val adapter = object : AbstractWheelTextAdapter(context) {
            override fun getItemsCount(): Int {
                return 60
            }

            override fun getItemText(index: Int): CharSequence {
                return String.format(" %02d ", index)
            }

        }
        adapter.itemResource = R.layout.item_text_view
        time_picker_minute.setInterpolator(AnticipateOvershootInterpolator())
        time_picker_minute.visibleItems = 3
        time_picker_minute.addChangingListener { _, oldValue, newValue ->
            calendar.roll(Calendar.MINUTE, newValue - oldValue)
        }
        return adapter
    }

    companion object {
        fun newInstance(uuid: String, title: String = "", minTime: Long): TimePickerFragment {
            val fragment = TimePickerFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putString("title", title)
            argument.putLong("minTime", minTime)
            fragment.arguments = argument
            return fragment
        }
    }
}