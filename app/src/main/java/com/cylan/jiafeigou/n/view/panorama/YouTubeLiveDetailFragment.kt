package com.cylan.jiafeigou.n.view.panorama

import android.os.Bundle
import android.text.TextUtils
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.rtmp.youtube.util.EventData
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.cylan.jiafeigou.utils.TimeUtils
import com.google.api.client.json.jackson2.JacksonFactory
import kotlinx.android.synthetic.main.fragment_youtube_detail.*

/**
 * Created by yanzhendong on 2017/9/7.
 */
class YouTubeLiveDetailFragment : BaseFragment<BasePresenter<JFGView>>() {
    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun getContentViewID(): Int {
        return R.layout.fragment_youtube_detail
    }

    private var youtubeEvent: EventData? = null
        private set
        get() {
            if (field == null) {
                val broadcast = PreferencesUtils.getString(JConstant.YOUTUBE_PREF_CONFIGURE+":"+uuid, null)
                if (!TextUtils.isEmpty(broadcast)) {
                    field = JacksonFactory.getDefaultInstance().fromString(broadcast, EventData::class.java)
                }
            }
            return field
        }

    override fun initViewAndListener() {
        super.initViewAndListener()
        custom_toolbar.setBackAction { activity.onBackPressed() }
        custom_toolbar.setRightAction { activity.onBackPressed() }

        youtube_detail_title.text = youtubeEvent?.title ?: getString(R.string.LIVE_DETAIL_DEFAULT_CONTENT)
        youtube_detail_description.text = youtubeEvent?.event?.snippet?.description ?: getString(R.string.LIVE_DETAIL_DEFAULT_CONTENT)
        if (youtubeEvent?.event?.snippet?.scheduledStartTime == null) {
            youtube_detail_start_time.subTitle = getString(R.string.NO_SET)
        } else {
            youtube_detail_start_time.subTitle = TimeUtils.getYMDHM(youtubeEvent!!.event.snippet.scheduledStartTime.value)
        }
        if (youtubeEvent?.event?.snippet?.actualEndTime?.value == null) {
            youtube_detail_end_time.subTitle = getString(R.string.NO_SET)
        } else {
            youtube_detail_end_time.subTitle = TimeUtils.getYMDHM(youtubeEvent!!.event.snippet.actualEndTime.value)
        }
    }

    companion object {
        fun newInstance(uuid: String): YouTubeLiveDetailFragment {
            val fragment = YouTubeLiveDetailFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }
}