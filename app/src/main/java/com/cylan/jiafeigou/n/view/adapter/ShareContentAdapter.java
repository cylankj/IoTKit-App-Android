package com.cylan.jiafeigou.n.view.adapter;

import com.cylan.jiafeigou.n.view.adapter.item.ShareContentItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

/**
 * Created by yanzhendong on 2017/5/31.
 */

public class ShareContentAdapter extends ItemAdapter<ShareContentItem> {



    public interface UnShareListener {
        void unShare(ShareContentItem shareContentItem, int position);
    }
}
