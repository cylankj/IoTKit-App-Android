package com.cylan.jiafeigou.n.view.cam


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.n.base.IBaseFragment
import com.cylan.jiafeigou.n.mvp.BasePresenter
import com.cylan.jiafeigou.widget.CustomToolbar


/**
 * A simple [Fragment] subclass.
 * Use the [WeekFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WeekFragment : IBaseFragment<BasePresenter>() {


    private var selected: Int = 0

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_week, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var toolbar: CustomToolbar = view?.findViewById(R.id.custom_toolbar) as CustomToolbar
        toolbar.setBackAction({
            activity.supportFragmentManager.popBackStack()
        })
        selected = arguments.getInt("index")
        for (i in 0..8) {
            val layout = (view as ViewGroup).getChildAt(i)
            if (layout is FrameLayout) {
                val cb = layout.getChildAt(1) as? CheckBox ?: continue
                cb.isChecked = (selected shr (7 - i) and 1) == 1
                Log.d("isChecked", "isChecked:" + i + "," + cb.isChecked)
                cb.setOnCheckedChangeListener { _, isChecked ->
                    if (!isChecked && getCount() == 1) {
                        cb.isChecked = true
                    } else {
                        cb.isChecked = isChecked
                        selected = selected xor (1 shl 7 - i)//按位取反
                        Log.d("isChecked", "isChecked:" + i + "," + Integer.toBinaryString(selected))
                    }
                }
            }
        }
    }

    fun getCount(): Int {
        var n = selected
        var c: Int = 0 // 计数器
        while (n > 0) {
            // 循环移位
            c += n and 1
            n = n shr 1
        } // 如果当前位是1，则计数器加1
        return c
    }

    override fun onDetach() {
        cache = selected
        super.onDetach()
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @return A new instance of fragment WeekFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(index: Int): WeekFragment {
            val fragment = WeekFragment()
            val args = Bundle()
            args.putInt("index", index)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
