package com.cylan.jiafeigou.n.view.panorama

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.wrapper.BaseFragment

/**
 * Created by yanzhendong on 2017/9/6.
 */
class YouTubeLiveCreateFragment : BaseFragment<YouTubeLiveCreateContract.Presenter>(), YouTubeLiveCreateContract.View {
    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun getContentViewID(): Int {
        return R.layout.fragment_youtube_create_live
    }

    companion object {
        fun newInstance(): YouTubeLiveCreateFragment {
            val fragment = YouTubeLiveCreateFragment()

            return fragment
        }
    }
}