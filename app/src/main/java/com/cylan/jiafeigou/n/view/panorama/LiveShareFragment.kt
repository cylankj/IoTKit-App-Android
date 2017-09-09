package com.cylan.jiafeigou.n.view.panorama

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.share.ShareManager
import kotlinx.android.synthetic.main.layout_live_share.*

/**
 * Created by yanzhendong on 2017/9/9.
 */
class LiveShareFragment : DialogFragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.layout_live_share, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewAndListener()
    }

    fun initViewAndListener() {
        tv_share_to_timeline.setOnClickListener {
        }

        tv_share_to_wechat_friends.setOnClickListener {

        }

        tv_share_to_tencent_qq.setOnClickListener {

        }

        tv_share_to_tencent_qzone.setOnClickListener {

        }

        tv_share_to_sina_weibo.setOnClickListener {

        }

        tv_share_to_twitter_friends.setOnClickListener {


        }

        tv_share_to_facebook_friends.setOnClickListener {

        }

        tv_share_to_by_links.setOnClickListener {

        }
    }

    companion object {

        fun newInstance(uuid: String): LiveShareFragment {
            val fragment = LiveShareFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }
}