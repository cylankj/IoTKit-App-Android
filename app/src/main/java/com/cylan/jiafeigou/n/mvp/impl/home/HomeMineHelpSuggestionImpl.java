package com.cylan.jiafeigou.n.mvp.impl.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineHelpSuggestionContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineHelpSuggestionBean;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/22 11:05
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMineHelpSuggestionImpl extends AbstractPresenter<HomeMineHelpSuggestionContract.View>
        implements HomeMineHelpSuggestionContract.Presenter {

    Subscription subscription;
    private ArrayList<MineHelpSuggestionBean> list;
    private Context context;

    private HomeMineHelpSuggestionImpl(HomeMineHelpSuggestionContract.View view,Context context) {
        super(view);
        this.context = context;
        view.setPresenter(this);
    }

    private ArrayList<MineHelpSuggestionBean> testData() {
        list = new ArrayList<>();
        String server = "亲爱的用户,客户端将于2016年4月1日23:00至00:00进行系统维护升级," +
                "期间对设备正常使用将会造成一定影响,对您造成的不便之处敬请谅解。再次感谢您对加菲狗的支持！";
        String client = "希望你们会做视频下载功能，非常实用呢。";
        for (int i = 0; i < 2; i++) {
            MineHelpSuggestionBean bean = new MineHelpSuggestionBean();
            if (i == 0) {
                bean.setType(0);
                bean.setText(server);
                bean.setIcon(R.drawable.pic_head);
                bean.setIsShowTime(true);
                list.add(bean);
            } else {
                bean.setType(1);
                bean.setText(client);
                bean.setIcon(R.drawable.img_head);
                bean.setIsShowTime(true);
                list.add(bean);
            }
        }
        return list;
    }

    @Override
    public void start() {
        subscription = Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Object, ArrayList<MineHelpSuggestionBean>>() {
                    @Override
                    public ArrayList<MineHelpSuggestionBean> call(Object o) {
                        return testData();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<MineHelpSuggestionBean>>() {
                    @Override
                    public void call(ArrayList<MineHelpSuggestionBean> beanArrayList) {
                        getView().onTalkList(beanArrayList);
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }

    /**
     * 添加条目
     */
    @Override
    public void addItemOfList() {

    }

    /**
     * 清空所有会话
     */
    @Override
    public void onClearAllTalk() {
    }
}
