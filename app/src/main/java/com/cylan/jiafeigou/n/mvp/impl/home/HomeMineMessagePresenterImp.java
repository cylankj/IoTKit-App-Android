package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;

import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineMessageContract;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class HomeMineMessagePresenterImp implements HomeMineMessageContract.Presenter {

    private ArrayList list;
    private HomeMineMessageContract.View view;

    public HomeMineMessagePresenterImp(HomeMineMessageContract.View view) {
        this.view = view;
    }

    @Override
    public ArrayList<SuggestionChatInfoBean> initMessageData() {

        list = new ArrayList<SuggestionChatInfoBean>();

        SuggestionChatInfoBean emMessage = new SuggestionChatInfoBean("亲爱的用户,客户端将进行系统维护升级,期间对设备正常使用将会造成一定影响，对您造成的不便之处敬请谅解。再次感谢您对加菲狗的支持！",1,System.currentTimeMillis()+"");

        list.add(emMessage);

        return list;

    }

    @Override
    public void addMessageItem() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
