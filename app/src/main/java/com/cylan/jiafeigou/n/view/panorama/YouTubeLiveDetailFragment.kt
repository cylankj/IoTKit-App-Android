package com.cylan.jiafeigou.n.view.panorama

import android.os.Bundle
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.base.wrapper.BasePresenter

/**
 * Created by yanzhendong on 2017/9/7.
 */
class YouTubeLiveDetailFragment : BaseFragment<BasePresenter<JFGView>>() {
    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun getContentViewID(): Int {
        return R.layout.fragment_youtube_detail
    }

    companion object {
        fun newInstance(uuid: String): YouTubeLiveDetailFragment {
            val fragment = YouTubeLiveDetailFragment()
            val argument = Bundle()
            fragment.arguments = argument
            return fragment
        }
    }
}