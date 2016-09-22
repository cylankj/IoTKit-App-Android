package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativesFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.n.view.adapter.AddRelativesAndFriendsAdapter;
import com.cylan.jiafeigou.n.view.adapter.RelativesAndFriendsAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineRelativesandFriendsPresenterImp extends AbstractPresenter<MineRelativesFriendsContract.View> implements MineRelativesFriendsContract.Presenter {

    private ArrayList<SuggestionChatInfoBean> list;

    public MineRelativesandFriendsPresenterImp(MineRelativesFriendsContract.View view) {
        super(view);
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

        SuggestionChatInfoBean emMessage2 = new SuggestionChatInfoBean("我是大大姨", 1, System.currentTimeMillis() + "");
        emMessage2.setName("张无忌");
        emMessage2.setShowAcceptButton(true);
        list.add(emMessage);
        list.add(emMessage2);
        sortList(list);
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
    public void addItems(SuggestionChatInfoBean bean, ArrayList<SuggestionChatInfoBean> list, RecyclerView.Adapter adapter) {
        bean.setShowAcceptButton(false);
        list.add(bean);
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean checkAddRequestOutTime(SuggestionChatInfoBean bean) {
        long oneMount = 30*24*60*60*1000L;
        return (System.currentTimeMillis() - Long.parseLong(bean.getTime())) > oneMount;
    }

    @Override
    public void doAddRequestClick(final int position, final ArrayList<SuggestionChatInfoBean> list, final AddRelativesAndFriendsAdapter relativesAndFriendsAddAdapter,
                                  final ArrayList<SuggestionChatInfoBean> friendList, final RelativesAndFriendsAdapter relativesAndFriendsAdapter) {

        if(checkAddRequestOutTime(list.get(position))){
            //请求过期
            AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
            builder.setMessage("当前消息已过期，是否向对方发送添加好友验证？");
            builder.setPositiveButton("发送", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    list.remove(position);
                    relativesAndFriendsAddAdapter.notifyDataSetChanged();
                    ToastUtil.showToast(getView().getContext(),"请求已发送"+position);
                    //TODO
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }else {
            //请求未过期
            addItems(list.get(position),friendList,relativesAndFriendsAdapter);
            list.remove(position);
            relativesAndFriendsAddAdapter.notifyDataSetChanged();
            ToastUtil.showToast(getView().getContext(),"添加成功");
        }

    }

    /**
     * desc：集合的排序
     * @param list
     * @return
     */
    public ArrayList<SuggestionChatInfoBean> sortList(ArrayList<SuggestionChatInfoBean> list){
        Comparator<SuggestionChatInfoBean> comparator = new Comparator<SuggestionChatInfoBean>() {
            @Override
            public int compare(SuggestionChatInfoBean lhs, SuggestionChatInfoBean rhs) {
                long oldTime = Long.parseLong(rhs.getTime());
                long newTime = Long.parseLong(lhs.getTime());
                return (int) (newTime - oldTime);
            }
        };
        Collections.sort(list,comparator);
        return list;
    }


}
