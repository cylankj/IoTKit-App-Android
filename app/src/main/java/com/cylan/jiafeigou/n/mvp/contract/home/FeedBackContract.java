package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.cache.db.module.FeedBackBean;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.util.List;

import rx.Observable;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/22 10:56
 */
public interface FeedBackContract {

    interface View extends BaseView {
        /**
         * 初始化显示列表
         *
         * @param list
         */
        void initList(List<FeedBackBean> list);

        void updateItem(FeedBackBean bean);

        void appendList(List<FeedBackBean> list);


    }

    interface Presenter extends BasePresenter {


        /**
         * 清空记录
         */
        void onClearAllTalk();

        /**
         * 保存到本地数据库
         *
         * @param bean
         */
        void saveIntoDb(FeedBackBean bean);

        /**
         * 检测是否超过2分钟
         *
         * @param time
         * @return
         */
        boolean checkOver20Min(long time);

        /**
         * 上传意见反馈
         */
        Observable<Boolean> sendFeedBack(FeedBackBean bean);

        /**
         * 数据库中删除一条数据
         *
         * @param bean
         */
        void deleteItemFromDb(FeedBackBean bean);

    }
}
