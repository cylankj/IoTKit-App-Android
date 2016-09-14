package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativesFriendsContract;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineRelativesandFriendsPresenterImp implements MineRelativesFriendsContract.Presenter {

    private ArrayList<SuggestionChatInfoBean> list;

    public MineRelativesandFriendsPresenterImp(MineRelativesFriendsContract.View view) {
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }


    @Override
    public ArrayList<SuggestionChatInfoBean> initAddRequestData() {
        if (list == null) {
            list = new ArrayList<SuggestionChatInfoBean>();
        }
        SuggestionChatInfoBean emMessage = new SuggestionChatInfoBean("我是小小姨", 1, System.currentTimeMillis() + "");
        emMessage.setName("乔帮主");
        emMessage.setShowAcceptButton(true);
        list.add(emMessage);
        return list;
    }

    @Override
    public ArrayList<SuggestionChatInfoBean> initRelativatesAndFriendsData() {
        ArrayList list = new ArrayList<SuggestionChatInfoBean>();
        for (int i = 0; i < 9; i++) {
            SuggestionChatInfoBean emMessage = new SuggestionChatInfoBean("1388383843" + i, 1, System.currentTimeMillis() + "");
            emMessage.setName("阿三" + i);
            list.add(emMessage);
        }
        return list;
    }

    @Override
    public void addItems(SuggestionChatInfoBean bean) {

    }
}
