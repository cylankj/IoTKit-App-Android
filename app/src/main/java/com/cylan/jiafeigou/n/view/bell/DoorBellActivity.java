package com.cylan.jiafeigou.n.view.bell;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.ActivityResultContract;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.impl.ActivityResultPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.bell.DBellHomePresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.utils.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DoorBellActivity extends BaseFullScreenFragmentActivity
        implements DoorBellHomeContract.View,
        ActivityResultContract.View {

    private static final String tag = "DoorBellActivity";
    @BindView(R.id.tv_top_bar_left)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    private DoorBellHomeContract.Presenter presenter;
    private ActivityResultContract.Presenter activityResultPresenter;
    private WeakReference<BellSettingFragment> fragmentWeakReference;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door_bell);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        initToolbar();
        initSomething();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (presenter == null)
            presenter = new DBellHomePresenterImpl(this);
        presenter.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (presenter != null)
            presenter.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activityResultPresenter != null)
            activityResultPresenter.stop();
    }

    private void initSomething() {
        if (activityResultPresenter == null)
            activityResultPresenter = new ActivityResultPresenterImpl(this);
        activityResultPresenter.start();
    }

    private void initToolbar() {
        imgVTopBarCenter.setText("");
        ViewUtils.setViewMarginStatusBar(fLayoutTopBarContainer);
    }

    @OnClick({R.id.tv_top_bar_left, R.id.imgv_toolbar_right})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgv_toolbar_right:
                ViewUtils.deBounceClick(v);
                initSettingFragment();
                BellSettingFragment fragment = fragmentWeakReference.get();
                new BellSettingPresenterImpl(fragment);
                getSupportFragmentManager().beginTransaction()
                        //如果需要动画，可以把动画添加进来
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(fragment.getClass().getSimpleName())
                        .commit();
                break;
            case R.id.tv_top_bar_left:
                onBackPressed();
                break;
        }
    }

    private void initSettingFragment() {
        if (fragmentWeakReference == null || fragmentWeakReference.get() == null) {
            fragmentWeakReference = new WeakReference<>(BellSettingFragment.newInstance(new Bundle()));
        }
        Bundle bundle = getIntent().getBundleExtra(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        if (bundle != null) {
            fragmentWeakReference.get().setArguments(bundle);
        } else {
            AppLogger.d("item bundle not found");
        }
    }

    @Override
    public void onBackPressed() {
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        finishExt();
    }


    @Override
    public void onLoginState(int state) {

    }

    @Override
    public void onRecordsListRsp(ArrayList<BellCallRecordBean> beanArrayList) {

    }

    @Override
    public void onActivityResult(RxEvent.ActivityResult result) {
        if (result == null || result.bundle == null)
            return;
        if (result.bundle.containsKey(JConstant.KEY_ACTIVITY_RESULT_CODE)) {
            final int resultCode = result.bundle.getInt(JConstant.KEY_ACTIVITY_RESULT_CODE);
            switch (resultCode) {
                case JConstant.RESULT_CODE_REMOVE_ITEM:
//                    activityResultPresenter.setActivityResult(result);
//                    popAllFragmentStack();
                    finishExt();
                    break;
            }
        }
    }

    @Override
    public void setPresenter(DoorBellHomeContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
