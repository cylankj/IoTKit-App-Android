package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.widget.Toast;

import com.cylan.jiafeigou.n.mvp.contract.mine.SuggestionChatContract;
import com.cylan.jiafeigou.n.mvp.model.MineMessageBean;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/8/30
 * 描述：
 */
public class SuggestionPresenterImp implements SuggestionChatContract.Presenter {

    private ArrayList<MineMessageBean> list;
    private int i = 0;
    private Context context;
    private SuggestionChatContract.View view;

    public SuggestionPresenterImp(SuggestionChatContract.View view, Context context) {
        view.setPresenter(this);
        this.context = context;
    }

    @Override
    public ArrayList<MineMessageBean> initChatData() {

        list = new ArrayList<MineMessageBean>();

        MineMessageBean emMessage = new MineMessageBean("我们一直在聆听，欢迎您提出宝贵的意见", 1, System.currentTimeMillis() + "");

        list.add(emMessage);

        return list;
    }

    @Override
    public void addChatItem(MineMessageBean emMessage) {
        list.add(emMessage);
    }

    @Override
    public MineMessageBean makeEMMessageBean(String content, int type, String time) {
        return new MineMessageBean(content, type, time);
    }

    @Override
    public void showToast() {
        Toast.makeText(context, "输入内容不能少于10个字", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void clearChatList(ArrayList<MineMessageBean> list) {
        list.clear();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public MineMessageBean testServerData(long times) {

        if (i == 2) {
            i = 0;
        }

        ArrayList<MineMessageBean> list = new ArrayList<>();

        MineMessageBean emMessage1 = new MineMessageBean("你的反馈已收到，我们将尽快回复", 1, times + "");
        MineMessageBean emMessage2 = new MineMessageBean("您好，PC客户端已经实现视频下载，请 在加菲够官网——服务中心下载安装。" +
                "感谢您使用加菲狗产品，祝您生活愉快！", 1, times + "");
        list.add(emMessage1);
        list.add(emMessage2);
        return list.get(i++);
    }

}
