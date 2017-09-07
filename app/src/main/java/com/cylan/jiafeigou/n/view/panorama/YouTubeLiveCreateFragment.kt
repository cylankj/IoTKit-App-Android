package com.cylan.jiafeigou.n.view.panorama

import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import kotlinx.android.synthetic.main.fragment_youtube_create_live.*

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
        custom_toolbar.setBackAction { onBackPressed() }
        custom_toolbar.setRightAction { createLiveEvent() }
    }

    private var title: String? = null
    private var description: String? = null
    private var startTime: Long = 0
    private var endTime: Long = 0

    private fun createLiveEvent() {
        presenter.createLiveBroadcast(title, description, startTime, endTime)
    }

    companion object {
        fun newInstance(): YouTubeLiveCreateFragment {
            val fragment = YouTubeLiveCreateFragment()

            return fragment
        }
    }
}